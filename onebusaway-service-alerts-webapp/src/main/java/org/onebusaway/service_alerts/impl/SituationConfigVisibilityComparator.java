package org.onebusaway.service_alerts.impl;

import java.util.Comparator;

import org.onebusaway.service_alerts.model.SituationConfiguration;

public class SituationConfigVisibilityComparator implements
    Comparator<SituationConfiguration> {

  public static final SituationConfigVisibilityComparator INSTANCE = new SituationConfigVisibilityComparator();

  @Override
  public int compare(SituationConfiguration o1, SituationConfiguration o2) {
    boolean v1 = o1.isVisible();
    boolean v2 = o2.isVisible();

    if (v1 == v2)
      return SituationConfigSummaryComparator.INSTANCE.compare(o1, o2);

    return v1 == false ? -1 : 1;
  }

}
