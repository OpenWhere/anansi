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

import com.google.common.collect.ImmutableMap;
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
}
