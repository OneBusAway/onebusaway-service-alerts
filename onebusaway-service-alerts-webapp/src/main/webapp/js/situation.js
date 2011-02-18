var OBA = window.OBA || {};

var oba_service_alerts_situation = function(data) {
	
	var affectedStopsById = {};
	
	var refreshConfiguration = function(config) {
		
		refreshVisibilityCheckBox(config);
		
		var situation = config.situation;
		
		var affected = situation.affects || {};
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
		var method = isAdd ? 'addAffectedStop' : 'removeAffectedStop';
		var url = 'situation!' + method + '.action';
		var params = {};
		params.id = data.id;
		params.stopId = stop.id;
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
			height: 500
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