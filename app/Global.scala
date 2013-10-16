import anorm.Id
import org.joda.time.{LocalDate, DateTime}
import play.api._
import play.api.mvc._
import models._
import play.filters.gzip._

object Global extends WithFilters(new GzipFilter()) {

  override def onStart(app: Application) {
    InitialData.insert()
  }

}

object InitialData {

  def insert() = {
    if (Client.indexes.isEmpty) {
      Seq(
        Client(Id(1), "Tarcio Saraiva", BigDecimal.valueOf(43.2), "tarcio@gmail.com", "tarciosaraiva", LocalDate.now()),
        Client(Id(2), "Fabiana Fernandes", BigDecimal.valueOf(230), "tarcio@gmail.com", "tarciosaraiva", LocalDate.now()),
        Client(Id(3), "Tatiana Saraiva", BigDecimal.valueOf(10), "tarcio@gmail.com", "tarciosaraiva", LocalDate.now()),
        Client(Id(4), "Dale Young", BigDecimal.valueOf(50), "tarcio@gmail.com", "tarciosaraiva", LocalDate.now()),
        Client(Id(5), "Sadat Rhaman", BigDecimal.valueOf(50), "tarcio@gmail.com", "tarciosaraiva", LocalDate.now()),
        Client(Id(6), "Julia Child", BigDecimal.valueOf(50), "tarcio@gmail.com", "tarciosaraiva", LocalDate.now()),
        Client(Id(7), "Pavi de Alwis", BigDecimal.valueOf(50), "tarcio@gmail.com", "tarciosaraiva", LocalDate.now()),
        Client(Id(8), "Trevor Plant", BigDecimal.valueOf(50), "tarcio@gmail.com", "tarciosaraiva", LocalDate.now())
      ).foreach(Client.create)

      Seq(
        Transaction(DateTime.now(), true, 50, "", 1L),
        Transaction(DateTime.now(), false, -3.4, "Coffee", 1L),
        Transaction(DateTime.now(), false, -3.4, "Coffee", 1L),
        Transaction(DateTime.now(), true, 25, "", 2L)
      ).foreach(Transaction.create)
    }
  }

  if (Setting.all.isEmpty) {
    Seq(
      Setting(Id(1), "logo", "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"),
      Setting(Id(2), "title", "Black Velvet Espresso")
    ).foreach(Setting.create)
  }

}
