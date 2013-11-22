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

import com.github.rconner.util.FifoQueue;
import com.google.common.base.Preconditions;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Package implementation of the Traverser returned by {@link Traversers#breadthFirst(Traverser)}.
 *
 * @param <V>
 * @param <E>
 */
final class BreadthFirstTraverser<V, E> implements Traverser<V, E> {
    private final Traverser<V, E> adjacency;

    BreadthFirstTraverser( final Traverser<V, E> adjacency ) {
        this.adjacency = adjacency;
    }

    @Override
    public Iterable<Walk<V, E>> apply( final V start ) {
        return new Iterable<Walk<V, E>>() {
            @Override
            public Iterator<Walk<V, E>> iterator() {
                return new BreadthFirstIterator<V, E>( start, adjacency );
            }
        };
    }

    private static final class BreadthFirstIterator<V, E> implements PruningIterator<Walk<V, E>> {
        /**
         * The supplied adjacency function.
         */
        private final Traverser<V, E> adjacency;

        @SuppressWarnings( "unchecked" )
        private final FifoQueue<TraversalMove<V, E>> moveQueue = FifoQueue.of();

        /**
         * True if this iterator is in a valid state for calling remove() or prune().
         */
        private boolean canMutate = false;

        BreadthFirstIterator( final V start, final Traverser<V, E> adjacency ) {
            this.adjacency = adjacency;
            moveQueue.enqueue( TraversalMove.<V, E>start( start ) );
        }

        @Override
        public boolean hasNext() {
            for( final TraversalMove<V, E> move : moveQueue ) {
                if( move.iterator.hasNext() ) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public Walk<V, E> next() {
            while( !moveQueue.isEmpty() && !moveQueue.head().iterator.hasNext() ) {
                moveQueue.dequeue();
            }
            if( moveQueue.isEmpty() ) {
                throw new NoSuchElementException();
            }
            TraversalMove<V, E> move = moveQueue.head();
            move = move.with( adjacency, move.iterator.next() );
            moveQueue.enqueue( move );
            canMutate = true;
            return move.builder.build();
        }

        @Override
        public void remove() {
            Preconditions.checkState( canMutate );
            moveQueue.head().iterator.remove();
            // FIXME: need to remove last queue element
            canMutate = false;
        }

        @Override
        public void prune() {
            Preconditions.checkState( canMutate );
            // FIXME: need to remove last queue element
            canMutate = false;
        }
    }
}
