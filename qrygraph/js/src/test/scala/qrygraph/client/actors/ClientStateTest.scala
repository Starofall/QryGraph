package qrygraph.client.actors

import org.scalatest._
import qrygraph.shared.data.PigQueryGraph

class ClientStateTest extends FunSpec {

  describe("ClientState") {
    it("should start with a valid graph") {
      val x = new ClientState()
      assert(x.graph == new PigQueryGraph())
    }

  }
}