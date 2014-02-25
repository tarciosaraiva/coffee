package models

import play.api.db._
import play.api.Play.current

import anorm._
import anorm.SqlParser._

import scala.language.postfixOps
import play.api.libs.json._
import org.joda.time._
import utils.AnormExtension._

case class Client(id: Pk[Long], name: String, balance: BigDecimal, email: Option[String], twitter: Option[String],
                  dob: Option[LocalDate], lastTransactionAmount: BigDecimal, notified: Boolean, hidden: Boolean) {

  val clientWrites = new Writes[Client] {
    def writes(c: Client): JsValue = Json.obj(
      "name" -> c.name,
      "balance" -> c.balance,
      "email" -> c.email,
      "twitter" -> c.twitter,
      "dob" -> c.dob
    )
  }

  def toJson = {
    clientWrites.writes(this)
  }

}

object Client {

  val simple = {
    get[Pk[Long]]("client.id") ~
      get[String]("name") ~
      get[java.math.BigDecimal]("client.balance") ~
      get[Option[String]]("client.email") ~
      get[Option[String]]("client.twitter") ~
      get[Option[LocalDate]]("client.dob") ~
      get[java.math.BigDecimal]("last_transaction_amount") ~
      get[Boolean]("notified") ~
      get[Boolean]("hidden") map {
      case id ~ name ~ balance ~ email ~ twitter ~ dob ~ last_transaction_amount ~ notified ~ hidden => {
        Client(id, name, BigDecimal(balance), email, twitter, dob, BigDecimal(last_transaction_amount), notified, hidden)
      }
    }
  }

  def indexes: Seq[String] = {
    DB.withConnection {
      implicit connection =>
        SQL("SELECT DISTINCT UPPER(LEFT(c.first_name, 1)) as itm FROM CLIENT C WHERE C.hidden = false ORDER BY 1").as(str("itm") *)
    }
  }

  def indexes(index: String): Seq[Client] = {
    DB.withConnection {
      implicit connection =>
        SQL("select id, concat(first_name, ' ', last_name) as name, balance, email, twitter, dob, " +
          "coalesce((select abs(amount) from transaction where client_id = client.id and credit = false " +
          "order by transaction_date desc limit 1), 0) as last_transaction_amount, notified, hidden " +
          "from client where lower(first_name) like lower({name}) and hidden = false order by 2")
          .on('name -> index.concat("%"))
          .as(Client.simple *)
    }
  }

  def findByName(name: String, showHidden: Boolean): Seq[Client] = {
    DB.withConnection {
      implicit connection =>
        SQL("select id, concat(first_name, ' ', last_name) AS name, balance, email, twitter, dob, " +
          "coalesce((select abs(amount) from transaction where client_id = client.id and credit = false " +
          "order by transaction_date desc limit 1), 0) as last_transaction_amount, notified, hidden " +
          "from client where lower(first_name) like lower({name}) and (hidden = false or hidden = {showHidden}) order by 2")
          .on(
          'name -> "%".concat(name).concat("%"),
          'showHidden -> showHidden)
          .as(Client.simple *)
    }
  }

  def findOne(id: Long): Option[Client] = {
    DB.withConnection {
      implicit connection =>
        SQL("select id, concat(first_name, ' ', last_name) AS name, balance, email, twitter, dob, " +
          "coalesce((select abs(amount) from transaction where client_id = client.id and credit = false " +
          "order by transaction_date desc limit 1), 0) as last_transaction_amount, notified, hidden " +
          "from client where id = {id}")
          .on('id -> id)
          .as(Client.simple.singleOpt)
    }
  }

  def findAllWithEmailAndSmallBalance: Seq[Client] = {
    DB.withConnection {
      implicit connection =>
        SQL("select id, concat(first_name, ' ', last_name) AS name, balance, email, twitter, dob, " +
          "0.0 as last_transaction_amount, notified, hidden " +
          "from client where email is not null and balance < 5 and notified = false")
          .as(Client.simple *)
    }
  }

  def updateBalance(id: Long): Int = {
    DB.withConnection {
      implicit connection =>
        SQL("update client set balance = (select sum(amount) from transaction where client_id = {id}) where id = {id}")
          .on('id -> id)
          .executeUpdate()
    }
  }

  def updateDetails(id: Long, details: (String, Option[String], Option[String], Option[LocalDate])): Int = {
    DB.withConnection {
      implicit connection =>
        val splitName = details._1.split(" ")
        SQL("update client set first_name = {fn}, last_name = {ln}, email = {email}, twitter = {twitter}, dob = {dob} where id = {id}")
          .on(
          'id -> id,
          'fn -> splitName(0),
          'ln -> {
            if (splitName.length == 2) splitName(1) else ""
          },
          'email -> details._2,
          'twitter -> details._3,
          'dob -> details._4
        ).executeUpdate()
    }
  }

  def deleteTransaction(cId: Long, tId: Long): Int = {
    DB.withConnection {
      implicit connection =>
        SQL("delete from transaction where id = {id} and client_id = {client_id}")
          .on('id -> tId)
          .on('client_id -> cId)
          .executeUpdate()
    }
  }

  def markNotified(id: Long): Int = {
    DB.withConnection {
      implicit connection =>
        SQL("update client set notified = not(notified) where id = {id}")
          .on('id -> id)
          .executeUpdate()
    }
  }

  def toggleVisibility(id: Long): Int = {
    DB.withConnection {
      implicit connection =>
        SQL("update client set hidden = not(hidden) where id = {id}")
          .on('id -> id)
          .executeUpdate()
    }
  }

  def delete(id: Long): Int = {
    DB.withConnection {
      implicit connection =>
        SQL("delete from client where id = {id}")
          .on('id -> id)
          .executeUpdate()
    }
  }

  def findAllTopUps: List[(String, LocalDate, java.math.BigDecimal)] = {
    DB.withConnection {
      implicit connection =>
        SQL("select concat(c.first_name, ' ', c.last_name) as name, t.transaction_date, t.amount from client c, transaction t " +
          "where t.client_id = c.id " +
          "and t.amount > 0 " +
          "and t.transaction_date between {before} and {after}" +
          "order by t.transaction_date desc")
          .on(
          'before -> LocalDate.now().minusDays(3),
          'after -> LocalDate.now()
        )
          .as(str("name") ~ get[LocalDate]("transaction_date") ~ get[java.math.BigDecimal]("amount") map (flatten) *)
    }
  }

  def create(client: Client): Option[Long] = {
    DB.withConnection {
      implicit connection =>
        val splitName = client.name.split(" ")
        SQL(
          """
          insert into client(first_name, last_name, balance, email, twitter, dob)
          values ({first_name}, {last_name}, {credit}, {email}, {twitter}, {dob})
          """
        ).on(
          'first_name -> splitName(0),
          'last_name -> {
            if (splitName.length == 2) splitName(1) else ""
          },
          'credit -> client.balance.doubleValue(),
          'email -> client.email,
          'twitter -> client.twitter,
          'dob -> client.dob
        ).executeInsert()
    }
  }

}
