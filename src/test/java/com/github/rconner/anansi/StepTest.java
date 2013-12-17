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

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

public final class StepTest {

    @Test
    public void empty() {
        final Step<String,String> step = Step.empty( "abc" );
        assertThat( step.getTo(), is( "abc" ) );
        assertThat( step.getOver(), is( nullValue() ) );
        assertThat( step.isEmpty(), is( true ) );
        step.toString();
    }

    @Test
    public void newInstanceVertexEdge() {
        final Step<String, String> step = Step.newInstance( "abc", "xyz" );
        assertThat( step.getTo(), is( "abc" ) );
        assertThat( step.getOver(), is( "xyz" ) );
        assertThat( step.isEmpty(), is( false ) );
        step.toString();
    }

    @Test
    public void newInstanceVertex() {
        final Step<String, String> step = Step.newInstance( "abc" );
        assertThat( step.getTo(), is( "abc" ) );
        assertThat( step.getOver(), is( nullValue() ) );
        assertThat( step.isEmpty(), is( false ) );
        step.toString();
    }
}
