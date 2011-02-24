package org.onebusaway.service_alerts.model;

import org.onebusaway.service_alerts.model.properties.AlertProperties;

public class AlertDescription {

  private String id;

  private AlertProperties group;

  private AlertProperties key;

  private AlertProperties properties = new AlertProperties();

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public AlertProperties getKey() {
    return key;
  }

  public void setKey(AlertProperties key) {
    this.key = key;
  }

  public AlertProperties getGroup() {
    return group;
  }

  public void setGroup(AlertProperties group) {
    this.group = group;
  }

  public AlertProperties getProperties() {
    return properties;
  }
}
