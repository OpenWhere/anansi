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

package com.github.rconner.util;

import com.google.common.annotations.Beta;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterators;
import com.google.common.collect.UnmodifiableIterator;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A minimal persistent list implementation, which does <strong>not</strong> implement {@link java.util.Collection}.
 * Note that it is not necessary for the contained elements to also be immutable; just remember that shared mutable
 * objects require synchronization even if they are contained in immutable data structures.
 * <p/>
 * A {@link #add(Object)} operation returns a new list. A {@link #rest()} operation returns the list upon which {@link
 * #add(Object)} was called to add the first element. Because instances are immutable, storage can be (and is) reused. A
 * single list instance can have many independent elements pushed onto it, with each push resulting in a new list
 * instance sharing the storage for all the elements beyond the first one.
 * <p/>
 * Instances of this class do not implement {@link #equals(Object)} or {@link #hashCode()}.
 *
 * @author rconner
 */
@Beta
public abstract class PersistentList<E> implements Iterable<E> {

    /**
     * Prevent instantiation.
     */
    private PersistentList() {
    }

    public abstract E first();

    public abstract PersistentList<E> rest();

    public abstract PersistentList<E> add( E element );

    public abstract int size();

    public abstract boolean isEmpty();

    @Override
    public abstract Iterator<E> iterator();

    public abstract Iterable<E> reverse();

    /**
     * Creates a new empty PersistentList.
     *
     * @param <E> the type of element
     *
     * @return a new empty PersistentList.
     */
    @SuppressWarnings( "unchecked" )
    public static <E> PersistentList<E> of() {
        return (PersistentList<E>) EmptyList.INSTANCE;
    }

    /**
     * Creates a new PersistentList with the given element.
     *
     * @param element the single element in the newly created PersistentList
     * @param <E> the type of element
     *
     * @return a new PersistentList with the given elements.
     */
    public static <E> PersistentList<E> of( final E element ) {
        return PersistentList.<E>of().add( element );
    }

    /**
     * Creates a new PersistentList with the given elements, in order from bottom to top.
     *
     * @param elements the elements for which to create a new PersistentList
     * @param <E> the type of element
     *
     * @return a new PersistentList with the given elements, in order from bottom to top.
     */
    public static <E> PersistentList<E> of( final E... elements ) {
        PersistentList<E> list = of();
        for( final E element : elements ) {
            list = list.add( element );
        }
        return list;
    }

    private static final class EmptyList extends PersistentList<Object> {
        private static final PersistentList<Object> INSTANCE = new EmptyList();

        EmptyList() {
        }

        @Override
        public Object first() {
            throw new NoSuchElementException();
        }

        @Override
        public PersistentList<Object> rest() {
            throw new NoSuchElementException();
        }

        @Override
        public PersistentList<Object> add( final Object element ) {
            return new List<Object>( element, this );
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
    }

    private static final class List<E> extends PersistentList<E> {
        private final E top;
        private final PersistentList<E> rest;
        private final int size;

        List( final E top, final PersistentList<E> rest ) {
            this.top = top;
            this.rest = rest;
            size = rest.size() + 1;
        }

        @Override
        public E first() {
            return top;
        }

        @Override
        public PersistentList<E> rest() {
            return rest;
        }

        @Override
        public PersistentList<E> add( final E element ) {
            return new List<E>( element, this );
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
            return new ListIterator<E>( this );
        }

        // TODO: Make this lazy, and keep the result around. But how lazy, and what parts, needs to be decided.

        // The array doesn't need to be constructed until Iterable.iterator() is called, or it can even wait until
        // Iterator.hasNext() or next() is called. That could be accomplished by wrapping this with Lazy.iterable()
        // and moving the code that builds the array.

        // The Iterable could be kept, or the backing array, or both. Once the backing array is built, you don't
        // need the list any more, so this iterable could be static instead of inner.

        @Override
        public Iterable<E> reverse() {
            // There's no way to do this without saving all the elements, at least no way that isn't O(n^2).
            // An ImmutableList would work, except they don't allow null elements.

            final Object[] array = new Object[ size ];
            PersistentList<E> list = this;
            for( int i = size - 1; i >= 0; i-- ) {
                array[ i ] = list.first();
                list = list.rest();
            }
            return new ArrayIterable<E>( array );
        }

        private static final Joiner JOINER = Joiner.on( ", " ).useForNull( "null" );

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder();
            sb.append( '[' );
            JOINER.appendTo( sb, this );
            sb.append( ']' );
            return sb.toString();
        }
    }

    private static class ListIterator<E> extends UnmodifiableIterator<E> {
        private PersistentList<E> list;

        private ListIterator( final PersistentList<E> list ) {
            this.list = list;
        }

        @Override
        public boolean hasNext() {
            return !list.isEmpty();
        }

        @Override
        public E next() {
            if( list.isEmpty() ) {
                throw new NoSuchElementException();
            }
            final E top = list.first();
            list = list.rest();
            return top;
        }
    }

    private static class ArrayIterable<E> implements Iterable<E> {
        private final Object[] array;

        private ArrayIterable( final Object[] array ) {
            this.array = array;
        }

        @Override
        public Iterator<E> iterator() {
            return new UnmodifiableIterator<E>() {
                private int index;

                @Override
                public boolean hasNext() {
                    return index < array.length;
                }

                @SuppressWarnings( "unchecked" )
                @Override
                public E next() {
                    if( index >= array.length ) {
                        throw new NoSuchElementException();
                    }
                    return (E) array[ index++ ];
                }
            };
        }
    }
}
