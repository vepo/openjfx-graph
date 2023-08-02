package dev.vepo.openjgraph.graph;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ElementInspectorTest {

    public record RecordPojo(String label, @Weight double weight) {
    }

    public class ClassPojo {
        private String label;
        @Weight
        private double weight;

        public ClassPojo(String label) {
            this.label = label;
            this.weight = label.length();
        }
    }

    @Test
    void getEdgeWeight() {
        assertThat(ElementInspector.evaluateEdgeWeight(new RecordPojo("Test", 0.51))).isEqualTo(0.51);
        assertThat(ElementInspector.evaluateEdgeWeight(new ClassPojo("Test"))).isEqualTo(4.0);
    }
}