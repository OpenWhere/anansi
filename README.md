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

This readme will have some code-like snippets, but they're not code unless explicitly noted as such. The point of this
document is clarity, and to explain how things fit together. In particular, an `Iterable<Foo>` will generally be written
as `[Foo]`, and an array or `List` implementation should not be inferred.


## History - Why this project exists

This project is a direct descendant of the [Plexus Graph Library](http://sourceforge.net/projects/plexus/), a previous
project of mine. My thinking on graphs and traversals has changed greatly since plexus was created, mostly due to
using it daily on things for which it wasn't designed. The direction I want to go is so different that it is actually
impossible to migrate Plexus in any way other than a complete rewrite. I could do that there, but github is just a
better place to host code, at least in my initial opinion.

The world has changed; data is now big, really big, and distributed. In my work life, it no longer makes much sense to
ask global questions of your graph, at least not in a way that treats the graph as a graph. You might ask your data
store for all people named "Fred", but that's not really treating your data store like a graph (unless the literal text
"Fred" is itself a first-class vertex, but then it's no longer a global query). There still is a use case for that kind
of whole-graph query, it's just not a use case I need to address. For example, I have absolutely no desire to mandate
that implementations provide a (whole-graph) vertex or edge iterator.

Somewhat related to this is the use of predicates in plexus. Because the graph implementation is (necessarily) managing
its own storage, or delegating to something that does, it must expose an API allowing clients to issue filtered queries.
Because such an API must be very general, you're stuck with something like a generic predicate, which the graph
implementation has to understand so it can translate that into an actual low-level data store query. Otherwise, you end
up filtering in the library rather than the data store, which is a performance killer.

This got me to thinking even more seriously about the API, and what functionality was truly essential. In the end, my
primary use cases always boiled down to traversals, and always traversals rooted at a fixed set of vertices, usually
just one. From the original Plexus API, once you realize that graph mutation is really implementation-specific, the only
essential method was

    Traverser Graph.traverser( vertex, Predicate )

where a `Traverser` is essentially an `Iterator` with both vertex and edge information. That method specifically
produces adjacency Traversers, but breath-first search, depth-first search, etc. are also Traversers in Plexus.

If we then decide that Predicates should also be implementation-specific, and redefine Traverser properly using generics
as an `Iterator<Step<Vertex,Edge>>` or similar, we're left with this:

    Iterator<Step<V,E>> traverser( vertex )

Returning an Iterable is more useful than an Iterator, so that's another change. Extending this to encompass BFS, DFS,
or other complex traversals is just a matter of replacing the single Steps being returned with entire
[Walks](http://en.wikipedia.org/wiki/Walk_\(graph_theory\)#Walks).

This is really just a general function, accepting a vertex and returning walks from that vertex. It is not a graph, nor
does it pretend to be one. Any implementation is free to implement whatever filtering it wants however it wants. In
practice, adjacency traversals are produced in implementation-specific ways. In the end, using
[Google Guava's](http://code.google.com/p/guava-libraries/) `Function` interface we have this:

    Iterable<Walk<V,E>> apply( V vertex )

The real point of all this is to cleanly separate how one manages the storage or representation of a graph-like
structure from how one traverses it. Just that single method is sufficiently expressive for building all kinds of
complex traversals, which is the half of the problem this library seeks to address.


## Overview

See the above history discussion for the reasoning behind all this. See below for the reasoning behind the choice of
Walk/Step design.

This library does not have any classes or interfaces representing a _vertex_ or _edge_. The type variables `V` and `E`
denoting these things are provided by clients of the library, and can be anything. Here, the `E` type variable typically
represents the _content_ of the edge (weight, e.g.), and does not necessarily have any information about the vertices
upon which it is incident, although nothing prevents a client from including that information.

These are the essential classes/interfaces involved:

    Step<V,E> {
        V to;
        E over;
    }

    Walk<V,E> {
        V from;
        V to;  // convenience, same as steps.last.to
        [ Step<V,E> ] steps;
    }

    Traverser<V,E> {
        Iterable<Walk<V,E>> apply( V vertex );
    }

Clarifying how this generally works ... there is some implementation-specific way of creating Traversers that answer the
question "Given V, what is adjacent to it?". Using that (or any Traverser created by this library), you can construct
more complex Traversers for performing things like a pre-order depth-first traversal.

Repeating this for emphasis: An adjacency function is a Traverser. Give an adjacency function a vertex and it will
return an Iterable of (short) Walks to adjacent vertices. A depth-first search is also a Traverser; give it a vertex and
it will return an Iterable of (non-trivial) Walks to reachable vertices.


## Reasoning about options for implementing Walk/Step

Technically, a graph-theory Walk is a sequence
v<sub>0</sub>, e<sub>1</sub>, v<sub>1</sub>, e<sub>2</sub>, v<sub>2</sub>, ..., e<sub>n</sub>, v<sub>n</sub>;
where each e<sub>i</sub> is an "edge" from v<sub>i-1</sub> to v<sub>i</sub>. _Edge_ is in quotes because this library
doesn't really have edges in the same sense as graph theory. Here, the `E` type variable represents the _content_ of the
edge (weight, e.g.), and is generally supplied by the client for adjacency traversals. There is no actual edge object in
this library.

Regardless, OOP languages don't particularly like lists to contain things of wildly different types, and an alternating
sequence of vertices and edges would be difficult for client iterators to handle. Separate lists for vertices and edges
would be an option, but that's not very OO either, and would require clients to iterate over two lists simultaneously,
ugh. Note also that there is one more vertex than edge in the sequence.

All of that is the easy reasoning, and leads to roughly this model (pseudo-code, don't compile this):

    Step<V,E> {
        V to;
        E over;
    }

    Walk<V,E> {
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

    Step<V,E> {
        V to;
        E over;
    }

    Walk<V,E> {
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

    Step<V,E> {
        V to;
        E over;               // only if trivial is true
        [ Step<V,E> ] children;
        boolean trivial;      // or "primitive", "leaf", ...
    }

    Walk<V,E> {
        V from;
        V to;
        E over;               // only if trivial is true
        [ Step<V,E> ] steps;
        boolean trivial;      // or "primitive", "leaf", ...
    }

At this point, it hardly seems worthwhile to have a distinct Step at all. Removing that, we can repurpose the term for
the boolean trivial/composite property. Now we have this:

    Walk<V,E> {
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


### Resolution

Until requirements force anansi to take a different direction (it is being used, by me), it will be using Option 1.
That option is the simplest of the three, and may be sufficient. Simpler is always better as long as you meet
requirements.
