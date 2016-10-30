package views

import models.DBEnums
import models.Tables.User

/**
  * Created by info on 17.04.2016.
  */
object Navigation {

  sealed trait NavButton {
    val name: String

    def route: String
  }

  case object Queries extends NavButton {
    val name = "Queries"

    def route = controllers.routes.Queries.indexGET.url
  }
  case object Components extends NavButton {
    val name = "Components"

    def route = controllers.routes.Components.indexGET.url
  }
  case object Settings extends NavButton {
    val name = "Settings"

    def route = controllers.routes.Settings.indexGET.url
  }

  def visibleNavButtons(user: User): Seq[NavButton] = {
    if (user.userRole == DBEnums.RoleAdmin) {
      Seq(Queries, Components, Settings)
    } else {
      Seq(Queries, Components)
    }
  }
}
