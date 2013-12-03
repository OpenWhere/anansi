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
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Random;

import static com.github.rconner.util.IterableTest.assertIterablesEqual;
import static com.github.rconner.util.IterableTest.assertIteratorContains;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public final class FifoQueueTest {

    private static <E> void assertQueueContains( final FifoQueue<E> queue, final E... elements ) {

        // These are repeated intentionally, to make sure invoking iterator() or reverse() does not change state.

        assertIteratorContains( queue.iterator(), elements );
        assertIteratorContains( queue.iterator(), elements );

        assertThat( queue.size(), is( elements.length ) );

        if( elements.length == 0 ) {
            assertThat( queue.isEmpty(), is( true ) );
            try {
                queue.head();
                fail( "Should throw NoSuchElementException." );
            } catch( NoSuchElementException ignored ) {
                // expected
            }
            try {
                queue.dequeue();
                fail( "Should throw NoSuchElementException." );
            } catch( NoSuchElementException ignored ) {
                // expected
            }
        } else {
            assertThat( queue.isEmpty(), is( false ) );
            assertThat( queue.head(), is( elements[ 0 ] ) );
        }

        // Test them again to make sure following the head/tail references didn't change state.

        assertIteratorContains( queue.iterator(), elements );
        assertThat( queue.size(), is( elements.length ) );
    }

    private static <E> void testDequeue( final FifoQueue<E> queue, final E... elements ) {
        assertQueueContains( queue, elements );
        if( elements.length == 0 ) {
            return;
        }
        final E element = queue.dequeue();
        assertThat( element, is( elements[ 0 ] ) );
        testDequeue( queue, Arrays.copyOfRange( elements, 1, elements.length ) );
    }

    @Test
    public void queueEmpty() {
        final FifoQueue<Integer> queue = FifoQueue.of();
        assertQueueContains( queue );
        queue.enqueue( 101 );
        queue.enqueue( 102 );
        assertQueueContains( queue, 101, 102 );
        testDequeue( queue, 101, 102 );
    }

    @Test
    public void queueOneElement() {
        final FifoQueue<Integer> queue = FifoQueue.of( 42 );
        assertQueueContains( queue, 42 );
        queue.enqueue( 101 );
        queue.enqueue( 102 );
        assertQueueContains( queue, 42, 101, 102 );
        testDequeue( queue, 42, 101, 102 );
    }

    @Test
    public void queueManyElements() {
        final FifoQueue<Integer> queue = FifoQueue.of( 2, 3, 5, null, 11 );
        assertQueueContains( queue, 2, 3, 5, null, 11 );
        queue.enqueue( 101 );
        queue.enqueue( null );
        queue.enqueue( 102 );
        assertQueueContains( queue, 2, 3, 5, null, 11, 101, null, 102 );
        testDequeue( queue, 2, 3, 5, null, 11, 101, null, 102 );
    }

    @Test
    public void queueRandomMutations() {
        final FifoQueue<Integer> queue = new FifoQueue<Integer>();
        final LinkedList<Integer> expected = Lists.newLinkedList();
        final Random random = new Random();

        // Put some initial values in.
        for( int i = 0; i < 1000; i++ ) {
            final Integer element = random.nextInt();
            queue.enqueue( element );
            expected.addLast( element );
        }
        assertIterablesEqual( queue, expected );
        assertThat( queue.size(), is( expected.size() ) );

        // Randomly enqueue and dequeue.
        // The number of iterations here must be no more than the number of initial values,
        // because we're not testing for an empty queue.
        for( int i = 0; i < 1000; i++ ) {
            if( random.nextBoolean() ) {
                final Integer element = random.nextInt();
                queue.enqueue( element );
                expected.addLast( element );
            } else {
                final Integer head = queue.head();
                assertThat( queue.dequeue(), is( head ) );
                assertThat( expected.removeFirst(), is( head ) );
            }
        }
        assertIterablesEqual( queue, expected );
        assertThat( queue.size(), is( expected.size() ) );
    }

    @Test
    public void queueToString() {
        final FifoQueue<Integer> queue = FifoQueue.of();
        assertThat( queue.toString(), is( "[]" ) );
        queue.enqueue( 101 );
        assertThat( queue.toString(), is( "[101]" ) );
        queue.enqueue( 102 );
        assertThat( queue.toString(), is( "[101, 102]" ) );
        queue.dequeue();
        assertThat( queue.toString(), is( "[102]" ) );
        queue.dequeue();
        assertThat( queue.toString(), is( "[]" ) );
    }
}
