/*
 * The MIT License
 *
 * JavaFXSmartGraph | Copyright 2019-2023  brunomnsilva@gmail.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package dev.vepo.openjgraph.graphview;

import static dev.vepo.openjgraph.graphview.UtilitiesJavaFX.pick;
import static dev.vepo.openjgraph.graphview.UtilitiesPoint2D.*;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;

import dev.vepo.openjgraph.graph.Digraph;
import dev.vepo.openjgraph.graph.Edge;
import dev.vepo.openjgraph.graph.Graph;
import dev.vepo.openjgraph.graph.Path;
import dev.vepo.openjgraph.graph.Vertex;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;

/**
 * JavaFX {@link Pane} that is capable of plotting a {@link Graph} or
 * {@link Digraph}. <br>
 * Be sure to call {@link #init() } after the Stage is displayed. <br>
 * Whenever changes to the underlying graph are made, you should call
 * {@link #update()} to force the rendering of any new elements and, also, the
 * removal of others, if applicable. <br>
 * Vertices can be dragged by the user, if configured to do so. Consequently,
 * any connected edges will also adjust automatically to the new vertex
 * positioning.
 *
 * @param <V> Type of element stored at a vertex
 * @param <E> Type of element stored at an edge
 * @author brunomnsilva
 */
public class SmartGraphPanel<V, E> extends Pane {

    /*
     * CONFIGURATION PROPERTIES
     */
    private final SmartGraphProperties graphProperties;

    /*
     * INTERNAL DATA STRUCTURE
     */
    private final Graph<V, E> theGraph;
    private final SmartPlacementStrategy placementStrategy;
    private final Map<Vertex<V, E>, SmartGraphVertexNode<V, E>> vertexNodes;
    private final Map<Edge<E, V>, SmartGraphEdgeBase<E,V>> edgeNodes;
    private Map<Edge<E, V>, Tuple<Vertex<V, E>>> connections;
    private final Map<Tuple<SmartGraphVertexNode<V,E>>, Integer> placedEdges = new HashMap<>();
    private boolean initialized = false;

    /*
     * INTERACTION WITH VERTICES AND EDGES
     */
    private Consumer<SmartGraphVertex<V, E>> vertexClickConsumer = null;
    private Consumer<SmartGraphEdge<E, V>> edgeClickConsumer = null;

    /*
     * AUTOMATIC LAYOUT RELATED ATTRIBUTES
     */
    public final BooleanProperty automaticLayoutProperty;
    private AnimationTimer timer;
    private final double repulsionForce;
    private final double attractionForce;
    private final double attractionScale;

    // This value was obtained experimentally
    private static final int AUTOMATIC_LAYOUT_ITERATIONS = 20;

    /**
     * Constructs a visualization of the graph referenced by <code>theGraph</code>,
     * using default properties and default random placement of vertices.
     *
     * @param theGraph underlying graph
     * @see Graph
     */
    public SmartGraphPanel(Graph<V, E> theGraph) {
        this(theGraph, new SmartGraphProperties(), null);
    }

    /**
     * Constructs a visualization of the graph referenced by <code>theGraph</code>,
     * using custom properties and default random placement of vertices.
     *
     * @param theGraph   underlying graph
     * @param properties custom properties
     */
    public SmartGraphPanel(Graph<V, E> theGraph, SmartGraphProperties properties) {
        this(theGraph, properties, null);
    }

    /**
     * Constructs a visualization of the graph referenced by <code>theGraph</code>,
     * using default properties and custom placement of vertices.
     *
     * @param theGraph          underlying graph
     * @param placementStrategy placement strategy, null for default
     */
    public SmartGraphPanel(Graph<V, E> theGraph, SmartPlacementStrategy placementStrategy) {
        this(theGraph, null, placementStrategy);
    }

    /**
     * Constructs a visualization of the graph referenced by <code>theGraph</code>,
     * using custom properties and custom placement of vertices.
     *
     * @param theGraph          underlying graph
     * @param properties        custom properties, null for default
     * @param placementStrategy placement strategy, null for default
     */
    public SmartGraphPanel(Graph<V, E> theGraph, SmartGraphProperties properties,
                           SmartPlacementStrategy placementStrategy) {

        this(theGraph, properties, placementStrategy, null);
    }

    /**
     * Constructs a visualization of the graph referenced by <code>theGraph</code>,
     * using custom properties and custom placement of vertices.
     *
     * @param theGraph          underlying graph
     * @param properties        custom properties, null for default
     * @param placementStrategy placement strategy, null for default
     * @param cssFile           alternative css file, instead of default
     *                          'smartgraph.css'
     */
    public SmartGraphPanel(Graph<V, E> theGraph, SmartGraphProperties properties,
                           SmartPlacementStrategy placementStrategy, URI cssFile) {

        if (theGraph == null) {
            throw new IllegalArgumentException("The graph cannot be null.");
        }
        this.theGraph = theGraph;
        this.graphProperties = properties != null ? properties : new SmartGraphProperties();
        this.placementStrategy = placementStrategy != null ? placementStrategy : new SmartRandomPlacementStrategy();

        this.repulsionForce = this.graphProperties.getRepulsionForce();
        this.attractionForce = this.graphProperties.getAttractionForce();
        this.attractionScale = this.graphProperties.getAttractionScale();

        vertexNodes = new HashMap<>();
        edgeNodes = new HashMap<>();
        connections = new HashMap<>();

        // set stylesheet and class
        loadStylesheet(cssFile);

        initNodes();

        enableDoubleClickListener();

        // automatic layout initializations
        timer = new AnimationTimer() {

            @Override public void handle(long now) {
                runLayoutIteration();
            }
        };

        this.automaticLayoutProperty = new SimpleBooleanProperty(false);
        this.automaticLayoutProperty.addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                timer.start();
            } else {
                timer.stop();
            }
        });

    }

    private synchronized void runLayoutIteration() {
        IntStream.range(0, AUTOMATIC_LAYOUT_ITERATIONS)
                 .forEach(i -> {
                     resetForces();
                     computeForces();
                     updateForces();
                 });
        applyForces();
    }

    /**
     * Runs the initial current vertex placement strategy.
     * <p>
     * This method should only be called once during the lifetime of the object and
     * only after the underlying {@link Scene} is displayed.
     * <p>
     * Further required updates should be performed through the {@link #update() }
     * method.
     *
     * @throws IllegalStateException The exception is thrown if: (1) the Scene is
     *                               not yet displayed; (2) It has zero width and/or
     *                               height, and; (3) If this method was already
     *                               called.
     */
    public void init() throws IllegalStateException {
        if (isNull(this.getScene())) {
            throw new IllegalStateException("You must call this method after the instance was added to a scene.");
        } else if (this.getWidth() == 0 || this.getHeight() == 0) {
            throw new IllegalStateException("The layout for this panel has zero width and/or height");
        } else if (this.initialized) {
            throw new IllegalStateException("Already initialized. Use update() method instead.");
        }

        if (placementStrategy != null) {
            // call strategy to place the vertices in their initial locations
            placementStrategy.place(this.widthProperty().doubleValue(), this.heightProperty().doubleValue(),
                                    this.theGraph, this.vertexNodes.values());
        } else {
            // apply random placement
            new SmartRandomPlacementStrategy().place(this.widthProperty().doubleValue(),
                                                     this.heightProperty().doubleValue(), this.theGraph,
                                                     this.vertexNodes.values());

            // start automatic layout
            timer.start();
        }

        this.initialized = true;
    }

    /**
     * Returns the property used to toggle the automatic layout of vertices.
     *
     * @return automatic layout property
     */
    public BooleanProperty automaticLayoutProperty() {
        return this.automaticLayoutProperty;
    }

    /**
     * Toggle the automatic layout of vertices.
     *
     * @param value true if enabling; false, otherwise
     */
    public void setAutomaticLayout(boolean value) {
        automaticLayoutProperty.set(value);
    }

    /**
     * Forces a refresh of the visualization based on current state of the
     * underlying graph, immediately returning to the caller.
     * <p>
     * This method invokes the refresh in the graphical thread through
     * Platform.runLater(), so its not guaranteed that the visualization is in sync
     * immediately after this method finishes. That is, this method immediately
     * returns to the caller without waiting for the update to the visualization.
     * <p>
     * New vertices will be added close to adjacent ones or randomly for isolated
     * vertices.
     */
    public void update() {
        if (isNull(this.getScene())) {
            throw new IllegalStateException("You must call this method after the instance was added to a scene.");
        }

        if (!this.initialized) {
            throw new IllegalStateException("You must call init() method before any updates.");
        }

        // this will be called from a non-javafx thread, so this must be guaranteed to
        // run of the graphics thread
        Platform.runLater(() -> updateNodes());

    }

    /**
     * Forces a refresh of the visualization based on current state of the
     * underlying graph and waits for completion of the update.
     * <p>
     * Use this variant only when necessary, e.g., need to style an element
     * immediately after adding it to the underlying graph. Otherwise, use
     * {@link #update() } instead for performance sake.
     * <p>
     * New vertices will be added close to adjacent ones or randomly for isolated
     * vertices.
     */
    public void updateAndWait() {
        if (isNull(this.getScene())) {
            throw new IllegalStateException("You must call this method after the instance was added to a scene.");
        }

        if (!this.initialized) {
            throw new IllegalStateException("You must call init() method before any updates.");
        }

        var update = new FutureTask<>(() -> {
            updateNodes();
            return true;
        });

        //
        if (!Platform.isFxApplicationThread()) {
            // this will be called from a non-javafx thread, so this must be guaranteed to
            // run of the graphics thread
            Platform.runLater(update);

            // wait for completion, only outside javafx thread; otherwise -> deadlock
            try {
                update.get(1, TimeUnit.SECONDS);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            } catch (ExecutionException | TimeoutException ex) {
                Logger.getLogger(SmartGraphPanel.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            updateNodes();
        }

    }

    private synchronized void updateNodes() {
        removeNodes();
        insertNodes();
        updateLabels();
    }

    /*
     * INTERACTION WITH VERTICES AND EDGES
     */

    /**
     * Sets the action that should be performed when a vertex is double clicked.
     *
     * @param action action to be performed
     */
    public void setVertexDoubleClickAction(Consumer<SmartGraphVertex<V, E>> action) {
        this.vertexClickConsumer = action;
    }

    /**
     * Sets the action that should be performed when an edge is double clicked.
     *
     * @param action action to be performed
     */
    public void setEdgeDoubleClickAction(Consumer<SmartGraphEdge<E, V>> action) {
        this.edgeClickConsumer = action;
    }

    /*
     * NODES CREATION/UPDATES
     */
    private void initNodes() {
        /* create vertex graphical representations */
        for (Vertex<V, E> vertex : listOfVertices()) {
            vertexNodes.put(vertex, new SmartGraphVertexNode<>(vertex, 0, 0, graphProperties.getVertexRadius(),
                                                               graphProperties.getVertexAllowUserMove()));
        }

        /* create edges graphical representations between existing vertices */
        // this is used to guarantee that no duplicate edges are ever inserted
        List<Edge<E, V>> edgesToPlace = listOfEdges();

        for (Vertex<V, E> vertex : vertexNodes.keySet()) {

            Iterable<Edge<E, V>> incidentEdges = theGraph.incidentEdges(vertex);

            for (Edge<E, V> edge : incidentEdges) {

                // if already plotted, ignore edge.
                if (!edgesToPlace.contains(edge)) {
                    continue;
                }

                Vertex<V, E> oppositeVertex = theGraph.opposite(vertex, edge);

                SmartGraphVertexNode<V, E> graphVertexIn = vertexNodes.get(vertex);
                SmartGraphVertexNode<V, E> graphVertexOppositeOut = vertexNodes.get(oppositeVertex);

                graphVertexIn.addAdjacentVertex(graphVertexOppositeOut);
                graphVertexOppositeOut.addAdjacentVertex(graphVertexIn);

                SmartGraphEdgeBase<E, V> graphEdge = createEdge(edge, graphVertexIn, graphVertexOppositeOut);

                /* Track Edges already placed */
                connections.put(edge, new Tuple<>(vertex, oppositeVertex));
                addEdge(graphEdge, edge);

                if (edge.directed()) {
                    SmartArrow arrow = new SmartArrow(this.graphProperties.getEdgeArrowSize());
                    graphEdge.attachArrow(arrow);
                    this.getChildren().add(arrow);
                }

                edgesToPlace.remove(edge);
            }

        }

        /* place vertices above lines */
        for (Vertex<V, E> vertex : vertexNodes.keySet()) {
            SmartGraphVertexNode<V, E> v = vertexNodes.get(vertex);

            addVertex(v);
        }
    }

    private SmartGraphEdgeBase<E, V> createEdge(Edge<E, V> edge, SmartGraphVertexNode<V, E> graphVertexInbound,
                                                SmartGraphVertexNode<V, E> graphVertexOutbound) {
        /*
         * Even if edges are later removed, the corresponding index remains the same.
         * Otherwise, we would have to regenerate the appropriate edges.
         */
        int edgeIndex = 0;
        Integer counter = placedEdges.get(new Tuple<>(graphVertexInbound, graphVertexOutbound));
        if (counter != null) {
            edgeIndex = counter;
        }

        SmartGraphEdgeBase<E, V> graphEdge;

        if (getTotalEdgesBetween(graphVertexInbound.getUnderlyingVertex(),
                                 graphVertexOutbound.getUnderlyingVertex()) > 1 || graphVertexInbound == graphVertexOutbound) {
            graphEdge = new SmartGraphEdgeCurve<>(edge, graphVertexInbound, graphVertexOutbound, edgeIndex);
        } else {
            graphEdge = new SmartGraphEdgeLine<>(edge, graphVertexInbound, graphVertexOutbound);
        }

        placedEdges.put(new Tuple<>(graphVertexInbound, graphVertexOutbound), ++edgeIndex);

        return graphEdge;
    }

    private void addVertex(SmartGraphVertexNode<V, E> v) {
        this.getChildren().add(v);

        String labelText = generateVertexLabel(v.getUnderlyingVertex().element());

        if (graphProperties.getUseVertexTooltip()) {
            Tooltip t = new Tooltip(labelText);
            Tooltip.install(v, t);
        }

        if (graphProperties.getUseVertexLabel()) {
            SmartLabel label = new SmartLabel(labelText);

            label.addStyleClass("vertex-label");
            this.getChildren().add(label);
            v.attachLabel(label);
        }
    }

    private void addEdge(SmartGraphEdgeBase<E, V> e, Edge<E, V> edge) {
        // edges to the back
        this.getChildren().add(0, (Node) e);
        edgeNodes.put(edge, e);

        String labelText = generateEdgeLabel(edge.element());

        if (graphProperties.getUseEdgeTooltip()) {
            Tooltip t = new Tooltip(labelText);
            Tooltip.install((Node) e, t);
        }

        if (graphProperties.getUseEdgeLabel()) {
            SmartLabel label = new SmartLabel(labelText);

            label.addStyleClass("edge-label");
            this.getChildren().add(label);
            e.attachLabel(label);
        }
    }

    private static final Random RANDOM = new SecureRandom();

    private void insertNodes() {
        Collection<Vertex<V, E>> unplottedVertices = unplottedVertices();

        List<SmartGraphVertexNode<V, E>> newVertices = null;

        Bounds bounds = getPlotBounds();
        double mx = bounds.getMinX() + bounds.getWidth() / 2.0;
        double my = bounds.getMinY() + bounds.getHeight() / 2.0;

        if (!unplottedVertices.isEmpty()) {

            newVertices = new LinkedList<>();

            for (Vertex<V, E> vertex : unplottedVertices) {
                // create node
                // Place new nodes in the vicinity of existing adjacent ones;
                // Place them in the middle of the plot, otherwise.
                double x, y;
                Collection<Edge<E, V>> incidentEdges = theGraph.incidentEdges(vertex);
                if (incidentEdges.isEmpty()) {
                    /* not (yet) connected, put in the middle of the plot */
                    x = mx;
                    y = my;
                } else {
                    Edge<E, V> firstEdge = incidentEdges.iterator().next();
                    Vertex<V, E> opposite = theGraph.opposite(vertex, firstEdge);
                    SmartGraphVertexNode<V, E> existing = vertexNodes.get(opposite);

                    if (existing == null) {
                        /*
                         * Updates may be coming too fast and we can get out of sync. The opposite
                         * vertex exists in the (di)graph, but we have not yet created it for the panel.
                         * Therefore, its position is unknown, so place the vertex representation in the
                         * middle.
                         */
                        x = mx;
                        y = my;
                    } else {
                        /* TODO: fix -- the placing point can be set out of bounds */
                        var p = rotate(existing.getPosition().add(50.0, 50.0), existing.getPosition(),
                                       RANDOM.nextDouble() * 360);

                        x = p.getX();
                        y = p.getY();
                    }
                }

                var newVertex = new SmartGraphVertexNode<>(vertex, x, y, graphProperties.getVertexRadius(),
                                                           graphProperties.getVertexAllowUserMove());

                // track new nodes
                newVertices.add(newVertex);
                // add to global mapping
                vertexNodes.put(vertex, newVertex);
            }

        }

        Collection<Edge<E, V>> unplottedEdges = unplottedEdges();
        if (!unplottedEdges.isEmpty()) {
            for (Edge<E, V> edge : unplottedEdges) {

                var u = edge.vertexA(); // oubound if digraph, by javadoc requirement
                var v = edge.vertexB(); // inbound if digraph, by javadoc requirement

                var graphVertexOut = vertexNodes.get(u);
                var graphVertexIn = vertexNodes.get(v);

                /*
                 * Updates may be coming too fast and we can get out of sync. Skip and wait for
                 * another update call, since they will surely be coming at this pace.
                 */
                if (graphVertexIn == null || graphVertexOut == null) {
                    continue;
                }

                graphVertexOut.addAdjacentVertex(graphVertexIn);
                graphVertexIn.addAdjacentVertex(graphVertexOut);

                var graphEdge = createEdge(edge, graphVertexIn, graphVertexOut);

                if (edge.directed()) {
                    SmartArrow arrow = new SmartArrow(this.graphProperties.getEdgeArrowSize());
                    graphEdge.attachArrow(arrow);
                    this.getChildren().add(arrow);
                }

                /* Track edges */
                connections.put(edge, new Tuple<>(u, v));
                addEdge(graphEdge, edge);

            }
        }

        if (newVertices != null) {
            newVertices.forEach(this::addVertex);
        }
    }

    private void removeNodes() {
        // remove edges (graphical elements) that were removed from the underlying graph
        Collection<Edge<E, V>> removedEdges = removedEdges();
        for (Edge<E, V> e : removedEdges) {
            SmartGraphEdgeBase<E, V> edgeToRemove = edgeNodes.get(e);
            edgeNodes.remove(e);
            removeEdge(edgeToRemove); // remove from panel

            // when edges are removed, the adjacency between vertices changes
            // the adjacency is kept in parallel in an internal data structure
            Tuple<Vertex<V, E>> vertexTuple = connections.get(e);

            if (getTotalEdgesBetween(vertexTuple.first, vertexTuple.second) == 0) {
                SmartGraphVertexNode<V, E> v0 = vertexNodes.get(vertexTuple.first);
                SmartGraphVertexNode<V, E> v1 = vertexNodes.get(vertexTuple.second);

                v0.removeAdjacentVertex(v1);
                v1.removeAdjacentVertex(v0);
            }

            connections.remove(e);
        }

        // remove vertices (graphical elements) that were removed from the underlying
        // graph
        removedVertices().forEach(removedVertex->{
            SmartGraphVertexNode<V, E> removed = vertexNodes.remove(removedVertex);
            removeVertex(removed);
        });
    }

    private void removeEdge(SmartGraphEdgeBase<E,V> e) {
        getChildren().remove((Node) e);

        SmartArrow attachedArrow = e.getAttachedArrow();
        if (attachedArrow != null) {
            getChildren().remove(attachedArrow);
        }

        Text attachedLabel = e.getAttachedLabel();
        if (attachedLabel != null) {
            getChildren().remove(attachedLabel);
        }
    }

    private void removeVertex(SmartGraphVertexNode<V,E> v) {
        getChildren().remove(v);

        Text attachedLabel = v.getAttachedLabel();
        if (attachedLabel != null) {
            getChildren().remove(attachedLabel);
        }
    }

    /**
     * Updates node's labels
     */
    private void updateLabels() {
        theGraph.vertices().forEach((v) -> {
            SmartGraphVertexNode<V, E> vertexNode = vertexNodes.get(v);
            if (vertexNode != null) {
                SmartLabel label = vertexNode.getAttachedLabel();
                if (label != null) {
                    String text = generateVertexLabel(v.element());
                    label.setText(text);
                }

            }
        });

        theGraph.edges().forEach((e) -> {
            SmartGraphEdgeBase<E,V> edgeNode = edgeNodes.get(e);
            if (edgeNode != null) {
                SmartLabel label = edgeNode.getAttachedLabel();
                if (label != null) {
                    String text = generateEdgeLabel(e.element());
                    label.setText(text);
                }
            }
        });
    }

    private String generateVertexLabel(V vertex) {
        if (nonNull(vertex)) {
            try {
                Class<?> clazz = vertex.getClass();
                for (Method method : clazz.getDeclaredMethods()) {
                    if (method.isAnnotationPresent(SmartLabelSource.class)) {
                        method.setAccessible(true);
                        Object value = method.invoke(vertex);
                        return value.toString();
                    }
                }
            } catch (SecurityException | IllegalAccessException | IllegalArgumentException |
                     InvocationTargetException ex) {
                Logger.getLogger(SmartGraphPanel.class.getName()).log(Level.SEVERE, null, ex);
            }
            return vertex.toString();
        } else {
            return "<NULL>";
        }
    }

    private String generateEdgeLabel(E edge) {
        if (nonNull(edge)) {
            try {
                Class<?> clazz = edge.getClass();
                for (Method method : clazz.getDeclaredMethods()) {
                    if (method.isAnnotationPresent(SmartLabelSource.class)) {
                        method.setAccessible(true);
                        Object value = method.invoke(edge);
                        return value.toString();
                    }
                }
            } catch (SecurityException | IllegalAccessException | IllegalArgumentException |
                     InvocationTargetException ex) {
                Logger.getLogger(SmartGraphPanel.class.getName()).log(Level.SEVERE, null, ex);
            }
            return edge.toString();
        } else {
            return "<NULL>";
        }
    }

    /**
     * Computes the bounding box from all displayed vertices.
     *
     * @return bounding box
     */
    private Bounds getPlotBounds() {
        double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE, maxX = Double.MIN_VALUE, maxY = Double.MIN_VALUE;

        if (vertexNodes.size() == 0)
            return new BoundingBox(0, 0, getWidth(), getHeight());

        for (SmartGraphVertexNode<V, E> v : vertexNodes.values()) {
            minX = Math.min(minX, v.getCenterX());
            minY = Math.min(minY, v.getCenterY());
            maxX = Math.max(maxX, v.getCenterX());
            maxY = Math.max(maxY, v.getCenterY());
        }

        return new BoundingBox(minX, minY, maxX - minX, maxY - minY);
    }

    /*
     * AUTOMATIC LAYOUT
     */
    private void computeForces() {
        for (SmartGraphVertexNode<V, E> v : vertexNodes.values()) {
            for (SmartGraphVertexNode<V, E> other : vertexNodes.values()) {
                if (v == other) {
                    continue; // NOP
                }

                // double k = Math.sqrt(getWidth() * getHeight() / graphVertexMap.size());
                Point2D repellingForce =
                        repellingForce(v.getUpdatedPosition(), other.getUpdatedPosition(), this.repulsionForce);

                double deltaForceX = 0, deltaForceY = 0;

                // compute attractive and reppeling forces
                // opt to use internal areAdjacent check, because a vertex can be removed from
                // the underlying graph before we have the chance to remove it from our
                // internal data structure
                if (areAdjacent(v, other)) {

                    Point2D attractiveForce =
                            attractiveForce(v.getUpdatedPosition(), other.getUpdatedPosition(), vertexNodes.size(),
                                            this.attractionForce, this.attractionScale);

                    deltaForceX = attractiveForce.getX() + repellingForce.getX();
                    deltaForceY = attractiveForce.getY() + repellingForce.getY();
                } else {
                    deltaForceX = repellingForce.getX();
                    deltaForceY = repellingForce.getY();
                }

                v.addForceVector(deltaForceX, deltaForceY);
            }
        }
    }

    private boolean areAdjacent(SmartGraphVertexNode<V, E> v, SmartGraphVertexNode<V, E> u) {
        return v.isAdjacentTo(u);
    }

    private void updateForces() {
        vertexNodes.values().forEach((v) -> {
            v.updateDelta();
        });
    }

    private void applyForces() {
        vertexNodes.values().forEach((v) -> {
            v.moveFromForces();
        });
    }

    private void resetForces() {
        vertexNodes.values().forEach((v) -> {
            v.resetForces();
        });
    }

    private int getTotalEdgesBetween(Vertex<V, E> v, Vertex<V, E> u) {
        // TODO: It may be necessary to adjust this method if you use another Graph
        // variant, e.g., Digraph (directed graph)
        int count = 0;
        for (Edge<E, V> edge : theGraph.edges()) {
            if (edge.vertexA().equals(v) && edge.vertexB().equals(u) || edge.vertexA().equals(u) && edge.vertexB()
                                                                                                        .equals(v)) {
                count++;
            }
        }
        return count;
    }

    private List<Edge<E, V>> listOfEdges() {
        List<Edge<E, V>> list = new LinkedList<>();
        for (Edge<E, V> edge : theGraph.edges()) {
            list.add(edge);
        }
        return list;
    }

    private List<Vertex<V, E>> listOfVertices() {
        List<Vertex<V, E>> list = new LinkedList<>();
        for (Vertex<V, E> vertex : theGraph.vertices()) {
            list.add(vertex);
        }
        return list;
    }

    /**
     * Computes the vertex collection of the underlying graph that are not currently
     * being displayed.
     *
     * @return collection of vertices
     */
    private Collection<Vertex<V, E>> unplottedVertices() {
        List<Vertex<V, E>> unplotted = new LinkedList<>();

        for (Vertex<V, E> v : theGraph.vertices()) {
            if (!vertexNodes.containsKey(v)) {
                unplotted.add(v);
            }
        }

        return unplotted;
    }

    /**
     * Computes the collection for vertices that are currently being displayed but
     * do not longer exist in the underlying graph.
     *
     * @return collection of vertices
     */
    private Collection<Vertex<V, E>> removedVertices() {
        List<Vertex<V, E>> removed = new LinkedList<>();

        Collection<Vertex<V, E>> graphVertices = theGraph.vertices();
        Collection<SmartGraphVertexNode<V, E>> plotted = vertexNodes.values();

        for (SmartGraphVertexNode<V, E> v : plotted) {
            if (!graphVertices.contains(v.getUnderlyingVertex())) {
                removed.add(v.getUnderlyingVertex());
            }
        }

        return removed;
    }

    /**
     * Computes the collection for edges that are currently being displayed but do
     * not longer exist in the underlying graph.
     *
     * @return collection of edges
     */
    private Collection<Edge<E, V>> removedEdges() {
        List<Edge<E, V>> removed = new LinkedList<>();

        Collection<Edge<E, V>> graphEdges = theGraph.edges();
        Collection<SmartGraphEdgeBase<E,V>> plotted = edgeNodes.values();

        for (SmartGraphEdgeBase<E,V> e : plotted) {
            if (!graphEdges.contains(e.getUnderlyingEdge())) {
                removed.add(e.getUnderlyingEdge());
            }
        }

        return removed;
    }

    /**
     * Computes the edge collection of the underlying graph that are not currently
     * being displayed.
     *
     * @return collection of edges
     */
    private Collection<Edge<E, V>> unplottedEdges() {
        List<Edge<E, V>> unplotted = new LinkedList<>();

        for (Edge<E, V> e : theGraph.edges()) {
            if (!edgeNodes.containsKey(e)) {
                unplotted.add(e);
            }
        }

        return unplotted;
    }

    /**
     * Sets a vertex position (its center) manually.
     * <p>
     * The positioning should be inside the boundaries of the panel, but no
     * restrictions are enforced by this method, so be aware.
     *
     * @param v underlying vertex
     * @param x x-coordinate on panel
     * @param y y-coordinate on panel
     */
    public void setVertexPosition(Vertex<V, E> v, double x, double y) {
        SmartGraphVertexNode<V, E> node = vertexNodes.get(v);
        if (node != null) {
            node.setPosition(x, y);
        }
    }

    /**
     * Return the current x-coordinate (relative to the panel) of a vertex.
     *
     * @param v underlying vertex
     * @return the x-coordinate or NaN if the vertex does not exist
     */
    public double getVertexPositionX(Vertex<V, E> v) {
        SmartGraphVertexNode<V, E> node = vertexNodes.get(v);
        if (node != null) {
            return node.getPositionCenterX();
        }
        return Double.NaN;
    }

    /**
     * Return the current y-coordinate (relative to the panel) of a vertex.
     *
     * @param v underlying vertex
     * @return the y-coordinate or NaN if the vertex does not exist
     */
    public double getVertexPositionY(Vertex<V, E> v) {
        SmartGraphVertexNode<V, E> node = vertexNodes.get(v);
        if (node != null) {
            return node.getPositionCenterY();
        }
        return Double.NaN;
    }

    public void highlight(Path<V, E> path) {
        update();
        vertexNodes.forEach((v, n) -> {
            if (path.contains(v)) {
                n.addStyleClass("highlight");
            }
        });
        edgeNodes.forEach((e, n) -> {
            if (path.contains(e)) {
                n.addStyleClass("highlight");
            }
        });
        update();
    }

    public void clearHighlight() {
        vertexNodes.forEach((v, n) -> n.removeStyleClass("highlight"));
        edgeNodes.forEach((e, n) -> n.removeStyleClass("highlight"));
        update();
    }

    /**
     * Returns the associated stylable element with a graph vertex.
     *
     * @param v underlying vertex
     * @return stylable element
     */
    public SmartStylableNode getStylableVertex(Vertex<V, E> v) {
        return vertexNodes.get(v);
    }

    /**
     * Returns the associated stylable element with a graph vertex.
     *
     * @param vertexElement underlying vertex's element
     * @return stylable element
     */
    public SmartStylableNode getStylableVertex(V vertexElement) {
        for (Vertex<V, E> v : vertexNodes.keySet()) {
            if (v.element().equals(vertexElement)) {
                return vertexNodes.get(v);
            }
        }
        return null;
    }

    /**
     * Returns the associated stylable element with a graph edge.
     *
     * @param edge underlying graph edge
     * @return stylable element
     */
    public SmartStylableNode getStylableEdge(Edge<E, V> edge) {
        return edgeNodes.get(edge);
    }

    /**
     * Returns the associated stylable element with a graph edge.
     *
     * @param edgeElement underlying graph edge's element
     * @return stylable element
     */
    public SmartStylableNode getStylableEdge(E edgeElement) {
        for (Edge<E, V> e : edgeNodes.keySet()) {
            if (e.element().equals(edgeElement)) {
                return edgeNodes.get(e);
            }
        }
        return null;
    }

    /**
     * Returns the associated stylable element with a graph vertex.
     *
     * @param v underlying vertex
     * @return stylable element (label)
     */
    public SmartStylableNode getStylableLabel(Vertex<V, E> v) {
        var vertex = vertexNodes.get(v);
        return vertex != null ? vertex.getStylableLabel() : null;
    }

    /**
     * Returns the associated stylable element with a graph edge.
     *
     * @param e underlying graph edge
     * @return stylable element (label)
     */
    public SmartStylableNode getStylableLabel(Edge<E, V> e) {
        var edge = edgeNodes.get(e);
        return edge != null ? edge.getStylableLabel() : null;
    }

    /**
     * Loads the stylesheet and applies the .graph class to this panel.
     */
    private void loadStylesheet(URI cssFile) {
        var resource = SmartGraphPanel.class.getResource("/smartgraph.css");
        if (nonNull(resource)) {
            getStylesheets().add(new String(resource.toExternalForm()));
        }

        try {
            var customCssFile = Paths.get("smartgraph.css").toFile();
            if (customCssFile.exists()) {
                getStylesheets().add(customCssFile.toURI().toURL().toExternalForm());
            }
        } catch (MalformedURLException ex) {
            Logger.getLogger(SmartGraphPanel.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            if (nonNull(cssFile)) {
                getStylesheets().add(cssFile.toURL().toExternalForm());
            }
            this.getStyleClass().add("graph");
        } catch (MalformedURLException ex) {
            Logger.getLogger(SmartGraphPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Enables the double click action on this pane.
     * <p>
     * This method identifies the node that was clicked and, if any, calls the
     * appropriate consumer, i.e., vertex or edge consumers.
     */
    private void enableDoubleClickListener() {
        setOnMouseClicked((MouseEvent mouseEvent) -> {
            if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
                if (mouseEvent.getClickCount() == 2) {
                    // no need to continue otherwise
                    if (vertexClickConsumer == null && edgeClickConsumer == null) {
                        return;
                    }

                    var node = pick(SmartGraphPanel.this, mouseEvent.getSceneX(), mouseEvent.getSceneY());
                    if (nonNull(node)) {
                        if (node instanceof SmartGraphVertex v) {
                            vertexClickConsumer.accept(v);
                        } else if (node instanceof SmartGraphEdge e) {
                            edgeClickConsumer.accept(e);
                        }
                    }
                }
            }
        });
    }

    /**
     * Represents a tuple in Java.
     *
     * @param <T> the type of the tuple
     */
    private record Tuple<T>(T first, T second) {
    }
}
