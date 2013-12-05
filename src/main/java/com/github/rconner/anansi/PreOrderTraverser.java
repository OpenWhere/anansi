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
 * Package implementation of the Traverser returned by {@link Traversers#preOrder(Traverser)}.
 *
 * @param <V> the vertex type
 * @param <E> the edge type
 *
 * @author rconner
 */
final class PreOrderTraverser<V, E> implements Traverser<V, E> {
    private final Traverser<V, E> adjacency;

    PreOrderTraverser( final Traverser<V, E> adjacency ) {
        this.adjacency = adjacency;
    }

    @Override
    public Iterable<Walk<V, E>> apply( final V start ) {
        return new Iterable<Walk<V, E>>() {
            @Override
            public Iterator<Walk<V, E>> iterator() {
                return new PreOrderIterator<V, E>( start, adjacency );
            }
        };
    }

    private static final class PreOrderIterator<V, E> implements PruningIterator<Walk<V, E>> {
        private final Traverser<V, E> adjacency;
        private PersistentList<TraversalMove<V, E>> moveStack;
        private boolean canMutate = false;

        PreOrderIterator( final V start, final Traverser<V, E> adjacency ) {
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
                canMutate = false;
                throw new NoSuchElementException();
            }
            moveStack = moveStack.add( moveStack.first().next( adjacency ) );
            canMutate = true;
            return moveStack.first().walk;
        }

        @Override
        public void remove() {
            Preconditions.checkState( canMutate );
            // The operations make more sense like this:
            //   moveStack = moveStack.rest();
            //   moveStack.first().iterator.remove();
            // But that doesn't fail atomically.
            moveStack.rest().first().iterator.remove();
            moveStack = moveStack.rest();
            canMutate = false;
        }

        @Override
        public void prune() {
            Preconditions.checkState( canMutate );
            moveStack = moveStack.rest();
            canMutate = false;
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
  A, B, D, E, C, D, F

The root walk will be written as:
  A:-
walk.to = A in this degenerate case, and there are no steps.

A single step walk may be written as from:->to, echoing the Walk structure of [ from, Stack<steps> ]. For example:
  B:->D

A multi-step walk will be written as from:[->to, ->to, ...], with steps ordered from stack top to bottom. For example:
  A:[->E, ->B]

The state of the Iterator is the state of its move stack, which will be written as:

              move.iterator       move.walk.via (move.walk.from is always A)
  bottom      [ A:- * ]           []
              [ A:->B, * A:->C ]  []
              [ B:->D, B:->E * ]  [->B]
  top         [ ]                 [->E, ->B]

Or as just "empty" if there are no moves in the stack. The "*" in the iterator precedes the next walk to be returned.

In the step-by-step below, colloquial language will be used (push, pop, top) rather than the actual method names.

"advance top and push next move" means:
  walk = top.iterator.next()
  push [ children(walk.to), top.walk.append( walk ) ]
So, advance the top iterator and push a move for its children.


@init
                                    [ * A:- ]           []

next()
  pop exhausted iterators           no change

  advance top and push next move    [ A:- * ]           []
                                    [ * A:->B, A:->C ]  []

  return top.walk = A:-

next()
  pop exhausted iterators           no change

  advance top and push next move    [ A:- * ]           []
                                    [ A:->B, * A:->C ]  []
                                    [ * B:->D, B:->E ]  [->B]

  return top.walk = A:[->B]


next()
  pop exhausted iterators           no change

  advance top and push next move    [ A:- * ]           []
                                    [ A:->B, * A:->C ]  []
                                    [ B:->D, * B:->E ]  [->B]
                                    [ ]                 [->D, ->B]

  return top.walk = A:[->D, ->B]


next()
  pop exhausted iterators           [ A:- * ]           []
                                    [ A:->B, * A:->C ]  []
                                    [ B:->D, * B:->E ]  [->B]

  advance top and push next move    [ A:- * ]           []
                                    [ A:->B, * A:->C ]  []
                                    [ B:->D, B:->E * ]  [->B]
                                    [ ]                 [->E, ->B]

  return top.walk = A:[->E, ->B]


next()
  pop exhausted iterators           [ A:- * ]           []
                                    [ A:->B, * A:->C ]  []

  advance top and push next move    [ A:- * ]           []
                                    [ A:->B, A:->C * ]  []
                                    [ * C:->D, C:->F ]  [->C]

  return top.walk = A:[->C]


next()
  pop exhausted iterators           no change

  advance top and push next move    [ A:- * ]           []
                                    [ A:->B, A:->C * ]  []
                                    [ C:->D, * C:->F ]  [->C]
                                    [ ]                 [->D, ->C]

  return top.walk = A:[->D, ->C]


next()
  pop exhausted iterators           [ A:- * ]           []
                                    [ A:->B, A:->C * ]  []
                                    [ C:->D, * C:->F ]  [->C]

  advance top and push next move    [ A:- * ]           []
                                    [ A:->B, A:->C * ]  []
                                    [ C:->D, C:->F * ]  [->C]
                                    [ ]                 [->F, ->C]

  return top.walk = A:[->F, ->C]

next()
  pop exhausted iterators           empty

  throw NoSuchElementException

*/
