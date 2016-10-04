package controllers

import javax.inject._

import com.google.inject.Inject
import models.{DatabaseAccess, LoginAccess}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc._
import util.Actions._

@Singleton
class Dashboard @Inject()(implicit val app: play.api.Application) extends Controller with DatabaseAccess with LoginAccess {

  import dbConfig.driver.api._


  def index() = AuthedAction(app).async { implicit request =>
    val user = request.user

    // count the job status
    val countQuery = for {
      (name, c) <- Tables.PigQueries.groupBy(_.authorizationStatus)
    } yield name -> c.countDistinct


    val ownQuerys = for {
      queries <- Tables.PigQueries join Tables.QueryAccessRights on (_.id === _.queryId)
      if queries._2.userId === user.id
    } yield queries._1

    for (
      jobStatusCounter <- runQuery(countQuery);
      myQ: Seq[Tables.PigQuery] <- runQuery(ownQuerys)
    ) yield Ok(views.html.dashboard(user, jobStatusCounter.toMap, myQ))

  }

}



