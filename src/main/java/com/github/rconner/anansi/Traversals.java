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
import com.github.rconner.util.PersistentList;
import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.TreeTraverser;

import java.util.Iterator;

/**
 * Static factory methods for building traversals.
 *
 * @author rconner
 */
@Beta
public final class Traversals {

    /**
     * Prevent instantiation.
     */
    @NoCoverage
    private Traversals() {
    }

    /**
     * Returns a pre-order iterable with <strong>NO</strong> cycle detection. This differs from {@link
     * TreeTraverser#preOrderTraversal(Object)} in that the created Iterators implement {@link PruningIterator} and
     * support the remove() operation if the Iterators produced by the argument adjacency TreeTraverser do. The root
     * vertex of the traversal cannot be removed.
     *
     * @param adjacency the adjacency function to use
     * @param <T> the vertex type
     *
     * @return a pre-order iterable with <strong>NO</strong> cycle detection.
     */
    public static <T> FluentIterable<T> preOrder( final T root, final TreeTraverser<T> adjacency ) {
        Preconditions.checkNotNull( adjacency );
        return new FluentIterable<T>() {
            @Override
            public Iterator<T> iterator() {
                return new PreOrderIterator<T>( root, Lazy.traverser( adjacency ) );
            }
        };
    }

    /**
     * Returns a post-order iterable with <strong>NO</strong> cycle detection. If a cycle is present, some call to
     * next() will infinitely loop (most likely resulting in an OutOfMemoryError). This differs from {@link
     * TreeTraverser#postOrderTraversal(Object)} in that the created Iterators support the remove() operation if the
     * Iterators produced by the argument adjacency TreeTraverser do. The root vertex of the traversal cannot be
     * removed.
     *
     * @param adjacency the adjacency function to use
     * @param <T> the vertex type
     *
     * @return a post-order iterable with <strong>NO</strong> cycle detection.
     */
    public static <T> FluentIterable<T> postOrder( final T root, final TreeTraverser<T> adjacency ) {
        Preconditions.checkNotNull( adjacency );
        return new FluentIterable<T>() {
            @Override
            public Iterator<T> iterator() {
                return new PostOrderIterator<T>( root, Lazy.traverser( adjacency ) );
            }
        };
    }

    /**
     * Returns a breadth-first iterable with <strong>NO</strong> cycle detection. This differs from {@link
     * TreeTraverser#breadthFirstTraversal(Object)} in that the created Iterators implement {@link PruningIterator} and
     * support the remove() operation if the Iterators produced by the argument adjacency TreeTraverser do. The root
     * vertex of the traversal cannot be removed.
     *
     * @param adjacency the adjacency function to use
     * @param <T> the vertex type
     *
     * @return a breadth-first iterable with <strong>NO</strong> cycle detection.
     */
    public static <T> FluentIterable<T> breadthFirst( final T root, final TreeTraverser<T> adjacency ) {
        Preconditions.checkNotNull( adjacency );
        return new FluentIterable<T>() {
            @Override
            public Iterator<T> iterator() {
                return new BreadthFirstIterator<T>( root, Lazy.traverser( adjacency ) );
            }
        };
    }

    /**
     * Returns an iterable to reachable leaves with <strong>NO</strong> cycle detection. If a cycle is present, some
     * call to next() will infinitely loop (most likely resulting in an OutOfMemoryError).
     *
     * @param adjacency the adjacency function to use
     * @param <T> the vertex type
     *
     * @return an iterable to reachable leaves with <strong>NO</strong> cycle detection.
     */
    public static <T> FluentIterable<T> leaves( final T root, final TreeTraverser<T> adjacency ) {
        Preconditions.checkNotNull( adjacency );
        return new FluentIterable<T>() {
            @Override
            public Iterator<T> iterator() {
                return new LeafIterator<T>( root, Lazy.traverser( adjacency ) );
            }
        };
    }

    /**
     * Returns a {@link #leaves(Object, TreeTraverser)} Iterable which uses {@link Traversers#elements()} as an
     * adjacency TreeTraverser.
     *
     * @return a {@code #leaves(Object, TreeTraverser)} Iterable which uses {@link Traversers#elements()} as an
     * adjacency TreeTraverser.
     */
    public static FluentIterable<PersistentList<Step<Object, String>>> leafElements( final Object root ) {
        return leaves( PersistentList.of( Step.<Object, String>empty( root ) ), Traversers.elements() );
    }

    /**
     * Returns an idiomatic String path for the given walk produced by {@link #leafElements(Object)}.
     *
     * @param walk the walk for which to return the idiomatic String path.
     *
     * @return an idiomatic String path for the given walk produced by {@code leafElements(Object)}.
     */
    public static String elementPath( final PersistentList<Step<Object, String>> walk ) {
        return Elements.path( walk );
    }
}
