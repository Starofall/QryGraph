package services

import qrygraph.shared.data._
import qrygraph.shared.nodes._

/**
  * Created by info on 27.04.2016.
  */
object Debug {

  // Create a demo graph
  val nodes = List(
    LoadNode("10", "CarDB", DataSource("1","1","1","1",List(Column("id","chararray")))),
    LoadNode("20", "ParkingSlotDB",  DataSource("1","1","1","1",List(Column("id","chararray")))),
    FilterNode("71", "filterDriving", "id != '1000'"),
    FilterNode("72", "filterAvailable", "id != '1000'"),
    JoinNode("30", "joinOnPosition", "id", "id"),
    GroupNode("50", "groupSlots", "id"),
    OutputNode("60")
//    ,
//    ComponentNode("60")
  )

  val edges = List(
    Edge(nodes(0).outputs.head, nodes(2).inputs(0)),
    Edge(nodes(1).outputs.head, nodes(3).inputs(0)),
    Edge(nodes(2).outputs.head, nodes(4).inputs(0)),
    Edge(nodes(3).outputs.head, nodes(4).inputs(1)),
    Edge(nodes(4).outputs.head, nodes(5).inputs(0)),
    Edge(nodes(5).outputs.head, nodes(6).inputs(0))
  )

  //apply graph
  nodes(0).position = NodePosition(250, 200, 200, 100)
  nodes(1).position = NodePosition(250, 400, 200, 100)
  nodes(2).position = NodePosition(500, 200, 200, 100)
  nodes(3).position = NodePosition(750, 400, 200, 100)
  nodes(4).position = NodePosition(1000, 400, 200, 100)
  nodes(5).position = NodePosition(1250, 400, 200, 100)


  var qrygraph = PigQueryGraph(nodes, edges)


}