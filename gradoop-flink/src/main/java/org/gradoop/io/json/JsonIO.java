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

package org.gradoop.io.json;

import com.google.common.collect.Maps;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.gradoop.model.api.EPGMAttributed;
import org.gradoop.model.api.EPGMGraphElement;
import org.gradoop.model.api.EPGMGraphHead;
import org.gradoop.model.api.EPGMLabeled;
import org.gradoop.model.impl.id.GradoopId;
import org.gradoop.model.impl.id.GradoopIds;

import java.util.Iterator;
import java.util.Map;

/**
 * Base class for Json reader and writer.
 *
 * @see JsonReader
 * @see JsonWriter
 */
public abstract class JsonIO {
  /**
   * Key for vertex, edge and graph id.
   */
  protected static final String IDENTIFIER = "id";
  /**
   * Key for meta Json object.
   */
  protected static final String META = "meta";
  /**
   * Key for data Json object.
   */
  protected static final String DATA = "data";
  /**
   * Key for vertex, edge and graph label.
   */
  protected static final String LABEL = "label";
  /**
   * Key for graph identifiers at vertices and edges.
   */
  protected static final String GRAPHS = "graphs";
  /**
   * Key for vertex identifiers at graphs.
   */
  protected static final String VERTICES = "vertices";
  /**
   * Key for edge identifiers at graphs.
   */
  protected static final String EDGES = "edges";
  /**
   * Key for edge source vertex id.
   */
  protected static final String EDGE_SOURCE = "source";
  /**
   * Key for edge target vertex id.
   */
  protected static final String EDGE_TARGET = "target";

  /**
   * Contains methods used by all entity readers (e.g. read label, properties).
   */
  protected abstract static class JsonToEntityMapper {

    /**
     * Default constructor.
     */
    public JsonToEntityMapper() {
    }

    /**
     * Reads the entity identifier from the json object.
     *
     * @param object json object
     * @return entity identifier
     * @throws JSONException
     */
    protected GradoopId getID(JSONObject object) throws JSONException {
      return GradoopId.fromLongString(object.getString(IDENTIFIER));
    }

    /**
     * Reads the entity label from the json object.
     *
     * @param object json object
     * @return entity label
     * @throws JSONException
     */
    protected String getLabel(JSONObject object) throws JSONException {
      return object.getJSONObject(META).getString(LABEL);
    }

    /**
     * Reads the key-value properties from the json object.
     *
     * @param object json object
     * @return key-value properties
     * @throws JSONException
     */
    protected Map<String, Object> getProperties(JSONObject object) throws
      JSONException {
      Map<String, Object> props =
        Maps.newHashMapWithExpectedSize(object.length() * 2);
      object = object.getJSONObject(DATA);
      Iterator<?> keys = object.keys();
      while (keys.hasNext()) {
        String key = keys.next().toString();
        Object o = object.get(key);
        props.put(key, o);
      }
      return props;
    }

    /**
     * Reads a set of graph identifiers from the json object.
     *
     * @param object json object
     * @return graph identifiers
     * @throws JSONException
     */
    protected GradoopIds getGraphs(JSONObject object) throws JSONException {
      GradoopIds result;
      if (!object.getJSONObject(META).has(GRAPHS)) {
        result = new GradoopIds();
      } else {
        result =
          getArrayValues(object.getJSONObject(META).getJSONArray(GRAPHS));
      }
      return result;
    }

    /**
     * Reads a set of Long values from a given json array.
     *
     * @param array json array
     * @return long values
     * @throws JSONException
     */
    protected GradoopIds getArrayValues(JSONArray array) throws JSONException {
      GradoopIds result = new GradoopIds();

      for (int i = 0; i < array.length(); i++) {
        result.add(GradoopId.fromLongString(array.getString(i)));
      }
      return result;
    }
  }

  /**
   * Contains methods used by all entity writers (e.g. write meta, data).
   */
  protected abstract static class EntityToJsonFormatter {

    /**
     * Writes all key-value properties to a json object and returns it.
     *
     * @param entity entity with key-value properties
     * @return json object containing the properties
     * @throws JSONException
     */
    protected JSONObject writeProperties(EPGMAttributed entity) throws
      JSONException {
      JSONObject data = new JSONObject();
      if (entity.getPropertyCount() > 0) {
        for (String propertyKey : entity.getPropertyKeys()) {
          data.put(propertyKey, entity.getProperty(propertyKey));
        }
      }
      return data;
    }

    /**
     * Writes all meta data regarding a labeled graph element to a json
     * object and returns it.
     *
     * @param entity labeled graph element (e.g., vertex and edge)
     * @param <T>    input element type
     * @return json object containing meta information
     * @throws JSONException
     */
    protected <T extends EPGMLabeled & EPGMGraphElement> JSONObject
    writeGraphElementMeta(
      T entity) throws JSONException {
      JSONObject meta = writeMeta(entity);
      if (entity.getGraphCount() > 0) {
        meta.put(GRAPHS, writeJsonArray(entity.getGraphIds()));
      }
      return meta;
    }

    /**
     * Writes all meta data regarding a logical graph to a json object and
     * returns it.
     *
     * @param entity logical graph data
     * @param <T>    graph data type
     * @return json object with graph meta data
     * @throws JSONException
     */
    protected <T extends EPGMGraphHead> JSONObject
    writeGraphMeta(T entity) throws JSONException {
      return writeMeta(entity);
    }

    /**
     * Writes meta data (e.g., label) to a json object and returns it.
     *
     * @param entity labeled entity
     * @return json object with meta data containing the label
     * @throws JSONException
     */
    private JSONObject writeMeta(EPGMLabeled entity) throws JSONException {
      JSONObject meta = new JSONObject();
      meta.put(LABEL, entity.getLabel());
      return meta;
    }

    /**
     * Creates a json array from a given set of identifiers.
     *
     * @param values identifier set
     * @return json array containing the identifiers
     */
    private JSONArray writeJsonArray(final GradoopIds values) {
      JSONArray jsonArray = new JSONArray();
      for (GradoopId val : values) {
        jsonArray.put(val);
      }
      return jsonArray;
    }
  }
}
