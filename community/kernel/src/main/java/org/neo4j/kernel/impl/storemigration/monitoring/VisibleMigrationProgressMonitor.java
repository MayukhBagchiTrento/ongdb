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
package org.neo4j.kernel.impl.storemigration.monitoring;

import java.time.Clock;

import org.neo4j.kernel.impl.util.monitoring.LogProgressReporter;
import org.neo4j.kernel.impl.util.monitoring.ProgressReporter;
import org.neo4j.logging.Log;

import static java.lang.String.format;
import static org.neo4j.helpers.Format.duration;

public class VisibleMigrationProgressMonitor implements MigrationProgressMonitor
{
    static final String MESSAGE_STARTED = "Starting upgrade of database";
    static final String MESSAGE_COMPLETED = "Successfully finished upgrade of database";
    private static final String MESSAGE_COMPLETED_WITH_DURATION = MESSAGE_COMPLETED + ", took %s";

    private final Log log;
    private final Clock clock;
    private int numStages;
    private int currentStage;
    private long startTime;

    public VisibleMigrationProgressMonitor( Log log )
    {
        this( log, Clock.systemUTC() );
    }

    VisibleMigrationProgressMonitor( Log log, Clock clock )
    {
        this.log = log;
        this.clock = clock;
    }

    @Override
    public void started( int numStages )
    {
        this.numStages = numStages;
        log.info( MESSAGE_STARTED );
        startTime = clock.millis();
    }

    @Override
    public ProgressReporter startSection( String name )
    {
        log.info( format( "Migrating %s (%d/%d):", name, ++currentStage, numStages ) );
        return new LogProgressReporter( log );
    }

    @Override
    public void completed()
    {
        long time = clock.millis() - startTime;
        log.info( MESSAGE_COMPLETED_WITH_DURATION, duration( time ) );
    }
}
