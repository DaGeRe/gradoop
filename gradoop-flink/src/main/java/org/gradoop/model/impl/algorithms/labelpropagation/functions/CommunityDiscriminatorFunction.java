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

import org.gradoop.model.api.EPGMVertex;
import org.gradoop.model.impl.functions.UnaryFunction;
import org.gradoop.model.impl.id.GradoopId;

/**
 * Given a vertex, the method returns the community id, that vertex is in. This
 * is used by {@link org.gradoop.model.impl.operators.auxiliary.SplitBy} and
 * {@link org.gradoop.model.impl.operators.auxiliary.OverlapSplitBy}.
 *
 * @param <VD> EPGM vertex type
 */
public class CommunityDiscriminatorFunction<VD extends EPGMVertex> implements
  UnaryFunction<VD, GradoopId> {

  /**
   * Property key to retrieve property value.
   */
  private final String propertyKey;

  /**
   * Creates a new function instance based on the given arguments.
   *
   * @param propertyKey property key to retrieve value
   */
  public CommunityDiscriminatorFunction(String propertyKey) {
    this.propertyKey = propertyKey;
  }

  @Override
  public GradoopId execute(VD entity) throws Exception {
    Object val = entity.getProperty(propertyKey);

    if (val != null && val instanceof GradoopId) {
      return (GradoopId) val;
    } else {
      throw new IllegalArgumentException(
        "non-valid property value for cluster identification");
    }
  }
}
