package com.intenthq.doobie

import java.util.UUID

import cats.effect.IO
import com.intenthq.doobie.DbContext.DbConfig
import doobie.implicits._
import doobie._
import doobie.specs2.analysisspec.IOChecker
import doobie.util.transactor.Transactor
import org.specs2.mutable.Specification
import org.specs2.specification.AfterAll

import scala.util.Properties

class DbSpecification extends Specification with IOChecker with AfterAll {

  protected val schema = s"test-${UUID.randomUUID().toString}"

  override def transactor: Transactor[IO] = dbContext.xa

  override def afterAll(): Unit = {
    Update0(s"""DROP SCHEMA "$schema" CASCADE""", None).run.transact(dbContext.xa).attempt.unsafeRunSync()
      .fold(err => println(s"Error dropping schema $schema $err"), _ => println(s"Schema $schema dropped"))
    dbContext.shutdown()
  }

  // These default values will work if you are starting the postgres docker
  // container using the default params: https://hub.docker.com/_/postgres/
  // Locally you can modify them using something like https://direnv.net/
  private lazy val dbConfig = DbConfig(
    Properties.envOrElse("DB_HOST", "localhost"),
    Properties.envOrElse("DB_PORT", "5433"),
    Properties.envOrNone("DB_NAME"),
    Properties.envOrElse("DB_USERNAME", "postgres"),
    Properties.envOrElse("DB_PASSWORD", "mysecretpassword"),
    Some(schema))

  lazy implicit val dbContext: DbContext = DbContext.load(dbConfig) match {
    case Right(ctx) => ctx
    case Left(err) =>
      println(s"Unable to load test db context $err")
      throw err
  }

}
