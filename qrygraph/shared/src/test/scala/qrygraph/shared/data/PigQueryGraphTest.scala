package qrygraph.shared.data

import org.scalatest.FunSpec
import qrygraph.shared.nodes.{ComponentNode, FilterNode, LoadNode, UnionNode}

/**
  * Created by info on 21.09.2016.
  */
class PigQueryGraphTest extends FunSpec {

  describe("Graph Editing"){

      it("should be able to apply changes") {
        val subgraph = PigQueryGraph(List(
          ComponentNode("1", "test",ServerComponent("1a","name","desc", PigQueryGraph(List(FilterNode("2", "test2", "nofilter",NodePosition.ZERO)))),NodePosition.ZERO))
        )

        val result = subgraph.applyToNodeId("2",f=>f.applyValueChanges("name","worked"))
        assert(result == PigQueryGraph(List(
          ComponentNode("1", "test", ServerComponent("1a","name","desc",PigQueryGraph(List(FilterNode("2", "worked", "nofilter",NodePosition.ZERO)))),NodePosition.ZERO))
        ))

        val result2 = result.applyToNodeId("2",f=>f.applyValueChanges("filterBy","nothing"))
        assert(result2 == PigQueryGraph(List(
          ComponentNode("1", "test", ServerComponent("1a","name","desc",PigQueryGraph(List(FilterNode("2", "worked", "nothing",NodePosition.ZERO)))),NodePosition.ZERO))
        ))

        val result3 = result2.applyToNodeId("1",f=>f.applyValueChanges("name","alsoworked"))
//        assert(result3 == PigQueryGraph(List(ComponentNode("1", "alsoworked", ServerComponent("1a","name","desc",PigQueryGraph(List(FilterNode("2", "worked", "nothing"))))))))

        // check the original is still the same
        assert(result == PigQueryGraph(List(
          ComponentNode("1", "test", ServerComponent("1a","name","desc",PigQueryGraph(List(FilterNode("2", "worked", "nofilter",NodePosition.ZERO)))),NodePosition.ZERO))
        ))
      }
  }


  describe("Example Graph with open Inputs/Outputs"){
    val defaultDataSource = QueryLoadSource("data","name","desc","LOAD 'bla.csv' ")

    // Create a demo graph
    val l1 = LoadNode("l1","load1",defaultDataSource)
    val u1 = UnionNode("u1","union1")
    val component = ServerComponent("comA","comA","comA",PigQueryGraph(
      List(l1,u1),
      List(Edge(l1.outputs(0),u1.inputs(0)))
    ))

    it("should have one open input"){
      assert(component.pigQueryGraph.openInputs == List(u1.inputs(1)))
    }
    it("should have one open output"){
      assert(component.pigQueryGraph.openOutputs == List(u1.outputs(0)))
    }
  }

}
