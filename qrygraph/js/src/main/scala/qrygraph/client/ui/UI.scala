package qrygraph.client.ui

import org.scalajs.jquery.jQuery
import qrygraph.client.actors.ClientState
import qrygraph.shared.data.Node
import qrygraph.shared.pig._

/**
  * the UI objects contains simple wrapper objects to the ui elements we might want to
  * modify in our scala code.
  */
object UI {

  def updateRightEditBar(state: ClientState) = {
    // we need to find the activatedNode in the new/current graph
    state.activatedNodeOption.foreach(n =>
      state.activatedNodeOption = state.graph.getNodeById(n.id)
    )
    // if there is one apply it to view
    state.activatedNodeOption match {
      case Some(n) =>
        // set the editFormHTML
        UI.EditArea.setFormHTML(NodeEditAreas.createEditArea(n, state.graph, state.queryContext, state.resultTypes))
        // update example data
        UI.ExampleData.setData(state.resultTypes, n, state.exampleData)
        // set the types of the node
        UI.ReturnTypes.setTypeInformations(state.resultTypes.get(n.name), state.resultErrors.get(n.name))
        UI.Description.setNodeDescription(n)
      case None    =>
        // we tried to update a note that is not available, so hide the bar
        UI.RightToolbar.hide()
    }

  }

  object QueryStatusIcon {
    val valid = jQuery("#queryValidIcon")
    val wrong = jQuery("#queryWrongIcon")
    val checking = jQuery("#queryCheckingIcon")

    def setValid() = {
      valid.show()
      wrong.hide()
      checking.hide()
    }

    def setWrong() = {
      valid.hide()
      wrong.show()
      checking.hide()
    }

    def setChecking() = {
      valid.hide()
      wrong.hide()
      checking.show()
    }
  }

  object UndoButton {

    val undoButtonEnabled = jQuery("#undoButtonEnabled")
    val undoButtonDisabled = jQuery("#undoButtonDisabled")

    def enable() = {
      undoButtonEnabled.show()
      undoButtonDisabled.hide()
    }

    def disable() = {
      undoButtonEnabled.hide()
      undoButtonDisabled.show()
    }
  }

  object UpdateExamplesButton {
    val updateExamplesButton = jQuery("#updateExamplesButton")

    def requestStarted() = {
      // updateExamplesButton.hide()
      updateExamplesButton.value("Waiting for results...")
      updateExamplesButton.addClass("disabled")
    }

    def requestDone() = {
      // updateExamplesButton.show()
      updateExamplesButton.value("Update examples")
      updateExamplesButton.removeClass("disabled")
    }
  }

  object DeployButton {

    val deployButtonEnabled = jQuery("#deployButtonEnabled")
    val deployButtonDisabled = jQuery("#deployButtonDisabled")

    def enable() = {
      deployButtonEnabled.show()
      deployButtonDisabled.hide()
    }

    def disable() = {
      deployButtonEnabled.hide()
      deployButtonDisabled.show()
    }
  }


  object EditArea {

    val nodeEditArea = jQuery("#nodeEditArea")

    def setFormHTML(html: String) = {
      nodeEditArea.html(html)
    }
  }

  object ReturnTypes {
    val returnTypesArea = jQuery("#returnTypesArea")

    def setTypeInformations(types: Option[ResultType], error: Option[String]) = {
      (types, error) match {
        case (Some(x), _) => returnTypesArea.html(resultTypeToHtml(x))
        case (_, Some(e)) => returnTypesArea.html(e)
        case (_, _)       => returnTypesArea.html("No type or error information available - there might be an error in the node")
      }
    }

    def resultTypeToHtml(n: ResultType): String = {

      var text = ""
      text ++= "<ul style='margin-bottom: 0px;padding-left: 3px;'>"

      def processPField(f: PField): Unit = {
        f.typ match {
          case PBag(xf)   =>
            text ++= "<p style='margin-bottom: 0px'>Bag: { </p>"
            text ++= "<ul style='margin-bottom: 0px;margin-top: 0px;border-left: 1px solid #000000;;padding-left: 5px;'>"
            xf.foreach(processPField)
            text ++= "}</ul>"
          case PTuple(xf) =>
            text ++= "<ul style='margin-top: 0px;margin-bottom: 0px;padding-left: 0px;'>"
            xf.foreach(processPField)
            text ++= "</ul>"
          //@todo integrate
          //          case PMap(x) =>
          case _ =>
            text ++= "<p style='margin-bottom: 0px;margin-top: 0px'>" + f.typ.getClass.getSimpleName.slice(1, 2) + " - " + f.name.replaceAll(".*::", "") + "</p>"
        }
      }

      n.fields.foreach(processPField)
      text ++= "</ul>"
      text
    }
  }

  object ExampleData {

    val resultsArea = jQuery("#resultData")

    def setData(resultType: Map[String, ResultType], activatedNode: Node, exampleDataMap: Map[String, List[List[String]]] = Map()) = {
      (exampleDataMap.get(activatedNode.name), resultType.get(activatedNode.name)) match {

        case (Some(nodeExamples), Some(nodeTypes)) =>
          var returnText = "<table><tr>"
          returnText += nodeTypes.fields.map(t => s"<th>${t.name}</th>").mkString("")
          returnText += "</tr>"
          returnText += nodeExamples.map(line => {
            "<tr>" + line.map(e => s"<td>$e</td>").mkString("") + "</tr>"
          }).mkString("")
          returnText += "</table>"
          resultsArea.html(returnText)

        case (_, _) => resultsArea.html("No examples available for this node")
      }
    }
  }

  object RightToolbar {
    val toolbar = jQuery("#rightToolbar")

    def hide(): Unit = {
      toolbar.hide()
    }

    def show(): Unit = {
      toolbar.show()
    }
  }

  object Description {
    val nodeDescription = jQuery("#nodeDescription")

    def setNodeDescription(n: Node) = {
      nodeDescription.html(NodeDescriptions.getNodeDescription(n))
    }
  }
}
