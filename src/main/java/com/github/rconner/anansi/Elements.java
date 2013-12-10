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
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.UnmodifiableIterator;

import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.regex.Pattern;

/**
 * Package implementations of Traversers returned by {@link Traversers#elements()} and {@link
 * Traversers#leafElements()}.
 *
 * @author rconner
 */
final class Elements {

    /**
     * Prevent instantiation.
     */
    @NoCoverage
    private Elements() {
    }

    @SuppressWarnings( "unchecked" )
    static final Traverser<Object, String> ELEMENT_ADJACENCY = new Traverser<Object, String>() {
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
                return iterableWalker( ( Iterable<?> ) object );
            }
            return ImmutableSet.of();
        }
    };


    static String path( final Walk<Object, String> walk ) {
        final StringBuilder sb = new StringBuilder();
        for( final Walk.Step<Object, String> step : walk.getVia().reverse() ) {
            sb.append( step.getOver() );
        }
        if( sb.length() > 0 && sb.charAt( 0 ) == '.' ) {
            return sb.substring( 1 );
        }
        return sb.toString();
    }

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

    private static final Pattern DELIMITER_PATTERN = Pattern.compile( "(\\.|\\[|\\])" );

    private static Iterable<Walk<Object, String>> mapWalker( final Map<String, Object> map ) {
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
                        final String over = DELIMITER_PATTERN.matcher( entry.getKey() ).replaceAll( "\\\\$0" );
                        return Walk.single( map, entry.getValue(), '.' + over );
                    }
                };
            }
        };
    }
}
