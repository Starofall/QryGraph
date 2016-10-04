package qrygraph.shared.nodes

import org.scalatest.FunSpec

/**
  * Created by info on 28.09.2016.
  */
class CustomNodeTest extends FunSpec {

  describe("CustomNode"){

    it("has the correct amount of inputs when using variable inputs") {
      val customNode = CustomNode("id","name","FILTER <1> by <2> and <3>")
      assert(customNode.inputs.length == 3)
    }
  }

}
