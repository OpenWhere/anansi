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
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

import static com.github.rconner.anansi.WalkTest.assertWalkContains;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public final class TraversersTest {

    // We'll be using Multimaps to define adjacency for test cases.
    // Lacking a key (vertex) means that there are no walks leaving that vertex. There is no way, using this
    // representation, to denote whether or not a vertex is present. Any vertex is present.

    private final Multimap<String, Walk<String, String>> empty = ImmutableMultimap.of();

    private final Multimap<String, Walk<String, String>> singleEdge = ImmutableMultimap.of(
            "A", Walk.single( "A", "B", "A->B" ) );

    // Warning! Do not perform a post-order traversal on this graph.
    private final Multimap<String, Walk<String, String>> loop = ImmutableMultimap.of(
            "A", Walk.single( "A", "A", "A->A" ) );

    // Warning! Do not perform a post-order traversal on this graph.
    private final Multimap<String, Walk<String, String>> cycle = ImmutableListMultimap.<String, Walk<String, String>>builder()
            .put( "A", Walk.single( "A", "B", "A->B" ) )
            .put( "B", Walk.single( "B", "C", "B->C" ) )
            .put( "C", Walk.single( "C", "A", "C->A" ) )
            .build();

    private final Multimap<String, Walk<String, String>> tree = ImmutableListMultimap.<String, Walk<String, String>>builder()
            .put( "A", Walk.single( "A", "B", "A->B" ) )
            .put( "A", Walk.single( "A", "C", "A->C" ) )
            .put( "B", Walk.single( "B", "D", "B->D" ) )
            .put( "B", Walk.single( "B", "E", "B->E" ) )
            .put( "C", Walk.single( "C", "F", "C->F" ) )
            .put( "C", Walk.single( "C", "G", "C->G" ) )
            .build();

    // Has two paths from A to D.
    private final Multimap<String, Walk<String, String>> dag = ImmutableListMultimap.<String, Walk<String, String>>builder()
            .put( "A", Walk.single( "A", "B", "A->B" ) )
            .put( "A", Walk.single( "A", "C", "A->C" ) )
            .put( "B", Walk.single( "B", "D", "B->D" ) )
            .put( "B", Walk.single( "B", "E", "B->E" ) )
            .put( "C", Walk.single( "C", "D", "C->D" ) )
            .put( "D", Walk.single( "D", "G", "D->G" ) )
            .build();

    private static Traverser<String, String> adjacencyFor( final Multimap<String, Walk<String, String>> graph ) {
        return new Traverser<String, String>() {
            @Override
            public Iterable<Walk<String, String>> apply( final String input ) {
                return graph.get( input );
            }
        };
    }


    static <V, E> void assertTraversalContains( final Iterable<Walk<V, E>> traversal, final Object[][] actualWalks ) {
        Iterator<Walk<V, E>> iterator = traversal.iterator();
        for( Object[] actualWalk : actualWalks ) {
            assertThat( iterator.hasNext(), is( true ) );
            assertWalkContains( iterator.next(), actualWalk );
        }
        assertThat( iterator.hasNext(), is( false ) );
        try {
            iterator.next();
            fail( "Should have thrown a NoSuchElementException." );
        } catch( NoSuchElementException ignored ) {
            // success
        }
    }

    static <V, E> void assertTraversalBegins( final Iterable<Walk<V, E>> traversal, final Object[][] actualWalks ) {
        Iterator<Walk<V, E>> iterator = traversal.iterator();
        for( Object[] actualWalk : actualWalks ) {
            assertThat( iterator.hasNext(), is( true ) );
            assertWalkContains( iterator.next(), actualWalk );
        }
        assertThat( iterator.hasNext(), is( true ) );
    }

    // empty()

    // TODO

    // preOrder( Traverser )

    @Test
    public void preOrderEmpty() {
        final Traverser<String, String> traverser = Traversers.preOrder( adjacencyFor( empty ) );
        assertTraversalContains( traverser.apply( "A" ), new Object[][] { { "A" } } );
    }

    @Test
    public void preOrderSingleton() {
        final Traverser<String, String> traverser = Traversers.preOrder( adjacencyFor( singleEdge ) );
        assertTraversalContains( traverser.apply( "A" ), new Object[][] { { "A" }, { "A", "A->B", "B" } } );
    }

    @Test
    public void preOrderLoop() {
        final Traverser<String, String> traverser = Traversers.preOrder( adjacencyFor( loop ) );
        assertTraversalBegins(
                traverser.apply( "A" ), new Object[][] {
                { "A" },
                { "A", "A->A", "A" },
                { "A", "A->A", "A", "A->A", "A" },
                { "A", "A->A", "A", "A->A", "A", "A->A", "A" } } );
    }

    @Test
    public void preOrderCycle() {
        final Traverser<String, String> traverser = Traversers.preOrder( adjacencyFor( cycle ) );
        assertTraversalBegins(
                traverser.apply( "A" ), new Object[][] {
                { "A" },
                { "A", "A->B", "B" },
                { "A", "A->B", "B", "B->C", "C" },
                { "A", "A->B", "B", "B->C", "C", "C->A", "A" },
                { "A", "A->B", "B", "B->C", "C", "C->A", "A", "A->B", "B" } } );
    }

    @Test
    public void preOrderTree() {
        final Traverser<String, String> traverser = Traversers.preOrder( adjacencyFor( tree ) );
        assertTraversalContains(
                traverser.apply( "A" ), new Object[][] {
                { "A" },
                { "A", "A->B", "B" },
                { "A", "A->B", "B", "B->D", "D" },
                { "A", "A->B", "B", "B->E", "E" },
                { "A", "A->C", "C" },
                { "A", "A->C", "C", "C->F", "F" },
                { "A", "A->C", "C", "C->G", "G" } } );
    }

    @Test
    public void preOrderDag() {
        final Traverser<String, String> traverser = Traversers.preOrder( adjacencyFor( dag ) );
        assertTraversalContains(
                traverser.apply( "A" ), new Object[][] {
                { "A" },
                { "A", "A->B", "B" },
                { "A", "A->B", "B", "B->D", "D" },
                { "A", "A->B", "B", "B->D", "D", "D->G", "G" },
                { "A", "A->B", "B", "B->E", "E" },
                { "A", "A->C", "C" },
                { "A", "A->C", "C", "C->D", "D" },
                { "A", "A->C", "C", "C->D", "D", "D->G", "G" } } );
    }

    // FIXME: Test pre-order prune()


    // postOrder( Traverser )

    @Test
    public void postOrderEmpty() {
        final Traverser<String, String> traverser = Traversers.postOrder( adjacencyFor( empty ) );
        assertTraversalContains( traverser.apply( "A" ), new Object[][] { { "A" } } );
    }

    @Test
    public void postOrderSingleton() {
        final Traverser<String, String> traverser = Traversers.postOrder( adjacencyFor( singleEdge ) );
        assertTraversalContains( traverser.apply( "A" ), new Object[][] { { "A", "A->B", "B" }, { "A" } } );
    }

    @Test
    public void postOrderTree() {
        final Traverser<String, String> traverser = Traversers.postOrder( adjacencyFor( tree ) );
        assertTraversalContains(
                traverser.apply( "A" ), new Object[][] {
                { "A", "A->B", "B", "B->D", "D" },
                { "A", "A->B", "B", "B->E", "E" },
                { "A", "A->B", "B" },
                { "A", "A->C", "C", "C->F", "F" },
                { "A", "A->C", "C", "C->G", "G" },
                { "A", "A->C", "C" },
                { "A" } } );
    }

    @Test
    public void postOrderDag() {
        final Traverser<String, String> traverser = Traversers.postOrder( adjacencyFor( dag ) );
        assertTraversalContains(
                traverser.apply( "A" ), new Object[][] {
                { "A", "A->B", "B", "B->D", "D", "D->G", "G" },
                { "A", "A->B", "B", "B->D", "D" },
                { "A", "A->B", "B", "B->E", "E" },
                { "A", "A->B", "B" },
                { "A", "A->C", "C", "C->D", "D", "D->G", "G" },
                { "A", "A->C", "C", "C->D", "D" },
                { "A", "A->C", "C" },
                { "A" } } );
    }


    // breadthFirst( Traverser )

    @Test
    public void breadthFirstEmpty() {
        final Traverser<String, String> traverser = Traversers.breadthFirst( adjacencyFor( empty ) );
        assertTraversalContains( traverser.apply( "A" ), new Object[][] { { "A" } } );
    }

    @Test
    public void breadthFirstSingleton() {
        final Traverser<String, String> traverser = Traversers.breadthFirst( adjacencyFor( singleEdge ) );
        assertTraversalContains( traverser.apply( "A" ), new Object[][] { { "A" }, { "A", "A->B", "B" } } );
    }

    @Test
    public void breadthFirstLoop() {
        final Traverser<String, String> traverser = Traversers.breadthFirst( adjacencyFor( loop ) );
        assertTraversalBegins(
                traverser.apply( "A" ), new Object[][] {
                { "A" },
                { "A", "A->A", "A" },
                { "A", "A->A", "A", "A->A", "A" },
                { "A", "A->A", "A", "A->A", "A", "A->A", "A" } } );
    }

    @Test
    public void breadthFirstCycle() {
        final Traverser<String, String> traverser = Traversers.breadthFirst( adjacencyFor( cycle ) );
        assertTraversalBegins(
                traverser.apply( "A" ), new Object[][] {
                { "A" },
                { "A", "A->B", "B" },
                { "A", "A->B", "B", "B->C", "C" },
                { "A", "A->B", "B", "B->C", "C", "C->A", "A" },
                { "A", "A->B", "B", "B->C", "C", "C->A", "A", "A->B", "B" } } );
    }

    @Test
    public void breadthFirstTree() {
        final Traverser<String, String> traverser = Traversers.breadthFirst( adjacencyFor( tree ) );
        assertTraversalContains(
                traverser.apply( "A" ), new Object[][] {
                { "A" },
                { "A", "A->B", "B" },
                { "A", "A->C", "C" },
                { "A", "A->B", "B", "B->D", "D" },
                { "A", "A->B", "B", "B->E", "E" },
                { "A", "A->C", "C", "C->F", "F" },
                { "A", "A->C", "C", "C->G", "G" } } );
    }

    @Test
    public void breadthFirstDag() {
        final Traverser<String, String> traverser = Traversers.breadthFirst( adjacencyFor( dag ) );
        assertTraversalContains(
                traverser.apply( "A" ), new Object[][] {
                { "A" },
                { "A", "A->B", "B" },
                { "A", "A->C", "C" },
                { "A", "A->B", "B", "B->D", "D" },
                { "A", "A->B", "B", "B->E", "E" },
                { "A", "A->C", "C", "C->D", "D" },
                { "A", "A->B", "B", "B->D", "D", "D->G", "G" },
                { "A", "A->C", "C", "C->D", "D", "D->G", "G" } } );
    }

    // FIXME: Test breadth-first prune()


    // leaves( Traverser )

    @Test
    public void leavesEmpty() {
        final Traverser<String, String> traverser = Traversers.leaves( adjacencyFor( empty ) );
        assertTraversalContains( traverser.apply( "A" ), new Object[][] { { "A" } } );
    }

    @Test
    public void leavesSingleton() {
        final Traverser<String, String> traverser = Traversers.leaves( adjacencyFor( singleEdge ) );
        assertTraversalContains( traverser.apply( "A" ), new Object[][] { { "A", "A->B", "B" } } );
    }

    @Test
    public void leavesTree() {
        final Traverser<String, String> traverser = Traversers.leaves( adjacencyFor( tree ) );
        assertTraversalContains(
                traverser.apply( "A" ), new Object[][] {
                { "A", "A->B", "B", "B->D", "D" },
                { "A", "A->B", "B", "B->E", "E" },
                { "A", "A->C", "C", "C->F", "F" },
                { "A", "A->C", "C", "C->G", "G" } } );
    }

    @Test
    public void leavesDag() {
        final Traverser<String, String> traverser = Traversers.leaves( adjacencyFor( dag ) );
        assertTraversalContains(
                traverser.apply( "A" ), new Object[][] {
                { "A", "A->B", "B", "B->D", "D", "D->G", "G" },
                { "A", "A->B", "B", "B->E", "E" },
                { "A", "A->C", "C", "C->D", "D", "D->G", "G" } } );
    }


    // These all return something precisely because an empty map/iterable/array *is* a leaf.

    // TODO
    // elements()


    // leafElements()
    // elementPath()

    @Test
    public void elementsEmptyMap() {
        Object root = Collections.emptyMap();
        Iterator<Walk<Object, String>> iterator = Traversers.leafElements().apply( root ).iterator();
        Walk<Object, String> walk = iterator.next();
        assertThat( walk.getTo(), is( ( Object ) root ) );
        assertThat( Traversers.elementPath( walk ), is( "" ) );
        assertThat( iterator.hasNext(), is( false ) );
    }

    @Test
    public void elementsEmptyList() {
        Object root = Collections.emptyList();
        Iterator<Walk<Object, String>> iterator = Traversers.leafElements().apply( root ).iterator();
        Walk<Object, String> walk = iterator.next();
        assertThat( walk.getTo(), is( ( Object ) root ) );
        assertThat( Traversers.elementPath( walk ), is( "" ) );
        assertThat( iterator.hasNext(), is( false ) );
    }

    @Test
    public void elementsEmptyArray() {
        Object root = new int[ 0 ];
        Iterator<Walk<Object, String>> iterator = Traversers.leafElements().apply( root ).iterator();
        Walk<Object, String> walk = iterator.next();
        assertThat( walk.getTo(), is( root ) );
        assertThat( Traversers.elementPath( walk ), is( "" ) );
        assertThat( iterator.hasNext(), is( false ) );
    }

    static void assertElementsContains(
            final Iterable<Walk<Object, String>> traversal, final Object[][] actualElements ) {
        Iterator<Walk<Object, String>> iterator = traversal.iterator();
        for( Object[] element : actualElements ) {
            assertThat( iterator.hasNext(), is( true ) );
            final Walk<Object, String> walk = iterator.next();
            assertThat( Traversers.elementPath( walk ), is( element[ 0 ] ) );
            assertThat( walk.getTo(), is( element[ 1 ] ) );
        }
        assertThat( iterator.hasNext(), is( false ) );
        try {
            iterator.next();
            fail( "Should have thrown a NoSuchElementException." );
        } catch( NoSuchElementException ignored ) {
            // success
        }
    }

    @Test
    public void nestedElements() {
        final Map<?, ?> map = ImmutableMap.builder()
                .put( "string", "A String" )
                .put( "integer", 42 )
                .put( "list", Arrays.asList( "zero", "one", "two", "three" ) )
                .put( "array", new Object[] { "four", "five", "six" } )
                .put( "boolean.array", new boolean[] { false, true, true, false, true } )
                .put(
                        "map", ImmutableMap.builder()
                        .put( "foo[abc]bar", "Another String" )
                        .put(
                                "people", Arrays.asList(
                                ImmutableMap.of( "name", "Alice", "age", 37 ),
                                ImmutableMap.of( "name", "Bob", "age", 55 ),
                                ImmutableMap.of( "name", "Carol", "age", 23 ),
                                ImmutableMap.of( "name", "Dave", "age", 27 ) ) )
                        .put( "owner", ImmutableMap.of( "name", "Elise", "age", 43 ) )
                        .build() )
                .build();

        assertElementsContains(
                Traversers.leafElements().apply( map ), new Object[][] {
                { "string", "A String" },
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
