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
package org.neo4j.consistency.checking;

import org.junit.jupiter.api.Test;

import org.neo4j.consistency.report.ConsistencyReport.PropertyKeyTokenConsistencyReport;
import org.neo4j.kernel.impl.store.record.DynamicRecord;
import org.neo4j.kernel.impl.store.record.PropertyKeyTokenRecord;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

class PropertyKeyTokenRecordCheckTest extends
        RecordCheckTestBase<PropertyKeyTokenRecord, PropertyKeyTokenConsistencyReport, PropertyKeyTokenRecordCheck>
{
    PropertyKeyTokenRecordCheckTest()
    {
        super( new PropertyKeyTokenRecordCheck(), PropertyKeyTokenConsistencyReport.class, new int[0] );
    }

    @Test
    void shouldNotReportAnythingForRecordNotInUse()
    {
        // given
        PropertyKeyTokenRecord key = notInUse( new PropertyKeyTokenRecord( 42 ) );

        // when
        PropertyKeyTokenConsistencyReport report = check( key );

        // then
        verifyNoMoreInteractions( report );
    }

    @Test
    void shouldNotReportAnythingForRecordThatDoesNotReferenceADynamicBlock()
    {
        // given
        PropertyKeyTokenRecord key = inUse( new PropertyKeyTokenRecord( 42 ) );

        // when
        PropertyKeyTokenConsistencyReport report = check( key );

        // then
        verifyNoMoreInteractions( report );
    }

    @Test
    void shouldReportDynamicBlockNotInUse()
    {
        // given
        PropertyKeyTokenRecord key = inUse( new PropertyKeyTokenRecord( 42 ) );
        DynamicRecord name = addKeyName( notInUse( new DynamicRecord( 6 ) ) );
        key.setNameId( (int) name.getId() );

        // when
        PropertyKeyTokenConsistencyReport report = check( key );

        // then
        verify( report ).nameBlockNotInUse( name );
        verifyNoMoreInteractions( report );
    }

    @Test
    void shouldReportEmptyName()
    {
        // given
        PropertyKeyTokenRecord key = inUse( new PropertyKeyTokenRecord( 42 ) );
        DynamicRecord name = addKeyName( inUse( new DynamicRecord( 6 ) ) );
        key.setNameId( (int) name.getId() );

        // when
        PropertyKeyTokenConsistencyReport report = check( key );

        // then
        verify( report ).emptyName( name );
        verifyNoMoreInteractions( report );
    }
}
