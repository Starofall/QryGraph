package controllers

import java.io.FileInputStream
import javax.inject._

import com.google.inject.Inject
import controllers.forms.SetupForm
import models.Tables.{User, _}
import models.{DBEnums, DatabaseAccess}
import org.mindrot.jbcrypt.BCrypt
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc._
import util.HDFS

import scala.concurrent.Future

@Singleton
class Setup @Inject()(implicit val app: play.api.Application, val messagesApi: MessagesApi) extends Controller with I18nSupport with DatabaseAccess {

  import dbConfig.driver.api._
  import util.FutureEnhancements._

  def indexGET = Action { implicit request =>
    Ok(views.html.setup(SetupForm.form))
  }


  def indexPOST = Action.async { implicit request =>
    // @todo disable once setup is done

    // check form
    SetupForm.form.bindFromRequest.fold(
      // Form errors
      formWithErrors => {
        Future(BadRequest(views.html.setup(formWithErrors)))
      },
      // Correct form
      setupForm => {
        val newGlobalSettings = GlobalSetting("1", setupForm.hadoopUser, setupForm.qrygraphFolder, setupForm.fsDefaultName, setupForm.mapredJobTracker)
        db.run(
          DBIO.seq(
            GlobalSettings += newGlobalSettings,
            Users += User("1", setupForm.email, BCrypt.hashpw(setupForm.password, BCrypt.gensalt()), setupForm.firstName, setupForm.lastName, DBEnums.RoleAdmin, None),

            DataSources.insertOrUpdate(DataSource("1","Example","A example database containing 'Most Popular Baby Names by Sex and Mother's Ethnic Group, New York City' from data.gov", setupForm.qrygraphFolder + "/qrygraph-example.csv")),
            DataSourcesColumns.insertOrUpdate(DataSourcesColumn("1a",0,"1","YEAR","chararray")),
            DataSourcesColumns.insertOrUpdate(DataSourcesColumn("1b",1,"1","GENDER","chararray")),
            DataSourcesColumns.insertOrUpdate(DataSourcesColumn("1c",2,"1","ETHNIC","chararray")),
            DataSourcesColumns.insertOrUpdate(DataSourcesColumn("1d",3,"1","NAME","chararray")),
            DataSourcesColumns.insertOrUpdate(DataSourcesColumn("1e",4,"1","COUNT","int")),
            DataSourcesColumns.insertOrUpdate(DataSourcesColumn("1f",5,"1","RANK","int"))
          )
        ).mapAll(_ => {
          // upload the example file to hdfs
          val fileName = "qrygraph-example.csv"
          // first try to get as a stream without "conf/" in path - this should work in dist production mode
          val inputStream = app.resourceAsStream(fileName).getOrElse(new FileInputStream(app.getFile(s"conf/$fileName")))
          HDFS.writeStream(newGlobalSettings,fileName, inputStream)
          Redirect(routes.Auth.loginGET())
        })
      }
    )
  }

}


