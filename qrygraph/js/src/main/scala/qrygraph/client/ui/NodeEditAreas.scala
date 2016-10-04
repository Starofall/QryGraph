package qrygraph.client.ui

import qrygraph.shared.data.{Input, Node, PigQueryGraph, QueryContext}
import qrygraph.shared.nodes._
import qrygraph.shared.pig.{PField, ResultType}

/**
  * Created by info on 31.08.2016.
  */
object NodeEditAreas {

  def getIncomingTypes(input: Input, graph: PigQueryGraph, queryTypes: Map[String, ResultType]): List[PField] = {
    // get the incoming fields
    graph.incomingNode(input).flatMap(n =>
      //get the right types and map for fields
      queryTypes.get(n.name).map(_.fields)
      //if not available - use empty list
    ).getOrElse(List())
  }

  def createEditArea(currentNode: Node, query: PigQueryGraph, queryContext: QueryContext, resultTypes: Map[String, ResultType]): String = currentNode match {
    case JoinNode(id, name, by1, by2, position)            => NodeFormHelper.createHTMLTextForm(id, "name", name) ++
      NodeFormHelper.createHTMLDropdownForm(id, "by1", by1, getIncomingTypes(currentNode.inputs(0), query, resultTypes)) ++
      NodeFormHelper.createHTMLDropdownForm(id, "by2", by2, getIncomingTypes(currentNode.inputs(1), query, resultTypes))

    case SampleNode(id, name, sampleCount, position)       => NodeFormHelper.createHTMLTextForm(id, "name", name) ++
        NodeFormHelper.createHTMLTextForm(id, "sampleCount", sampleCount)

    case FilterNode(id, name, filterBy, position)          => NodeFormHelper.createHTMLTextForm(id, "name", name) ++
      NodeFormHelper.createHTMLTextForm(id, "filterBy", filterBy)

    case COGroupNode(id, name, by1, by2, position)         => NodeFormHelper.createHTMLTextForm(id, "name", name) ++
        NodeFormHelper.createHTMLDropdownForm(id, "by1", by1, getIncomingTypes(currentNode.inputs(0), query, resultTypes)) ++
        NodeFormHelper.createHTMLDropdownForm(id, "by2", by2, getIncomingTypes(currentNode.inputs(1), query, resultTypes))

    case CustomNode(id, name, queryString, position)       => NodeFormHelper.createHTMLTextForm(id, "name", name) ++
      NodeFormHelper.createHTMLTextForm(id, "queryString", queryString)

    case ForeachNode(id, name, foreachQuery, position)     => NodeFormHelper.createHTMLTextForm(id, "name", name) ++
      NodeFormHelper.createHTMLTextForm(id, "foreachQuery", foreachQuery)

    case LoadNode(id, name, sourceId, position)            => NodeFormHelper.createHTMLTextForm(id, "name", name)

    case LimitNode(id, name, limitCount, position)         => NodeFormHelper.createHTMLTextForm(id, "name", name) ++
      NodeFormHelper.createHTMLTextForm(id, "limitCount", limitCount)

    case OrderNode(id, name, orderBy, position)            => NodeFormHelper.createHTMLTextForm(id, "name", name) ++
      NodeFormHelper.createHTMLTextForm(id, "orderBy", orderBy)

    case GroupNode(id, name, groupBy, position)            => NodeFormHelper.createHTMLTextForm(id, "name", name) ++
      NodeFormHelper.createHTMLDropdownForm(id, "groupBy", groupBy, getIncomingTypes(currentNode.inputs(0), query, resultTypes))

    case CrossNode(id, name, position)                     =>  NodeFormHelper.createHTMLTextForm(id, "name", name)

    case UnionNode(id, name, position)                     => NodeFormHelper.createHTMLTextForm(id, "name", name)

    case SplitNode(id, name, splitBy1, splitBy2, position) => NodeFormHelper.createHTMLTextForm(id, "name", name) ++
        NodeFormHelper.createHTMLTextForm(id, "splitBy1", splitBy1) ++
        NodeFormHelper.createHTMLTextForm(id, "splitBy2", splitBy2)

    case DistinctNode(id, name, position)                  => NodeFormHelper.createHTMLTextForm(id, "name", name)

    case ComponentNode(id, name, serverComponent, position)    => s"""
                                                                 |<div class="form-group row">
                                                                 | <div class="col-sm-10">
                                                                 | <a class="btn btn-primary" href="/components/editor/${serverComponent.id}">Open component editor</a>
                                                                 |  </div>
                                                                 |</div>
    """.stripMargin ++ serverComponent.pigQueryGraph.nodes.map(n=>createEditArea(n,serverComponent.pigQueryGraph,queryContext,resultTypes)).mkString("")
    case OutputNode(id, name, position)                    => "Can not be edited"
    case _                                                 => ""
  }
}
