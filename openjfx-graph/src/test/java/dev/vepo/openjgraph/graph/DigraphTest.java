package dev.vepo.openjgraph.graph;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class DigraphTest {

    @Test
    @DisplayName("It should be possible to create a Digraph")
    void simpleTest() {
        var graph = Digraph.<String, String>newDigraph();
        graph.insertVertex("A");
        graph.insertVertex("B");
        graph.insertEdge("A", "B", "PATH");

        assertEquals(2, graph.numVertices(), "It should have 2 vertices");
        assertEquals(1, graph.numEdges(), "It should have 1 edge");
    }

    @Test
    @DisplayName("It should test if two vertex are adjacentes")
    void areAdjacentTest() {
        var graph = Digraph.<String, String>newDigraph();
        graph.insertVertex("A");
        graph.insertVertex("B");
        graph.insertVertex("C");
        graph.insertEdge("A", "B", "A - B");
        graph.insertEdge("B", "C", "B - C");
        graph.insertEdge("C", "A", "C - A");

        assertEquals(3, graph.numVertices(), "It should have 2 vertices");
        assertEquals(3, graph.numEdges(), "It should have 1 edge");
        assertTrue(graph.areAdjacent("A", "B"));
        assertFalse(graph.areAdjacent("B", "A"));
    }

    @Test
    @DisplayName("It should avoid adding duplicated edge elements")
    void duplicatedEdgeElementsTest() {
        var graph = Digraph.<String, String>newDigraph();
        graph.insertVertex("A");
        graph.insertVertex("B");
        graph.insertVertex("C");
        graph.insertEdge("A", "B", "A - B");
        assertThrows(InvalidEdgeException.class, () -> graph.insertEdge("B", "C", "A - B"));
        assertThrows(InvalidVertexException.class, () -> graph.insertEdge("X", "C", "X- B"));
        assertThrows(InvalidVertexException.class, () -> graph.insertEdge("B", "X", "B - X"));
    }

    @Test
    @DisplayName("It should avoid adding duplicated edge")
    void validateVertexTest() {
        var graph = Digraph.<String, String>newDigraph();
        graph.insertVertex("A");
        graph.insertVertex("B");
        graph.insertVertex("C");
        assertThrows(InvalidVertexException.class, () -> graph.insertEdge("X", "C", "X- B"));
        assertThrows(InvalidVertexException.class, () -> graph.insertEdge("B", "X", "B - X"));
        assertThrows(InvalidVertexException.class, () -> graph.insertEdge(null, "X", "B - X"));
        assertThrows(InvalidVertexException.class, () -> graph.insertEdge(new Vertex<>("X"), new Vertex<>("C"), "X- B"));
        assertThrows(InvalidVertexException.class, () -> graph.insertEdge(new Vertex<>("B"), new Vertex<>("X"), "B - X"));
        assertThrows(InvalidVertexException.class, () -> graph.insertEdge(null, new Vertex<>("X"), "B - X"));
    }
}
