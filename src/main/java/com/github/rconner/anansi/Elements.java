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
import com.google.common.collect.TreeTraverser;
import com.google.common.collect.UnmodifiableIterator;

import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.regex.Pattern;

/**
 * Package implementations of Traverser returned by {@link Traversers#elements()} and the implementation of {@link
 * Traversers#elementPath(Walk)}.
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

    static final TreeTraverser<Walk<Object, String>> ELEMENT_ADJACENCY = new TreeTraverser<Walk<Object, String>>() {
        @Override
        public Iterable<Walk<Object, String>> children( final Walk<Object, String> root ) {
            final Object vertex = root.getTo();
            if( vertex == null ) {
                return ImmutableSet.of();
            } else if( vertex.getClass().isArray() ) {
                return arrayWalker( root );
                // Put Map before Iterable just in case someone declared an iterable map.
            } else if( vertex instanceof Map ) {
                return mapWalker( root );
            } else if( vertex instanceof Iterable ) {
                return iterableWalker( root );
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


    private static Iterable<Walk<Object, String>> arrayWalker( final Walk<Object, String> root ) {
        final Object array = root.getTo();
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
                        return root.append( Array.get( array, index++ ), over );
                    }
                };
            }
        };
    }

    private static Iterable<Walk<Object, String>> iterableWalker( final Walk<Object, String> root ) {
        final Iterable<?> iterable = (Iterable<?>) root.getTo();
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
                        return root.append( element, over );
                    }
                };
            }
        };
    }

    private static final Pattern DELIMITER_PATTERN = Pattern.compile( "(\\.|\\[|\\])" );

    private static Iterable<Walk<Object, String>> mapWalker( final Walk<Object, String> root ) {
        @SuppressWarnings( "unchecked" )
        final Map<String, Object> map = (Map<String, Object>) root.getTo();
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
                        return root.append( entry.getValue(), '.' + over );
                    }
                };
            }
        };
    }
}
