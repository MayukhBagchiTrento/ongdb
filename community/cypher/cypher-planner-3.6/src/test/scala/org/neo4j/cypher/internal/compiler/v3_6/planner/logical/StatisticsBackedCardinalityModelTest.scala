/*
 * Copyright (c) 2018-2020 "Graph Foundation"
 * Graph Foundation, Inc. [https://graphfoundation.org]
 *
 * Copyright (c) 2002-2020 "Neo4j,"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * This file is part of ONgDB.
 *
 * ONgDB is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.cypher.internal.compiler.v3_6.planner.logical

import org.neo4j.cypher.internal.compiler.v3_6.planner._
import org.neo4j.cypher.internal.compiler.v3_6.planner.logical.Metrics.QueryGraphCardinalityModel
import org.neo4j.cypher.internal.compiler.v3_6.planner.logical.cardinality.CardinalityModelTestHelper
import org.neo4j.cypher.internal.compiler.v3_6.planner.logical.cardinality.assumeIndependence.AssumeIndependenceQueryGraphCardinalityModel
import org.neo4j.cypher.internal.planner.v3_6.spi.GraphStatistics
import org.neo4j.cypher.internal.v3_6.util.test_helpers.CypherFunSuite

class StatisticsBackedCardinalityModelTest extends CypherFunSuite with LogicalPlanningTestSupport with CardinalityModelTestHelper {

  val allNodes = 733.0
  val personCount = 324.0
  val relCount = 50.0
  val rel2Count = 78.0

  test("query containing a WITH and LIMIT on low/fractional cardinality") {
    val i = .1
    givenPattern("MATCH (a:Person) WITH a LIMIT 10 MATCH (a)-[:REL]->()").
      withGraphNodes(allNodes).
      withLabel('Person -> i).
      withRelationshipCardinality('Person -> 'REL -> 'Person -> relCount).
      shouldHavePlannerQueryCardinality(createCardinalityModel)(
        Math.min(i, 10.0) * relCount / i
      )
  }

  test("query containing a WITH and LIMIT on high cardinality") {
    val i = personCount
    givenPattern("MATCH (a:Person) WITH a LIMIT 10 MATCH (a)-[:REL]->()").
      withGraphNodes(allNodes).
      withLabel('Person -> i).
      withRelationshipCardinality('Person -> 'REL -> 'Person -> relCount).
      shouldHavePlannerQueryCardinality(createCardinalityModel)(
        Math.min(i, 10.0) * relCount / i
      )
  }

  test("query containing a WITH and LIMIT on parameterized cardinality") {
    val i = personCount
    givenPattern("MATCH (a:Person) WITH a LIMIT $limit MATCH (a)-[:REL]->()").
      withGraphNodes(allNodes).
      withLabel('Person -> i).
      withRelationshipCardinality('Person -> 'REL -> 'Person -> relCount).
      shouldHavePlannerQueryCardinality(createCardinalityModel)(
        Math.min(i, DEFAULT_LIMIT_CARDINALITY) * relCount / i
      )
  }

  test("query containing a WITH and aggregation vol. 2") {
    val patternNodeCrossProduct = allNodes * allNodes
    val labelSelectivity = personCount / allNodes
    val maxRelCount = patternNodeCrossProduct * labelSelectivity
    val relSelectivity = rel2Count / maxRelCount

    val firstQG = patternNodeCrossProduct * labelSelectivity * relSelectivity

    val aggregation = Math.sqrt(firstQG)

    givenPattern("MATCH (a:Person)-[:REL2]->(b) WITH a, count(*) as c MATCH (a)-[:REL]->()").
    withGraphNodes(allNodes).
    withLabel('Person -> personCount).
    withRelationshipCardinality('Person -> 'REL -> 'Person -> relCount).
    withRelationshipCardinality('Person -> 'REL2 -> 'Person -> rel2Count).
    shouldHavePlannerQueryCardinality(createCardinalityModel)(aggregation * relCount / personCount)
  }

  test("aggregations should never increase cardinality") {
    givenPattern("MATCH (a:Person)-[:REL]->() WITH a, count(*) as c MATCH (a)-[:REL]->()").
      withGraphNodes(allNodes).
      withLabel('Person -> .1).
      withRelationshipCardinality('Person -> 'REL -> 'Person -> .5).
      shouldHavePlannerQueryCardinality(createCardinalityModel)(2.5)
  }

  test("query containing both SKIP and LIMIT") {
    val i = personCount
    givenPattern( "MATCH (n:Person) WITH n SKIP 5 LIMIT 10").
      withGraphNodes(allNodes).
      withLabel('Person -> i).
      shouldHavePlannerQueryCardinality(createCardinalityModel)(
        Math.min(i, 10.0)
      )
  }

  // We do not improve in this case
  ignore("query containing both SKIP and LIMIT with large skip, so skip + limit exceeds total row count boundary") {
    val i = personCount
    givenPattern( s"MATCH (n:Person) WITH n SKIP ${personCount - 5} LIMIT 10").
      withGraphNodes(allNodes).
      withLabel('Person -> i).
      shouldHavePlannerQueryCardinality(createCardinalityModel)(
        Math.min(i, 5.0)
      )
  }

  test("should reduce cardinality for a WHERE after a WITH") {
    val i = personCount
    givenPattern("MATCH (a:Person) WITH a LIMIT 10 WHERE a.age = 20").
      withGraphNodes(allNodes).
      withLabel('Person -> i).
      shouldHavePlannerQueryCardinality(createCardinalityModel)(
        Math.min(i, 10.0) * DEFAULT_EQUALITY_SELECTIVITY
      )
  }

  test("should reduce cardinality for a WHERE after a WITH, unknown LIMIT") {
    val i = personCount
    givenPattern("MATCH (a:Person) WITH a LIMIT $limit WHERE a.age = 20").
      withGraphNodes(allNodes).
      withLabel('Person -> i).
      shouldHavePlannerQueryCardinality(createCardinalityModel)(
        Math.min(i, DEFAULT_LIMIT_CARDINALITY) * DEFAULT_EQUALITY_SELECTIVITY
      )
  }

  test("should reduce cardinality for a WHERE after a WITH, with ORDER BY") {
    val i = personCount
    givenPattern("MATCH (a:Person) WITH a ORDER BY a.name WHERE a.age = 20").
      withGraphNodes(allNodes).
      withLabel('Person -> i).
      shouldHavePlannerQueryCardinality(createCardinalityModel)(
        i * DEFAULT_EQUALITY_SELECTIVITY
      )
  }

  test("should reduce cardinality for a WHERE after a WITH, with DISTINCT") {
    val i = personCount
    givenPattern("MATCH (a:Person) WITH DISTINCT a WHERE a.age = 20").
      withGraphNodes(allNodes).
      withLabel('Person -> i).
      shouldHavePlannerQueryCardinality(createCardinalityModel)(
        i * DEFAULT_DISTINCT_SELECTIVITY * DEFAULT_EQUALITY_SELECTIVITY
      )
  }

  test("should reduce cardinality for a WHERE after a WITH, with AGGREGATION without grouping") {
    val i = personCount
    givenPattern("MATCH (a:Person) WITH count(a) AS count WHERE count > 20").
      withGraphNodes(allNodes).
      withLabel('Person -> i).
      shouldHavePlannerQueryCardinality(createCardinalityModel)(
        DEFAULT_RANGE_SELECTIVITY
      )
  }

  test("should reduce cardinality for a WHERE after a WITH, with AGGREGATION with grouping") {
    val i = personCount
    givenPattern("MATCH (a:Person) WITH count(a) AS count, a.name AS name WHERE count > 20").
      withGraphNodes(allNodes).
      withLabel('Person -> i).
      shouldHavePlannerQueryCardinality(createCardinalityModel)(
        Math.sqrt(i) * DEFAULT_RANGE_SELECTIVITY
      )
  }

  def createCardinalityModel(in: QueryGraphCardinalityModel): Metrics.CardinalityModel =
    new StatisticsBackedCardinalityModel(in, newExpressionEvaluator)

  override def createQueryGraphCardinalityModel(stats: GraphStatistics): QueryGraphCardinalityModel =
    AssumeIndependenceQueryGraphCardinalityModel(stats, combiner)
}
