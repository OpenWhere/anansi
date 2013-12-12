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

import com.google.common.base.Function;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.google.common.collect.TreeTraverser;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.github.rconner.util.IterableTest.assertIteratorEmpty;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class TraversersTest {

    // empty()

    @Test
    public void empty() {
        final TreeTraverser<String> traverser = Traversers.empty();
        assertIteratorEmpty( traverser.children( "A" ).iterator() );
    }

    // elements()

    private static final Object[][] EMPTY_EXPECTED_WALKS = new Object[][] { };

    private static void assertPathWalksAre( final Iterable<Walk<Object, String>> traversal,
                                            final Object[][] expectedElements ) {
        final Iterator<Walk<Object, String>> iterator = traversal.iterator();
        for( final Object[] element : expectedElements ) {
            assertThat( iterator.hasNext(), is( true ) );
            final Walk<Object, String> walk = iterator.next();
            assertThat( Traversals.elementPath( walk ), is( element[ 0 ] ) );
            assertThat( walk.getTo(), is( element[ 1 ] ) );
        }
        assertIteratorEmpty( iterator );
    }

    @Test
    public void elementsNull() {
        final Object root = null;
        assertPathWalksAre( Traversers.elements().children( Walk.<Object, String>empty( root ) ), EMPTY_EXPECTED_WALKS );
    }

    @Test
    public void elementsString() {
        final Object root = "abc";
        assertPathWalksAre( Traversers.elements().children( Walk.<Object, String>empty( root ) ), EMPTY_EXPECTED_WALKS );
    }

    @Test
    public void elementsEmptyMap() {
        final Object root = Collections.emptyMap();
        assertPathWalksAre( Traversers.elements().children( Walk.<Object, String>empty( root ) ), EMPTY_EXPECTED_WALKS );
    }

    @Test
    public void elementsEmptyList() {
        final Object root = Collections.emptyList();
        assertPathWalksAre( Traversers.elements().children( Walk.<Object, String>empty( root ) ), EMPTY_EXPECTED_WALKS );
    }

    private static final int[] EMPTY_INT_ARRAY = new int[ 0 ];

    @Test
    public void elementsEmptyArray() {
        final Object root = EMPTY_INT_ARRAY;
        assertPathWalksAre( Traversers.elements().children( Walk.<Object, String>empty( root ) ), EMPTY_EXPECTED_WALKS );
    }

    @Test
    public void elementsSimpleMap() {
        final Object root = ImmutableMap.of( "name", "Alice", "age", 37, "deceased", false );
        assertPathWalksAre( Traversers.elements().children( Walk.<Object, String>empty( root ) ),
                            new Object[][] { { "name", "Alice" }, { "age", 37 }, { "deceased", false } } );
    }

    @Test
    public void elementsSimpleList() {
        final Object root = Arrays.<Object>asList( "Alice", 37, false );
        assertPathWalksAre( Traversers.elements().children( Walk.<Object, String>empty( root ) ),
                            new Object[][] { { "[0]", "Alice" }, { "[1]", 37 }, { "[2]", false } } );
    }

    @Test
    public void elementsSimpleArray() {
        final Object root = new int[] { 42, 99, 256 };
        assertPathWalksAre( Traversers.elements().children( Walk.<Object, String>empty( root ) ),
                            new Object[][] { { "[0]", 42 }, { "[1]", 99 }, { "[2]", 256 } } );
    }

    @Test
    public void elementsMap() {
        final Map<String, Object> root = ImmutableMap.<String, Object>of( "names", Arrays.asList( "Alice", "Becky", "Carol" ),
                                                                          "ages", Arrays.asList( 37, 42, 32 ) );
        assertPathWalksAre( Traversers.elements().children( Walk.<Object, String>empty( root ) ),
                            new Object[][] { { "names", root.get( "names" ) },
                                             { "ages", root.get( "ages" ) } } );
    }

    @SuppressWarnings( "unchecked" )
    @Test
    public void elementsList() {
        final List<Object> root = Arrays.<Object>asList( ImmutableMap.of( "name", "Alice", "age", 37 ),
                                                         ImmutableMap.of( "name", "Becky", "age", 42 ) );
        assertPathWalksAre( Traversers.elements().children( Walk.<Object, String>empty( root ) ),
                            new Object[][] { { "[0]", root.get( 0 ) },
                                             { "[1]", root.get( 1 ) } } );
    }

    @Test
    public void elementsArray() {
        final Object[] root = new Object[] { ImmutableMap.of( "name", "Alice", "age", 37 ),
                                             ImmutableMap.of( "name", "Becky", "age", 42 ) };
        assertPathWalksAre( Traversers.elements().children( Walk.<Object, String>empty( root ) ),
                            new Object[][] { { "[0]", root[ 0 ] },
                                             { "[1]", root[ 1 ] } } );
    }

    // weighted( TreeTraverser, Function )
    // We will use variants of the preOrder() tests to work with the weighted walks.

    private final Multimap<String, List<String>> empty = ImmutableMultimap.of();

    private final Multimap<String, List<String>> singleEdge = ImmutableMultimap.of(
            "A", Arrays.asList( "B", "123" ) );

    // Warning! Do not perform a post-order traversal on this graph.
    private final Multimap<String, List<String>> loop = ImmutableMultimap.of(
            "A", Arrays.asList( "A", "123" ) );

    // Warning! Do not perform a post-order traversal on this graph.
    private final Multimap<String, List<String>> cycle = ImmutableListMultimap.<String, List<String>>builder()
            .put( "A", Arrays.asList( "B", "1" ) )
            .put( "B", Arrays.asList( "C", "12" ) )
            .put( "C", Arrays.asList( "A", "123" ) )
            .build();

    private final Multimap<String, List<String>> tree = ImmutableListMultimap.<String, List<String>>builder()
            .put( "A", Arrays.asList( "B", "1" ) )
            .put( "A", Arrays.asList( "C", "12" ) )
            .put( "B", Arrays.asList( "D", "123" ) )
            .put( "B", Arrays.asList( "E", "1234" ) )
            .put( "C", Arrays.asList( "F", "12345" ) )
            .put( "C", Arrays.asList( "G", "123456" ) )
            .build();

    // Has two paths from A to D.
    private final Multimap<String, List<String>> dag = ImmutableListMultimap.<String, List<String>>builder()
            .put( "A", Arrays.asList( "B", "1" ) )
            .put( "A", Arrays.asList( "C", "12" ) )
            .put( "B", Arrays.asList( "D", "123" ) )
            .put( "B", Arrays.asList( "E", "1234" ) )
            .put( "C", Arrays.asList( "D", "12345" ) )
            .put( "D", Arrays.asList( "G", "123456" ) )
            .build();

    private static final Function<String, Double> WEIGHT = new Function<String, Double>() {
        @Override
        public Double apply( final String input ) {
            return (double) input.length();
        }
    };

    private static TreeTraverser<Walk<String, String>> adjacencyFor( final Multimap<String, List<String>> graph ) {
        return new TreeTraverser<Walk<String, String>>() {
            @Override
            public Iterable<Walk<String, String>> children( final Walk<String, String> root ) {
                return new Iterable<Walk<String, String>>() {
                    @Override
                    public Iterator<Walk<String, String>> iterator() {
                        final Iterator<List<String>> iterator = graph.get( root.getTo() ).iterator();

                        return new Iterator<Walk<String, String>>() {
                            @Override
                            public boolean hasNext() {
                                return iterator.hasNext();
                            }

                            @Override
                            public Walk<String, String> next() {
                                final List<String> list = iterator.next();
                                return root.append( list.get( 0 ), list.get( 1 ) );
                            }

                            @Override
                            public void remove() {
                                iterator.remove();
                            }
                        };
                    }
                };
            }
        };
    }

    private static TreeTraverser<Walk<String, String>> mutableAdjacencyFor( final Multimap<String, List<String>> graph ) {
        return adjacencyFor( ArrayListMultimap.create( graph ) );
    }

    // from, weight, over, to, over, to, ...
    private static <V, E> void assertWalkContains( final WeightedWalk<V, E> actual, final Object... expected ) {
        assertThat( actual.getFrom(), is( expected[ 0 ] ) );
        assertThat( actual.getWeight(), is( expected[ 1 ] ) );
        assertThat( Iterables.size( actual.getVia() ), is( ( expected.length - 2 ) / 2 ) );
        int i = expected.length - 2;
        for( final Walk.Step<V, E> step : actual.getVia() ) {
            assertThat( step.getOver(), is( expected[ i ] ) );
            assertThat( step.getTo(), is( expected[ i + 1 ] ) );
            i -= 2;
        }
    }

    private static <V, E> void assertNextWalkIs( final Iterator<WeightedWalk<V, E>> iterator, final Object... expectedWalk ) {
        assertThat( iterator.hasNext(), is( true ) );
        assertWalkContains( iterator.next(), expectedWalk );
    }

    private static <V, E> void assertNextWalksAre( final Iterator<WeightedWalk<V, E>> iterator, final Object[][] expectedWalks ) {
        for( final Object[] expectedWalk : expectedWalks ) {
            assertNextWalkIs( iterator, expectedWalk );
        }
    }

    private static <V, E> void assertTraversalContains( final Iterable<WeightedWalk<V, E>> traversal, final Object[][] expectedWalks ) {
        final Iterator<WeightedWalk<V, E>> iterator = traversal.iterator();
        assertNextWalksAre( iterator, expectedWalks );
        assertThat( iterator.hasNext(), is( false ) );
        assertIteratorEmpty( iterator );
    }

    private static <V, E> void assertTraversalBegins( final Iterable<WeightedWalk<V, E>> traversal, final Object[][] expectedWalks ) {
        final Iterator<WeightedWalk<V, E>> iterator = traversal.iterator();
        assertNextWalksAre( iterator, expectedWalks );
        assertThat( iterator.hasNext(), is( true ) );
    }

    @Test
    public void weightedEmpty() {
        final TreeTraverser<WeightedWalk<String, String>> traverser = Traversers.weighted( adjacencyFor( empty ), WEIGHT );
        final Iterable<WeightedWalk<String, String>> traversal = Traversals.preOrder( WeightedWalk.<String, String>empty( "A" ), traverser );
        assertTraversalContains( traversal, new Object[][] { { "A", 0.0 } } );
    }

    @Test
    public void weightedSingleEdge() {
        final TreeTraverser<WeightedWalk<String, String>> traverser = Traversers.weighted( adjacencyFor( singleEdge ), WEIGHT );
        final Iterable<WeightedWalk<String, String>> traversal = Traversals.preOrder( WeightedWalk.<String, String>empty( "A" ), traverser );
        assertTraversalContains( traversal, new Object[][] { { "A", 0.0 }, { "A", 3.0, "123", "B" } } );
    }

    @Test
    public void weightedLoop() {
        final TreeTraverser<WeightedWalk<String, String>> traverser = Traversers.weighted( adjacencyFor( loop ), WEIGHT );
        final Iterable<WeightedWalk<String, String>> traversal = Traversals.preOrder( WeightedWalk.<String, String>empty( "A" ), traverser );
        assertTraversalBegins( traversal,
                               new Object[][] { { "A", 0.0 },
                                                { "A", 3.0, "123", "A" },
                                                { "A", 6.0, "123", "A", "123", "A" },
                                                { "A", 9.0, "123", "A", "123", "A", "123", "A" } } );
    }

    @Test
    public void weightedCycle() {
        final TreeTraverser<WeightedWalk<String, String>> traverser = Traversers.weighted( adjacencyFor( cycle ), WEIGHT );
        final Iterable<WeightedWalk<String, String>> traversal = Traversals.preOrder( WeightedWalk.<String, String>empty( "A" ), traverser );
        assertTraversalBegins( traversal,
                               new Object[][] { { "A", 0.0 },
                                                { "A", 1.0, "1", "B" },
                                                { "A", 3.0, "1", "B", "12", "C" },
                                                { "A", 6.0, "1", "B", "12", "C", "123", "A" },
                                                { "A", 7.0, "1", "B", "12", "C", "123", "A", "1", "B" } } );
    }

    @Test
    public void weightedTree() {
        final TreeTraverser<WeightedWalk<String, String>> traverser = Traversers.weighted( adjacencyFor( tree ), WEIGHT );
        final Iterable<WeightedWalk<String, String>> traversal = Traversals.preOrder( WeightedWalk.<String, String>empty( "A" ), traverser );
        assertTraversalContains( traversal,
                                 new Object[][] { { "A", 0.0 },
                                                  { "A", 1.0, "1", "B" },
                                                  { "A", 4.0, "1", "B", "123", "D" },
                                                  { "A", 5.0, "1", "B", "1234", "E" },
                                                  { "A", 2.0, "12", "C" },
                                                  { "A", 7.0, "12", "C", "12345", "F" },
                                                  { "A", 8.0, "12", "C", "123456", "G" } } );
    }

    private static void assertPreOrderFullDag( final Iterable<WeightedWalk<String, String>> traversal ) {
        assertTraversalContains( traversal,
                                 new Object[][] { { "A", 0.0 },
                                                  { "A", 1.0, "1", "B" },
                                                  { "A", 4.0, "1", "B", "123", "D" },
                                                  { "A", 10.0, "1", "B", "123", "D", "123456", "G" },
                                                  { "A", 5.0, "1", "B", "1234", "E" },
                                                  { "A", 2.0, "12", "C" },
                                                  { "A", 7.0, "12", "C", "12345", "D" },
                                                  { "A", 13.0, "12", "C", "12345", "D", "123456", "G" } } );
    }

    @Test
    public void weightedDag() {
        final TreeTraverser<WeightedWalk<String, String>> traverser = Traversers.weighted( adjacencyFor( dag ), WEIGHT );
        final Iterable<WeightedWalk<String, String>> traversal = Traversals.preOrder( WeightedWalk.<String, String>empty( "A" ), traverser );
        assertPreOrderFullDag( traversal );
    }

    @Test( expected = IllegalStateException.class )
    public void weightedRemoveBeforeNext() {
        final TreeTraverser<WeightedWalk<String, String>> adjacency = Traversers.weighted( mutableAdjacencyFor( dag ), WEIGHT );
        final Iterable<WeightedWalk<String, String>> traversal = Traversals.preOrder( WeightedWalk.<String, String>empty( "A" ), adjacency );
        final Iterator<WeightedWalk<String, String>> iterator = traversal.iterator();
        iterator.remove();
    }

    @Test( expected = IllegalStateException.class )
    public void weightedRemoveRoot() {
        final TreeTraverser<WeightedWalk<String, String>> adjacency = Traversers.weighted( mutableAdjacencyFor( dag ),
                                                                                           WEIGHT );
        final Iterable<WeightedWalk<String, String>> traversal = Traversals.preOrder( WeightedWalk.<String, String>empty( "A" ), adjacency );
        final Iterator<WeightedWalk<String, String>> iterator = traversal.iterator();
        assertNextWalkIs( iterator, "A", 0.0 );
        iterator.remove();
    }

    @Test
    public void weightedRemoveB() {
        final TreeTraverser<WeightedWalk<String, String>> adjacency = Traversers.weighted( mutableAdjacencyFor( dag ),
                                                                                           WEIGHT );
        final Iterable<WeightedWalk<String, String>> traversal = Traversals.preOrder( WeightedWalk.<String, String>empty( "A" ), adjacency );
        final Iterator<WeightedWalk<String, String>> iterator = traversal.iterator();
        assertNextWalksAre( iterator, new Object[][] { { "A", 0.0 }, { "A", 1.0, "1", "B" } } );
        iterator.remove();
        assertNextWalksAre( iterator,
                            new Object[][] { { "A", 2.0, "12", "C" },
                                             { "A", 7.0, "12", "C", "12345", "D" },
                                             { "A", 13.0, "12", "C", "12345", "D", "123456", "G" } } );

        // Check that data structure was actually changed
        assertTraversalContains( Traversals.preOrder( WeightedWalk.<String, String>empty( "A" ), adjacency ),
                                 new Object[][] { { "A", 0.0 },
                                                  { "A", 2.0, "12", "C" },
                                                  { "A", 7.0, "12", "C", "12345", "D" },
                                                  { "A", 13.0, "12", "C", "12345", "D", "123456", "G" } } );
    }

    @Test
    public void weightedRemoveD() {
        final TreeTraverser<WeightedWalk<String, String>> adjacency = Traversers.weighted( mutableAdjacencyFor( dag ),
                                                                                           WEIGHT );
        final Iterable<WeightedWalk<String, String>> traversal = Traversals.preOrder( WeightedWalk.<String, String>empty( "A" ), adjacency );
        final Iterator<WeightedWalk<String, String>> iterator = traversal.iterator();
        assertNextWalksAre( iterator,
                            new Object[][] { { "A", 0.0 }, { "A", 1.0, "1", "B" }, { "A", 4.0, "1", "B", "123", "D" } } );
        iterator.remove();
        assertNextWalksAre( iterator,
                            new Object[][] { { "A", 5.0, "1", "B", "1234", "E" },
                                             { "A", 2.0, "12", "C" },
                                             { "A", 7.0, "12", "C", "12345", "D" },
                                             { "A", 13.0, "12", "C", "12345", "D", "123456", "G" } } );

        // Check that data structure was actually changed
        assertTraversalContains( Traversals.preOrder( WeightedWalk.<String, String>empty( "A" ), adjacency ),
                                 new Object[][] { { "A", 0.0 },
                                                  { "A", 1.0, "1", "B" },
                                                  { "A", 5.0, "1", "B", "1234", "E" },
                                                  { "A", 2.0, "12", "C" },
                                                  { "A", 7.0, "12", "C", "12345", "D" },
                                                  { "A", 13.0, "12", "C", "12345", "D", "123456", "G" } } );
    }

    @Test
    public void weightedRemoveE() {
        final TreeTraverser<WeightedWalk<String, String>> adjacency = Traversers.weighted( mutableAdjacencyFor( dag ),
                                                                                           WEIGHT );
        final Iterable<WeightedWalk<String, String>> traversal = Traversals.preOrder( WeightedWalk.<String, String>empty( "A" ), adjacency );
        final Iterator<WeightedWalk<String, String>> iterator = traversal.iterator();
        assertNextWalksAre( iterator,
                            new Object[][] { { "A", 0.0 },
                                             { "A", 1.0, "1", "B" },
                                             { "A", 4.0, "1", "B", "123", "D" },
                                             { "A", 10.0, "1", "B", "123", "D", "123456", "G" },
                                             { "A", 5.0, "1", "B", "1234", "E" } } );
        iterator.remove();
        assertNextWalksAre( iterator,
                            new Object[][] { { "A", 2.0, "12", "C" },
                                             { "A", 7.0, "12", "C", "12345", "D" },
                                             { "A", 13.0, "12", "C", "12345", "D", "123456", "G" } } );

        // Check that data structure was actually changed
        assertTraversalContains( Traversals.preOrder( WeightedWalk.<String, String>empty( "A" ), adjacency ),
                                 new Object[][] { { "A", 0.0 },
                                                  { "A", 1.0, "1", "B" },
                                                  { "A", 4.0, "1", "B", "123", "D" },
                                                  { "A", 10.0, "1", "B", "123", "D", "123456", "G" },
                                                  { "A", 2.0, "12", "C" },
                                                  { "A", 7.0, "12", "C", "12345", "D" },
                                                  { "A", 13.0, "12", "C", "12345", "D", "123456", "G" } } );
    }

    @Test( expected = IllegalStateException.class )
    public void weightedRemoveTwice() {
        final TreeTraverser<WeightedWalk<String, String>> adjacency = Traversers.weighted( mutableAdjacencyFor( dag ),
                                                                                           WEIGHT );
        final Iterable<WeightedWalk<String, String>> traversal = Traversals.preOrder( WeightedWalk.<String, String>empty( "A" ), adjacency );
        final Iterator<WeightedWalk<String, String>> iterator = traversal.iterator();
        assertNextWalksAre( iterator, new Object[][] { { "A", 0.0 }, { "A", 1.0, "1", "B" } } );
        iterator.remove();
        iterator.remove();
    }
}
