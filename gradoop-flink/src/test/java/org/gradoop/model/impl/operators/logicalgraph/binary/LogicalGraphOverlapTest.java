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

package org.gradoop.model.impl.operators.logicalgraph.binary;

import com.google.common.collect.Lists;
import org.apache.flink.api.java.io.LocalCollectionOutputFormat;
import org.gradoop.GradoopTestBaseUtils;
import org.gradoop.model.impl.LogicalGraph;
import org.gradoop.model.impl.id.GradoopId;
import org.gradoop.model.impl.id.GradoopIds;
import org.gradoop.model.impl.pojo.EdgePojo;
import org.gradoop.model.impl.pojo.GraphHeadPojo;
import org.gradoop.model.impl.pojo.VertexPojo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Collection;
import java.util.Set;

import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class LogicalGraphOverlapTest extends BinaryGraphOperatorsTestBase {

  public LogicalGraphOverlapTest(TestExecutionMode mode) {
    super(mode);
  }

  @Test
  public void testSameGraph() throws Exception {
    GradoopId firstGraph = GradoopId.fromLong(0L);
    GradoopId secondGraph = GradoopId.fromLong(0L);
    long expectedVertexCount = 3L;
    long expectedEdgeCount = 4L;

    LogicalGraph<VertexPojo, EdgePojo, GraphHeadPojo> first =
      getGraphStore().getGraph(firstGraph);
    LogicalGraph<VertexPojo, EdgePojo, GraphHeadPojo> second =
      getGraphStore().getGraph(secondGraph);

    LogicalGraph<VertexPojo, EdgePojo, GraphHeadPojo> result =
      first.overlap(second);

    performTest(result, expectedVertexCount, expectedEdgeCount);
  }

  @Test
  public void testOverlappingGraphs() throws Exception {
    GradoopId firstGraph = GradoopId.fromLong(0L);
    GradoopId secondGraph = GradoopId.fromLong(2L);
    long expectedVertexCount = 2L;
    long expectedEdgeCount = 2L;

    LogicalGraph<VertexPojo, EdgePojo, GraphHeadPojo> first =
      getGraphStore().getGraph(firstGraph);
    LogicalGraph<VertexPojo, EdgePojo, GraphHeadPojo> second =
      getGraphStore().getGraph(secondGraph);

    LogicalGraph<VertexPojo, EdgePojo, GraphHeadPojo> result =
      first.overlap(second);

    performTest(result, expectedVertexCount, expectedEdgeCount);
  }

  @Test
  public void testOverlappingSwitchedGraphs() throws Exception {
    GradoopId firstGraph = GradoopId.fromLong(2L);
    GradoopId secondGraph = GradoopId.fromLong(0L);
    long expectedVertexCount = 2L;
    long expectedEdgeCount = 2L;

    LogicalGraph<VertexPojo, EdgePojo, GraphHeadPojo> first =
      getGraphStore().getGraph(firstGraph);
    LogicalGraph<VertexPojo, EdgePojo, GraphHeadPojo> second =
      getGraphStore().getGraph(secondGraph);

    LogicalGraph<VertexPojo, EdgePojo, GraphHeadPojo> result =
      first.overlap(second);

    performTest(result, expectedVertexCount, expectedEdgeCount);
  }

  @Test
  public void testNonOverlappingGraphs() throws Exception {
    GradoopId firstGraph = GradoopId.fromLong(0L);
    GradoopId secondGraph = GradoopId.fromLong(1L);
    long expectedVertexCount = 0L;
    long expectedEdgeCount = 0L;

    LogicalGraph<VertexPojo, EdgePojo, GraphHeadPojo> first =
      getGraphStore().getGraph(firstGraph);
    LogicalGraph<VertexPojo, EdgePojo, GraphHeadPojo> second =
      getGraphStore().getGraph(secondGraph);

    LogicalGraph<VertexPojo, EdgePojo, GraphHeadPojo> result =
      first.overlap(second);

    performTest(result, expectedVertexCount, expectedEdgeCount);
  }

  @Test
  public void testNonOverlappingSwitchedGraphs() throws Exception {
    GradoopId firstGraph = GradoopId.fromLong(1L);
    GradoopId secondGraph = GradoopId.fromLong(0L);
    long expectedVertexCount = 0L;
    long expectedEdgeCount = 0L;

    LogicalGraph<VertexPojo, EdgePojo, GraphHeadPojo> first =
      getGraphStore().getGraph(firstGraph);
    LogicalGraph<VertexPojo, EdgePojo, GraphHeadPojo> second =
      getGraphStore().getGraph(secondGraph);

    LogicalGraph<VertexPojo, EdgePojo, GraphHeadPojo> result =
      first.overlap(second);

    performTest(result, expectedVertexCount, expectedEdgeCount);
  }

  @Test
  public void testOverlappingVertexSetGraphs() throws Exception {
    GradoopId firstGraph = GradoopId.fromLong(3L);
    GradoopId secondGraph = GradoopId.fromLong(1L);
    long expectedVertexCount = 2L;
    long expectedEdgeCount = 1L;

    LogicalGraph<VertexPojo, EdgePojo, GraphHeadPojo> first =
      getGraphStore().getGraph(firstGraph);
    LogicalGraph<VertexPojo, EdgePojo, GraphHeadPojo> second =
      getGraphStore().getGraph(secondGraph);

    LogicalGraph<VertexPojo, EdgePojo, GraphHeadPojo> result =
      first.overlap(second);

    performTest(result, expectedVertexCount, expectedEdgeCount);
  }

  @Test
  public void testOverlappingVertexSetSwitchedGraphs() throws Exception {
    GradoopId firstGraph = GradoopId.fromLong(1L);
    GradoopId secondGraph = GradoopId.fromLong(3L);
    long expectedVertexCount = 2L;
    long expectedEdgeCount = 1L;

    LogicalGraph<VertexPojo, EdgePojo, GraphHeadPojo> first =
      getGraphStore().getGraph(firstGraph);
    LogicalGraph<VertexPojo, EdgePojo, GraphHeadPojo> second =
      getGraphStore().getGraph(secondGraph);

    LogicalGraph<VertexPojo, EdgePojo, GraphHeadPojo> result =
      first.overlap(second);

    performTest(result, expectedVertexCount, expectedEdgeCount);
  }

  @Test
  public void testAssignment() throws Exception {
    LogicalGraph<VertexPojo, EdgePojo, GraphHeadPojo>
      databaseCommunity = getGraphStore().getGraph(GradoopId.fromLong(0L));
    LogicalGraph<VertexPojo, EdgePojo, GraphHeadPojo>
      graphCommunity = getGraphStore().getGraph(GradoopId.fromLong(2L));

    LogicalGraph<VertexPojo, EdgePojo, GraphHeadPojo>
      newGraph = graphCommunity.overlap(databaseCommunity);

    // use collections as data sink
    Collection<VertexPojo> vertexData = Lists.newArrayList();
    Collection<EdgePojo> edgeData = Lists.newArrayList();

    newGraph.getVertices()
      .output(new LocalCollectionOutputFormat<>(vertexData));
    newGraph.getEdges()
      .output(new LocalCollectionOutputFormat<>(edgeData));

    getExecutionEnvironment().execute();

    for (VertexPojo v : vertexData) {
      GradoopIds gIDs = v.getGraphIds();
      if (v.equals(GradoopTestBaseUtils.VERTEX_PERSON_ALICE)) {
        assertEquals("wrong number of graphs", 3, gIDs.size());
      } else if (v.equals(GradoopTestBaseUtils.VERTEX_PERSON_BOB)) {
        assertEquals("wrong number of graphs", 3, gIDs.size());
      }
    }

    for (EdgePojo e : edgeData) {
      GradoopIds gIDs = e.getGraphIds();
      if (e.equals(GradoopTestBaseUtils.EDGE_0_KNOWS)) {
        assertEquals("wrong number of graphs", 3, gIDs.size());
      } else if (e.equals(GradoopTestBaseUtils.EDGE_1_KNOWS)) {
        assertEquals("wrong number of graphs", 3, gIDs.size());
      }
    }
  }
}
