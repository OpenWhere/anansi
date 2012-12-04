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

import com.google.common.collect.Iterables;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class WalkTest {

    private static <V, E> void assertWalkContains( final Walk<V, E> actual, final Object... expected ) {
        assertThat( actual.getFrom(), is( expected[ 0 ] ) );
        assertThat( actual.getTo(), is( expected[ expected.length - 1 ] ) );
        assertThat( Iterables.size( actual.getVia() ), is( ( expected.length - 1 ) / 2 ) );
        int i = 1;
        for( Walk.Step<V, E> step : actual.getVia() ) {
            assertThat( step.getOver(), is( expected[ i ] ) );
            assertThat( step.getTo(), is( expected[ i + 1 ] ) );
            i += 2;
        }
    }

    @Test
    public void simple() {
        assertWalkContains( Walk.single( 2, 3 ), 2, null, 3 );
    }

    @Test
    public void simpleOver() {
        assertWalkContains( Walk.single( 5, 7, "abc" ), 5, "abc", 7 );
    }

    @Test
    @SuppressWarnings( "unchecked" )
    public void builder() {
        final Walk.Builder<Integer, String> rootBuilder = Walk.from( 11 );
        final Walk<Integer, String> rootWalk = rootBuilder.build();
        assertWalkContains( rootWalk, 11 );

        rootBuilder.add( 13, null );
        final Walk<Integer, String> walkTo13 = rootBuilder.build();
        assertWalkContains( walkTo13, 11, null, 13 );

        rootBuilder.add( 17, "to 17" );
        final Walk<Integer, String> walkTo17 = rootBuilder.build();
        assertWalkContains( walkTo17, 11, null, 13, "to 17", 17 );

        // Test that all the previously built walks have not changed.
        assertWalkContains( rootWalk, 11 );
        assertWalkContains( walkTo13, 11, null, 13 );
        assertWalkContains( walkTo17, 11, null, 13, "to 17", 17 );
    }
}
