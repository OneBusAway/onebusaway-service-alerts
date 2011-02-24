package org.onebusaway.service_alerts.model.properties;

import java.io.Serializable;

public final class AlertProperty implements Serializable {

  private static final long serialVersionUID = 1L;

  private final EAlertPropertyType type;
 
  private final String value;

  public AlertProperty(EAlertPropertyType type, String value) {
    if (type == null)
      throw new IllegalArgumentException("type is null");
    if (value == null)
      throw new IllegalArgumentException("value is null");
    this.type = type;
    this.value = value;
  }

  public EAlertPropertyType getType() {
    return type;
  }

  public String getValue() {
    return value;
  }

  /****
   * {@link Object} Interface
   ****/

  @Override
  public String toString() {
    return value + "[" + type + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + type.hashCode();
    result = prime * result + value.hashCode();
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
    AlertProperty other = (AlertProperty) obj;
    if (type != other.type)
      return false;
    if (!value.equals(other.value))
      return false;
    return true;
  }

}