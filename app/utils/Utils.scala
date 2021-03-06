package utils

import org.joda.time.format.{DateTimeFormat, DateTimeFormatter}
import anorm.{ToStatement, TypeDoesNotMatch, MetaDataItem, Column}
import org.joda.time._
import java.text.{SimpleDateFormat, NumberFormat}
import models.{Client, Transaction}

object Utils {

  def formatNumber(amount: BigDecimal) = NumberFormat.getCurrencyInstance.format(amount)

  def formatDateTime(dateTime: DateTime) = {
    new SimpleDateFormat("dd MMM h:mm a").format(dateTime.withZone(DateTimeZone.forID("Australia/Melbourne")).toDate)
  }

  def formatLocalDate(localDate: Option[LocalDate]) = {
    localDate match {
      case Some(d) => new SimpleDateFormat("ddMM").format(d.toDate)
      case None => ""
    }
  }

  def isBirthday(birthDate: Option[LocalDate]): Boolean = {
    if (birthDate.isEmpty) return false

    val sdf = new SimpleDateFormat("ddMM")
    val now = sdf.format(LocalDate.now.toDate)
    val birth = sdf.format(birthDate.get.toDate)

    now == birth
  }

  def addTransaction(cid: Long, t: Seq[Transaction], update: Boolean = true) = {
    t.foreach(Transaction.create)
    if (update)
      Client.updateBalance(cid)
  }

}

object AnormExtension {

  val dateFormatGeneration: DateTimeFormatter = DateTimeFormat.forPattern("yyyyMMddHHmmssSS");

  implicit def rowToDateTime: Column[DateTime] = Column.nonNull {
    (value, meta) =>
      val MetaDataItem(qualified, nullable, clazz) = meta
      value match {
        case ts: java.sql.Timestamp => Right(new DateTime(ts.getTime))
        case d: java.sql.Date => Right(new DateTime(d.getTime))
        case str: java.lang.String => Right(dateFormatGeneration.parseDateTime(str))
        case _ => Left(TypeDoesNotMatch("Cannot convert " + value + ":" + value.asInstanceOf[AnyRef].getClass))
      }
  }

  implicit val dateTimeToStatement = new ToStatement[DateTime] {
    def set(s: java.sql.PreparedStatement, index: Int, aValue: DateTime): Unit = {
      s.setTimestamp(index, new java.sql.Timestamp(aValue.withMillisOfSecond(0).getMillis()))
    }
  }

  implicit def rowToLocalDate: Column[LocalDate] = Column.nonNull {
    (value, meta) =>
      val MetaDataItem(qualified, nullable, clazz) = meta
      value match {
        case ts: java.sql.Timestamp => Right(new LocalDate(ts.getTime))
        case d: java.sql.Date => Right(new LocalDate(d.getTime))
        case str: java.lang.String => Right(dateFormatGeneration.parseLocalDate(str))
        case _ => Left(TypeDoesNotMatch("Cannot convert " + value + ":" + value.asInstanceOf[AnyRef].getClass))
      }
  }

  implicit val localDateToStatement = new ToStatement[LocalDate] {
    def set(s: java.sql.PreparedStatement, index: Int, aValue: LocalDate): Unit = {
      s.setTimestamp(index, new java.sql.Timestamp(aValue.toDateTimeAtCurrentTime.withMillisOfSecond(0).getMillis()))
    }
  }

}
