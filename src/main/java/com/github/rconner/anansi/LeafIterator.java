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
import com.google.common.collect.TreeTraverser;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Package implementation of the Iterator returned by {@link Traversals#leaves(Object, TreeTraverser)}.
 *
 * @param <T> the vertex type
 *
 * @author rconner
 */
final class LeafIterator<T> implements Iterator<T> {
    private final TreeTraverser<T> adjacency;
    private PersistentList<Move<T>> stack;

    LeafIterator( final T root, final TreeTraverser<T> adjacency) {
        this.adjacency = adjacency;
        stack = PersistentList.of( new Move<T>( root, new RootIterator<T>( root ) ) );
    }

    @Override
    public boolean hasNext() {
        for( final Move<T> move : stack ) {
            if( move.iterator.hasNext() ) {
                return true;
            }
        }
        return false;
    }

    @Override
    public T next() {
        while( !stack.isEmpty() && !stack.first().iterator.hasNext() ) {
            stack = stack.rest();
        }
        if( stack.isEmpty() ) {
            throw new NoSuchElementException();
        }
        while( stack.first().iterator.hasNext() ) {
            final T vertex = stack.first().iterator.next();
            stack = stack.add( new Move<T>( vertex, adjacency.children( vertex ).iterator() ) );
        }
        final T vertex = stack.first().vertex;
        stack = stack.rest();
        return vertex;
    }

    @Override
    public void remove() {
        Preconditions.checkState( !stack.isEmpty() );
        stack.first().iterator.remove();
    }

    private static final class Move<T> {
        final T vertex;
        final Iterator<T> iterator;

        Move( final T vertex, final Iterator<T> iterator ) {
            this.vertex = vertex;
            this.iterator = iterator;
        }
    }
}

/*

This is a step-by-step example of iterating with this Iterator. The example graph is below, child order is alphabetic.

          A
         / \
        B   C
       / \ / \
      E   D   F

The iteration order will be:
  D, E, D, F

The state of the Iterator is the state of its move stack, which will be written as:

              move.vertex  move.iterator
  bottom      A            [ B, * C ]
              B            [ D, E * ]
  top         E            [ ]

Or as just "empty" if there are no moves in the stack. The "*" in the iterator precedes the next vertex to be returned.

In the step-by-step below, colloquial language will be used (push, pop, top) rather than the actual method names.

"advance top and push next" means:
  vertex = top.iterator.next()
  push [ vertex, children(vertex).iterator() ]
So, advance the top iterator and push a move for its children.


@init
                                           A  [ * A ]

next()
  pop exhausted iterators                  no change

  while( top.iterator not exhausted ) {    A  [ A * ]
    advance top and push next              A  [ B, * C ]
  }                                        B  [ D, * E ]
                                           D  [ ]

  pop stack                                A  [ A * ]
                                           A  [ B, * C ]
                                           B  [ D, * E ]

  return popped move.vertex = D

next()
  pop exhausted iterators                  no change

  while( top.iterator not exhausted ) {    A  [ A * ]
    advance top and push next              A  [ B, * C ]
  }                                        B  [ D, E * ]
                                           E  [ ]

  pop stack                                A  [ A * ]
                                           A  [ B, * C ]
                                           B  [ D, E * ]

  return popped move.vertex = E

next()
  pop exhausted iterators                  A  [ A * ]
                                           A  [ B, * C ]

  while( top.iterator not exhausted ) {    A  [ A * ]
    advance top and push next              A  [ B, C * ]
  }                                        C  [ D, * F ]
                                           D  [ ]

  pop stack                                A  [ A * ]
                                           A  [ B, C * ]
                                           C  [ D, * F ]

  return popped move.vertex = D

next()
  pop exhausted iterators                  no change

  while( top.iterator not exhausted ) {    A  [ A * ]
    advance top and push next              A  [ B, C * ]
  }                                        C  [ D, F * ]
                                           F  [ ]

  pop stack                                A  [ A * ]
                                           A  [ B, C * ]
                                           C  [ D, F * ]

  return popped move.vertex = F

next()
  pop exhausted iterators                  empty

  throw NoSuchElementException

*/
