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
package dev.vepo.openjgraph.graph;

/**
 * An edge connects two {@link Vertex} of type <code>V</code> and stores an
 * element of type <code>E</code>. <br/>
 * The edge may be used in oriented and non-oriented graphs.
 * 
 * @param <E> Type of value stored in the edge
 * @param <V> Type of value stored in the vertices that this edge connects.
 * 
 * @see Graph
 * @see Digraph
 */
public record Edge<E, V>(Vertex<V, E> vertexA,
                         Vertex<V, E> vertexB,
                         boolean directed,
                         double weight,
                         E element) {

    public boolean contains(Vertex<V, E> vertex) { return vertexA.equals(vertex) || vertexB.equals(vertex); }

    public String label() { return element.toString(); }
}
