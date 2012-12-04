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
        [ Step<V,E> ] steps;  // rename this field!
    }

Where this gets difficult is that we also want to:

- Model an adjacency as essentially a one-step Walk.
- Referencing the parts (form/to/over) of a one-step adjacency walk should be simple getters.
- Model an empty Walk. For example, the first Walk in a pre-order traversal is just the start vertex, no edge having
  been followed yet.
- Model a self-loop, which must be different than an empty Walk.
- (maybe?) Distinguish between a one-step Walk being an adjacency Walk, or produced by BFS/DFS/etc. This is possibly a
  concern because the two could be handled differently by (another) compound traversal.

It is important to figure out how a compound traversal builds Walks from the Walks returned by the client-supplied
Traverser. Referring back to the basic interace definitions:

    Traversal := [ Walk ]
    Traverser := function: V -> Traversal

In particular, an adjacency function is a Traverser. Give an adjacency function a vertex and it will return an Iterable
of (short) Walks to adjacent vertices. This is very similar to a DFS, which is given a vertex and returns an Iterable of
non-trivial Walks. Note that it is a bad idea to test whether the "over" contains an Iterable (even of Steps) as
distinguishing between an adjacency Walk and a one-step compound walk, because an adjacency Walk just might be over some
Iterable which has nothing to do with compound traversals. Only the creator of the Walk knows what kind they are
building.

Some options for Walk implementions are:

1. The implementation described above.

2. Ignore what I just said, there is no `Step`.
```
    V from;
    V to;
    E over;  // can be a [Walk]
```

3. Exactly like the previous option, but there is a `Step` interface and `Walk.over` can be an `[Step]`.

4. A `Walk` is `from` + one `Step`, and `Step.over` can be an `[Step]`.
```
    V from;
    Step step;  // rename!
```

5. `Walk` extends `Step`, adding `from` field.
```
    V from;
    V to;  // inherited
    E over;  // inherited
```
Structurally, this is essentially the same as the previous option except Walk can be used directly as a Step, which
allows Walks to be reused. Compound traversals wouldn't need to construct Steps from adjacency Walks, they could just
use those Walks directly. That makes this the same as the option with no Step at all, since there would be no concrete
Step implementations; why bother when you can use the already-built Walk? This option will get no further consideration
because the no-Step option is simpler and equivalent.

6. Distinct classes/interfaces for trivial (adjacency) walks and compund walks.
```
    interface TrivialWalk {
        V from;
        V to;
        E over;
    }
    interface CompoundWalk {
        V from;
        V to;  // convenience, same as steps.last.to
        [ Step<V,E> ] steps;  // rename this field!
    }
```
The main problem with this option is complexity. The client has to either know what kind of thing they're getting, or
has to use instanceof checks.


Let's work through an example using each of these, except #5. The adjacency graph will be as follows, with `x` being
arbitrary at this point. During the discussion, certain values of `x` will be explored to ascertain ambiguity.

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

A `Step` will be written as `-> to "over" over`. For example, `-> B over x`.

A `Walk` will be written as `from -> to "over"/"steps" over/steps` if possible. For example, `A -> B over x` or
`A -> C over [ -> B over x, -C over x ]`.

Following the examples will be the pseudo-code snippet required to construct a compound walk from an Iterable of
adjacency walks.

<BR/>

<table>

    <tr>
        <th>Impl.</th>
        <th>{AB}</th>
        <th>{AA}</th>
        <th>[A]</th>
        <th>[AB]</th>
        <th>[ABC]</th>
        <th>[AA]</th>
    </tr>


    <tr>
        <td rowspan="3">1</td>
        <td>A -> B steps [ -> B over x ]</td>
        <td>A -> A steps [ -> A over x ]</td>
        <td>A -> A steps []</td>
        <td>A -> B steps [ -> B over x ]</td>
        <td>A -> C steps [ -> B over x, -> C over x ]</td>
        <td>A -> A steps [ -> A over x ]</td>
    </tr>

    <tr>
        <td colspan="7">
            {AB} and [AB] are ambiguous. {AA} and [AA] are ambiguous.<BR/>
            Adjacency Steps are reused when constructing compound Walks.<BR/>
            More deeply nested compound walks are flattened, so that Walk.steps always contains &quot;leaf&quot; steps.<BR/>
            Over value for adjacency walks is not easy to retrieve (walk.steps[0].over).
        </td>
    </tr>

    <tr>
        <td colspan="7">
<pre><code>
for( walk : adjacentWalks ) {
    compoundWalk.steps.appendAll( walk.steps );
}
</code></pre>
        </td>
    </tr>


    <tr>
        <td rowspan="3">2</td>
        <td>A -> B over x</td>
        <td>A -> A over x</td>
        <td>A -> A over []</td>
        <td>A -> B over [ A -> B over x ]</td>
        <td>A -> C over [ A -> B over x, B -> C over x ]</td>
        <td>A -> A over [ A -> A over x ]</td>
    </tr>

    <tr>
        <td colspan="7">
            {AA} and [A] are ambiguous if x=[]. All are ambiguous if x=[Walk].<BR/>
            Adjacency Walks are reused when constructing compound Walks.<BR/>
            Structure of more deeply nested compound walks is retained.
        </td>
    </tr>

    <tr>
        <td colspan="7">
<pre><code>
compoundWalk.over = adjacentWalks;
</code></pre>
        </td>
    </tr>


    <tr>
        <td rowspan="3">3</td>
        <td>A -> B over x</td>
        <td>A -> A over x</td>
        <td>A -> A over []</td>
        <td>A -> B over [ -> B over x ]</td>
        <td>A -> C over [ -> B over x, -> C over x ]</td>
        <td>A -> A over [ -> A over x ]</td>
    </tr>

    <tr>
        <td colspan="7">
            {AA} and [A] are ambiguous if x=[]. All are ambiguous if x=[Step].<BR/>
            No objects are reused when constructing compound Walks.<BR/>
            Structure of more deeply nested compound walks is retained.
        </td>
    </tr>

    <tr>
        <td colspan="7">
<pre><code>
for( walk : adjacentWalks ) {
    compoundWalk.over.append( -> walk.to over walk.over );
}
</code></pre>
        </td>
    </tr>


    <tr>
        <td rowspan="3">4</td>
        <td>A -> { B over x }</td>
        <td>A -> { A over x }</td>
        <td>A -> { null }</td>
        <td>A -> { B over [ { B over x } ]</td>
        <td>A -> { C over [ { B over x }, { C over x } ]</td>
        <td>A -> { A over [ { A over x } ]</td>
    </tr>

    <tr>
        <td colspan="7">
            All are ambiguous if x=[Step].<BR/>
            Adjacency Steps are reused when constructing compound Walks.<BR/>
            Structure of more deeply nested compound walks is retained.
        </td>
    </tr>

    <tr>
        <td colspan="7">
<pre><code>
for( walk : adjacentWalks ) {
    compoundWalk.step.over.append( walk.step );
}
</code></pre>
        </td>
    </tr>


    <tr>
        <td rowspan="3">6</td>
        <td>A -> B over x</td>
        <td>A -> A over x</td>
        <td>A -> A steps []</td>
        <td>A -> B steps [ -> B over x ]</td>
        <td>A -> C steps [ -> B over x, -> C over x ]</td>
        <td>A -> A steps [ -> A over x ]</td>
    </tr>

    <tr>
        <td colspan="7">
            There is no ambiguity, but there are also two classes to distinguish.<BR/>
            No objects are reused when constructing compound Walks.<BR/>
            Structure of more deeply nested compound walks is retained.
        </td>
    </tr>

    <tr>
        <td colspan="7">
<pre><code>
for( walk : adjacentWalks ) {
    if( walk instanceof TrivialWalk ) {
        compoundWalk.steps.append( -> walk.to over walk.over );
    } else {
        compoundWalk.steps.append( -> walk.to over walk.steps );
    }
}
</code></pre>
        </td>
    </tr>

</table>
