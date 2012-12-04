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
- Distinguish between a one-step Walk being an adjacency Walk, or produced by BFS/DFS/etc.
- Keep track of nested compound traversals.


### Option 1

Stick with the above interfaces. We sacrifice:

- Referencing the parts (form/to/over) of a one-step adjacency walk should be simple getters.
- Distinguish between a one-step Walk being an adjacency Walk, or produced by BFS/DFS/etc.
- Keep track of nested compound traversals.

But we gain some simplicity from that. All Walks are the modeled the same way, and there is no if/then code for testing
adjacency vs. compound, because they are exactly the same.


### Option 2

Provide a way to distinguish one-step adjacency and compound Walks.

Not having children is not a sufficient criterion, since an empty walk is composite, but has no Steps. Therefore, there
either has to be method to distinguish the two, or they must be different public classes/interfaces. Having a method is
a bit cleaner.

After having done that, it's relatively simple to allow referencing the `over` of an adjacency Walk.

    interface Step<V,E> {
        V to;
        E over;
    }

    interface Walk<V,E> {
        V from;
        V to;
        E over;               // only if trivial is true
        [ Step<V,E> ] steps;
        boolean trivial;      // or "primitive", "leaf", ...
    }

The one capability we now don't have is to keep track of nested compound traversals. If you chain together two compound
walks `[A -> B -> ... -> M]` and `[M -> N -> ... -> Z]`, you get a flattened walk `[A -> ... -> Z ]`. Note that this
might actaully be a good thing.


### Option 3

Implement a composite pattern, to keep track of nested compound traversals. Otherwise, you'll end up declaring things
like `Walk< V, Step< V, Step< V,E > > >`. While that might describe the structure accurately, it's not good code.

That leads you to this:

    interface Step<V,E> {
        V to;
        E over;               // only if trivial is true
        [ Step<V,E> ] children;
        boolean trivial;      // or "primitive", "leaf", ...
    }

    interface Walk<V,E> {
        V from;
        V to;
        E over;               // only if trivial is true
        [ Step<V,E> ] steps;
        boolean trivial;      // or "primitive", "leaf", ...
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

This is essentially the same as the previous option, except replacing `[Step] steps` with `[Walk] children`. So we lose
the semantic difference between a Walk and a Step.


### Examples

Let's work through an example. The adjacency graph will be as follows, with `x` being arbitrary. No choice of `x`
results in ambiguity because the `over` and `steps/children` are stored in different fields.

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

A step will be written as `-> B over x`. An Option 1 walk will be written as `A -> B via [ ... ]`. An Option 2 or 3
trivial walk will be written as `A - > B over x`. An Option 2 or 3 compound walk will be written as
`A - > B via [ ... ]`.

<BR/>

<table>
    <tr>
        <th></th>
        <th>Option 1</th>
        <th>Option 2</th>
        <th>Option 3</th>
    </tr>
    <tr>
        <th>{AB}</th>
        <td>A -> B via [ -> B over x ]</td>
        <td>A -> B over x</td>
        <td>A -> B over x</td>
    </tr>
    <tr>
        <th>{AA}</th>
        <td>A -> A via [ -> A over x ]</td>
        <td>A -> A over x</td>
        <td>A -> A over x</td>
    </tr>
    <tr>
        <th>[A]</th>
        <td>A -> A via []</td>
        <td>A -> A via []</td>
        <td>A -> A via []</td>
    </tr>
    <tr>
        <th>[AB]</th>
        <td>A -> B via [ -> B over x ]</td>
        <td>A -> B via [ -> B over x ]</td>
        <td>A -> B via [ A -> B over x ]</td>
    </tr>
    <tr>
        <th>[ABC]</th>
        <td>A -> B via [ -> B over x, -> C over x ]</td>
        <td>A -> B via [ -> B over x, -> C over x ]</td>
        <td>A -> B via [ A -> B over x, B -> C over x ]</td>
    </tr>
    <tr>
        <th>[AA]</th>
        <td>A -> A via [ -> A over x ]</td>
        <td>A -> A via [ -> A over x ]</td>
        <td>A -> A via [ A -> A over x ]</td>
    </tr>
</table>

Besides the obvious differences, it should be noted that with Options 1 and 3, a compound traversal can reuse
Steps/Walks from the adjacency traversal to build the compound walks. Option 2 requires that the compound traversal
create new Step instances.
