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
package org.opencypher.okapi.neo4j.io

import org.opencypher.okapi.api.graph.GraphName
import org.opencypher.okapi.api.schema.LabelPropertyMap.LabelPropertyMap
import org.opencypher.okapi.api.schema.PropertyKeys.PropertyKeys
import org.opencypher.okapi.api.schema.RelTypePropertyMap.RelTypePropertyMap
import org.opencypher.okapi.neo4j.io.Neo4jHelpers.Neo4jDefaults.{metaPrefix, metaPropertyKey}

object MetaLabelSupport {

  implicit class RichGraphName(val graphName: GraphName) {
    def metaLabel(entireGraphName: GraphName): Option[String] =
      if (graphName == entireGraphName) None
      else Some(metaLabelForSubgraph)

    def metaLabelForSubgraph = s"$metaPrefix$graphName"
  }

  implicit class RichPropertyKeys(val keys: PropertyKeys) extends AnyVal {
    def withoutMetaProperty: PropertyKeys = keys.filterKeys(k => k != metaPropertyKey)
  }

  implicit class LabelPropertyMapWithMetaSupport(val map: LabelPropertyMap) extends AnyVal {
    def withoutMetaLabel(metaLabel: String): LabelPropertyMap = map.map { case (k, v) => (k - metaLabel) -> v }
    def withoutMetaProperty: LabelPropertyMap = map.mapValues(_.withoutMetaProperty)
  }

  implicit class RelTypePropertyMapWithMetaSupport(val map: RelTypePropertyMap) extends AnyVal  {
    def withoutMetaProperty: RelTypePropertyMap = map.mapValues(_.withoutMetaProperty)
  }

}
