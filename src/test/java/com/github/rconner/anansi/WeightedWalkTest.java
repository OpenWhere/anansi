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

public final class WeightedWalkTest {

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

    static <V, E> void assertWalkContains( final WeightedWalk<V, E> actual, final double weight, final Object... expected ) {
        assertThat( actual.getWeight(), is( weight ) );
        assertWalkContains( actual.getWalk(), expected );
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
    public void empty() {
        assertWalkContains( WeightedWalk.empty( 5 ), 0.0, 5 );
    }

    @Test
    public void simpleOver() {
        assertWalkContains( WeightedWalk.empty( 5 ).append( 7, "abc", 2.3 ), 2.3, 5, "abc", 7 );
    }

    @Test
    public void newInstance() {
        final WeightedWalk<Integer, String> walk = WeightedWalk.newInstance( Walk.single( 5, 7, "abc" ), 99.0 );
        assertWalkContains( walk, 99.0, 5, "abc", 7 );
    }

    @Test( expected = NullPointerException.class )
    public void newInstanceNull() {
        WeightedWalk.newInstance( null, 99.0 );
    }

    @Test
    public void next() {
        final WeightedWalk<Integer, String> rootWalk = WeightedWalk.empty( 11 );
        assertWalkContains( rootWalk, 0.0, 11 );

        final WeightedWalk<Integer, String> walkTo13 = rootWalk.append( 13, null, 1.2 );
        assertWalkContains( walkTo13, 1.2, 11, null, 13 );

        final WeightedWalk<Integer, String> subWalkTo13 = WeightedWalk.empty( 13 );

        final WeightedWalk<Integer, String> stillWalkTo13 = walkTo13.append( subWalkTo13 );
        assertWalkContains( stillWalkTo13, 1.2, 11, null, 13 );

        final WeightedWalk<Integer, String> subWalkTo15 = subWalkTo13.append( 15, null, 3.4 );
        assertWalkContains( subWalkTo15, 3.4, 13, null, 15 );

        final WeightedWalk<Integer, String> walkTo15 = stillWalkTo13.append( subWalkTo15 );
        assertWalkContains( walkTo15, 4.6, 11, null, 13, null, 15 );

        final WeightedWalk<Integer, String> walkTo17 = walkTo15.append( 17, "to 17", 1.0 );
        assertWalkContains( walkTo17, 5.6, 11, null, 13, null, 15, "to 17", 17 );

        WeightedWalk<Integer, String> subWalkTo23 = WeightedWalk.empty( 17 );
        subWalkTo23 = subWalkTo23.append( WeightedWalk.<Integer, String>empty( 17 ).append( 19, "to 19", 1.0 ) );
        subWalkTo23 = subWalkTo23.append( WeightedWalk.<Integer, String>empty( 19 ).append( 21, "to 21", 2.0 ) );
        subWalkTo23 = subWalkTo23.append( WeightedWalk.<Integer, String>empty( 21 ).append( 23, "to 23", 3.0 ) );
        assertWalkContains( subWalkTo23, 6.0, 17, "to 19", 19, "to 21", 21, "to 23", 23 );

        final WeightedWalk<Integer, String> walkTo23 = walkTo17.append( subWalkTo23 );
        assertWalkContains( walkTo23, 11.6, 11, null, 13, null, 15, "to 17", 17, "to 19", 19, "to 21", 21, "to 23", 23 );

        // Test that all the previously built walks have not changed.
        assertWalkContains( rootWalk, 0.0, 11 );
        assertWalkContains( walkTo13, 1.2, 11, null, 13 );
        assertWalkContains( stillWalkTo13, 1.2, 11, null, 13 );
        assertWalkContains( subWalkTo15, 3.4, 13, null, 15 );
        assertWalkContains( walkTo15, 4.6, 11, null, 13, null, 15 );
        assertWalkContains( walkTo17, 5.6, 11, null, 13, null, 15, "to 17", 17 );
        assertWalkContains( subWalkTo23, 6.0, 17, "to 19", 19, "to 21", 21, "to 23", 23 );
        assertWalkContains( walkTo23, 11.6, 11, null, 13, null, 15, "to 17", 17, "to 19", 19, "to 21", 21, "to 23", 23 );
    }

    @Test
    public void walkToString() {
        // Not asserting content of toString here, just that it doesn't throw.
        WeightedWalk.empty( 2 ).toString();
        WeightedWalk.empty( 2 ).append( 3, null, 42.0 ).toString();
        WeightedWalk.empty( 2 ).append( 3, "2->3", 42.0 ).toString();

        WeightedWalk<Integer, String> walk = WeightedWalk.empty( 11 );
        walk.toString();
        walk = walk.append( 13, "11->13", 5.723 );
        walk.toString();
    }
}
