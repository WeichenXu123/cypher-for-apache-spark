package org.opencypher.spark_legacy.benchmark

import org.opencypher.spark_legacy.impl.{In, Out}
import org.opencypher.spark.StdTestSuite

class GraphFramesBenchmarksTest extends StdTestSuite {

  test("motif construction") {
    GraphFramesBenchmarks.buildMotif(Seq(Out("") -> "")) should equal("(n0)-[r0]->(n1)")
    GraphFramesBenchmarks.buildMotif(Seq(Out("") -> "", Out("") -> "")) should equal("(n0)-[r0]->(n1); (n1)-[r1]->(n2)")
    GraphFramesBenchmarks.buildMotif(Seq(Out("") -> "", In("") -> "")) should equal("(n0)-[r0]->(n1); (n2)-[r1]->(n1)")
    GraphFramesBenchmarks.buildMotif(Seq(Out("") -> "", In("") -> "", In("") -> "")) should equal("(n0)-[r0]->(n1); (n2)-[r1]->(n1); (n3)-[r2]->(n2)")

    an [IllegalArgumentException] should be thrownBy {
      GraphFramesBenchmarks.buildMotif(Seq(In("") -> ""))
    }
  }

}