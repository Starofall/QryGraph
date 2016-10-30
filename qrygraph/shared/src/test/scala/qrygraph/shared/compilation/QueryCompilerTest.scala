package qrygraph.shared.compilation

import org.scalatest.FunSpec
import qrygraph.shared.data._
import qrygraph.shared.nodes._

class QueryCompilerTest extends FunSpec {

  val defaultDataSource = QueryLoadSource("data","name","desc","LOAD 'url' USING PigStorage(',') AS ()")

  describe("QueryCompiler") {
    // Create a demo graph
    val nodes = List(
      LoadNode("10", "CarDB", defaultDataSource),
      LoadNode("20", "ParkingSlotDB", defaultDataSource),
      FilterNode("71", "filterDriving", "reviewerId != '1000'"),
      FilterNode("72", "filterAvailable", "reviewerId != '1000'"),
      JoinNode("30", "joinOnPosition", "name", "latitude"),
      GroupNode("50", "groupSlots", "age"),
      OutputNode("60")
    )
    val edges = List(
      Edge(nodes(0).outputs.head, nodes(2).inputs(0)),
      Edge(nodes(1).outputs.head, nodes(3).inputs(0)),
      Edge(nodes(2).outputs.head, nodes(4).inputs(0)),
      Edge(nodes(3).outputs.head, nodes(4).inputs(1)),
      Edge(nodes(4).outputs.head, nodes(5).inputs(0)),
      Edge(nodes(5).outputs.head, nodes(6).inputs(0))
    )

    val exampleGraph = PigQueryGraph(nodes, edges)
    val dataSource = List(
     QueryLoadSource("data","name","desc","LOAD 'url' USING PigStorage(',') AS ()")
    )

    it("should compile complex graphs") {

      val result = QueryCompiler.compile(exampleGraph).mkString("")
      assert(result ==
        "CarDB = LOAD 'url' USING PigStorage(',') AS ();" +
          "ParkingSlotDB = LOAD 'url' USING PigStorage(',') AS ();" +
          "filterDriving = FILTER CarDB BY reviewerId != '1000';" +
          "filterAvailable = FILTER ParkingSlotDB BY reviewerId != '1000';" +
          "joinOnPosition = JOIN filterDriving BY name, filterAvailable BY latitude;" +
          "groupSlots = GROUP joinOnPosition BY age;" +
          "storage = LIMIT groupSlots 1000000;--(dummy)")
    }

    it("should compile complex graphs into HTML") {
      val resultHTML = QueryCompiler.compileHTML(exampleGraph)
      assert(resultHTML ==
        "CarDB = LOAD 'url' USING PigStorage(',') AS ();<br>" +
          "ParkingSlotDB = LOAD 'url' USING PigStorage(',') AS ();<br>" +
          "filterDriving = FILTER CarDB BY reviewerId != '1000';<br>" +
          "filterAvailable = FILTER ParkingSlotDB BY reviewerId != '1000';<br>" +
          "joinOnPosition = JOIN filterDriving BY name, filterAvailable BY latitude;<br>" +
          "groupSlots = GROUP joinOnPosition BY age;<br>" +
          "storage = LIMIT groupSlots 1000000;--(dummy)")
    }
  }



  //    describe("Compile Components") {
  //      it("should be able to compile components") {
  //        val node = ComponentNode("1", "name", "00001")
  //        val compilation = node.compileToPig(List(),List(), incomingNames = List(Some("input1"), Some("input2"), Some("input3"))).mkString("")
  //        assert(compilation ==
  //          "filterDriving = FILTER input1 BY id != '1000';" +
  //            "filterAvailable = FILTER input2 BY id != '1000';" +
  //            "joinOnPosition = JOIN input3 BY id, filterAvailable BY id;")
  //      }
  //    }


}