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
package org.neo4j.kernel.ha.lock;

import org.junit.Before;
import org.junit.Test;

import org.neo4j.kernel.availability.DatabaseAvailabilityGuard;
import org.neo4j.kernel.configuration.Config;
import org.neo4j.kernel.ha.com.RequestContextFactory;
import org.neo4j.kernel.ha.com.master.Master;
import org.neo4j.kernel.impl.locking.Locks;
import org.neo4j.kernel.impl.locking.community.CommunityLockManger;
import org.neo4j.kernel.impl.transaction.log.TransactionIdStore;
import org.neo4j.logging.NullLogProvider;
import org.neo4j.time.Clocks;

import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.neo4j.function.Suppliers.singleton;
import static org.neo4j.graphdb.factory.GraphDatabaseSettings.DEFAULT_DATABASE_NAME;
import static org.neo4j.logging.NullLog.getInstance;

public class SlaveLockManagerTest
{
    private RequestContextFactory requestContextFactory;
    private Master master;
    private DatabaseAvailabilityGuard databaseAvailabilityGuard;

    @Before
    public void setUp()
    {
        requestContextFactory = new RequestContextFactory( 1, singleton( mock( TransactionIdStore.class ) ) );
        master = mock( Master.class );
        databaseAvailabilityGuard = new DatabaseAvailabilityGuard( DEFAULT_DATABASE_NAME, Clocks.systemClock(), getInstance() );
    }

    @Test
    public void shutsDownLocalLocks()
    {
        Locks localLocks = mock( Locks.class );
        SlaveLockManager slaveLockManager = newSlaveLockManager( localLocks );

        slaveLockManager.close();

        verify( localLocks ).close();
    }

    @Test
    public void doesNotCreateClientsAfterShutdown()
    {
        SlaveLockManager slaveLockManager =
                newSlaveLockManager( new CommunityLockManger( Config.defaults(), Clocks.systemClock() ) );

        assertNotNull( slaveLockManager.newClient() );

        slaveLockManager.close();

        try
        {
            slaveLockManager.newClient();
            fail( "Exception expected" );
        }
        catch ( Exception e )
        {
            assertThat( e, instanceOf( IllegalStateException.class ) );
        }
    }

    private SlaveLockManager newSlaveLockManager( Locks localLocks )
    {
        return new SlaveLockManager( localLocks, requestContextFactory, master, databaseAvailabilityGuard,
                NullLogProvider.getInstance(), Config.defaults() );
    }
}
