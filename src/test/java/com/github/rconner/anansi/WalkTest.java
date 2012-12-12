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

    static <V, E> void assertWalkContains( final Walk<V, E> actual, final Object... expected ) {
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
        Walk.Builder<Integer, String> rootBuilder = Walk.from( 11 );
        final Walk<Integer, String> rootWalk = rootBuilder.build();
        assertWalkContains( rootWalk, 11 );

        rootBuilder = rootBuilder.add( Walk.single( 11, 13, ( String ) null ) );
        final Walk<Integer, String> walkTo13 = rootBuilder.build();
        assertWalkContains( walkTo13, 11, null, 13 );

        Walk.Builder<Integer, String> subBuilderFrom13 = Walk.from( 13 );
        final Walk<Integer, String> subWalkTo13 = subBuilderFrom13.build();

        rootBuilder = rootBuilder.add( subWalkTo13 );
        final Walk<Integer, String> stillWalkTo13 = rootBuilder.build();
        assertWalkContains( stillWalkTo13, 11, null, 13 );

        subBuilderFrom13 = subBuilderFrom13.add( Walk.single( 13, 15, ( String ) null ) );
        final Walk<Integer, String> subWalkTo15 = subBuilderFrom13.build();
        assertWalkContains( subWalkTo15, 13, null, 15 );

        rootBuilder = rootBuilder.add( subWalkTo15 );
        final Walk<Integer, String> walkTo15 = rootBuilder.build();
        assertWalkContains( walkTo15, 11, null, 13, null, 15 );

        rootBuilder = rootBuilder.add( Walk.single( 15, 17, "to 17" ) );
        final Walk<Integer, String> walkTo17 = rootBuilder.build();
        assertWalkContains( walkTo17, 11, null, 13, null, 15, "to 17", 17 );

        Walk.Builder<Integer, String> subBuilderFrom17 = Walk.from( 17 );
        subBuilderFrom17 = subBuilderFrom17.add( Walk.single( 17, 19, "to 19" ) );
        subBuilderFrom17 = subBuilderFrom17.add( Walk.single( 19, 21, "to 21" ) );
        subBuilderFrom17 = subBuilderFrom17.add( Walk.single( 21, 23, "to 23" ) );
        final Walk<Integer, String> subWalkTo23 = subBuilderFrom17.build();
        assertWalkContains( subWalkTo23, 17, "to 19", 19, "to 21", 21, "to 23", 23 );

        rootBuilder = rootBuilder.add( subWalkTo23 );
        final Walk<Integer, String> walkTo23 = rootBuilder.build();
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
}
