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
package org.neo4j.cypher.internal.compatibility.v3_6.runtime.phases

import org.neo4j.cypher.internal.compatibility.v3_6.runtime.executionplan.{ExecutionPlan => RuntimeExecutionPlan}
import org.neo4j.cypher.internal.compiler.v3_6.phases.LogicalPlanState

import scala.util.{Failure, Try}

class CompilationState(ls: LogicalPlanState,
                           val maybeExecutionPlan: Try[RuntimeExecutionPlan] = Failure(new UnsupportedOperationException))
  extends LogicalPlanState(ls.queryText, ls.startPosition, ls.plannerName, ls.planningAttributes, ls.maybeStatement, ls.maybeSemantics,
                           ls.maybeExtractedParams, ls.maybeSemanticTable, ls.maybeUnionQuery, ls.maybeLogicalPlan,
                           ls.maybePeriodicCommit, ls.accumulatedConditions)