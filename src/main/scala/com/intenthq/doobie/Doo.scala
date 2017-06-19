package com.intenthq.doobie

import doobie.free.connection.ConnectionIO
import doobie.imports._

object Doo {

  private[doobie] object Q {

    val companyNames: Query0[String] =
      sql"SELECT name FROM companies ORDER BY name ASC".query

  }

    def companyNames(implicit dbContext: DbContext): Either[DbError, List[String]] = {
      // Defines a Query0[String], which is a one-column query that maps each returned row to a String
      val query: Query0[String] = sql"SELECT name FROM companies ORDER BY name ASC".query[String]
      // list is a convenience method that streams the results, accumulating them in a List,
      // in this case yielding a ConnectionIO[List[String]]
      val io: ConnectionIO[List[String]] = query.list
      // transact(xa) yields a Task[List[String]] which we run, giving us a normal Scala List[String]
      val result: Either[Throwable, List[String]] = io.transact(dbContext.xa).attempt.unsafePerformIO
      // We handle the error in here and transform it into our own error type
      result.left.map(DbError.from)
    }


  def companyNamesOneLiner(implicit dbContext: DbContext): Either[DbError, List[String]] =
    Q.companyNames.list.performTransact

  def createCompany(name: String): Either[DbError, CompanyId] = ???

  def updateCompanyName(id: CompanyId, name: String): Either[DbError, Int] = ???

  def companyTuples: Either[DbError, List[(String, Option[String])]] = ???

  def companyCaseClasses: Either[DbError, List[Company]] = ???

  def companyCaseClass(id: CompanyId): Either[DbError, Option[Company]] = ???

  def jobOffersTuples: Either[DbError, List[(String, String)]] = ???

  def jobOffersCaseClasses: Either[DbError, List[JobOffer]] = ???

  def createJobOffer(companyName: String, offerSummary: String, offerDescription: String): Either[DbError, (CompanyId, JobOfferId)] = ???

  implicit class ConnectionIOOps[A](cio: ConnectionIO[A]) {
    def performTransact(implicit dbContext: DbContext): Either[DbError, A] =
      cio.transact(dbContext.xa).attempt.unsafePerformIO.left.map(DbError.from)
  }

}

case class CompanyId(value: Long) extends AnyVal

case class Company(id: CompanyId, name: String, website: Option[String])

case class JobOfferId(value: Long) extends AnyVal

case class JobOffer(id: JobOfferId, summary: String, description: String, company: Company)

case class DbError(cause: Throwable) extends Exception {
  override def getMessage: String = cause.getMessage

  override def getCause: Throwable = cause
}

object DbError {

  // You could do some better error handling in here and create different types of
  // errors depending on the exception and/or the sql state
  def from(tr: Throwable): DbError = DbError(tr)

}