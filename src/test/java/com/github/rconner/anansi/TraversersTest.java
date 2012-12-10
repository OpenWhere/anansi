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


    // These all return something precisely because an empty map/iterable/array *is* a leaf.

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

    @Test
    public void normal() {
        final Map<?, ?> map = ImmutableMap.builder().put( "string", "A String" ).put( "integer", 42 ).put( "list", Arrays.asList( "zero", "one", "two", "three" ) ).put( "array", new Object[] { "four", "five", "six" } ).put( "booleanArray", new boolean[] { false, true, true, false, true } ).put( "map", ImmutableMap.builder().put( "string", "Another String" ).put( "people", Arrays.asList( ImmutableMap.of( "name", "Alice", "age", 37 ), ImmutableMap.of( "name", "Bob", "age", 55 ), ImmutableMap.of( "name", "Carol", "age", 23 ), ImmutableMap.of( "name", "Dave", "age", 27 ) ) ).put( "owner", ImmutableMap.of( "name", "Elise", "age", 43 ) ).build() ).build();

        Iterator<Walk<Object, String>> iterator = Traversers.leafElements().apply( map ).iterator();
        Walk<Object, String> walk;

        walk = iterator.next();
        assertThat( walk.getTo(), is( ( Object ) "A String" ) );
        assertThat( Traversers.elementPath( walk ), is( "string" ) );

        walk = iterator.next();
        assertThat( walk.getTo(), is( ( Object ) 42 ) );
        assertThat( Traversers.elementPath( walk ), is( "integer" ) );

        walk = iterator.next();
        assertThat( walk.getTo(), is( ( Object ) "zero" ) );
        assertThat( Traversers.elementPath( walk ), is( "list[0]" ) );

        walk = iterator.next();
        assertThat( walk.getTo(), is( ( Object ) "one" ) );
        assertThat( Traversers.elementPath( walk ), is( "list[1]" ) );

        walk = iterator.next();
        assertThat( walk.getTo(), is( ( Object ) "two" ) );
        assertThat( Traversers.elementPath( walk ), is( "list[2]" ) );

        walk = iterator.next();
        assertThat( walk.getTo(), is( ( Object ) "three" ) );
        assertThat( Traversers.elementPath( walk ), is( "list[3]" ) );

        walk = iterator.next();
        assertThat( walk.getTo(), is( ( Object ) "four" ) );
        assertThat( Traversers.elementPath( walk ), is( "array[0]" ) );

        walk = iterator.next();
        assertThat( walk.getTo(), is( ( Object ) "five" ) );
        assertThat( Traversers.elementPath( walk ), is( "array[1]" ) );

        walk = iterator.next();
        assertThat( walk.getTo(), is( ( Object ) "six" ) );
        assertThat( Traversers.elementPath( walk ), is( "array[2]" ) );

        walk = iterator.next();
        assertThat( walk.getTo(), is( ( Object ) false ) );
        assertThat( Traversers.elementPath( walk ), is( "booleanArray[0]" ) );

        walk = iterator.next();
        assertThat( walk.getTo(), is( ( Object ) true ) );
        assertThat( Traversers.elementPath( walk ), is( "booleanArray[1]" ) );

        walk = iterator.next();
        assertThat( walk.getTo(), is( ( Object ) true ) );
        assertThat( Traversers.elementPath( walk ), is( "booleanArray[2]" ) );

        walk = iterator.next();
        assertThat( walk.getTo(), is( ( Object ) false ) );
        assertThat( Traversers.elementPath( walk ), is( "booleanArray[3]" ) );

        walk = iterator.next();
        assertThat( walk.getTo(), is( ( Object ) true ) );
        assertThat( Traversers.elementPath( walk ), is( "booleanArray[4]" ) );

        walk = iterator.next();
        assertThat( walk.getTo(), is( ( Object ) "Another String" ) );
        assertThat( Traversers.elementPath( walk ), is( "map.string" ) );

        walk = iterator.next();
        assertThat( walk.getTo(), is( ( Object ) "Alice" ) );
        assertThat( Traversers.elementPath( walk ), is( "map.people[0].name" ) );

        walk = iterator.next();
        assertThat( walk.getTo(), is( ( Object ) 37 ) );
        assertThat( Traversers.elementPath( walk ), is( "map.people[0].age" ) );

        walk = iterator.next();
        assertThat( walk.getTo(), is( ( Object ) "Bob" ) );
        assertThat( Traversers.elementPath( walk ), is( "map.people[1].name" ) );

        walk = iterator.next();
        assertThat( walk.getTo(), is( ( Object ) 55 ) );
        assertThat( Traversers.elementPath( walk ), is( "map.people[1].age" ) );

        walk = iterator.next();
        assertThat( walk.getTo(), is( ( Object ) "Carol" ) );
        assertThat( Traversers.elementPath( walk ), is( "map.people[2].name" ) );

        walk = iterator.next();
        assertThat( walk.getTo(), is( ( Object ) 23 ) );
        assertThat( Traversers.elementPath( walk ), is( "map.people[2].age" ) );

        walk = iterator.next();
        assertThat( walk.getTo(), is( ( Object ) "Dave" ) );
        assertThat( Traversers.elementPath( walk ), is( "map.people[3].name" ) );

        walk = iterator.next();
        assertThat( walk.getTo(), is( ( Object ) 27 ) );
        assertThat( Traversers.elementPath( walk ), is( "map.people[3].age" ) );

        walk = iterator.next();
        assertThat( walk.getTo(), is( ( Object ) "Elise" ) );
        assertThat( Traversers.elementPath( walk ), is( "map.owner.name" ) );

        walk = iterator.next();
        assertThat( walk.getTo(), is( ( Object ) 43 ) );
        assertThat( Traversers.elementPath( walk ), is( "map.owner.age" ) );

        assertThat( iterator.hasNext(), is( false ) );
    }

}
