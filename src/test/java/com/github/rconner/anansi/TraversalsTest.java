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
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.TreeTraverser;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import static com.github.rconner.util.IterableTest.assertIteratorEmpty;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class TraversalsTest {

    // We'll be using Multimaps to define adjacency for test cases.
    // Lacking a key (vertex) means that there are no children for that vertex. There is no way, using this
    // representation, to denote whether or not a vertex is present. Any vertex is present.

    private final Multimap<String, String> empty = ImmutableMultimap.of();

    private final Multimap<String, String> singleEdge = ImmutableMultimap.of( "A", "B" );

    // Warning! Do not perform a post-order traversal on this graph.
    private final Multimap<String, String> loop = ImmutableMultimap.of( "A", "A" );

    // Warning! Do not perform a post-order traversal on this graph.
    private final Multimap<String, String> cycle = ImmutableListMultimap.<String, String>builder()
            .put( "A", "B" )
            .put( "B", "C" )
            .put( "C", "A" )
            .build();

    private final Multimap<String, String> tree = ImmutableListMultimap.<String, String>builder()
            .put( "A", "B" )
            .put( "A", "C" )
            .put( "B", "D" )
            .put( "B", "E" )
            .put( "C", "F" )
            .put( "C", "G" )
            .build();

    // Has two paths from A to D.
    private final Multimap<String, String> dag = ImmutableListMultimap.<String, String>builder()
            .put( "A", "B" )
            .put( "A", "C" )
            .put( "B", "D" )
            .put( "B", "E" )
            .put( "C", "D" )
            .put( "D", "G" )
            .build();

    private static TreeTraverser<String> adjacencyFor( final Multimap<String, String> graph ) {
        return new TreeTraverser<String>() {
            @Override
            public Iterable<String> children( final String vertex ) {
                return graph.get( vertex );
            }
        };
    }

    private static TreeTraverser<String> mutableAdjacencyFor( final Multimap<String, String> graph ) {
        return adjacencyFor( ArrayListMultimap.create( graph ) );
    }


    private static final String[] EMPTY_EXPECTED_VERTICES = new String[0];

    private static void assertNextVerticesAre( final Iterator<String> iterator, final String... expected ) {
        for( final String vertex : expected ) {
            assertThat( iterator.hasNext(), is( true ) );
            assertThat( iterator.next(), is( vertex ) );
        }
    }

    private static void assertTraversalContains( final Iterable<String> traversal, final String... expected ) {
        final Iterator<String> iterator = traversal.iterator();
        assertNextVerticesAre( iterator, expected );
        assertThat( iterator.hasNext(), is( false ) );
        assertIteratorEmpty( iterator );
    }

    private static void assertTraversalBegins( final Iterable<String> traversal, final String... expected ) {
        final Iterator<String> iterator = traversal.iterator();
        assertNextVerticesAre( iterator, expected );
        assertThat( iterator.hasNext(), is( true ) );
    }

    // preOrder( root, Traverser )

    @Test
    public void preOrderEmpty() {
        final Iterable<String> traverser = Traversals.preOrder( "A", adjacencyFor( empty ) );
        assertTraversalContains( traverser, "A" );
    }

    @Test
    public void preOrderSingleEdge() {
        final Iterable<String> traverser = Traversals.preOrder( "A", adjacencyFor( singleEdge ) );
        assertTraversalContains( traverser, "A", "B" );
    }

    @Test
    public void preOrderLoop() {
        final Iterable<String> traverser = Traversals.preOrder( "A", adjacencyFor( loop ) );
        assertTraversalBegins( traverser, "A", "A", "A", "A" );
    }

    @Test
    public void preOrderCycle() {
        final Iterable<String> traverser = Traversals.preOrder( "A", adjacencyFor( cycle ) );
        assertTraversalBegins( traverser, "A", "B", "C", "A", "B" );
    }

    @Test
    public void preOrderTree() {
        final Iterable<String> traverser = Traversals.preOrder( "A", adjacencyFor( tree ) );
        assertTraversalContains( traverser, "A", "B", "D", "E", "C", "F", "G" );
    }

    private static void assertPreOrderFullDag( final Iterable<String> traversal ) {
        assertTraversalContains( traversal, "A", "B", "D", "G", "E", "C", "D", "G" );
    }

    @Test
    public void preOrderDag() {
        final Iterable<String> traverser = Traversals.preOrder( "A", adjacencyFor( dag ) );
        assertPreOrderFullDag( traverser );
    }

    @Test( expected = IllegalStateException.class )
    public void preOrderRemoveBeforeNext() {
        final TreeTraverser<String> adjacency = mutableAdjacencyFor( dag );
        final Iterable<String> traverser = Traversals.preOrder( "A", adjacency );
        final Iterator<String> iterator = traverser.iterator();
        iterator.remove();
    }

    @Test( expected = IllegalStateException.class )
    public void preOrderRemoveRoot() {
        final TreeTraverser<String> adjacency = mutableAdjacencyFor( dag );
        final Iterable<String> traverser = Traversals.preOrder( "A", adjacency );
        final Iterator<String> iterator = traverser.iterator();
        assertNextVerticesAre( iterator, "A" );
        iterator.remove();
    }

    @Test
    public void preOrderRemoveB() {
        final TreeTraverser<String> adjacency = mutableAdjacencyFor( dag );
        final Iterable<String> traverser = Traversals.preOrder( "A", adjacency );
        final Iterator<String> iterator = traverser.iterator();
        assertNextVerticesAre( iterator, "A", "B" );
        iterator.remove();
        assertNextVerticesAre( iterator, "C", "D", "G" );

        // Check that data structure was actually changed
        assertTraversalContains( Traversals.preOrder( "A", adjacency ), "A", "C", "D", "G" );
    }

    @Test
    public void preOrderRemoveD() {
        final TreeTraverser<String> adjacency = mutableAdjacencyFor( dag );
        final Iterable<String> traverser = Traversals.preOrder( "A", adjacency );
        final Iterator<String> iterator = traverser.iterator();
        assertNextVerticesAre( iterator, "A", "B", "D" );
        iterator.remove();
        assertNextVerticesAre( iterator, "E", "C", "D", "G" );

        // Check that data structure was actually changed
        assertTraversalContains( Traversals.preOrder( "A", adjacency ), "A", "B", "E", "C", "D", "G" );
    }

    @Test
    public void preOrderRemoveE() {
        final TreeTraverser<String> adjacency = mutableAdjacencyFor( dag );
        final Iterable<String> traverser = Traversals.preOrder( "A", adjacency );
        final Iterator<String> iterator = traverser.iterator();
        assertNextVerticesAre( iterator, "A", "B", "D", "G", "E" );
        iterator.remove();
        assertNextVerticesAre( iterator, "C", "D", "G" );

        // Check that data structure was actually changed
        assertTraversalContains( Traversals.preOrder( "A", adjacency ), "A", "B", "D", "G", "C", "D", "G" );
    }

    @Test( expected = IllegalStateException.class )
    public void preOrderRemoveTwice() {
        final TreeTraverser<String> adjacency = mutableAdjacencyFor( dag );
        final Iterable<String> traverser = Traversals.preOrder( "A", adjacency );
        final Iterator<String> iterator = traverser.iterator();
        assertNextVerticesAre( iterator, "A", "B" );
        iterator.remove();
        iterator.remove();
    }

    @Test( expected = IllegalStateException.class )
    public void preOrderPruneBeforeNext() {
        final TreeTraverser<String> adjacency = mutableAdjacencyFor( dag );
        final Iterable<String> traverser = Traversals.preOrder( "A", adjacency );
        final Iterator<String> iterator = traverser.iterator();
        ( (PruningIterator) iterator ).prune();
    }

    @Test
    public void preOrderPruneRoot() {
        final TreeTraverser<String> adjacency = mutableAdjacencyFor( dag );
        final Iterable<String> traverser = Traversals.preOrder( "A", adjacency );
        final Iterator<String> iterator = traverser.iterator();
        assertNextVerticesAre( iterator, "A" );
        ( (PruningIterator) iterator ).prune();
        assertIteratorEmpty( iterator );

        // Check that data structure is unchanged
        assertPreOrderFullDag( Traversals.preOrder( "A", adjacency ) );
    }

    @Test
    public void preOrderPruneB() {
        final TreeTraverser<String> adjacency = mutableAdjacencyFor( dag );
        final Iterable<String> traverser = Traversals.preOrder( "A", adjacency );
        final Iterator<String> iterator = traverser.iterator();
        assertNextVerticesAre( iterator, "A", "B" );
        ( (PruningIterator) iterator ).prune();
        assertNextVerticesAre( iterator, "C", "D", "G" );

        // Check that data structure is unchanged
        assertPreOrderFullDag( Traversals.preOrder( "A", adjacency ) );
    }

    @Test
    public void preOrderPruneD() {
        final TreeTraverser<String> adjacency = mutableAdjacencyFor( dag );
        final Iterable<String> traverser = Traversals.preOrder( "A", adjacency );
        final Iterator<String> iterator = traverser.iterator();
        assertNextVerticesAre( iterator, "A", "B", "D" );
        ( (PruningIterator) iterator ).prune();
        assertNextVerticesAre( iterator, "E", "C", "D", "G" );

        // Check that data structure is unchanged
        assertPreOrderFullDag( Traversals.preOrder( "A", adjacency ) );
    }

    @Test
    public void preOrderPruneE() {
        final TreeTraverser<String> adjacency = mutableAdjacencyFor( dag );
        final Iterable<String> traverser = Traversals.preOrder( "A", adjacency );
        final Iterator<String> iterator = traverser.iterator();
        assertNextVerticesAre( iterator, "A", "B", "D", "G", "E" );
        ( (PruningIterator) iterator ).prune();
        assertNextVerticesAre( iterator, "C", "D", "G" );

        // Check that data structure is unchanged
        assertPreOrderFullDag( Traversals.preOrder( "A", adjacency ) );
    }

    @Test( expected = IllegalStateException.class )
    public void preOrderPruneTwice() {
        final TreeTraverser<String> adjacency = mutableAdjacencyFor( dag );
        final Iterable<String> traverser = Traversals.preOrder( "A", adjacency );
        final Iterator<String> iterator = traverser.iterator();
        assertNextVerticesAre( iterator, "A", "B" );
        ( (PruningIterator) iterator ).prune();
        ( (PruningIterator) iterator ).prune();
    }

    @Test( expected = IllegalStateException.class )
    public void preOrderRemoveThenPrune() {
        final TreeTraverser<String> adjacency = mutableAdjacencyFor( dag );
        final Iterable<String> traverser = Traversals.preOrder( "A", adjacency );
        final Iterator<String> iterator = traverser.iterator();
        assertNextVerticesAre( iterator, "A", "B" );
        ( (PruningIterator) iterator ).remove();
        ( (PruningIterator) iterator ).prune();
    }

    @Test( expected = IllegalStateException.class )
    public void preOrderPruneThenRemove() {
        final TreeTraverser<String> adjacency = mutableAdjacencyFor( dag );
        final Iterable<String> traverser = Traversals.preOrder( "A", adjacency );
        final Iterator<String> iterator = traverser.iterator();
        assertNextVerticesAre( iterator, "A", "B" );
        ( (PruningIterator) iterator ).prune();
        ( (PruningIterator) iterator ).remove();
    }

    // postOrder( root, Traverser )

    @Test
    public void postOrderEmpty() {
        final Iterable<String> traverser = Traversals.postOrder( "A", adjacencyFor( empty ) );
        assertTraversalContains( traverser, "A" );
    }

    @Test
    public void postOrderSingleEdge() {
        final Iterable<String> traverser = Traversals.postOrder( "A", adjacencyFor( singleEdge ) );
        assertTraversalContains( traverser, "B", "A" );
    }

    @Test
    public void postOrderTree() {
        final Iterable<String> traverser = Traversals.postOrder( "A", adjacencyFor( tree ) );
        assertTraversalContains( traverser, "D", "E", "B", "F", "G", "C", "A" );
    }

    @Test
    public void postOrderDag() {
        final Iterable<String> traverser = Traversals.postOrder( "A", adjacencyFor( dag ) );
        assertTraversalContains( traverser, "G", "D", "E", "B", "G", "D", "C", "A" );
    }

    @Test( expected = IllegalStateException.class )
    public void postOrderRemoveBeforeNext() {
        final TreeTraverser<String> adjacency = mutableAdjacencyFor( dag );
        final Iterable<String> traverser = Traversals.postOrder( "A", adjacency );
        final Iterator<String> iterator = traverser.iterator();
        iterator.remove();
    }

    @Test( expected = IllegalStateException.class )
    public void postOrderRemoveRoot() {
        final TreeTraverser<String> adjacency = mutableAdjacencyFor( dag );
        final Iterable<String> traverser = Traversals.postOrder( "A", adjacency );
        final Iterator<String> iterator = traverser.iterator();
        assertNextVerticesAre( iterator, "G", "D", "E", "B", "G", "D", "C", "A" );
        iterator.remove();
    }

    @Test
    public void postOrderRemoveB() {
        final TreeTraverser<String> adjacency = mutableAdjacencyFor( dag );
        final Iterable<String> traverser = Traversals.postOrder( "A", adjacency );
        final Iterator<String> iterator = traverser.iterator();
        assertNextVerticesAre( iterator, "G", "D", "E", "B" );
        iterator.remove();
        assertNextVerticesAre( iterator, "G", "D", "C", "A" );

        // Check that data structure was actually changed
        assertTraversalContains( Traversals.postOrder( "A", adjacency ), "G", "D", "C", "A" );
    }

    @Test
    public void postOrderRemoveD() {
        final TreeTraverser<String> adjacency = mutableAdjacencyFor( dag );
        final Iterable<String> traverser = Traversals.postOrder( "A", adjacency );
        final Iterator<String> iterator = traverser.iterator();
        assertNextVerticesAre( iterator, "G", "D" );
        iterator.remove();
        assertNextVerticesAre( iterator, "E", "B", "G", "D", "C", "A" );

        // Check that data structure was actually changed
        assertTraversalContains( Traversals.postOrder( "A", adjacency ), "E", "B", "G", "D", "C", "A" );
    }

    @Test
    public void postOrderRemoveE() {
        final TreeTraverser<String> adjacency = mutableAdjacencyFor( dag );
        final Iterable<String> traverser = Traversals.postOrder( "A", adjacency );
        final Iterator<String> iterator = traverser.iterator();
        assertNextVerticesAre( iterator, "G", "D", "E" );
        iterator.remove();
        assertNextVerticesAre( iterator, "B", "G", "D", "C", "A" );

        // Check that data structure was actually changed
        assertTraversalContains( Traversals.postOrder( "A", adjacency ), "G", "D", "B", "G", "D", "C", "A" );
    }

    @Test( expected = IllegalStateException.class )
    public void postOrderRemoveTwice() {
        final TreeTraverser<String> adjacency = mutableAdjacencyFor( dag );
        final Iterable<String> traverser = Traversals.postOrder( "A", adjacency );
        final Iterator<String> iterator = traverser.iterator();
        assertNextVerticesAre( iterator, "G", "D", "E", "B" );
        iterator.remove();
        iterator.remove();
    }

    // breadthFirst( root, Traverser )

    @Test
    public void breadthFirstEmpty() {
        final Iterable<String> traverser = Traversals.breadthFirst( "A", adjacencyFor( empty ) );
        assertTraversalContains( traverser,"A" );
    }

    @Test
    public void breadthFirstSingleEdge() {
        final Iterable<String> traverser = Traversals.breadthFirst( "A", adjacencyFor( singleEdge ) );
        assertTraversalContains( traverser, "A", "B" );
    }

    @Test
    public void breadthFirstLoop() {
        final Iterable<String> traverser = Traversals.breadthFirst( "A", adjacencyFor( loop ) );
        assertTraversalBegins( traverser, "A", "A", "A", "A" );
    }

    @Test
    public void breadthFirstCycle() {
        final Iterable<String> traverser = Traversals.breadthFirst( "A", adjacencyFor( cycle ) );
        assertTraversalBegins( traverser, "A", "B", "C", "A", "B" );
    }

    @Test
    public void breadthFirstTree() {
        final Iterable<String> traverser = Traversals.breadthFirst( "A", adjacencyFor( tree ) );
        assertTraversalContains( traverser, "A", "B", "C", "D", "E", "F", "G" );
    }

    private static void assertBreadthFirstFullDag( final Iterable<String> traversal ) {
        assertTraversalContains( traversal, "A", "B", "C", "D", "E", "D", "G", "G" );
    }

    @Test
    public void breadthFirstDag() {
        final Iterable<String> traverser = Traversals.breadthFirst( "A", adjacencyFor( dag ) );
        assertBreadthFirstFullDag( traverser );
    }

    @Test( expected = IllegalStateException.class )
    public void breadthFirstRemoveBeforeNext() {
        final TreeTraverser<String> adjacency = mutableAdjacencyFor( dag );
        final Iterable<String> traverser = Traversals.breadthFirst( "A", adjacency );
        final Iterator<String> iterator = traverser.iterator();
        iterator.remove();
    }

    @Test( expected = IllegalStateException.class )
    public void breadthFirstRemoveRoot() {
        final TreeTraverser<String> adjacency = mutableAdjacencyFor( dag );
        final Iterable<String> traverser = Traversals.breadthFirst( "A", adjacency );
        final Iterator<String> iterator = traverser.iterator();
        assertNextVerticesAre( iterator, "A" );
        iterator.remove();
    }

    @Test
    public void breadthFirstRemoveB() {
        final TreeTraverser<String> adjacency = mutableAdjacencyFor( dag );
        final Iterator<String> iterator = Traversals.breadthFirst( "A", adjacency ).iterator();
        assertNextVerticesAre( iterator, "A", "B" );
        iterator.remove();
        assertNextVerticesAre( iterator, "C", "D", "G" );

        // Check that data structure was actually changed
        assertTraversalContains( Traversals.breadthFirst( "A", adjacency ), "A", "C", "D", "G" );
    }

    @Test
    public void breadthFirstRemoveC() {
        final TreeTraverser<String> adjacency = mutableAdjacencyFor( dag );
        final Iterator<String> iterator = Traversals.breadthFirst( "A", adjacency ).iterator();
        assertNextVerticesAre( iterator, "A", "B", "C" );
        iterator.remove();
        assertNextVerticesAre( iterator, "D", "E", "G" );

        // Check that data structure was actually changed
        assertTraversalContains( Traversals.breadthFirst( "A", adjacency ), "A", "B", "D", "E", "G" );
    }

    @Test
    public void breadthFirstRemoveFirstD() {
        final TreeTraverser<String> adjacency = mutableAdjacencyFor( dag );
        final Iterator<String> iterator = Traversals.breadthFirst( "A", adjacency ).iterator();
        assertNextVerticesAre( iterator, "A", "B", "C", "D" );
        iterator.remove();
        assertNextVerticesAre( iterator, "E", "D", "G" );

        // Check that data structure was actually changed
        assertTraversalContains( Traversals.breadthFirst( "A", adjacency ), "A", "B", "C", "E", "D", "G" );
    }

    @Test
    public void breadthFirstRemoveE() {
        final TreeTraverser<String> adjacency = mutableAdjacencyFor( dag );
        final Iterator<String> iterator = Traversals.breadthFirst( "A", adjacency ).iterator();
        assertNextVerticesAre( iterator, "A", "B", "C", "D", "E" );
        iterator.remove();
        assertNextVerticesAre( iterator, "D", "G", "G" );

        // Check that data structure was actually changed
        assertTraversalContains( Traversals.breadthFirst( "A", adjacency ), "A", "B", "C", "D", "D", "G", "G" );
    }

    @Test
    public void breadthFirstRemoveSecondD() {
        final TreeTraverser<String> adjacency = mutableAdjacencyFor( dag );
        final Iterator<String> iterator = Traversals.breadthFirst( "A", adjacency ).iterator();
        assertNextVerticesAre( iterator, "A", "B", "C", "D", "E", "D" );
        iterator.remove();
        assertNextVerticesAre( iterator, "G" );

        // Check that data structure was actually changed
        assertTraversalContains( Traversals.breadthFirst( "A", adjacency ), "A", "B", "C", "D", "E", "G" );
    }

    @Test
    public void breadthFirstRemoveFirstG() {
        final TreeTraverser<String> adjacency = mutableAdjacencyFor( dag );
        final Iterator<String> iterator = Traversals.breadthFirst( "A", adjacency ).iterator();
        assertNextVerticesAre( iterator, "A", "B", "C", "D", "E", "D", "G" );
        iterator.remove();
        assertNextVerticesAre( iterator, EMPTY_EXPECTED_VERTICES );

        // Check that data structure was actually changed
        assertTraversalContains( Traversals.breadthFirst( "A", adjacency ), "A", "B", "C", "D", "E", "D" );
    }

    @Test
    public void breadthFirstRemoveSecondG() {
        final TreeTraverser<String> adjacency = mutableAdjacencyFor( dag );
        final Iterator<String> iterator = Traversals.breadthFirst( "A", adjacency ).iterator();
        assertNextVerticesAre( iterator, "A", "B", "C", "D", "E", "D", "G", "G" );
        iterator.remove();
        assertNextVerticesAre( iterator, EMPTY_EXPECTED_VERTICES );

        // Check that data structure was actually changed
        assertTraversalContains( Traversals.breadthFirst( "A", adjacency ), "A", "B", "C", "D", "E", "D" );
    }

    @Test( expected = IllegalStateException.class )
    public void breadthFirstRemoveTwice() {
        final TreeTraverser<String> adjacency = mutableAdjacencyFor( dag );
        final Iterator<String> iterator = Traversals.breadthFirst( "A", adjacency ).iterator();
        assertNextVerticesAre( iterator, "A", "B" );
        iterator.remove();
        iterator.remove();
    }

    @Test( expected = IllegalStateException.class )
    public void breadthFirstPruneBeforeNext() {
        final TreeTraverser<String> adjacency = mutableAdjacencyFor( dag );
        final Iterable<String> traverser = Traversals.breadthFirst( "A", adjacency );
        final Iterator<String> iterator = traverser.iterator();
        ( (PruningIterator) iterator ).prune();
    }

    @Test
    public void breadthFirstPruneRoot() {
        final TreeTraverser<String> adjacency = mutableAdjacencyFor( dag );
        final Iterable<String> traverser = Traversals.breadthFirst( "A", adjacency );
        final Iterator<String> iterator = traverser.iterator();
        assertNextVerticesAre( iterator, "A" );
        ( (PruningIterator) iterator ).prune();
        assertNextVerticesAre( iterator, EMPTY_EXPECTED_VERTICES );

        // Check that data structure is unchanged
        assertBreadthFirstFullDag( Traversals.breadthFirst( "A", adjacency ) );
    }

    @Test
    public void breadthFirstPruneB() {
        final TreeTraverser<String> adjacency = mutableAdjacencyFor( dag );
        final Iterator<String> iterator = Traversals.breadthFirst( "A", adjacency ).iterator();
        assertNextVerticesAre( iterator, "A", "B" );
        ( (PruningIterator) iterator ).prune();
        assertNextVerticesAre( iterator, "C", "D", "G" );

        // Check that data structure is unchanged
        assertBreadthFirstFullDag( Traversals.breadthFirst( "A", adjacency ) );
    }

    @Test
    public void breadthFirstPruneC() {
        final TreeTraverser<String> adjacency = mutableAdjacencyFor( dag );
        final Iterator<String> iterator = Traversals.breadthFirst( "A", adjacency ).iterator();
        assertNextVerticesAre( iterator, "A", "B", "C" );
        ( (PruningIterator) iterator ).prune();
        assertNextVerticesAre( iterator, "D", "E", "G" );

        // Check that data structure is unchanged
        assertBreadthFirstFullDag( Traversals.breadthFirst( "A", adjacency ) );
    }

    @Test
    public void breadthFirstPruneFirstD() {
        final TreeTraverser<String> adjacency = mutableAdjacencyFor( dag );
        final Iterator<String> iterator = Traversals.breadthFirst( "A", adjacency ).iterator();
        assertNextVerticesAre( iterator, "A", "B", "C", "D" );
        ( (PruningIterator) iterator ).prune();
        assertNextVerticesAre( iterator, "E", "D", "G" );

        // Check that data structure is unchanged
        assertBreadthFirstFullDag( Traversals.breadthFirst( "A", adjacency ) );
    }

    @Test
    public void breadthFirstPruneE() {
        final TreeTraverser<String> adjacency = mutableAdjacencyFor( dag );
        final Iterator<String> iterator = Traversals.breadthFirst( "A", adjacency ).iterator();
        assertNextVerticesAre( iterator, "A", "B", "C", "D", "E" );
        ( (PruningIterator) iterator ).prune();
        assertNextVerticesAre( iterator, "D", "G", "G" );

        // Check that data structure is unchanged
        assertBreadthFirstFullDag( Traversals.breadthFirst( "A", adjacency ) );
    }

    @Test
    public void breadthFirstPruneSecondD() {
        final TreeTraverser<String> adjacency = mutableAdjacencyFor( dag );
        final Iterator<String> iterator = Traversals.breadthFirst( "A", adjacency ).iterator();
        assertNextVerticesAre( iterator, "A", "B", "C", "D", "E", "D" );
        ( (PruningIterator) iterator ).prune();
        assertNextVerticesAre( iterator, "G" );

        // Check that data structure is unchanged
        assertBreadthFirstFullDag( Traversals.breadthFirst( "A", adjacency ) );
    }

    @Test
    public void breadthFirstPruneFirstG() {
        final TreeTraverser<String> adjacency = mutableAdjacencyFor( dag );
        final Iterator<String> iterator = Traversals.breadthFirst( "A", adjacency ).iterator();
        assertNextVerticesAre( iterator, "A", "B", "C", "D", "E", "D", "G" );
        ( (PruningIterator) iterator ).prune();
        assertNextVerticesAre( iterator, EMPTY_EXPECTED_VERTICES );

        // Check that data structure is unchanged
        assertBreadthFirstFullDag( Traversals.breadthFirst( "A", adjacency ) );
    }

    @Test
    public void breadthFirstPruneSecondG() {
        final TreeTraverser<String> adjacency = mutableAdjacencyFor( dag );
        final Iterator<String> iterator = Traversals.breadthFirst( "A", adjacency ).iterator();
        assertNextVerticesAre( iterator, "A", "B", "C", "D", "E", "D", "G", "G" );
        ( (PruningIterator) iterator ).prune();
        assertNextVerticesAre( iterator, EMPTY_EXPECTED_VERTICES );

        // Check that data structure is unchanged
        assertBreadthFirstFullDag( Traversals.breadthFirst( "A", adjacency ) );
    }

    @Test( expected = IllegalStateException.class )
    public void breadthFirstPruneTwice() {
        final TreeTraverser<String> adjacency = mutableAdjacencyFor( dag );
        final Iterator<String> iterator = Traversals.breadthFirst( "A", adjacency ).iterator();
        assertNextVerticesAre( iterator, "A", "B" );
        ( (PruningIterator) iterator ).prune();
        ( (PruningIterator) iterator ).prune();
    }

    @Test( expected = IllegalStateException.class )
    public void breadthFirstRemoveThenPrune() {
        final TreeTraverser<String> adjacency = mutableAdjacencyFor( dag );
        final Iterable<String> traverser = Traversals.breadthFirst( "A", adjacency );
        final Iterator<String> iterator = traverser.iterator();
        assertNextVerticesAre( iterator, "A", "B" );
        ( (PruningIterator) iterator ).remove();
        ( (PruningIterator) iterator ).prune();
    }

    @Test( expected = IllegalStateException.class )
    public void breadthFirstPruneThenRemove() {
        final TreeTraverser<String> adjacency = mutableAdjacencyFor( dag );
        final Iterable<String> traverser = Traversals.breadthFirst( "A", adjacency );
        final Iterator<String> iterator = traverser.iterator();
        assertNextVerticesAre( iterator, "A", "B" );
        ( (PruningIterator) iterator ).prune();
        ( (PruningIterator) iterator ).remove();
    }

    // leaves( Traverser )

    @Test
    public void leavesEmpty() {
        final Iterable<String> traverser = Traversals.leaves( "A", adjacencyFor( empty ) );
        assertTraversalContains( traverser, "A" );
    }

    @Test
    public void leavesSingleEdge() {
        final Iterable<String> traverser = Traversals.leaves( "A", adjacencyFor( singleEdge ) );
        assertTraversalContains( traverser, "B" );
    }

    @Test
    public void leavesTree() {
        final Iterable<String> traverser = Traversals.leaves( "A", adjacencyFor( tree ) );
        assertTraversalContains( traverser, "D", "E", "F", "G" );
    }

    @Test
    public void leavesDag() {
        final Iterable<String> traverser = Traversals.leaves( "A", adjacencyFor( dag ) );
        assertTraversalContains( traverser, "G", "E", "G" );
    }

    @Test( expected = IllegalStateException.class )
    public void leavesRemoveBeforeNext() {
        final TreeTraverser<String> adjacency = mutableAdjacencyFor( dag );
        final Iterable<String> traverser = Traversals.leaves( "A", adjacency );
        final Iterator<String> iterator = traverser.iterator();
        iterator.remove();
    }

    @Test
    public void leavesRemoveFirstG() {
        final TreeTraverser<String> adjacency = mutableAdjacencyFor( dag );
        final Iterable<String> traverser = Traversals.leaves( "A", adjacency );
        final Iterator<String> iterator = traverser.iterator();
        assertNextVerticesAre( iterator, "G" );
        iterator.remove();
        assertNextVerticesAre( iterator, "E", "D" );

        // Check that data structure was actually changed
        assertTraversalContains( Traversals.leaves( "A", adjacency ), "D", "E", "D" );
    }

    @Test
    public void leavesRemoveSecondG() {
        final TreeTraverser<String> adjacency = mutableAdjacencyFor( dag );
        final Iterable<String> traverser = Traversals.leaves( "A", adjacency );
        final Iterator<String> iterator = traverser.iterator();
        assertNextVerticesAre( iterator, "G", "E", "G" );
        iterator.remove();
        assertIteratorEmpty( iterator );

        // Check that data structure was actually changed
        assertTraversalContains( Traversals.leaves( "A", adjacency ), "D", "E", "D" );
    }

    @Test
    public void leavesRemoveE() {
        final TreeTraverser<String> adjacency = mutableAdjacencyFor( dag );
        final Iterable<String> traverser = Traversals.leaves( "A", adjacency );
        final Iterator<String> iterator = traverser.iterator();
        assertNextVerticesAre( iterator, "G", "E" );
        iterator.remove();
        assertNextVerticesAre( iterator, "G" );

        // Check that data structure was actually changed
        assertTraversalContains( Traversals.leaves( "A", adjacency ), "G", "G" );
    }

    @Test( expected = IllegalStateException.class )
    public void leavesRemoveTwice() {
        final TreeTraverser<String> adjacency = mutableAdjacencyFor( dag );
        final Iterable<String> traverser = Traversals.leaves( "A", adjacency );
        final Iterator<String> iterator = traverser.iterator();
        assertNextVerticesAre( iterator, "G", "E" );
        iterator.remove();
        iterator.remove();
    }

    @Test( expected = IllegalStateException.class )
    public void leavesNextAfterLastThenRemove() {
        // This use case is slightly different, because it exercises different logic in the traverser.
        final TreeTraverser<String> adjacency = mutableAdjacencyFor( dag );
        final Iterable<String> traverser = Traversals.leaves( "A", adjacency );
        final Iterator<String> iterator = traverser.iterator();
        assertNextVerticesAre( iterator, "G", "E", "G" );
        try {
            iterator.next();
        } catch( NoSuchElementException ignored ) {
            // expected
        }
        iterator.remove();
    }

    // leafElements()
    // elementPath()

    // Note that an empty map/iterable/array *is* a leaf.

    private static void assertPathWalksAre( final Iterable<PersistentList<Step<Object, String>>> traversal,
                                            final Object[][] expectedElements ) {
        final Iterator<PersistentList<Step<Object, String>>> iterator = traversal.iterator();
        for( final Object[] element : expectedElements ) {
            assertThat( iterator.hasNext(), is( true ) );
            final PersistentList<Step<Object, String>> walk = iterator.next();
            assertThat( Traversals.elementPath( walk ), is( element[ 0 ] ) );
            assertThat( walk.first().getTo(), is( element[ 1 ] ) );
        }
        assertIteratorEmpty( iterator );
    }

    @Test
    public void leafElementsNull() {
        final Object root = null;
        assertPathWalksAre( Traversals.leafElements( root ), new Object[][] { { "", root } } );
    }

    @Test
    public void leafElementsString() {
        final Object root = "abc";
        assertPathWalksAre( Traversals.leafElements( root ), new Object[][] { { "", root } } );
    }

    @Test
    public void leafElementsEmptyMap() {
        final Object root = Collections.emptyMap();
        assertPathWalksAre( Traversals.leafElements( root ), new Object[][] { { "", root } } );
    }

    @Test
    public void leafElementsEmptyList() {
        final Object root = Collections.emptyList();
        assertPathWalksAre( Traversals.leafElements( root ), new Object[][] { { "", root } } );
    }

    private static final int[] EMPTY_INT_ARRAY = new int[ 0 ];

    @Test
    public void leafElementsEmptyArray() {
        final Object root = EMPTY_INT_ARRAY;
        assertPathWalksAre( Traversals.leafElements( root ), new Object[][] { { "", root } } );
    }

    @Test
    public void leafElementsSimpleMap() {
        final Object root = ImmutableMap.of( "name", "Alice", "age", 37, "deceased", false );
        assertPathWalksAre( Traversals.leafElements( root ),
                            new Object[][] { { "name", "Alice" }, { "age", 37 }, { "deceased", false } } );
    }

    @Test
    public void leafElementsSimpleList() {
        final Object root = Arrays.<Object>asList( "Alice", 37, false );
        assertPathWalksAre( Traversals.leafElements( root ),
                            new Object[][] { { "[0]", "Alice" }, { "[1]", 37 }, { "[2]", false } } );
    }

    @Test
    public void leafElementsSimpleArray() {
        final Object root = new int[] { 42, 99, 256 };
        assertPathWalksAre( Traversals.leafElements( root ),
                            new Object[][] { { "[0]", 42 }, { "[1]", 99 }, { "[2]", 256 } } );
    }

    @Test
    public void leafElementsMap() {
        final Map<String, Object> root = ImmutableMap.<String, Object>of( "names", Arrays.asList( "Alice", "Becky", "Carol" ),
                                                                          "ages", Arrays.asList( 37, 42, 32 ) );
        assertPathWalksAre( Traversals.leafElements( root ),
                            new Object[][] { { "names[0]", "Alice" },
                                             { "names[1]", "Becky" },
                                             { "names[2]", "Carol" },
                                             { "ages[0]", 37 },
                                             { "ages[1]", 42 },
                                             { "ages[2]", 32 } } );
    }

    @SuppressWarnings( "unchecked" )
    @Test
    public void leafElementsList() {
        final List<Object> root = Arrays.<Object>asList( ImmutableMap.of( "name", "Alice", "age", 37 ),
                                                         ImmutableMap.of( "name", "Becky", "age", 42 ) );
        assertPathWalksAre( Traversals.leafElements( root ),
                            new Object[][] { { "[0].name", "Alice" },
                                             { "[0].age", 37 },
                                             { "[1].name", "Becky" },
                                             { "[1].age", 42 } } );
    }

    @Test
    public void leafElementsArray() {
        final Object[] root = new Object[] { ImmutableMap.of( "name", "Alice", "age", 37 ),
                                       ImmutableMap.of( "name", "Becky", "age", 42 ) };
        assertPathWalksAre( Traversals.leafElements( root ),
                            new Object[][] { { "[0].name", "Alice" },
                                             { "[0].age", 37 },
                                             { "[1].name", "Becky" },
                                             { "[1].age", 42 } } );
    }

    // Test a more complex graph

    @SuppressWarnings( "unchecked" )
    @Test
    public void leafElementsComplex() {
        final Map<?, ?> map = ImmutableMap.builder()
                .put( "string", "A String" )
                .put( "integer", 42 )
                .put( "list", Arrays.asList( "zero", "one", "two", "three" ) )
                .put( "array", new Object[] { "four", "five", "six" } )
                .put( "boolean.array", new boolean[] { false, true, true, false, true } )
                .put( "map",
                      ImmutableMap.builder()
                              .put( "foo[abc]bar", "Another String" )
                              .put( "people",
                                    Arrays.asList( ImmutableMap.of( "name", "Alice", "age", 37 ),
                                                   ImmutableMap.of( "name", "Bob", "age", 55 ),
                                                   ImmutableMap.of( "name", "Carol", "age", 23 ),
                                                   ImmutableMap.of( "name", "Dave", "age", 27 ) ) )
                              .put( "owner", ImmutableMap.of( "name", "Elise", "age", 43 ) )
                              .build() )
                .build();

        assertPathWalksAre( Traversals.leafElements( map ),
                            new Object[][] { { "string", "A String" },
                                             { "integer", 42 },
                                             { "list[0]", "zero" },
                                             { "list[1]", "one" },
                                             { "list[2]", "two" },
                                             { "list[3]", "three" },
                                             { "array[0]", "four" },
                                             { "array[1]", "five" },
                                             { "array[2]", "six" },
                                             { "boolean\\.array[0]", false },
                                             { "boolean\\.array[1]", true },
                                             { "boolean\\.array[2]", true },
                                             { "boolean\\.array[3]", false },
                                             { "boolean\\.array[4]", true },
                                             { "map.foo\\[abc\\]bar", "Another String" },
                                             { "map.people[0].name", "Alice" },
                                             { "map.people[0].age", 37 },
                                             { "map.people[1].name", "Bob" },
                                             { "map.people[1].age", 55 },
                                             { "map.people[2].name", "Carol" },
                                             { "map.people[2].age", 23 },
                                             { "map.people[3].name", "Dave" },
                                             { "map.people[3].age", 27 },
                                             { "map.owner.name", "Elise" },
                                             { "map.owner.age", 43 } } );
    }
}
