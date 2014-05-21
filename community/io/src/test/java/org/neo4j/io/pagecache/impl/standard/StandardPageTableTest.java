/**
 * Copyright (c) 2002-2014 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
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
package org.neo4j.io.pagecache.impl.standard;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.neo4j.io.pagecache.PageLock;


import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import static org.neo4j.io.pagecache.impl.standard.PageTable.PinnablePage;

public class StandardPageTableTest
{
    private StandardPageTable table;
    private Thread sweeperThread;

    @Before
    public void startPageTable()
    {
        table = new StandardPageTable( 1024 );
        sweeperThread = new Thread( table );
        sweeperThread.start();
    }

    @After
    public void stopPageTable()
    {
        sweeperThread.interrupt();
    }

    @Test
    public void loading_must_read_file() throws Exception
    {
        // Given
        StandardPageTable table = new StandardPageTable( 1024 );
        byte[] expected = "Hello, cruel world!".getBytes( "UTF-8" );
        BufferPageIO io = new BufferPageIO( ByteBuffer.wrap( expected ) );

        // When
        PinnablePage page = table.load( io, 1, PageLock.EXCLUSIVE );

        // Then
        byte[] actual = new byte[expected.length];
        page.getBytes( actual, 0 );

        assertThat( actual, equalTo( expected ));
    }

    @Test
    public void loading_with_shared_lock_allows_other_shared() throws Exception
    {
        // Given
        StandardPageTable table = new StandardPageTable( 1024 );
        BufferPageIO io = new BufferPageIO( ByteBuffer.wrap( "expected".getBytes("UTF-8") ) );

        // When
        PinnablePage page = table.load( io, 12, PageLock.SHARED );

        // Then we should be able to grab another shared lock on it
        assertTrue( page.pin( io, 12, PageLock.SHARED ) );
    }

    @Test(timeout = 1000)
    public void loading_with_shared_lock_stops_exclusive_pinners() throws Exception
    {
        // Given
        StandardPageTable table = new StandardPageTable( 1024 );
        final BufferPageIO io = new BufferPageIO( ByteBuffer.wrap( "expected".getBytes("UTF-8") ) );

        // When
        final PinnablePage page = table.load( io, 12, PageLock.SHARED );

        // Then we should have to wait for the page to be unpinned if we want an
        // exclusive lock on it.
        final AtomicBoolean acquiredPage = new AtomicBoolean( false );
        Thread otherThread = new Thread( new Runnable()
        {
            @Override
            public void run()
            {
                page.pin( io, 12, PageLock.EXCLUSIVE );
                acquiredPage.set( true );
            }
        } );
        otherThread.start();
        awaitThreadState( otherThread, Thread.State.WAITING );

        assertFalse( acquiredPage.get() );

        // And when I unpin mine, the other thread should get it
        page.unpin( PageLock.SHARED );

        otherThread.join();

        assertTrue( acquiredPage.get() );
    }

    @Test(timeout = 1000)
    public void loading_with_exclusive_lock_stops_all_others() throws Exception
    {
        // Given
        StandardPageTable table = new StandardPageTable( 1024 );
        final BufferPageIO io = new BufferPageIO( ByteBuffer.wrap( "expected".getBytes("UTF-8") ) );

        // When
        final PinnablePage page = table.load( io, 12, PageLock.EXCLUSIVE );

        // Then we should have to wait for the page to be unpinned if we want an
        // exclusive lock on it.
        final AtomicBoolean acquiredPage = new AtomicBoolean( false );
        Thread otherThread = new Thread( new Runnable()
        {
            @Override
            public void run()
            {
                page.pin( io, 12, PageLock.SHARED );
                acquiredPage.set( true );
            }
        } );
        otherThread.start();
        awaitThreadState( otherThread, Thread.State.WAITING );

        assertFalse( acquiredPage.get() );

        // And when I unpin mine, the other thread should get it
        page.unpin( PageLock.EXCLUSIVE );

        otherThread.join();

        assertTrue( acquiredPage.get() );
    }

    private void awaitThreadState( Thread otherThread, Thread.State state ) throws InterruptedException
    {
        do
        {
            otherThread.join( 100 );
        } while(otherThread.getState() != state );
    }

    @Test(timeout = 1000)
    public void pinning_replaced_page_must_fail() throws Exception
    {
        // Given
        final byte[] expectedBytes = "expected".getBytes( "UTF-8" );
        final byte[] otherBytes = "rare brand string".getBytes( "UTF-8" );
        BufferPageIO io = new BufferPageIO( ByteBuffer.wrap( expectedBytes ) );

        PinnablePage page = table.load( io, 12, PageLock.SHARED );
        page.unpin( PageLock.SHARED );

        // When
        Thread thread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                // This thread will cause the single page in the cache to be replaced
                BufferPageIO io = new BufferPageIO( ByteBuffer.wrap( otherBytes ) );
                table.load( io, 3, PageLock.SHARED ).unpin( PageLock.SHARED );
            }
        });
        thread.start();
        thread.join();

        // Then
        assertFalse( page.pin( io, 12, PageLock.SHARED ) );
    }

    // TODO evicting page should not allow readers or writers access

    private class BufferPageIO implements PageTable.PageIO
    {
        private final ByteBuffer buffer;

        private BufferPageIO( ByteBuffer buffer )
        {
            this.buffer = buffer;
        }

        @Override
        public void read( long pageId, ByteBuffer into )
        {
            buffer.position(0);
            into.put( buffer );
        }

        @Override
        public void write( long pageId, ByteBuffer from )
        {
            throw new UnsupportedOperationException(  );
        }
    }
}
