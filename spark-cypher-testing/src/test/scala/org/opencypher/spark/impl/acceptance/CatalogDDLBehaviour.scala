/*
 * Copyright (c) 2016-2018 "Neo4j Sweden, AB" [https://neo4j.com]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Attribution Notice under the terms of the Apache License 2.0
 *
 * This work was created by the collective efforts of the openCypher community.
 * Without limiting the terms of Section 6, any Derivative Work that is not
 * approved by the public consensus process of the openCypher Implementers Group
 * should not be described as “Cypher” (and Cypher® is a registered trademark of
 * Neo4j Inc.) or as "openCypher". Extensions by implementers or prototypes or
 * proposals for change that have been documented or implemented should only be
 * described as "implementation extensions to Cypher" or as "proposed changes to
 * Cypher that are not yet approved by the openCypher community".
 */
package org.opencypher.spark.impl.acceptance

import org.opencypher.okapi.api.graph.GraphName
import org.opencypher.spark.testing.CAPSTestSuite
import org.scalatest.DoNotDiscover

@DoNotDiscover
class CatalogDDLBehaviour extends CAPSTestSuite with DefaultGraphInit {

  describe("CATALOG CREATE GRAPH") {
    it("supports CATALOG CREATE GRAPH on the session") {
      val inputGraph = initGraph(
        """
          |CREATE (:A)
        """.stripMargin)

      caps.catalog.store("foo", inputGraph)

      val result = caps.cypher(
        """
          |CATALOG CREATE GRAPH bar {
          | From GRAPH foo
          | RETURN GRAPH
          |}
        """.stripMargin)

      val sessionSource = caps.catalog.source(caps.catalog.sessionNamespace)
      sessionSource.hasGraph(GraphName("bar")) shouldBe true
      sessionSource.graph(GraphName("bar")) shouldEqual inputGraph
      result.getGraph shouldBe None
      result.getRecords shouldBe None
    }
  }

    describe("DROP GRAPH") {
      it("can drop a session graph") {

        caps.catalog.store("foo", initGraph("CREATE (:A)"))

      val result = caps.cypher(
        """
          |CATALOG DROP GRAPH session.foo
        """.stripMargin
      )

        caps.catalog.source(caps.catalog.sessionNamespace).hasGraph(GraphName("foo")) shouldBe false
        result.getGraph shouldBe None
        result.getRecords shouldBe None
    }
  }
}
