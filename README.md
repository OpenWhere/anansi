<!--
  ~ Copyright (c) 2012 Ray A. Conner
  ~
  ~ Permission is hereby granted, free of charge, to any person obtaining a
  ~ copy of this software and associated documentation files (the
  ~ "Software"), to deal in the Software without restriction, including
  ~ without limitation the rights to use, copy, modify, merge, publish,
  ~ distribute, sublicense, and/or sell copies of the Software, and to
  ~ permit persons to whom the Software is furnished to do so, subject to
  ~ the following conditions:
  ~
  ~ The above copyright notice and this permission notice shall be included
  ~ in all copies or substantial portions of the Software.
  ~
  ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
  ~ OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
  ~ MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
  ~ IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
  ~ CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
  ~ TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
  ~ SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
  -->

# anansi

Traversal primitives and operations for graph-like structures.

**TODO** Add basic discussion of the interfaces and how things work.

    Traversal := [ Walk ]
    Traverser := function: V -> Traversal

In particular, an adjacency function is a Traverser. Give an adjacency function a vertex and it will return an Iterable
of (short) Walks to adjacent vertices. This is very similar to a DFS, which is given a vertex and returns an Iterable of
non-trivial Walks.


## Reasoning about options for implementing Walk/Step

Technically, a graph-theory Walk is a sequence
v<sub>0</sub>, e<sub>1</sub>, v<sub>1</sub>, e<sub>2</sub>, v<sub>2</sub>, ..., e<sub>n</sub>, v<sub>n</sub>;
where each e<sub>i</sub> is an "edge" from v<sub>i-1</sub> to v<sub>i</sub>. _Edge_ is in quotes because this library
doesn't really have edges in the same sense as graph theory. Here, the **`E`** type variable represents the _content_
of the edge (weight, e.g.), and is generally supplied by the client for adjacency traversals. There is no actual edge
object in this library.

Regardless, OOP languages don't particularly like lists to contain things of wildly different types, and an alternating
sequence of vertices and edges would be difficult for client iterators to handle. Separate lists for vertices and edges
would be an option, but that's not very OO either, and would require clients to iterate over two lists simultaneously,
ugh. Note also that there is one more vertex than edge in the sequence.

All of that is the easy reasoning, and leads to roughly this model (pseudo-code, don't compile this):

    interface Step<V,E> {
        V to;
        E over;
    }

    interface Walk<V,E> {
        V from;
        V to;  // convenience, same as steps.last.to
        [ Step<V,E> ] steps;
    }

Where this gets difficult is that we also want to:

- Model an adjacency as essentially a one-step Walk.
- Referencing the parts (form/to/over) of a one-step adjacency walk should be simple getters.
- Model an empty Walk. For example, the first Walk in a pre-order traversal is just the start vertex, no edge having
  been followed yet.
- Model a self-loop, which must be different than an empty Walk.
- Distinguish between a one-step Walk being an adjacency Walk, or produced by BFS/DFS/etc. By extension, keep track of
  nested compound traversals.

To make that last point happen, you have to implement the composite pattern. Otherwise, you'll end up declaring things
like `Walk< V, Step< V, Step< V,E > > >`. While that might describe the structure accurately, it's not a good code.

It has to be possible for both client and library code to distinguish trivial walks/steps from compoosite ones. Not
having children is not sufficient, since the aforementioned empty walk is composite, but has no Steps. Therefore, there
either has to be method to distinguish the two, or they must be different public classes or interfaces.

That leads you to this:

    interface Step<V,E> {
        V to;
        E over;                  // only if trivial is true
        [ Step<V,E> ] children;  // only if trivial is false
        boolean trivial;         // or "primitive", "leaf", ...
    }

    interface Walk<V,E> {
        V from;
        V to;
        [ Step<V,E> ] steps;
    }

If you then decide you also want a trivial adjacency walk to have an easily accessible `over` property, without having
to invoke `walk.steps[0].over`, you end up with this:

    interface Step<V,E> {
        V to;
        E over;
        [ Step<V,E> ] children;
        boolean trivial;
    }

    interface Walk<V,E> {
        V from;
        V to;
        E over;
        [ Step<V,E> ] steps;
        boolean trivial;
    }

At this point, it hardly seems worthwhile to have a distinct Step interface at all. Removing that, we can repurpose the
term for the boolean trivial/composite property. Now we have this:

    interface Walk<V,E> {
        V from;
        V to;
        E over;                  // only if step is true, throws an exception otherwise
        [ Walk<V,E> ] children;  // only if step is false, throws an exception otherwise, or maybe returns []?
        boolean step;
    }


Let's work through an example. The adjacency graph will be as follows, with `x` being arbitrary at this point. During
the discussion, certain values of `x` will be explored to ascertain ambiguity.

    A --> B (over x)
    B --> C (over x)
    A --> A (over x)

A pre-order traversal from A would yield the following walks:

    A  // visting the root node
    A --> B
    A --> B --> C
    A --> A  // self-loop

The adjacency walks will be denoted as `{AB}`, `{BC}`, and `{AA}` respectively.

The pre-order traversal walks will be denoted as `[A]`, `[AB]`, `[ABC]`, and `[AA]` respectively.

If a walk is a step, it will be written as (for exampe) `A -> B over x`. If a walk is compound, it will be written as
(for example) `A -> C via [ A -> B over x, B -> C over x ]`.

<BR/>

<table>
    <tr>
        <th>{AB}</th>
        <td><pre><code>A -> B over x</code></pre></td>
    </tr>
    <tr>
        <th>{AA}</th>
        <td><pre><code>A -> A over x</code></pre></td>
    </tr>
    <tr>
        <th>[A]</th>
        <td><pre><code>A -> A via []</code></pre></td>
    </tr>
    <tr>
        <th>[AB]</th>
        <td><pre><code>A -> B via [ A -> B over x ]</code></pre></td>
    </tr>
    <tr>
        <th>[ABC]</th>
        <td><pre><code>A -> B via [ A -> B over x, B -> C over x ]</code></pre></td>
    </tr>
    <tr>
        <th>[AA]</th>
        <td><pre><code>A -> A via [ A -> A over x ]</code></pre></td>
    </tr>
</table>

Because trivial and compound walks have a method to distinguish them, there is no ambiguity even if `x` is an Iterable
of walks. When building a compound walk, the walks produced by the adjacency traverser can be used directly (if
immutable), and we don't even need to test whether they are trivial or not.
