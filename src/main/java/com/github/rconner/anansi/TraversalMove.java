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

import com.google.common.collect.Iterators;

import java.util.Iterator;

/**
 * A step during a traversal, a helper class. Holds an iterator (created by a delegate adjacency function) and a
 * Walk.Builder at that point in the traversal.
 */
final class TraversalMove<V, E> {
    final Iterator<Walk<V, E>> iterator;
    final Walk.Builder<V, E> builder;

    TraversalMove( final Iterator<Walk<V, E>> iterator, final Walk.Builder<V, E> builder ) {
        this.iterator = iterator;
        this.builder = builder;
    }

    /**
     * Returns a move &quot;to&quot; the start node in a traversal.
     *
     * @param start
     * @param <V>
     * @param <E>
     *
     * @return
     */
    static <V, E> TraversalMove<V, E> start( final V start ) {
        return new TraversalMove<V, E>(
                Iterators.singletonIterator( Walk.<V, E>empty( start ) ), Walk.<V, E>from( start ) );
    }

    /**
     * Returns a new move with the given walk appended, with the iterator from the adjacency function applied to the
     * walk&apos;s end vertex.
     *
     * @param adjacency
     * @param walk
     *
     * @return
     */
    TraversalMove<V, E> with( final Traverser<V, E> adjacency, final Walk<V, E> walk ) {
        return new TraversalMove<V, E>( adjacency.apply( walk.getTo() ).iterator(), builder.add( walk ) );
    }
}
