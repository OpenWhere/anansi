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

/**
 * A path from one vertex to another, optionally over some implementation-specific object. It is not uncommon for a Path
 * to be over an Iterable of (sub) Paths.
 *
 * @param <V>
 * @param <E>
 *
 * @author rconner
 */
@Beta
public abstract class Path<V, E> {

    /**
     * @return
     */
    public abstract V getFrom();

    /**
     * @return
     */
    public abstract V getTo();

    /**
     * @return
     */
    public abstract E getOver();

    /**
     * Creates a new immutable Path, with an over of null.
     *
     * @param from
     * @param to
     * @param <V>
     * @param <E>
     *
     * @return
     */
    public static <V, E> Path<V, E> newInstance( V from, V to ) {
        return new TrivialPath<V, E>( from, to, null );
    }

    /**
     * Creates a new immutable Path.
     *
     * @param from
     * @param to
     * @param over
     * @param <V>
     * @param <E>
     *
     * @return
     */
    public static <V, E> Path<V, E> newInstance( V from, V to, E over ) {
        return new TrivialPath<V, E>( from, to, over );
    }

    private static final class TrivialPath<V, E> extends Path<V, E> {
        private final V from;
        private final V to;
        private final E over;

        private TrivialPath( V from, V to, E over ) {
            this.from = from;
            this.to = to;
            this.over = over;
        }

        @Override
        public V getFrom() {
            return from;
        }

        @Override
        public V getTo() {
            return to;
        }

        @Override
        public E getOver() {
            return over;
        }
    }

    /**
     * Creates a builder used to create a Path with sub-paths. Note that Path.Builder instances are immutable.
     *
     * @param from
     * @param <V>
     * @param <E>
     *
     * @return
     */
    public static <V, E> Builder<V, E> from( V from ) {
        return new Builder<V, E>( from );
    }

    public static final class Builder<V, E> {
        private final V from;
        private final V to;
        private final ImmutableStack<Path<V, E>> stack;

        @SuppressWarnings( "unchecked" )
        private Builder( V from ) {
            this( from, from, ImmutableStack.<Path<V, E>>of() );
        }

        private Builder( V from, V to, ImmutableStack<Path<V, E>> stack ) {
            this.from = from;
            this.to = to;
            this.stack = stack;
        }

        public Builder<V, E> to( V to ) {
            return to( to, null );
        }

        public Builder<V, E> to( V to, E over ) {
            Path<V, E> step = Path.newInstance( this.to, to, over );
            return new Builder<V, E>( from, to, stack.push( step ) );
        }

        public Path<V, Iterable<Path<V, E>>> build() {
            // if stack is empty, do not know from/to
            // otherwise:
            //   from := stack.last.from
            //   to := stack.head.to
            // FIXME: Instead, build a *really* lazy path? b/c often the caller will only
            // be interested in path.to anyway. So just keep the stack around in the path.
            return Path.newInstance( from, to, stack.reverse() );
        }
    }

}
