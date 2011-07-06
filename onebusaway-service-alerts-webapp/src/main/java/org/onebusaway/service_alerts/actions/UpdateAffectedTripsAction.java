package org.onebusaway.service_alerts.actions;

import java.io.IOException;
import java.util.List;

public class UpdateAffectedTripsAction extends SituationActionSupport {

  private static final long serialVersionUID = 1L;

  private List<String> _tripIds;

  private boolean _enabled;

  public void setTripIds(List<String> tripIds) {
    _tripIds = tripIds;
  }

  public void setEnabled(boolean enabled) {
    _enabled = enabled;
  }

  /****
   * Methods
   ****/

  @Override
  public String execute() throws IOException {

    _model = _situationService.setAffectedVehicleJourneyTripsForSituation(
        _model.getId(), _tripIds, _enabled);

    if (_model == null)
      return INPUT;
    fillResponse();
    return "json";
  }
}
