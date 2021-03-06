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
package org.neo4j.kernel.impl.query;

import org.neo4j.graphdb.Result;
import org.neo4j.values.virtual.MapValue;

public interface QueryExecutionEngine
{
    Result executeQuery( String query, MapValue parameters, TransactionalContext context )
            throws QueryExecutionKernelException;

    Result profileQuery( String query, MapValue parameters, TransactionalContext context )
            throws QueryExecutionKernelException;

    /**
     * @return {@code true} if the query is a PERIODIC COMMIT query and not an EXPLAIN query
     */
    boolean isPeriodicCommit( String query );

    long clearQueryCaches();
}

