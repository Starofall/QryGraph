package qrygraph.shared.data

import qrygraph.shared.pig.{PField, Primitives}

import scala.util.Random

/** A node in the graph */
trait Node {
  /** a unique node id */
  var id: String
  /** the name of the node in the qrygraph */
  var name: String
  /** the visual position of the node */
  var position: NodePosition
  /** the inputs of the node */
  val inputs: List[Input]
  /** the outputs of the node */
  val outputs: List[Output]

  /** converts the node into valid pig code */
  def compileToPig(incomingNames: List[Option[String]]): List[String]

  /** this function is called if the user changed some value on a node in the UI - it contains the name and the new value of a variable */
  def applyValueChanges(valueName: String, newValue: String): Node = this
}

/** an Edge of a node */
case class Edge(from: Output, to: Input, id: String){
  override def toString: String = s"Edge($id # ${from.id} => ${to.id})"
}
object Edge{
  // create the ID of the edge out of the two corresponding ids to be more deterministic
  def apply(from: Output, to: Input) = new Edge(from,to,""+from.id+to.id)
}

/** an Input of a node */
case class Input(var parent: Node, var id: String, var label: String)
/** a Output of a node */
case class Output(var parent: Node, var id: String, var label: String)

/** represents the size and position of a node */
case class NodePosition(x: Int, y: Int, width: Int, height: Int)

/** demo positions */
object NodePosition {
  def ZERO = NodePosition(0, 0, 200, 100)

  def randomPosition = NodePosition(Random.nextInt(500), Random.nextInt(500), 200, 100)
}

/** a dataSource is a xml-described csv file that can be analyzed in the tool */
case class DataSource(id: String, name: String, description: String, link: String, columns: List[Column], separator:String = ",") {
  def getResultFields: List[PField] = columns.map(f => PField(f.name, Primitives.PInt))
}

/** a column describes a csv-column and is used in schema configuration */
case class Column(name: String, typeValue: String)

/** a QueryContext holds a query, the dataSourceDefinitions and the QueryTypes */
case class QueryContext(queryDataSources: List[DataSource] = List(),
                        components: List[ServerComponent] = List())


/** contains information about a component that is stored on the server */
case class ServerComponent(id: String, name: String, description: String, pigQueryGraph: PigQueryGraph)




