package org.onebusaway.service_alerts.impl;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.lang.ObjectUtils;
import org.onebusaway.collections.CollectionsLibrary;
import org.onebusaway.collections.FunctionalLibrary;
import org.onebusaway.geospatial.model.EncodedPolylineBean;
import org.onebusaway.service_alerts.model.ServiceAlertsBundle;
import org.onebusaway.service_alerts.model.SituationConfiguration;
import org.onebusaway.service_alerts.model.properties.AlertProperties;
import org.onebusaway.service_alerts.model.properties.AlertProperty;
import org.onebusaway.service_alerts.model.properties.EAlertPropertyType;
import org.onebusaway.service_alerts.services.AlertDao;
import org.onebusaway.service_alerts.services.SiriService;
import org.onebusaway.service_alerts.services.SituationService;
import org.onebusaway.siri.AffectedApplicationStructure;
import org.onebusaway.siri.OneBusAwayAffects;
import org.onebusaway.siri.OneBusAwayAffectsStructure;
import org.onebusaway.siri.OneBusAwayConsequence;
import org.onebusaway.siri.core.SiriServer;
import org.onebusaway.transit_data.model.service_alerts.ESeverity;
import org.onebusaway.transit_data.model.service_alerts.NaturalLanguageStringBean;
import org.onebusaway.transit_data.model.service_alerts.SituationAffectedAgencyBean;
import org.onebusaway.transit_data.model.service_alerts.SituationAffectedApplicationBean;
import org.onebusaway.transit_data.model.service_alerts.SituationAffectedCallBean;
import org.onebusaway.transit_data.model.service_alerts.SituationAffectedStopBean;
import org.onebusaway.transit_data.model.service_alerts.SituationAffectedVehicleJourneyBean;
import org.onebusaway.transit_data.model.service_alerts.SituationAffectsBean;
import org.onebusaway.transit_data.model.service_alerts.SituationBean;
import org.onebusaway.transit_data.model.service_alerts.SituationConditionDetailsBean;
import org.onebusaway.transit_data.model.service_alerts.SituationConsequenceBean;
import org.onebusaway.utility.ObjectSerializationLibrary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import uk.org.siri.siri.AffectedCallStructure;
import uk.org.siri.siri.AffectedOperatorStructure;
import uk.org.siri.siri.AffectedStopPointStructure;
import uk.org.siri.siri.AffectedVehicleJourneyStructure;
import uk.org.siri.siri.AffectedVehicleJourneyStructure.Calls;
import uk.org.siri.siri.AffectsScopeStructure;
import uk.org.siri.siri.AffectsScopeStructure.Operators;
import uk.org.siri.siri.AffectsScopeStructure.StopPoints;
import uk.org.siri.siri.AffectsScopeStructure.VehicleJourneys;
import uk.org.siri.siri.DefaultedTextStructure;
import uk.org.siri.siri.DirectionRefStructure;
import uk.org.siri.siri.EntryQualifierStructure;
import uk.org.siri.siri.EnvironmentReasonEnumeration;
import uk.org.siri.siri.EquipmentReasonEnumeration;
import uk.org.siri.siri.ExtensionsStructure;
import uk.org.siri.siri.LineRefStructure;
import uk.org.siri.siri.MiscellaneousReasonEnumeration;
import uk.org.siri.siri.OperatorRefStructure;
import uk.org.siri.siri.PersonnelReasonEnumeration;
import uk.org.siri.siri.PtConsequenceStructure;
import uk.org.siri.siri.PtConsequencesStructure;
import uk.org.siri.siri.PtSituationElementStructure;
import uk.org.siri.siri.ServiceConditionEnumeration;
import uk.org.siri.siri.ServiceDelivery;
import uk.org.siri.siri.SeverityEnumeration;
import uk.org.siri.siri.SituationExchangeDeliveryStructure;
import uk.org.siri.siri.SituationExchangeDeliveryStructure.Situations;
import uk.org.siri.siri.SituationVersion;
import uk.org.siri.siri.StopPointRefStructure;
import uk.org.siri.siri.WorkflowStatusEnumeration;

@Component
class SituationServiceImpl implements SituationService {

  private static final String DEFAULT_SITUATION_ID = "default-situation";

  private static Logger _log = LoggerFactory.getLogger(SituationServiceImpl.class);

  private AlertDao _dao;

  private SiriService _sirivService;

  private ServiceAlertsBundle _bundle;

  @Autowired
  public void setDao(AlertDao dao) {
    _dao = dao;
  }

  @Autowired
  public void setSiriService(SiriService siriService) {
    _sirivService = siriService;
  }

  @Autowired
  public void setServiceAlertsBundle(ServiceAlertsBundle bundle) {
    _bundle = bundle;
  }

  /****
   * 
   ****/

  @PostConstruct
  public void start() throws IOException, ClassNotFoundException {

    File path = _bundle.getSituationConfigurationsPath();

    if (path.exists()) {
      Collection<SituationConfiguration> configs = ObjectSerializationLibrary.readObject(path);
      for (SituationConfiguration config : configs) {
        // Default to not visible
        config.setVisible(false);
        _dao.addConfiguration(config);
      }
    }

    /**
     * Make sure we have a default configuration
     */
    getDefaultSituation();
  }

  @PreDestroy
  public void stop() {
    saveAllAlerts();
  }

  /****
   * 
   ****/

  @Override
  public void saveAllAlerts() {

    File path = _bundle.getSituationConfigurationsPath();

    try {
      Collection<SituationConfiguration> configs = _dao.getConfigurations();
      configs = new ArrayList<SituationConfiguration>(configs);
      ObjectSerializationLibrary.writeObject(path, configs);
    } catch (Exception ex) {
      _log.warn("error saving alerts to file " + path, ex);
    }
  }

  @Override
  public SituationConfiguration getDefaultSituation() {

    SituationConfiguration configuration = _dao.getConfigurationForId(DEFAULT_SITUATION_ID);

    if (configuration == null) {
      configuration = new SituationConfiguration();

      configuration.setId(DEFAULT_SITUATION_ID);
      configuration.setLastUpdate(System.currentTimeMillis());
      configuration.setVisible(false);
      configuration.setGroup(new AlertProperties());

      SituationBean situation = new SituationBean();
      situation.setId(DEFAULT_SITUATION_ID);
      situation.setCreationTime(System.currentTimeMillis());
      configuration.setSituation(situation);

      _dao.addConfiguration(configuration);
    }
    return configuration;
  }

  @Override
  public SituationConfiguration createSituation(AlertProperties group) {

    SituationConfiguration configuration = new SituationConfiguration();

    long t = System.currentTimeMillis();
    String id = Long.toString(t);
    configuration.setId(id);
    configuration.setLastUpdate(t);
    configuration.setVisible(false);
    configuration.setGroup(group);

    SituationBean situation = new SituationBean();
    situation.setId(id);
    situation.setCreationTime(t);
    configuration.setSituation(situation);

    _dao.addConfiguration(configuration);

    /**
     * If the group specifies a ROUTE_ID key, we automatically add the route to
     * the set of affected routes.
     */
    for (String key : group.getKeys()) {
      AlertProperty property = group.getProperty(key);
      if (property.getType() == EAlertPropertyType.ROUTE_ID) {
        String routeId = property.getValue();
        setAffectedVehicleJourneyForSituation(configuration.getId(), routeId,
            null, true);
      }
    }

    return configuration;
  }

  @Override
  public SituationConfiguration createSituation(
      SituationConfiguration configuration) {

    _dao.addConfiguration(configuration);

    return configuration;
  }

  @Override
  public void deleteSituationForId(String id) {
    SituationConfiguration config = _dao.getConfigurationForId(id);
    if (config != null) {
      _dao.removeConfiguration(config);
    }
  }

  /****
   * 
   ****/

  @Override
  public SituationConfiguration updateConfigurationDetails(String id,
      SituationBean situation) {

    SituationConfiguration config = _dao.getConfigurationForId(id);
    if (config == null)
      return null;

    SituationBean model = config.getSituation();
    model.setAdvice(situation.getAdvice());
    model.setDescription(situation.getDescription());
    model.setDetail(situation.getDetail());
    model.setInternal(situation.getInternal());
    model.setSummary(situation.getSummary());

    model.setEnvironmentReason(situation.getEnvironmentReason());
    model.setEquipmentReason(situation.getEquipmentReason());
    model.setMiscellaneousReason(situation.getMiscellaneousReason());
    model.setPersonnelReason(situation.getPersonnelReason());
    model.setUndefinedReason(situation.getUndefinedReason());

    model.setSensitivity(situation.getSensitivity());
    model.setSeverity(situation.getSeverity());

    handleUpdate(config);

    return config;
  }

  @Override
  public SituationConfiguration updateVisibility(String id, boolean visible) {

    SituationConfiguration config = _dao.getConfigurationForId(id);
    if (config == null)
      return null;

    if (config.isVisible() != visible) {
      config.setVisible(visible);
      handleUpdate(config, !visible);
    }

    return config;
  }

  @Override
  public SituationConfiguration setAffectedAgencyForSituation(String id,
      String agencyId, boolean active) {

    SituationConfiguration config = _dao.getConfigurationForId(id);
    if (config == null)
      return null;

    SituationBean situation = config.getSituation();
    SituationAffectsBean affects = getAffectsForSituation(situation);

    List<SituationAffectedAgencyBean> agencies = affects.getAgencies();

    if (active) {

      if (agencies == null) {
        agencies = new ArrayList<SituationAffectedAgencyBean>();
        affects.setAgencies(agencies);
      }

      SituationAffectedAgencyBean match = FunctionalLibrary.filterFirst(
          agencies, "agencyId", agencyId);

      if (match == null) {
        match = new SituationAffectedAgencyBean();
        match.setAgencyId(agencyId);
        agencies.add(match);
        handleUpdate(config);
      }
    } else {
      if (agencies != null) {

        List<SituationAffectedAgencyBean> matches = FunctionalLibrary.filter(
            agencies, "agencyId", agencyId);

        if (agencies.removeAll(matches))
          handleUpdate(config);
      }
    }

    return config;
  }

  @Override
  public SituationConfiguration setAffectedStopsForSituation(String id,
      List<String> stopIds, boolean active) {

    SituationConfiguration config = _dao.getConfigurationForId(id);
    if (config == null)
      return null;

    if (stopIds.isEmpty())
      return config;

    SituationBean situation = config.getSituation();
    SituationAffectsBean affects = getAffectsForSituation(situation);

    List<SituationAffectedStopBean> stops = affects.getStops();

    if (active) {

      if (stops == null) {
        stops = new ArrayList<SituationAffectedStopBean>();
        affects.setStops(stops);
      }

      boolean updated = false;

      for (String stopId : stopIds) {

        SituationAffectedStopBean match = FunctionalLibrary.filterFirst(stops,
            "stopId", stopId);

        if (match == null) {
          match = new SituationAffectedStopBean();
          match.setStopId(stopId);
          stops.add(match);
          updated = true;
        }
      }

      if (updated)
        handleUpdate(config);

    } else {
      if (stops != null) {

        boolean updated = false;

        for (String stopId : stopIds) {
          List<SituationAffectedStopBean> matches = FunctionalLibrary.filter(
              stops, "stopId", stopId);

          updated |= stops.removeAll(matches);
        }

        if (updated)
          handleUpdate(config);
      }
    }

    return config;
  }

  @Override
  public SituationConfiguration setAffectedVehicleJourneyForSituation(
      String id, String routeId, String directionId, boolean active) {

    SituationConfiguration config = _dao.getConfigurationForId(id);
    if (config == null)
      return null;

    SituationBean situation = config.getSituation();
    SituationAffectsBean affects = getAffectsForSituation(situation);

    List<SituationAffectedVehicleJourneyBean> vehicleJourneys = affects.getVehicleJourneys();

    if (active) {

      if (vehicleJourneys == null) {
        vehicleJourneys = new ArrayList<SituationAffectedVehicleJourneyBean>();
        affects.setVehicleJourneys(vehicleJourneys);
      }

      SituationAffectedVehicleJourneyBean match = filterFirst(vehicleJourneys,
          routeId, directionId);

      if (match == null) {
        match = new SituationAffectedVehicleJourneyBean();
        match.setLineId(routeId);
        match.setDirection(directionId);
        vehicleJourneys.add(match);
        handleUpdate(config);
      }
    } else {
      if (vehicleJourneys != null) {

        List<SituationAffectedVehicleJourneyBean> matches = filter(
            vehicleJourneys, routeId, directionId);

        if (vehicleJourneys.removeAll(matches))
          handleUpdate(config);
      }
    }

    return config;
  }

  @Override
  public SituationConfiguration setAffectedVehicleJourneyStopCallForSituation(
      String id, String routeId, String directionId, String stopId,
      boolean active) {

    SituationConfiguration config = _dao.getConfigurationForId(id);
    if (config == null)
      return null;

    SituationBean situation = config.getSituation();
    SituationAffectsBean affects = getAffectsForSituation(situation);

    List<SituationAffectedVehicleJourneyBean> vehicleJourneys = affects.getVehicleJourneys();

    if (vehicleJourneys == null)
      return config;

    SituationAffectedVehicleJourneyBean match = filterFirst(vehicleJourneys,
        routeId, directionId);

    if (match == null)
      return config;

    List<SituationAffectedCallBean> calls = match.getCalls();

    if (active) {
      if (calls == null) {
        calls = new ArrayList<SituationAffectedCallBean>();
        match.setCalls(calls);
      }

      SituationAffectedCallBean call = FunctionalLibrary.filterFirst(calls,
          "stopId", stopId);

      if (call == null) {
        call = new SituationAffectedCallBean();
        call.setStopId(stopId);
        calls.add(call);
        handleUpdate(config);
      }

    } else {
      if (calls != null) {
        List<SituationAffectedCallBean> matches = FunctionalLibrary.filter(
            calls, "stopId", stopId);
        if (calls.removeAll(matches))
          handleUpdate(config);
      }
    }

    return config;
  }

  @Override
  public SituationConfiguration setAffectedApplicationForSituation(String id,
      String apiKey, boolean active) {

    SituationConfiguration config = _dao.getConfigurationForId(id);
    if (config == null)
      return null;

    SituationBean situation = config.getSituation();
    SituationAffectsBean affects = getAffectsForSituation(situation);

    List<SituationAffectedApplicationBean> applications = affects.getApplications();

    if (active) {

      if (applications == null) {
        applications = new ArrayList<SituationAffectedApplicationBean>();
        affects.setApplications(applications);
      }

      SituationAffectedApplicationBean match = FunctionalLibrary.filterFirst(
          applications, "apiKey", apiKey);

      if (match == null) {
        match = new SituationAffectedApplicationBean();
        match.setApiKey(apiKey);
        applications.add(match);
        handleUpdate(config);
      }
    } else {
      if (applications != null) {

        List<SituationAffectedApplicationBean> matches = FunctionalLibrary.filter(
            applications, "apiKey", apiKey);

        if (applications.removeAll(matches))
          handleUpdate(config);
      }
    }

    return config;
  }

  @Override
  public SituationConfiguration addConsequenceForSituation(String id,
      SituationConsequenceBean consequence) {

    SituationConfiguration config = _dao.getConfigurationForId(id);
    if (config == null)
      return null;

    SituationBean situation = config.getSituation();

    List<SituationConsequenceBean> consequences = situation.getConsequences();

    if (consequences == null) {
      consequences = new ArrayList<SituationConsequenceBean>();
      situation.setConsequences(consequences);
    }

    consequences.add(consequence);
    handleUpdate(config);

    return config;
  }

  @Override
  public SituationConfiguration updateConsequenceForSituation(String id,
      int index, SituationConsequenceBean consequence) {

    SituationConfiguration config = _dao.getConfigurationForId(id);
    if (config == null)
      return null;

    SituationBean situation = config.getSituation();

    List<SituationConsequenceBean> consequences = situation.getConsequences();

    if (consequences != null && 0 <= index && index < consequences.size()) {
      consequences.set(index, consequence);
      handleUpdate(config);
    }

    return config;
  }

  @Override
  public SituationConfiguration removeConsequenceForSituation(String id,
      int index) {

    SituationConfiguration config = _dao.getConfigurationForId(id);
    if (config == null)
      return null;

    SituationBean situation = config.getSituation();

    List<SituationConsequenceBean> consequences = situation.getConsequences();

    if (consequences != null && 0 <= index && index < consequences.size()) {
      consequences.remove(index);
      handleUpdate(config);
    }

    return config;
  }

  /****
   * Private Methods
   ****/

  private SituationAffectsBean getAffectsForSituation(SituationBean situation) {

    SituationAffectsBean affects = situation.getAffects();
    if (affects == null) {
      affects = new SituationAffectsBean();
      situation.setAffects(affects);
    }
    return affects;
  }

  private SituationAffectedVehicleJourneyBean filterFirst(
      List<SituationAffectedVehicleJourneyBean> vehicleJourneys,
      String routeId, String directionId) {

    List<SituationAffectedVehicleJourneyBean> matches = filter(vehicleJourneys,
        routeId, directionId);
    if (matches.isEmpty())
      return null;
    return matches.get(0);
  }

  private List<SituationAffectedVehicleJourneyBean> filter(
      List<SituationAffectedVehicleJourneyBean> vehicleJourneys,
      String routeId, String directionId) {

    List<SituationAffectedVehicleJourneyBean> matches = new ArrayList<SituationAffectedVehicleJourneyBean>();

    for (SituationAffectedVehicleJourneyBean bean : vehicleJourneys) {
      if (!ObjectUtils.equals(bean.getLineId(), routeId))
        continue;
      if (!ObjectUtils.equals(bean.getDirection(), directionId))
        continue;
      matches.add(bean);
    }

    return matches;
  }

  private void handleUpdate(SituationConfiguration config) {
    handleUpdate(config, config.isVisible());
  }

  private void handleUpdate(SituationConfiguration config,
      boolean previousVisibility) {

    config.setLastUpdate(System.currentTimeMillis());

    /**
     * If we're visible or we were visible and now we're not, publish
     */
    if (config.isVisible() || previousVisibility) {
      PtSituationElementStructure ptSituation = constructSiriSituation(config);
      publishPtSituation(ptSituation);
    }
  }

  private PtSituationElementStructure constructSiriSituation(
      SituationConfiguration config) {

    SituationBean situation = config.getSituation();

    PtSituationElementStructure ptSituation = new PtSituationElementStructure();

    ptSituation.setCreationTime(new Date(situation.getCreationTime()));

    WorkflowStatusEnumeration progress = config.isVisible()
        ? WorkflowStatusEnumeration.OPEN : WorkflowStatusEnumeration.CLOSED;
    ptSituation.setProgress(progress);

    EntryQualifierStructure situationId = new EntryQualifierStructure();
    situationId.setValue(situation.getId());
    ptSituation.setSituationNumber(situationId);

    long lastUpdate = config.getLastUpdate();
    SituationVersion version = new SituationVersion();
    version.setValue(BigInteger.valueOf(lastUpdate));
    ptSituation.setVersion(version);
    ptSituation.setVersionedAtTime(new Date(lastUpdate));

    ptSituation.setAdvice(text(situation.getAdvice()));
    ptSituation.setDescription(text(situation.getDescription()));
    ptSituation.setDetail(text(situation.getDetail()));
    ptSituation.setInternal(text(situation.getInternal()));
    ptSituation.setSummary(text(situation.getSummary()));

    String envReason = situation.getEnvironmentReason();
    if (envReason != null)
      ptSituation.setEnvironmentReason(EnvironmentReasonEnumeration.fromValue(envReason));

    String equipReason = situation.getEquipmentReason();
    if (equipReason != null)
      ptSituation.setEquipmentReason(EquipmentReasonEnumeration.fromValue(equipReason));

    String miscReason = situation.getMiscellaneousReason();
    if (miscReason != null)
      ptSituation.setMiscellaneousReason(MiscellaneousReasonEnumeration.fromValue(miscReason));

    String personReason = situation.getPersonnelReason();
    if (personReason != null)
      ptSituation.setPersonnelReason(PersonnelReasonEnumeration.fromValue(personReason));

    ptSituation.setUndefinedReason(situation.getUndefinedReason());

    ESeverity severity = situation.getSeverity();
    if (severity != null) {
      String[] tpegCodes = severity.getTpegCodes();
      SeverityEnumeration severityEnum = SeverityEnumeration.fromValue(tpegCodes[0]);
      ptSituation.setSeverity(severityEnum);
    }

    constructionSiriSituationAffects(situation, ptSituation);
    constructSiriSituationConsequences(situation, ptSituation);

    return ptSituation;
  }

  private void constructionSiriSituationAffects(SituationBean situation,
      PtSituationElementStructure ptSituation) {
    SituationAffectsBean affects = situation.getAffects();

    if (affects != null) {
      AffectsScopeStructure sAffects = new AffectsScopeStructure();
      ptSituation.setAffects(sAffects);

      /**
       * Affected Agencies?
       */
      if (!CollectionsLibrary.isEmpty(affects.getAgencies())) {
        Operators operators = new Operators();
        sAffects.setOperators(operators);

        for (SituationAffectedAgencyBean affectedAgency : affects.getAgencies()) {
          AffectedOperatorStructure sAffectedOperator = new AffectedOperatorStructure();
          OperatorRefStructure operatorRef = new OperatorRefStructure();
          operatorRef.setValue(affectedAgency.getAgencyId());
          sAffectedOperator.setOperatorRef(operatorRef);
          operators.getAffectedOperator().add(sAffectedOperator);
        }
      }

      /**
       * Affected Stops?
       */
      if (!CollectionsLibrary.isEmpty(affects.getStops())) {

        StopPoints stopPoints = new StopPoints();
        sAffects.setStopPoints(stopPoints);

        for (SituationAffectedStopBean affectedStop : affects.getStops()) {
          AffectedStopPointStructure sAffectedStopPoint = new AffectedStopPointStructure();
          StopPointRefStructure stopPointRef = new StopPointRefStructure();
          stopPointRef.setValue(affectedStop.getStopId());
          sAffectedStopPoint.setStopPointRef(stopPointRef);
          stopPoints.getAffectedStopPoint().add(sAffectedStopPoint);
        }
      }

      /****
       * Affected Vehicle Journeys?
       */
      if (!CollectionsLibrary.isEmpty(affects.getVehicleJourneys())) {

        VehicleJourneys journeys = new VehicleJourneys();
        sAffects.setVehicleJourneys(journeys);

        for (SituationAffectedVehicleJourneyBean affectedVehicleJourney : affects.getVehicleJourneys()) {

          AffectedVehicleJourneyStructure sAffectedVehicleJourney = new AffectedVehicleJourneyStructure();

          LineRefStructure lineId = new LineRefStructure();
          lineId.setValue(affectedVehicleJourney.getLineId());
          sAffectedVehicleJourney.setLineRef(lineId);

          if (affectedVehicleJourney.getDirection() != null) {
            DirectionRefStructure directionId = new DirectionRefStructure();
            directionId.setValue(affectedVehicleJourney.getDirection());
            sAffectedVehicleJourney.setDirectionRef(directionId);
          }

          List<SituationAffectedCallBean> calls = affectedVehicleJourney.getCalls();

          if (!CollectionsLibrary.isEmpty(calls)) {
            Calls sCalls = new Calls();
            sAffectedVehicleJourney.setCalls(sCalls);

            for (SituationAffectedCallBean call : calls) {
              AffectedCallStructure sCall = new AffectedCallStructure();
              StopPointRefStructure stopPointRef = new StopPointRefStructure();
              stopPointRef.setValue(call.getStopId());
              sCall.setStopPointRef(stopPointRef);
              sCalls.getCall().add(sCall);
            }
          }

          journeys.getAffectedVehicleJourney().add(sAffectedVehicleJourney);
        }
      }

      /**
       * Affected Applications
       */
      if (!CollectionsLibrary.isEmpty(affects.getApplications())) {

        OneBusAwayAffects obaAffects = new OneBusAwayAffects();
        ExtensionsStructure extension = new ExtensionsStructure();
        extension.setAny(obaAffects);
        sAffects.setExtensions(extension);

        OneBusAwayAffectsStructure.Applications applications = new OneBusAwayAffectsStructure.Applications();
        obaAffects.setApplications(applications);

        for (SituationAffectedApplicationBean affectedApplication : affects.getApplications()) {
          AffectedApplicationStructure sAffectedApplication = new AffectedApplicationStructure();
          sAffectedApplication.setApiKey(affectedApplication.getApiKey());
          applications.getAffectedApplication().add(sAffectedApplication);
        }
      }
    }
  }

  private void constructSiriSituationConsequences(SituationBean situation,
      PtSituationElementStructure ptSituation) {
    List<SituationConsequenceBean> consequences = situation.getConsequences();

    if (!CollectionsLibrary.isEmpty(consequences)) {

      PtConsequencesStructure ptConsequences = new PtConsequencesStructure();
      ptSituation.setConsequences(ptConsequences);

      for (SituationConsequenceBean consequence : consequences) {
        PtConsequenceStructure ptConsequence = new PtConsequenceStructure();
        ptConsequences.getConsequence().add(ptConsequence);

        if (consequence.getCondition() != null)
          ptConsequence.setCondition(ServiceConditionEnumeration.fromValue(consequence.getCondition()));

        SituationConditionDetailsBean details = consequence.getConditionDetails();

        if (details != null && details.getDiversionPath() != null) {

          OneBusAwayConsequence obaConsequence = new OneBusAwayConsequence();

          EncodedPolylineBean path = details.getDiversionPath();
          obaConsequence.setDiversionPath(path.getPoints());

          ExtensionsStructure extension = new ExtensionsStructure();
          extension.setAny(obaConsequence);
          ptConsequence.setExtensions(extension);
        }
      }
    }
  }

  private void publishPtSituation(PtSituationElementStructure ptSituation) {
    Situations situations = new Situations();
    situations.getPtSituationElement().add(ptSituation);

    SituationExchangeDeliveryStructure situationExchangeDelivery = new SituationExchangeDeliveryStructure();
    situationExchangeDelivery.setSituations(situations);

    ServiceDelivery serviceDelivery = new ServiceDelivery();
    List<SituationExchangeDeliveryStructure> situationExchangeDeliveries = serviceDelivery.getSituationExchangeDelivery();
    situationExchangeDeliveries.add(situationExchangeDelivery);

    SiriServer server = _sirivService.getServer();
    server.publish(serviceDelivery);
  }

  private DefaultedTextStructure text(NaturalLanguageStringBean nls) {

    if (nls == null)
      return null;

    DefaultedTextStructure text = new DefaultedTextStructure();
    text.setLang(nls.getLang());
    text.setValue(nls.getValue());
    return text;
  }
}
