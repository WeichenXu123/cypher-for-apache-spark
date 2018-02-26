/*
 * Copyright (c) 2016-2018 "Neo4j, Inc." [https://neo4j.com]
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
 */
package org.opencypher.caps.impl.spark

import org.apache.spark.storage.StorageLevel
import org.opencypher.caps.api.CAPSSession
import org.opencypher.caps.api.graph.{GraphOperations, PropertyGraph}
import org.opencypher.caps.api.io.{CAPSEntityTable, CAPSNodeTable}
import org.opencypher.caps.api.schema._
import org.opencypher.caps.api.table.CypherRecords
import org.opencypher.caps.api.types.{CTNode, CTRelationship}
import org.opencypher.caps.impl.exception.IllegalArgumentException
import org.opencypher.caps.impl.spark.CAPSConverters._
import org.opencypher.caps.impl.table.{OpaqueField, RecordHeader}
import org.opencypher.caps.ir.api.expr._

trait CAPSGraph extends PropertyGraph with GraphOperations with Serializable {

  override def nodes(name: String, nodeCypherType: CTNode = CTNode): CAPSRecords

  override def relationships(name: String, relCypherType: CTRelationship = CTRelationship): CAPSRecords

  override def session: CAPSSession

  override def union(other: PropertyGraph): CAPSGraph

  def cache(): CAPSGraph

  def persist(): CAPSGraph

  def persist(storageLevel: StorageLevel): CAPSGraph

  def unpersist(): CAPSGraph

  def unpersist(blocking: Boolean): CAPSGraph

  override def toString = s"${getClass.getSimpleName}"

}

object CAPSGraph {

  def empty(implicit caps: CAPSSession): CAPSGraph =
    new EmptyGraph() {

      override def session: CAPSSession = caps

      override def cache(): CAPSGraph = this

      override def persist(): CAPSGraph = this

      override def persist(storageLevel: StorageLevel): CAPSGraph = this

      override def unpersist(): CAPSGraph = this

      override def unpersist(blocking: Boolean): CAPSGraph = this
    }

  def create(nodeTable: CAPSNodeTable, entityTables: CAPSEntityTable*)(implicit caps: CAPSSession): CAPSGraph = {
    val allTables = nodeTable +: entityTables
    val schema = allTables.map(_.schema).reduce(_ ++ _)
    new CAPSScanGraph(allTables, schema)
  }

  def create(records: CypherRecords, schema: Schema)(implicit caps: CAPSSession): CAPSGraph = {
    val capsRecords = records.asCaps
    new CAPSPatternGraph(capsRecords, schema)
  }

  def createLazy(theSchema: Schema, loadGraph: => CAPSGraph)(implicit caps: CAPSSession): CAPSGraph =
    new LazyGraph(theSchema, loadGraph) {}

  sealed abstract class LazyGraph(override val schema: Schema, loadGraph: => CAPSGraph)(implicit caps: CAPSSession)
    extends CAPSGraph {
    protected lazy val lazyGraph: CAPSGraph = {
      val g = loadGraph
      if (g.schema == schema) g else throw IllegalArgumentException(s"a graph with schema $schema", g.schema)
    }

    override def session: CAPSSession = caps

    override def nodes(name: String, nodeCypherType: CTNode): CAPSRecords =
      lazyGraph.nodes(name, nodeCypherType)

    override def relationships(name: String, relCypherType: CTRelationship): CAPSRecords =
      lazyGraph.relationships(name, relCypherType)

    override def union(other: PropertyGraph): CAPSGraph =
      lazyGraph.union(other)

    override def cache(): CAPSGraph = {
      lazyGraph.cache(); this
    }

    override def persist(): CAPSGraph = {
      lazyGraph.persist(); this
    }

    override def persist(storageLevel: StorageLevel): CAPSGraph = {
      lazyGraph.persist(storageLevel)
      this
    }

    override def unpersist(): CAPSGraph = {
      lazyGraph.unpersist(); this
    }

    override def unpersist(blocking: Boolean): CAPSGraph = {
      lazyGraph.unpersist(blocking); this
    }
  }

  sealed abstract class EmptyGraph(implicit val caps: CAPSSession) extends CAPSGraph {

    override val schema: Schema = Schema.empty

    override def nodes(name: String, cypherType: CTNode): CAPSRecords =
      CAPSRecords.empty(RecordHeader.from(OpaqueField(Var(name)(cypherType))))

    override def relationships(name: String, cypherType: CTRelationship): CAPSRecords =
      CAPSRecords.empty(RecordHeader.from(OpaqueField(Var(name)(cypherType))))

    override def union(other: PropertyGraph): CAPSGraph = other.asCaps
  }

}
