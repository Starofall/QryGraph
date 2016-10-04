package qrygraph.shared.nodes

import qrygraph.shared.data.{Node, NodePosition, QueryContext}

import scala.util.Random

object NodeHelper {

  def createNewNode(nodeName: String, context: QueryContext): Node = nodeName match {
    case "Cross"    => CrossNode()
    case "Distinct" => DistinctNode()
    case "Filter"   => FilterNode()
    case "Split"    => SplitNode()
    case "Group"    => GroupNode()
    case "Join"     => JoinNode()
    case "Foreach"  => ForeachNode()
    case "Custom"   => CustomNode()
    case "COGroup"  => COGroupNode()
    case "Limit"    => LimitNode()
    case "Order"    => OrderNode()
    case "Sample"   => SampleNode()
    case "Union"    => UnionNode()
    case id         =>
      if (context.queryDataSources.exists(_.id == id)) {
        LoadNode(source = context.queryDataSources.find(_.id == id).get)
      } else if (context.components.exists(_.id == id)) {
        ComponentNode(serverComponent = context.components.find(_.id == id).get)
      } else {
        println("ERROR: UNKNOWN NODE CREATION")
        JoinNode()
      }
  }

  //@todo apply all rules like reserved names from pig latin and prevent duplications
  def assureValidNodeName(string: String, otherNames: List[String]): String = {
    val noDigets = string.replaceAll("\\d*", "")
    if (noDigets.length == 0 || otherNames.contains(noDigets)) {
      // recursion try it again
      createNodeName()
    } else {
      noDigets
    }
  }

  def createNodeName(): String = {
    assureValidNodeName(Random.alphanumeric.take(6).toList.mkString, List())
  }

  /** this is actually necessary, as scala does offer a copy only on the implementation level and not in the trait */
  def nodePositionHelper(n: Node, newPosition: NodePosition): Node = n match {
    case x@OutputNode(id, name, position)                    => x.copy(position = newPosition)
    case x@JoinNode(id, name, by1, by2, position)            => x.copy(position = newPosition)
    case x@SampleNode(id, name, sampleCount, position)       => x.copy(position = newPosition)
    case x@FilterNode(id, name, filterBy, position)          => x.copy(position = newPosition)
    case x@COGroupNode(id, name, by1, by2, position)         => x.copy(position = newPosition)
    case x@CustomNode(id, name, queryString, position)       => x.copy(position = newPosition)
    case x@ForeachNode(id, name, foreachQuery, position)     => x.copy(position = newPosition)
    case x@LoadNode(id, name, sourceId, position)            => x.copy(position = newPosition)
    case x@LimitNode(id, name, limitCount, position)         => x.copy(position = newPosition)
    case x@OrderNode(id, name, orderBy, position)            => x.copy(position = newPosition)
    case x@GroupNode(id, name, groupBy, position)            => x.copy(position = newPosition)
    case x@CrossNode(id, name, position)                     => x.copy(position = newPosition)
    case x@UnionNode(id, name, position)                     => x.copy(position = newPosition)
    case x@SplitNode(id, name, splitBy1, splitBy2, position) => x.copy(position = newPosition)
    case x@DistinctNode(id, name, position)                  => x.copy(position = newPosition)
    case x@ComponentNode(id, name, componentId, position)    => x.copy(position = newPosition)
    case y                                                   => y
  }
}
