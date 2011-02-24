package org.onebusaway.service_alerts.impl;

import java.util.Comparator;

import org.onebusaway.service_alerts.model.SituationConfiguration;
import org.onebusaway.transit_data.model.service_alerts.NaturalLanguageStringBean;
import org.onebusaway.transit_data.model.service_alerts.SituationBean;

public class SituationConfigSummaryComparator implements
    Comparator<SituationConfiguration> {

  public static final SituationConfigSummaryComparator INSTANCE = new SituationConfigSummaryComparator();

  @Override
  public int compare(SituationConfiguration o1, SituationConfiguration o2) {
    String summaryA = getSummaryForSituationConfiguration(o1);
    String summaryB = getSummaryForSituationConfiguration(o2);
    return summaryA.compareTo(summaryB);
  }

  private String getSummaryForSituationConfiguration(
      SituationConfiguration config) {
    SituationBean situation = config.getSituation();
    if (situation == null)
      return "";
    NaturalLanguageStringBean summary = situation.getSummary();
    if (summary == null)
      return "";
    String value = summary.getValue();
    if (value == null)
      return "";
    return value;
  }

}
