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

import com.github.rconner.util.PersistentList;
import com.google.common.base.Preconditions;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Package implementation of the Traverser returned by {@link Traversers#leaves(Traverser)}.
 *
 * @param <V> the vertex type
 * @param <E> the edge type
 *
 * @author rconner
 */
final class LeafTraverser<V, E> implements Traverser<V, E> {
    private final Traverser<V, E> adjacency;

    LeafTraverser( final Traverser<V, E> adjacency ) {
        this.adjacency = adjacency;
    }

    @Override
    public Iterable<Walk<V, E>> apply( final V start ) {
        return new Iterable<Walk<V, E>>() {
            @Override
            public Iterator<Walk<V, E>> iterator() {
                return new LeafIterator<V, E>( start, adjacency );
            }
        };
    }

    private static final class LeafIterator<V, E> implements Iterator<Walk<V, E>> {
        private final Traverser<V, E> adjacency;
        private PersistentList<TraversalMove<V, E>> moveStack;

        LeafIterator( final V start, final Traverser<V, E> adjacency ) {
            this.adjacency = adjacency;
            moveStack = PersistentList.of( TraversalMove.<V, E>start( start ) );
        }

        @Override
        public boolean hasNext() {
            for( final TraversalMove<V, E> move : moveStack ) {
                if( move.iterator.hasNext() ) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public Walk<V, E> next() {
            while( !moveStack.isEmpty() && !moveStack.first().iterator.hasNext() ) {
                moveStack = moveStack.rest();
            }
            if( moveStack.isEmpty() ) {
                throw new NoSuchElementException();
            }
            TraversalMove<V, E> move = moveStack.first();
            while( move.iterator.hasNext() ) {
                move = move.next( adjacency );
                moveStack = moveStack.add( move );
            }
            moveStack = moveStack.rest();
            return move.walk;
        }

        @Override
        public void remove() {
            Preconditions.checkState( !moveStack.isEmpty() );
            moveStack.first().iterator.remove();
        }
    }
}
