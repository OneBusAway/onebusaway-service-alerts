package org.onebusaway.service_alerts.model.properties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class AlertProperties {

  private Map<String, AlertProperty> properties = new HashMap<String, AlertProperty>();

  public void putProperty(String key, EAlertPropertyType type, String value) {
    putProperty(key, new AlertProperty(type, value));
  }

  public void putProperty(String key, AlertProperty property) {
    properties.put(key, property);
  }

  public void putEncodedProperty(String token) {

    int index = token.indexOf(':');
    if (index == -1)
      throw new IllegalArgumentException("invalid encoded property: " + token);

    String key = token.substring(0, index);
    token = token.substring(index + 1);

    index = token.indexOf(':');
    if (index == -1)
      throw new IllegalArgumentException("invalid encoded property: " + token);

    EAlertPropertyType type = EAlertPropertyType.valueOf(token.substring(0,index));
    String value = token.substring(index + 1);

    putProperty(key, type, value);
  }

  public AlertProperty getProperty(String key) {
    return properties.get(key);
  }

  public Set<String> getKeys() {
    return properties.keySet();
  }

  public AlertProperties subset(String... keys) {
    AlertProperties sub = new AlertProperties();
    for (String key : keys)
      sub.putProperty(key, getProperty(key));
    return sub;
  }

  public List<String> getAsList() {
    List<String> values = new ArrayList<String>();
    for (Map.Entry<String, AlertProperty> entry : properties.entrySet()) {
      String key = entry.getKey();
      AlertProperty prop = entry.getValue();
      String encoded = key + ':' + prop.getType() + ':' + prop.getValue();
      values.add(encoded);
    }
    return values;
  }

  /****
   * {@link Object} Interface
   ****/

  @Override
  public String toString() {
    return properties.toString();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + properties.hashCode();
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    AlertProperties other = (AlertProperties) obj;
    return (properties.equals(other.properties));
  }

}
