var OBA = window.OBA || {};

var obaKingCountyMetroAlertsFactory = function() {
	var that = {};
	
	var getPolylinesForAlertConfigAndStopsForRoute = function(config, stopsForRoute) {
		if( ! config.directionId )
			return stopsForRoute.polylines;
		if( ! stopsForRoute.stopGroupings )
			return stopsForRoute.polylines;
		
		for( var i=0; i < stopsForRoute.stopGroupings.length; i++) {
			var stopGrouping = stopsForRoute.stopGroupings[i];
			if( stopGrouping.type != 'direction')
				continue;
			for( var j=0; j < stopGrouping.stopGroups.length; j++) {
				var stopGroup = stopGrouping.stopGroups[j];
				if( stopGroup.id == config.directionId )
					return stopGroup.polylines;
			}
		}
		
		return stopsForRoute.polylines;
	};
	
	var alertConfigurationAndStopsForRouteHandler = function(map, mapControlsId, config) {
		
		return function(json) {
			
			/**
			 * First we draw the route polylines
			 */
			var polylines = getPolylinesForAlertConfigAndStopsForRoute(config,json);
			
			for(var i=0; i<polylines.length; i++) {
				var path = OBA.Maps.decodePolyline(polylines[i].points);
		        var opts = {path: path, map: map, strokeColor: '#000000'};
		        new google.maps.Polyline(opts);
			}
			
			/**
			 * Next we draw the reroute, so it will be on top
			 */
			var path = OBA.Maps.decodePolyline(config.reroteAsPolylineString);
	        
	        var bounds = OBA.Maps.getPointsAsBounds(path);
	        if( bounds.isEmpty() )
	            return;
	        
	        map.fitBounds(bounds); 
	        
	        var opts = {path: path, strokeColor: '#ff0000'};
	        var line = new google.maps.Polyline(opts);
	        line.setMap(map);
	        
	        var startPoint = path[0];
	        var startUrl = OBA.Resources.Map['RouteStart.png'];
			new google.maps.Marker({position: startPoint, map: map, icon: startUrl});
			
	        var endPoint = path[path.length-1];
	        var endUrl = OBA.Resources.Map['RouteEnd.png'];
			new google.maps.Marker({position: endPoint, map: map, icon: endUrl});
		};
	};
	
	var alertConfigurationHandler = function(map, mapControlsId) {
		return function(json) {
			if( ! json.reroteAsPolylineString )
				return;
			OBA.Api.stopsForRoute(json.route.id,alertConfigurationAndStopsForRouteHandler(map,mapControlsId,json));
		};
	};
	
	that.setupSituationConfigurationMap = function(situationConfigId,mapElementId, mapControlsId) {
		var mapElement = jQuery(mapElementId).get(0);
		var map = OBA.Maps.map(mapElement);
		var url = OBA.Config.baseUrl + "/alert-configuration!json.action";
		var handler = alertConfigurationHandler(map, mapControlsId);
		jQuery.getJSON(url, {id:situationConfigId}, handler);
	};
	
	that.setupRouteMap = function(polylines,mapElementId) {
		var mapElement = jQuery(mapElementId).get(0);
		var map = OBA.Maps.map(mapElement);

		var bounds = new google.maps.LatLngBounds();
		
		for( var i=0; i<polylines.length; i++ ) {
			
			var path = OBA.Maps.decodePolyline(polylines[i]);
			for( var pi=0; pi<path.length;pi++)
				bounds.extend(path[pi]);
			
	        
	        var opts = {path: path, strokeColor: '#000000'};
	        var line = new google.maps.Polyline(opts);
	        line.setMap(map);
		}
		
        if( ! bounds.isEmpty() )
        	map.fitBounds(bounds); 
	};
	
	return that;
};

OBA.KingCountyMetroAlerts =  obaKingCountyMetroAlertsFactory();