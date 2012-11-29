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

import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterators;
import com.google.common.collect.UnmodifiableIterator;

import java.util.EmptyStackException;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A minimal immutable stack implementation, which does <strong>not</strong> implement {@link java.util.Collection}.
 * Note that it is not necessary for the contained elements to also be immutable; just remember that shared mutable
 * objects require synchronization even if they are contained in immutable data structures.
 * <p/>
 * A {@link #push(Object)} operation returns a new stack. A {@link #pop()} operation returns the stack upon which {@link
 * #push(Object)} was called to add the top element. Because instances are immutable, storage can be (and is) reused. A
 * single stack instance can have many independent elements pushed onto it, with each push resulting in a new stack
 * instance sharing the storage for all the elements below the top one.
 * <p/>
 * Instances of this class do not implement {@link #equals(Object)} or {@link #hashCode()}.
 */
public abstract class ImmutableStack<E> implements Iterable<E> {

    /**
     * Prevent instantiation.
     */
    private ImmutableStack() {
    }

    public abstract ImmutableStack<E> push(E element);

    public abstract ImmutableStack<E> pop();

    public abstract E peek();

    public abstract boolean contains(Object object);

    public abstract int size();

    public abstract boolean isEmpty();

    @Override
    public abstract Iterator<E> iterator();

    public abstract Iterable<E> reverse();

    /**
     * Creates a new ImmutableStack with the given elements, in order from bottom to top.
     *
     * @param elements
     * @param <E>
     * @return
     */
    public static <E> ImmutableStack<E> of(E... elements) {
        @SuppressWarnings("unchecked")
        ImmutableStack<E> stack = (ImmutableStack<E>) EMPTY_STACK;
        for (E element : elements) {
            stack = stack.push(element);
        }
        return stack;
    }

    private static final ImmutableStack<Object> EMPTY_STACK = new ImmutableStack<Object>() {
        @Override
        public ImmutableStack<Object> push(Object element) {
            return new Stack<Object>(element, this);
        }

        @Override
        public ImmutableStack<Object> pop() {
            throw new EmptyStackException();
        }

        @Override
        public Object peek() {
            throw new EmptyStackException();
        }

        @Override
        public boolean contains(Object object) {
            return false;
        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public Iterator<Object> iterator() {
            return Iterators.emptyIterator();
        }

        @Override
        public Iterable<Object> reverse() {
            return ImmutableSet.of();
        }

        @Override
        public String toString() {
            return "[]";
        }
    };

    private static class Stack<E> extends ImmutableStack<E> {
        private final E top;
        private final ImmutableStack<E> rest;
        private final int size;

        private Stack(E top, ImmutableStack<E> rest) {
            this.top = top;
            this.rest = rest;
            this.size = rest.size() + 1;
        }

        @Override
        public ImmutableStack<E> push(E element) {
            return new Stack<E>(element, this);
        }

        @Override
        public ImmutableStack<E> pop() {
            return rest;
        }

        @Override
        public E peek() {
            return top;
        }

        @Override
        public boolean contains(Object object) {
            ImmutableStack<E> stack = this;
            while (!stack.isEmpty()) {
                if (Objects.equal(object, stack.peek())) {
                    return true;
                }
                stack = stack.pop();
            }
            return false;
        }

        @Override
        public int size() {
            return size;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public Iterator<E> iterator() {
            return new UnmodifiableIterator<E>() {
                ImmutableStack<E> stack = Stack.this;

                @Override
                public boolean hasNext() {
                    return !stack.isEmpty();
                }

                @Override
                public E next() {
                    if (stack.isEmpty()) {
                        throw new NoSuchElementException();
                    }
                    E top = stack.peek();
                    stack = stack.pop();
                    return top;
                }
            };
        }

        // TODO: Make this lazy, and keep the result around. But how lazy, and what parts, needs to be decided.

        // The array doesn't need to be constructed until Iterable.iterator() is called, or it can even wait until
        // Iterator.hasNext() or next() is called. That could be accomplished by wrapping this with Lazy.iterable()
        // and moving the code that builds the array.

        // The Iterable could be kept, or the backing array, or both. Once the backing array is built, you don't
        // need the stack any more, so this iterable could be static instead of inner.

        @Override
        public Iterable<E> reverse() {
            // There's no way to do this without saving all the elements, at least no way that isn't O(n^2).
            // An ImmutableList would work, except they don't allow null elements.

            final Object[] array = new Object[size];
            ImmutableStack<E> stack = this;
            for (int i = size - 1; i >= 0; i--) {
                array[i] = stack.peek();
                stack = stack.pop();
            }

            return new Iterable<E>() {
                @Override
                public Iterator<E> iterator() {
                    return new ArrayIterator<E>(array);
                }
            };
        }

        private static final Joiner JOINER = Joiner.on(", ").useForNull("null");

        @Override
        public String toString() {
            StringBuilder s = new StringBuilder();
            s.append('[');
            JOINER.appendTo(s, this);
            s.append(']');
            return s.toString();
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
