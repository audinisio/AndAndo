package com.javielinux.BubblesMap.view;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Paint.Style;
import android.location.Location;
import android.location.LocationManager;
import android.view.MotionEvent;

import com.android.dataframework.DataFramework;
import com.android.dataframework.Entity;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.javielinux.andando.R;
import com.javielinux.andando.ShowRoute;
import com.javielinux.andando.Utils;

public class MapLocationsManager {
		
	public static final int TYPE_MARKS_RESOURCES = 0;
	public static final int TYPE_MARKS_TIME = 1;
	public static final int TYPE_MARKS_DISTANCE = 2;
	public static final int TYPE_MARKS_NONE = 3;
	
	public static final int TYPE_DRAW_TOTAL = 0;
	public static final int TYPE_DRAW_SPLIT = 1;
	
	private int typeMarksOnMap = TYPE_MARKS_RESOURCES;
	
	private List<MapLocation> marksResources;
	private List<MapLocation> marksDistance;
	private List<MapLocation> marksTime;
	private List<MapLocation> marksRoute;
	private List<PathLocation> pointsRoute;
	private Path pathRoute;
	private MapLocationViewer mMapView;
	
	private int typeDraw = TYPE_DRAW_TOTAL;
	
    private float distance = 0;
    private float speed = 0;
	
	private boolean centerMap = true;
	
	private boolean prf_animate = true;
	
	private int prf_mindistance_path = 20;
		
	private boolean player = false;
	private int posLocationPlayer = 0;
	
	private Location lastLocation = null;

	public static Paint routePaint, textPaint, bgPaint, textGreenPaint, textRedPaint;
	
	private MapLocation selectedLocation = null;
	private int posSelectedLocation = -1;
	
	private boolean showRangeAccuracy = false;
	private int currentAccuracy = 0;
	private int prf_accuracy_gps = 0;
	
	private boolean reDraw = true;
	private GeoPoint gpoint00 = null;
	private Point pointIni;
	
	private CurrentLocation currentLocation = null;
	
	private boolean doingPath = false;
	private boolean isNewLocation = false;
	
	private float distancePathToMyLocation = 1000;
	private float distanceFromStart = 0;
	private Location locationMyLocation = null;
		
    /**
     * Constructor - Clase que se encarga de manejar todos los
     * MapLocations y la ruta que se dibujan en el mapa
     * 
     * @param mlv MapLocationViewer
     */
	
    public MapLocationsManager(MapLocationViewer mlv) {
    	   	
    	mMapView = mlv;
    	
    	pointIni = new Point();
    	    	
    	typeMarksOnMap = TYPE_MARKS_RESOURCES;
    	
    	if (Utils.preference!=null) {
        	prf_accuracy_gps = Integer.parseInt(Utils.preference.getString("prf_accuracy", "0"));
        	prf_mindistance_path = Integer.parseInt(Utils.preference.getString("prf_mindistance_path", "20"));
        	typeDraw = Integer.parseInt(Utils.preference.getString("prf_type_draw_route", "0"));
    		centerMap = Utils.preference.getBoolean("prf_center", true);
    		prf_animate = Utils.preference.getBoolean("prf_animate", true);
    	}    	
    	
    	marksRoute = new ArrayList<MapLocation>();
    	marksResources = new ArrayList<MapLocation>();
    	marksDistance = new ArrayList<MapLocation>();
    	marksTime = new ArrayList<MapLocation>();
    	   	
    	pointsRoute = new ArrayList<PathLocation>();
    	    	
    	pathRoute = new Path();
    	
    	reDraw = true;
    			
		routePaint = new Paint();
		routePaint.setARGB(255, 255, 0, 0);
		routePaint.setAntiAlias(true);
		routePaint.setStyle(Style.STROKE);
		routePaint.setStrokeWidth(2);
		
		textPaint = new Paint();
		textPaint.setARGB(255, 50, 50, 50);
		textPaint.setAntiAlias(true);
		textPaint.setStyle(Style.STROKE);
		textPaint.setStrokeWidth(1);
		
		textGreenPaint = new Paint();
		textGreenPaint.setARGB(255, 121, 147, 1);
		textGreenPaint.setAntiAlias(true);
		textGreenPaint.setStyle(Style.STROKE);
		textGreenPaint.setStrokeWidth(1);
		
		textRedPaint = new Paint();
		textRedPaint.setARGB(255, 207, 71, 11);
		textRedPaint.setAntiAlias(true);
		textRedPaint.setStyle(Style.STROKE);
		textRedPaint.setStrokeWidth(1);
		
		bgPaint = new Paint();
		bgPaint.setARGB(255, 255, 255, 255);
		bgPaint.setAntiAlias(true);
		bgPaint.setStyle(Style.FILL);
		
		currentLocation = new CurrentLocation(mMapView);
		
	}
    
    /**
     * Moverte a un punto del mapa
     * 
     * @param point Punto
     * 
     */
    
    public void moveTo(GeoPoint point) {
    	if (prf_animate)
    		mMapView.getController().animateTo(point);
    	else
    		mMapView.getController().setCenter(point);
    }
    
    /**
     * Crea la ruta en el mapa
     */
    
    public void createRoute(long idRoute, boolean endRoute) {
    	clear();
    	
		int minLatitude = (int)(+81 * 1E6);
	    int maxLatitude = (int)(-81 * 1E6);
	    int minLongitude  = (int)(+181 * 1E6);
	    int maxLongitude  = (int)(-181 * 1E6);
    	
    	Cursor c = DataFramework.getInstance().getCursor("locations", "route_id = " + idRoute, "position asc");
    	c.moveToFirst();
    	while (!c.isAfterLast()) {
    		int latitude = (int) (c.getFloat(c.getColumnIndex("latitude"))*1E6);
    		int longitude = (int) (c.getFloat(c.getColumnIndex("longitude"))*1E6);
    		minLatitude = (minLatitude > latitude) ? latitude : minLatitude;
    		maxLatitude = (maxLatitude < latitude) ? latitude : maxLatitude;               
    		minLongitude = (minLongitude > longitude) ? longitude : minLongitude;
    		maxLongitude = (maxLongitude < longitude) ? longitude : maxLongitude;
    		
    		Location loc = new Location(LocationManager.GPS_PROVIDER);
    		loc.setLatitude(c.getFloat(c.getColumnIndex("latitude")));
    		loc.setLongitude(c.getFloat(c.getColumnIndex("longitude")));
    		loc.setAltitude(c.getFloat(c.getColumnIndex("altitude")));
    		loc.setBearing(c.getFloat(c.getColumnIndex("bearing")));
    		addLocationOnPathRoute(loc, c.getInt(c.getColumnIndex("pause")), c.getLong(c.getColumnIndex(DataFramework.KEY_ID)));
    		
    		if (c.getInt(c.getColumnIndex("pause"))==1) {
    			addMarkContinue(loc);
    		}
    		
    		if (c.isLast()) {
    		    distance = c.getFloat(c.getColumnIndex("distance"));
    		    speed = c.getFloat(c.getColumnIndex("speed"));
    		}
    		c.moveToNext();
    		
    		if (!c.isAfterLast()) {
	    		if (c.getInt(c.getColumnIndex("pause"))==1) {
	    			addMarkPause(loc);
	    		}
    		}
    		
    	}
    	c.close();
    	
    	if (endRoute) { 
    		endRoute();
    		if (getStartGeoPoint() != null) {
    			mMapView.getController().zoomToSpan((maxLatitude - minLatitude), (maxLongitude - minLongitude));
    			mMapView.getController().animateTo(new GeoPoint((maxLatitude + minLatitude)/2, (maxLongitude + minLongitude)/2 ));
    		}
    			//moveTo(mMapView.getManager().getStartGeoPoint());
    	} else {
    		if (getEndGeoPoint() != null)
    			moveTo(getEndGeoPoint());    		
    	}
		    			
		createMarksResourcesOnRoute(idRoute);
		createMarksTimeOnRoute(idRoute);
		createMarksDistanceOnRoute(idRoute);
		reDraw = true;
    	
    }
    
    /**
     * Crea la ruta en el mapa
     */
    
    public void createRouteSimplified(long idRoute, boolean endRoute) {
    	clear();
    	Cursor c = DataFramework.getInstance().getCursor("locations", "route_id = " + idRoute, "position asc");
    	c.moveToFirst();
    	int count = 0;
    	while (!c.isAfterLast()) {
    		Location loc = null;
    		if ( (count%2==0) || (c.getInt(c.getColumnIndex("pause"))==1) ) {
	    		float lat = c.getFloat(c.getColumnIndex("latitude"));
	    		float lon = c.getFloat(c.getColumnIndex("longitude"));
	    		float alt = c.getFloat(c.getColumnIndex("altitude"));
	    		loc = new Location(LocationManager.GPS_PROVIDER);
	    		loc.setLatitude(lat);
	    		loc.setLongitude(lon);
	    		loc.setAltitude(alt);
	    		addLocationOnPathRoute(loc, c.getInt(c.getColumnIndex("pause")), c.getLong(c.getColumnIndex(DataFramework.KEY_ID)));
	    		if (c.getInt(c.getColumnIndex("pause"))==1) {
	    			addMarkContinue(loc);
	    		}
	    		
    		}
    		count++;
    		c.moveToNext();
    		
    		if (!c.isAfterLast()) {
	    		if (c.getInt(c.getColumnIndex("pause"))==1) {
	    			if (loc!=null) addMarkPause(loc);
	    		}
    		}
    		
    	}
    	c.close();
    	
    	if (endRoute) endRoute();
		    	
		if (getStartGeoPoint() != null)
			moveTo(getStartGeoPoint());
		
		createMarksResourcesOnRoute(idRoute);
		createMarksTimeOnRoute(idRoute);
		createMarksDistanceOnRoute(idRoute);
    	
    }
    
    
    /**
     * Crea los recursos ruta en el mapa
     */
    
    public void createMarksResourcesOnRoute(long idRoute) {
		Cursor c = DataFramework.getInstance().getCursor("resource", "route_id = " + idRoute, "");
		c.moveToFirst();
		while (!c.isAfterLast()) {
			int type = c.getInt(c.getColumnIndex("type_resource_id"));
			String text = c.getString(c.getColumnIndex("text"));
			String file = c.getString(c.getColumnIndex("file"));
			
			float lat = c.getFloat(c.getColumnIndex("latitude"));
    		float lon = c.getFloat(c.getColumnIndex("longitude"));
    		float alt = c.getFloat(c.getColumnIndex("altitude"));
    		Location loc = new Location(LocationManager.GPS_PROVIDER);
    		loc.setLatitude(lat);
    		loc.setLongitude(lon);
    		loc.setAltitude(alt);
    		
    		switch (type) {
    		case MapLocation.TYPE_BUBBLE_MSG:
    			addResource_Text(loc, text);
    			break;
    		case MapLocation.TYPE_BUBBLE_PHOTO:
    			addResource_Image(loc, file);
    			break;
    		case MapLocation.TYPE_BUBBLE_SOUND:
    			addResource_Sound(loc, file);
    			break;
    		}

			c.moveToNext();
		}
		c.close();
    }
    
    /**
     * Crea las marcas de tiempo en el mapa
     */
    
    public void createMarksTimeOnRoute(long idRoute) {
    	clearMarksTime();
    	Entity route = new Entity("routes", idRoute);
		int totalTime = route.getInt("time");
    	
    	int timeMarks = route.getInt("divide_time");
    	int nextTime = timeMarks;
    	
		Cursor c = DataFramework.getInstance().getCursor("locations", "route_id = " + idRoute, "");
		c.moveToFirst();
		while (!c.isAfterLast()) {
			int time = c.getInt(c.getColumnIndex("time"));
			
			if (time>=nextTime) {
				if (totalTime>nextTime + (timeMarks/10)) {
					float lat = c.getFloat(c.getColumnIndex("latitude"));
		    		float lon = c.getFloat(c.getColumnIndex("longitude"));
		    		float alt = c.getFloat(c.getColumnIndex("altitude"));
		    		Location loc = new Location(LocationManager.GPS_PROVIDER);
		    		loc.setLatitude(lat);
		    		loc.setLongitude(lon);
		    		loc.setAltitude(alt);
		    		addMarkTime(loc, Utils.formatShortTime(nextTime));
		    		nextTime += timeMarks;
				}
			}
			c.moveToNext();
		}
		c.close();
    }
    
    /**
     * Crea los marcas de distancia en el mapa
     */
    
    public void createMarksDistanceOnRoute(long idRoute) {
    	clearMarksDistance();
    	Entity route = new Entity("routes", idRoute);
		int totalDistance= (int)(route.getFloat("distance"));
		    	
    	int distanceMarks = route.getInt("divide_distance");
    	int nextDistance = distanceMarks;
    	
		Cursor c = DataFramework.getInstance().getCursor("locations", "route_id = " + idRoute, "");
		c.moveToFirst();
		while (!c.isAfterLast()) {
			int distance = c.getInt(c.getColumnIndex("distance"));
			
			if (distance>=nextDistance) {
				if (totalDistance>nextDistance + (distanceMarks/10)) {
					float lat = c.getFloat(c.getColumnIndex("latitude"));
		    		float lon = c.getFloat(c.getColumnIndex("longitude"));
		    		float alt = c.getFloat(c.getColumnIndex("altitude"));
		    		Location loc = new Location(LocationManager.GPS_PROVIDER);
		    		loc.setLatitude(lat);
		    		loc.setLongitude(lon);
		    		loc.setAltitude(alt);
		    		addMarkDistance(loc, Utils.formatDistance(nextDistance));
		    		nextDistance += distanceMarks;
				}
			}
			c.moveToNext();
		}
		c.close();
    }

    
    /**
     * Muestra la marca anterior
     * 
     */
    
    public void previousMark() {
    	boolean todo=false;
    	if (typeMarksOnMap == TYPE_MARKS_RESOURCES) {
    		if (marksResources.size()>0) {
    			todo=true;
	    		if (posSelectedLocation>=0) {
	    			if (posSelectedLocation>0) {
	    				posSelectedLocation--;
	    			} else {
	    				posSelectedLocation = marksResources.size()-1;
	    			}
	    		} else {
	    			posSelectedLocation = 0;
	    		}
	    		selectedLocation = marksResources.get(posSelectedLocation);
    		}
    	} else if (typeMarksOnMap == TYPE_MARKS_DISTANCE) {
    		if (marksDistance.size()>0) {
    			todo=true;
	    		if (posSelectedLocation>=0) {
	    			if (posSelectedLocation>0) {
	    				posSelectedLocation--;
	    			} else {
	    				posSelectedLocation = marksDistance.size()-1;
	    			}
	    		} else {
	    			posSelectedLocation = 0;
	    		}
	    		selectedLocation = marksDistance.get(posSelectedLocation);
    		}
    	} else if (typeMarksOnMap == TYPE_MARKS_TIME) {
    		if (marksTime.size()>0) {
    			todo=true;
	    		if (posSelectedLocation>=0) {
	    			if (posSelectedLocation>0) {
	    				posSelectedLocation--;
	    			} else {
	    				posSelectedLocation = marksTime.size()-1;
	    			}
	    		} else {
	    			posSelectedLocation = 0;
	    		}
	    		selectedLocation = marksTime.get(posSelectedLocation);
    		}
    	}
    	
    	if (todo) {
	    	if (selectedLocation.getType() == MapLocation.TYPE_BUBBLE_MSG) {
	    		((ShowRoute)mMapView.getMapActivity()).showInformation(selectedLocation.getType(), selectedLocation.getText());
	    	} else { 
	    		((ShowRoute)mMapView.getMapActivity()).showInformation(selectedLocation.getType(), selectedLocation.getFile());
	    	}
	    	
			if (selectedLocation!=null) {
				GeoPoint p = new GeoPoint((int)(selectedLocation.getLocation().getLatitude()*1E6), (int)(selectedLocation.getLocation().getLongitude()*1E6));
				moveTo(p);
			}
	    	
	    	//mMapView.refresh();
    	}
    }
    
    /**
     * Muestra la marca siguiente
     * 
     */
    
    public void nextMark() {
    	boolean todo=false;
    	if (typeMarksOnMap == TYPE_MARKS_RESOURCES) {
    		if (marksResources.size()>0) {
    			todo=true;
	    		if (posSelectedLocation>=0) {
	    			if (marksResources.size()-1>posSelectedLocation) {
	    				posSelectedLocation++;
	    			} else {
	    				posSelectedLocation = 0;
	    			}
	    		} else {
	    			posSelectedLocation = 0;
	    		}
	    		selectedLocation = marksResources.get(posSelectedLocation);
    		}
    	} else if (typeMarksOnMap == TYPE_MARKS_DISTANCE) {
    		if (marksDistance.size()>0) {
    			todo=true;
	    		if (posSelectedLocation>=0) {
	    			if (marksDistance.size()-1>posSelectedLocation) {
	    				posSelectedLocation++;
	    			} else {
	    				posSelectedLocation = 0;
	    			}
	    		} else {
	    			posSelectedLocation = 0;
	    		}
	    		selectedLocation = marksDistance.get(posSelectedLocation);
    		}
    	} else if (typeMarksOnMap == TYPE_MARKS_TIME) {
    		if (marksTime.size()>0) {
    			todo=true;
	    		if (posSelectedLocation>=0) {
	    			if (marksTime.size()-1>posSelectedLocation) {
	    				posSelectedLocation++;
	    			} else {
	    				posSelectedLocation = 0;
	    			}
	    		} else {
	    			posSelectedLocation = 0;
	    		}
	    		selectedLocation = marksTime.get(posSelectedLocation);
    		}
    	}
    	
    	if (todo) {
			if (selectedLocation.getType() == MapLocation.TYPE_BUBBLE_MSG) {
				((ShowRoute)mMapView.getMapActivity()).showInformation(selectedLocation.getType(), selectedLocation.getText());
			} else {
				((ShowRoute)mMapView.getMapActivity()).showInformation(selectedLocation.getType(), selectedLocation.getFile());
			}
			
			if (selectedLocation!=null) {
				GeoPoint p = new GeoPoint((int)(selectedLocation.getLocation().getLatitude()*1E6), (int)(selectedLocation.getLocation().getLongitude()*1E6));
				moveTo(p);
			}
			
	    	//mMapView.refresh();
    	}
    }
    
    /**
     * Muestra la capa de marcas anteriores
     * 
     */
    
    public void previousLayerMarks() {
    	if (typeMarksOnMap==0) {
    		typeMarksOnMap = 2;
    	} else {
    		typeMarksOnMap--;
    	}
    	unSelectedLocation();
    	mMapView.refresh();
    }
    
    /**
     * Muestra la capa de marcas siguientes
     * 
     */
    
    public void nextLayerMarks() {
    	if (typeMarksOnMap==2) {
    		typeMarksOnMap = 0;
    	} else {
    		typeMarksOnMap++;
    	}
    	unSelectedLocation();
    	mMapView.refresh();
    }
    
    /**
     * Elimina la seleccion de una marca si existiera
     */
    
	public void unSelectedLocation() {
		selectedLocation = null;
		posSelectedLocation = -1;
	}  
    
    /**
     * Limpia el mapa
     */
    
	public void clear() {
		marksRoute.clear();
		marksResources.clear();
		marksDistance.clear();
		marksTime.clear();
		pointsRoute.clear();
	}
	
    /**
     * Limpia marcas de distancia en el mapa
     */
    
	public void clearMarksDistance() {
		marksDistance.clear();
	}
	
    /**
     * Limpia marcas de tiempo en el mapa
     */
    
	public void clearMarksTime() {
		marksTime.clear();
	}
	
    /**
     * Agrega un nuevo recurso al mapa
     * 
     * @param ml nuevo MapLocation
     */
    
	public void addMarkResource(MapLocation ml) {
		marksResources.add(ml);
	}
	
    /**
     * Agrega un nuevo marca de tiempo al mapa
     * 
     * @param ml nuevo MapLocation
     */
    
	public void addMarkTime(MapLocation ml) {
		marksTime.add(ml);
	}
	
    /**
     * Agrega un nuevo marca de distancia al mapa
     * 
     * @param ml nuevo MapLocation
     */
    
	public void addMarkDistance(MapLocation ml) {
		marksDistance.add(ml);
	}
	
    /**
     * Agrega un nuevo marca de ruta al mapa
     * 
     * @param ml nuevo MapLocation
     */
    
	public void addMarkRoute(MapLocation ml) {
		marksRoute.add(ml);
	}
	
    /**
     * Devuelve la lista de marcas de recursos
     * 
     * @return Lista de MapLocations
     */
	
	public List<MapLocation> getMarksResources() {
		return marksResources;
	}
	
    /**
     * Devuelve la lista de marcas de tiempo
     * 
     * @return Lista de MapLocations
     */
	
	public List<MapLocation> getMarksTime() {
		return marksTime;
	}
	
    /**
     * Devuelve la lista de marcas de distancia
     * 
     * @return Lista de MapLocations
     */
	
	public List<MapLocation> getMarksDistance() {
		return marksDistance;
	}
	
    /**
     * Devuelve la lista de marcas de ruta
     * 
     * @return Lista de MapLocations
     */
	
	public List<MapLocation> getMarksRoute() {
		return marksRoute;
	}
	
    /**
     * Devuelve el MapLocations en el caso que hubiese alguno
     * 
     * @return MapLocation
     */
	
	public MapLocation getSelectedMapLocation() {
		return selectedLocation;
	}
	
    /**
     * Primera localizacion de la ruta
     * 
     * @return Location
     */
	
	public GeoPoint getStartGeoPoint() {
		if (pointsRoute.size()>0)
			return pointsRoute.get(0).geopoint;
		else 
			return null;
	}
	
	public void goToStart() {
		GeoPoint gp = getStartGeoPoint();
		if (gp!=null) {
			moveTo(gp);
		}
	}
	
    /**
     * Ultima localizacion de la ruta
     * 
     * @return Location
     */
	
	public GeoPoint getEndGeoPoint() {
		if (pointsRoute.size()>0)
			return pointsRoute.get(pointsRoute.size()-1).geopoint;
		else 
			return null;
	}
	
	/**
     * Agrega un nuevo recurso de texto en la ruta
     * 
     * @param loc nuevo Location
     * @param text Texto
     */
	
	public void addResource_Text(Location loc, String text) {
		if (loc!=null) {
			MapLocation ml = new MapLocation(mMapView, "recurso_" + marksResources.size(), text, loc, MapLocation.TYPE_BUBBLE_MSG);
			addMarkResource(ml);
		} else {
            Utils.showMessage(Utils.context.getResources().getString(R.string.no_location));
		}
	}
		
	/**
     * Agrega un nuevo recurso de imagen en la ruta
     * 
     * @param loc nuevo Location
     * @param text Texto
     */
	
	public void addResource_Image(Location loc, String path) {
		if (loc!=null) {
			MapLocation ml = new MapLocation(mMapView, "recurso_" + marksResources.size(), "", loc, MapLocation.TYPE_BUBBLE_PHOTO);
			ml.setFile(path);
			addMarkResource(ml);
		} else {
			Utils.showMessage(Utils.context.getResources().getString(R.string.no_location));
		}
	}
	
	public void addResource_Sound(Location loc, String path) {
		if (loc!=null) {
			MapLocation ml = new MapLocation(mMapView, "recurso_" + marksResources.size(), "", loc, MapLocation.TYPE_BUBBLE_SOUND);
			ml.setFile(path);
			addMarkResource(ml);
		} else {
			Utils.showMessage(Utils.context.getResources().getString(R.string.no_location));
		}
	}
	
	public void addMarkContinue(Location loc) {
		if (loc!=null) {
			MapLocation ml = new MapLocation(mMapView, "continue", mMapView.getResources().getString(R.string.route_continue), loc, MapLocation.TYPE_BUBBLE_CONTINUE);
			addMarkRoute(ml);
		} else {
			Utils.showMessage(Utils.context.getResources().getString(R.string.no_location));
		}
	}
	
	public void addMarkPause(Location loc) {
		if (loc!=null) {
			MapLocation ml = new MapLocation(mMapView, "pause", mMapView.getResources().getString(R.string.route_pause), loc, MapLocation.TYPE_BUBBLE_PAUSE);
			addMarkRoute(ml);
		} else {
			Utils.showMessage(Utils.context.getResources().getString(R.string.no_location));
		}
	}
	
	/**
     * Agrega una nueva marca de tiempo
     * 
     * @param loc nuevo Location
     */
	
	public void addMarkTime(Location loc, String text) {
		if (loc!=null) {
			MapLocation ml = new MapLocation(mMapView, "time_" + marksTime.size(), text, loc, MapLocation.TYPE_BUBBLE_TIME);
			addMarkTime(ml);
		} else {
			Utils.showMessage(Utils.context.getResources().getString(R.string.no_location));
		}
	}
	
	/**
     * Agrega una nueva marca de distancia
     * 
     * @param loc nuevo Location
     */
	
	public void addMarkDistance(Location loc, String text) {
		if (loc!=null) {
			MapLocation ml = new MapLocation(mMapView, "distance_" + marksDistance.size(), text, loc, MapLocation.TYPE_BUBBLE_DISTANCE);
			addMarkDistance(ml);
		} else {
			Utils.showMessage(Utils.context.getResources().getString(R.string.no_location));
		}
	}
	
    /**
     * Agrega una nueva localizacion a la ruta del mapa
     * 
     * @param loc nuevo Location
     */
	
	public void addLocationOnPathRoute(Location loc, int pause, long id) {
		if (pointsRoute.size()==0) {
			MapLocation ml = new MapLocation(mMapView, "start", mMapView.getResources().getString(R.string.start), loc, MapLocation.TYPE_BUBBLE_START);
			addMarkRoute(ml);
		}
		PathLocation pl = new PathLocation();
		pl.location = loc;
		pl.geopoint = Utils.Location2Geopoint(loc);
		pl.pause = pause;
		pl.id = id;
		pointsRoute.add(pl);
		
		reDraw = true;
		
		lastLocation = loc;
		if (centerMap) {
			GeoPoint p = new GeoPoint((int)(loc.getLatitude()*1E6), (int)(loc.getLongitude()*1E6));
			moveTo(p);
		}
	}
	
	public void createPathRoute() {
		if (typeDraw == TYPE_DRAW_TOTAL)
			createPathRouteTotal();
		else
			createPathRouteSplit();
	}
	
    /**
     * Crea el objeto Path con la ruta
     * 
     */
	
	public void createPathRouteTotal() {
				
		/*if (mMapView.getZoomLevel() != zoom) {
			reDraw = true;
			
			if (gpoint00==null) {
				zoom = mMapView.getZoomLevel();
			} else {
				Point p = new Point();
				mMapView.getProjection().toPixels(gpoint00, p);
				
				if (p.x==0 && p.y==0) {
					zoom = mMapView.getZoomLevel();
				}
			}
			
		}*/
		
		if (pointsRoute.size()>0) {
		
			if (!reDraw) {			
				Point p = new Point();
				mMapView.getProjection().toPixels(gpoint00, p);
				
				pathRoute.offset(p.x, p.y);
				
				pointIni.offset(p.x, p.y);
				
				Point pointIni2 = new Point();
				mMapView.getProjection().toPixels(getStartGeoPoint(), pointIni2);
				
				if (pointIni.x!=pointIni2.x || pointIni.y!=pointIni2.y) {
					reDraw = true;
				}
			
			}
					
			if (reDraw) {
				
				//System.out.println("pintando...");
					
				pathRoute.reset();
						
				Iterator<PathLocation> itPointRoute = pointsRoute.iterator();
				boolean sw = true;

				int auxX =  0;
				int auxY =  0;
				while (itPointRoute.hasNext()) {   
					Point p = new Point();
					
					PathLocation pl = itPointRoute.next();
					
					mMapView.getProjection().toPixels(pl.geopoint, p);
		    		
		    		if (sw) { 
		    			pointIni = p;
		    			pathRoute.moveTo(p.x, p.y);
		    			sw = false;
		    		} else {
		    			if (pl.pause==1) {
	    					pathRoute.moveTo(p.x, p.y);
		    			} else {
			    			if ( (auxX!= p.x) || (auxY!= p.y) ) {
		    					pathRoute.lineTo(p.x, p.y);
			    			}
		    			}
		    		}
		    		
		    		auxX =  p.x;
					auxY =  p.y;
		    	}
				
				reDraw = false;
				
			}
			
		}
		gpoint00 = mMapView.getProjection().fromPixels(0, 0);
		
	}
	
	
	public void createPathRouteSplit() {
		pathRoute.reset();
				
		Iterator<PathLocation> itPointRoute = pointsRoute.iterator();
		boolean sw = true;
		boolean lastPointOutScreen = false;
		
		int auxX =  0;
		int auxY =  0;
		
		while (itPointRoute.hasNext()) {   
			Point p = new Point();
			PathLocation pl = itPointRoute.next();
			GeoPoint gp = pl.geopoint;

    		mMapView.getProjection().toPixels(gp, p);
    		
    		boolean outScreen = false;
            if (mMapView.getResources().getConfiguration().orientation==2) {
            	if ( (p.x<0) || (p.x>480) || (p.y<0) || (p.y>320) ) {
            		outScreen = true;
            	}
            } else {
            	if ( (p.x<0) || (p.x>320) || (p.y<0) || (p.y>480) ) {
            		outScreen = true;
            	}
            }
            if (!(outScreen && lastPointOutScreen)) {
	    		if (sw) { 
	    			pathRoute.moveTo(p.x, p.y);
	    			sw = false;
	    		} else {
	    			if (pl.pause==1) {
	    				pathRoute.moveTo(p.x, p.y);
	    			} else {
		    			if ( (auxX!= p.x) || (auxY!= p.y) ) {
		    				if (lastPointOutScreen && !outScreen) pathRoute.moveTo(auxX, auxY);
	    					pathRoute.lineTo(p.x, p.y);
		    			}
	    			}
	    		}
            }
            lastPointOutScreen = outScreen;
            auxX =  p.x;
			auxY =  p.y;
    	}
		
	}
	
	/*
	public void createPathRoute() {
		pathRoute.reset();
				
		boolean sw = true;
		boolean lastPointOutScreen = false;
		
		int auxX =  0;
		int auxY =  0;
		
		//Location loc1 = Utils.Geopoint2Location(mMapView.getProjection().fromPixels(0, 0));
		//Location loc2 = Utils.Geopoint2Location(mMapView.getProjection().fromPixels(mMapView.getWidth(), mMapView.getHeight()));
		
		Cursor c = DataFramework.getInstance().getCursor("locations", "route_id = " + idRoute, "position asc");
    	c.moveToFirst();
    	while (!c.isAfterLast()) {
		   
			Point p = new Point();
    		Location loc = new Location(LocationManager.GPS_PROVIDER);
    		loc.setLatitude(c.getFloat(c.getColumnIndex("latitude")));
    		loc.setLongitude(c.getFloat(c.getColumnIndex("longitude")));

    		mMapView.getProjection().toPixels(Utils.Location2Geopoint(loc), p);
    		
    		boolean outScreen = false;
            if (mMapView.getResources().getConfiguration().orientation==2) {
            	if ( (p.x<0) || (p.x>480) || (p.y<0) || (p.y>320) ) {
            		outScreen = true;
            	}
            } else {
            	if ( (p.x<0) || (p.x>320) || (p.y<0) || (p.y>480) ) {
            		outScreen = true;
            	}
            }
            if (!(outScreen && lastPointOutScreen)) {
	    		if (sw) { 
	    			pathRoute.moveTo(p.x, p.y);
	    			sw = false;
	    		} else {
	    			if ( (auxX!= p.x) || (auxY!= p.y) ) {
	    				pathRoute.lineTo(p.x, p.y);
	    			}
	    		}
            }
            lastPointOutScreen = outScreen;
            auxX =  p.x;
			auxY =  p.y;
			c.moveToNext();
    	}
		
	}
	*/
    /**
     * Finaliza la ruta
     * 
     */
	
	public void endRoute() {
		if (pointsRoute.size()>0) {
			MapLocation ml = new MapLocation(mMapView, "end", mMapView.getResources().getString(R.string.end), Utils.Geopoint2Location(getEndGeoPoint()), MapLocation.TYPE_BUBBLE_END);
			addMarkRoute(ml);
			mMapView.refresh();
		}
	}
	
    /**
     * Comprueba si se ha pulsado sobre un MapLocation
     * 
     * @param mapView Mapa
     * @param event MotionEvent
     * 
     */
	
	
    public boolean verifyHitMapLocation(MapView mapView, MotionEvent event) {
    	if (event.getAction()==MotionEvent.ACTION_DOWN) {
	    	Iterator<MapLocation> iterator = getMarksRoute().iterator();
	    	while(iterator.hasNext()) {
	    		MapLocation testLocation = iterator.next();
	    		
	    		Point p = new Point();
	    		
	    		mapView.getProjection().toPixels(testLocation.getGeoPoint(), p);
	    		
	    		if (testLocation.getHit(p.x, p.y, event.getX(),event.getY())) {
	    			if (mMapView.getTypeActivity() == Utils.ACTIVITY_SHOW) {
	    				if (testLocation.getType() == MapLocation.TYPE_BUBBLE_MSG) {
	    					((ShowRoute)mMapView.getMapActivity()).showInformation(testLocation.getType(), testLocation.getText());
	    				} else { 
	    					((ShowRoute)mMapView.getMapActivity()).showInformation(testLocation.getType(), testLocation.getFile());
	    				}
	    				
		    			if (selectedLocation == testLocation) {
		    				selectedLocation = null;
		    			} else {
		    				selectedLocation = testLocation;
		    			}
	    			}

	    		}
	    	}
	    	
	    	if (typeMarksOnMap == TYPE_MARKS_RESOURCES) {
	    		
	    		Iterator<MapLocation> itResources = getMarksResources().iterator();
	    		int count = 0;
		    	while(itResources.hasNext()) {
		    		MapLocation testLocation = itResources.next();
		    		
		    		Point p = new Point();
		    		
		    		mapView.getProjection().toPixels(testLocation.getGeoPoint(), p);
		    		
		    		if (testLocation.getHit(p.x, p.y, event.getX(),event.getY())) {
		    			if (mMapView.getTypeActivity() == Utils.ACTIVITY_SHOW) {
		    				if (testLocation.getType() == MapLocation.TYPE_BUBBLE_MSG) {
		    					((ShowRoute)mMapView.getMapActivity()).showInformation(testLocation.getType(), testLocation.getText());
		    				} else { 
		    					((ShowRoute)mMapView.getMapActivity()).showInformation(testLocation.getType(), testLocation.getFile());
		    				}
		    				
			    			if (selectedLocation == testLocation) {
			    				selectedLocation = null;
			    			} else {
			    				selectedLocation = testLocation;
			    				posSelectedLocation = count;
			    			}
		    			}

		    		}
		    		count++;
		    	}
	    		
	    	}
	    	
	    	if (typeMarksOnMap == TYPE_MARKS_TIME) {
	    		
	    		Iterator<MapLocation> itTime = getMarksTime().iterator();
	    		int count = 0;
		    	while(itTime.hasNext()) {
		    		MapLocation testLocation = itTime.next();
		    		
		    		Point p = new Point();
		    		
		    		mapView.getProjection().toPixels(testLocation.getGeoPoint(), p);
		    		
		    		if (testLocation.getHit(p.x, p.y, event.getX(),event.getY())) {
		    			if (mMapView.getTypeActivity() == Utils.ACTIVITY_SHOW) {
	    					((ShowRoute)mMapView.getMapActivity()).showInformation(testLocation.getType(), testLocation.getText());
		    				
			    			if (selectedLocation == testLocation) {
			    				selectedLocation = null;
			    			} else {
			    				selectedLocation = testLocation;
			    				posSelectedLocation = count;
			    			}
		    			}

		    		}
		    		count++;
		    	}
	    		
	    	}
	    	
	    	if (typeMarksOnMap == TYPE_MARKS_DISTANCE) {
	    		
	    		Iterator<MapLocation> itDistance = getMarksDistance().iterator();
	    		int count = 0;
		    	while(itDistance.hasNext()) {
		    		MapLocation testLocation = itDistance.next();
		    		
		    		Point p = new Point();
		    		
		    		mapView.getProjection().toPixels(testLocation.getGeoPoint(), p);
		    		
		    		if (testLocation.getHit(p.x, p.y, event.getX(),event.getY())) {
		    			if (mMapView.getTypeActivity() == Utils.ACTIVITY_SHOW) {
	    					((ShowRoute)mMapView.getMapActivity()).showInformation(testLocation.getType(), testLocation.getText());
		    				
			    			if (selectedLocation == testLocation) {
			    				selectedLocation = null;
			    			} else {
			    				selectedLocation = testLocation;
			    				posSelectedLocation = count;
			    			}
		    			}

		    		}
		    		count++;
		    	}
	    		
	    	}
	    	
	    	
    	}
    	//selectedLocation = null;
    	return false; 
    }
    
    /**
     * Dibuja la ruta y los MapLocations en el mapa
     * 
     * @param canvas Canvas sobre el que se dibuja
     * @param mapView Mapa
     * @param shadow Si es la sombra
     */
    
    public void draw(Canvas canvas, MapView mapView, boolean shadow) {

    	if (shadow) {
    		createPathRoute();
    		canvas.drawPath(pathRoute, routePaint);
    		if (isPlayer()) {
    			
    			Point p = new Point();
        		mMapView.getProjection().toPixels(pointsRoute.get(posLocationPlayer).geopoint, p);

        		canvas.drawCircle(p.x, p.y, 2, routePaint);
        		canvas.drawCircle(p.x, p.y, 6, routePaint);
    		}
    		
    		if (isDoingPath() && currentLocation.getLocation()!=null) {
    			if (isNewLocation) {
    				isNewLocation = false;
	    			Iterator<PathLocation> itPointRoute = pointsRoute.iterator();
	    			distancePathToMyLocation = 1000;
	    			locationMyLocation = null;
					while (itPointRoute.hasNext()) {   
						PathLocation pl = itPointRoute.next();
						float dt = currentLocation.getLocation().distanceTo(pl.location);
						if (dt<distancePathToMyLocation) {
							distancePathToMyLocation = dt;
							locationMyLocation = pl.location;
							Entity ent = new Entity ("locations", pl.id);
							distanceFromStart = ent.getFloat("distance");
						}
					}
					
    			}
				if (distancePathToMyLocation<prf_mindistance_path) {
					currentLocation.setLocation(locationMyLocation);
					currentLocation.setType(CurrentLocation.TYPE_GREEN);
					canvas.drawRect(0, 0, canvas.getWidth(), 31, bgPaint);
	    			canvas.drawText(Utils.formatDistance(distanceFromStart) + " " + mapView.getContext().getString(R.string.location_in_path_text), 10, 20, textGreenPaint);
				} else {
					currentLocation.setType(CurrentLocation.TYPE_RED);
					canvas.drawRect(0, 0, canvas.getWidth(), 31, bgPaint);
	    			canvas.drawText(Utils.formatDistance(distancePathToMyLocation) + " " + mapView.getContext().getString(R.string.location_out_path_text), 10, 20, textRedPaint);
				}
				currentLocation.draw(canvas, mapView);    			
				
    		}
    		
    		if (showRangeAccuracy) {
    			canvas.drawRect(0, 0, canvas.getWidth(), 54, bgPaint);
    			canvas.drawText(mapView.getContext().getString(R.string.accuracy_greater_than) + " " + prf_accuracy_gps + " " + mapView.getContext().getString(R.string.metres) + ". (" + mapView.getContext().getString(R.string.currently) + " " + currentAccuracy + " " + mapView.getContext().getString(R.string.metres) + ")", 
    					10, 20, textPaint);
    			canvas.drawText(mapView.getContext().getString(R.string.preference_currently), 
    					10, 43, textPaint);
    		}
    		
    	}
    	    	
		Iterator<MapLocation> iterator = getMarksRoute().iterator();

		while(iterator.hasNext()) {	   
    		MapLocation location = iterator.next();
    		location.draw(canvas, mapView, shadow);
    	}
		
		if (typeMarksOnMap == TYPE_MARKS_RESOURCES) {
			Iterator<MapLocation> itResources = getMarksResources().iterator();

			while(itResources.hasNext()) {	   
	    		MapLocation location = itResources.next();
	    		location.draw(canvas, mapView, shadow);
	    	}
		}
		
		if (typeMarksOnMap == TYPE_MARKS_TIME) {
			Iterator<MapLocation> itTime = getMarksTime().iterator();

			while(itTime.hasNext()) {	   
	    		MapLocation location = itTime.next();
	    		location.draw(canvas, mapView, shadow);
	    	}
		}
		
		if (typeMarksOnMap == TYPE_MARKS_DISTANCE) {
			Iterator<MapLocation> itDistance = getMarksDistance().iterator();

			while(itDistance.hasNext()) {	   
	    		MapLocation location = itDistance.next();
	    		location.draw(canvas, mapView, shadow);
	    	}
		}
		
		// dibujar on
    	if ( selectedLocation != null) {
    		selectedLocation.drawOn(canvas, mapView, shadow);
    	}
    	
    	if (shadow) currentLocation.draw(canvas, mapView);
    	
    }

    /**
     * Devuelve la lista de Locations con la ruta
     * @return Lista de Location
     */
	
	public List<PathLocation> getPointsRoute() {
		return pointsRoute;
	}

    /**
     * Devuelve si el mapa se centra cada vez se da se encuentra
     * una nueva localizacion por GPS
     * @return Boolean
     */
	
	public boolean isCenterMap() {
		return centerMap;
	}
	

    /**
     * Devuelve la ultima localizacion
     * @return Location
     */
	
	public Location getLastLocation() {
		return lastLocation;
	}

    /**
     * Devuelve el numero de localizaciones de la ruta
     * @return Numero de localizaciones
     */
	
	public int getNLocations() {
		return pointsRoute.size();
	}
	
    /**
     * Devuelve el tipo de marcas que se veran en el mapa
     * @return Tipo de marcas
     */

	public int getTypeMarksOnMap() {
		return typeMarksOnMap;
	}
	
    /**
     * Establece el tipo de marcas que se veran en el mapa
     * @param typeMarksOnMap Tipo de marcas
     */

	public void setTypeMarksOnMap(int typeMarksOnMap) {
		this.typeMarksOnMap = typeMarksOnMap;
	}

	public float getDistance() {
		return distance;
	}

	public float getSpeed() {
		return speed;
	}

	public boolean isPlayer() {
		return player;
	}

	public void startPlayer() {
		this.player = true;
		setPositionPlayer(posLocationPlayer);
	}
    
	public void stopPlayer() {
		this.player = false;
	}
	
	public void setPositionPlayer(int pos) {
		posLocationPlayer = pos;
		moveTo(pointsRoute.get(posLocationPlayer).geopoint);
		//mMapView.refresh();
	}

	public int getPositionPlayer() {
		return posLocationPlayer;
	}

	public void setShowRangeAccuracy(boolean showRangeAccuracy, int currentAccuracy) {
		this.showRangeAccuracy = showRangeAccuracy;
		this.currentAccuracy = currentAccuracy;
	}

	public boolean isShowRangeAccuracy() {
		return showRangeAccuracy;
	}
	
	public void showCurrentLocation() {
		currentLocation.show();
	}
	
	public void hideCurrentLocation() {
		currentLocation.hide();
	}
	
	public void setCurrentLocation(Location location) {
		currentLocation.setLocation(location);
		isNewLocation = true;
	}
	
	public void centerCurrentLocation() {
		if (centerMap) {
			GeoPoint p = new GeoPoint((int)(currentLocation.getLocation().getLatitude()*1E6), (int)(currentLocation.getLocation().getLongitude()*1E6));
			moveTo(p);
		}
	}

	public void setDoingPath(boolean doingPath) {
		this.doingPath = doingPath;
	}

	public boolean isDoingPath() {
		return doingPath;
	}

	class PathLocation {
		Location location;
		GeoPoint geopoint;
		int pause = 0;
		long id = 0;
	}
	
}
