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

package com.github.rconner.util;

import com.google.common.collect.Lists;
import org.junit.Test;

import java.util.Arrays;
import java.util.NoSuchElementException;

import static com.github.rconner.util.IterableTest.assertIteratorContains;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public final class PersistentListTest {

    private static <E> void assertStackContains( final PersistentList<E> stack, final E... elements ) {

        // These are repeated intentionally, to make sure invoking iterator() or reverse() does not change state.

        assertIteratorContains( stack.iterator(), elements );
        assertIteratorContains( stack.iterator(), elements );

        final Iterable<E> reverse = stack.reverse();
        final Object[] expectedReverse = Lists.reverse( Lists.newArrayList( elements ) ).toArray();
        assertIteratorContains( reverse.iterator(), expectedReverse );
        assertIteratorContains( reverse.iterator(), expectedReverse );

        if( elements.length == 0 ) {
            assertThat( stack.isEmpty(), is( true ) );
            try {
                stack.first();
                fail( "Should throw NoSuchElementException." );
            } catch( NoSuchElementException ignored ) {
                // expected
            }
            try {
                stack.rest();
                fail( "Should throw NoSuchElementException." );
            } catch( NoSuchElementException ignored ) {
                // expected
            }
        } else {
            assertThat( stack.isEmpty(), is( false ) );
            assertThat( stack.first(), is( elements[ 0 ] ) );
            assertStackContains( stack.rest(), Arrays.copyOfRange( elements, 1, elements.length ) );
        }

        // Test them again to make sure following the head/tail references didn't change state.

        assertIteratorContains( stack.iterator(), elements );
        assertIteratorContains( stack.reverse().iterator(), expectedReverse );
    }

    @Test
    public void stackEmpty() {
        final PersistentList<Integer> stack = PersistentList.of();
        assertStackContains( stack );
        assertStackContains( stack.add( 101 ).add( 102 ), 102, 101 );
        assertStackContains( stack );
    }

    @Test
    public void stackOneElement() {
        final PersistentList<Integer> stack = PersistentList.of( 42 );
        assertStackContains( stack, 42 );
        assertStackContains( stack.add( 101 ).add( 102 ), 102, 101, 42 );
        assertStackContains( stack, 42 );
    }

    @Test
    public void stackManyElements() {
        final PersistentList<Integer> stack = PersistentList.of( 11, null, 5, 3, 2 );
        assertStackContains( stack, 2, 3, 5, null, 11 );
        assertStackContains( stack.add( 101 ).add( null ).add( 102 ), 102, null, 101, 2, 3, 5, null, 11 );
        assertStackContains( stack, 2, 3, 5, null, 11 );
    }

    @Test
    public void stackToString() {
        final PersistentList<Integer> stack = PersistentList.of();
        assertThat( stack.toString(), is( "[]" ) );
        assertThat( stack.add( 101 ).add( 102 ).toString(), is( "[102, 101]" ) );
    }
}
