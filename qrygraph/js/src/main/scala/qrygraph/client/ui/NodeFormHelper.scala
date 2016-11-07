package qrygraph.client.ui

import qrygraph.shared.pig.PField

/**
  * Helper class to create HTML forms to edit the nodes
  */
object NodeFormHelper {

  def createHTMLDropdownForm(id: String, fieldName: String, selectedValue: String, incomingTypes: List[PField]): String = {
    s"""
       |<div class="form-group">
       |  <label for="node-$id-$fieldName" class="form-control-label">$fieldName</label>
       |     <select id="input1" id="node-$id-$fieldName" class="c-select form-control input-sm" onchange="UIListener().updateFieldValue('$id','$fieldName',this.value)">
       |     ${generateHTMLOptions(incomingTypes, selectedValue)}
       |    </select>
       |</div>
      """.stripMargin
  }

  def createHTMLTextForm(id: String, fieldName: String, defaultValue: String): String = {
    //@todo make sure names are char-only and longer than 0 chars
    s"""
       |<div class="form-group">
       |  <label for="node-$id-$fieldName" class="form-control-label">$fieldName</label>
       |   <input type="text" class="form-control input-sm" id="node-$id-$fieldName" value="$defaultValue" onchange="UIListener().updateFieldValue('$id','$fieldName',this.value)">
       |</div>
    """.stripMargin
  }


  private def generateHTMLOptions(list: List[PField], selectedString: String): String = {
    // create a buffer to add option html
    var options = ""
    options ++= s"<option value='none'>-none-</option>"
    list.foreach(f => {
      val selected = if (f.name == selectedString) "selected='selected'" else ""
      options ++= s"<option $selected value='${f.name}'>${f.name}</option>"
    })
    val selectedAll = if ("all" == selectedString) "selected='selected'" else ""
    options ++= s"<option $selectedAll value='all'>all</option>"
    options
  }
}
