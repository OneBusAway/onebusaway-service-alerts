package org.onebusaway.king_county_metro.service_alerts.builder;

import java.io.File;

import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureSource;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.factory.Hints;
import org.geotools.feature.FeatureCollection;
import org.geotools.referencing.ReferencingFactoryFinder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CRSAuthorityFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

public class ShapefileLibrary {

  public static FeatureCollection<SimpleFeatureType, SimpleFeature> loadShapeFile(
      File path) throws Exception {
  
    ShapefileDataStore dataStore = new ShapefileDataStore(path.toURI().toURL());
  
    String typeNames[] = dataStore.getTypeNames();
    String typeName = typeNames[0];
  
    FeatureSource<SimpleFeatureType, SimpleFeature> featureSource = dataStore.getFeatureSource(typeName);
    CoordinateReferenceSystem sourceCRS = featureSource.getInfo().getCRS();
  
    Hints hints = new Hints(Hints.FORCE_LONGITUDE_FIRST_AXIS_ORDER,
        Boolean.TRUE);
    CRSAuthorityFactory factory = ReferencingFactoryFinder.getCRSAuthorityFactory(
        "EPSG", hints);
    CoordinateReferenceSystem worldCRS = factory.createCoordinateReferenceSystem("EPSG:4326");
  
    DefaultQuery query = new DefaultQuery();
    query.setCoordinateSystem(sourceCRS);
    query.setCoordinateSystemReproject(worldCRS);
  
    return featureSource.getFeatures(query);
  }

}
