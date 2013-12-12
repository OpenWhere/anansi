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
import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.TreeTraverser;

import java.util.Iterator;

/**
 * Static factory methods for building traversers.
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

    /**
     * Returns a TreeTraverser which will return the (adjacency Walks to) immediate Iterable elements, array elements,
     * or Map values for any such non-empty value of Walk.getTo(). If the input is an empty Iterable, array, or Map, an
     * empty Iterable will be returned. If the input is not an Iterable, array, or Map, an empty Iterable will be
     * returned. The values of {@link Walk.Step#getOver()} are strings representing the path in normal idiomatic usage.
     * Because they are used as path separators, periods (property reference) and brackets (array indexing) are escaped
     * with preceding backslashes if they appear as Map keys.
     *
     * @return a TreeTraverser which will return the (adjacency Walks to) immediate Iterable elements, array elements,
     *         or Map values for any such non-empty value of Walk.getTo().
     */
    public static TreeTraverser<Walk<Object, String>> elements() {
        return Elements.ELEMENT_ADJACENCY;
    }

    /**
     * Creates a {@link TreeTraverser} of {@link WeightedWalk WeightedWalks} given a {@code TreeTraverser} of {@link
     * Walk Walks} and an edge weight {@link Function}. Edge weights are assumed to be additive along a walk.
     *
     * @param traverser the TreeTraverser of Walks.
     * @param weightFunction the edge weight Function.
     * @param <V> the vertex type
     * @param <E> the edge type
     *
     * @return a {@code TreeTraverser} of {@code WeightedWalks} given a {@code TreeTraverser} of {@code Walks} and an
     * edge weight {@code Function}.
     */
    public static <V, E> TreeTraverser<WeightedWalk<V, E>> weighted( final TreeTraverser<Walk<V, E>> traverser,
                                                                     final Function<E, Double> weightFunction ) {
        return new WeightedTraverser<V, E>( traverser, weightFunction );
    }

    private static class WeightedTraverser<V, E> extends TreeTraverser<WeightedWalk<V, E>> {
        private final TreeTraverser<Walk<V, E>> delegate;
        private final Function<E, Double> weightFunction;

        private WeightedTraverser( final TreeTraverser<Walk<V, E>> delegate, final Function<E, Double> weightFunction ) {
            this.delegate = delegate;
            this.weightFunction = weightFunction;
        }

        @Override
        public Iterable<WeightedWalk<V, E>> children( final WeightedWalk<V, E> weightedWalk ) {
            return new Iterable<WeightedWalk<V, E>>() {
                @Override
                public Iterator<WeightedWalk<V, E>> iterator() {
                    final Iterator<Walk<V, E>> iterator = Lazy.iterator( delegate.children( weightedWalk.getWalk() ) );
                    return new Iterator<WeightedWalk<V, E>>() {
                        @Override
                        public boolean hasNext() {
                            return iterator.hasNext();
                        }

                        @Override
                        public WeightedWalk<V, E> next() {
                            final Walk<V, E> walk = iterator.next();
                            final double stepWeight = weightFunction.apply( walk.getVia().first().getOver() );
                            return WeightedWalk.newInstance( walk, weightedWalk.getWeight() + stepWeight );
                        }

                        @Override
                        public void remove() {
                            iterator.remove();
                        }
                    };
                }
            };
        }
    }
}
