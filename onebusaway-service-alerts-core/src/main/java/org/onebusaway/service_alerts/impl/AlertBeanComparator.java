package org.onebusaway.service_alerts.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.onebusaway.service_alerts.model.beans.AbstractAlertBean;
import org.onebusaway.service_alerts.model.properties.AlertProperties;
import org.onebusaway.service_alerts.model.properties.AlertProperty;

public class AlertBeanComparator implements Comparator<AbstractAlertBean> {

  @Override
  public int compare(AbstractAlertBean o1, AbstractAlertBean o2) {

    AlertProperties key1 = o1.getKey();
    AlertProperties key2 = o2.getKey();

    Set<String> allKeys = new HashSet<String>();
    allKeys.addAll(key1.getKeys());
    allKeys.addAll(key2.getKeys());

    List<String> allKeysInOrder = new ArrayList<String>(allKeys);
    Collections.sort(allKeysInOrder);

    for (String key : allKeysInOrder) {
      AlertProperty p1 = key1.getProperty(key);
      AlertProperty p2 = key2.getProperty(key);

      String v1 = "";
      String v2 = "";

      if (p1 != null)
        v1 = p1.getValue();
      if (p2 != null)
        v2 = p2.getValue();

      int rc = v1.compareTo(v2);

      if (rc != 0)
        return rc;
    }

    return 0;
  }
}
