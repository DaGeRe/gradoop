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

import com.google.common.collect.Lists;
import org.apache.commons.lang.NotImplementedException;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.JoinFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.tuple.Tuple1;
import org.gradoop.io.json.JsonWriter;
import org.gradoop.model.api.EPGMGraphHead;
import org.gradoop.model.api.EPGMEdge;
import org.gradoop.model.api.EPGMVertex;
import org.gradoop.model.api.operators.BinaryCollectionToCollectionOperator;
import org.gradoop.model.api.operators.BinaryGraphToGraphOperator;
import org.gradoop.model.api.operators.GraphCollectionOperators;
import org.gradoop.model.api.operators.UnaryCollectionToCollectionOperator;
import org.gradoop.model.api.operators.UnaryCollectionToGraphOperator;
import org.gradoop.model.api.operators.UnaryGraphToGraphOperator;
import org.gradoop.model.impl.functions.Predicate;
import org.gradoop.model.impl.functions.filterfunctions.EdgeInGraphFilter;
import org.gradoop.model.impl.functions.filterfunctions.EdgeInGraphsFilter;
import org.gradoop.model.impl.functions.filterfunctions
  .EdgeInGraphsFilterWithBC;
import org.gradoop.model.impl.functions.filterfunctions.VertexInGraphFilter;
import org.gradoop.model.impl.functions.filterfunctions.VertexInGraphsFilter;
import org.gradoop.model.impl.functions.filterfunctions
  .VertexInGraphsFilterWithBC;
import org.gradoop.model.impl.functions.keyselectors.GraphKeySelector;
import org.gradoop.model.impl.id.GradoopId;
import org.gradoop.model.impl.id.GradoopIds;
import org.gradoop.model.impl.operators.collection.binary.Difference;
import org.gradoop.model.impl.operators.collection.binary.DifferenceUsingList;
import org.gradoop.model.impl.operators.collection.binary.Intersect;
import org.gradoop.model.impl.operators.collection.binary.IntersectUsingList;
import org.gradoop.model.impl.operators.collection.binary.Union;
import org.gradoop.util.GradoopFlinkConfig;
import org.gradoop.util.Order;

import java.util.Arrays;
import java.util.List;

/**
 * Represents a collection of graphs inside the EPGM. As graphs may share
 * vertices and edges, the collections contains a single gelly graph
 * representing all subgraphs. Graph data is stored in an additional dataset.
 *
 * @param <VD> EPGM vertex type
 * @param <ED> EPGM edge type
 * @param <GD> EPGM graph head type
 */
public class GraphCollection<
  VD extends EPGMVertex,
  ED extends EPGMEdge,
  GD extends EPGMGraphHead>
  extends AbstractGraph<VD, ED, GD>
  implements GraphCollectionOperators<VD, ED, GD> {

  /**
   * Graph data associated with the logical graphs in that collection.
   */
  private DataSet<GD> graphHeads;

  /**
   * Creates a graph collection from the given arguments.
   *
   * @param vertices    vertices
   * @param edges       edges
   * @param graphHeads  graph heads
   * @param config      Gradoop Flink configuration
   */
  public GraphCollection(DataSet<VD> vertices,
    DataSet<ED> edges,
    DataSet<GD> graphHeads,
    GradoopFlinkConfig<VD, ED, GD> config) {
    super(vertices, edges, config);
    this.graphHeads = graphHeads;
  }

  /**
   * Returns the graph heads associated with the logical graphs in that
   * collection.
   *
   * @return graph heads
   */
  public DataSet<GD> getGraphHeads() {
    return this.graphHeads;
  }

  /**
   * {@inheritDoc}
   */
  @SuppressWarnings("unchecked")
  @Override
  public LogicalGraph<VD, ED, GD> getGraph(final GradoopId graphID) throws
    Exception {
    // filter vertices and edges based on given graph id
    DataSet<VD> vertices = getVertices()
      .filter(new VertexInGraphFilter<VD>(graphID));
    DataSet<ED> edges = getEdges()
      .filter(new EdgeInGraphFilter<ED>(graphID));

    DataSet<Tuple1<GradoopId>> graphIDDataSet = getConfig()
      .getExecutionEnvironment()
      .fromCollection(Lists.newArrayList(new Tuple1<>(graphID)));

    // get graph data based on graph id
    List<GD> graphData = this.graphHeads
      .joinWithTiny(graphIDDataSet)
      .where(new GraphKeySelector<GD>())
      .equalTo(0)
      .with(new JoinFunction<GD, Tuple1<GradoopId>, GD>() {
        @Override
        public GD join(GD g, Tuple1<GradoopId> gID) throws Exception {
          return g;
        }
      }).first(1).collect();

    return (graphData.size() > 0) ? LogicalGraph
      .fromDataSets(vertices, edges, graphData.get(0), getConfig()) : null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public GraphCollection<VD, ED, GD> getGraphs(final GradoopId... identifiers) throws
    Exception {

    GradoopIds graphIds = new GradoopIds();

    for(GradoopId id : identifiers) {
      graphIds.add(id);
    }

    return getGraphs(graphIds);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public GraphCollection<VD, ED, GD> getGraphs(
    final GradoopIds identifiers) throws Exception {

    DataSet<GD> newGraphHeads =
      this.graphHeads.filter(new FilterFunction<GD>() {

        @Override
        public boolean filter(GD graphHead) throws Exception {
          return identifiers.contains(graphHead.getId());

        }
      });

    // build new vertex set
    DataSet<VD> vertices = getVertices()
      .filter(new VertexInGraphsFilter<VD>(identifiers));

    // build new edge set
    DataSet<ED> edges = getEdges()
      .filter(new EdgeInGraphsFilter<ED>(identifiers));

    return new GraphCollection<>(vertices, edges, newGraphHeads, getConfig());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public long getGraphCount() throws Exception {
    return this.graphHeads.count();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public GraphCollection<VD, ED, GD> filter(
    final Predicate<GD> predicateFunction) throws Exception {
    // find graph heads matching the predicate
    DataSet<GD> filteredGraphHeads =
      this.graphHeads.filter(new FilterFunction<GD>() {
        @Override
        public boolean filter(GD g) throws Exception {
          return predicateFunction.filter(g);
        }
      });

    // get the identifiers of these subgraphs
    DataSet<GradoopId> graphIDs =
      filteredGraphHeads.map(new MapFunction<GD, GradoopId>() {
        @Override
        public GradoopId map(GD g) throws Exception {
          return g.getId();
        }
      });

    // use graph ids to filter vertices from the actual graph structure
    DataSet<VD> vertices = getVertices()
      .filter(new VertexInGraphsFilterWithBC<VD>())
      .withBroadcastSet(graphIDs, VertexInGraphsFilterWithBC.BC_IDENTIFIERS);
    DataSet<ED> edges = getEdges()
      .filter(new EdgeInGraphsFilterWithBC<ED>())
      .withBroadcastSet(graphIDs, EdgeInGraphsFilterWithBC.BC_IDENTIFIERS);

    return new GraphCollection<>(vertices, edges, filteredGraphHeads,
      getConfig());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public GraphCollection<VD, ED, GD> select(
    Predicate<LogicalGraph<VD, ED, GD>> predicateFunction) throws Exception {
    throw new NotImplementedException();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public GraphCollection<VD, ED, GD> union(
    GraphCollection<VD, ED, GD> otherCollection) throws Exception {
    return callForCollection(new Union<VD, ED, GD>(), otherCollection);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public GraphCollection<VD, ED, GD> intersect(
    GraphCollection<VD, ED, GD> otherCollection) throws Exception {
    return callForCollection(new Intersect<VD, ED, GD>(), otherCollection);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public GraphCollection<VD, ED, GD> intersectWithSmall(
    GraphCollection<VD, ED, GD> otherCollection) throws Exception {
    return callForCollection(new IntersectUsingList<VD, ED, GD>(),
      otherCollection);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public GraphCollection<VD, ED, GD> difference(
    GraphCollection<VD, ED, GD> otherCollection) throws Exception {
    return callForCollection(new Difference<VD, ED, GD>(), otherCollection);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public GraphCollection<VD, ED, GD> differenceWithSmallResult(
    GraphCollection<VD, ED, GD> otherCollection) throws Exception {
    return callForCollection(new DifferenceUsingList<VD, ED, GD>(),
      otherCollection);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public GraphCollection<VD, ED, GD> distinct() {
    throw new NotImplementedException();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public GraphCollection<VD, ED, GD> sortBy(String propertyKey, Order order) {
    throw new NotImplementedException();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public GraphCollection<VD, ED, GD> top(int limit) {
    throw new NotImplementedException();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public GraphCollection<VD, ED, GD> apply(
    UnaryGraphToGraphOperator<VD, ED, GD> op) {
    throw new NotImplementedException();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public LogicalGraph<VD, ED, GD> reduce(
    BinaryGraphToGraphOperator<VD, ED, GD> op) {
    throw new NotImplementedException();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public GraphCollection<VD, ED, GD> callForCollection(
    UnaryCollectionToCollectionOperator<VD, ED, GD> op) {
    return op.execute(this);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public GraphCollection<VD, ED, GD> callForCollection(
    BinaryCollectionToCollectionOperator<VD, ED, GD> op,
    GraphCollection<VD, ED, GD> otherCollection) throws Exception {
    return op.execute(this, otherCollection);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public LogicalGraph<VD, ED, GD> callForGraph(
    UnaryCollectionToGraphOperator<VD, ED, GD> op) {
    return op.execute(this);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void writeAsJson(String vertexFile, String edgeFile,
    String graphFile) throws Exception {
    getVertices().writeAsFormattedText(vertexFile,
      new JsonWriter.VertexTextFormatter<VD>());
    getEdges().writeAsFormattedText(edgeFile,
      new JsonWriter.EdgeTextFormatter<ED>());
    getGraphHeads().writeAsFormattedText(graphFile,
      new JsonWriter.GraphTextFormatter<GD>());
    getConfig().getExecutionEnvironment().execute();
  }
}
