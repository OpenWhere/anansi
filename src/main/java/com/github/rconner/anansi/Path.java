/*
    Copyright (c) 2012 Ray A. Conner

    Permission is hereby granted, free of charge, to any person obtaining a copy
    of this software and associated documentation files (the "Software"), to deal
    in the Software without restriction, including without limitation the rights
    to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
    copies of the Software, and to permit persons to whom the Software is
    furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all
    copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.
 */

package com.github.rconner.anansi;

import com.google.common.annotations.Beta;

/**
 * A path from one vertex to another, optionally over some implementation-specific object. It is not uncommon for a Path
 * to be over an Iterable of (sub) Paths.
 *
 * @param <V>
 * @param <E>
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
     * @return
     */
    public static <V, E> Path<V, E> newInstance(V from, V to) {
        return new TrivialPath<V, E>(from, to, null);
    }

    /**
     * Creates a new immutable Path.
     *
     * @param from
     * @param to
     * @param over
     * @param <V>
     * @param <E>
     * @return
     */
    public static <V, E> Path<V, E> newInstance(V from, V to, E over) {
        return new TrivialPath<V, E>(from, to, over);
    }

    private static final class TrivialPath<V, E> extends Path<V, E> {
        private final V from;
        private final V to;
        private final E over;

        private TrivialPath(V from, V to, E over) {
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


    public static <V, E> Builder<V, E> from(V from) {
        return new Builder<V, E>(from);
    }

    // TODO: Explore having Builder be immutable, and therefore thread-safe.
    // to( ... ) would have to return new instances, and pop() would be OBE,
    // but you get the benefit of allowing concurrent traversals.

    public static final class Builder<V, E> {
        private final V from;
        private Chain<Path<V, E>> chain = null;

        private Builder(V from) {
            this.from = from;
        }

        public Builder<V, E> to(V to) {
            return to(to, null);
        }

        public Builder<V, E> to(V to, E over) {
            if (chain == null) {
                chain = Chain.of(Path.newInstance(from, to, over));
            }
            chain = chain.with(Path.newInstance(chain.head().getTo(), to, over));
            return this;
        }

        public Builder<V, E> pop() {
            chain = chain.tail();
            return this;
        }

        public Path<V, ? extends Iterable<Path<V, E>>> build() {
            return Path.newInstance(from, chain.head().getTo(), chain.reverse());
        }
    }

}
