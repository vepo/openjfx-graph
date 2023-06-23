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

import static dev.vepo.openjgraph.graph.ElementInspector.getEdgeWeight;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * ADT Graph implementation that stores a collection of edges (and vertices) and
 * where each edge contains the references for the vertices it connects. <br>
 * Does not allow duplicates of stored elements through <b>equals</b> criteria.
 *
 * @param <V> Type of element stored at a vertex
 * @param <E> Type of element stored at an edge
 * 
 * @author brunomnsilva
 */
class GraphEdgeList<V, E> implements Graph<V, E> {

    /*
     * inner classes are defined at the end of the class, so are the auxiliary
     * methods
     */
    private final Map<V, Vertex<V, E>> vertices;
    private final Map<E, Edge<E, V>> edges;

    /**
     * Default constructor that initializes an empty graph.
     */
    public GraphEdgeList() {
        this.vertices = new HashMap<>();
        this.edges = new HashMap<>();
    }

    @Override
    public int numVertices() { return vertices.size(); }

    @Override
    public int numEdges() { return edges.size(); }

    @Override
    public Collection<Vertex<V, E>> vertices() { return new ArrayList<>(vertices.values()); }

    @Override
    public Collection<Edge<E, V>> edges() { return new ArrayList<>(edges.values()); }

    @Override
    public boolean hasEdge(E e) { return existsEdgeWith(e); }

    @Override
    public Collection<Edge<E, V>> incidentEdges(Vertex<V, E> v) throws InvalidVertexException {

        checkVertex(v);

        List<Edge<E, V>> incidentEdges = new ArrayList<>();
        for (Edge<E, V> edge : edges.values()) {
            if (edge.contains(v)) {
                incidentEdges.add(edge);
            }
        }

        return incidentEdges;
    }

    @Override
    public Vertex<V, E> opposite(Vertex<V, E> v, Edge<E, V> e) throws InvalidVertexException, InvalidEdgeException {
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
    public synchronized boolean areAdjacent(Vertex<V, E> u, Vertex<V, E> v) throws InvalidVertexException {
        // we allow loops, so we do not check if u == v
        checkVertex(v);
        checkVertex(u);

        /* find and edge that contains both u and v */
        for (Edge<E, V> edge : edges.values()) {
            if (edge.contains(u) && edge.contains(v)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean areAdjacent(V u, V v) throws InvalidVertexException { return areAdjacent(vertices.get(u), vertices.get(v)); }

    @Override
    public synchronized Vertex<V, E> insertVertex(V vElement) throws InvalidVertexException {
        if (existsVertexWith(vElement)) {
            throw new InvalidVertexException("There's already a vertex with this element.");
        }

        var newVertex = new Vertex<V, E>(vElement, this);

        vertices.put(vElement, newVertex);

        return newVertex;
    }

    @Override
    public synchronized Edge<E, V> insertEdge(Vertex<V, E> u, Vertex<V, E> v, E edgeElement) throws InvalidVertexException, InvalidEdgeException {
        return insertEdge(u, v, edgeElement, getEdgeWeight(edgeElement));
    }

    @Override
    public synchronized Edge<E, V> insertEdge(Vertex<V, E> u, Vertex<V, E> v, E edgeElement, double weight) throws InvalidVertexException, InvalidEdgeException {

        if (existsEdgeWith(edgeElement)) {
            throw new InvalidEdgeException("There's already an edge with this element.");
        }

        Vertex<V, E> outVertex = checkVertex(u);
        Vertex<V, E> inVertex = checkVertex(v);

        Edge<E, V> newEdge = new Edge<E, V>(outVertex, inVertex, false, weight, edgeElement);
        edges.put(edgeElement, newEdge);
        return newEdge;
    }

    @Override
    public synchronized Edge<E, V> insertEdge(V vElement1, V vElement2, E edgeElement, double weight) throws InvalidVertexException, InvalidEdgeException { // TODO
        // Auto-generated
        // method stub
        if (existsEdgeWith(edgeElement)) {
            throw new InvalidEdgeException("There's already an edge with this element.");
        }

        if (!existsVertexWith(vElement1)) {
            throw new InvalidVertexException("No vertex contains " + vElement1);
        }
        if (!existsVertexWith(vElement2)) {
            throw new InvalidVertexException("No vertex contains " + vElement2);
        }

        var outVertex = vertexOf(vElement1);
        var inVertex = vertexOf(vElement2);

        var newEdge = new Edge<E, V>(outVertex, inVertex, false, weight, edgeElement);

        edges.put(edgeElement, newEdge);

        return newEdge;
    }

    @Override
    public synchronized Edge<E, V> insertEdge(V vElement1, V vElement2, E edgeElement) throws InvalidVertexException, InvalidEdgeException {
        return insertEdge(vElement1, vElement2, edgeElement, getEdgeWeight(edgeElement));
    }

    @Override
    public synchronized V removeVertex(Vertex<V, E> v) throws InvalidVertexException {
        checkVertex(v);

        V element = v.element();

        // remove incident edges
        Iterable<Edge<E, V>> incidentEdges = incidentEdges(v);
        for (Edge<E, V> edge : incidentEdges) {
            edges.remove(edge.element());
        }

        vertices.remove(v.element());

        return element;
    }

    @Override
    public Optional<E> removeEdge(V u, V v) throws InvalidEdgeException {
        return edges.values()
                    .stream()
                    .filter(edge -> (edge.vertexA().element().equals(u) && edge.vertexB().element().equals(v))
                            || (edge.vertexA().element().equals(v) && edge.vertexB().element().equals(u)))
                    .findFirst()
                    .map(this::removeEdge);
    }

    @Override
    public synchronized E removeEdge(Edge<E, V> e) throws InvalidEdgeException {
        checkEdge(e);

        E element = e.element();
        edges.remove(e.element());

        return element;
    }

    @Override
    public V replace(Vertex<V, E> v, V newElement) throws InvalidVertexException {
        if (existsVertexWith(newElement)) {
            throw new InvalidVertexException("There's already a vertex with this element.");
        }

        var vertex = checkVertex(v);
        var newVertex = new Vertex<V, E>(newElement, this);
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
                     edges.replace(e.element(), new Edge<E, V>(newVertex, e.vertexB(), e.directed(), e.weight(), e.element()));
                 } else {
                     edges.replace(e.element(), new Edge<E, V>(e.vertexA(), newVertex, e.directed(), e.weight(), e.element()));
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

    @Override
    public boolean hasVertex(Vertex<V, E> source) { return vertices.containsValue(source); }

    @Override
    public Optional<Vertex<V, E>> vertex(V value) { return vertices.containsKey(value) ? Optional.of(vertices.get(value)) : Optional.empty(); }

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
     * Checks whether a given vertex is valid (i.e., not <i>null</i>) and belongs to
     * this graph
     *
     * @param v vertex to check
     * @return the reference of the vertex
     * @throws InvalidVertexException if the vertex is invalid
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
                    .filter(e -> (e.vertexA().equals(vertexA) && e.vertexB().equals(vertexB)) ||
                            e.vertexA().equals(vertexB) && e.vertexB().equals(vertexA))
                    .sorted(Comparator.comparingDouble(Edge::weight))
                    .findFirst();
    }
}
