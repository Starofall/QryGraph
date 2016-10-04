package qrygraph.client.ui

import qrygraph.shared.data._
import qrygraph.shared.nodes._

/**
  *  Collection of text usage descriptions for a given node
  */
object NodeDescriptions {

  def getNodeDescription(node: Node): String = node match {
    case SampleNode(id, name, sampleCount, position)       =>
      "<p>Selects a random sample of data based on the specified sample size." +
        "<a  target='_blank'href='http://pig.apache.org/docs/r0.16.0/basic.html#sample'> See Details</a></p>"
    case COGroupNode(id, name, by1, by2, position)         =>
      "<p>Computes the cross product of two or more relations." +
        "<a  target='_blank'href='http://pig.apache.org/docs/r0.16.0/basic.html#cogroup'> See Details</a></p>"
    case CustomNode(id, name, queryString, position)       =>
      "<p>Allows custom Pig commands. Use #1 as input in the query.</p>"
    case ForeachNode(id, name, foreachQuery, position)     =>
      "<p>Generates data transformations based on columns of data." +
        "<a  target='_blank'href='http://pig.apache.org/docs/r0.16.0/basic.html#foreach'> See Details</a></p>"
    case LimitNode(id, name, limitCount, position)         =>
      "<p>Limits the number of output tuples.\n." +
        "<a  target='_blank'href='http://pig.apache.org/docs/r0.16.0/basic.html#limit'> See Details</a></p>"
    case OrderNode(id, name, orderBy, position)            =>
      "<p>Sorts a relation based on one or more fields." +
        "<a  target='_blank'href='http://pig.apache.org/docs/r0.16.0/basic.html#orderby'> See Details</a></p>"
    case UnionNode(id, name, position)                     =>
      "<p>Computes the union of two or more relations." +
        "<a  target='_blank'href='http://pig.apache.org/docs/r0.16.0/basic.html#union'> See Details</a></p>"
    case JoinNode(id, name, by1, by2, position)            =>
      "<p>Performs an join of two or more relations based on common field values." +
        "<a  target='_blank'href='http://pig.apache.org/docs/r0.16.0/basic.html#join-outer'> See Details</a></p>"
    case SplitNode(id, name, splitBy1, splitBy2, position) =>
      "<p>Partitions a relation into two or more relations." +
        "<a  target='_blank'href='http://pig.apache.org/docs/r0.16.0/basic.html#SPLIT'> See Details</a></p>"
    case FilterNode(id, name, filterBy, position)          =>
      "<p>Selects tuples from a relation based on some condition." +
        "<a  target='_blank'href='http://pig.apache.org/docs/r0.16.0/basic.html#filter'> See Details</a></p>"
    case OutputNode(id, name, position)                    =>
      "<p>This node is used by QryGraph as an output of a query.</p>"
    case LoadNode(id, name, dataSource, position)            =>
      "<p>Loads data from the file system." +
        "<a  target='_blank'href='http://pig.apache.org/docs/r0.16.0/basic.html#load'> See Details</a></p>" +
        s"<p>Loads the database:<br>${dataSource.name} - ${dataSource.description}</p>"
    case GroupNode(id, name, groupBy, position)            =>
      "<p>Groups the data in one or more relations." +
        "<a  target='_blank'href='http://pig.apache.org/docs/r0.16.0/basic.html#group'> See Details</a></p>"
    case CrossNode(id, name, position)                     =>
      "<p>Computes the cross product of two or more relations." +
        "<a  target='_blank'href='http://pig.apache.org/docs/r0.16.0/basic.html#cross'> See Details</a></p>"
    case DistinctNode(id, name, position)                  =>
      "<p>Removes duplicate tuples in a relation." +
        "<a  target='_blank'href='http://pig.apache.org/docs/r0.16.0/basic.html#distinct'> See Details</a></p>"
    case ComponentNode(id, name, serverComponent, position)    =>
      s"<p>A QryGraph component defined at the component page</p>" +
        s"<p>Integrates the component:<br>${serverComponent.name} - ${serverComponent.description}</p>"
    case _                                                 => "NOT YET SPECIFIED"
  }
}
