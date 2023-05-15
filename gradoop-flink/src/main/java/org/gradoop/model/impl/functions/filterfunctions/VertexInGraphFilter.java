/*
 * This file is part of gradoop.
 *
 * gradoop is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * gradoop is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with gradoop. If not, see <http://www.gnu.org/licenses/>.
 */

package org.gradoop.model.impl.functions.filterfunctions;


import org.apache.flink.api.common.functions.FilterFunction;
import org.gradoop.model.api.EPGMVertex;
import org.gradoop.model.impl.id.GradoopId;

/**
 * Checks if a vertex is contained in the given graph.
 *
 * @param <VD> EPGM vertex type
 */
public class VertexInGraphFilter<VD extends EPGMVertex>
  implements FilterFunction<VD> {

  /**
   * Graph identifier
   */
  private final GradoopId graphId;

  /**
   * Creates a filter
   *
   * @param graphId graphId for containment check
   */
  public VertexInGraphFilter(GradoopId graphId) {
    this.graphId = graphId;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean filter(VD vertex) throws Exception {
    return (vertex.getGraphCount() > 0) &&
      vertex.getGraphIds().contains(graphId);
  }
}

