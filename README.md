<!--
  ~ Copyright (c) 2012-2013 Ray A. Conner
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
[Google Guava's](http://code.google.com/p/guava-libraries/) `Function` interface, we have this:

    Iterable<Walk<V,E>> apply( V vertex )

The real point of all this is to cleanly separate how one manages the storage or representation of a graph-like
structure from how one traverses it. Just that single method is sufficiently expressive for building all kinds of
complex traversals, which is the half of the problem this library seeks to address.


## Update

Since this project started, Google Guava has added an abstract [TreeTraverser]
(http://docs.guava-libraries.googlecode.com/git-history/release/javadoc/com/google/common/collect/TreeTraverser.html)
class, which is essentially the previously described interface without the Walk semantics:

    public abstract Iterable<T> children( T root );

The class and method name have a definite connotation of "tree", but nothing in the implementation mandates that. As for
the desired Walk/Step capability, Guava's Louis Wasserman was kind enough to suggest an alternative way to handle that
with the simpler interface, documented [here](https://code.google.com/p/guava-libraries/issues/detail?id=174#c44).

Anansi now uses Guava's `TreeTraverser` to define an adjacency function. The `PersistentList` class is used to represent
a Walk. If you need to track the vertex paths from the root of the traversal, define a
`TreeTraverser<PersistentList<V>>`, and if you need to track both vertices and edges, define a
`TreeTraverser<PersistentList<Step<V,E>>>`.


## Simple examples

Assume we have a traverser for this graph, with children always alphabetical:

          A
         / \
        B   C
       / \ /
      E   D
          |
          F

Using the TreeTraverser methods provided by Guava:

    assertThat( Lists.newArrayList( treeTraverser.preOrderTraversal( "A" ) ),
                is( Arrays.asList( "A", "B", "D", "F", "E", "C", "D", "F" ) ) );

    assertThat( Lists.newArrayList( treeTraverser.postOrderTraversal( "A" ) ),
                is( Arrays.asList( "F", "D", "E", "B", "F", "D", "C", "A" ) ) );

    assertThat( Lists.newArrayList( treeTraverser.breadthFirstTraversal( "A" ) ),
                is( Arrays.asList( "A", "B", "C", "D", "E", "D", "F", "F" ) ) );

Using the equivalent methods in anansi. Note that anansi's Iterators support prune (pre-order and breadth-first only)
and Iterator.remove() (if the value returned by TreeTraverser.children() supports it). If you are performing a basic
traversal and don't need pruning or Iterator.remove(), you should probably use the Guava methods instead.

    assertThat( Lists.newArrayList( Traversals.preOrder( "A", treeTraverser ) ),
                is( Arrays.asList( "A", "B", "D", "F", "E", "C", "D", "F" ) ) );

    assertThat( Lists.newArrayList( Traversals.postOrder( "A", treeTraverser ) ),
                is( Arrays.asList( "F", "D", "E", "B", "F", "D", "C", "A" ) ) );

    assertThat( Lists.newArrayList( Traversals.breadthFirst( "A", treeTraverser ) ),
                is( Arrays.asList( "A", "B", "C", "D", "E", "D", "F", "F" ) ) );

Other anansi traversals:

    assertThat( Lists.newArrayList( Traversals.leaves( "A", treeTraverser ) ),
                is( Arrays.asList( "F", "E", "F" ) ) );

Pruning traversals (Yes, it's ugly. I know!):

    PruningIterator<String> iter = (PruningIterator<String>) Traversals.preOrder( "A", treeTraverser ).iterator();
    List<String> results = Lists.newArrayList();
    while( iter.hasNext() ) {
        String s = iter.next();
        results.add( s );
        if( s.equals( "D" ) ) {
            iter.prune();
        }
    }
    assertThat( results,
                is( Arrays.asList( "A", "B", "D", "E", "C", "D" ) ) );

    iter = (PruningIterator<String>) Traversals.breadthFirst( "A", treeTraverser ).iterator();
    results = Lists.newArrayList();
    while( iter.hasNext() ) {
        String s = iter.next();
        results.add( s );
        if( s.equals( "D" ) ) {
            iter.prune();
        }
    }
    assertThat( results,
                is( Arrays.asList( "A", "B", "C", "D", "E", "D" ) ) );


## Traversing Data Structure Leaves

Assume we have this JSON, already read into a data structure implemented with Maps and Lists (or arrays):

    {
        "name" : {
            "first" : "Alex",
            "last" : "Anderson"
        },
        "aliases" : [
            {
                "first" : "Bob",
                "last" : "Barnes"
            },
            {
                "first" : "Carl",
                "last" : "Cooper"
            },
            {
                "first" : "Dan",
                "last" : "Davis"
            }
        ]
    }

Then we can get the leaf elements and their json paths like this:

    // Just for verification by the assert, putting both paths and leaves in the same list is a bad idea
    List<Object> results = Lists.newArrayList();
    for( final PersistentList<Step<Object, String>> walk : Traversals.leafElements( jsonObject ) ) {
        final String path = Traversals.elementPath( walk );
        final Object leaf = walk.first().getTo();
        results.add( path );
        results.add( leaf );
    }
    assertThat( results,
                is( Arrays.<Object>asList(
                        "name.first", "Alex",
                        "name.last", "Anderson",
                        "aliases[0].first", "Bob",
                        "aliases[0].last", "Barnes",
                        "aliases[1].first", "Carl",
                        "aliases[1].last", "Cooper",
                        "aliases[2].first", "Dan",
                        "aliases[2].last", "Davis" ) ) );
