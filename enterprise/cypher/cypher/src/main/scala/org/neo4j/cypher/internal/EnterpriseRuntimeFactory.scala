/*
 * Copyright (c) 2002-2018 "Neo4j"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * Copyright (c) 2018-2020 "Graph Foundation"
 * Graph Foundation, Inc. [https://graphfoundation.org]
 *
 * This file is part of ONgDB Enterprise Edition. The included source
 * code can be redistributed and/or modified under the terms of the
 * GNU AFFERO GENERAL PUBLIC LICENSE Version 3
 * (http://www.fsf.org/licensing/licenses/agpl-3.0.html) as found
 * in the associated LICENSE.txt file.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 */
package org.neo4j.cypher.internal

import org.neo4j.cypher.CypherRuntimeOption

// import org.neo4j.cypher.internal.compatibility.{CypherRuntime, FallbackRuntime, InterpretedRuntime, ProcedureCallOrSchemaCommandRuntime}
// See: https://github.com/graphfoundation/ongdb/blob/e95e701325c53995a186b9222ff8cca022410286/community/cypher/cypher/src/main/scala/org/neo4j/cypher/internal/CommunityRuntimeFactory.scala
object EnterpriseRuntimeFactory {
  val interpreted = new FallbackRuntime[EnterpriseRuntimeContext](List(SchemaCommandRuntime, InterpretedRuntime), CypherRuntimeOption.interpreted)
  val slotted = new FallbackRuntime[EnterpriseRuntimeContext](List(SchemaCommandRuntime, SlottedRuntime, InterpretedRuntime), CypherRuntimeOption.slotted)
  val compiledWithoutFallback = new FallbackRuntime[EnterpriseRuntimeContext](List(SchemaCommandRuntime, CompiledRuntime), CypherRuntimeOption.compiled)
  val compiled = new FallbackRuntime[EnterpriseRuntimeContext](List(SchemaCommandRuntime, CompiledRuntime, SlottedRuntime, InterpretedRuntime), CypherRuntimeOption.compiled)

  // Added pipelined using same naming conventions as others.
  val pipelinedWithoutFallback = new FallbackRuntime[EnterpriseRuntimeContext](List(SchemaCommandRuntime, PipelinedRuntime.PIPELINED), CypherRuntimeOption.pipelined)
  val pipelined = new FallbackRuntime[EnterpriseRuntimeContext](List(SchemaCommandRuntime, PipelinedRuntime.PIPELINED, CompiledRuntime, SlottedRuntime, InterpretedRuntime), CypherRuntimeOption.pipelined)

  // TODO: Implement these in future.
  // val parallel = new FallbackRuntime[EnterpriseRuntimeContext](List(SchemaCommandRuntime, PipelinedRuntime.PARALLEL, CompiledRuntime, SlottedRuntime, InterpretedRuntime), CypherRuntimeOption.pipelined)
  //val parallelWithoutFallback = new FallbackRuntime[EnterpriseRuntimeContext](List(SchemaCommandRuntime, PipelinedRuntime.PARALLEL), CypherRuntimeOption.parallel)

  val default = new FallbackRuntime[EnterpriseRuntimeContext](List(SchemaCommandRuntime, CompiledRuntime, SlottedRuntime, InterpretedRuntime), CypherRuntimeOption.default)

  def getRuntime(cypherRuntime: CypherRuntimeOption, disallowFallback: Boolean): CypherRuntime[EnterpriseRuntimeContext] =
    cypherRuntime match {
      case CypherRuntimeOption.interpreted => interpreted

      case CypherRuntimeOption.slotted => slotted

      case CypherRuntimeOption.compiled if disallowFallback => compiledWithoutFallback

      case CypherRuntimeOption.compiled => compiled

      case CypherRuntimeOption.pipelined if disallowFallback => pipelinedWithoutFallback

      case CypherRuntimeOption.pipelined => pipelined

      case CypherRuntimeOption.parallel => pipelinedWithoutFallback

      case CypherRuntimeOption.default => default
    }
}
