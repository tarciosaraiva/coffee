package models

import utils.AnormExtension._
import org.joda.time._
import anorm._
import play.api.db._
import play.api.Play.current
import anorm.SqlParser._
import scala._
import anorm.~
import scala.language.postfixOps
import play.api.libs.json.{Json, JsValue, Writes}

case class Transaction(transactionDate: DateTime, credit: Boolean, amount: BigDecimal, notes: Option[String], client: Long) {

  val transactionWrites = new Writes[Transaction] {
    def writes(t: Transaction): JsValue = Json.obj(
      "date" -> t.transactionDate,
      "credit" -> t.credit,
      "amount" -> t.amount,
      "notes" -> t.notes
    )
  }

  def toJson = {
    transactionWrites.writes(this)
  }

}

object Transaction {

  val simple = {
    get[DateTime]("transaction.transaction_date") ~
      get[Boolean]("transaction.credit") ~
      get[java.math.BigDecimal]("transaction.amount") ~
      get[Option[String]]("transaction.notes") ~
      get[Long]("transaction.client_id") map {
      case transactionDate ~ credit ~ amount ~ notes ~ client => Transaction(transactionDate, credit, BigDecimal(amount), notes, client)
    }
  }

  def allByClient(clientId: Long): Seq[Transaction] = {
    DB.withConnection {
      implicit connection =>
        SQL("select * from transaction where client_id = {client_id} order by transaction_date desc")
          .on('client_id -> clientId)
          .as(Transaction.simple *)
    }
  }

  def create(transaction: Transaction) = {
    DB.withConnection {
      implicit connection =>
        SQL(
          """
          insert into `transaction` values (current_timestamp, {credit}, {amount}, {clientId}, {notes})
          """
        ).on(
          'credit -> transaction.credit,
          'amount -> transaction.amount.doubleValue(),
          'notes -> transaction.notes,
          'clientId -> transaction.client
        ).executeInsert()
    }
  }

}


