var OBA = window.OBA || {};

var oba_service_alerts_situation = function(data) {
	
	var affectedAgenciesById = {};
	var affectedStopsById = {};
	
	var refreshConfiguration = function(config) {
		
		refreshVisibilityCheckBox(config);
		
		var situation = config.situation;
		
		var affected = situation.affects || {};
		
		/****
		 * Agencies
		 ****/
		
		var affectedAgencies = affected.agencies || [];
		
		var affectedAgenciesElement = jQuery('#affectedAgencies');
		affectedAgenciesElement.empty();
		
		affectedAgenciesById = {};
		
		jQuery.each(affectedAgencies, function() {
			refreshAffectedAgency(affectedAgenciesElement, this);
		});
		
		/****
		 * Stops
		 ****/
		
		var affectedStops = affected.stops || [];
		
		var affectedStopsElement = jQuery('#affectedStops');
		affectedStopsElement.empty();
		
		affectedStopsById = {};
		
		jQuery.each(affectedStops, function() {
			refreshAffectedStop(affectedStopsElement, this);
		});
	};
	
	var processConfiguration = function(config, references) {
		OBA.Api.processSituation(config.situation, references);
	};
	
	var configRawHandler = OBA.Api.createEntryHandler(refreshConfiguration, null, processConfiguration);
	
	/****
	 * Visibility
	 ****/
	
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
	
	/****
	 * Affected Agencies 
	 ****/
	
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
			anchor.attr('href','javascript:void(0)');
			anchor.text(agency.name);
			anchor.appendTo(listItem);
			
			anchor.click(function() {
				updateAffectedAgency(agency,true);
				content.dialog('close');
			});
		});
		
		var dialogOptions = {
			title: 'Select an Agency',
			modal: true,
			width: '50%'
		};
		
		content.dialog(dialogOptions);
		
		return false;
	};

	var addAffectedAgency = function() {
		
		OBA.Api.agenciesWithCoverage(showAgenciesDialog);
		
		return false;
		
	};
	
	jQuery('#addAffectedAgency').click(addAffectedAgency);
	
	
	/****
	 * Affected Stops
	 ****/
	
	var refreshAffectedStop = function(affectedStopsElement, entry) {
		
		var content = jQuery('.affectedStopTemplate').clone();
		content.removeClass('affectedStopTemplate');
		content.addClass('affectedStop');
		
		OBA.Presentation.applyStopNameToElement(entry.stop,content);
		
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
			affectedStopClickHandler(stop,stopSelectionWidget.map);
		};
		
		var stopSelectionWidget = OBA.StopSelectionWidget(content, params);
		
		var dialogOptions = {
			title: 'Select Stops',
			modal: true,
			width: '90%',
			height: 700
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
			if( stop.id in affectedStopsById) {
				addContent.hide();
				removeContent.show();
			}
			else {
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
		pos.set('position',new google.maps.LatLng(stop.lat,stop.lon));
	    
	    infoWindow.setContent(content.show().get(0));
	    infoWindow.open(map,pos);
	};
	
	jQuery('#addAffectedStop').click(addAffectedStop);

	/**
	 * Finally, display the situation
	 */	
	configRawHandler(data.config);
};