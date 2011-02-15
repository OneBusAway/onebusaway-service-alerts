package org.onebusaway.service_alerts.model;

public class AlertDescriptionKey {

  private final RouteAndRegionRef routeAndRegion;

  private final String description;

  public AlertDescriptionKey(RouteAndRegionRef routeAndRegion,
      String description) {
    this.routeAndRegion = routeAndRegion;
    this.description = description;
  }

  public AlertDescriptionKey(String routeId, String region, String description) {
    this.routeAndRegion = new RouteAndRegionRef(routeId, region);
    this.description = description;
  }

  public RouteAndRegionRef getRouteAndRegion() {
    return routeAndRegion;
  }

  public String getDescription() {
    return description;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result
        + ((description == null) ? 0 : description.hashCode());
    result = prime * result
        + ((routeAndRegion == null) ? 0 : routeAndRegion.hashCode());
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
    AlertDescriptionKey other = (AlertDescriptionKey) obj;
    if (description == null) {
      if (other.description != null)
        return false;
    } else if (!description.equals(other.description))
      return false;
    if (routeAndRegion == null) {
      if (other.routeAndRegion != null)
        return false;
    } else if (!routeAndRegion.equals(other.routeAndRegion))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "AlertDescriptionKey(routeAndRegion=" + routeAndRegion
        + ", description=" + description + ")";
  }
}
