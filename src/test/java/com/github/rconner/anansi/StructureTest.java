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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.UnmodifiableIterator;
import org.junit.Test;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * A test covering a use case I have at work. This will probably migrate into the main code at some point, as soon as
 * I can figure out how to organize generally useful examples usages like this.
 * <p/>
 * This is essentially walking a json-ish map/collection/primitive nested structure at the leaf level.
 */
@SuppressWarnings( "unchecked" )
public class StructureTest {

    private static Iterable<Walk<Object, String>> arrayWalker( final Object array ) {
        final int size = Array.getLength( array );
        return new Iterable<Walk<Object, String>>() {
            @Override
            public Iterator<Walk<Object, String>> iterator() {
                return new UnmodifiableIterator<Walk<Object, String>>() {
                    private int index;

                    @Override
                    public boolean hasNext() {
                        return index < size;
                    }

                    @Override
                    public Walk<Object, String> next() {
                        if( index >= size ) {
                            throw new NoSuchElementException();
                        }
                        final String over = "[" + index + "]";
                        return Walk.single( array, Array.get( array, index++ ), over );
                    }
                };
            }
        };
    }

    private static Iterable<Walk<Object, String>> iterableWalker( final Iterable<?> iterable ) {
        return new Iterable<Walk<Object, String>>() {
            @Override
            public Iterator<Walk<Object, String>> iterator() {
                return new UnmodifiableIterator<Walk<Object, String>>() {
                    private final Iterator<?> delegate = iterable.iterator();
                    private int index;

                    @Override
                    public boolean hasNext() {
                        return delegate.hasNext();
                    }

                    @Override
                    public Walk<Object, String> next() {
                        final Object element = delegate.next();
                        final String over = "[" + index++ + "]";
                        return Walk.single( iterable, element, over );
                    }
                };
            }
        };
    }

    private static Iterable<Walk<Object, String>> mapWalker( final Map<String,Object> map ) {
        return new Iterable<Walk<Object, String>>() {
            @Override
            public Iterator<Walk<Object, String>> iterator() {
                return new UnmodifiableIterator<Walk<Object, String>>() {
                    private final Iterator<Map.Entry<String, Object>> delegate = map.entrySet().iterator();

                    @Override
                    public boolean hasNext() {
                        return delegate.hasNext();
                    }

                    @Override
                    public Walk<Object, String> next() {
                        final Map.Entry<String, ?> entry = delegate.next();
                        return Walk.single( map, entry.getValue(), '.' + entry.getKey() );
                    }
                };
            }
        };
    }

    public static final Traverser<Object, String> ADJACENCY = new Traverser<Object, String>() {
        @Override
        public Iterable<Walk<Object, String>> apply( final Object object ) {
            if( object == null ) {
                return ImmutableSet.of();
            } else if( object.getClass().isArray() ) {
                return arrayWalker( object );
            // Put Map before Iterable just in case someone declared an iterable map.
            } else if( object instanceof Map ) {
                return mapWalker( ( Map<String, Object> ) object );
            } else if( object instanceof Iterable ) {
                return iterableWalker( (Iterable<?>) object );
            }
            return ImmutableSet.of();
        }
    };

    public static String toPath( final Walk<Object, String> walk ) {
        final StringBuilder sb = new StringBuilder();
        for( final Walk.Step<Object, String> step : walk.getVia() ) {
            sb.append( step.getOver() );
        }
        return sb.toString();
    }

    // Some test cases.

    private final Map<String,Object> emptyMap = Collections.emptyMap();

    private final Iterable<Object> emptyIterable = Collections.emptyList();

    private final Object emptyArray = new int[0];

    @Test
    public void empty() {
        Iterator< Walk< Object, String > > iterator;
        Walk< Object, String > walk;

        // These all return something precisely because an empty map/iterable/array *is* a leaf.
        // We'll probably have to exclude this use case explicitly.

        iterator = Traversers.leaves( ADJACENCY ).apply( emptyMap ).iterator();
        walk = iterator.next();
        assertThat( walk.getTo(), is( (Object) emptyMap ) );
        assertThat( toPath( walk ), is( "" ) );
        assertThat( iterator.hasNext(), is( false ) );

        iterator = Traversers.leaves( ADJACENCY ).apply( emptyIterable ).iterator();
        walk = iterator.next();
        assertThat( walk.getTo(), is( (Object) emptyIterable ) );
        assertThat( toPath( walk ), is( "" ) );
        assertThat( iterator.hasNext(), is( false ) );

        iterator = Traversers.leaves( ADJACENCY ).apply( emptyArray ).iterator();
        walk = iterator.next();
        assertThat( walk.getTo(), is( emptyArray ) );
        assertThat( toPath( walk ), is( "" ) );
        assertThat( iterator.hasNext(), is( false ) );
    }


    private final Map<?,?> map = ImmutableMap.builder()
            .put( "string", "A String" )
            .put( "integer", 42 )
            .put( "list", Arrays.asList( "zero", "one", "two", "three" ) )
            .put( "array", new Object[] { "four", "five", "six" } )
            .put( "booleanArray", new boolean[] { false, true, true, false, true } )
            .put( "map", ImmutableMap.builder()
                    .put( "string", "Another String" )
                    .put( "people", Arrays.asList(
                            ImmutableMap.of( "name", "Alice", "age", 37 ),
                            ImmutableMap.of( "name", "Bob", "age", 55 ),
                            ImmutableMap.of( "name", "Carol", "age", 23 ),
                            ImmutableMap.of( "name", "Dave", "age", 27 ) ) )
                    .put( "owner", ImmutableMap.of( "name", "Elise", "age", 43 ) )
                    .build() )
            .build();

    @Test
    public void normal() {
        Iterator< Walk< Object, String > > iterator = Traversers.leaves( ADJACENCY ).apply( map ).iterator();
        Walk< Object, String > walk;

        walk = iterator.next();
        assertThat( walk.getTo(), is( (Object) "A String" ) );
        assertThat( toPath( walk ), is( ".string" ) );

        walk = iterator.next();
        assertThat( walk.getTo(), is( (Object) 42 ) );
        assertThat( toPath( walk ), is( ".integer" ) );

        walk = iterator.next();
        assertThat( walk.getTo(), is( (Object) "zero" ) );
        assertThat( toPath( walk ), is( ".list[0]" ) );

        walk = iterator.next();
        assertThat( walk.getTo(), is( (Object) "one" ) );
        assertThat( toPath( walk ), is( ".list[1]" ) );

        walk = iterator.next();
        assertThat( walk.getTo(), is( (Object) "two" ) );
        assertThat( toPath( walk ), is( ".list[2]" ) );

        walk = iterator.next();
        assertThat( walk.getTo(), is( (Object) "three" ) );
        assertThat( toPath( walk ), is( ".list[3]" ) );

        walk = iterator.next();
        assertThat( walk.getTo(), is( (Object) "four" ) );
        assertThat( toPath( walk ), is( ".array[0]" ) );

        walk = iterator.next();
        assertThat( walk.getTo(), is( (Object) "five" ) );
        assertThat( toPath( walk ), is( ".array[1]" ) );

        walk = iterator.next();
        assertThat( walk.getTo(), is( (Object) "six" ) );
        assertThat( toPath( walk ), is( ".array[2]" ) );

        walk = iterator.next();
        assertThat( walk.getTo(), is( (Object) false ) );
        assertThat( toPath( walk ), is( ".booleanArray[0]" ) );

        walk = iterator.next();
        assertThat( walk.getTo(), is( (Object) true ) );
        assertThat( toPath( walk ), is( ".booleanArray[1]" ) );

        walk = iterator.next();
        assertThat( walk.getTo(), is( (Object) true ) );
        assertThat( toPath( walk ), is( ".booleanArray[2]" ) );

        walk = iterator.next();
        assertThat( walk.getTo(), is( (Object) false ) );
        assertThat( toPath( walk ), is( ".booleanArray[3]" ) );

        walk = iterator.next();
        assertThat( walk.getTo(), is( (Object) true ) );
        assertThat( toPath( walk ), is( ".booleanArray[4]" ) );

        walk = iterator.next();
        assertThat( walk.getTo(), is( (Object) "Another String" ) );
        assertThat( toPath( walk ), is( ".map.string" ) );

        walk = iterator.next();
        assertThat( walk.getTo(), is( (Object) "Alice" ) );
        assertThat( toPath( walk ), is( ".map.people[0].name" ) );

        walk = iterator.next();
        assertThat( walk.getTo(), is( (Object) 37 ) );
        assertThat( toPath( walk ), is( ".map.people[0].age" ) );

        walk = iterator.next();
        assertThat( walk.getTo(), is( (Object) "Bob" ) );
        assertThat( toPath( walk ), is( ".map.people[1].name" ) );

        walk = iterator.next();
        assertThat( walk.getTo(), is( (Object) 55 ) );
        assertThat( toPath( walk ), is( ".map.people[1].age" ) );

        walk = iterator.next();
        assertThat( walk.getTo(), is( (Object) "Carol" ) );
        assertThat( toPath( walk ), is( ".map.people[2].name" ) );

        walk = iterator.next();
        assertThat( walk.getTo(), is( (Object) 23 ) );
        assertThat( toPath( walk ), is( ".map.people[2].age" ) );

        walk = iterator.next();
        assertThat( walk.getTo(), is( (Object) "Dave" ) );
        assertThat( toPath( walk ), is( ".map.people[3].name" ) );

        walk = iterator.next();
        assertThat( walk.getTo(), is( (Object) 27 ) );
        assertThat( toPath( walk ), is( ".map.people[3].age" ) );

        walk = iterator.next();
        assertThat( walk.getTo(), is( (Object) "Elise" ) );
        assertThat( toPath( walk ), is( ".map.owner.name" ) );

        walk = iterator.next();
        assertThat( walk.getTo(), is( (Object) 43 ) );
        assertThat( toPath( walk ), is( ".map.owner.age" ) );

        assertThat( iterator.hasNext(), is( false ) );
    }
}
