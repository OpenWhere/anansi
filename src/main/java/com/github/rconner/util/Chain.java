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

package com.github.rconner.util;

import com.google.common.annotations.Beta;
import com.google.common.collect.UnmodifiableIterator;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A special case immutable singly-linked list used for building Paths. This is singly-linked for a couple reasons. One
 * is that in graph traversals, the reverse direction would not be a list, but a tree. Another is that being
 * singly-linked allows unused paths to be garbage collected. This is roughly how LISP implements lists, and works very
 * nicely for building predecessor graphs.
 */
@Beta
public final class Chain<E> implements Iterable<E> {

    // Should really be a singleton-enum pattern, but Chain would need to
    // be an interface for that to happen.
    private static final Chain<Object> EMPTY = new Chain<Object>();

    private final E head;
    private final Chain<E> tail;
    private final int size;

    /**
     * Used only to construct EMPTY.
     */
    private Chain() {
        head = null;
        tail = null;
        size = 0;
    }

    private Chain(E head, Chain<E> tail) {
        // tail must be non-null
        this.head = head;
        this.tail = tail;
        size = tail.size + 1;
    }

    /**
     * Creates a Chain with the given elements, in order.
     *
     * @param elements
     * @return
     */
    public static <T> Chain<T> of(T... elements) {
        @SuppressWarnings("unchecked")
        Chain<T> chain = (Chain<T>) EMPTY;
        for (int i = elements.length - 1; i >= 0; i--) {
            chain = new Chain<T>(elements[i], chain);
        }
        return chain;
    }

    public E head() {
        if (this == EMPTY) {
            throw new NoSuchElementException();
        }
        return head;
    }

    public Chain<E> tail() {
        if (this == EMPTY) {
            throw new NoSuchElementException();
        }
        return tail;
    }

    public int size() {
        return size;
    }

    /**
     * Creates a returns new Chain from this Chain with an element prepended. This is essentially a &quot;push&quot;
     * operation, except that it creates a new Chain.
     */
    public Chain<E> with(E element) {
        return new Chain<E>(element, this);
    }

    @Override
    public Iterator<E> iterator() {
        return new UnmodifiableIterator<E>() {
            Chain<E> rest = Chain.this;

            @Override
            public boolean hasNext() {
                return rest != EMPTY;
            }

            @Override
            public E next() {
                if (rest == EMPTY) {
                    throw new NoSuchElementException();
                }
                E element = rest.head;
                rest = rest.tail;
                return element;
            }
        };
    }

    // This is a trade-off that probably needs some benchmarking.
    // This implementation computes the backing array once, and then keeps it around forever.
    // One alternative is to create a new backing array every time iterator() is called.
    // Another is to keep a weak reference to the backing array and only recompute if it has been gc'd.

    // FIXME: Return a REUSED immutable iterable => keep a weak ref to the Iterable.

    public Iterable<E> reverse() {
        // There's no way to do this without saving all the elements, at least no way that isn't O(n^2).
        // An ImmutableList would work, except they don't allow null elements.
        return new ChainIterable<E>(this);
    }

    // This has TWO levels of laziness:
    // - Do not create the Iterable until asked by chain.reverse()
    // - Do not traverse the chain to create the array until asked for Iterable.iterator()

    // If we just return a LazyIterable around this, do we get all that without any explicit laziness here?

    // The entire Iterable should be reused if someone else calls chain.reverse(), not just the array.
    // If the iterator is traversed, it has the array and no longer needs the chain.


    private static final class ChainIterable<E> implements Iterable<E> {
        private final Chain<E> chain;
        // Lazily initialized
        private Object[] array;

        private ChainIterable(Chain<E> chain) {
            this.chain = chain;
        }

        private synchronized Object[] getArray() {
            if (array == null) {
                array = new Object[chain.size];
                Chain<E> rest = chain;
                for (int i = chain.size - 1; i >= 0; i--) {
                    array[i] = rest.head;
                    rest = rest.tail;
                }
            }
            return array;
        }

        @Override
        public Iterator<E> iterator() {
            return new ArrayIterator<E>(getArray());
        }
    }

    private static final class ArrayIterator<E> extends UnmodifiableIterator<E> {
        private final Object[] array;
        private int i = 0;

        private ArrayIterator(Object[] array) {
            this.array = array;
        }

        @Override
        public boolean hasNext() {
            return i < array.length;
        }

        @Override
        @SuppressWarnings("unchecked")
        public E next() {
            if (i >= array.length) {
                throw new NoSuchElementException();
            }
            return (E) array[i++];
        }
    }
}
