package com.intenthq.doobie

import doobie.hikari.imports._
import doobie.imports._
import org.flywaydb.core.Flyway

import scala.util.Try

object DbContext {

  case class DbConfig(host: String, port: String, dbName: Option[String], username: String, password: String, schema: Option[String]) {
    def url = s"jdbc:postgresql://$host:$port/${dbName.getOrElse("")}${schema.fold("")(s => s"?currentSchema=$s")}"

    override def toString: String = url
  }

  def load(config: DbConfig): Either[Throwable, DbContext] = for {
    _ <- migrateDB(config)
    xa <- createPool(config)
  } yield DbContext(xa, xa.shutdown)

  def migrateDB(config: DbConfig): Either[Throwable, Int] = Try {
    val flyway = new Flyway()
    //For some reason flyway needs the url without the schema in order to work
    flyway.setDataSource(config.copy(schema = None).url, config.username, config.password)
    config.schema.foreach(flyway.setSchemas(_))
    flyway.migrate()
  }.toEither

  private def createPool(config: DbConfig): Either[Throwable, HikariTransactor[IOLite]] =
    HikariTransactor[IOLite]("org.postgresql.Driver", config.url, config.username, config.password)
      .attempt.unsafePerformIO
}

case class DbContext(xa: Transactor[IOLite], shutdown: IOLite[Unit])