var OBA = window.OBA || {};

var oba_service_alerts_situation = function(data) {

	var affectedAgenciesById = {};
	var affectedStopsById = {};
	var affectedVehicleJourneysById = {};

	var refreshConfiguration = function(config) {

		refreshVisibilityCheckBox(config);

		var situation = config.situation;

		var affected = situation.affects || {};

		/***********************************************************************
		 * Agencies
		 **********************************************************************/

		var affectedAgencies = affected.agencies || [];

		var affectedAgenciesElement = jQuery('#affectedAgencies');
		affectedAgenciesElement.empty();

		affectedAgenciesById = {};

		jQuery.each(affectedAgencies, function() {
			refreshAffectedAgency(affectedAgenciesElement, this);
		});

		/***********************************************************************
		 * Stops
		 **********************************************************************/

		var affectedStops = affected.stops || [];

		var affectedStopsElement = jQuery('#affectedStops');
		affectedStopsElement.empty();

		affectedStopsById = {};

		jQuery.each(affectedStops, function() {
			refreshAffectedStop(affectedStopsElement, this);
		});

		/***********************************************************************
		 * Vehicle Journeys
		 **********************************************************************/

		var affectedVehicleJourneys = affected.vehicleJourneys || [];

		var affectedVehicleJourneysElement = jQuery('#affectedVehicleJourneys');
		affectedVehicleJourneysElement.empty();

		affectedVehicleJourneysById = {};

		jQuery.each(affectedVehicleJourneys,
				function() {
					refreshAffectedVehicleJourney(
							affectedVehicleJourneysElement, this);
				});
	};

	var processConfiguration = function(config, references) {
		OBA.Api.processSituation(config.situation, references);
	};

	var configRawHandler = OBA.Api.createEntryHandler(refreshConfiguration,
			null, processConfiguration);

	/***************************************************************************
	 * Visibility
	 **************************************************************************/

	var visibilityCheckBox = jQuery('#visibilityCheckBox');

	var refreshVisibilityCheckBox = function(config) {
		visibilityCheckBox.attr('checked', config.visible);
	};

	visibilityCheckBox.click(function() {
		var url = 'situation!updateVisibility.action';
		var params = {};
		params.id = data.id;
		params.visible = visibilityCheckBox.is(':checked');
		jQuery.getJSON(url, params, configRawHandler);
	});

	/***************************************************************************
	 * Affected Agencies
	 **************************************************************************/

	var refreshAffectedAgency = function(affectedAgenciesElement, entry) {

		var agency = entry.agency;

		var content = jQuery('.affectedAgencyTemplate').clone();
		content.removeClass('affectedAgencyTemplate');
		content.addClass('affectedAgency');

		content.find('.name').text(agency.name);

		var removeElement = content.find('a');
		removeElement.click(function() {
			updateAffectedAgency(agency, false);
		});

		content.appendTo(affectedAgenciesElement);
		content.show();

		affectedAgenciesById[agency.id] = true;
	};

	var updateAffectedAgency = function(agency, enabled) {
		var url = 'situation!updateAffectedAgency.action';
		var params = {};
		params.id = data.id;
		params.agencyId = agency.id;
		params.enabled = enabled;
		jQuery.getJSON(url, params, configRawHandler);
	};

	var showAgenciesDialog = function(agenciesWithCoverage) {

		var content = jQuery('<div/>');
		content.addClass('agencySelectionDialog');

		var listElement = jQuery('<ul/>');
		listElement.appendTo(content);

		jQuery.each(agenciesWithCoverage.list || [], function() {

			var agency = this.agency;

			var listItem = jQuery('<li/>');
			listItem.appendTo(listElement);

			var anchor = jQuery('<a/>');
			anchor.attr('href', 'javascript:void(0)');
			anchor.text(agency.name);
			anchor.appendTo(listItem);

			anchor.click(function() {
				updateAffectedAgency(agency, true);
				content.dialog('close');
			});
		});

		var dialogOptions = {
			title : 'Select an Agency',
			modal : true,
			width : '50%'
		};

		content.dialog(dialogOptions);

		return false;
	};

	var addAffectedAgency = function() {

		OBA.Api.agenciesWithCoverage(showAgenciesDialog);

		return false;

	};

	jQuery('#addAffectedAgency').click(addAffectedAgency);

	/***************************************************************************
	 * Affected Stops
	 **************************************************************************/

	var refreshAffectedStop = function(affectedStopsElement, entry) {

		var content = jQuery('.affectedStopTemplate').clone();
		content.removeClass('affectedStopTemplate');
		content.addClass('affectedStop');

		OBA.Presentation.applyStopNameToElement(entry.stop, content);

		var removeElement = content.find('a');
		removeElement.click(function() {
			updateAffectedStop(entry.stop, false);
		});

		content.appendTo(affectedStopsElement);
		content.show();

		affectedStopsById[entry.stopId] = true;
	};

	var updateAffectedStop = function(stop, isAdd) {
		var url = 'situation!updateAffectedStop.action';
		var params = {};
		params.id = data.id;
		params.stopId = stop.id;
		params.enabled = isAdd;
		jQuery.getJSON(url, params, configRawHandler);
	};

	var addAffectedStop = function() {

		var content = jQuery('<div/>');
		content.addClass('stopSelectionDialog');

		var params = {};
		params.stopClickHandler = function(stop) {
			affectedStopClickHandler(stop, stopSelectionWidget.map);
		};

		var stopSelectionWidget = OBA.StopSelectionWidget(content, params);

		var dialogOptions = {
			title : 'Select Stops',
			modal : true,
			width : '90%',
			height : 700
		};

		content.dialog(dialogOptions);

		return false;
	};

	var infoWindow = new google.maps.InfoWindow();

	var affectedStopClickHandler = function(stop, map) {

		var content = OBA.Presentation.createStopInfoWindow(stop);

		var addContent = content.find('.stopContent .addStop');
		var removeContent = content.find('.stopContent .removeStop');

		var addAnchor = addContent.find('a');
		var removeAnchor = removeContent.find('a');

		var contentRefresh = function() {
			if (stop.id in affectedStopsById) {
				addContent.hide();
				removeContent.show();
			} else {
				addContent.show();
				removeContent.hide();
			}
			infoWindow.setContent(content.show().get(0));
		};

		addAnchor.click(function() {
			affectedStopsById[stop.id] = true;
			contentRefresh();
			updateAffectedStop(stop, true);
		});

		removeAnchor.click(function() {
			delete affectedStopsById[stop.id];
			contentRefresh();
			updateAffectedStop(stop, false);
		});

		contentRefresh();

		var pos = new google.maps.MVCObject();
		pos.set('position', new google.maps.LatLng(stop.lat, stop.lon));

		infoWindow.setContent(content.show().get(0));
		infoWindow.open(map, pos);
	};

	jQuery('#addAffectedStop').click(addAffectedStop);

	/***************************************************************************
	 * Affected Vehicle Journeys
	 **************************************************************************/
	
	var refreshAffectedVehicleJourney = function(affectedVehicleJourneysElement, entry) {

		var route = entry.route;

		var content = jQuery('.affectedVehicleJourneyTemplate').clone();
		content.removeClass('affectedVehicleJourneyTemplate');
		content.addClass('affectedVehicleJourney');

		content.find('.routeName').text(entry.lineId);
		
		var desc = content.find('.routeDescription');
		if( entry.direction )
			desc.text(' - ' + entry.direction);
		else
			desc.hide();

		var removeElement = content.find('a');
		removeElement.click(function() {
			updateAffectedVehicleJourney(entry.lineId, entry.direction, false);
		});

		content.appendTo(affectedVehicleJourneysElement);
		content.show();
	};

	var updateAffectedVehicleJourney = function(routeId, directionId, enabled) {
		var url = 'situation!updateAffectedVehicleJourney.action';
		var params = {};
		params.id = data.id;
		params.routeId = routeId;
		if( directionId )
			params.directionId = directionId;
		params.enabled = enabled;
		jQuery.getJSON(url, params, configRawHandler);
	};
	
	var showRoute = function(parentContent, route, stopsForRoute) {
		
		parentContent.empty();
		
		var content = jQuery('.routeEntryTemplate').clone();
		content.removeClass('routeEntryTemplate');
		content.addClass('routeEntry');
		
		content.find('h2').text(OBA.Presentation.getNameForRoute(route));
		content.find('p>a').click(function() {
			updateAffectedVehicleJourney(route.id,null,true);
			parentContent.dialog('close');
		});
		
		var list = content.find('ul');
		
		var stopGroupings = stopsForRoute.stopGroupings || [];
		jQuery.each(stopGroupings, function() {
			if( this.type != 'direction')
				return;
			jQuery.each(this.stopGroups,function() {
				var stopGroup = this;
				var name = OBA.Presentation.nameAsString(stopGroup.name);
				var el = content.find('.affectedDirectionTemplate').clone();
				el.removeClass('affectedDirectionTemplate');
				el.addClass('affectedDirection');
				el.find('.name').text(name);
				el.find('a').click(function() {
					updateAffectedVehicleJourney(route.id,stopGroup.id,true);
					parentContent.dialog('close');
				});
				el.show();
				el.appendTo(list);
			});
		});
		
		content.show();
		content.appendTo(parentContent);
	};
	
	var showRoutes = function(parentContent,routesList) {
		
		var routes = routesList.list;
		
		var routeSearchResults = parentContent.find('.routeSearchResults');
		routeSearchResults.empty();
		
		var list = jQuery('<ul/>');
		list.appendTo(routeSearchResults);
		
		if( routes.length == 0 ) {
			var item = jQuery('<li/>');
			item.text('No routes found');
			item.appendTo(list);
		}
		else {
			jQuery.each(routes, function() {
				var route = this;
				var content = jQuery('.routeItemTemplate').clone();
				content.removeClass('routeItemTemplate');
				content.addClass('routeItem');
				var anchor = content.find('a');
				anchor.text(OBA.Presentation.getNameForRoute(route));
				anchor.click(function() {
					OBA.Api.stopsForRoute(route.id, function(stopsForRoute) {
						showRoute(parentContent, route, stopsForRoute);
					});
				});
				content.appendTo(list);
			});
		}
	};

	var showRoutesDialog = function() {

		var content = jQuery('.routeDialogTemplate').clone();
		content.removeClass('routeDialogTemplate');
		content.addClass('routeDialog');
		
		var routeNameInput = content.find('.routeNameInput');
		var routeNameSearchButton = content.find('.routeNameSearchButton');
		
		
		routeNameSearchButton.click(function(){
			var text = routeNameInput.val();
			if( text.length == 0)
				return;
			var params = {};
			params.query = text;
			params.lat = OBA.Config.centerLat || 47.606828;
			params.lon = OBA.Config.centerLon || -122.332505;
			params.radius = 20000;
			
			OBA.Api.routesForLocation(params,function(routes) {
				showRoutes(content,routes);
				
				var list = jQuery('<ul/>');
			});
		});
		
		var dialogOptions = {
			title : 'Select a Route',
			modal : true,
			width : '50%'
		};

		content.dialog(dialogOptions);

		return false;
	};

	var addAffectedVehicleJourney = function() {

		showRoutesDialog();

		return false;

	};

	jQuery('#addAffectedVehicleJourney').click(addAffectedVehicleJourney);

	/**
	 * Finally, display the situation
	 */
	configRawHandler(data.config);
};