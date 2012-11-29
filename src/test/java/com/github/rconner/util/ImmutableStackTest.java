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

package com.github.rconner.util;

import com.google.common.collect.Lists;
import org.junit.Test;

import java.util.Arrays;
import java.util.EmptyStackException;

import static com.github.rconner.util.IterableTest.assertIteratorContains;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class ImmutableStackTest {

    private static <E> void assertStackContains( ImmutableStack<E> stack, E... elements ) {

        // These are repeated intentionally, to make sure invoking iterator() or reverse() does not change state.

        for( E element : elements ) {
            assertThat( stack.contains( element ), is( true ) );
        }
        assertThat( stack.contains( new Object() ), is( false ) );

        assertIteratorContains( stack.iterator(), elements );
        assertIteratorContains( stack.iterator(), elements );

        Iterable<E> reverse = stack.reverse();
        Object[] expectedReverse = Lists.reverse( Lists.newArrayList( elements ) ).toArray();
        assertIteratorContains( reverse.iterator(), expectedReverse );
        assertIteratorContains( reverse.iterator(), expectedReverse );

        assertThat( stack.size(), is( elements.length ) );

        if( elements.length == 0 ) {
            assertThat( stack.isEmpty(), is( true ) );
            try {
                stack.peek();
                fail( "Should throw EmptyStackException." );
            } catch( EmptyStackException ignored ) {
                // expected
            }
            try {
                stack.pop();
                fail( "Should throw EmptyStackException." );
            } catch( EmptyStackException ignored ) {
                // expected
            }
        } else {
            assertThat( stack.isEmpty(), is( false ) );
            assertThat( stack.peek(), is( elements[ 0 ] ) );
            assertStackContains( stack.pop(), Arrays.copyOfRange( elements, 1, elements.length ) );
        }

        // Test them again to make sure following the head/tail references didn't change state.

        assertIteratorContains( stack.iterator(), elements );
        assertIteratorContains( stack.reverse().iterator(), expectedReverse );
        assertThat( stack.size(), is( elements.length ) );
    }

    @Test
    @SuppressWarnings( "unchecked" )
    public void testEmpty() {
        ImmutableStack<Integer> stack = ImmutableStack.of();
        assertStackContains( stack );
        assertStackContains( stack.push( 101 ).push( 102 ), 102, 101 );
        assertStackContains( stack );
    }

    @Test
    public void testSingle() {
        ImmutableStack<Integer> stack = ImmutableStack.of( 42 );
        assertStackContains( stack, 42 );
        assertStackContains( stack.push( 101 ).push( 102 ), 102, 101, 42 );
        assertStackContains( stack, 42 );
    }

    @Test
    public void testMany() {
        ImmutableStack<Integer> stack = ImmutableStack.of( 11, null, 5, 3, 2 );
        assertStackContains( stack, 2, 3, 5, null, 11 );
        assertStackContains( stack.push( 101 ).push( null ).push( 102 ), 102, null, 101, 2, 3, 5, null, 11 );
        assertStackContains( stack, 2, 3, 5, null, 11 );
    }
}
