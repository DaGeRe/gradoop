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
 * along with Gradoop. If not, see <http://www.gnu.org/licenses/>.
 */

package org.gradoop.model.impl.operators.summarization.functions;

import org.apache.flink.api.common.functions.GroupReduceFunction;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.typeutils.ResultTypeQueryable;
import org.apache.flink.api.java.typeutils.TypeExtractor;
import org.apache.flink.util.Collector;
import org.gradoop.model.api.EPGMEdge;
import org.gradoop.model.api.EPGMEdgeFactory;
import org.gradoop.model.impl.operators.summarization.functions.aggregation.PropertyValueAggregator;
import org.gradoop.model.impl.operators.summarization.tuples.EdgeGroupItem;

import java.util.List;

/**
 * Creates a new summarized {@link EPGMEdge} from a group of
 * {@link EdgeGroupItem}.
 *
 * @param <E> EPGM edge type
 */
public class ReduceEdgeGroupItems<E extends EPGMEdge>
  extends BuildSummarizedEdge
  implements GroupReduceFunction<EdgeGroupItem, E>, ResultTypeQueryable<E> {

  /**
   * Edge factory.
   */
  private final EPGMEdgeFactory<E> edgeFactory;

  /**
   * Creates group reducer
   *
   * @param groupPropertyKeys edge property keys
   * @param useLabel          use edge label
   * @param valueAggregator   aggregate function for edge values
   * @param edgeFactory       edge factory
   */
  public ReduceEdgeGroupItems(List<String> groupPropertyKeys, boolean useLabel,
    PropertyValueAggregator valueAggregator, EPGMEdgeFactory<E> edgeFactory) {
    super(groupPropertyKeys, useLabel, valueAggregator);
    this.edgeFactory = edgeFactory;
  }

  /**
   * Reduces edge group items to a single edge group item, creates a new
   * summarized EPGM edge and collects it.
   *
   * @param edgeGroupItems  edge group items
   * @param collector       output collector
   * @throws Exception
   */
  @Override
  public void reduce(Iterable<EdgeGroupItem> edgeGroupItems,
    Collector<E> collector) throws Exception {

    EdgeGroupItem edgeGroupItem = reduceInternal(edgeGroupItems);

    E sumEdge = edgeFactory.createEdge(
      edgeGroupItem.getGroupLabel(),
      edgeGroupItem.getSourceId(),
      edgeGroupItem.getTargetId());

    setGroupProperties(sumEdge, edgeGroupItem.getGroupPropertyValues());
    setAggregate(sumEdge);
    resetAggregator();

    collector.collect(sumEdge);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @SuppressWarnings("unchecked")
  public TypeInformation<E> getProducedType() {
    return (TypeInformation<E>)
      TypeExtractor.createTypeInfo(edgeFactory.getType());
  }
}
