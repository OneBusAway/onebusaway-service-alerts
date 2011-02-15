package org.onebusaway.king_county_metro.service_alerts.builder;

import java.io.File;
import java.util.Iterator;

import org.geotools.feature.FeatureCollection;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public class ShapefileMain {
  public static void main(String[] args) throws Exception {
    for (String arg : args) {

      FeatureCollection<SimpleFeatureType, SimpleFeature> features = ShapefileLibrary.loadShapeFile(new File(
          arg));

      Iterator<SimpleFeature> it = features.iterator();

      while (it.hasNext()) {
        SimpleFeature feature = it.next();

        for (Property property : feature.getProperties()) {
          Object value = property.getValue();
          if (value instanceof Geometry)
            continue;
          System.out.println(property.getName() + "=" + value);
        }

        System.out.println("== geometry ==");
        Geometry geometry = (Geometry) feature.getDefaultGeometry();
        if (geometry != null)
          dump(geometry);

      }

      features.close(it);
    }
  }

  private static void dump(Geometry geometry) {

    if (geometry instanceof MultiPolygon) {
      dumpMultipolygon((MultiPolygon) geometry);
    } else if (geometry instanceof Polygon) {
      dumpPolygon((Polygon) geometry);
    } else if (geometry instanceof MultiLineString) {
      dumpMultiLineString((MultiLineString) geometry);
    } else if (geometry instanceof LineString) {
      dumpLineString((LineString) geometry);
    } else {
      System.err.println("unknown geo type: " + geometry.getClass());
    }
  }

  private static void dumpMultipolygon(MultiPolygon multi) {
    for (int i = 0; i < multi.getNumGeometries(); i++) {
      System.out.println("=== polygon i=" + i + " ===");
      dump(multi.getGeometryN(i));
    }
  }

  private static void dumpPolygon(Polygon poly) {
    dumpLineString(poly.getExteriorRing());
  }

  private static void dumpMultiLineString(MultiLineString mls) {
    for (int i = 0; i < mls.getNumGeometries(); i++) {
      System.out.println("=== line i=" + i + " ===");
      dump(mls.getGeometryN(i));
    }
  }

  private static void dumpLineString(LineString line) {
    for (int i = 0; i < line.getNumPoints(); i++) {
      Point point = line.getPointN(i);
      System.out.println(point.getY() + " " + point.getX());
    }
  }

}
