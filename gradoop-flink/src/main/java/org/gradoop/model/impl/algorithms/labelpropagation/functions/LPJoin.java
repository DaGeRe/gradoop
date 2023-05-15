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

package org.gradoop.model.impl.algorithms.labelpropagation.functions;

import org.apache.flink.api.common.functions.JoinFunction;
import org.apache.flink.graph.Vertex;
import org.gradoop.model.api.EPGMVertex;
import org.gradoop.model.impl.algorithms.epgmlabelpropagation
  .EPGMLabelPropagationAlgorithm;
import org.gradoop.model.impl.algorithms.labelpropagation.pojos.LPVertexValue;
import org.gradoop.model.impl.id.GradoopId;

/**
 * JoinFunction over VertexIDs
 *
 * @param <VD> EPGM vertex type
 */
public class LPJoin<VD extends EPGMVertex>
  implements JoinFunction<Vertex<GradoopId, LPVertexValue>, VD, VD> {

  @Override
  public VD join(Vertex<GradoopId, LPVertexValue> lpVertex,
    VD epgmVertex) throws Exception {
    epgmVertex.setProperty(
      EPGMLabelPropagationAlgorithm.CURRENT_VALUE,
      lpVertex.getValue().getCurrentCommunity());
    return epgmVertex;
  }
}
