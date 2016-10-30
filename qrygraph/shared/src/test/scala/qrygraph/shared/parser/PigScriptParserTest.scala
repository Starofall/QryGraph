package qrygraph.shared.parser

import org.scalatest.FunSpec
import qrygraph.shared.compilation.QueryCompiler
import qrygraph.shared.data.QueryLoadSource

/**
  * Created by info on 24.10.2016.
  */
class PigScriptParserTest extends FunSpec {

  describe("Parser") {
    it("should parse simple query correct") {

      val defaultDataSource = QueryLoadSource("custom", "custom", "custom", "LOAD 'url' USING PigStorage(',') AS ()")

      val input = "CarDB = LOAD 'url' USING PigStorage(',') AS ();" +
        "ParkingSlotDB = LOAD 'url' USING PigStorage(',') AS ();" +
        "filterDriving = FILTER CarDB BY reviewerId != '1000';" +
        "filterAvailable = FILTER ParkingSlotDB BY reviewerId != '1000';" +
        "joinOnPosition = JOIN filterDriving BY name, filterAvailable BY latitude;" +
        "groupSlots = GROUP joinOnPosition BY age;" +
        "storage = LIMIT groupSlots 1000000;"

      assert(QueryCompiler.compile(PigScriptParser.parsePigScript(input)).mkString("") == input)
    }
  }

}
