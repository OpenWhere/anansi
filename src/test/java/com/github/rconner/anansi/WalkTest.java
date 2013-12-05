/*
 * Copyright (c) 2012-2013 Ray A. Conner
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

    static <V, E> void assertWalkContains( final Walk<V, E> actual, final Object... expected ) {
        assertThat( actual.getFrom(), is( expected[ 0 ] ) );
        assertThat( actual.getTo(), is( expected[ expected.length - 1 ] ) );
        assertThat( Iterables.size( actual.getVia() ), is( ( expected.length - 1 ) / 2 ) );
        int i = expected.length - 2;
        for( final Walk.Step<V, E> step : actual.getVia() ) {
            assertThat( step.getOver(), is( expected[ i ] ) );
            assertThat( step.getTo(), is( expected[ i + 1 ] ) );
            i -= 2;
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
    public void next() {
        final Walk<Integer, String> rootWalk = Walk.empty( 11 );
        assertWalkContains( rootWalk, 11 );

        final Walk<Integer, String> walkTo13 = rootWalk.append( 13 );
        assertWalkContains( walkTo13, 11, null, 13 );

        final Walk<Integer, String> subWalkTo13 = Walk.empty( 13 );

        final Walk<Integer, String> stillWalkTo13 = walkTo13.append( subWalkTo13 );
        assertWalkContains( stillWalkTo13, 11, null, 13 );

        final Walk<Integer, String> subWalkTo15 = subWalkTo13.append( 15 );
        assertWalkContains( subWalkTo15, 13, null, 15 );

        final Walk<Integer, String> walkTo15 = stillWalkTo13.append( subWalkTo15 );
        assertWalkContains( walkTo15, 11, null, 13, null, 15 );

        final Walk<Integer, String> walkTo17 = walkTo15.append( 17, "to 17" );
        assertWalkContains( walkTo17, 11, null, 13, null, 15, "to 17", 17 );

        Walk<Integer, String> subWalkTo23 = Walk.empty( 17 );
        subWalkTo23 = subWalkTo23.append( Walk.single( 17, 19, "to 19" ) );
        subWalkTo23 = subWalkTo23.append( Walk.single( 19, 21, "to 21" ) );
        subWalkTo23 = subWalkTo23.append( Walk.single( 21, 23, "to 23" ) );
        assertWalkContains( subWalkTo23, 17, "to 19", 19, "to 21", 21, "to 23", 23 );

        final Walk<Integer, String> walkTo23 = walkTo17.append( subWalkTo23 );
        assertWalkContains( walkTo23, 11, null, 13, null, 15, "to 17", 17, "to 19", 19, "to 21", 21, "to 23", 23 );

        // Test that all the previously built walks have not changed.
        assertWalkContains( rootWalk, 11 );
        assertWalkContains( walkTo13, 11, null, 13 );
        assertWalkContains( stillWalkTo13, 11, null, 13 );
        assertWalkContains( subWalkTo15, 13, null, 15 );
        assertWalkContains( walkTo15, 11, null, 13, null, 15 );
        assertWalkContains( walkTo17, 11, null, 13, null, 15, "to 17", 17 );
        assertWalkContains( subWalkTo23, 17, "to 19", 19, "to 21", 21, "to 23", 23 );
        assertWalkContains( walkTo23, 11, null, 13, null, 15, "to 17", 17, "to 19", 19, "to 21", 21, "to 23", 23 );
    }

    @Test
    public void walkToString() {
        // Not asserting content of toString here, just that it doesn't throw.
        Walk.empty( 2 ).toString();
        Walk.single( 2, 3 ).toString();
        Walk.single( 2, 3, "2->3" ).toString();

        Walk<Integer, String> walk = Walk.empty( 11 );
        walk.toString();
        walk = walk.append( 13, "11->13" );
        walk.toString();
    }
}
