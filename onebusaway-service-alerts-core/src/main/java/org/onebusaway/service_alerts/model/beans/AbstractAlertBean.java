package org.onebusaway.service_alerts.model.beans;

import org.onebusaway.service_alerts.model.properties.AlertProperties;

public class AbstractAlertBean {

  private String id;

  private long timeOfCreation;

  private long timeOfLastUpdate;

  private AlertProperties group;

  private AlertProperties key;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public long getTimeOfCreation() {
    return timeOfCreation;
  }

  public void setTimeOfCreation(long timeOfCreation) {
    this.timeOfCreation = timeOfCreation;
  }

  public long getTimeOfLastUpdate() {
    return timeOfLastUpdate;
  }

  public void setTimeOfLastUpdate(long timeOfLastUpdate) {
    this.timeOfLastUpdate = timeOfLastUpdate;
  }

  public AlertProperties getGroup() {
    return group;
  }

  public void setGroup(AlertProperties group) {
    this.group = group;
  }

  public AlertProperties getKey() {
    return key;
  }

  public void setKey(AlertProperties key) {
    this.key = key;
  }
}
