package qrygraph.shared.compilation

import qrygraph.shared.data._


/** compiles a query down into a Pig Latin script */
object QueryCompiler {

  /** compiles the Pig Latin into a valid list of Pig Latin commands - also adding the node */
  def advancedCompile(queryGraph: PigQueryGraph): List[(Node, String)] = {

    //@todo recursivly do removal
    // first we remove all components in the graph and replace them with their content
    val componentRemovedGraph = GraphFlatten.removeComponentNodes(queryGraph)
    // then we flatten the dependencies into a linear list
    val flattenDependency = GraphFlatten.flattenDependency(componentRemovedGraph)
    //    print(flattenDependency)
    // then for each node compile
    flattenDependency.map(n => {
      // create a list of incoming node names
      val incomingnames = n.inputs
        // for each input
        .map(i => componentRemovedGraph.incomingOutput(i))
        // find the corresponding outputOption
        .map(option =>
        // select the label of it
        option.map(output =>
          output.label
        )
      )
      (n, n.compileToPig(incomingnames).mkString(""))
    })
  }

  /** removes the node element of the advancedCompile result */
  def compile(queryGraph: PigQueryGraph): List[String] = {
    advancedCompile(queryGraph).map(_._2)
  }

  /** compiles to Pig Latin suitable for HTML display using br tag */
  def compileHTML(queryGraph: PigQueryGraph): String = compile(queryGraph).mkString("<br>")

}
