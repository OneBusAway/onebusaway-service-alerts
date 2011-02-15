package org.onebusaway.king_county_metro.service_alerts.model;

public class NotFoundException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  private final Class<?> entityType;

  private final Object id;

  public NotFoundException(Class<?> entityType, Object id) {
    super("not found: entityType=" + entityType.getName() + " id=" + id);
    this.entityType = entityType;
    this.id = id;
  }

  public Class<?> getEntityType() {
    return entityType;
  }

  public Object getId() {
    return id;
  }
}
