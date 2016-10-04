package views

/**
  * Created by info on 17.04.2016.
  */
object Navigation {

  sealed trait NavButton {
    def name: String

    def route: String
  }
  case object Dashboard extends NavButton {
    def name = "Dashboard"

    def route = controllers.routes.Dashboard.index.url
  }
  case object Queries extends NavButton {
    def name = "Queries"

    def route = controllers.routes.Queries.index.url
  }
  case object Components extends NavButton {
    def name = "Components"

    def route = controllers.routes.Components.index.url
  }
  case object Settings extends NavButton {
    def name = "Settings"

    def route = controllers.routes.Settings.indexGET.url
  }

  def visibleNavButtons(): Seq[NavButton] = {
    Seq(Dashboard, Queries, Components, Settings)
  }
}
