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
import com.google.common.collect.Iterators;
import com.google.common.collect.UnmodifiableIterator;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A minimal persistent list implementation, behaving somewhat like a stack, which does <strong>not</strong> implement
 * {@link java.util.Collection}. Note that it is not necessary for the contained elements to also be immutable; just
 * remember that shared mutable objects require synchronization even if they are contained in immutable data structures.
 * <p/>
 * An {@link #add(Object)} operation returns a new list. A {@link #rest()} operation returns the list upon which {@link
 * #add(Object)} was called to add the first element. Because instances are immutable, storage can be (and is) reused. A
 * single list instance can have many independent elements added to it, with each add resulting in a new list instance
 * sharing the storage for all the elements beyond the first one.
 * <p/>
 * Instances of this class inherit {@link #equals(Object)} and {@link #hashCode()} from {@link Object}.
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

    public abstract E last();

    public abstract PersistentList<E> rest();

    public abstract PersistentList<E> add( E element );

    public abstract boolean isEmpty();

    public abstract int size();

    @Override
    public abstract Iterator<E> iterator();

    public abstract PersistentList<E> reverse();

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
        return new SingleList<E>( element );
    }

    /**
     * Creates a new PersistentList with the given elements, in order from first to last.
     *
     * @param elements the elements for which to create a new PersistentList
     * @param <E> the type of element
     *
     * @return a new PersistentList with the given elements, in order from first to last.
     */
    public static <E> PersistentList<E> of( final E... elements ) {
        PersistentList<E> list = of();
        for( int i = elements.length - 1; i >= 0; i-- ) {
            list = list.add( elements[i] );
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
        public Object last() {
            throw new NoSuchElementException();
        }

        @Override
        public PersistentList<Object> rest() {
            throw new NoSuchElementException();
        }

        @Override
        public PersistentList<Object> add( final Object element ) {
            return new SingleList<Object>( element );
        }

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public Iterator<Object> iterator() {
            return Iterators.emptyIterator();
        }

        @Override
        public PersistentList<Object> reverse() {
            return this;
        }

        @Override
        public String toString() {
            return "[]";
        }
    }

    private static final class SingleList<E> extends PersistentList<E> {
        private final E element;

        SingleList( final E element ) {
            this.element = element;
        }

        @Override
        public E first() {
            return element;
        }

        @Override
        public E last() {
            return element;
        }

        @SuppressWarnings( "unchecked" )
        @Override
        public PersistentList<E> rest() {
            return (PersistentList<E>) EmptyList.INSTANCE;
        }

        @Override
        public PersistentList<E> add( final E element ) {
            return new List<E>( element, this );
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public int size() {
            return 1;
        }

        @Override
        public Iterator<E> iterator() {
            return Iterators.singletonIterator( element );
        }

        @Override
        public PersistentList<E> reverse() {
            return this;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder();
            sb.append( '[' );
            sb.append( element );
            sb.append( ']' );
            return sb.toString();
        }
    }

    private static final class List<E> extends PersistentList<E> {
        private final E first;
        private final PersistentList<E> rest;
        private final E last;
        private final int size;
        // Must be volatile to be used by the double-check locking idiom in reverse()
        private volatile PersistentList<E> reverse;

        List( final E first, final PersistentList<E> rest ) {
            this.first = first;
            this.rest = rest;
            this.last = rest.last();
            this.size = rest.size() + 1;
        }

        @Override
        public E first() {
            return first;
        }

        @Override
        public E last() {
            return last;
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
        public boolean isEmpty() {
            return false;
        }

        @Override
        public int size() {
            return size;
        }

        @Override
        public Iterator<E> iterator() {
            return new ListIterator<E>( this );
        }

        @Override
        public PersistentList<E> reverse() {
            PersistentList<E> result = reverse;
            if( result == null ) {
                synchronized( this ) {
                    result = reverse;
                    if( result == null ) {
                        result = PersistentList.of();
                        for( final E element : this ) {
                            result = result.add( element );
                        }
                        ( (List<E>) result ).reverse = this;
                        reverse = result;
                    }
                }
            }
            return result;
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
            final E first = list.first();
            list = list.rest();
            return first;
        }
    }
}
