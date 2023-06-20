package dev.vepo.openjgraph.graph;

public interface Subgraph<V, E> {

    boolean contains(Vertex<V, E> vertex);

    boolean contains(Edge<E, V> edge);

}
