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

case class Transaction(transactionDate: DateTime, credit: Boolean, amount: BigDecimal, coffeeType: String, milkType: String, client: Long)

object Transaction {

  val simple = {
    get[DateTime]("transaction.transaction_date") ~
      get[Boolean]("transaction.credit") ~
      get[java.math.BigDecimal]("transaction.amount") ~
      get[String]("transaction.coffee_type") ~
      get[String]("transaction.milk_type") ~
      get[Long]("transaction.client_id") map {
      case transactionDate ~ credit ~ amount ~ coffeeType ~ milkType ~ client => Transaction(transactionDate, credit, BigDecimal(amount), coffeeType, milkType, client)
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
          insert into `transaction` values (current_timestamp, {credit}, {amount}, {coffeeType}, {milkType}, {clientId})
          """
        ).on(
          'credit -> transaction.credit,
          'amount -> transaction.amount.doubleValue(),
          'coffeeType -> transaction.coffeeType,
          'milkType -> transaction.milkType,
          'clientId -> transaction.client
        ).executeInsert()
    }
  }

}


