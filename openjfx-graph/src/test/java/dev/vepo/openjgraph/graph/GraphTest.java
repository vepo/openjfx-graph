package dev.vepo.openjgraph.graph;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class GraphTest {

    @Test
    @DisplayName("It should be possible to create a Graph")
    void simpleTest() {
        var graph = Graph.<String, String>newGraph();
        graph.insertVertex("A");
        graph.insertVertex("B");
        graph.insertEdge("A", "B", "PATH");

        assertEquals(2, graph.numVertices(), "It should have 2 vertices");
        assertEquals(1, graph.numEdges(), "It should have 1 edge");
    }

    @Test
    @DisplayName("It should test if two vertex are adjacentes")
    void areAdjacentTest() {
        var graph = Graph.<String, String>newGraph();
        graph.insertVertex("A");
        graph.insertVertex("B");
        graph.insertVertex("C");
        graph.insertEdge("A", "B", "A - B");
        graph.insertEdge("B", "C", "B - C");
        graph.insertEdge("C", "A", "C - A");

        assertEquals(3, graph.numVertices(), "It should have 2 vertices");
        assertEquals(3, graph.numEdges(), "It should have 1 edge");
        assertTrue(graph.areAdjacent("A", "B"));
        assertTrue(graph.areAdjacent("B", "A"));
    }
}
