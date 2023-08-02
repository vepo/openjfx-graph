module dev.vepo.openjgraph {
    requires javafx.controls;
    requires java.logging;
    requires org.eclipse.collections.api;
    requires transitive javafx.base;
    requires transitive javafx.graphics;

    exports dev.vepo.openjgraph.graph;
    exports dev.vepo.openjgraph.graphview;
}
