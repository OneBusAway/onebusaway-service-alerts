package org.onebusaway.service_alerts.model;

public class AlertDescription {

  private String id;

  private String routeId;

  private String region;

  private String neighborhood;

  private String description;

  private String url;

  private String mapUrl;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
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

  public RouteAndRegionRef getRef() {
    return new RouteAndRegionRef(routeId, region);
  }

  public String getNeighborhood() {
    return neighborhood;
  }

  public void setNeighborhood(String neighborhood) {
    this.neighborhood = neighborhood;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getMapUrl() {
    return mapUrl;
  }

  public void setMapUrl(String mapUrl) {
    this.mapUrl = mapUrl;
  }

  public boolean allPropertiesAreEqual(AlertDescription other) {
    if (description == null) {
      if (other.description != null)
        return false;
    } else if (!description.equals(other.description))
      return false;
    if (id == null) {
      if (other.id != null)
        return false;
    } else if (!id.equals(other.id))
      return false;
    if (mapUrl == null) {
      if (other.mapUrl != null)
        return false;
    } else if (!mapUrl.equals(other.mapUrl))
      return false;
    if (neighborhood == null) {
      if (other.neighborhood != null)
        return false;
    } else if (!neighborhood.equals(other.neighborhood))
      return false;
    if (region == null) {
      if (other.region != null)
        return false;
    } else if (!region.equals(other.region))
      return false;
    if (routeId == null) {
      if (other.routeId != null)
        return false;
    } else if (!routeId.equals(other.routeId))
      return false;
    if (url == null) {
      if (other.url != null)
        return false;
    } else if (!url.equals(other.url))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "ActiveReroute(route=" + routeId + " region=" + region
        + " neighborhood " + neighborhood + " desc=" + description + " url="
        + url + " mapUrl=" + mapUrl + ")";
  }

}
