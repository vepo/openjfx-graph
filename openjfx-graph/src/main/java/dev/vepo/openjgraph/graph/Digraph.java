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

import java.security.SecureRandom;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.IntFunction;
import java.util.stream.IntStream;

import org.eclipse.collections.api.factory.primitive.IntObjectMaps;

/**
 * A directed graph (or digraph) is a graph that is made up of a set of vertices
 * connected by edges, where the edges have a direction associated with them.
 * <br>
 * A directed-edge leaves the <i>outbound vertex</i> towards the <i>inbound
 * vertex</i> and this changes the reasoning behind some methods of the
 * {@link Graph} interface, which are overridden in this interface to provide
 * different documentation of expected behavior.
 *
 * @param <V> Type of element stored at a vertex
 * @param <E> Type of element stored at an edge
 * @see Graph
 * @see Edge
 * @see Vertex
 */
public interface Digraph<V, E> extends Graph<V, E> {

    /**
     * Create a new Digraph.
     */
    public static <V, E> Digraph<V, E> newDigraph() { return new DigraphEdgeList<>(); }

    // http://networksciencebook.com/chapter/3#random-network
    static <V, E> Digraph<V, E> random(int nodeSize,
                                       double edgeProbability,
                                       IntFunction<V> vertexGenerator,
                                       BiFunction<V, V, E> edgeGenerator,
                                       long seed) {
        var random = new SecureRandom();
        random.setSeed(seed);
        var graph = Digraph.<V, E>newDigraph();
        var vertexObjects = IntObjectMaps.mutable.<V>empty();
        IntStream.range(1, nodeSize + 1)
                 .mapToObj(i -> {
                     var v = vertexGenerator.apply(i);
                     vertexObjects.put(i, v);
                     return v;
                 })
                 .forEach(graph::insertVertex);
        IntStream.range(1, nodeSize + 1)
                 .forEach(i -> IntStream.range(1, i + 1)
                                        .filter(j -> random.nextDouble() < edgeProbability)
                                        .forEach(j -> graph.insertEdge(vertexObjects.get(i),
                                                                       vertexObjects.get(j),
                                                                       edgeGenerator.apply(vertexObjects.get(i),
                                                                                           vertexObjects.get(j)))));
        return graph;
    }

    static <V, E> Graph<V, E> random(int nodeSize,
                                     double edgeProbability,
                                     IntFunction<V> vertexGenerator,
                                     BiFunction<V, V, E> edgeGenerator) {
        return random(nodeSize, edgeProbability, vertexGenerator, edgeGenerator, 0);
    }

    /**
     * Returns a vertex's <i>incident</i> edges as a collection. <br/>
     * Incident edges are all edges that have vertex <code>inbound</code> as the
     * <i>inbound vertex</i>, i.e., the edges "entering" vertex
     * <code>inbound</code>. If there are no incident edges, e.g., an isolated
     * vertex, returns an empty collection.
     *
     * @param inbound vertex for which to obtain the incident edges
     * @return collection of edges
     */
    @Override
    Collection<Edge<E, V>> incidentEdges(Vertex<V, E> inbound) throws InvalidVertexException;

    /**
     * Returns a vertex's <i>outbound</i> edges as a collection. <br/>
     * Incident edges are all edges that have vertex <code>outbound</code> as the
     * <i>outbound vertex</i>, i.e., the edges "leaving" vertex
     * <code>outbound</code>. If there are no outbound edges, e.g., an isolated
     * vertex, returns an empty collection.
     *
     * @param outbound vertex for which to obtain the outbound edges
     * @return collection of edges
     */
    Collection<Edge<E, V>> outboundEdges(Vertex<V, E> outbound) throws InvalidVertexException;

    /**
     * Evaluates whether two vertices are adjacent, i.e., there exists some
     * directed-edge connecting <code>outbound</code> and <code>inbound</code>.
     * <br/>
     * The existing edge must be directed as <code>outbound --&gt; inbound</code>.
     * <br/>
     * If, for example, there exists only an edge
     * <code>outbound &lt;-- inbound</code>, they are not considered adjacent.
     *
     * @param outbound outbound vertex
     * @param inbound  inbound vertex
     * @return true if they are adjacent, false otherwise.
     * @throws InvalidVertexException if <code>outbound</code> or
     *                                <code>inbound</code> are invalid vertices for
     *                                the graph
     */
    @Override
    boolean areAdjacent(Vertex<V, E> outbound, Vertex<V, E> inbound) throws InvalidVertexException;

    /**
     * Evaluates whether two vertices are adjacent, i.e., there exists some
     * directed-edge connecting <code>outbound</code> and <code>inbound</code>.
     * <br/>
     * The existing edge must be directed as <code>outbound --&gt; inbound</code>.
     * <br/>
     * If, for example, there exists only an edge
     * <code>outbound &lt;-- inbound</code>, they are not considered adjacent.
     *
     * @param outbound outbound vertex
     * @param inbound  inbound vertex
     * @return true if they are adjacent, false otherwise.
     * @throws InvalidVertexException if <code>outbound</code> or
     *                                <code>inbound</code> are invalid vertices for
     *                                the graph
     */
    @Override
    boolean areAdjacent(V outbound, V inbound) throws InvalidVertexException;

    /**
     * Inserts a new edge with a given element between two existing vertices and
     * return its (the edge's) reference.
     *
     * @param outbound    outbound vertex
     * @param inbound     inbound vertex
     * @param edgeElement the element to store in the new edge
     * @return the reference for the newly created edge
     * @throws InvalidVertexException if <code>outbound</code> or
     *                                <code>inbound</code> are invalid vertices for
     *                                the graph
     * @throws InvalidEdgeException   if there already exists an edge containing
     *                                <code>edgeElement</code> according to the
     *                                equality of
     *                                {@link Object#equals(java.lang.Object)}
     *                                method.
     */
    @Override
    Edge<E, V> insertEdge(Vertex<V, E> outbound, Vertex<V, E> inbound, E edgeElement)
            throws InvalidVertexException, InvalidEdgeException;

    /**
     * Inserts a new edge with a given element between two existing vertices and
     * return its (the edge's) reference.
     *
     * @param outbound    outbound vertex
     * @param inbound     inbound vertex
     * @param edgeElement the element to store in the new edge
     * @param weight      edge weight
     * @param properties  element properties
     * @return the reference for the newly created edge
     * @throws InvalidVertexException if <code>outbound</code> or
     *                                <code>inbound</code> are invalid vertices for
     *                                the graph
     * @throws InvalidEdgeException   if there already exists an edge containing
     *                                <code>edgeElement</code> according to the
     *                                equality of
     *                                {@link Object#equals(java.lang.Object)}
     *                                method.
     */
    @Override
    Edge<E, V> insertEdge(Vertex<V, E> outbound, Vertex<V, E> inbound, E edgeElement, double weight, Map<String, Object> properties)
            throws InvalidVertexException, InvalidEdgeException;

    /**
     * Inserts a new edge with a given element between two existing vertices and
     * return its (the edge's) reference.
     *
     * @param outbound    outbound vertex
     * @param inbound     inbound vertex
     * @param edgeElement the element to store in the new edge
     * @param weight      edge weight
     * @return the reference for the newly created edge
     * @throws InvalidVertexException if <code>outbound</code> or
     *                                <code>inbound</code> are invalid vertices for
     *                                the graph
     * @throws InvalidEdgeException   if there already exists an edge containing
     *                                <code>edgeElement</code> according to the
     *                                equality of
     *                                {@link Object#equals(java.lang.Object)}
     *                                method.
     */
    @Override
    Edge<E, V> insertEdge(Vertex<V, E> outbound, Vertex<V, E> inbound, E edgeElement, double weight)
            throws InvalidVertexException, InvalidEdgeException;

    /**
     * Inserts a new edge with a given element between two existing vertices and
     * return its (the edge's) reference.
     *
     * @param outboundElement outbound vertex's stored element
     * @param inboundElement  inbound vertex's stored element
     * @param edgeElement     element to store in the new edge
     * @return the reference for the newly created edge
     * @throws InvalidVertexException if <code>outboundElement</code> or
     *                                <code>inboundElement</code> are not found in
     *                                any vertices of the graph according to the
     *                                equality of
     *                                {@link Object#equals(java.lang.Object) }
     *                                method.
     * @throws InvalidEdgeException   if there already exists an edge containing
     *                                <code>edgeElement</code> according to the
     *                                equality of
     *                                {@link Object#equals(java.lang.Object) }
     *                                method.
     */
    @Override
    Edge<E, V> insertEdge(V outboundElement, V inboundElement, E edgeElement)
            throws InvalidVertexException, InvalidEdgeException;

    /**
     * Inserts a new edge with a given element between two existing vertices and
     * return its (the edge's) reference.
     *
     * @param outboundElement outbound vertex's stored element
     * @param inboundElement  inbound vertex's stored element
     * @param edgeElement     element to store in the new edge
     * @param weight          edge weight
     * @return the reference for the newly created edge
     * @throws InvalidVertexException if <code>outboundElement</code> or
     *                                <code>inboundElement</code> are not found in
     *                                any vertices of the graph according to the
     *                                equality of
     *                                {@link Object#equals(java.lang.Object) }
     *                                method.
     * @throws InvalidEdgeException   if there already exists an edge containing
     *                                <code>edgeElement</code> according to the
     *                                equality of
     *                                {@link Object#equals(java.lang.Object) }
     *                                method.
     */
    @Override
    Edge<E, V> insertEdge(V outboundElement, V inboundElement, E edgeElement, double weight)
            throws InvalidVertexException, InvalidEdgeException;

    /**
     * Inserts a new edge with a given element between two existing vertices and
     * return its (the edge's) reference.
     *
     * @param outboundElement outbound vertex's stored element
     * @param inboundElement  inbound vertex's stored element
     * @param edgeElement     element to store in the new edge
     * @param weight          edge weight
     * @param properties      element properties
     * @return the reference for the newly created edge
     * @throws InvalidVertexException if <code>outboundElement</code> or
     *                                <code>inboundElement</code> are not found in
     *                                any vertices of the graph according to the
     *                                equality of
     *                                {@link Object#equals(java.lang.Object) }
     *                                method.
     * @throws InvalidEdgeException   if there already exists an edge containing
     *                                <code>edgeElement</code> according to the
     *                                equality of
     *                                {@link Object#equals(java.lang.Object) }
     *                                method.
     */
    @Override
    Edge<E, V> insertEdge(V outboundElement, V inboundElement, E edgeElement, double weight, Map<String, Object> properties)
            throws InvalidVertexException, InvalidEdgeException;

    /**
     * Removes an edge and return its element.
     *
     * @param outbound outbound vertex
     * @param inbound  inbound vertex
     * @return element stored at the removed edge
     * @throws InvalidEdgeException if <code>e</code> is an invalid edge for the
     *                              graph.
     */
    Optional<E> removeEdge(V outbound, V inbound) throws InvalidEdgeException;

}
