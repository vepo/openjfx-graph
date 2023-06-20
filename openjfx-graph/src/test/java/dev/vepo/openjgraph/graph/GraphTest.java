package dev.vepo.openjgraph.graph;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

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

        assertEquals(3, graph.numVertices(), "It should have 3 vertices");
        assertEquals(3, graph.numEdges(), "It should have 3 edge");
        assertTrue(graph.areAdjacent("A", "B"));
        assertTrue(graph.areAdjacent("B", "A"));
    }

    @Test
    @DisplayName("It should test if one vertex can be removed")
    void removeVertexTest() {
        var graph = Graph.<String, String>newGraph();
        graph.insertVertex("A");
        graph.insertVertex("B");
        graph.insertVertex("C");
        graph.insertEdge("A", "B", "A - B");
        graph.insertEdge("B", "C", "B - C");
        graph.insertEdge("C", "A", "C - A");

        assertEquals(3, graph.numVertices(), "It should have 3 vertices");
        assertEquals(3, graph.numEdges(), "It should have 3 edge");
        assertTrue(graph.areAdjacent("A", "B"));
        assertTrue(graph.areAdjacent("B", "A"));

        graph.removeVertex(new Vertex<>("C", graph));

        assertEquals(2, graph.numVertices(), "It should have 2 vertices");
        assertEquals(1, graph.numEdges(), "It should have 1 edge");
    }

    @Test
    @DisplayName("It should test if one edge can be removed")
    void removeEdgeTest() {
        var graph = Graph.<String, String>newGraph();
        graph.insertVertex("A");
        graph.insertVertex("B");
        graph.insertVertex("C");
        graph.insertEdge("A", "B", "A - B");
        graph.insertEdge("B", "C", "B - C");
        graph.insertEdge("C", "A", "C - A");

        assertEquals(3, graph.numVertices(), "It should have 3 vertices");
        assertEquals(3, graph.numEdges(), "It should have 3 edge");
        assertTrue(graph.areAdjacent("A", "B"));
        assertTrue(graph.areAdjacent("B", "A"));

        graph.removeEdge("A", "B");

        assertFalse(graph.areAdjacent("A", "B"));
        assertFalse(graph.areAdjacent("B", "A"));
        assertEquals(3, graph.numVertices(), "It should have 2 vertices");
        assertEquals(2, graph.numEdges(), "It should have 1 edge");
    }

    @Test
    @DisplayName("It should be possible to replace a vertex")
    void replaceVertexTest() {
        var graph = Graph.<String, String>newGraph();
        graph.insertVertex("A");
        graph.insertVertex("B");
        graph.insertVertex("C");
        graph.insertEdge("A", "B", "A - B");
        graph.insertEdge("B", "C", "B - C");
        graph.insertEdge("C", "A", "C - A");
        graph.replace(new Vertex<>("C", graph), "D");
        assertEquals(3, graph.numVertices(), "It should have 3 vertices");
        assertEquals(3, graph.numEdges(), "It should have 3 edge");
        assertTrue(graph.areAdjacent("A", "B"));
        assertTrue(graph.areAdjacent("D", "A"));
    }

    @Test
    @DisplayName("It should be possible to replace a edge")
    void replaceEdgeTest() {
        var graph = Graph.<String, String>newGraph();
        graph.insertVertex("A");
        graph.insertVertex("B");
        graph.insertVertex("C");
        graph.insertEdge("A", "B", "A - B");
        graph.insertEdge("B", "C", "B - C");
        var edge = graph.insertEdge("C", "A", "C - A");
        graph.replace(edge, "X - A");

        var newEdge = graph.incidentEdges(new Vertex<>("A", graph))
                           .stream()
                           .filter(e -> e.vertexA().element().equals("C"))
                           .findFirst();
        assertTrue(newEdge.isPresent());
        assertEquals("X - A", newEdge.get().element());

        assertEquals(3, graph.numVertices(), "It should have 3 vertices");
        assertEquals(3, graph.numEdges(), "It should have 3 edge");
        assertTrue(graph.areAdjacent("A", "B"));
        assertTrue(graph.areAdjacent("C", "A"));
    }

    @Test
    @DisplayName("It should be possible to replace a edge")
    void oppositeTest() {
        var graph = Graph.<String, String>newGraph();
        graph.insertVertex("A");
        graph.insertVertex("B");
        graph.insertVertex("C");
        graph.insertEdge("A", "B", "A - B");
        graph.insertEdge("B", "C", "B - C");
        graph.insertEdge("C", "A", "C - A");
        assertEquals(new Vertex<>("B", graph), graph.opposite("A", "A - B"));
        assertEquals(new Vertex<>("A", graph), graph.opposite("B", "A - B"));
        assertNull(graph.opposite("C", "A - B"));
    }

    @Test
    @DisplayName("It should be possible find a vertex")
    void vertexFindTest() {
        var graph = Graph.<String, String>newGraph();
        graph.insertVertex("A");
        graph.insertVertex("B");
        graph.insertVertex("C");
        graph.insertEdge("A", "B", "A - B");
        graph.insertEdge("B", "C", "B - C");
        graph.insertEdge("C", "A", "C - A");
        assertEquals(Optional.of(new Vertex<>("A", graph)), graph.vertex("A"));
        assertEquals(Optional.empty(), graph.vertex("D"));
    }

    @Test
    @DisplayName("It should avoid adding duplicated edge elements")
    void duplicatedEdgeElementsTest() {
        var graph = Graph.<String, String>newGraph();
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
        var graph = Graph.<String, String>newGraph();
        graph.insertVertex("A");
        graph.insertVertex("B");
        graph.insertVertex("C");
        assertThrows(InvalidVertexException.class, () -> graph.insertEdge("X", "C", "X- B"));
        assertThrows(InvalidVertexException.class, () -> graph.insertEdge("B", "X", "B - X"));
        assertThrows(InvalidVertexException.class, () -> graph.insertEdge(null, "X", "B - X"));
        assertThrows(InvalidVertexException.class, () -> graph.insertEdge(new Vertex<>("X", graph), new Vertex<>("C", graph), "X- B"));
        assertThrows(InvalidVertexException.class, () -> graph.insertEdge(new Vertex<>("B", graph), new Vertex<>("X", graph), "B - X"));
        assertThrows(InvalidVertexException.class, () -> graph.insertEdge(null, new Vertex<>("X", graph), "B - X"));
    }

    @Test
    void dijkstraTest() {
        // ____B
        // A_<___>_D_-_E
        // ____C
        var graph = Graph.<String, String>newGraph();
        var va = graph.insertVertex("A");
        graph.insertVertex("B");
        graph.insertVertex("C");
        graph.insertVertex("D");
        var ve = graph.insertVertex("E");
        graph.insertEdge("A", "B", "A - B", 1.0);
        var edgeA2C = graph.insertEdge("A", "C", "A - C", 0.9);
        graph.insertEdge("B", "D", "B - D", 1.0);
        var edgeC2D = graph.insertEdge("C", "D", "C - D", 1.0);
        var edgeD2E = graph.insertEdge("D", "E", "D - E", 1.0);
        assertEquals(new Path<>(va).walk(edgeA2C).walk(edgeC2D).walk(edgeD2E), graph.dijkstra(va, ve));
    }
}
