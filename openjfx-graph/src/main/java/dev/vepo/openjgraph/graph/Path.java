package dev.vepo.openjgraph.graph;

import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

public class Path<V, E> implements Subgraph<V, E> {

    public static <V, E> Path<V, E> startFrom(Vertex<V, E> source) {
        return new Path<>(source);
    }

    private final Deque<Vertex<V, E>> vertices;
    private final Deque<Edge<E, V>> edges;

    public Path(Vertex<V, E> source) {
        this.vertices = new LinkedList<>();
        this.edges = new LinkedList<>();
        this.vertices.addFirst(source);
    }

    private Path(Deque<Vertex<V, E>> vertices,
                 Deque<Edge<E, V>> edges) {
        this.vertices = vertices;
        this.edges = edges;
    }

    public Vertex<V, E> tail() {
        return vertices.peekLast();
    }

    public Set<Vertex<V, E>> vertices() {
        return new HashSet<>(vertices);
    }

    @Override
    public boolean contains(Vertex<V, E> vertex) {
        return vertices.contains(vertex);
    }

    @Override
    public boolean contains(Edge<E, V> edge) {
        return edges.contains(edge);
    }

    public boolean endsWith(Vertex<V, E> destiny) {
        return tail().equals(destiny);
    }

    public double distance() {
        return edges.stream()
                    .sequential()
                    .mapToDouble(Edge::weight)
                    .sum();
    }

    public Stream<Vertex<V, E>> accessibleVertices() {
        var lastVertex = vertices.peekLast();
        return lastVertex.graph()
                         .incidentEdges(lastVertex)
                         .stream()
                         .filter(e -> !e.directed() || e.vertexA().equals(lastVertex))
                         .map(e -> e.vertexA().equals(lastVertex) ? e.vertexB() : e.vertexA());
    }

    public Path<V, E> walk(Edge<E, V> edge) {
        Vertex<V, E> tail = tail();
        if ((edge.directed() && !edge.vertexA().equals(tail)) ||
                (!edge.directed() && !edge.contains(tail))) {
            throw new IllegalStateException(String.format("Cannot walk edge %s from %s", edge, tail));
        }
        var newEdges = new LinkedList<>(this.edges);
        var newVertices = new LinkedList<>(this.vertices);
        newEdges.addLast(edge);
        newVertices.addLast(tail.graph().opposite(tail, edge));
        return new Path<>(newVertices, newEdges);
    }

    @Override
    public int hashCode() {
        return Objects.hash(vertices, edges);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null || getClass() != obj.getClass()) {
            return false;
        } else {
            var other = (Path<V, E>) obj;
            return Objects.equals(vertices, other.vertices) && Objects.equals(edges, other.edges);
        }
    }

    @Override
    public String toString() {
        return String.format("Path[vertices=%s, edges=%s]", vertices, edges);
    }

}
