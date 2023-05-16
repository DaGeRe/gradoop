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

package org.gradoop.storage.impl.hbase;

import org.gradoop.model.api.EPGMGraphHead;
import org.gradoop.model.impl.id.GradoopId;
import org.gradoop.model.impl.id.GradoopIds;
import org.gradoop.storage.api.PersistentGraphHead;

/**
 * Represents a persistent vertex data object.
 */
public class HBaseGraphHead extends HBaseElement<EPGMGraphHead> implements PersistentGraphHead {

  /**
   * Vertex identifiers contained in that logical graph.
   */
  private GradoopIds vertexIds;

  /**
   * Edge identifiers contained in that logical graph.
   */
  private GradoopIds edgeIds;

  /**
   * Default constructor.
   */
  public HBaseGraphHead() {
  }

  /**
   * Creates  persistent graph data.
   *
   * @param graphHead encapsulated graph data
   * @param vertexIds  vertexIds contained in that graph
   * @param edgeIds     edgeIds contained in that graph
   */
  public HBaseGraphHead(EPGMGraphHead graphHead, GradoopIds vertexIds,
    GradoopIds edgeIds) {
    super(graphHead);
    this.vertexIds = vertexIds;
    this.edgeIds = edgeIds;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public GradoopIds getVertexIds() {
    return vertexIds;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setVertexIds(GradoopIds vertices) {
    this.vertexIds = vertices;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void addVertexId(GradoopId vertexID) {
    if (vertexIds != null) {
      vertexIds.add(vertexID);
    } else {
      vertexIds = new GradoopIds(vertexID);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public long getVertexCount() {
    return (vertexIds != null) ? vertexIds.size() : 0;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public GradoopIds getEdgeIds() {
    return edgeIds;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setEdgeIds(GradoopIds edgeIds) {
    this.edgeIds = edgeIds;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void addEdgeId(GradoopId edgeID) {
    if (edgeIds != null) {
      edgeIds.add(edgeID);
    } else {
      edgeIds = new GradoopIds(edgeID);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public long getEdgeCount() {
    return (edgeIds != null) ? edgeIds.size() : 0;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("HBaseGraphHead{");
    sb.append("super=").append(super.toString());
    sb.append(", vertexIds=").append(vertexIds);
    sb.append(", edgeIds=").append(edgeIds);
    sb.append('}');
    return sb.toString();
  }
}
