package org.onebusaway.service_alerts.model.beans;

import org.onebusaway.service_alerts.model.RouteAndRegionRef;
import org.onebusaway.transit_data.model.RouteBean;

public class AbstractAlertBean {

  private String id;

  private long timeOfCreation;

  private long timeOfLastUpdate;

  private RouteBean route;

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

  public RouteBean getRoute() {
    return route;
  }

  public void setRoute(RouteBean route) {
    this.route = route;
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
  
  public RouteAndRegionRef getAsRouteAndRegion() {
    return new RouteAndRegionRef(route.getId(), region);
  }
}
