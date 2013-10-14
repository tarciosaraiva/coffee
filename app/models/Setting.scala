package models

import anorm.{~, Pk}
import play.api.libs.json.{Json, JsValue, Writes}
import anorm.SqlParser._
import scala._
import anorm.~

class Setting(id: Pk[Long], key: String, value: String) {

  val settingsWrites = new Writes[Settings] {
    def writes(s: Settings): JsValue = Json.obj(
      "key" -> s.key,
      "value" -> s.value
    )
  }

  def toJson = {
    settingsWrites.writes(this)
  }
}

object Setting {

  val simple = {
    get[Pk[Long]]("settings.id") ~
      get[String]("name") ~
      get[java.math.BigDecimal]("client.balance") ~
      get[String]("client.coffee_type") ~
      get[String]("client.milk_type") map {
      case id ~ name ~ balance ~ coffeeType ~ milkType => Client(id, name, BigDecimal(balance), coffeeType, milkType)
    }
  }

}
