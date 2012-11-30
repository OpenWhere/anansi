/*
 * Copyright (c) 2012 Ray A. Conner
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.github.rconner.anansi;

import org.junit.Test;

import java.util.Iterator;

import static com.github.rconner.util.IterableTest.assertIteratorEmpty;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

public final class WalkTest {

    @Test
    public void simple() {
        final Walk<Integer, ?> walk = Walk.newInstance( 2, 3 );
        assertThat( walk.getFrom(), is( 2 ) );
        assertThat( walk.getTo(), is( 3 ) );
        assertThat( walk.getOver(), nullValue() );
    }

    @Test
    public void simpleOver() {
        final Walk<Integer, String> walk = Walk.newInstance( 5, 7, "abc" );
        assertThat( walk.getFrom(), is( 5 ) );
        assertThat( walk.getTo(), is( 7 ) );
        assertThat( walk.getOver(), is( "abc" ) );
    }

    private static <V, E> void assertWalkContains( final Walk<V, Iterable<Walk<V, E>>> actual, final Walk<V, E>... expected ) {
        assertThat( actual.getFrom(), is( expected[ 0 ].getFrom() ) );
        assertThat( actual.getTo(), is( expected[ expected.length - 1 ].getTo() ) );

        final Iterator<Walk<V, E>> iterator = actual.getOver().iterator();
        for( final Walk<V, E> expectedSubWalk : expected ) {
            final Walk<V, E> actualSubWalk = iterator.next();
            assertThat( actualSubWalk.getFrom(), is( expectedSubWalk.getFrom() ) );
            assertThat( actualSubWalk.getTo(), is( expectedSubWalk.getTo() ) );
            assertThat( actualSubWalk.getOver(), is( expectedSubWalk.getOver() ) );
        }
        assertIteratorEmpty( iterator );
    }

    private static <V, E> void assertWalkEmpty( final Walk<V, Iterable<Walk<V, E>>> actual, final V root ) {
        assertThat( actual.getFrom(), is( root ) );
        assertThat( actual.getTo(), is( root ) );
        assertIteratorEmpty( actual.getOver().iterator() );
    }

    @Test
    @SuppressWarnings( "unchecked" )
    public void builder() {
        final Walk.Builder<Integer, String> rootBuilder = Walk.from( 11 );
        final Walk<Integer, Iterable<Walk<Integer, String>>> rootWalk = rootBuilder.build();
        assertWalkEmpty( rootWalk, 11 );

        rootBuilder.add( Walk.<Integer, String>newInstance( 11, 13 ) );
        final Walk<Integer, Iterable<Walk<Integer, String>>> walkTo13 = rootBuilder.build();
        assertWalkContains( walkTo13, Walk.<Integer, String>newInstance( 11, 13 ) );

        rootBuilder.add( Walk.newInstance( 13, 17, "to 17" ) );
        final Walk<Integer, Iterable<Walk<Integer, String>>> walkTo17 = rootBuilder.build();
        assertWalkContains( walkTo17, Walk.<Integer, String>newInstance( 11, 13 ), Walk.newInstance( 13, 17, "to 17" ) );

        // Test that all the previously built walks have not changed.
        assertWalkEmpty( rootWalk, 11 );
        assertWalkContains( walkTo13, Walk.<Integer, String>newInstance( 11, 13 ) );
        assertWalkContains( walkTo17, Walk.<Integer, String>newInstance( 11, 13 ), Walk.newInstance( 13, 17, "to 17" ) );
    }
}
