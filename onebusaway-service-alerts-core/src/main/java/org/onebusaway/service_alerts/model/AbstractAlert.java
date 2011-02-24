package org.onebusaway.service_alerts.model;

import java.io.Serializable;

import org.onebusaway.service_alerts.model.properties.AlertProperties;

public class AbstractAlert implements Serializable {

  private static final long serialVersionUID = 1L;

  private String id;

  private long timeOfCreation;

  private long timeOfLastUpdate;

  private String dataSourceId;

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

  public String getDataSourceId() {
    return dataSourceId;
  }

  public void setDataSourceId(String dataSourceId) {
    this.dataSourceId = dataSourceId;
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
