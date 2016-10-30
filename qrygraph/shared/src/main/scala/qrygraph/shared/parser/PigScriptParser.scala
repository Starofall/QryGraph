package qrygraph.shared.parser

import qrygraph.shared.data.{Edge, Node, PigQueryGraph, QueryLoadSource}
import qrygraph.shared.nodes._

/** parses pig scripts into commands */
object PigScriptParser {

  /** simple class for parsedLines */
  case class ParsedLine(name: String, command: String, parameter: String)
  /** a connection between two nodes */
  case class NodeConnection(fromNode: String, toNode: String, inputIndex: Int = 0)

  /** parses a string and returns a pigQueryGraph */
  def parsePigScript(script: String): PigQueryGraph = {
    // first split into tokens and trim
    var tokens = script.split(";").map(_.trim).toList

    // convert the tokens into ParsedLines
    val parsed = tokens.flatMap(s => {
      val split = s.split("=", 2).map(_.trim)
      if (split.length == 2) {
        var rest = split(1).split(" ", 2)
        Some(ParsedLine(split(0), rest(0), rest(1)))
      } else {
        None
      }
    })

    // find nodes and connections in the tokens
    val nodesAndConnections: List[(List[NodeConnection], Node)] = parsed.map {
      case ParsedLine(name, "LOAD", command) =>
        (List(), LoadNode(id = name, name = name, source = QueryLoadSource("custom", "custom", "custom", "LOAD " + command)))

      case ParsedLine(name, "FILTER", rest) =>
        val restSplit = rest.split(" BY ").map(_.trim)
        (List(NodeConnection(restSplit(0), name)), FilterNode(id = name, name = name, filterBy = restSplit(1)))

      case ParsedLine(name, "JOIN", rest) =>
        val splitList = rest.split(",").map(_.split(" BY ").map(_.trim))
        (List(
          NodeConnection(splitList(0)(0), name, 0),
          NodeConnection(splitList(1)(0), name, 1)),
          JoinNode(id = name, name = name, by1 = splitList(0)(1), by2 = splitList(1)(1)))

      case ParsedLine(name, "GROUP", rest) =>
        val restSplit = rest.split(" BY ").map(_.trim)
        (List(NodeConnection(restSplit(0), name)), GroupNode(id = name, name = name, groupBy = restSplit(1)))

      case ParsedLine(name, "LIMIT", rest) =>
        val restSplit = rest.split(" ").map(_.trim)
        (List(NodeConnection(restSplit(0), name)), LimitNode(id = name, name = name, limitCount = restSplit(1)))

      // @TODO
      // COGROUP
      // CROSS
      // DISTINCT
      // FOREACH
      // ORDER
      // SAMPLE
      // SPLIT
      // UNION

      case ParsedLine(name, _, command) =>
        println("ERROR CASE")
        (List(), LoadNode(name = name, source = QueryLoadSource("custom", "custom", "custom", "LOAD " + command)))
    }

    // extract nodes
    val nodes = nodesAndConnections.map(_._2)

    // create edges for the parsed connections
    val edges = nodesAndConnections.flatMap(_._1).flatMap(c => {
      val fromNode = nodes.find(_.name == c.fromNode)
      val toNode = nodes.find(_.name == c.toNode)
      (fromNode, toNode) match {
        case (Some(x), Some(y)) =>
          val output = x.outputs.head
          val input = y.inputs(c.inputIndex)
          Some(Edge(output, input))

        case (_, _) =>
          println("NOT FOUND: " + c.fromNode + " " + c.toNode + " in " + nodes.map(_.name))
          None
      }
    })

    // @todo ADD OUTPUT NODE if not there

    // return final graph
    PigQueryGraph(nodes, edges)
  }

}
