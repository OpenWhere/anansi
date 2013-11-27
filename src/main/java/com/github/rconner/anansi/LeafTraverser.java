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

import com.github.rconner.util.ImmutableStack;
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
        private ImmutableStack<TraversalMove<V, E>> moveStack;

        @SuppressWarnings( "unchecked" )
        LeafIterator( final V start, final Traverser<V, E> adjacency ) {
            this.adjacency = adjacency;
            moveStack = ImmutableStack.of( TraversalMove.<V, E>start( start ) );
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
            while( !moveStack.isEmpty() && !moveStack.peek().iterator.hasNext() ) {
                moveStack = moveStack.pop();
            }
            if( moveStack.isEmpty() ) {
                throw new NoSuchElementException();
            }
            TraversalMove<V, E> move = moveStack.peek();
            while( move.iterator.hasNext() ) {
                move = move.with( adjacency, move.iterator.next() );
                moveStack = moveStack.push( move );
            }
            final Walk<V, E> walk = move.builder.build();
            moveStack = moveStack.pop();
            return walk;
        }

        @Override
        public void remove() {
            Preconditions.checkState( !moveStack.isEmpty() );
            moveStack.peek().iterator.remove();
        }
    }
}
