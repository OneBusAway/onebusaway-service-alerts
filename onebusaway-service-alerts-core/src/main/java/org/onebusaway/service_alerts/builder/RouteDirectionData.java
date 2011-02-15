package org.onebusaway.service_alerts.builder;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.onebusaway.collections.DirectedGraph;
import org.onebusaway.collections.tuple.Pair;
import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.transit_data.model.StopBean;

class RouteDirectionData {

  private String routeId;

  private String directionId;

  private String directionName;

  private List<List<CoordinatePoint>> allPoints;

  private DirectedGraph<CoordinatePoint> graph;

  private List<String> polylineStrings;

  private List<StopBean> stops;

  private Map<Pair<CoordinatePoint>, Set<StopBean>> stopsByEdge;

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

  public String getDirectionName() {
    return directionName;
  }

  public void setDirectionName(String directionName) {
    this.directionName = directionName;
  }

  public List<StopBean> getStops() {
    return stops;
  }

  public void setStops(List<StopBean> stops) {
    this.stops = stops;
  }

  public List<List<CoordinatePoint>> getAllPoints() {
    return allPoints;
  }

  public void setAllPoints(List<List<CoordinatePoint>> allPoints) {
    this.allPoints = allPoints;
  }

  public DirectedGraph<CoordinatePoint> getGraph() {
    return graph;
  }

  public void setGraph(DirectedGraph<CoordinatePoint> graph) {
    this.graph = graph;
  }

  public List<String> getPolylineStrings() {
    return polylineStrings;
  }

  public void setPolylineStrings(List<String> polylineStrings) {
    this.polylineStrings = polylineStrings;
  }

  public Map<Pair<CoordinatePoint>, Set<StopBean>> getStopsByEdge() {
    return stopsByEdge;
  }

  public void setStopsByEdge(
      Map<Pair<CoordinatePoint>, Set<StopBean>> stopsByEdge) {
    this.stopsByEdge = stopsByEdge;
  }
}