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
 * Traversals#elementPath(PersistentList)}.
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

    static final TreeTraverser<PersistentList<Step<Object, String>>> ELEMENT_ADJACENCY = new TreeTraverser<PersistentList<Step<Object, String>>>() {
        @Override
        public Iterable<PersistentList<Step<Object, String>>> children( final PersistentList<Step<Object, String>> walk ) {
            final Object vertex = walk.first().getTo();
            if( vertex == null ) {
                return ImmutableSet.of();
            } else if( vertex.getClass().isArray() ) {
                return arrayWalker( walk );
                // Put Map before Iterable just in case someone declared an iterable map.
            } else if( vertex instanceof Map ) {
                return mapWalker( walk );
            } else if( vertex instanceof Iterable ) {
                return iterableWalker( walk );
            }
            return ImmutableSet.of();
        }
    };

    static String path( final PersistentList<Step<Object, String>> walk ) {
        final StringBuilder sb = new StringBuilder();
        // Skip the root of the walk, since its over will be null.
        for( final Step<Object, String> step : walk.reverse().rest() ) {
            sb.append( step.getOver() );
        }
        if( sb.length() > 0 && sb.charAt( 0 ) == '.' ) {
            return sb.substring( 1 );
        }
        return sb.toString();
    }


    private static Iterable<PersistentList<Step<Object, String>>> arrayWalker( final PersistentList<Step<Object, String>> walk ) {
        final Object array = walk.first().getTo();
        final int size = Array.getLength( array );
        return new Iterable<PersistentList<Step<Object, String>>>() {
            @Override
            public Iterator<PersistentList<Step<Object, String>>> iterator() {
                return new UnmodifiableIterator<PersistentList<Step<Object, String>>>() {
                    private int index;

                    @Override
                    public boolean hasNext() {
                        return index < size;
                    }

                    @Override
                    public PersistentList<Step<Object, String>> next() {
                        if( index >= size ) {
                            throw new NoSuchElementException();
                        }
                        final String over = "[" + index + "]";
                        return walk.add( Step.newInstance( Array.get( array, index++ ), over ) );
                    }
                };
            }
        };
    }

    private static Iterable<PersistentList<Step<Object, String>>> iterableWalker( final PersistentList<Step<Object, String>> walk ) {
        final Iterable<?> iterable = (Iterable<?>) walk.first().getTo();
        return new Iterable<PersistentList<Step<Object, String>>>() {
            @Override
            public Iterator<PersistentList<Step<Object, String>>> iterator() {
                return new UnmodifiableIterator<PersistentList<Step<Object, String>>>() {
                    private final Iterator<?> delegate = iterable.iterator();
                    private int index;

                    @Override
                    public boolean hasNext() {
                        return delegate.hasNext();
                    }

                    @Override
                    public PersistentList<Step<Object, String>> next() {
                        final Object element = delegate.next();
                        final String over = "[" + index++ + "]";
                        return walk.add( Step.newInstance( element, over ) );
                    }
                };
            }
        };
    }

    private static final Pattern DELIMITER_PATTERN = Pattern.compile( "(\\.|\\[|\\])" );

    private static Iterable<PersistentList<Step<Object, String>>> mapWalker( final PersistentList<Step<Object, String>> walk ) {
        @SuppressWarnings( "unchecked" )
        final Map<String, Object> map = (Map<String, Object>) walk.first().getTo();
        return new Iterable<PersistentList<Step<Object, String>>>() {
            @Override
            public Iterator<PersistentList<Step<Object, String>>> iterator() {
                return new UnmodifiableIterator<PersistentList<Step<Object, String>>>() {
                    private final Iterator<Map.Entry<String, Object>> delegate = map.entrySet().iterator();

                    @Override
                    public boolean hasNext() {
                        return delegate.hasNext();
                    }

                    @Override
                    public PersistentList<Step<Object, String>> next() {
                        final Map.Entry<String, ?> entry = delegate.next();
                        final String over = DELIMITER_PATTERN.matcher( entry.getKey() ).replaceAll( "\\\\$0" );
                        return walk.add( Step.newInstance( entry.getValue(), '.' + over ) );
                    }
                };
            }
        };
    }
}
