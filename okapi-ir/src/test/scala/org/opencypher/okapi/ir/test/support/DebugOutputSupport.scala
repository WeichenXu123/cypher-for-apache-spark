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
package org.opencypher.okapi.ir.test.support

import scala.collection.Bag
import scala.collection.immutable.HashedBagConfiguration

trait DebugOutputSupport {

  implicit class GenericIterableToBagConverter[T](val elements: TraversableOnce[T]) {
    implicit val m: HashedBagConfiguration[T] = Bag.configuration.compact[T]
    def toBag: Bag[T] = Bag(elements.toSeq: _*)
  }

  implicit class ArrayToBagConverter[T](val elements: Array[T]) {
    implicit val m: HashedBagConfiguration[T] = Bag.configuration.compact[T]
    def toBag: Bag[T] = Bag(elements.toSeq: _*)
  }
}