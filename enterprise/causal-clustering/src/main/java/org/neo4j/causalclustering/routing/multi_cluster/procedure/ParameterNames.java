/*
 * Copyright (c) 2018-2020 "Graph Foundation"
 * Graph Foundation, Inc. [https://graphfoundation.org]
 *
 * Copyright (c) 2002-2018 "Neo4j,"
 * Neo4j Sweden AB [http://neo4j.com]
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
package org.neo4j.causalclustering.routing.multi_cluster.procedure;

/**
 * Enumerates the parameter names used for the multi-cluster routing procedures
 */
public enum ParameterNames
{
    /**
     * Type: IN
     *
     * An string specifying the database in the multi-cluster for which to provide routers.
     */
    DATABASE( "database" ),

    /**
     * Type: OUT
     *
     * Defines the time-to-live of the returned information,
     * after which it shall be refreshed.
     *
     * Refer to the specific routing plugin deployed to
     * understand the impact of this setting.
     */
    TTL( "ttl" ),

    /**
     * Type: OUT
     *
     * Contains a multimap of database names to router endpoints.
     */
    ROUTERS( "routers" );

    private final String parameterName;

    ParameterNames( String parameterName )
    {
        this.parameterName = parameterName;
    }

    public String parameterName()
    {
        return parameterName;
    }
}
