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

import com.github.rconner.util.ImmutableStack;
import com.google.common.annotations.Beta;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

/**
 * A walk from one vertex to another, via an Iterable of {@link Step Steps}.
 *
 * @param <V>
 * @param <E>
 *
 * @author rconner
 */
@Beta
public final class Walk<V, E> {
    private final V from;
    private final V to;
    private final Iterable<Step<V, E>> via;

    /**
     * The only Walk constructor, private to prevent direct instantiation by clients.
     *
     * @param from
     * @param to
     * @param via
     */
    private Walk( final V from, final V to, final Iterable<Step<V, E>> via ) {
        this.from = from;
        this.to = to;
        this.via = via;
    }

    /**
     * @return
     */
    public V getFrom() {
        return from;
    }

    /**
     * @return
     */
    public V getTo() {
        return to;
    }

    /**
     * @return
     */
    public Iterable<Step<V, E>> getVia() {
        return via;
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
    }

    /**
     * Creates a new, empty, immutable Walk. This should only be used when a Walk literally has travelled over no edges,
     * the Walk to the root of a breadth- or depth-first traversal for example.
     *
     * @param from
     * @param to
     * @param <V>
     * @param <E>
     *
     * @return
     */
    public static <V, E> Walk<V, E> empty( final V from ) {
        return new Walk<V, E>( from, from, ImmutableSet.<Step<V, E>>of() );
    }

    /**
     * Creates a new, immutable Walk with a single Step.
     *
     * @param from
     * @param to
     * @param over
     * @param <V>
     * @param <E>
     *
     * @return
     */
    public static <V, E> Walk<V, E> single( final V from, final V to, final E over ) {
        return new Walk<V, E>( from, to, ImmutableSet.of( new Step<V, E>( to, over ) ) );
    }

    /**
     * Creates a new, immutable Walk with a single Step with an over of null.
     *
     * @param from
     * @param to
     * @param over
     * @param <V>
     * @param <E>
     *
     * @return
     */
    public static <V, E> Walk<V, E> single( final V from, final V to ) {
        return single( from, to, null );
    }

    /**
     * Creates a new, immutable Walk with multiple steps.
     *
     * @param from
     * @param to
     * @param via
     * @param <V>
     * @param <E>
     *
     * @return
     */
    static <V, E> Walk<V, E> multi( final V from, final V to, final Iterable<Step<V, E>> via ) {
        return new Walk<V, E>( from, to, via );
    }

    /**
     * Creates a builder used to create a Walk with multiple Steps.
     *
     * @param from
     * @param <V>
     * @param <E>
     *
     * @return
     */
    public static <V, E> Builder<V, E> from( final V from ) {
        return new Builder<V, E>( from );
    }

    public static final class Builder<V, E> {
        private final V from;
        @SuppressWarnings( "unchecked" )
        private ImmutableStack<Walk<V, E>> stack = ImmutableStack.of();

        Builder( final V from ) {
            this.from = from;
        }

        public Builder<V, E> add( final Walk<V, E> walk ) {
            Preconditions.checkNotNull( walk );
            stack = stack.push( walk );
            return this;
        }

        public Builder<V, E> pop() {
            stack = stack.pop();
            return this;
        }

        @SuppressWarnings( "unchecked" )
        public Walk<V, E> build() {
            // FIXME: Instead, build a *really* lazy walk? b/c often the caller will only
            // be interested in walk.to anyway. So just keep the stack around in the walk.
            if( stack.isEmpty() ) {
                return Walk.empty( from );
            }
            final Iterable<Walk<V, E>> walks = stack.reverse();
            final Function<Walk<V, E>, Iterable<Step<V, E>>> func = GetVia.getInstance();
            final Iterable<Step<V, E>> steps = Iterables.concat( Iterables.transform( walks, func ) );
            return Walk.multi( from, stack.peek().getTo(), steps );
        }
    }

    private static class GetVia<V, E> implements Function<Walk<V, E>, Iterable<Step<V, E>>> {
        private static final Function<?, ?> INSTANCE = new GetVia();

        @SuppressWarnings( "unchecked" )
        static <V, E> Function<Walk<V, E>, Iterable<Step<V, E>>> getInstance() {
            return ( Function<Walk<V, E>, Iterable<Step<V, E>>> ) INSTANCE;
        }

        @Override
        public Iterable<Step<V, E>> apply( final Walk<V, E> walk ) {
            return walk.getVia();
        }
    }
}
