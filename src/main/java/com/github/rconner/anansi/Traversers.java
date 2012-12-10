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

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.UnmodifiableIterator;

import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Static factory methods for building Traversers.
 */
public class Traversers {

    /**
     * Prevent instantiation.
     */
    private Traversers() {
    }


    /**
     * Returns a Traverser that returns an empty Iterable for all inputs.
     *
     * @param <V>
     * @param <E>
     *
     * @return
     */
    @SuppressWarnings( "unchecked" )
    public static <V, E> Traverser<V, E> empty() {
        return ( Traverser<V, E> ) EmptyTraverser.INSTANCE;
    }

    private static final class EmptyTraverser<V, E> implements Traverser<V, E> {
        static final Traverser<?, ?> INSTANCE = new EmptyTraverser<Object, Object>();

        @Override
        public Iterable<Walk<V, E>> apply( final V input ) {
            return ImmutableSet.of();
        }
    }


    /**
     * Returns a pre-order traverser with <strong>NO</strong> cycle detection.
     *
     * @param adjacency
     * @param <V>
     * @param <E>
     *
     * @return
     */
    public static <V, E> Traverser<V, E> preOrder( final Traverser<V, E> adjacency ) {
        Preconditions.checkNotNull( adjacency );
        return new PreOrderTraverser<V, E>( Lazy.traverser( adjacency ) );
    }

    private static final class PreOrderTraverser<V, E> implements Traverser<V, E> {
        private final Traverser<V, E> adjacency;

        PreOrderTraverser( final Traverser<V, E> adjacency ) {
            this.adjacency = adjacency;
        }

        @Override
        public Iterable<Walk<V, E>> apply( final V start ) {
            return new Iterable<Walk<V, E>>() {
                @Override
                public Iterator<Walk<V, E>> iterator() {
                    return new PreOrderIterator<V, E>( start, adjacency );
                }
            };
        }
    }

    private static final class PreOrderIterator<V, E> implements PruningIterator<Walk<V, E>> {
        /**
         * The supplied adjacency function.
         */
        private final Traverser<V, E> adjacency;

        /**
         * A stack of Iterators. The next object to be returned is from the topmost Iterator which has something left to
         * return. As objects are returned, the above function is used to create new Iterators which are pushed onto the
         * stack, even if they are empty.
         */
        private final LinkedList<Iterator<Walk<V, E>>> iteratorStack = Lists.newLinkedList();

        /**
         * The Iterator which supplied the last object returned by next(). A value of {@code null} indicates that the
         * last object has been removed.
         */
        private Iterator<Walk<V, E>> current;

        /**
         * Collects adjacency walks and produces the compound walks to return.
         */
        private final Walk.Builder<V, E> builder;

        PreOrderIterator( final V start, final Traverser<V, E> adjacency ) {
            this.adjacency = adjacency;
            iteratorStack.addFirst( Iterators.singletonIterator( Walk.<V, E>empty( start ) ) );
            builder = Walk.from( start );
        }

        @Override
        public boolean hasNext() {
            for( final Iterator<Walk<V, E>> iterator : iteratorStack ) {
                if( iterator.hasNext() ) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public Walk<V, E> next() {
            while( !iteratorStack.isEmpty() ) {
                final Iterator<Walk<V, E>> t = iteratorStack.getFirst();
                if( t.hasNext() ) {
                    current = t;
                    final Walk<V, E> result = current.next();
                    iteratorStack.addFirst( adjacency.apply( result.getTo() ).iterator() );
                    builder.add( result );
                    return builder.build();
                }
                iteratorStack.removeFirst();
                builder.pop();
            }
            current = null;
            throw new NoSuchElementException();
        }

        @Override
        public void remove() {
            Preconditions.checkState( current != null );
            current.remove();
            current = null;
            iteratorStack.removeFirst();
            builder.pop();
        }

        @Override
        public void prune() {
            Preconditions.checkState( current != null );
            current = null;
            iteratorStack.removeFirst();
            builder.pop();
        }
    }


    /**
     * Returns a post-order traverser with <strong>NO</strong> cycle detection. If a cycle is present, some call to
     * next() will infinitely loop (most likely resulting in an OutOfMemoryError).
     *
     * @param adjacency
     * @param <V>
     * @param <E>
     *
     * @return
     */
    public static <V, E> Traverser<V, E> postOrder( final Traverser<V, E> adjacency ) {
        Preconditions.checkNotNull( adjacency );
        return new PostOrderTraverser<V, E>( Lazy.traverser( adjacency ) );
    }

    private static final class PostOrderTraverser<V, E> implements Traverser<V, E> {
        private final Traverser<V, E> adjacency;

        PostOrderTraverser( final Traverser<V, E> adjacency ) {
            this.adjacency = adjacency;
        }

        @Override
        public Iterable<Walk<V, E>> apply( final V start ) {
            return new Iterable<Walk<V, E>>() {
                @Override
                public Iterator<Walk<V, E>> iterator() {
                    return new PostOrderIterator<V, E>( start, adjacency );
                }
            };
        }
    }

    private static final class PostOrderIterator<V, E> implements Iterator<Walk<V, E>> {
        /**
         * The supplied adjacency function.
         */
        private final Traverser<V, E> adjacency;

        /**
         * A stack of Iterators.
         */
        private final LinkedList<Iterator<Walk<V, E>>> iteratorStack = Lists.newLinkedList();

        /**
         * Collects adjacency walks and produces the compound walks to return.
         */
        private final Walk.Builder<V, E> builder;

        PostOrderIterator( final V start, final Traverser<V, E> adjacency ) {
            this.adjacency = adjacency;
            iteratorStack.addFirst( Iterators.singletonIterator( Walk.<V, E>empty( start ) ) );
            builder = Walk.from( start );
        }

        @Override
        public boolean hasNext() {
            return !builder.isEmpty() || iteratorStack.getFirst().hasNext();
        }

        @Override
        public Walk<V, E> next() {
            Iterator<Walk<V, E>> top = iteratorStack.getFirst();

            if( !top.hasNext() ) {
                if( builder.isEmpty() ) {
                    throw new NoSuchElementException();
                }
                iteratorStack.removeFirst();
                final Walk<V, E> result = builder.build();
                builder.pop();
                return result;
            }

            while( true ) {
                final Walk<V, E> walk = top.next();
                top = adjacency.apply( walk.getTo() ).iterator();
                builder.add( walk );
                if( !top.hasNext() ) {
                    final Walk<V, E> result = builder.build();
                    builder.pop();
                    return result;
                }
                iteratorStack.addFirst( top );
            }
        }

        @Override
        public void remove() {
            Preconditions.checkState( !iteratorStack.isEmpty() );
            iteratorStack.getFirst().remove();
        }
    }


    /**
     * Returns a traverser to reachable leaves with <strong>NO</strong> cycle detection. If a cycle is present, some
     * call to next() will infinitely loop (most likely resulting in an OutOfMemoryError).
     *
     * @param adjacency
     * @param <V>
     * @param <E>
     *
     * @return
     */
    public static <V, E> Traverser<V, E> leaves( final Traverser<V, E> adjacency ) {
        Preconditions.checkNotNull( adjacency );
        return new LeafTraverser<V, E>( Lazy.traverser( adjacency ) );
    }

    private static final class LeafTraverser<V, E> implements Traverser<V, E> {
        private final Traverser<V, E> adjacency;

        LeafTraverser( final Traverser<V, E> adjacency ) {
            this.adjacency = adjacency;
        }

        @Override
        public Iterable<Walk<V, E>> apply( final V start ) {
            return new Iterable<Walk<V, E>>() {
                @Override
                public Iterator<Walk<V, E>> iterator() {
                    return new LeafIterator<V, E>( start, adjacency );
                }
            };
        }
    }

    private static final class LeafIterator<V, E> implements Iterator<Walk<V, E>> {
        /**
         * The supplied adjacency function.
         */
        private final Traverser<V, E> adjacency;

        /**
         * A stack of Iterators.
         */
        private final LinkedList<Iterator<Walk<V, E>>> iteratorStack = Lists.newLinkedList();

        /**
         * Collects adjacency walks and produces the compound walks to return.
         */
        private final Walk.Builder<V, E> builder;

        LeafIterator( final V start, final Traverser<V, E> adjacency ) {
            this.adjacency = adjacency;
            iteratorStack.addFirst( Iterators.singletonIterator( Walk.<V, E>empty( start ) ) );
            builder = Walk.from( start );
        }

        @Override
        public boolean hasNext() {
            for( final Iterator<Walk<V, E>> iterator : iteratorStack ) {
                if( iterator.hasNext() ) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public Walk<V, E> next() {
            while( !iteratorStack.getFirst().hasNext() ) {
                if( builder.isEmpty() ) {
                    throw new NoSuchElementException();
                }
                iteratorStack.removeFirst();
                builder.pop();
            }

            Iterator<Walk<V, E>> top = iteratorStack.getFirst();
            while( true ) {
                final Walk<V, E> walk = top.next();
                top = adjacency.apply( walk.getTo() ).iterator();
                builder.add( walk );
                if( !top.hasNext() ) {
                    final Walk<V, E> result = builder.build();
                    builder.pop();
                    return result;
                }
                iteratorStack.addFirst( top );
            }
        }

        @Override
        public void remove() {
            Preconditions.checkState( !iteratorStack.isEmpty() );
            iteratorStack.getFirst().remove();
        }
    }


    /**
     * Returns a Traverser which will return the (adjacency Walks to) immediate Iterable elements, array elements, or
     * Map values for any such non-empty input. If the input is an empty Iterable, array, or Map, an empty Iterable will
     * be returned. If the input is not an Iterable, array, or Map, an empty Iterable will be returned. The values of
     * {@link Walk.Step#getOver()} are strings representing the path in normal idiomatic usage.
     *
     * @return
     */
    public static Traverser<Object, String> elements() {
        return ELEMENT_ADJACENCY;
    }

    /**
     * Returns a {@link #leaves(Traverser)} Traverser which uses {@link #elements()} as an adjacency Traverser.
     *
     * @return
     */
    public static Traverser<Object, String> leafElements() {
        return LEAF_ELEMENTS_TRAVERSER;
    }

    /**
     * Returns an idiomatic String path for the given Walk produced by {@link #leafElements()}.
     *
     * @param walk
     *
     * @return
     */
    public static String elementPath( final Walk<Object, String> walk ) {
        final StringBuilder sb = new StringBuilder();
        for( final Walk.Step<Object, String> step : walk.getVia() ) {
            sb.append( step.getOver() );
        }
        if( sb.length() > 0 && sb.charAt( 0 ) == '.' ) {
            return sb.substring( 1 );
        }
        return sb.toString();
    }

    private static final Traverser<Object, String> ELEMENT_ADJACENCY = new Traverser<Object, String>() {
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

    private static final Traverser<Object, String> LEAF_ELEMENTS_TRAVERSER = leaves( ELEMENT_ADJACENCY );

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
                        return Walk.single( map, entry.getValue(), '.' + entry.getKey() );
                    }
                };
            }
        };
    }

}
