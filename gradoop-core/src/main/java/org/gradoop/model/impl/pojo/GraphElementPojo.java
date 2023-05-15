package org.gradoop.model.impl.pojo;

import org.gradoop.model.api.EPGMGraphElement;
import org.gradoop.model.impl.id.GradoopId;
import org.gradoop.model.impl.id.GradoopIds;

import java.util.Map;

/**
 * Abstract class representing an EPGM element that is containd in logical
 * graphIds (i.e., vertices and edge).
 */
public abstract class GraphElementPojo extends ElementPojo implements
  EPGMGraphElement {

  /**
   * Set of graph identifiers that element is contained in
   */
  private GradoopIds graphIds;

  /**
   * Default constructor.
   */
  protected GraphElementPojo() {
  }

  /**
   * Creates an EPGM graph element using the given arguments.
   *  @param id         element id
   * @param label      element label
   * @param properties element properties
   * @param graphIds     graphIds that element is contained in
   */
  protected GraphElementPojo(GradoopId id, String label,
    Map<String, Object> properties, GradoopIds graphIds) {
    super(id, label, properties);
    this.graphIds = graphIds;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public GradoopIds getGraphIds() {
    return graphIds;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void addGraphId(GradoopId graphId) {
    if (graphIds == null) {
      graphIds = new GradoopIds();
    }
    graphIds.add(graphId);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setGraphIds(GradoopIds graphIds) {
    this.graphIds = graphIds;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void resetGraphIds() {
    if (graphIds != null) {
      graphIds.clear();
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getGraphCount() {
    return (graphIds != null) ? graphIds.size() : 0;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return "EPGMGraphElement{" +
      super.toString() +
      ", graphIds=" + graphIds +
      '}';
  }
}
