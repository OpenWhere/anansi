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

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;

/**
 * Static factory methods for building Traversers.
 *
 * @author rconner
 */
@Beta
public class Traversers {

    /**
     * Prevent instantiation.
     */
    private Traversers() {
    }


    /**
     * Returns a Traverser that returns an empty Iterable for all inputs.
     *
     * @param <V> the vertex type
     * @param <E> the edge type
     *
     * @return a Traverser that returns an empty Iterable for all inputs.
     */
    @SuppressWarnings( "unchecked" )
    public static <V, E> Traverser<V, E> empty() {
        return ( Traverser<V, E> ) EmptyTraverser.INSTANCE;
    }

    private static final class EmptyTraverser<V, E> implements Traverser<V, E> {
        static final Traverser<?, ?> INSTANCE = new EmptyTraverser<Object, Object>();

        @Override
        public Iterable<Walk<V, E>> apply( final V input ) {
            return ImmutableSet.of();
        }
    }

    /**
     * Returns a pre-order traverser with <strong>NO</strong> cycle detection.
     *
     * @param adjacency the adjacency function to use
     * @param <V> the vertex type
     * @param <E> the edge type
     *
     * @return a pre-order traverser with <strong>NO</strong> cycle detection.
     */
    public static <V, E> Traverser<V, E> preOrder( final Traverser<V, E> adjacency ) {
        Preconditions.checkNotNull( adjacency );
        return new PreOrderTraverser<V, E>( Lazy.traverser( adjacency ) );
    }

    /**
     * Returns a post-order traverser with <strong>NO</strong> cycle detection. If a cycle is present, some call to
     * next() will infinitely loop (most likely resulting in an OutOfMemoryError).
     *
     * @param adjacency the adjacency function to use
     * @param <V> the vertex type
     * @param <E> the edge type
     *
     * @return a post-order traverser with <strong>NO</strong> cycle detection.
     */
    public static <V, E> Traverser<V, E> postOrder( final Traverser<V, E> adjacency ) {
        Preconditions.checkNotNull( adjacency );
        return new PostOrderTraverser<V, E>( Lazy.traverser( adjacency ) );
    }

    /**
     * Returns a breadth-first traverser with <strong>NO</strong> cycle detection.
     *
     * @param adjacency the adjacency function to use
     * @param <V> the vertex type
     * @param <E> the edge type
     *
     * @return a breadth-first traverser with <strong>NO</strong> cycle detection.
     */
    public static <V, E> Traverser<V, E> breadthFirst( final Traverser<V, E> adjacency ) {
        Preconditions.checkNotNull( adjacency );
        return new BreadthFirstTraverser<V, E>( Lazy.traverser( adjacency ) );
    }

    /**
     * Returns a traverser to reachable leaves with <strong>NO</strong> cycle detection. If a cycle is present, some
     * call to next() will infinitely loop (most likely resulting in an OutOfMemoryError).
     *
     * @param adjacency the adjacency function to use
     * @param <V> the vertex type
     * @param <E> the edge type
     *
     * @return a traverser to reachable leaves with <strong>NO</strong> cycle detection.
     */
    public static <V, E> Traverser<V, E> leaves( final Traverser<V, E> adjacency ) {
        Preconditions.checkNotNull( adjacency );
        return new LeafTraverser<V, E>( Lazy.traverser( adjacency ) );
    }

    /**
     * Returns a Traverser which will return the (adjacency Walks to) immediate Iterable elements, array elements, or
     * Map values for any such non-empty input. If the input is an empty Iterable, array, or Map, an empty Iterable will
     * be returned. If the input is not an Iterable, array, or Map, an empty Iterable will be returned. The values of
     * {@link Walk.Step#getOver()} are strings representing the path in normal idiomatic usage. Because they are used as
     * path separators, periods (property reference) and brackets (array indexing) are escaped with preceding
     * backslashes if they appear as Map keys.
     *
     * @return a Traverser which will return the (adjacency Walks to) immediate Iterable elements, array elements, or
     * Map values for any such non-empty input.
     */
    public static Traverser<Object, String> elements() {
        return Elements.ELEMENT_ADJACENCY;
    }

    /**
     * Returns a {@link #leaves(Traverser)} Traverser which uses {@link #elements()} as an adjacency Traverser.
     *
     * @return a {@code leaves(Traverser)} Traverser which uses {@code elements()} as an adjacency Traverser.
     */
    public static Traverser<Object, String> leafElements() {
        return Elements.LEAF_ELEMENTS_TRAVERSER;
    }

    /**
     * Returns an idiomatic String path for the given Walk produced by {@link #leafElements()}.
     *
     * @param walk the Walk for which to return the idiomatic String path.
     *
     * @return an idiomatic String path for the given Walk produced by {@code leafElements()}.
     */
    public static String elementPath( final Walk<Object, String> walk ) {
        return Elements.path( walk );
    }
}
