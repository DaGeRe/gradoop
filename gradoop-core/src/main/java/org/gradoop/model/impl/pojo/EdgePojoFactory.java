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

package org.gradoop.model.impl.pojo;

import org.gradoop.model.impl.id.GradoopId;
import org.gradoop.model.impl.id.GradoopIds;
import org.gradoop.util.GConstants;
import org.gradoop.model.api.EPGMEdgeFactory;

import java.util.Map;
import java.util.Set;

/**
 * Factory for creating default edge data.
 */
public class EdgePojoFactory extends ElementPojoFactory implements
  EPGMEdgeFactory<EdgePojo> {

  /**
   * serial version uid
   */
  private static final long serialVersionUID = 42L;

  /**
   * {@inheritDoc}
   */
  @Override
  public EdgePojo createEdge(final GradoopId id, final GradoopId sourceVertexId,
    final GradoopId targetVertexId) {
    return createEdge(id, GConstants.DEFAULT_EDGE_LABEL, sourceVertexId,
      targetVertexId);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public EdgePojo createEdge(final GradoopId id, final String label,
    final GradoopId sourceVertexId, final GradoopId targetVertexId) {
    return createEdge(id, label, sourceVertexId, targetVertexId, null, null);
  }
  
  public EdgePojo createEdge(final Long id, final String label,
        final Long sourceVertexId, final Long targetVertexId) {
        return createEdge(new GradoopId(id), label, new GradoopId(sourceVertexId), new GradoopId(targetVertexId), null, null);
      }

  /**
   * {@inheritDoc}
   */
  @Override
  public EdgePojo createEdge(GradoopId id, String label, GradoopId sourceVertexId,
    GradoopId targetVertexId, Map<String, Object> properties) {
    return createEdge(id, label, sourceVertexId, targetVertexId, properties,
      null);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public EdgePojo createEdge(final GradoopId id, final String label,
    final GradoopId sourceVertexId, final GradoopId targetVertexId,
    GradoopIds graphs) {
    return createEdge(id, label, sourceVertexId, targetVertexId, null, graphs);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public EdgePojo createEdge(final GradoopId id, final String label,
    final GradoopId sourceVertexId, final GradoopId targetVertexId,
    final Map<String, Object> properties, GradoopIds graphIds) {
    checkId(id);
    checkLabel(label);
    checkId(sourceVertexId);
    checkId(targetVertexId);

    return new EdgePojo(id, label, sourceVertexId, targetVertexId,
      properties, graphIds);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Class<EdgePojo> getType() {
    return EdgePojo.class;
  }
}
