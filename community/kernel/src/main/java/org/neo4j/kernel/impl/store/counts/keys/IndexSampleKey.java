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
package org.neo4j.kernel.impl.store.counts.keys;

import org.neo4j.kernel.impl.api.CountsVisitor;

public final class IndexSampleKey extends IndexKey
{
    IndexSampleKey( long indexId )
    {
        super( indexId, CountsKeyType.INDEX_SAMPLE );
    }

    @Override
    public void accept( CountsVisitor visitor, long unique, long size )
    {
        visitor.visitIndexSample( indexId(), unique, size );
    }

    @Override
    public int compareTo( CountsKey other )
    {
        if ( other instanceof IndexSampleKey )
        {
            return super.compareTo( other );
        }
        return recordType().compareTo( other.recordType() );
    }
}
