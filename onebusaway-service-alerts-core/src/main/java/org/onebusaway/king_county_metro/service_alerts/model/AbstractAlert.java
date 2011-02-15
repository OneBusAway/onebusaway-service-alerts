package org.onebusaway.king_county_metro.service_alerts.model;

public class AbstractAlert {

  private String id;

  private long timeOfCreation;

  private long timeOfLastUpdate;

  private String routeId;

  private String region;

  private String description;

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

  public String getRouteId() {
    return routeId;
  }

  public void setRouteId(String routeId) {
    this.routeId = routeId;
  }

  public String getRegion() {
    return region;
  }

  public void setRegion(String region) {
    this.region = region;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public AlertDescriptionKey getKey() {
    return new AlertDescriptionKey(routeId, region, description);
  }
}
