module dev.vepo.openjgraph {
    requires javafx.controls;
    requires java.logging;
    requires transitive javafx.base;
    requires transitive javafx.graphics;

    exports dev.vepo.openjgraph.graph;
    exports dev.vepo.openjgraph.graphview;
}
