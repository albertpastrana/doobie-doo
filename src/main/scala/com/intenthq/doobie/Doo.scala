package com.intenthq.doobie

import doobie.free.connection.ConnectionIO
import doobie.implicits._
import doobie._

object Doo {

  private[doobie] object Q {

    val companyNames: Query0[String] = sql"SELECT name FROM companies ORDER BY name ASC".query

    def createCompany(name: String): Update0 =
      sql"INSERT INTO companies (name) VALUES ($name)".update

    def updateCompanyName(id: CompanyId, name: String): Update0 =
      sql"UPDATE companies SET name = $name WHERE id = $id".update

    val companyTuples: Query0[(String, Option[String])] =
      sql"SELECT name, website FROM companies ORDER BY name ASC".query

    val companyCaseClasses: Query0[Company] =
      sql"SELECT id, name, website FROM companies ORDER BY name ASC".query

    def companyCaseClass(id: CompanyId): Query0[Company] =
      sql"SELECT * FROM companies WHERE id = $id".query

    val jobOffersTuples: Query0[(String, String)] =
      sql"""
        SELECT c.name, j.summary
        FROM companies c, job_offers j
        WHERE c.id = j.company_id
        ORDER BY c.name ASC""".query

    val jobOffersCaseClasses: Query0[JobOffer] =
      sql"""
        SELECT j.id, j.summary, j.description, c.*
        FROM companies c, job_offers j
        WHERE c.id = j.company_id
        ORDER BY c.name ASC""".query

    def createJobOffer(companyId: CompanyId, summary: String, description: String): Update0 =
      sql"""
        INSERT INTO job_offers (company_id, summary, description)
        VALUES ($companyId, $summary, $description)""".update
  }

  def companyNames(implicit dbContext: DbContext): Either[DbError, List[String]] = {
    // Defines a Query0[String], which is a one-column query that maps each returned row to a String
    val query: Query0[String] = sql"SELECT name FROM companies ORDER BY name ASC".query[String]
    // list is a convenience method that streams the results, accumulating them in a List,
    // in this case yielding a ConnectionIO[List[String]]
    val io: ConnectionIO[List[String]] = query.to[List]
    // transact(xa) yields a Task[List[String]] which we run, giving us a normal Scala List[String]
    val result: Either[Throwable, List[String]] = io.transact(dbContext.xa).attempt.unsafeRunSync()
    // We handle the error in here and transform it into our own error type
    result.left.map(DbError.from)
  }


  def companyNamesOneLiner(implicit dbContext: DbContext): Either[DbError, List[String]] =
    Q.companyNames.to[List].performTransact

  def createCompany(name: String)(implicit dbContext: DbContext): Either[DbError, CompanyId] =
    Q.createCompany(name)
      .withUniqueGeneratedKeys[CompanyId]("id")
      .performTransact

  def updateCompanyName(id: CompanyId, name: String)(implicit dbContext: DbContext): Either[DbError, Int] =
    Q.updateCompanyName(id, name).run.performTransact

  def companyTuples(implicit dbContext: DbContext): Either[DbError, List[(String, Option[String])]] =
    Q.companyTuples.to[List].performTransact

  def companyCaseClasses(implicit dbContext: DbContext): Either[DbError, List[Company]] =
    Q.companyCaseClasses.to[List].performTransact

  def companyCaseClass(id: CompanyId)(implicit dbContext: DbContext): Either[DbError, Option[Company]] =
    Q.companyCaseClass(id).option.performTransact

  def jobOffersTuples(implicit dbContext: DbContext): Either[DbError, List[(String, String)]] =
    Q.jobOffersTuples.to[List].performTransact

  def jobOffersCaseClasses(implicit dbContext: DbContext): Either[DbError, List[JobOffer]] =
    Q.jobOffersCaseClasses.to[List].performTransact

  def createJobOffer(companyName: String, offerSummary: String, offerDescription: String)
                    (implicit dbContext: DbContext): Either[DbError, (CompanyId, JobOfferId)] = {
    (for {
      cid <- Q.createCompany(companyName).withUniqueGeneratedKeys[CompanyId]("id")
      joid <- Q.createJobOffer(cid, offerSummary, offerDescription).withUniqueGeneratedKeys[JobOfferId]("id")
    } yield (cid, joid)).performTransact
  }

  implicit class ConnectionIOOps[A](cio: ConnectionIO[A]) {
    def performTransact(implicit dbContext: DbContext): Either[DbError, A] =
      cio.transact(dbContext.xa).attempt.unsafeRunSync.left.map(DbError.from)
  }

}

case class CompanyId(value: Long) extends AnyVal

case class Company(id: CompanyId, name: String, website: Option[String])

case class JobOfferId(value: Long) extends AnyVal

case class JobOffer(id: JobOfferId, summary: String, description: String, company: Company)

case class DbError(cause: Throwable) extends Exception(cause)

object DbError {

  // You could do some better error handling in here and create different types of
  // errors depending on the exception and/or the sql state
  def from(tr: Throwable): DbError = DbError(tr)

}