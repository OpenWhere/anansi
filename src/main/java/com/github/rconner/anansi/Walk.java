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

import com.github.rconner.util.ImmutableStack;
import com.google.common.annotations.Beta;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;

/**
 * A walk from one vertex to another, via an Iterable of {@link Step Steps}.
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
    private final Iterable<Step<V, E>> via;

    /**
     * The only Walk constructor, private to prevent direct instantiation by clients.
     *
     * @param from the first vertex in this Walk.
     * @param to the last Vertex in this Walk.
     * @param via the Steps in this Walk.
     */
    Walk( final V from, final V to, final Iterable<Step<V, E>> via ) {
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

    /**
     * Return the last vertex in this Walk. This is a convenience method, returning the value of {@link Step#getTo()}
     * for the last {@code Step} returned by {@link #getVia()}.
     *
     * @return the last vertex in this Walk.
     */
    public V getTo() {
        return to;
    }

    /**
     * Returns the {@link Step Steps} in this Walk.
     *
     * @return the {@code Steps} in this Walk.
     */
    public Iterable<Step<V, E>> getVia() {
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
     * Creates a new, empty, immutable Walk. This should only be used when a Walk literally has travelled over no edges,
     * the Walk to the root of a breadth- or depth-first traversal for example.
     *
     * @param vertex the first and last vertex in the Walk.
     * @param <V> the vertex type
     * @param <E> the edge type
     *
     * @return a new, empty, immutable Walk.
     */
    public static <V, E> Walk<V, E> empty( final V vertex ) {
        return new Walk<V, E>( vertex, vertex, ImmutableSet.<Step<V, E>>of() );
    }

    /**
     * Creates a new, immutable Walk with a single Step.
     *
     * @param from the first vertex in the Walk.
     * @param to the last vertex in the Walk.
     * @param over the single edge over which the Walk steps.
     * @param <V> the vertex type
     * @param <E> the edge type
     *
     * @return a new, immutable Walk with a single Step.
     */
    public static <V, E> Walk<V, E> single( final V from, final V to, final E over ) {
        return new Walk<V, E>( from, to, ImmutableSet.of( new Step<V, E>( to, over ) ) );
    }

    /**
     * Creates a new, immutable Walk with a single Step with an over of null.
     *
     * @param from the first vertex in the Walk.
     * @param to the last vertex in the Walk.
     * @param <V> the vertex type
     * @param <E> the edge type
     *
     * @return a new, immutable Walk with a single Step with an over of null.
     */
    public static <V, E> Walk<V, E> single( final V from, final V to ) {
        return single( from, to, null );
    }

    /**
     * Creates an immutable builder used to create a Walk with multiple Steps.
     *
     * @param from the first vertex in the Walk.
     * @param <V> the vertex type
     * @param <E> the edge type
     *
     * @return an immutable builder used to create a Walk with multiple Steps.
     */
    @SuppressWarnings( "unchecked" )
    public static <V, E> Builder<V, E> from( final V from ) {
        return new Builder<V, E>( from, ImmutableStack.<Step<V, E>>of() );
    }

    public static final class Builder<V, E> {
        private final V from;
        private final ImmutableStack<Step<V, E>> stack;

        Builder( final V from, final ImmutableStack<Step<V, E>> stack ) {
            this.from = from;
            this.stack = stack;
        }

        public Builder<V, E> add( final Walk<V, E> walk ) {
            Preconditions.checkNotNull( walk );
            ImmutableStack<Step<V, E>> next = stack;
            for( final Step<V, E> step : walk.getVia() ) {
                next = next.push( step );
            }
            return new Builder<V, E>( from, next );
        }

        @SuppressWarnings( "unchecked" )
        public Walk<V, E> build() {
            if( stack.isEmpty() ) {
                return Walk.empty( from );
            }
            return new Walk( from, stack.peek().getTo(), stack.reverse() );
        }
    }
}
