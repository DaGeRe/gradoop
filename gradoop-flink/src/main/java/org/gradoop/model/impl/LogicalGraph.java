/*
 * This file is part of Gradoop.
 *
 * Gradoop is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Gradoop is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Gradoop.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gradoop.model.impl;

import org.apache.commons.lang.NotImplementedException;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.graph.Edge;
import org.apache.flink.graph.Graph;
import org.apache.flink.graph.Vertex;
import org.gradoop.io.json.JsonWriter;
import org.gradoop.model.api.EPGMAttributed;
import org.gradoop.model.api.EPGMEdge;
import org.gradoop.model.api.EPGMGraphHead;
import org.gradoop.model.api.EPGMIdentifiable;
import org.gradoop.model.api.EPGMLabeled;
import org.gradoop.model.api.EPGMVertex;
import org.gradoop.model.api.operators.BinaryGraphToGraphOperator;
import org.gradoop.model.api.operators.LogicalGraphOperators;
import org.gradoop.model.api.operators.UnaryGraphToCollectionOperator;
import org.gradoop.model.api.operators.UnaryGraphToGraphOperator;
import org.gradoop.model.impl.functions.Predicate;
import org.gradoop.model.impl.functions.UnaryFunction;
import org.gradoop.model.impl.id.GradoopId;
import org.gradoop.model.impl.operators.logicalgraph.binary.Combination;
import org.gradoop.model.impl.operators.logicalgraph.binary.Exclusion;
import org.gradoop.model.impl.operators.logicalgraph.binary.Overlap;
import org.gradoop.model.impl.operators.logicalgraph.unary.Aggregation;
import org.gradoop.model.impl.operators.logicalgraph.unary.Projection;
import org.gradoop.model.impl.operators.logicalgraph.unary.sampling.RandomNodeSampling;
import org.gradoop.model.impl.operators.logicalgraph.unary.summarization
  .SummarizationGroupCombine;
import org.gradoop.util.GradoopFlinkConfig;

import java.util.Map;

/**
 * Represents a logical graph inside the EPGM. Holds the graph data (label,
 * properties) and offers unary, binary and auxiliary operators.
 *
 * @param <VD> EPGM vertex type
 * @param <ED> EPGM edge type
 * @param <GD> EPGM graph head type
 */
public class LogicalGraph
  <VD extends EPGMVertex, ED extends EPGMEdge, GD extends EPGMGraphHead>
  extends AbstractGraph<VD, ED, GD>
  implements LogicalGraphOperators<VD, ED, GD>,
  EPGMIdentifiable, EPGMAttributed, EPGMLabeled {

  /**
   * Graph data associated with that logical graph.
   */
  private final GD graphHead;

  /**
   * Creates a new logical graph based on the given parameters.
   *
   * @param vertices  vertex data set
   * @param edges     edge data set
   * @param graphHead graph data associated with that logical graph
   * @param config    Gradoop Flink configuration
   */
  private LogicalGraph(DataSet<VD> vertices, DataSet<ED> edges, GD graphHead,
    GradoopFlinkConfig<VD, ED, GD> config) {
    super(vertices, edges, config);
    this.graphHead = graphHead;
  }

  /**
   * Creates a logical graph from the given arguments.
   *
   * @param graph     Flink Gelly graph
   * @param graphData Graph head associated with the logical graph
   * @param config    Gradoop Flink configuration
   * @param <VD>      vertex data type
   * @param <ED>      edge data type
   * @param <GD>      graph data type
   * @return logical graph
   */
  public static
  <VD extends EPGMVertex, ED extends EPGMEdge, GD extends EPGMGraphHead>
  LogicalGraph<VD, ED, GD> fromGellyGraph(
    Graph<GradoopId, VD, ED> graph, GD graphData,
    GradoopFlinkConfig<VD, ED, GD> config) {
    return fromDataSets(graph.getVertices().map(
      new MapFunction<Vertex<GradoopId, VD>, VD>() {
        @Override
        public VD map(Vertex<GradoopId, VD> gellyVertex) throws Exception {
          return gellyVertex.getValue();
        }
      }).withForwardedFields("f1->*"),
      graph.getEdges().map(new MapFunction<Edge<GradoopId, ED>, ED>() {
        @Override
        public ED map(Edge<GradoopId, ED> gellyEdge) throws Exception {
          return gellyEdge.getValue();
        }
      }).withForwardedFields("f2->*"),
      graphData,
      config);
  }

  /**
   * Creates a logical graph from the given arguments.
   *
   * @param vertices  Vertex DataSet
   * @param edges     Edge DataSet
   * @param graphData graph data associated with the logical graph
   * @param config    Gradoop Flink configuration
   * @param <VD>      EPGM vertex type
   * @param <ED>      EPGM edge type
   * @param <GD>      EPGM graph head graph head type
   * @return logical graph
   */
  public static
  <VD extends EPGMVertex, ED extends EPGMEdge, GD extends EPGMGraphHead>
  LogicalGraph<VD, ED, GD> fromDataSets(DataSet<VD> vertices,
    DataSet<ED> edges,
    GD graphData,
    GradoopFlinkConfig<VD, ED, GD> config) {
    return new LogicalGraph<>(vertices,
      edges,
      graphData,
      config);
  }


  /**
   * {@inheritDoc}
   */
  @Override
  public GraphCollection<VD, ED, GD> match(String graphPattern,
    Predicate<LogicalGraph> predicateFunc) {
    throw new NotImplementedException();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public LogicalGraph<VD, ED, GD> project(UnaryFunction<VD, VD> vertexFunction,
    UnaryFunction<ED, ED> edgeFunction) throws Exception {
    return callForGraph(
      new Projection<VD, ED, GD>(vertexFunction, edgeFunction));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public <O extends Number> LogicalGraph<VD, ED, GD> aggregate(
    String propertyKey,
    UnaryFunction<LogicalGraph<VD, ED, GD>, O> aggregateFunc) throws Exception {
    return callForGraph(new Aggregation<>(propertyKey, aggregateFunc));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public LogicalGraph<VD, ED, GD> sampleRandomNodes(Float
    sampleSize) throws Exception {
    return callForGraph(new RandomNodeSampling<VD, ED, GD>(sampleSize));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public LogicalGraph<VD, ED, GD> summarize(String vertexGroupingKey) throws
    Exception {
    return summarize(vertexGroupingKey, null);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public LogicalGraph<VD, ED, GD> summarize(String vertexGroupingKey,
    String edgeGroupingKey) throws Exception {
    return callForGraph(
      new SummarizationGroupCombine<VD, ED, GD>(vertexGroupingKey,
        edgeGroupingKey, false, false));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public LogicalGraph<VD, ED, GD> summarizeOnVertexLabel() throws Exception {
    return summarizeOnVertexLabel(null, null);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public LogicalGraph<VD, ED, GD> summarizeOnVertexLabelAndVertexProperty(
    String vertexGroupingKey) throws Exception {
    return summarizeOnVertexLabel(vertexGroupingKey, null);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public LogicalGraph<VD, ED, GD> summarizeOnVertexLabelAndEdgeProperty(
    String edgeGroupingKey) throws Exception {
    return summarizeOnVertexLabel(null, edgeGroupingKey);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public LogicalGraph<VD, ED, GD> summarizeOnVertexLabel(
    String vertexGroupingKey, String edgeGroupingKey) throws Exception {
    return callForGraph(
      new SummarizationGroupCombine<VD, ED, GD>(vertexGroupingKey,
        edgeGroupingKey, true, false));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public LogicalGraph<VD, ED, GD> summarizeOnVertexAndEdgeLabel() throws
    Exception {
    return summarizeOnVertexAndEdgeLabel(null, null);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public LogicalGraph<VD, ED, GD>
  summarizeOnVertexAndEdgeLabelAndVertexProperty(
    String vertexGroupingKey) throws Exception {
    return summarizeOnVertexAndEdgeLabel(vertexGroupingKey, null);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public LogicalGraph<VD, ED, GD> summarizeOnVertexAndEdgeLabelAndEdgeProperty(
    String edgeGroupingKey) throws Exception {
    return summarizeOnVertexAndEdgeLabel(null, edgeGroupingKey);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public LogicalGraph<VD, ED, GD> summarizeOnVertexAndEdgeLabel(
    String vertexGroupingKey, String edgeGroupingKey) throws Exception {
    return callForGraph(
      new SummarizationGroupCombine<VD, ED, GD>(vertexGroupingKey,
        edgeGroupingKey, true, true));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public LogicalGraph<VD, ED, GD> combine(LogicalGraph<VD, ED, GD> otherGraph) {
    return callForGraph(new Combination<VD, ED, GD>(), otherGraph);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public LogicalGraph<VD, ED, GD> overlap(LogicalGraph<VD, ED, GD> otherGraph) {
    return callForGraph(new Overlap<VD, ED, GD>(), otherGraph);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public LogicalGraph<VD, ED, GD> exclude(LogicalGraph<VD, ED, GD> otherGraph) {
    return callForGraph(new Exclusion<VD, ED, GD>(), otherGraph);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public LogicalGraph<VD, ED, GD> callForGraph(
    UnaryGraphToGraphOperator<VD, ED, GD> operator) throws Exception {
    return operator.execute(this);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public LogicalGraph<VD, ED, GD> callForGraph(
    BinaryGraphToGraphOperator<VD, ED, GD> operator,
    LogicalGraph<VD, ED, GD> otherGraph) {
    return operator.execute(this, otherGraph);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public GraphCollection<VD, ED, GD> callForCollection(
    UnaryGraphToCollectionOperator<VD, ED, GD> operator) throws Exception {
    return operator.execute(this);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Map<String, Object> getProperties() {
    return graphHead.getProperties();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Iterable<String> getPropertyKeys() {
    return graphHead.getPropertyKeys();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Object getProperty(String key) {
    return graphHead.getProperty(key);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public <T> T getProperty(String key, Class<T> type) {
    return graphHead.getProperty(key, type);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setProperties(Map<String, Object> properties) {
    graphHead.setProperties(properties);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setProperty(String key, Object value) {
    graphHead.setProperty(key, value);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getPropertyCount() {
    return graphHead.getPropertyCount();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public GradoopId getId() {
    return graphHead.getId();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setId(GradoopId id) {
    graphHead.setId(id);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getLabel() {
    return graphHead.getLabel();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setLabel(String label) {
    graphHead.setLabel(label);
  }

  /**
   * {@inheritDoc}
   */
  @SuppressWarnings("unchecked")
  @Override
  public void writeAsJson(String vertexFile, String edgeFile,
    String graphFile) throws Exception {
    getVertices().writeAsFormattedText(vertexFile,
      new JsonWriter.VertexTextFormatter<VD>());
    getEdges().writeAsFormattedText(edgeFile,
      new JsonWriter.EdgeTextFormatter<ED>());
    getConfig().getExecutionEnvironment().fromElements(graphHead)
      .writeAsFormattedText(graphFile, new JsonWriter.GraphTextFormatter<GD>());
    getConfig().getExecutionEnvironment().execute();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void print() throws Exception {
    final StringBuilder sb =
      new StringBuilder(String.format("LogicalGraph{%n"));
    sb.append(
      String.format("%-10d %s %n", graphHead.getId(), graphHead.toString()));
    sb.append(String.format("Vertices:%n"));
    for (VD v : this.getVertices().collect()) {
      sb.append(String.format("%-10d %s %n", v.getId(), v));
    }
    sb.append(String.format("Edges:%n"));
    for (ED e : this.getEdges().collect()) {
      sb.append(String.format("%-10d %s %n", e.getId(), e));
    }
    sb.append('}');

    System.out.println(sb);
  }
}
