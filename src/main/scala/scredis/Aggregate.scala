/*
 * Copyright (c) 2013 Livestream LLC. All rights reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. See accompanying LICENSE file.
 */
package scredis

/**
 * Represents the aggregation function to be used for aggregating scores when computing the
 * union or intersection of sorted sets
 */
sealed abstract class Aggregate(name: String) {
  override def toString = name
}

/**
 * Contains all available aggregation functions
 */
object Aggregate {
  case object Sum extends Aggregate("SUM")
  case object Min extends Aggregate("MIN")
  case object Max extends Aggregate("MAX")
}