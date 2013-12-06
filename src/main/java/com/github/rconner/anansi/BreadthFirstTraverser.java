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

import com.github.rconner.util.FifoQueue;
import com.google.common.base.Preconditions;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Package implementation of the Traverser returned by {@link Traversers#breadthFirst(Traverser)}.
 *
 * @param <V> the vertex type
 * @param <E> the edge type
 *
 * @author rconner
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
        private final Traverser<V, E> adjacency;
        private final FifoQueue<TraversalMove<V, E>> moveQueue = FifoQueue.of();
        private TraversalMove<V, E> nextTail = null;

        BreadthFirstIterator( final V start, final Traverser<V, E> adjacency ) {
            this.adjacency = adjacency;
            moveQueue.enqueue( TraversalMove.<V, E>start( start ) );
        }

        @Override
        public boolean hasNext() {
            if( nextTail != null && nextTail.iterator.hasNext() ) {
                return true;
            }
            for( final TraversalMove<V, E> move : moveQueue ) {
                if( move.iterator.hasNext() ) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public Walk<V, E> next() {
            if( nextTail != null && nextTail.iterator.hasNext() ) {
                moveQueue.enqueue( nextTail );
                nextTail = null;
            }
            while( !moveQueue.isEmpty() && !moveQueue.head().iterator.hasNext() ) {
                moveQueue.dequeue();
            }
            if( moveQueue.isEmpty() ) {
                throw new NoSuchElementException();
            }
            nextTail = moveQueue.head().next( adjacency );
            return nextTail.walk;
        }

        @Override
        public void remove() {
            Preconditions.checkState( nextTail != null );
            moveQueue.head().iterator.remove();
            nextTail = null;
        }

        @Override
        public void prune() {
            Preconditions.checkState( nextTail != null );
            nextTail = null;
        }
    }
}

/*

This is a step-by-step example of iterating with this traverser. The example graph is below, child order is alphabetic.

          A
         / \
        B   C
       / \ / \
      E   D   F

The iteration order will be (walks from A to):
  A, B, C, D, E, D, F

The root walk will be written as:
  A:-
walk.to = A in this degenerate case, and there are no steps.

A single step walk may be written as from:->to, echoing the Walk structure of [ from, Stack<steps> ]. For example:
  B:->D

A multi-step walk will be written as from:[->to, ->to, ...], with steps ordered from stack top to bottom. For example:
  A:[->E, ->B]

The state of the Iterator is the state of its move queue and the value of nextTail, which will be written as:

              move.iterator       move.walk.via (move.walk.from is always A)
queue|-head   [ A:->B, A:->C * ]    []
     |-tail   [ * B:->D, B:->E ]    [->B]
nextTail      T [ * C:->D, C:->F ]  [->C]

Or as just "empty" if there are no moves in the queue. The "*" in the iterator precedes the next walk to be returned.

In the step-by-step below, colloquial language will be used rather than the actual method names.

"nextTail = next move" means:
  walk = head.iterator.next()
  nextTail = [ children(walk.to), head.walk.append( walk ) ]
So, advance the head iterator and set nextTail to the move for its children.


@init
                                      [ * A:- ]             []
                                      T null

next()
  enqueue nextTail if not null/empty  no change

  dequeue exhausted iterators         no change

  nextTail = next move                [ A:- * ]             []
                                      T [ * A:->B, A:->C ]  []

  return nextTail.walk = A:-

next()
  enqueue nextTail if not null/empty  [ A:- * ]             []
                                      [ * A:->B, A:->C ]    []
                                      T null

  dequeue exhausted iterators         [ * A:->B, A:->C ]    []
                                      T null

  nextTail = next move                [ A:->B, * A:->C ]    []
                                      T [ * B:->D, B:->E ]  [->B]

  return nextTail.walk = A:[->B]

next()
  enqueue nextTail if not null/empty  [ A:->B, * A:->C ]    []
                                      [ * B:->D, B:->E ]    [->B]
                                      T null

  dequeue exhausted iterators         no change

  nextTail = next move                [ A:->B, A:->C * ]    []
                                      [ * B:->D, B:->E ]    [->B]
                                      T [ * C:->D, C:->F ]  [->C]

  return nextTail.walk = A:[->C]

next()
  enqueue nextTail if not null/empty  [ A:->B, A:->C * ]    []
                                      [ * B:->D, B:->E ]    [->B]
                                      [ * C:->D, C:->F ]    [->C]
                                      T null

  dequeue exhausted iterators         [ * B:->D, B:->E ]    [->B]
                                      [ * C:->D, C:->F ]    [->C]
                                      T null

  nextTail = next move                [ B:->D, * B:->E ]    [->B]
                                      [ * C:->D, C:->F ]    [->C]
                                      T [ ]                 [->D, ->B]

  return nextTail.walk = A:[->D, ->B]

next()
  enqueue nextTail if not null/empty  [ B:->D, * B:->E ]    [->B]
                                      [ * C:->D, C:->F ]    [->C]
                                      T null

  dequeue exhausted iterators         no change

  nextTail = next move                [ B:->D, B:->E * ]    [->B]
                                      [ * C:->D, C:->F ]    [->C]
                                      T [ ]                 [->E, ->B]

  return nextTail.walk = A:[->E, ->B]

next()
  enqueue nextTail if not null/empty  [ B:->D, B:->E * ]    [->B]
                                      [ * C:->D, C:->F ]    [->C]
                                      T null

  dequeue exhausted iterators         [ * C:->D, C:->F ]    [->C]
                                      T null

  nextTail = next move                [ C:->D, * C:->F ]    [->C]
                                      T [ ]                 [->D, ->C]

  return nextTail.walk = A:[->D, ->C]

next()
  enqueue nextTail if not null/empty  [ C:->D, * C:->F ]    [->C]
                                      T null

  dequeue exhausted iterators         no change

  nextTail = next move                [ C:->D, C:->F * ]    [->C]
                                      T [ ]                 [->F, ->C]

  return nextTail.walk = A:[->F, ->C]

next()
  enqueue nextTail if not null/empty  [ C:->D, C:->F * ]    [->C]
                                      T null

  dequeue exhausted iterators         empty

  throw NoSuchElementException

*/
