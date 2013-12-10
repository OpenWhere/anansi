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
import com.google.common.collect.TreeTraverser;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Package implementation of the Iterator returned by {@link Traversers#breadthFirst(Object, TreeTraverser)}.
 *
 * @param <T> the vertex type
 *
 * @author rconner
 */
final class BreadthFirstIterator<T> implements PruningIterator<T> {
    private final TreeTraverser<T> adjacency;
    private final FifoQueue<Iterator<T>> queue = FifoQueue.of();
    private Iterator<T> nextTail = null;

    BreadthFirstIterator( final T root, final TreeTraverser<T> adjacency ) {
        this.adjacency = adjacency;
        queue.enqueue( TraversalMove.rootIterator( root ) );
    }

    @Override
    public boolean hasNext() {
        if( nextTail != null && nextTail.hasNext() ) {
            return true;
        }
        for( final Iterator<T> iterator : queue ) {
            if( iterator.hasNext() ) {
                return true;
            }
        }
        return false;
    }

    @Override
    public T next() {
        if( nextTail != null && nextTail.hasNext() ) {
            queue.enqueue( nextTail );
            nextTail = null;
        }
        while( !queue.isEmpty() && !queue.head().hasNext() ) {
            queue.dequeue();
        }
        if( queue.isEmpty() ) {
            throw new NoSuchElementException();
        }
        final T vertex = queue.head().next();
        nextTail = adjacency.children( vertex ).iterator();
        return vertex;
    }

    @Override
    public void remove() {
        Preconditions.checkState( nextTail != null );
        queue.head().remove();
        nextTail = null;
    }

    @Override
    public void prune() {
        Preconditions.checkState( nextTail != null );
        nextTail = null;
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
  A, B, C, D, E, D, F

The state of the Iterator is the state of its queue and the value of nextTail, which will be written as:

              iterator
queue|-head   [ B, C * ]
     |-tail   [ * D, E ]
nextTail      T [ * D, F ]

Or as just "empty" if there are no iterators in the queue. The "*" in the iterator precedes the next vertex to be returned.

In the step-by-step below, colloquial language will be used rather than the actual method names.

"nextTail = next move" means:
  vertex = head.next();
  nextTail = adjacency.children( vertex ).iterator();
So, advance the head iterator and set nextTail to the move for its children.


@init
                                      [ * A ]
                                      T null

next()
  enqueue nextTail if not null/empty  no change

  dequeue exhausted iterators         no change

  nextTail = next move                [ A * ]
                                      T [ * B, C ]

  return advanced head vertex = A

next()
  enqueue nextTail if not null/empty  [ A * ]
                                      [ * B, C ]
                                      T null

  dequeue exhausted iterators         [ * B, C ]
                                      T null

  nextTail = next move                [ B, * C ]
                                      T [ * D, E ]

  return advanced head vertex = B

next()
  enqueue nextTail if not null/empty  [ B, * C ]
                                      [ * D, E ]
                                      T null

  dequeue exhausted iterators         no change

  nextTail = next move                [ B, C * ]
                                      [ * D, E ]
                                      T [ * D, F ]

  return advanced head vertex = C

next()
  enqueue nextTail if not null/empty  [ B, C * ]
                                      [ * D, E ]
                                      [ * D, F ]
                                      T null

  dequeue exhausted iterators         [ * D, E ]
                                      [ * D, F ]
                                      T null

  nextTail = next move                [ D, * E ]
                                      [ * D, F ]
                                      T [ ]

  return advanced head vertex = D

next()
  enqueue nextTail if not null/empty  [ D, * E ]
                                      [ * D, F ]
                                      T null

  dequeue exhausted iterators         no change

  nextTail = next move                [ D, E * ]
                                      [ * D, F ]
                                      T [ ]

  return advanced head vertex = E

next()
  enqueue nextTail if not null/empty  [ D, E * ]
                                      [ * D, F ]
                                      T null

  dequeue exhausted iterators         [ * D, F ]
                                      T null

  nextTail = next move                [ D, * F ]
                                      T [ ]

  return advanced head vertex = D

next()
  enqueue nextTail if not null/empty  [ D, * F ]
                                      T null

  dequeue exhausted iterators         no change

  nextTail = next move                [ D, F * ]
                                      T [ ]

  return advanced head vertex = F

next()
  enqueue nextTail if not null/empty  [ D, F * ]
                                      T null

  dequeue exhausted iterators         empty

  throw NoSuchElementException

*/
