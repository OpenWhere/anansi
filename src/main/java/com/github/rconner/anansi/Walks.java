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

import com.google.common.collect.FluentIterable;
import com.google.common.collect.TreeTraverser;

import java.util.Iterator;

/**
 * Static factory methods related to Walks and traversals producing them.
 */
public final class Walks {

    public static <V, E> TreeTraverser<Walk<V, E>> of( final TreeTraverser<V> traverser ) {
        return new TreeTraverser<Walk<V, E>>() {
            @Override
            public Iterable<Walk<V, E>> children( final Walk<V, E> root ) {
                return new FluentIterable<Walk<V, E>>() {
                    @Override
                    public Iterator<Walk<V, E>> iterator() {
                        return new WalkingIterator<V, E>( root, traverser );
                    }
                };
            }
        };
    }

    private static class WalkingIterator<V, E> implements Iterator<Walk<V, E>> {
        private final Walk<V, E> root;
        private final Iterator<V> children;

        private WalkingIterator( final Walk<V, E> root, final TreeTraverser<V> traverser ) {
            this.root = root;
            children = traverser.children( root.getTo() ).iterator();
        }

        @Override
        public boolean hasNext() {
            return children.hasNext();
        }

        @Override
        public Walk<V, E> next() {
            return root.append( children.next() );
        }

        @Override
        public void remove() {
            children.remove();
        }
    }

    // Need *all* of the existing traversals wrapped this way...

    public static <V, E> FluentIterable<Walk<V, E>> preOrder( final V root, final TreeTraverser<V> adjacency ) {
        return Traversals.preOrder( Walk.<V, E>empty( root ), Walks.<V, E>of( adjacency ) );
    }
}
