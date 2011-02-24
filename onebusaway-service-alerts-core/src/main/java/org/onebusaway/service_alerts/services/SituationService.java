package org.onebusaway.service_alerts.services;

import java.util.Collection;
import java.util.List;

import org.onebusaway.service_alerts.model.SituationConfiguration;
import org.onebusaway.service_alerts.model.properties.AlertProperties;
import org.onebusaway.transit_data.model.service_alerts.SituationBean;
import org.onebusaway.transit_data.model.service_alerts.SituationConsequenceBean;

public interface SituationService {

  public SituationConfiguration createSituation(AlertProperties group);

  public SituationConfiguration createSituation(
      SituationConfiguration configuration);

  public SituationConfiguration getSituationForId(String id);

  public List<SituationConfiguration> getAllSituations();

  public Collection<SituationConfiguration> getSituationsForGroup(
      AlertProperties group);

  public SituationConfiguration getSituationForKey(AlertProperties key);

  /****
   * 
   ****/

  public void addKeyToSituationConfiguration(SituationConfiguration config,
      AlertProperties key);

  public SituationConfiguration updateConfigurationDetails(String id,
      SituationBean situation);

  public SituationConfiguration updateVisibility(String id, boolean visible);

  public SituationConfiguration setAffectedAgencyForSituation(String id,
      String agencyId, boolean active);

  public SituationConfiguration setAffectedStopForSituation(String id,
      String stopId, boolean active);

  public SituationConfiguration setAffectedVehicleJourneyForSituation(
      String id, String routeId, String directionId, boolean active);

  public SituationConfiguration setAffectedVehicleJourneyStopCallForSituation(
      String id, String routeId, String directionId, String stopId,
      boolean active);

  public SituationConfiguration addConsequenceForSituation(String id,
      SituationConsequenceBean consequence);

  public SituationConfiguration updateConsequenceForSituation(String id,
      int index, SituationConsequenceBean consequence);

  public SituationConfiguration removeConsequenceForSituation(String id,
      int index);

  public void deleteSituationForId(String id);

}
