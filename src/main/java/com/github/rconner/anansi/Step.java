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

/**
 * An abstraction for a step in a traversal. A {@code Step} is &quot;to&quot; a vertex &quot;over&quot; an edge. A
 * {@code Step} is empty if it is was not actually taken over any edge, the root of a breadth- or depth-first traversal
 * for example. Traversals do not need to use this abstraction, it is merely a convenience.
 *
 * @param <V> the vertex type
 * @param <E> the edge type
 */
public abstract class Step<V, E> {

    /**
     * Returns the vertex to which this {@code Step} was taken.
     *
     * @return the vertex to which this {@code Step} was taken.
     */
    public abstract V getTo();

    /**
     * Returns the edge over which this {@code Step} was taken.
     *
     * @return the edge over which this {@code Step} was taken.
     */
    public abstract E getOver();

    /**
     * Returns true if this {@code Step} was not actually &quot;taken&quot; over any edge, the root of a breadth- or
     * depth-first traversal for example.
     *
     * @return true if this {@code Step} was not actually &quot;taken&quot; over any edge, the root of a breadth- or
     * depth-first traversal for example.
     */
    public abstract boolean isEmpty();

    /**
     * Creates a new empty {@code Step} to the given vertex.
     *
     * @param vertex the vertex to which the new {@code Step} is taken.
     * @param <V> the vertex type
     * @param <E> the edge type
     *
     * @return a new empty {@code Step} to the given vertex.
     */
    public static <V, E> Step<V, E> empty( final V vertex ) {
        return new Step<V, E>() {
            @Override
            public V getTo() {
                return vertex;
            }

            @Override
            public E getOver() {
                return null;
            }

            @Override
            public boolean isEmpty() {
                return true;
            }

            @Override
            public String toString() {
                return "to (" + vertex + ")";
            }
        };
    }

    /**
     * Creates a new empty {@code Step} to the given vertex over the given edge.
     *
     * @param vertex the vertex to which the new {@code Step} is taken.
     * @param over the edge over which the new {@code Step} is taken.
     * @param <V> the vertex type
     * @param <E> the edge type
     *
     * @return a new empty {@code Step} to the given vertex over the given edge.
     */
    public static <V, E> Step<V, E> newInstance( final V vertex, final E over ) {
        return new Step<V, E>() {
            @Override
            public V getTo() {
                return vertex;
            }

            @Override
            public E getOver() {
                return over;
            }

            @Override
            public boolean isEmpty() {
                return false;
            }

            @Override
            public String toString() {
                return "to (" + vertex + ") over (" + over + ")";
            }
        };
    }

    /**
     * Creates a new empty {@code Step} to the given vertex over an edge of null.
     *
     * @param vertex the vertex to which the new {@code Step} is taken.
     * @param <V> the vertex type
     * @param <E> the edge type
     *
     * @return a new empty {@code Step} to the given vertex over an edge of null.
     */
    public static <V, E> Step<V, E> newInstance( final V vertex ) {
        return new Step<V, E>() {
            @Override
            public V getTo() {
                return vertex;
            }

            @Override
            public E getOver() {
                return null;
            }

            @Override
            public boolean isEmpty() {
                return false;
            }

            @Override
            public String toString() {
                return "to (" + vertex + ")";
            }
        };
    }
}
