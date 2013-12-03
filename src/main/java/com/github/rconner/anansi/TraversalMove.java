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

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A step during a traversal, a helper class. Holds an iterator (created by a delegate adjacency function) and a
 * Walk at that point in the traversal.
 *
 * @param <V> the vertex type
 * @param <E> the edge type
 *
 * @author rconner
 */
final class TraversalMove<V, E> {
    final Iterator<Walk<V, E>> iterator;
    final Walk<V, E> walk;

    TraversalMove( final Iterator<Walk<V, E>> iterator, final Walk<V, E> walk ) {
        this.iterator = iterator;
        this.walk = walk;
    }

    /**
     * Returns a move &quot;to&quot; the start node in a traversal.
     *
     * @param start the start node in a traversal
     * @param <V> the vertex type
     * @param <E> the edge type
     *
     * @return a move &quot;to&quot; the start node in a traversal.
     */
    static <V, E> TraversalMove<V, E> start( final V start ) {
        return new TraversalMove<V, E>( rootIterator( Walk.<V, E>empty( start ) ), Walk.<V, E>empty( start ) );
    }

    /**
     * Returns a new move with the next walk appended, with the iterator from the adjacency function applied to the
     * walk&apos;s end vertex.
     *
     * @param adjacency the adjacency function to apply to the next walk&apos;s end vertex
     *
     * @return a new move with the next walk appended.
     */
    TraversalMove<V, E> next( final Traverser<V, E> adjacency ) {
        final Walk<V, E> nextWalk = iterator.next();
        return new TraversalMove<V, E>( adjacency.apply( nextWalk.getTo() ).iterator(), this.walk.append( nextWalk ) );
    }

    private static <T> Iterator<T> rootIterator( final T value ) {
        return new Iterator<T>() {
            boolean done;

            @Override
            public boolean hasNext() {
                return !done;
            }

            @Override
            public T next() {
                if( done ) {
                    throw new NoSuchElementException();
                }
                done = true;
                return value;
            }

            @Override
            public void remove() {
                throw new IllegalStateException();
            }
        };
    }
}
