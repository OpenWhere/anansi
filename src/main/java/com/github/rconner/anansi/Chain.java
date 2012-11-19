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

import com.google.common.collect.ImmutableList;

/**
 * A special case immutable singly-linked list used for building Paths. This is singly-linked for a couple reasons. One
 * is that in graph traversals, the reverse direction would not be a list, but a tree. Another is that being
 * singly-linked allows unused paths to be garbage collected. This is roughly how LISP implements lists, and works very
 * nicely for building predecessor graphs.
 */
final class Chain<E> {

    private final E head;
    private final Chain<E> tail;
    private final int size;

    private Chain(E head, Chain<E> tail) {
        this.head = head;
        this.tail = tail;
        size = (tail == null) ? 1 : tail.size + 1;
    }

    /**
     * Creates a Chain with a single element.
     *
     * @param element
     * @return
     */
    public static <T> Chain<T> of(T element) {
        return new Chain<T>(element, null);
    }

    /**
     * Creates a Chain with the given elements, in order.
     *
     * @param elements
     * @return
     */
    public static <T> Chain<T> copyOf(T... elements) {
        Chain<T> chain = null;
        for (int i = elements.length - 1; i >= 0; i--) {
            chain = new Chain<T>(elements[i], chain);
        }
        return chain;
    }

    public E head() {
        return head;
    }

    public Chain<E> tail() {
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

    public ImmutableList<E> toList() {
        ImmutableList.Builder<E> builder = ImmutableList.builder();
        Chain<E> chain = this;
        for (int i = 0; i < size; i++) {
            builder.add(chain.head());
            chain = chain.tail();
        }
        return builder.build();
    }

    public ImmutableList<E> toReverseList() {
        return toList().reverse();
    }

}
