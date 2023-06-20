/*
 * The MIT License
 *
 * JavaFXSmartGraph | Copyright 2019-2023  brunomnsilva@gmail.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package dev.vepo.openjgraph.graph;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;

/**
 * A graph is made up of a set of vertices connected by edges, where the edges
 * have no direction associated with them, i.e., they establish a two-way
 * connection.
 *
 * @param <V> Type of element stored at a vertex
 * @param <E> Type of element stored at an edge
 * 
 * @see Edge
 * @see Vertex
 */
public interface Graph<V, E> {

    /**
     * Create a new Graph.
     */
    public static <V, E> Graph<V, E> newGraph() { return new GraphEdgeList<>(); }

    /**
     * Returns the total number of vertices of the graph.
     * 
     * @return total number of vertices
     */
    int numVertices();

    /**
     * Calculate the shortest path using Dijkstra.
     * 
     * @param source  start of the path
     * @param destiny destiny
     * @return the path
     */
    default Path<V, E> dijkstra(Vertex<V, E> source, Vertex<V, E> destiny) {
        if (!this.hasVertex(source) || !this.hasVertex(destiny)) {
            throw new InvalidVertexException("Vertex does not exists!");
        }

        Path<V, E> shortestPath = null;
        var queue = new LinkedList<Path<V, E>>();
        queue.add(new Path<V, E>(source));
        while (!queue.isEmpty()) {
            var path = queue.removeFirst();
            if (path.endsWith(destiny)) {
                if (shortestPath == null || shortestPath.distance() > path.distance()) {
                    shortestPath = path;
                }
            } else {
                path.accessibleVertices()
                    .filter(v -> !path.contains(v))
                    .map(v -> path.walk(v.graph().edge(path.tail(), v).get()))
                    .forEach(queue::add);
            }
        }
        return shortestPath;
    }

    Optional<Edge<E, V>> edge(Vertex<V, E> vertexA, Vertex<V, E> vertexB);

    boolean hasVertex(Vertex<V, E> source);

    /**
     * Returns the vertex if present.
     * 
     * @param value
     * @return
     */
    Optional<Vertex<V, E>> vertex(V value);

    /**
     * Returns the total number of edges of the graph.
     * 
     * @return total number of vertices
     */
    int numEdges();

    /**
     * Returns the vertices of the graph as a collection. <br/>
     * If there are no vertices, returns an empty collection.
     * 
     * @return collection of vertices
     */
    Collection<Vertex<V, E>> vertices();

    /**
     * Returns the edges of the graph as a collection. <br/>
     * If there are no edges, returns an empty collection.
     * 
     * @return collection of edges
     */
    Collection<Edge<E, V>> edges();

    /**
     * Returns if the given element exists in edge collection.
     * 
     * @param e
     * @return
     */
    boolean hasEdge(E e);

    /**
     * Returns a vertex's <i>incident</i> edges as a collection. <br/>
     * Incident edges are all edges that are connected to vertex <code>v</code>. If
     * there are no incident edges, e.g., an isolated vertex, returns an empty
     * collection.
     * 
     * @param v vertex for which to obtain the incident edges
     * 
     * @return collection of edges
     */
    Collection<Edge<E, V>> incidentEdges(Vertex<V, E> v) throws InvalidVertexException;

    /**
     * Given vertex <code>v</code>, return the opposite vertex at the other end of
     * edge <code>e</code>. <br/>
     * If both <code>v</code> and <code>e</code> are valid, but <code>e</code> is
     * not connected to <code>v</code>, returns <i>null</i>.
     * 
     * @param v vertex on one end of <code>e</code>
     * @param e edge connected to <code>v</code>
     * @return opposite vertex along <code>e</code>
     * 
     * @exception InvalidVertexException if the vertex is invalid for the graph
     * @exception InvalidEdgeException   if the edge is invalid for the graph
     */
    Vertex<V, E> opposite(Vertex<V, E> v, Edge<E, V> e) throws InvalidVertexException, InvalidEdgeException;

    /**
     * Given vertex <code>v</code>, return the opposite vertex at the other end of
     * edge <code>e</code>. <br/>
     * If both <code>v</code> and <code>e</code> are valid, but <code>e</code> is
     * not connected to <code>v</code>, returns <i>null</i>.
     * 
     * @param v vertex on one end of <code>e</code>
     * @param e edge connected to <code>v</code>
     * @return opposite vertex along <code>e</code>
     * 
     * @exception InvalidVertexException if the vertex is invalid for the graph
     * @exception InvalidEdgeException   if the edge is invalid for the graph
     */
    Vertex<V, E> opposite(V v, E e) throws InvalidVertexException, InvalidEdgeException;

    /**
     * Evaluates whether two vertices are adjacent, i.e., there exists some edge
     * connecting <code>u</code> and <code>v</code>.
     * 
     * @param u a vertex
     * @param v another vertex
     * 
     * @return true if they are adjacent, false otherwise.
     * 
     * @exception InvalidVertexException if <code>u</code> or <code>v</code> are
     *                                   invalid vertices for the graph
     */
    boolean areAdjacent(Vertex<V, E> u, Vertex<V, E> v) throws InvalidVertexException;

    /**
     * Evaluates whether two vertices are adjacent, i.e., there exists some edge
     * connecting <code>u</code> and <code>v</code>.
     * 
     * @param u a vertex
     * @param v another vertex
     * 
     * @return true if they are adjacent, false otherwise.
     * 
     * @exception InvalidVertexException if <code>u</code> or <code>v</code> are
     *                                   invalid vertices for the graph
     */
    boolean areAdjacent(V u, V v) throws InvalidVertexException;

    /**
     * Inserts a new vertex with a given element, returning its reference.
     * 
     * @param vElement the element to store at the vertex
     * 
     * @return the reference of the newly created vertex
     * 
     * @exception InvalidVertexException if there already exists a vertex containing
     *                                   <code>vElement</code> according to the
     *                                   equality of
     *                                   {@link Object#equals(java.lang.Object) }
     *                                   method.
     * 
     */
    Vertex<V, E> insertVertex(V vElement) throws InvalidVertexException;

    /**
     * Inserts a new edge with a given element between two existing vertices and
     * return its (the edge's) reference.
     * 
     * @param u           a vertex
     * @param v           another vertex
     * @param edgeElement the element to store in the new edge
     * 
     * @return the reference for the newly created edge
     * 
     * @exception InvalidVertexException if <code>u</code> or <code>v</code> are
     *                                   invalid vertices for the graph
     * 
     * @exception InvalidEdgeException   if there already exists an edge containing
     *                                   <code>edgeElement</code> according to the
     *                                   equality of
     *                                   {@link Object#equals(java.lang.Object) }
     *                                   method.
     */
    Edge<E, V> insertEdge(Vertex<V, E> u, Vertex<V, E> v, E edgeElement) throws InvalidVertexException, InvalidEdgeException;

    /**
     * Inserts a new edge with a given element between two existing vertices and
     * return its (the edge's) reference.
     * 
     * @param vElement1   a vertex's stored element
     * @param vElement2   another vertex's stored element
     * @param edgeElement the element to store in the new edge
     * @param weight
     * 
     * @return the reference for the newly created edge
     * 
     * @exception InvalidVertexException if <code>vElement1</code> or
     *                                   <code>vElement2</code> are not found in any
     *                                   vertices of the graph according to the
     *                                   equality of
     *                                   {@link Object#equals(java.lang.Object) }
     *                                   method.
     * 
     * @exception InvalidEdgeException   if there already exists an edge containing
     *                                   <code>edgeElement</code> according to the
     *                                   equality of
     *                                   {@link Object#equals(java.lang.Object) }
     *                                   method.
     */
    Edge<E, V> insertEdge(V vElement1, V vElement2, E edgeElement, double weight) throws InvalidVertexException, InvalidEdgeException;

    /**
     * Inserts a new edge with a given element between two existing vertices and
     * return its (the edge's) reference.
     * 
     * @param vElement1   a vertex's stored element
     * @param vElement2   another vertex's stored element
     * @param edgeElement the element to store in the new edge
     * 
     * @return the reference for the newly created edge
     * 
     * @exception InvalidVertexException if <code>vElement1</code> or
     *                                   <code>vElement2</code> are not found in any
     *                                   vertices of the graph according to the
     *                                   equality of
     *                                   {@link Object#equals(java.lang.Object) }
     *                                   method.
     * 
     * @exception InvalidEdgeException   if there already exists an edge containing
     *                                   <code>edgeElement</code> according to the
     *                                   equality of
     *                                   {@link Object#equals(java.lang.Object) }
     *                                   method.
     */
    Edge<E, V> insertEdge(V vElement1, V vElement2, E edgeElement) throws InvalidVertexException, InvalidEdgeException;

    /**
     * Removes a vertex, along with all of its incident edges, and returns the
     * element stored at the removed vertex.
     * 
     * @param v vertex to remove
     * 
     * @return element stored at the removed vertex
     * 
     * @exception InvalidVertexException if <code>v</code> is an invalid vertex for
     *                                   the graph
     */
    V removeVertex(Vertex<V, E> v) throws InvalidVertexException;

    /**
     * Removes an edge and return its element.
     * 
     * @param e edge to remove
     * 
     * @return element stored at the removed edge
     * 
     * @exception InvalidEdgeException if <code>e</code> is an invalid edge for the
     *                                 graph.
     */
    E removeEdge(Edge<E, V> e) throws InvalidEdgeException;

    /**
     * Removes an edge and return its element.
     * 
     * @param u a vertex
     * @param v another vertex
     * 
     * @return element stored at the removed edge
     * 
     * @exception InvalidEdgeException if <code>e</code> is an invalid edge for the
     *                                 graph.
     */
    Optional<E> removeEdge(V u, V v) throws InvalidEdgeException;

    /**
     * Replaces the element of a given vertex with a new element and returns the
     * previous element stored at <code>v</code>.
     * 
     * @param v          vertex to replace its element
     * @param newElement new element to store in <code>v</code>
     * 
     * @return previous element previously stored in <code>v</code>
     * 
     * @exception InvalidVertexException if the vertex <code>v</code> is invalid for
     *                                   the graph, or; if there already exists
     *                                   another vertex containing the element
     *                                   <code>newElement</code> according to the
     *                                   equality of
     *                                   {@link Object#equals(java.lang.Object) }
     *                                   method.
     */
    V replace(Vertex<V, E> v, V newElement) throws InvalidVertexException;

    /**
     * Replaces the element of a given edge with a new element and returns the
     * previous element stored at <code>e</code>.
     * 
     * @param e          edge to replace its element
     * @param newElement new element to store in <code>e</code>
     * 
     * @return previous element previously stored in <code>e</code>
     * 
     * @exception InvalidVertexException if the edge <code>e</code> is invalid for
     *                                   the graph, or; if there already exists
     *                                   another edge containing the element
     *                                   <code>newElement</code> according to the
     *                                   equality of
     *                                   {@link Object#equals(java.lang.Object)}
     *                                   method.
     */
    E replace(Edge<E, V> e, E newElement) throws InvalidEdgeException;
}
