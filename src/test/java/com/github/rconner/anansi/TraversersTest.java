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

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import org.junit.Test;

import java.util.Iterator;

import static com.github.rconner.anansi.WalkTest.assertWalkContains;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class TraversersTest {

    // We'll be using Multimaps to define adjacency for test cases.
    // Lacking a key (vertex) means that there are no walks leaving that vertex. There is no way, using this
    // representation, to denote whether or not a vertex is present. Any vertex is present.

    private final Multimap<String, Walk<String, String>> emptyGraph = ImmutableMultimap.of();

    private final Multimap<String, Walk<String, String>> singletomGraph = ImmutableMultimap.of( "A", Walk.single( "A", "B", "A->B" ) );

    // Warning! Do not perform a post-order traversal on this graph.
    private final Multimap<String, Walk<String, String>> loop = ImmutableMultimap.of( "A", Walk.single( "A", "A", "A->A" ) );

    // Warning! Do not perform a post-order traversal on this graph.
    private final Multimap<String, Walk<String, String>> cycle = ImmutableListMultimap.<String, Walk<String, String>>builder().put( "A", Walk.single( "A", "B", "A->B" ) ).put( "B", Walk.single( "B", "C", "B->C" ) ).put( "C", Walk.single( "C", "A", "C->A" ) ).build();

    private final Multimap<String, Walk<String, String>> tree = ImmutableListMultimap.<String, Walk<String, String>>builder().put( "A", Walk.single( "A", "B", "A->B" ) ).put( "A", Walk.single( "A", "C", "A->C" ) ).put( "B", Walk.single( "B", "D", "B->D" ) ).put( "B", Walk.single( "B", "E", "B->E" ) ).put( "C", Walk.single( "C", "F", "C->F" ) ).put( "C", Walk.single( "C", "G", "C->G" ) ).build();

    // Has two paths from A to D.
    private final Multimap<String, Walk<String, String>> dag = ImmutableListMultimap.<String, Walk<String, String>>builder().put( "A", Walk.single( "A", "B", "A->B" ) ).put( "A", Walk.single( "A", "C", "A->C" ) ).put( "B", Walk.single( "B", "D", "B->D" ) ).put( "B", Walk.single( "B", "E", "B->E" ) ).put( "C", Walk.single( "C", "D", "C->D" ) ).put( "D", Walk.single( "D", "G", "D->G" ) ).build();

    private static Traverser<String, String> adjacencyFor( final Multimap<String, Walk<String, String>> graph ) {
        return new Traverser<String, String>() {
            @Override
            public Iterable<Walk<String, String>> apply( final String input ) {
                return graph.get( input );
            }
        };
    }


    @Test
    public void preOrderEmpty() {
        final Traverser<String, String> traverser = Traversers.preOrder( adjacencyFor( emptyGraph ) );
        final Iterator<Walk<String, String>> iterator = traverser.apply( "A" ).iterator();
        assertThat( iterator.hasNext(), is( true ) );
        assertWalkContains( iterator.next(), "A" );
        assertThat( iterator.hasNext(), is( false ) );
    }

    @Test
    public void preOrderSingleton() {
        final Traverser<String, String> traverser = Traversers.preOrder( adjacencyFor( singletomGraph ) );
        final Iterator<Walk<String, String>> iterator = traverser.apply( "A" ).iterator();
        assertThat( iterator.hasNext(), is( true ) );
        assertWalkContains( iterator.next(), "A" );
        assertThat( iterator.hasNext(), is( true ) );
        assertWalkContains( iterator.next(), "A", "A->B", "B" );
        assertThat( iterator.hasNext(), is( false ) );
    }

    @Test
    public void preOrderLoop() {
        final Traverser<String, String> traverser = Traversers.preOrder( adjacencyFor( loop ) );
        final Iterator<Walk<String, String>> iterator = traverser.apply( "A" ).iterator();
        assertThat( iterator.hasNext(), is( true ) );
        assertWalkContains( iterator.next(), "A" );
        assertThat( iterator.hasNext(), is( true ) );
        assertWalkContains( iterator.next(), "A", "A->A", "A" );
        assertThat( iterator.hasNext(), is( true ) );
        assertWalkContains( iterator.next(), "A", "A->A", "A", "A->A", "A" );
        assertThat( iterator.hasNext(), is( true ) );
        assertWalkContains( iterator.next(), "A", "A->A", "A", "A->A", "A", "A->A", "A" );
        assertThat( iterator.hasNext(), is( true ) );
    }

    @Test
    public void preOrderCycle() {
        final Traverser<String, String> traverser = Traversers.preOrder( adjacencyFor( cycle ) );
        final Iterator<Walk<String, String>> iterator = traverser.apply( "A" ).iterator();
        assertThat( iterator.hasNext(), is( true ) );
        assertWalkContains( iterator.next(), "A" );
        assertThat( iterator.hasNext(), is( true ) );
        assertWalkContains( iterator.next(), "A", "A->B", "B" );
        assertThat( iterator.hasNext(), is( true ) );
        assertWalkContains( iterator.next(), "A", "A->B", "B", "B->C", "C" );
        assertThat( iterator.hasNext(), is( true ) );
        assertWalkContains( iterator.next(), "A", "A->B", "B", "B->C", "C", "C->A", "A" );
        assertThat( iterator.hasNext(), is( true ) );
        assertWalkContains( iterator.next(), "A", "A->B", "B", "B->C", "C", "C->A", "A", "A->B", "B" );
        assertThat( iterator.hasNext(), is( true ) );
    }

    @Test
    public void preOrderTree() {
        final Traverser<String, String> traverser = Traversers.preOrder( adjacencyFor( tree ) );
        final Iterator<Walk<String, String>> iterator = traverser.apply( "A" ).iterator();
        assertThat( iterator.hasNext(), is( true ) );
        assertWalkContains( iterator.next(), "A" );
        assertThat( iterator.hasNext(), is( true ) );
        assertWalkContains( iterator.next(), "A", "A->B", "B" );
        assertThat( iterator.hasNext(), is( true ) );
        assertWalkContains( iterator.next(), "A", "A->B", "B", "B->D", "D" );
        assertThat( iterator.hasNext(), is( true ) );
        assertWalkContains( iterator.next(), "A", "A->B", "B", "B->E", "E" );
        assertThat( iterator.hasNext(), is( true ) );
        assertWalkContains( iterator.next(), "A", "A->C", "C" );
        assertThat( iterator.hasNext(), is( true ) );
        assertWalkContains( iterator.next(), "A", "A->C", "C", "C->F", "F" );
        assertThat( iterator.hasNext(), is( true ) );
        assertWalkContains( iterator.next(), "A", "A->C", "C", "C->G", "G" );
        assertThat( iterator.hasNext(), is( false ) );
    }

    @Test
    public void preOrderDag() {
        final Traverser<String, String> traverser = Traversers.preOrder( adjacencyFor( dag ) );
        final Iterator<Walk<String, String>> iterator = traverser.apply( "A" ).iterator();
        assertThat( iterator.hasNext(), is( true ) );
        assertWalkContains( iterator.next(), "A" );
        assertThat( iterator.hasNext(), is( true ) );
        assertWalkContains( iterator.next(), "A", "A->B", "B" );
        assertThat( iterator.hasNext(), is( true ) );
        assertWalkContains( iterator.next(), "A", "A->B", "B", "B->D", "D" );
        assertThat( iterator.hasNext(), is( true ) );
        assertWalkContains( iterator.next(), "A", "A->B", "B", "B->D", "D", "D->G", "G" );
        assertThat( iterator.hasNext(), is( true ) );
        assertWalkContains( iterator.next(), "A", "A->B", "B", "B->E", "E" );
        assertThat( iterator.hasNext(), is( true ) );
        assertWalkContains( iterator.next(), "A", "A->C", "C" );
        assertThat( iterator.hasNext(), is( true ) );
        assertWalkContains( iterator.next(), "A", "A->C", "C", "C->D", "D" );
        assertThat( iterator.hasNext(), is( true ) );
        assertWalkContains( iterator.next(), "A", "A->C", "C", "C->D", "D", "D->G", "G" );
        assertThat( iterator.hasNext(), is( false ) );
    }

    // FIXME: Test pre-order prune()


    @Test
    public void postOrderEmpty() {
        final Traverser<String, String> traverser = Traversers.postOrder( adjacencyFor( emptyGraph ) );
        final Iterator<Walk<String, String>> iterator = traverser.apply( "A" ).iterator();
        assertThat( iterator.hasNext(), is( true ) );
        assertWalkContains( iterator.next(), "A" );
        assertThat( iterator.hasNext(), is( false ) );
    }

    @Test
    public void postOrderSingleton() {
        final Traverser<String, String> traverser = Traversers.postOrder( adjacencyFor( singletomGraph ) );
        final Iterator<Walk<String, String>> iterator = traverser.apply( "A" ).iterator();
        assertThat( iterator.hasNext(), is( true ) );
        assertWalkContains( iterator.next(), "A", "A->B", "B" );
        assertThat( iterator.hasNext(), is( true ) );
        assertWalkContains( iterator.next(), "A" );
        assertThat( iterator.hasNext(), is( false ) );
    }

    @Test
    public void postOrderTree() {
        final Traverser<String, String> traverser = Traversers.postOrder( adjacencyFor( tree ) );
        final Iterator<Walk<String, String>> iterator = traverser.apply( "A" ).iterator();
        assertThat( iterator.hasNext(), is( true ) );
        assertWalkContains( iterator.next(), "A", "A->B", "B", "B->D", "D" );
        assertThat( iterator.hasNext(), is( true ) );
        assertWalkContains( iterator.next(), "A", "A->B", "B", "B->E", "E" );
        assertThat( iterator.hasNext(), is( true ) );
        assertWalkContains( iterator.next(), "A", "A->B", "B" );
        assertThat( iterator.hasNext(), is( true ) );
        assertWalkContains( iterator.next(), "A", "A->C", "C", "C->F", "F" );
        assertThat( iterator.hasNext(), is( true ) );
        assertWalkContains( iterator.next(), "A", "A->C", "C", "C->G", "G" );
        assertThat( iterator.hasNext(), is( true ) );
        assertWalkContains( iterator.next(), "A", "A->C", "C" );
        assertThat( iterator.hasNext(), is( true ) );
        assertWalkContains( iterator.next(), "A" );
        assertThat( iterator.hasNext(), is( false ) );
    }

    @Test
    public void postOrderDag() {
        final Traverser<String, String> traverser = Traversers.postOrder( adjacencyFor( dag ) );
        final Iterator<Walk<String, String>> iterator = traverser.apply( "A" ).iterator();
        assertThat( iterator.hasNext(), is( true ) );
        assertWalkContains( iterator.next(), "A", "A->B", "B", "B->D", "D", "D->G", "G" );
        assertThat( iterator.hasNext(), is( true ) );
        assertWalkContains( iterator.next(), "A", "A->B", "B", "B->D", "D" );
        assertThat( iterator.hasNext(), is( true ) );
        assertWalkContains( iterator.next(), "A", "A->B", "B", "B->E", "E" );
        assertThat( iterator.hasNext(), is( true ) );
        assertWalkContains( iterator.next(), "A", "A->B", "B" );
        assertThat( iterator.hasNext(), is( true ) );
        assertWalkContains( iterator.next(), "A", "A->C", "C", "C->D", "D", "D->G", "G" );
        assertThat( iterator.hasNext(), is( true ) );
        assertWalkContains( iterator.next(), "A", "A->C", "C", "C->D", "D" );
        assertThat( iterator.hasNext(), is( true ) );
        assertWalkContains( iterator.next(), "A", "A->C", "C" );
        assertThat( iterator.hasNext(), is( true ) );
        assertWalkContains( iterator.next(), "A" );
        assertThat( iterator.hasNext(), is( false ) );
    }


    @Test
    public void leavesEmpty() {
        final Traverser<String, String> traverser = Traversers.leaves( adjacencyFor( emptyGraph ) );
        final Iterator<Walk<String, String>> iterator = traverser.apply( "A" ).iterator();
        assertThat( iterator.hasNext(), is( true ) );
        assertWalkContains( iterator.next(), "A" );
        assertThat( iterator.hasNext(), is( false ) );
    }

    @Test
    public void leavesSingleton() {
        final Traverser<String, String> traverser = Traversers.leaves( adjacencyFor( singletomGraph ) );
        final Iterator<Walk<String, String>> iterator = traverser.apply( "A" ).iterator();
        assertThat( iterator.hasNext(), is( true ) );
        assertWalkContains( iterator.next(), "A", "A->B", "B" );
        assertThat( iterator.hasNext(), is( false ) );
    }

    @Test
    public void leavesTree() {
        final Traverser<String, String> traverser = Traversers.leaves( adjacencyFor( tree ) );
        final Iterator<Walk<String, String>> iterator = traverser.apply( "A" ).iterator();
        assertThat( iterator.hasNext(), is( true ) );
        assertWalkContains( iterator.next(), "A", "A->B", "B", "B->D", "D" );
        assertThat( iterator.hasNext(), is( true ) );
        assertWalkContains( iterator.next(), "A", "A->B", "B", "B->E", "E" );
        assertThat( iterator.hasNext(), is( true ) );
        assertWalkContains( iterator.next(), "A", "A->C", "C", "C->F", "F" );
        assertThat( iterator.hasNext(), is( true ) );
        assertWalkContains( iterator.next(), "A", "A->C", "C", "C->G", "G" );
        assertThat( iterator.hasNext(), is( false ) );
    }

    @Test
    public void leavesDag() {
        final Traverser<String, String> traverser = Traversers.leaves( adjacencyFor( dag ) );
        final Iterator<Walk<String, String>> iterator = traverser.apply( "A" ).iterator();
        assertThat( iterator.hasNext(), is( true ) );
        assertWalkContains( iterator.next(), "A", "A->B", "B", "B->D", "D", "D->G", "G" );
        assertThat( iterator.hasNext(), is( true ) );
        assertWalkContains( iterator.next(), "A", "A->B", "B", "B->E", "E" );
        assertThat( iterator.hasNext(), is( true ) );
        assertWalkContains( iterator.next(), "A", "A->C", "C", "C->D", "D", "D->G", "G" );
        assertThat( iterator.hasNext(), is( false ) );
    }

}
