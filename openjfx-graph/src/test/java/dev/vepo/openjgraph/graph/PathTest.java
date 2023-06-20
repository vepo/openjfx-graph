package dev.vepo.openjgraph.graph;

import static java.util.stream.Collectors.toSet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

public class PathTest {
    @Test
    void accessibleVerticesTest() {
        var graph = graph();
        var vertexA = graph.vertex("A").get();
        var vertexB = graph.vertex("B").get();
        var vertexC = graph.vertex("C").get();
        assertEquals(Stream.of(vertexB, vertexC).collect(toSet()),
                     new Path<>(vertexA).accessibleVertices().collect(toSet()));
    }

    Graph<String, String> graph() {

        // ____B
        // A_<___>_D_-_E
        // ____C
        var graph = Graph.<String, String>newGraph();
        var va = graph.insertVertex("A");
        graph.insertVertex("B");
        graph.insertVertex("C");
        graph.insertVertex("D");
        graph.insertVertex("E");
        graph.insertEdge("A", "B", "A - B", 1.0);
        graph.insertEdge("A", "C", "A - C", 0.9);
        graph.insertEdge("B", "D", "B - D", 1.0);
        graph.insertEdge("C", "D", "C - D", 1.0);
        graph.insertEdge("D", "E", "D - E", 1.0);
        return graph;
    }

    @Test
    void containsVertexTest() {
        var graph = graph();
        var vertexA = graph.vertex("A").get();
        var vertexB = graph.vertex("B").get();
        var vertexC = graph.vertex("C").get();
        var vertexD = graph.vertex("D").get();
        var vertexE = graph.vertex("E").get();

        var edgeA2B = graph.edge(vertexA, vertexB).get();
        graph.edge(vertexA, vertexC).get();
        var edgeB2D = graph.edge(vertexB, vertexD).get();
        graph.edge(vertexC, vertexD).get();
        var edgeD2E = graph.edge(vertexD, vertexE).get();
        var fullPath = new Path<>(vertexA).walk(edgeA2B)
                                          .walk(edgeB2D)
                                          .walk(edgeD2E);

        assertTrue(fullPath.contains(vertexA));
        assertTrue(fullPath.contains(vertexB));
        assertFalse(fullPath.contains(vertexC));
        assertTrue(fullPath.contains(vertexD));
        assertTrue(fullPath.contains(vertexE));
    }

    @Test
    void containsEdgeTest() {
        var graph = graph();
        var vertexA = graph.vertex("A").get();
        var vertexB = graph.vertex("B").get();
        var vertexC = graph.vertex("C").get();
        var vertexD = graph.vertex("D").get();
        var vertexE = graph.vertex("E").get();

        var edgeA2B = graph.edge(vertexA, vertexB).get();
        var edgeA2C = graph.edge(vertexA, vertexC).get();
        var edgeB2D = graph.edge(vertexB, vertexD).get();
        var edgeC2D = graph.edge(vertexC, vertexD).get();
        var edgeD2E = graph.edge(vertexD, vertexE).get();

        var fullPath = new Path<>(vertexA).walk(edgeA2B)
                                          .walk(edgeB2D)
                                          .walk(edgeD2E);

        assertTrue(fullPath.contains(edgeA2B));
        assertFalse(fullPath.contains(edgeA2C));
        assertTrue(fullPath.contains(edgeB2D));
        assertFalse(fullPath.contains(edgeC2D));
        assertTrue(fullPath.contains(edgeD2E));
    }

    @Test
    void distanceTest() {
        var graph = graph();
        var vertexA = graph.vertex("A").get();
        var vertexB = graph.vertex("B").get();
        var vertexC = graph.vertex("C").get();
        var vertexD = graph.vertex("D").get();
        var vertexE = graph.vertex("E").get();

        var edgeA2B = graph.edge(vertexA, vertexB).get();
        graph.edge(vertexA, vertexC).get();
        var edgeB2D = graph.edge(vertexB, vertexD).get();
        graph.edge(vertexC, vertexD).get();
        var edgeD2E = graph.edge(vertexD, vertexE).get();

        var fullPath = new Path<>(vertexA).walk(edgeA2B)
                                          .walk(edgeB2D)
                                          .walk(edgeD2E);
        assertEquals(3.0, fullPath.distance());
    }

    @Test
    void endsWithTest() {
        var graph = graph();
        var vertexA = graph.vertex("A").get();
        var vertexB = graph.vertex("B").get();
        var vertexC = graph.vertex("C").get();
        var vertexD = graph.vertex("D").get();
        var vertexE = graph.vertex("E").get();

        var edgeA2B = graph.edge(vertexA, vertexB).get();
        graph.edge(vertexA, vertexC).get();
        var edgeB2D = graph.edge(vertexB, vertexD).get();
        graph.edge(vertexC, vertexD).get();
        var edgeD2E = graph.edge(vertexD, vertexE).get();
        var fullPath = new Path<>(vertexA).walk(edgeA2B)
                                          .walk(edgeB2D)
                                          .walk(edgeD2E);

        assertFalse(fullPath.endsWith(vertexA));
        assertFalse(fullPath.endsWith(vertexB));
        assertFalse(fullPath.endsWith(vertexC));
        assertFalse(fullPath.endsWith(vertexD));
        assertTrue(fullPath.endsWith(vertexE));
    }

    @Test
    void tailTest() {
        var graph = graph();
        var vertexA = graph.vertex("A").get();
        var vertexB = graph.vertex("B").get();
        var vertexC = graph.vertex("C").get();
        var vertexD = graph.vertex("D").get();
        var vertexE = graph.vertex("E").get();

        var edgeA2B = graph.edge(vertexA, vertexB).get();
        graph.edge(vertexA, vertexC).get();
        var edgeB2D = graph.edge(vertexB, vertexD).get();
        graph.edge(vertexC, vertexD).get();
        var edgeD2E = graph.edge(vertexD, vertexE).get();
        var fullPath = new Path<>(vertexA).walk(edgeA2B)
                                          .walk(edgeB2D)
                                          .walk(edgeD2E);

        assertEquals(vertexE, fullPath.tail());
    }

    @Test
    void invalidWalkTest() {
        var graphA = graph();
        var vertexA = graphA.vertex("A").get();

        var graphB = Graph.<String, String>newGraph();
        var vertex1 = graphB.insertVertex("N1");
        var vertex2 = graphB.insertVertex("N2");
        var vertex3 = graphB.insertVertex("N3");
        var edge1 = graphB.insertEdge(vertex1, vertex2, "1");
        graphB.insertEdge(vertex2, vertex3, "2");

        assertThrows(IllegalStateException.class, () -> new Path<>(vertexA).walk(edge1));

    }
}
