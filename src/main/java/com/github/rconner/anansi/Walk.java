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

import com.github.rconner.util.PersistentList;
import com.google.common.annotations.Beta;
import com.google.common.base.Joiner;

/**
 * An immutable walk from one vertex to another, via an Iterable of {@link Step Steps}.
 *
 * @param <V> the vertex type
 * @param <E> the edge type
 *
 * @author rconner
 */
@Beta
public final class Walk<V, E> {
    private static final Joiner joiner = Joiner.on( ", " );

    private final V from;
    private final V to;
    private final PersistentList<Step<V, E>> via;

    /**
     * The only Walk constructor, package-private to prevent direct instantiation by clients.
     *
     * @param from the first vertex in this Walk.
     * @param to the last Vertex in this Walk.
     * @param via the Steps in this Walk, in reverse order.
     */
    Walk( final V from, final V to, final PersistentList<Step<V, E>> via ) {
        this.from = from;
        this.to = to;
        this.via = via;
    }

    /**
     * Return the first vertex in this Walk.
     *
     * @return the first vertex in this Walk.
     */
    public V getFrom() {
        return from;
    }

    // TODO: remove this since it's easy to get now?

    /**
     * Return the last vertex in this Walk. This is a convenience method, returning the value of {@link Step#getTo()}
     * for the first {@code Step} returned by {@link #getVia()} (the last {@code Step} in this Walk)
     *
     * @return the last vertex in this Walk.
     */
    public V getTo() {
        return to;
    }

    /**
     * Returns the {@link Step Steps} in this Walk in reverse order.
     *
     * @return the {@code Steps} in this Walk in reverse order.
     */
    public PersistentList<Step<V, E>> getVia() {
        return via;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append( getFrom() ).append( "=>" ).append( getTo() );
        builder.append( " via [" );
        joiner.appendTo( builder, getVia() );
        builder.append( "]" );
        return builder.toString();
    }

    public static final class Step<V, E> {
        private final V to;
        private final E over;

        Step( final V to, final E over ) {
            this.to = to;
            this.over = over;
        }

        public V getTo() {
            return to;
        }

        public E getOver() {
            return over;
        }

        @Override
        public String toString() {
            return "to (" + to + ") over (" + over + ')';
        }
    }

    /**
     * Creates a new empty Walk. This should only be used when a Walk literally has travelled over no edges, the Walk to
     * the root of a breadth- or depth-first traversal for example.
     *
     * @param vertex the first and last vertex in the Walk.
     * @param <V> the vertex type
     * @param <E> the edge type
     *
     * @return a new empty Walk.
     */
    public static <V, E> Walk<V, E> empty( final V vertex ) {
        return new Walk<V, E>( vertex, vertex, PersistentList.<Step<V, E>>of() );
    }

    /**
     * Creates a new Walk with a single Step.
     *
     * @param from the first vertex in the Walk.
     * @param to the last vertex in the Walk.
     * @param over the single edge over which the Walk steps.
     * @param <V> the vertex type
     * @param <E> the edge type
     *
     * @return a new Walk with a single Step.
     */
    public static <V, E> Walk<V, E> single( final V from, final V to, final E over ) {
        return new Walk<V, E>( from, to, PersistentList.of( new Step<V, E>( to, over ) ) );
    }

    /**
     * Creates a new Walk with a single Step with an over of null.
     *
     * @param from the first vertex in the Walk.
     * @param to the last vertex in the Walk.
     * @param <V> the vertex type
     * @param <E> the edge type
     *
     * @return a new Walk with a single Step with an over of null.
     */
    public static <V, E> Walk<V, E> single( final V from, final V to ) {
        return single( from, to, null );
    }

    /**
     * Creates a new Walk starting with this Walk and appending a single Step.
     *
     * @param to the to vertex in the final Step of the new Walk.
     * @param over the single edge over which the the final Step of the new Walk steps.
     *
     * @return a new Walk starting with this Walk and appending a single Step.
     */
    public Walk<V, E> append( final V to, final E over ) {
        return new Walk<V, E>( from, to, via.add( new Step<V, E>( to, over ) ) );
    }

    /**
     * Creates a new Walk starting with this Walk and appending a single Step with an over of null.
     *
     * @param to the to vertex in the final Step of the new Walk.
     *
     * @return a new Walk starting with this Walk and appending a single Step with an over of null.
     */
    public Walk<V, E> append( final V to ) {
        return append( to, null );
    }

    /**
     * Creates a new Walk starting with this Walk and appending the given Walk. The appended Walk would normally start
     * where this Walk ends, but this condition is not checked.
     *
     * @param walk the Walk to append to this Walk.
     *
     * @return a new Walk starting with this Walk and appending the given Walk.
     */
    public Walk<V, E> append( final Walk<V, E> walk ) {
        return new Walk<V, E>( from, walk.getTo(), via.addAll( walk.getVia().reverse() ) );
    }
}
