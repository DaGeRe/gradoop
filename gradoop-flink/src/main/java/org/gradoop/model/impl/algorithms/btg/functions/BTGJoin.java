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

package org.gradoop.model.impl.algorithms.btg.functions;

import org.apache.flink.api.common.functions.JoinFunction;
import org.apache.flink.graph.Vertex;
import org.gradoop.model.api.EPGMVertex;
import org.gradoop.model.impl.algorithms.btg.BTG;
import org.gradoop.model.impl.algorithms.btg.pojos.BTGVertexValue;
import org.gradoop.model.impl.id.GradoopId;

/**
 * JoinFunction over VertexIDs
 *
 * @param <VD> EPGM vertex type
 */
public class BTGJoin<VD extends EPGMVertex>
  implements JoinFunction<
  Vertex<GradoopId, BTGVertexValue>, VD, VD> {
  @Override
  public VD join(Vertex<GradoopId, BTGVertexValue> btgVertex, VD epVertex)
      throws Exception {
    epVertex.setProperty(BTG.VERTEX_TYPE_PROPERTYKEY,
      btgVertex.getValue().getVertexType());
    epVertex.setProperty(BTG.VERTEX_VALUE_PROPERTYKEY,
      btgVertex.getValue().getVertexValue());
    epVertex.setProperty(BTG.VERTEX_BTGIDS_PROPERTYKEY,
      btgVertex.getValue().getGraphs());
    return epVertex;
  }
}
