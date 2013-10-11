import anorm.Id
import org.joda.time.DateTime
import play.api._
import models._
import models.Enums._
import play.api.mvc.WithFilters
import play.filters.gzip.GzipFilter

object Global extends WithFilters(new GzipFilter()) {

  override def onStart(app: Application) {
    InitialData.insert()
  }

}

object InitialData {

  def insert() = {
    if (Client.indexes.isEmpty) {
      Seq(
        Client(Id(1), "Tarcio Saraiva", BigDecimal.valueOf(43.2), coffees(0), milks(1)),
        Client(Id(2), "Fabiana Fernandes", BigDecimal.valueOf(230), coffees(10), milks(1)),
        Client(Id(3), "Tatiana Saraiva", BigDecimal.valueOf(10), coffees(1), milks(0)),
        Client(Id(4), "Dale Young", BigDecimal.valueOf(50), coffees(0), milks(0)),
        Client(Id(5), "Sadat Rhaman", BigDecimal.valueOf(50), coffees(0), milks(0)),
        Client(Id(6), "Julia Child", BigDecimal.valueOf(50), coffees(0), milks(0)),
        Client(Id(7), "Pavi de Alwis", BigDecimal.valueOf(50), coffees(0), milks(0)),
        Client(Id(8), "Trevor Plant", BigDecimal.valueOf(50), coffees(0), milks(0))
      ).foreach(Client.create)

      Seq(
        Transaction(DateTime.now(), true, 50, coffees(0), milks(1), 1L),
        Transaction(DateTime.now(), false, -3.4, coffees(1), milks(0), 1L),
        Transaction(DateTime.now(), false, -3.4, coffees(0), milks(0), 1L),
        Transaction(DateTime.now(), true, 25, coffees(2), milks(1), 2L)
      ).foreach(Transaction.create)
    }
  }

}
