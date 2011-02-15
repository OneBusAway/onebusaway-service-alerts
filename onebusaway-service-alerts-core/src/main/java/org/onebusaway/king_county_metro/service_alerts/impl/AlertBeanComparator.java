package org.onebusaway.king_county_metro.service_alerts.impl;

import java.util.Comparator;

import org.onebusaway.king_county_metro.service_alerts.model.beans.AbstractAlertBean;
import org.onebusaway.utility.text.NaturalStringOrder;

public class AlertBeanComparator implements Comparator<AbstractAlertBean> {

  @Override
  public int compare(AbstractAlertBean o1, AbstractAlertBean o2) {

    String routeId1 = "";
    String routeId2 = "";

    if (o1.getRoute() != null)
      routeId1 = o1.getRoute().getId();
    if (o2.getRoute() != null)
      routeId2 = o2.getRoute().getId();

    int c = NaturalStringOrder.compareNatural(routeId1, routeId2);

    if (c != 0)
      return c;

    c = o1.getRegion().compareTo(o2.getRegion());

    if (c != 0)
      return c;

    return o1.getDescription().compareTo(o2.getDescription());
  }
}
