package models

import utils.AnormExtension._
import play.api.db._
import play.api.Play.current

import anorm._
import anorm.SqlParser._

import scala.language.postfixOps
import play.api.libs.json._
import org.joda.time._

case class Client(id: Pk[Long], name: String, balance: BigDecimal, email: String, twitter: String, dob: LocalDate) {

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
      get[String]("client.email") ~
      get[String]("client.twitter") ~
      get[LocalDate]("client.dob") map {
      case id ~ name ~ balance ~ email ~ twitter ~ dob => Client(id, name, BigDecimal(balance), email, twitter, dob)
    }
  }

  def indexes: Seq[String] = {
    DB.withConnection {
      implicit connection =>
        SQL("SELECT DISTINCT UPPER(LEFT(c.first_name, 1)) as itm FROM CLIENT C ORDER BY 1").as(str("itm") *)
    }
  }

  def indexes(index: String): Seq[Client] = {
    DB.withConnection {
      implicit connection =>
        SQL("select id, concat(first_name, ' ', last_name) as `name`, balance, email, twitter, dob " +
          "from client where lower(first_name) like lower({name}) order by 1")
          .on('name -> index.concat("%"))
          .as(Client.simple *)
    }
  }

  def findByName(name: String): Seq[Client] = {
    DB.withConnection {
      implicit connection =>
        SQL("select id, concat(first_name, ' ', last_name) AS `name`, balance, email, twitter, dob " +
          "from client where lower(first_name) like lower({name}) order by 1")
          .on('name -> "%".concat(name).concat("%"))
          .as(Client.simple *)
    }
  }

  def findOne(id: Long): Option[Client] = {
    DB.withConnection {
      implicit connection =>
        SQL("select id, concat(first_name, ' ', last_name) AS `name`, balance, email, twitter, dob " +
          "from client where id = {id}")
          .on('id -> id)
          .as(Client.simple.singleOpt)
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

  def create(client: Client): Option[Long] = {
    DB.withConnection {
      implicit connection =>
        val splitName = client.name.split(" ")
        SQL(
          """
          insert into `client`(`first_name`, `last_name`, `balance`, `email`, `twitter`, `dob`)
          values ({first_name}, {last_name}, {credit}, {email}, {twitter}, {dob})
          """
        ).on(
          'first_name -> splitName(0),
          'last_name -> splitName(1),
          'credit -> client.balance.doubleValue(),
          'email -> client.email,
          'twitter -> "@".concat(client.twitter),
          'dob -> client.dob
        ).executeInsert()
    }
  }

}
