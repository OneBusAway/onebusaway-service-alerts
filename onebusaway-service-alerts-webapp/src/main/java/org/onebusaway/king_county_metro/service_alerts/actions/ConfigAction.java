package org.onebusaway.king_county_metro.service_alerts.actions;

import org.onebusaway.presentation.services.cachecontrol.CacheControl;

@CacheControl(maxAge = 60 * 60)
public class ConfigAction extends
    org.onebusaway.presentation.impl.configuration.ConfigAction {

  private static final long serialVersionUID = 1L;

}
