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

import com.github.rconner.util.NoCoverage;
import com.google.common.annotations.Beta;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;

import java.util.Iterator;

/**
 * Factory methods for creating lazy Iterators and Iterables.
 *
 * @author rconner
 */
@Beta
public class Lazy {

    /**
     * Prevent instantiation.
     */
    @NoCoverage
    private Lazy() {
    }

    public static <T> Iterator<T> iterator( final Iterable<T> iterable ) {
        Preconditions.checkNotNull( iterable );
        if( iterable instanceof LazyIterable<?> ) {
            return iterable.iterator();
        }
        return new LazyIterator<T>( iterable );
    }

    public static <T> Iterable<T> iterable( final Iterable<T> iterable ) {
        Preconditions.checkNotNull( iterable );
        if( iterable instanceof LazyIterable<?> ) {
            return iterable;
        }
        return new LazyIterable<T>( iterable );
    }

    public static <V, E> Traverser<V, E> traverser( final Function<V, Iterable<Walk<V, E>>> traverser ) {
        Preconditions.checkNotNull( traverser );
        if( traverser instanceof LazyTraverser ) {
            return ( Traverser<V, E> ) traverser;
        }
        return new LazyTraverser<V, E>( traverser );
    }


    private static final class LazyIterator<T> implements Iterator<T> {
        private final Iterable<T> iterable;
        private Iterator<T> delegate;

        LazyIterator( final Iterable<T> iterable ) {
            this.iterable = iterable;
        }

        private Iterator<T> getDelegate() {
            if( delegate == null ) {
                delegate = iterable.iterator();
            }
            return delegate;
        }

        @Override
        public boolean hasNext() {
            return getDelegate().hasNext();
        }

        @Override
        public T next() {
            return getDelegate().next();
        }

        @Override
        public void remove() {
            Preconditions.checkState( delegate != null );
            delegate.remove();
        }
    }

    private static final class LazyIterable<T> implements Iterable<T> {
        private final Iterable<T> delegate;

        LazyIterable( final Iterable<T> delegate ) {
            this.delegate = delegate;
        }

        @Override
        public Iterator<T> iterator() {
            return Lazy.iterator( delegate );
        }
    }

    private static final class LazyTraverser<V, E> implements Traverser<V, E> {
        private final Function<V, Iterable<Walk<V, E>>> delegate;

        LazyTraverser( final Function<V, Iterable<Walk<V, E>>> delegate ) {
            this.delegate = delegate;
        }

        @Override
        public Iterable<Walk<V, E>> apply( final V from ) {
            return new Iterable<Walk<V, E>>() {
                @Override
                public Iterator<Walk<V, E>> iterator() {
                    return Lazy.iterator( delegate.apply( from ) );
                }
            };
        }
    }

}
