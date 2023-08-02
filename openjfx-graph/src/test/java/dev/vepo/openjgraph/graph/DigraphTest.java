package dev.vepo.openjgraph.graph;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.data.Percentage.withPercentage;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

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

        assertEquals(3, graph.numVertices(), "It should have 3 vertices");
        assertEquals(3, graph.numEdges(), "It should have 3 edge");
        assertTrue(graph.areAdjacent("A", "B"));
        assertFalse(graph.areAdjacent("B", "A"));
    }

    @Test
    @DisplayName("It should test if one vertex can be removed")
    void removeVertexTest() {
        var graph = Digraph.<String, String>newDigraph();
        graph.insertVertex("A");
        graph.insertVertex("B");
        graph.insertVertex("C");
        graph.insertEdge("A", "B", "A - B");
        graph.insertEdge("B", "C", "B - C");
        graph.insertEdge("C", "A", "C - A");

        assertEquals(3, graph.numVertices(), "It should have 3 vertices");
        assertEquals(3, graph.numEdges(), "It should have 3 edge");
        assertTrue(graph.areAdjacent("A", "B"));
        assertFalse(graph.areAdjacent("B", "A"));

        graph.removeVertex(new Vertex<>("C", graph));

        assertEquals(2, graph.numVertices(), "It should have 2 vertices");
        assertEquals(1, graph.numEdges(), "It should have 1 edge");
    }

    @Test
    @DisplayName("It should test if one edge can be removed")
    void removeEdgeTest() {
        var graph = Digraph.<String, String>newDigraph();
        graph.insertVertex("A");
        graph.insertVertex("B");
        graph.insertVertex("C");
        graph.insertEdge("A", "B", "A - B");
        graph.insertEdge("B", "C", "B - C");
        graph.insertEdge("C", "A", "C - A");

        assertEquals(3, graph.numVertices(), "It should have 3 vertices");
        assertEquals(3, graph.numEdges(), "It should have 3 edge");
        assertTrue(graph.areAdjacent("A", "B"));
        assertFalse(graph.areAdjacent("B", "A"));

        graph.removeEdge("A", "B");

        assertFalse(graph.areAdjacent("A", "B"));
        assertFalse(graph.areAdjacent("B", "A"));
        assertEquals(3, graph.numVertices(), "It should have 2 vertices");
        assertEquals(2, graph.numEdges(), "It should have 1 edge");
    }

    @Test
    @DisplayName("It should be possible to replace a vertex")
    void replaceVertexTest() {
        var graph = Digraph.<String, String>newDigraph();
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
        var graph = Digraph.<String, String>newDigraph();
        graph.insertVertex("A");
        graph.insertVertex("B");
        graph.insertVertex("C");
        graph.insertEdge("A", "B", "A - B");
        graph.insertEdge("B", "C", "B - C");
        var edge = graph.insertEdge("C", "A", "C - A");
        graph.replace(edge, "X - A");

        var newEdge = graph.incidentEdges(new Vertex<>("C", graph))
                           .stream()
                           .filter(e -> e.vertexB().element().equals("A"))
                           .findFirst();
        assertTrue(newEdge.isPresent());
        assertEquals("X - A", newEdge.get().element());

        assertEquals(3, graph.numVertices(), "It should have 3 vertices");
        assertEquals(3, graph.numEdges(), "It should have 3 edge");
        assertTrue(graph.areAdjacent("A", "B"));
        assertTrue(graph.areAdjacent("C", "A"));
    }

    @Test
    @DisplayName("It should be possible find a vertex")
    void vertexFindTest() {
        var graph = Digraph.<String, String>newDigraph();
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
    @DisplayName("It should be possible to replace a edge")
    void oppositeTest() {
        var graph = Digraph.<String, String>newDigraph();
        var va = graph.insertVertex("A");
        var vb = graph.insertVertex("B");
        var vc = graph.insertVertex("C");
        graph.insertEdge(va, vb, "A - B");
        graph.insertEdge(vb, vc, "B - C");
        graph.insertEdge(vc, va, "C - A");
        assertEquals(new Vertex<>("B", graph), graph.opposite("A", "A - B"));
        assertEquals(new Vertex<>("A", graph), graph.opposite("B", "A - B"));
        assertNull(graph.opposite("C", "A - B"));
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
        assertThrows(InvalidVertexException.class,
                     () -> graph.insertEdge(new Vertex<>("X", graph), new Vertex<>("C", graph), "X- B"));
        assertThrows(InvalidVertexException.class,
                     () -> graph.insertEdge(new Vertex<>("B", graph), new Vertex<>("X", graph), "B - X"));
        assertThrows(InvalidVertexException.class, () -> graph.insertEdge(null, new Vertex<>("X", graph), "B - X"));
    }

    @Test
    @DisplayName("Dijkstra - Long Path with shortened distance")
    void dijkstraLongPathShortDistanceTest() {
        // ____B_-_C_-_D_______
        // A_<_________________>_I
        // ____E_-_F_-_G_-_H___
        var graph = Digraph.<String, String>newDigraph();
        var va = graph.insertVertex("A");
        graph.insertVertex("B");
        graph.insertVertex("C");
        graph.insertVertex("D");
        graph.insertVertex("E");
        graph.insertVertex("F");
        graph.insertVertex("G");
        graph.insertVertex("H");
        var vi = graph.insertVertex("I");

        graph.insertEdge("A", "B", "A-B", 2.0);
        graph.insertEdge("B", "C", "B-C", 2.0);
        graph.insertEdge("C", "D", "C-D", 2.0);
        graph.insertEdge("D", "I", "D-I", 2.0);

        var edgeA2E = graph.insertEdge("A", "E", "A-E", 0.1);
        var edgeE2F = graph.insertEdge("E", "F", "E-F", 0.1);
        var edgeF2G = graph.insertEdge("F", "G", "F-G", 0.1);
        var edgeG2H = graph.insertEdge("G", "H", "G-H", 0.1);
        var edgeH2I = graph.insertEdge("H", "I", "H-I", 0.1);
        assertThat(graph.dijkstra(va, vi)).isEqualTo(Path.startFrom(va)
                                                         .walk(edgeA2E)
                                                         .walk(edgeE2F)
                                                         .walk(edgeF2G)
                                                         .walk(edgeG2H)
                                                         .walk(edgeH2I));
    }

    @Test
    @DisplayName("Dijkstra - Invalid Nodes")
    void dijkstraInvalidNodesTest() {
        // ____B
        // A_<___>_D_-_E
        // ____C
        var graph = Digraph.<String, String>newDigraph();
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

        var nGraph = Graph.<String, String>newGraph();
        var xVertex = nGraph.insertVertex("X");

        assertThatThrownBy(() -> graph.dijkstra(xVertex, va)).isInstanceOf(InvalidVertexException.class);
        assertThatThrownBy(() -> graph.dijkstra(va, xVertex)).isInstanceOf(InvalidVertexException.class);
    }

    @Test
    @DisplayName("Random")
    void randomTest() {
        var graph = Digraph.<String, String>random(1000,
                                                   0.1,
                                                   i -> String.format("%02d", i),
                                                   (i, j) -> String.format("%s-%s", i, j));
        assertEquals(1000, graph.numVertices(), "It should have 1000 vertex");
        assertThat(graph.numEdges()).as("It should have approximately 100 edges")
                                    .isCloseTo((int) ((1000 * 1000 / 2) * 0.1), withPercentage(10));

    }
}
