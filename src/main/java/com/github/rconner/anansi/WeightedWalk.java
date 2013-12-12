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

import com.github.rconner.util.PersistentList;
import com.google.common.base.Preconditions;

/**
 * An immutable composition of a {@link Walk} and a weight. This class is not final, permitting extension. If extended,
 * then the immutability of added fields cannot be guaranteed.
 *
 * @param <V> the vertex type
 * @param <E> the edge type
 *
 * @author rconner
 */
public class WeightedWalk<V, E> {
    private final Walk<V, E> walk;
    private final double weight;

    /**
     * Creates a new WeightedWalk with the given Walk and weight.
     *
     * @param walk the Walk.
     * @param weight the weight.
     */
    public WeightedWalk( final Walk<V, E> walk, final double weight ) {
        Preconditions.checkNotNull( walk );
        this.walk = walk;
        this.weight = weight;
    }

    // Accessors

    /**
     * Returns the Walk component of this WeightedWalk.
     *
     * @return the Walk component of this WeightedWalk.
     */
    public final Walk<V, E> getWalk() {
        return walk;
    }

    /**
     * Returns the weight of this WeightedWalk.
     *
     * @return the weight of this WeightedWalk.
     */
    public final double getWeight() {
        return weight;
    }

    // Delegate accessors

    /**
     * Return the first vertex in this WeightedWalk. This method delegates to {@code getWalk().getFrom()}.
     *
     * @return the first vertex in this WeightedWalk.
     */
    public final V getFrom() {
        return walk.getFrom();
    }

    /**
     * Return the last vertex in this WeightedWalk. This method delegates to {@code getWalk().getTo()}.
     *
     * @return the last vertex in this WeightedWalk.
     */
    public final V getTo() {
        return walk.getTo();
    }

    /**
     * Returns the {@link Walk.Step Steps} in this WeightedWalk in reverse order. This method delegates to {@code
     * getWalk().getVia()}.
     *
     * @return the {@code Steps} in this WeightedWalk in reverse order.
     */
    public final PersistentList<Walk.Step<V, E>> getVia() {
        return walk.getVia();
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append( "(" );
        builder.append( weight );
        builder.append( ") " );
        builder.append( walk );
        return builder.toString();
    }

    // Static factory methods

    /**
     * Creates a new empty WeightedWalk with weight 0.0. This should only be used when a WeightedWalk literally has
     * travelled over no edges, the walk to the root of a breadth- or depth-first traversal for example.
     *
     * @param vertex the first and last vertex in the Walk.
     * @param <V> the vertex type
     * @param <E> the edge type
     *
     * @return a new empty WeightedWalk with weight 0.0.
     */
    public static <V, E> WeightedWalk<V, E> empty( final V vertex ) {
        return new WeightedWalk<V, E>( Walk.<V, E>empty( vertex ), 0.0 );
    }

    /**
     * Creates a new WeightedWalk with the given Walk and weight. This convenience method merely calls the constructor
     * allowing the caller to avoid specifying the generic type arguments.
     *
     * @param walk the Walk.
     * @param weight the weight.
     * @param <V> the vertex type
     * @param <E> the edge type
     *
     * @return a new WeightedWalk with the given Walk and weight.
     */
    public static <V, E> WeightedWalk<V, E> newInstance( final Walk<V, E> walk, final double weight ) {
        return new WeightedWalk<V, E>( walk, weight );
    }

    // Factory methods creating new WeightedWalks

    /**
     * Creates a new WeightedWalk starting with this WeightedWalk and appending a single Step.
     *
     * @param to the to vertex in the final Step of the new WeightedWalk.
     * @param over the single edge over which the the final Step of the new WeightedWalk steps.
     * @param stepWeight the weight of the appended Step.
     *
     * @return a new WeightedWalk starting with this WeightedWalk and appending a single Step.
     */
    public WeightedWalk<V, E> append( final V to, final E over, final double stepWeight ) {
        return new WeightedWalk<V, E>( walk.append( to, over ), weight + stepWeight );
    }

    /**
     * Creates a new WeightedWalk starting with this WeightedWalk and appending the given WeightedWalk, with the weight
     * of the sum of the two walks. The appended walk would normally start where this walk ends, but this condition is
     * not checked.
     *
     * @param weightedWalk the WeightedWalk to append to this WeightedWalk.
     *
     * @return a new WeightedWalk starting with this WeightedWalk and appending the given WeightedWalk, with the weight
     * of the sum of the two walks.
     */
    public WeightedWalk<V, E> append( final WeightedWalk<V, E> weightedWalk ) {
        return new WeightedWalk<V, E>( walk.append( weightedWalk.walk ), weight + weightedWalk.weight );
    }
}
