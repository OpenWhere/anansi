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

import com.github.rconner.util.NoCoverage;
import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.TreeTraverser;

import java.util.Iterator;

/**
 * Static factory methods for building Traversers.
 *
 * @author rconner
 */
@Beta
public final class Traversers {

    /**
     * Prevent instantiation.
     */
    @NoCoverage
    private Traversers() {
    }

    /**
     * Returns a TreeTraverser that returns an empty Iterable for all inputs.
     *
     * @param <T> the vertex type
     *
     * @return a TreeTraverser that returns an empty Iterable for all inputs.
     */
    @SuppressWarnings( "unchecked" )
    public static <T> TreeTraverser<T> empty() {
        return (TreeTraverser<T>) EmptyTraverser.INSTANCE;
    }

    private static class EmptyTraverser extends TreeTraverser<Object> {
        private static final TreeTraverser<Object> INSTANCE = new EmptyTraverser();

        @Override
        public Iterable<Object> children( final Object root ) {
            return ImmutableSet.of();
        }
    }

    // TODO: Document how these are different from Gauva's implementations.

    /**
     * Returns a pre-order iterable with <strong>NO</strong> cycle detection.
     *
     * @param adjacency the adjacency function to use
     * @param <T> the vertex type
     *
     * @return a pre-order iterable with <strong>NO</strong> cycle detection.
     */
    public static <T> FluentIterable<T> preOrder( final T root, final TreeTraverser<T> adjacency ) {
        Preconditions.checkNotNull( adjacency );
        return new FluentIterable<T>() {
            @Override
            public Iterator<T> iterator() {
                return new PreOrderIterator<T>( root, Lazy.traverser( adjacency ) );
            }
        };
    }

    /**
     * Returns a post-order iterable with <strong>NO</strong> cycle detection. If a cycle is present, some call to
     * next() will infinitely loop (most likely resulting in an OutOfMemoryError).
     *
     * @param adjacency the adjacency function to use
     * @param <T> the vertex type
     *
     * @return a post-order iterable with <strong>NO</strong> cycle detection.
     */
    public static <T> FluentIterable<T> postOrder( final T root, final TreeTraverser<T> adjacency ) {
        Preconditions.checkNotNull( adjacency );
        return new FluentIterable<T>() {
            @Override
            public Iterator<T> iterator() {
                return new PostOrderIterator<T>( root, Lazy.traverser( adjacency ) );
            }
        };
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
