package org.onebusaway.king_county_metro.service_alerts.model;

import java.io.Serializable;
import java.util.List;

import org.onebusaway.geospatial.model.CoordinatePoint;

public class RerouteBean implements Serializable {

  private static final long serialVersionUID = 1L;

  private Number id;

  private String regionId;

  private String routeId;

  private String directionId;

  private List<String> stopIds;

  private List<CoordinatePoint> reroute;

  private String reroteAsPolylineString;

  private String routeShortName;

  private String description;

  public Number getId() {
    return id;
  }

  public void setId(Number id) {
    this.id = id;
  }

  public String getRegionId() {
    return regionId;
  }

  public void setRegionId(String regionId) {
    this.regionId = regionId;
  }

  public String getRouteId() {
    return routeId;
  }

  public void setRouteId(String routeId) {
    this.routeId = routeId;
  }

  public String getDirectionId() {
    return directionId;
  }

  public void setDirectionId(String directionId) {
    this.directionId = directionId;
  }

  public List<String> getStopIds() {
    return stopIds;
  }

  public void setStopIds(List<String> stopIds) {
    this.stopIds = stopIds;
  }

  public List<CoordinatePoint> getReroute() {
    return reroute;
  }

  public void setReroute(List<CoordinatePoint> reroute) {
    this.reroute = reroute;
  }

  public String getReroteAsPolylineString() {
    return reroteAsPolylineString;
  }

  public void setReroteAsPolylineString(String reroteAsPolylineString) {
    this.reroteAsPolylineString = reroteAsPolylineString;
  }

  public String getRouteShortName() {
    return routeShortName;
  }

  public void setRouteShortName(String routeShortName) {
    this.routeShortName = routeShortName;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }
}
