package models

import play.api.db._
import play.api.Play.current

import anorm._
import anorm.SqlParser._

import scala.language.postfixOps
import play.api.libs.json.{Json, JsValue, Writes}

case class Client(id: Pk[Long], name: String, balance: BigDecimal, coffeeType: String, milkType: String) {

  val clientWrites = new Writes[Client] {
    def writes(c: Client): JsValue = Json.obj(
      "name" -> c.name,
      "balance" -> c.balance,
      "coffee" -> c.coffeeType,
      "milk" -> c.milkType
    )
  }

  def toJson = {
    clientWrites.writes(this)
  }

}

object Client {

  val simple = {
    get[Pk[Long]]("client.id") ~
      get[String]("client.name") ~
      get[java.math.BigDecimal]("client.balance") ~
      get[String]("client.coffee_type") ~
      get[String]("client.milk_type") map {
      case id ~ name ~ balance ~ coffeeType ~ milkType => Client(id, name, BigDecimal(balance), coffeeType, milkType)
    }
  }

  def all: Seq[Client] = {
    DB.withConnection {
      implicit connection =>
        SQL("select * from client order by name").as(Client.simple *)
    }
  }

  def indexes: Seq[String] = {
    DB.withConnection {
      implicit connection =>
        SQL("SELECT DISTINCT LEFT(c.name, 1) as itm FROM CLIENT C ORDER BY 1").as(str("itm") *)
    }
  }

  def indexes(index: String): Seq[Client] = {
    DB.withConnection {
      implicit connection =>
        SQL("select * from client where lower(name) like lower({name}) order by name")
          .on('name -> index.concat("%"))
          .as(Client.simple *)
    }
  }

  def findByName(name: String): Seq[Client] = {
    DB.withConnection {
      implicit connection =>
        SQL("select * from client where lower(name) like lower({name}) order by name")
          .on('name -> "%".concat(name).concat("%"))
          .as(Client.simple *)
    }
  }

  def findOne(id: Long): Option[Client] = {
    DB.withConnection {
      implicit connection =>
        SQL("select * from client where id = {id}")
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

  def create(client: Client): Client = {
    DB.withConnection {
      implicit connection =>
        SQL(
          """
          insert into `client`(`name`, `balance`, `coffee_type`, `milk_type`) values ({name}, {credit}, {coffeeType}, {milkType})
          """
        ).on(
          'name -> client.name,
          'credit -> client.balance.doubleValue(),
          'coffeeType -> client.coffeeType,
          'milkType -> client.milkType
        ).executeInsert()

        client

    }
  }

}
