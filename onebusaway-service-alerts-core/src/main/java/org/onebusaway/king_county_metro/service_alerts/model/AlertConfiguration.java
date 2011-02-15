package org.onebusaway.king_county_metro.service_alerts.model;

import java.io.Serializable;
import java.util.List;

import org.onebusaway.geospatial.model.CoordinatePoint;

public class AlertConfiguration implements Serializable {

  private static final long serialVersionUID = 1L;

  private String id;

  private List<String> descriptions;

  private String routeId;

  private String region;

  private String directionId;

  private List<String> stopIds;

  private EAlertType type;

  private List<CoordinatePoint> reroute;

  private String reroteAsPolylineString;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public RouteAndRegionRef getRouteAndRegion() {
    return new RouteAndRegionRef(routeId, region);
  }

  public List<String> getDescriptions() {
    return descriptions;
  }

  public void setDescriptions(List<String> descriptions) {
    this.descriptions = descriptions;
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

  public EAlertType getType() {
    return type;
  }

  public void setType(EAlertType type) {
    this.type = type;
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
}
