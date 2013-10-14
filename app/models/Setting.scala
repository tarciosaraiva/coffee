package models

import play.api.db._
import play.api.Play.current

import anorm._
import anorm.SqlParser._

import scala.language.postfixOps

import play.api.libs.json.{Json, JsValue, Writes}

case class Setting(id: Pk[Long], key: String, value: String) {

  val settingsWrites = new Writes[Setting] {
    def writes(s: Setting): JsValue = Json.obj(
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
    get[Pk[Long]]("setting.id") ~
      get[String]("setting.key") ~
      get[String]("setting.value") map {
      case id ~ key ~ value => Setting(id, key, value)
    }
  }

  def all: Seq[Setting] = {
    DB.withConnection {
      implicit connection =>
        SQL("select * from setting order by 2").as(Setting.simple *)
    }
  }

  def valueByKey(key: String): Option[String] = {
    DB.withConnection {
      implicit connection =>
        SQL("select value from setting where key = {key}").on('key -> key).as(scalar[String].singleOpt)
    }
  }

  // CRUD

  def create(setting: Setting): Option[Long] = {
    DB.withConnection {
      implicit connection =>
        SQL(
          """
          insert into `setting`(`key`, `value`) values ({key}, {value})
          """
        ).on(
          'key -> setting.key,
          'value -> setting.value
        ).executeInsert()
    }
  }

  def update(setting: Setting): Int = {
    DB.withConnection {
      implicit connection =>
        SQL(
          """
          update `setting` set `key` = {key}, `value` = {value} where id = {id}
          """
        ).on(
          'key -> setting.key,
          'value -> setting.value,
          'id -> setting.id
        ).executeUpdate()
    }
  }

  def delete(id: Long): Int = {
    DB.withConnection {
      implicit connection =>
        SQL("delete from `setting` where id = {id}").on('id -> id).executeUpdate()
    }
  }

}
