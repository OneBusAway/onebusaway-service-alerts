package org.onebusaway.service_alerts.builder;

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.geotools.feature.FeatureCollection;
import org.onebusaway.collections.DirectedGraph;
import org.onebusaway.collections.FactoryMap;
import org.onebusaway.collections.MappingLibrary;
import org.onebusaway.collections.Max;
import org.onebusaway.collections.Min;
import org.onebusaway.collections.tuple.Pair;
import org.onebusaway.collections.tuple.T2;
import org.onebusaway.collections.tuple.Tuples;
import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.geospatial.model.EncodedPolylineBean;
import org.onebusaway.geospatial.model.XYPoint;
import org.onebusaway.geospatial.services.GeometryLibrary;
import org.onebusaway.geospatial.services.PolylineEncoder;
import org.onebusaway.geospatial.services.UTMLibrary;
import org.onebusaway.geospatial.services.UTMProjection;
import org.onebusaway.service_alerts.model.AlertConfiguration;
import org.onebusaway.service_alerts.model.EAlertType;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data.model.StopGroupBean;
import org.onebusaway.transit_data.model.StopGroupingBean;
import org.onebusaway.transit_data.model.StopsForRouteBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.onebusaway.utility.ObjectSerializationLibrary;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.caucho.hessian.client.HessianProxyFactory;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.Point;

public class SnowRerouteBundleCreator {

  private static final String[] AGENCY_IDS = {"1", "40"};

  private Map<String, List<RouteDirectionData>> _dataByRouteShortName = new HashMap<String, List<RouteDirectionData>>();

  private List<T2<String, Geometry>> _regions = new ArrayList<T2<String, Geometry>>();

  private TransitDataService _transitDataService;

  private UTMProjection _projection;

  private File _rerouteShapefile;

  private File _regionsShapefile;

  private String _transitDataServiveUrl;

  private File _bundleOutputPath;

  public void setRerouteShapefile(File rerouteShapefile) {
    _rerouteShapefile = rerouteShapefile;
  }

  public void setRegionsShapefile(File regionsShapefile) {
    _regionsShapefile = regionsShapefile;
  }

  public void setTransitDataServiveUrl(String transitDataServiveUrl) {
    _transitDataServiveUrl = transitDataServiveUrl;
  }

  public void setBundleOutputPath(File bundleOutputPath) {
    _bundleOutputPath = bundleOutputPath;
  }

  public void run() throws Exception {

    wireTransitDataService();
    loadRegions();

    Map<String, AlertConfiguration> reroutesById = new HashMap<String, AlertConfiguration>();
    if (_bundleOutputPath.exists())
      reroutesById = ObjectSerializationLibrary.readObject(_bundleOutputPath);

    loadReroutes(reroutesById);

  }

  /****
   * Private Methods
   ****/

  private void wireTransitDataService() throws MalformedURLException {
    HessianProxyFactory factory = new HessianProxyFactory();
    _transitDataService = (TransitDataService) factory.create(
        TransitDataService.class, _transitDataServiveUrl);
  }

  private void loadRegions() throws Exception {

    FeatureCollection<SimpleFeatureType, SimpleFeature> features = ShapefileLibrary.loadShapeFile(_regionsShapefile);

    Iterator<SimpleFeature> it = features.iterator();

    while (it.hasNext()) {
      SimpleFeature feature = it.next();

      Property nameProperty = feature.getProperty("ADV_DIST");
      String name = (String) nameProperty.getValue();

      System.out.println("region=" + name);

      T2<String, Geometry> tuple = Tuples.tuple(name,
          (Geometry) feature.getDefaultGeometry());

      _regions.add(tuple);
    }

    features.close(it);
  }

  private void loadReroutes(Map<String, AlertConfiguration> reroutesById)
      throws Exception {

    FeatureCollection<SimpleFeatureType, SimpleFeature> features = ShapefileLibrary.loadShapeFile(_rerouteShapefile);

    Iterator<SimpleFeature> it = features.iterator();

    int expected = 0;

    while (it.hasNext()) {
      SimpleFeature feature = it.next();
      expected++;

      System.out.println("expected=" + expected + " actual="
          + reroutesById.size());

      Property objIdProperty = feature.getProperty("OBJECTID");
      Number objId = (Number) objIdProperty.getValue();
      String id = "1_awr-" + objId;

      if (reroutesById.containsKey(id))
        continue;

      Property routeNumberProperty = feature.getProperty("RTE_NUM");
      Number routeNumber = (Number) routeNumberProperty.getValue();
      String routeShortName = routeNumber.toString();

      Property fromNameProperty = feature.getProperty("FROM_NAME");
      String fromName = (String) fromNameProperty.getValue();

      Property toNameProperty = feature.getProperty("TO_NAME");
      String toName = (String) toNameProperty.getValue();

      System.out.println("==========================================");
      System.out.println(routeNumber + " " + fromName + " " + toName);

      Geometry geometry = (Geometry) feature.getDefaultGeometry();
      LineString lineString = getGeometryAsLineString(geometry);

      if (lineString == null)
        continue;

      List<RouteDirectionData> directions = getDirectionsForRoute(routeShortName);

      AlertConfiguration reroute = getBestDirectionForReroute(routeShortName,
          fromName, toName, lineString, directions);

      if (reroute != null) {
        reroute.setId(id);
        reroutesById.put(id, reroute);
        ObjectSerializationLibrary.writeObject(_bundleOutputPath, reroutesById);
      }

    }

    features.close(it);

    System.out.println("expected=" + expected);
    System.out.println("actual=" + reroutesById.size());
  }

  private LineString getGeometryAsLineString(Geometry geometry) {

    if (geometry instanceof LineString)
      return (LineString) geometry;

    if (geometry instanceof MultiLineString) {
      MultiLineString mls = (MultiLineString) geometry;
      if (mls.getNumGeometries() != 1) {
        for (int i = 0; i < mls.getNumGeometries(); i++) {
          dumpLineString((LineString) mls.getGeometryN(i));
        }
        return null;
        /*
         * throw new IllegalStateException("multiple lines: " +
         * mls.getNumGeometries());
         */
      }
      return (LineString) mls.getGeometryN(0);
    }

    throw new IllegalStateException("unknown geometry: " + geometry);
  }

  private List<RouteDirectionData> getDirectionsForRoute(String routeShortName) {

    List<RouteDirectionData> data = _dataByRouteShortName.get(routeShortName);

    if (data == null) {

      StopsForRouteBean stopsForRoute = null;
      String routeId = null;
      for (String agencyId : AGENCY_IDS) {
        routeId = agencyId + "_" + routeShortName;
        stopsForRoute = _transitDataService.getStopsForRoute(routeId);
        if (stopsForRoute != null)
          break;
      }

      if (stopsForRoute == null) {

        System.err.println("no route info found for route=" + routeShortName);
        data = Collections.emptyList();

      } else {

        Map<String, StopBean> stopsById = MappingLibrary.mapToValue(
            stopsForRoute.getStops(), "id");

        StopGroupingBean grouping = getDirections(stopsForRoute);
        data = getGroupingAsData(routeId, stopsById, grouping);
      }

      _dataByRouteShortName.put(routeShortName, data);
    }

    return data;
  }

  private StopGroupingBean getDirections(StopsForRouteBean stopsForRoute) {
    List<StopGroupingBean> stopGroupings = stopsForRoute.getStopGroupings();
    for (StopGroupingBean stopGrouping : stopGroupings) {
      if (stopGrouping.getType().equals("direction"))
        return stopGrouping;
    }
    throw new IllegalStateException("direction grouping not found");
  }

  private List<RouteDirectionData> getGroupingAsData(String routeId,
      Map<String, StopBean> stopsById, StopGroupingBean grouping) {

    List<RouteDirectionData> datas = new ArrayList<RouteDirectionData>();

    for (StopGroupBean group : grouping.getStopGroups()) {

      RouteDirectionData data = new RouteDirectionData();
      data.setRouteId(routeId);
      data.setDirectionId(group.getId());
      data.setDirectionName(group.getName().getName());
      datas.add(data);

      List<StopBean> stops = new ArrayList<StopBean>();

      for (String stopId : group.getStopIds()) {
        StopBean stop = stopsById.get(stopId);
        if (stop == null)
          throw new IllegalStateException("unkown stop: " + stopId);
        stops.add(stop);
      }

      data.setStops(stops);

      List<List<CoordinatePoint>> allPoints = new ArrayList<List<CoordinatePoint>>();
      DirectedGraph<CoordinatePoint> graph = new DirectedGraph<CoordinatePoint>();
      List<String> polylineStrings = new ArrayList<String>();

      for (EncodedPolylineBean bean : group.getPolylines()) {
        List<CoordinatePoint> points = PolylineEncoder.decode(bean);
        allPoints.add(points);
        CoordinatePoint prev = null;
        for (CoordinatePoint point : points) {
          if (prev != null)
            graph.addEdge(prev, point);
          prev = point;
        }
        polylineStrings.add(bean.getPoints());
      }

      data.setAllPoints(allPoints);
      data.setGraph(graph);
      data.setPolylineStrings(polylineStrings);

      Map<Pair<CoordinatePoint>, Set<StopBean>> stopsByEdge = new FactoryMap<Pair<CoordinatePoint>, Set<StopBean>>(
          new HashSet<StopBean>());

      for (StopBean stop : stops) {
        CoordinatePoint p = new CoordinatePoint(stop.getLat(), stop.getLon());
        Min<Pair<CoordinatePoint>> m = new Min<Pair<CoordinatePoint>>();
        for (Pair<CoordinatePoint> edge : graph.getEdges()) {
          double d = getDistanceToEdge(edge, p);
          m.add(d, edge);
        }
        Pair<CoordinatePoint> bestEdge = m.getMinElement();
        stopsByEdge.get(bestEdge).add(stop);
      }

      data.setStopsByEdge(stopsByEdge);

    }
    return datas;
  }

  private AlertConfiguration getBestDirectionForReroute(String routeShortName,
      String fromName, String toName, LineString ls,
      List<RouteDirectionData> directions) {
    
    String region = getRegionForReroute(ls);

    //dumpLineString(ls);

    CoordinatePoint firstPoint = point(ls.getPointN(0));
    CoordinatePoint lastPoint = point(ls.getPointN(ls.getNumPoints() - 1));

    List<RouteSegment> segments = new ArrayList<RouteSegment>();

    for (RouteDirectionData data : directions) {
      System.out.println("dir=" + data.getDirectionId());
      DirectedGraph<CoordinatePoint> graph = data.getGraph();
      Pair<CoordinatePoint> fromEdge = getClosestEdge(graph, firstPoint);
      Pair<CoordinatePoint> toEdge = getClosestEdge(graph, lastPoint);

      System.out.println(fromEdge.getFirst() + " " + fromEdge.getSecond());
      System.out.println(toEdge.getFirst() + " " + toEdge.getSecond());

      List<Pair<CoordinatePoint>> path = new ArrayList<Pair<CoordinatePoint>>();
      Set<CoordinatePoint> visited = new HashSet<CoordinatePoint>();

      if (connected(graph, fromEdge.getFirst(), toEdge.getSecond(), visited,
          path)) {
        RouteSegment segment = new RouteSegment();
        segment.setData(data);
        segment.setFrom(fromEdge);
        segment.setTo(toEdge);
        segment.setPath(path);
        segments.add(segment);
      }
    }

    if (segments.size() != 1) {
      
      System.err.println("problem");
      
      Set<String> routeIds = new HashSet<String>();
      
      for (RouteDirectionData data : directions) {
        System.out.println("=== direction=" + data.getDirectionId() + " "
            + data.getDirectionName() + " ====");
        for (String polyline : data.getPolylineStrings())
          System.out.println(polyline);
        
        routeIds.add(data.getRouteId());
      }
      
      if( routeIds.size() != 1)
        return null;
      
      String routeId = routeIds.iterator().next();
      
      AlertConfiguration bean = new AlertConfiguration();
      bean.setType(EAlertType.REROUTE);
      bean.setRouteId(routeId);
      bean.setRegion(region);
      bean.setReroute(getLineStringAsPoints(ls));
      EncodedPolylineBean polyline = PolylineEncoder.createEncodings(bean.getReroute());
      bean.setReroteAsPolylineString(polyline.getPoints());      
      return bean;
    }

    RouteSegment segment = segments.get(0);
    RouteDirectionData data = segment.getData();
    Map<Pair<CoordinatePoint>, Set<StopBean>> stopsByEdge = data.getStopsByEdge();
    Set<StopBean> stopsAlongReroute = new HashSet<StopBean>();
    for (Pair<CoordinatePoint> pair : segment.getPath()) {
      stopsAlongReroute.addAll(stopsByEdge.get(pair));
    }

    List<String> stopIds = new ArrayList<String>();
    for (StopBean stop : stopsAlongReroute) {
      stopIds.add(stop.getId());
      System.out.println(stop.getLat() + " " + stop.getLon() + " "
          + stop.getId() + " " + stop.getDirection());
    }

    

    System.out.println("yeah!");

    AlertConfiguration bean = new AlertConfiguration();
    bean.setType(EAlertType.REROUTE);
    bean.setRouteId(data.getRouteId());
    bean.setRegion(region);
    bean.setDirectionId(data.getDirectionId());
    bean.setReroute(getLineStringAsPoints(ls));
    EncodedPolylineBean polyline = PolylineEncoder.createEncodings(bean.getReroute());
    bean.setReroteAsPolylineString(polyline.getPoints());
    bean.setStopIds(stopIds);
    return bean;
  }

  private String getRegionForReroute(LineString reroute) {

    Geometry env = reroute.getEnvelope();

    Max<String> m = new Max<String>();
    for (T2<String, Geometry> entry : _regions) {
      String name = entry.getFirst();
      Geometry g = entry.getSecond();
      Geometry intersection = g.intersection(env);
      double area = intersection.getArea();
      m.add(area, name);
    }
    return m.getMaxElement();
  }

  private Pair<CoordinatePoint> getClosestEdge(
      DirectedGraph<CoordinatePoint> graph, CoordinatePoint point) {

    Min<Pair<CoordinatePoint>> m = new Min<Pair<CoordinatePoint>>();

    for (Pair<CoordinatePoint> edge : graph.getEdges()) {
      double d = getDistanceToEdge(edge, point);
      m.add(d, edge);
    }

    return m.getMinElement();
  }

  private double getDistanceToEdge(Pair<CoordinatePoint> edge,
      CoordinatePoint point) {
    XYPoint a = xy(edge.getFirst());
    XYPoint b = xy(edge.getSecond());
    XYPoint c = xy(point);
    XYPoint d = GeometryLibrary.projectPointToSegment(a, b, c);
    return d.getDistance(c);
  }

  private boolean connected(DirectedGraph<CoordinatePoint> graph,
      CoordinatePoint from, CoordinatePoint to, Set<CoordinatePoint> visited,
      List<Pair<CoordinatePoint>> edgesAlongPath) {
    if (from.equals(to))
      return true;
    if (!visited.add(from))
      return false;
    for (CoordinatePoint p : graph.getOutboundNodes(from)) {
      if (connected(graph, p, to, visited, edgesAlongPath)) {
        edgesAlongPath.add(Tuples.pair(from, p));
        return true;
      }
    }
    return false;
  }

  private XYPoint xy(CoordinatePoint p) {
    if (_projection == null)
      _projection = UTMLibrary.getProjectionForPoint(p);
    return _projection.forward(p);
  }

  private CoordinatePoint point(Point p) {
    return new CoordinatePoint(p.getY(), p.getX());
  }

  private List<CoordinatePoint> getLineStringAsPoints(LineString ls) {
    ArrayList<CoordinatePoint> points = new ArrayList<CoordinatePoint>();
    for (int i = 0; i < ls.getNumPoints(); i++)
      points.add(point(ls.getPointN(i)));
    points.trimToSize();
    return points;
  }

  private void dumpLineString(LineString ls) {
    System.out.println("===");
    for (int i = 0; i < ls.getNumPoints(); i++) {
      Point p = ls.getPointN(i);
      System.out.println(p.getY() + " " + p.getX());
    }
    System.out.println("===");
  }

}
