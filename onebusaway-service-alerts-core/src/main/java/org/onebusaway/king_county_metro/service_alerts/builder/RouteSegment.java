package org.onebusaway.king_county_metro.service_alerts.builder;

import java.util.List;

import org.onebusaway.collections.tuple.Pair;
import org.onebusaway.geospatial.model.CoordinatePoint;

public class RouteSegment {
  private RouteDirectionData data;
  private Pair<CoordinatePoint> from;
  private Pair<CoordinatePoint> to;
  private List<Pair<CoordinatePoint>> path;

  public RouteDirectionData getData() {
    return data;
  }

  public void setData(RouteDirectionData data) {
    this.data = data;
  }

  public Pair<CoordinatePoint> getFrom() {
    return from;
  }

  public void setFrom(Pair<CoordinatePoint> from) {
    this.from = from;
  }

  public Pair<CoordinatePoint> getTo() {
    return to;
  }

  public void setTo(Pair<CoordinatePoint> to) {
    this.to = to;
  }

  public List<Pair<CoordinatePoint>> getPath() {
    return path;
  }

  public void setPath(List<Pair<CoordinatePoint>> path) {
    this.path = path;
  }
}
