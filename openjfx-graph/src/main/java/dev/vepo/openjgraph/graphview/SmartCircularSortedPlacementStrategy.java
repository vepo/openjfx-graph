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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import dev.vepo.openjgraph.graph.Graph;
import javafx.geometry.Point2D;

/**
 * Places vertices around a circle, ordered by the underlying
 * vertices {@code element.toString() value}.
 * 
 * @see SmartPlacementStrategy
 * 
 * @author brunomnsilva
 */
public class SmartCircularSortedPlacementStrategy implements SmartPlacementStrategy {

    @Override
    public <V, E> void place(double width, double height, Graph<V, E> theGraph, Collection<? extends SmartGraphVertex<V>> vertices) {
        Point2D center = new Point2D(width / 2, height / 2);
        int N = vertices.size();
        double angleIncrement = -360f / N;
        
        //place first vertex at north position, others in clockwise manner
        boolean first = true;
        Point2D p = null;
        for (SmartGraphVertex<V> vertex : sort(vertices)) {
            
            if (first) {
                //verify the smallest width and height.
                if(width > height)
                    p = new Point2D(center.getX(),
                            center.getY() - height / 2 + vertex.getRadius() * 2);
                else
                    p = new Point2D(center.getX(),
                            center.getY() - width / 2 + vertex.getRadius() * 2);

                first = false;
            } else {
                p = UtilitiesPoint2D.rotate(p, center, angleIncrement);
            }

            vertex.setPosition(p.getX(), p.getY());
        }
    }

    /**
     * Sort vertices by their element's label.
     * @param vertices collection of vertices to sort
     * @return a new collection with sorted vertices
     * @param <V> the type of the element stored at the vertices
     */
    protected <V> Collection<SmartGraphVertex<V>> sort(Collection<? extends SmartGraphVertex<V>> vertices) {
        
        List<SmartGraphVertex<V>> list = new ArrayList<>(vertices);

        list.sort( (v1, v2) -> {
            V element1 = v1.getUnderlyingVertex().element();
            V element2 = v2.getUnderlyingVertex().element();
            return getVertexElementLabel(element1).compareToIgnoreCase(getVertexElementLabel(element2));
        });
        
        return list;
    }

    private <V> String getVertexElementLabel(V vertex) {

        try {
            Class<?> clazz = vertex.getClass();
            for (Method method : clazz.getDeclaredMethods()) {
                if (method.isAnnotationPresent(SmartLabelSource.class)) {
                    method.setAccessible(true);
                    Object value = method.invoke(vertex);
                    return value.toString();
                }
            }
        } catch (SecurityException | IllegalAccessException | IllegalArgumentException
                 | InvocationTargetException | NullPointerException ex) {
            Logger.getLogger(SmartGraphPanel.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }

        return vertex != null ? vertex.toString() : "<NULL>";
    }
}
