package org.onebusaway.service_alerts.services;

import java.util.List;

import org.onebusaway.service_alerts.model.SituationConfiguration;
import org.onebusaway.transit_data.model.service_alerts.SituationBean;

public interface SituationService {

  public SituationConfiguration createSituation();

  public SituationConfiguration getSituationForId(String id);

  public void deleteSituationForId(String id);

  public List<SituationConfiguration> getAllSituations();

  public SituationConfiguration updateConfigurationDetails(String id,
      SituationBean situation);

  public SituationConfiguration updateVisibility(String id, boolean visible);

  public SituationConfiguration setAffectedStopForSituation(String id,
      String stopId, boolean active);

}
