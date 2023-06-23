/*
 * The MIT License
 *
 * JavaFXSmartGraph | Copyright 2023  brunomnsilva@gmail.com
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

import static dev.vepo.openjgraph.graph.ElementInspector.getEdgeWeight;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Implementation of a digraph that adheres to the {@link Digraph} interface.
 * <br>
 * Does not allow duplicates of stored elements through <b>equals</b> criteria.
 * <br>
 * 
 * @param <V> Type of element stored at a vertex
 * @param <E> Type of element stored at an edge
 * 
 * @author brunomnsilva
 */
class DigraphEdgeList<V, E> implements Digraph<V, E> {

    /*
     * inner classes are defined at the end of the class, so are the auxiliary
     * methods
     */
    private final Map<V, Vertex<V, E>> vertices;
    private final Map<E, Edge<E, V>> edges;

    public DigraphEdgeList() {
        this.vertices = new HashMap<>();
        this.edges = new HashMap<>();
    }

    @Override
    public boolean hasVertex(Vertex<V, E> source) { return vertices.containsValue(source); }

    @Override
    public synchronized Collection<Edge<E, V>> incidentEdges(Vertex<V, E> inbound) throws InvalidVertexException {
        checkVertex(inbound);

        List<Edge<E, V>> incidentEdges = new ArrayList<>();
        for (Edge<E, V> edge : edges.values()) {

            if (edge.vertexA().equals(inbound)) {
                incidentEdges.add(edge);
            }
        }
        return incidentEdges;
    }

    @Override
    public synchronized Collection<Edge<E, V>> outboundEdges(Vertex<V, E> outbound) throws InvalidVertexException {
        checkVertex(outbound);

        List<Edge<E, V>> outboundEdges = new ArrayList<>();
        for (Edge<E, V> edge : edges.values()) {

            if (edge.vertexB().equals(outbound)) {
                outboundEdges.add(edge);
            }
        }
        return outboundEdges;
    }

    @Override
    public boolean areAdjacent(Vertex<V, E> outbound, Vertex<V, E> inbound) throws InvalidVertexException {
        // we allow loops, so we do not check if outbound == inbound
        checkVertex(outbound);
        checkVertex(inbound);

        /* find and edge that goes outbound ---> inbound */
        for (Edge<E, V> edge : edges.values()) {
            if (edge.vertexA().equals(outbound) && edge.vertexB().equals(inbound)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean areAdjacent(V outbound, V inbound) throws InvalidVertexException { return areAdjacent(vertices.get(outbound), vertices.get(inbound)); }

    @Override
    public synchronized Edge<E, V> insertEdge(Vertex<V, E> outbound, Vertex<V, E> inbound, E edgeElement) throws InvalidVertexException, InvalidEdgeException {
        return insertEdge(outbound, inbound, edgeElement, getEdgeWeight(edgeElement));
    }

    @Override
    public synchronized Edge<E, V> insertEdge(Vertex<V, E> outbound, Vertex<V, E> inbound, E edgeElement, double weight)
            throws InvalidVertexException, InvalidEdgeException {
        var outVertex = checkVertex(outbound);
        var inVertex = checkVertex(inbound);

        var newEdge = new Edge<>(outVertex, inVertex, true, weight, edgeElement);
        edges.put(edgeElement, newEdge);

        return newEdge;
    }

    @Override
    public synchronized Edge<E, V> insertEdge(V outboundElement, V inboundElement, E edgeElement, double weight)
            throws InvalidVertexException, InvalidEdgeException {
        if (existsEdgeWith(edgeElement)) {
            throw new InvalidEdgeException("There's already an edge with this element.");
        }

        if (!existsVertexWith(outboundElement)) {
            throw new InvalidVertexException("No vertex contains " + outboundElement);
        }
        if (!existsVertexWith(inboundElement)) {
            throw new InvalidVertexException("No vertex contains " + inboundElement);
        }

        var outVertex = vertexOf(outboundElement);
        var inVertex = vertexOf(inboundElement);

        var newEdge = new Edge<E, V>(outVertex, inVertex, true, weight, edgeElement);

        edges.put(edgeElement, newEdge);

        return newEdge;
    }

    @Override
    public synchronized Edge<E, V> insertEdge(V outboundElement, V inboundElement, E edgeElement) throws InvalidVertexException, InvalidEdgeException {
        return insertEdge(outboundElement, inboundElement, edgeElement, getEdgeWeight(edgeElement));
    }

    @Override
    public int numVertices() { return vertices.size(); }

    @Override
    public int numEdges() { return edges.size(); }

    @Override
    public boolean hasEdge(E e) { return existsEdgeWith(e); }

    @Override
    public synchronized Collection<Vertex<V, E>> vertices() { return new ArrayList<>(vertices.values()); }

    @Override
    public synchronized Collection<Edge<E, V>> edges() { return new ArrayList<>(edges.values()); }

    @Override
    public synchronized Vertex<V, E> opposite(Vertex<V, E> v, Edge<E, V> e)
            throws InvalidVertexException, InvalidEdgeException {
        checkVertex(v);
        Edge<E, V> edge = checkEdge(e);

        if (!edge.contains(v)) {
            return null; /* this edge does not connect vertex v */
        }

        if (edge.vertexA().equals(v)) {
            return edge.vertexB();
        } else {
            return edge.vertexA();
        }

    }

    @Override
    public Vertex<V, E> opposite(V v, E e) throws InvalidVertexException, InvalidEdgeException { return opposite(vertexOf(v), edges.get(e)); }

    @Override
    public synchronized Vertex<V, E> insertVertex(V vElement) throws InvalidVertexException {
        if (existsVertexWith(vElement)) {
            throw new InvalidVertexException("There's already a vertex with this element.");
        }

        var newVertex = new Vertex<>(vElement, this);

        vertices.put(vElement, newVertex);

        return newVertex;
    }

    @Override
    public synchronized V removeVertex(Vertex<V, E> v) throws InvalidVertexException {
        checkVertex(v);

        V element = v.element();

        // remove incident edges
        Collection<Edge<E, V>> inOutEdges = incidentEdges(v);
        inOutEdges.addAll(outboundEdges(v));

        for (Edge<E, V> edge : inOutEdges) {
            edges.remove(edge.element());
        }

        vertices.remove(v.element());

        return element;
    }

    @Override
    public synchronized E removeEdge(Edge<E, V> e) throws InvalidEdgeException {
        checkEdge(e);

        E element = e.element();
        edges.remove(e.element());

        return element;
    }

    @Override
    public Optional<E> removeEdge(V outbound, V inbound) throws InvalidEdgeException {
        return edges.values()
                    .stream()
                    .filter(edge -> edge.vertexA().element().equals(outbound) && edge.vertexB().element().equals(inbound))
                    .findFirst()
                    .map(this::removeEdge);
    }

    @Override
    public V replace(Vertex<V, E> v, V newElement) throws InvalidVertexException {
        if (existsVertexWith(newElement)) {
            throw new InvalidVertexException("There's already a vertex with this element.");
        }

        var vertex = checkVertex(v);
        var newVertex = new Vertex<>(newElement, this);
        V oldElement = vertex.element();

        vertices.remove(oldElement);
        vertices.put(newElement, newVertex);
        edges.values()
             .stream()
             .filter(e -> e.contains(vertex))
             .toList()
             .forEach(e -> {
                 edges.remove(e);
                 if (e.vertexA().equals(vertex)) {
                     edges.replace(e.element(), new Edge<>(newVertex, e.vertexB(), e.directed(), e.weight(), e.element()));
                 } else {
                     edges.replace(e.element(), new Edge<>(e.vertexA(), newVertex, e.directed(), e.weight(), e.element()));
                 }
             });

        return oldElement;
    }

    @Override
    public E replace(Edge<E, V> e, E newElement) throws InvalidEdgeException {
        if (existsEdgeWith(newElement)) {
            throw new InvalidEdgeException("There's already an edge with this element.");
        }

        var edge = checkEdge(e);

        var newEdge = new Edge<>(e.vertexA(), e.vertexB(), e.directed(), e.weight(), newElement);

        edges.remove(e.element());
        edges.put(newElement, newEdge);

        return edge.element();
    }

    @Override
    public Optional<Vertex<V, E>> vertex(V value) { return vertices.containsKey(value) ? Optional.of(vertices.get(value)) : Optional.empty(); }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(String.format("Graph with %d vertices and %d edges:%n", numVertices(), numEdges()));

        sb.append("--- Vertices: \n");
        for (Vertex<V, E> v : vertices.values()) {
            sb.append("\t").append(v.toString()).append("\n");
        }
        sb.append("\n--- Edges: \n");
        for (Edge<E, V> e : edges.values()) {
            sb.append("\t").append(e.toString()).append("\n");
        }
        return sb.toString();
    }

    private Vertex<V, E> vertexOf(V vElement) {
        for (Vertex<V, E> v : vertices.values()) {
            if (v.element().equals(vElement)) {
                return v;
            }
        }
        return null;
    }

    private boolean existsVertexWith(V vElement) { return vertices.containsKey(vElement); }

    private boolean existsEdgeWith(E edgeElement) { return edges.containsKey(edgeElement); }

    /**
     * Checks whether a given vertex is valid and belongs to this graph.
     *
     * @param v the vertex to check
     * @return the reference of the vertex, with cast to the underlying
     *         implementation of {@link Vertex}
     * @throws InvalidVertexException if the vertex is <code>null</code> or does not
     *                                belong to this graph
     */
    private Vertex<V, E> checkVertex(Vertex<V, E> v) throws InvalidVertexException {
        if (v == null) {
            throw new InvalidVertexException("Null vertex.");
        }

        Vertex<V, E> vertex;
        try {
            vertex = (Vertex<V, E>) v;
        } catch (ClassCastException e) {
            throw new InvalidVertexException("Not a vertex.");
        }

        if (!vertices.containsKey(vertex.element())) {
            throw new InvalidVertexException("Vertex does not belong to this graph.");
        }

        return vertex;
    }

    private Edge<E, V> checkEdge(Edge<E, V> e) throws InvalidEdgeException {
        if (e == null) {
            throw new InvalidEdgeException("Null edge.");
        }

        Edge<E, V> edge;
        try {
            edge = e;
        } catch (ClassCastException ex) {
            throw new InvalidVertexException("Not an adge.");
        }

        if (!edges.containsKey(edge.element())) {
            throw new InvalidEdgeException("Edge does not belong to this graph.");
        }

        return edge;
    }

    public Optional<Edge<E, V>> edge(Vertex<V, E> vertexA, Vertex<V, E> vertexB) {
        return edges.values()
                    .stream()
                    .filter(e -> e.vertexA().equals(vertexA) && e.vertexB().equals(vertexB))
                    .sorted(Comparator.comparingDouble(Edge::weight))
                    .findFirst();
    }

}
