package qrygraph.shared.compilation

import org.scalatest.FunSpec
import qrygraph.shared.data.{Edge, _}
import qrygraph.shared.nodes._

class GraphFlattenTest extends FunSpec {

  val defaultDataSource = QueryLoadSource("data","name","desc","LOAD 'bla.csv' ")

  describe("GraphFlatten") {
    val l1 = LoadNode("10", "CarDB", defaultDataSource)
    val l2 = LoadNode("20", "ParkingSlotDB", defaultDataSource)
    val f1 = FilterNode("71", "filterDriving", "reviewerId != '1000'")
    val f2 = FilterNode("72", "filterAvailable", "reviewerId != '1000'")
    val j1 = JoinNode("30", "joinOnPosition", "name", "latitude")
    val g1 = GroupNode("50", "groupSlots", "age")
    val o1 = OutputNode("60")

    it("should flat a simple graph correctly") {
      // Create a demo graph
      val nodes = List(l1, l2, f1, f2, j1, g1, o1)

      val edges = List(
        Edge(l1.outputs.head, f1.inputs(0)),
        Edge(l2.outputs.head, f2.inputs(0)),
        Edge(f1.outputs.head, j1.inputs(0)),
        Edge(f2.outputs.head, j1.inputs(1)),
        Edge(j1.outputs.head, g1.inputs(0)),
        Edge(g1.outputs.head, o1.inputs(0))
      )

      val exampleGraph = PigQueryGraph(nodes, edges)

      val flatten = GraphFlatten.flattenDependency(exampleGraph)

      val correctResult = List(l1, l2, f1, f2, j1, g1, o1)
      assert(flatten == correctResult)
    }
    it("should flat a simple query with wrong default order") {
      // Create a demo graph

      val edges = List(
        Edge(l1.outputs.head, f1.inputs(0)),
        Edge(l2.outputs.head, f2.inputs(0)),
        Edge(f1.outputs.head, j1.inputs(0)),
        Edge(f2.outputs.head, j1.inputs(1)),
        Edge(j1.outputs.head, g1.inputs(0)),
        Edge(g1.outputs.head, o1.inputs(0))
      )

      def testFlattening(nodes: List[Node]) = {
        val exampleGraph = PigQueryGraph(nodes, edges)
        val flatten = GraphFlatten.flattenDependency(exampleGraph)
        val correctResults = List(
          //each line
          List(l1, f1, l2, f2, j1, g1, o1),
          List(l2, f2, l1, f1, j1, g1, o1),
          //load first
          List(l1, l2, f1, f2, j1, g1, o1),
          List(l2, l1, f1, f2, j1, g1, o1),
          List(l1, l2, f2, f1, j1, g1, o1),
          List(l2, l1, f2, f1, j1, g1, o1)
        )
        assert(correctResults.contains(flatten))
      }

      //      List(l1,l2,f1,f2,j1,g1,o1).permutations.foreach(testFlattening)
      List(List(l1, l2, f1, f2, j1, g1, o1)).foreach(testFlattening)
    }

    describe("with components") {
      // Create a demo graph
      val l1 = LoadNode("l1", "load1", defaultDataSource)
      val u1 = UnionNode("u1", "union1")
      val serverComponent = ServerComponent("comA", "comA", "comA", PigQueryGraph(List(l1, u1), List(Edge(l1.outputs.head, u1.inputs.head))))
      val c1 = ComponentNode("c1", "comp1", serverComponent)

      val l2 = LoadNode("l2", "load2", defaultDataSource)
      val o1 = OutputNode("o1", "out1")

      val nodes = List(l2, c1, o1)
      val edges = List(
        Edge(l2.outputs.head, c1.inputs.head),
        Edge(c1.outputs.head, o1.inputs.head)
      )
      val exampleGraph = PigQueryGraph(nodes, edges)

      it("should remove components and add their content to the graph") {
        val correctResult = PigQueryGraph(
          List(l2, l1, u1, o1),
          List(
            Edge(l1.outputs.head, u1.inputs.head),
            Edge(l2.outputs.head, u1.inputs(1)),
            Edge(u1.outputs.head, o1.inputs.head)
          )
        )
        val removed = GraphFlatten.removeComponentNodes(exampleGraph)
        assert(removed.nodes == correctResult.nodes)
        assert(removed.edges == correctResult.edges)
      }
    }

    describe("SpecialCases") {

      val limit = LimitNode("lim", "avg", "9999")
      val group = GroupNode("gr", "grouped", "all")
      val custom = CustomNode("cu", "avgresult", "foreach <1> generate AVG(<2>)")

      val component = ComponentNode("com", "average", ServerComponent("av1", "average", "average calculation",
        PigQueryGraph(
          List(custom,group,limit),
          List(
            Edge(limit.outputs(0), group.inputs(0)),
            Edge(group.outputs(0), custom.inputs(0)),
            Edge(limit.outputs(0), custom.inputs(1)))
        )
      ))

      val load = LoadNode("manid", "man", defaultDataSource)
      val output = OutputNode("out")

      // Create a demo graph
      val nodes = List(component,load, output)
      val edges = List(
        Edge(load.outputs(0), component.inputs(0)),
        Edge(component.outputs(0), output.inputs(0)))

      val exampleGraph = PigQueryGraph(nodes, edges)

      it("should be compiled - Component-#1") {
        val removed = GraphFlatten.removeComponentNodes(exampleGraph)
        val flattend = GraphFlatten.flattenDependency(removed)
        assert(flattend == List(load, limit, group, custom, output))
      }



      it("should detect cycles #1") {
        val graph = PigQueryGraph(List(limit,group),List(Edge(limit.outputs.head,group.inputs.head),Edge(group.outputs.head,limit.inputs.head)))
        val flattend = GraphFlatten.flattenDependency(graph)
        assert(flattend.length == 0)
      }

      it("should detect cycles #2") {
        val graph = PigQueryGraph(List(load,limit,group),List(Edge(limit.outputs.head,group.inputs.head),Edge(group.outputs.head,limit.inputs.head)))
        val flattend = GraphFlatten.flattenDependency(graph)
        assert(flattend.length == 1)
      }

      it("should handle unconnected nodes") {
        val graph = PigQueryGraph(List(load,limit,group))
        val flattend = GraphFlatten.flattenDependency(graph)
        assert(flattend.length == 3)
      }
    }
  }

}
