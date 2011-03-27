package com.javielinux.andando;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.os.Vibrator;

import com.android.dataframework.DataFramework;
import com.android.dataframework.Entity;
import com.javielinux.BubblesMap.view.MapLocationsManager;

public class ServiceGPS extends Service {
	
	private static final long MILLISECONDS_VIBRATE = 2000;
	
	private static long elapsedRealtime = SystemClock.elapsedRealtime();
	
	private LocationManager mLocationManager;
	private MyLocationListener mLocationListener;
		
    public static int divideDistance = 0;
    public static int divideTime = 0;
    public static int nextDistance = 0;
    public static int nextTime = 0;
    
    public static int typeMarksOnMap = 0;
    
    private boolean running = false;
	
    private float distance = 0;
    private float speed = 0;
    private long idRoute = -1;
    private int position= 0;
    private static Location lastLocationOnDataBase = null;
    
    private static boolean pause = false;
    private static boolean todoPauseInEntity = false;
    
    private float prf_accuracy_gps = 0;
    private boolean prf_vibrate_marks = true;
    private boolean prf_notification_marks = false;
    private long prf_time_gps = 0;
    private float prf_distance_gps = 0;
    
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	public static void startTime() {
		elapsedRealtime = SystemClock.elapsedRealtime();
	}

	@Override
	public void onCreate() {
		super.onCreate();
		prf_accuracy_gps = Float.parseFloat(Utils.preference.getString("prf_accuracy", "0"));
		prf_vibrate_marks = Utils.preference.getBoolean("prf_vibrate_marks", true);
		prf_notification_marks = Utils.preference.getBoolean("prf_notification_marks", false);
		prf_time_gps = Long.parseLong(Utils.preference.getString("prf_time_gps", "0"));
		prf_distance_gps = Float.parseFloat(Utils.preference.getString("prf_distance_gps", "0"));
				
		pause = false;
		todoPauseInEntity = false;
		distance = 0;
	    speed = 0;
		mLocationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
	}
	
	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
			
		Bundle extras = intent.getExtras();  
		if (extras != null) {
			idRoute = (extras.containsKey(DataFramework.KEY_ID)) ? extras.getLong(DataFramework.KEY_ID) : -1;
		}
		_startService();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		_shutdownService();
	}
	
	private void _startService() {
		        
        try {
        	DataFramework.getInstance().open(this, Utils.getPackage());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			running = true;
			startTime();
			long timeRegister = 1000;
			float distanceRegister = 5;
			
        	if (prf_time_gps==0) {
        		Entity e = new Entity("routes", idRoute);
        		timeRegister = Long.parseLong(e.getEntity("category_id").getValue("default_time_gps").toString())*1000;
        	} else {
        		timeRegister = prf_time_gps;
        	}
        	if (prf_distance_gps==0) {
        		Entity e = new Entity("routes", idRoute);
        		distanceRegister = e.getEntity("category_id").getFloat("default_distance_gps");
        	} else {
        		distanceRegister = prf_distance_gps;
        	}
						
			mLocationListener = new MyLocationListener();
			mLocationManager.removeUpdates(mLocationListener);
			
			mLocationManager.requestLocationUpdates(
	                LocationManager.GPS_PROVIDER, timeRegister, distanceRegister, mLocationListener);
		}/* else {
			
			if (mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
				long timeRegister = 1000;
				float distanceRegister = 5;
									
				mLocationListener = new MyLocationListener();
				mLocationManager.removeUpdates(mLocationListener);
				
				mLocationManager.requestLocationUpdates(
		                LocationManager.NETWORK_PROVIDER, timeRegister, distanceRegister, mLocationListener);
			}
			
		}*/
	}
	
	private void _shutdownService() {
		running = false;
		DataFramework.getInstance().close();
		if ( (mLocationManager != null) && (mLocationListener != null) ) mLocationManager.removeUpdates(mLocationListener);
	}
		
    private class MyLocationListener implements LocationListener 
    {
    	
        /**
         * Metodo que se ejecuta cada vez que cambia la localizacion del GPS
         * 
         * @param loc Objeto Location
         * 
         */
    	
        @Override
        public void onLocationChanged(Location loc) {
            if (loc != null) {
            	if (running) {
            		createNewLocation(loc);
            	}
            }
        }

        @Override
        public void onProviderDisabled(String provider) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    }
    	
    /**
     * Crear nueva localizacion
     * 
     * @param loc Location
     * 
     */
    
    public void createNewLocation(Location loc) {
    	
    	if (!pause) {
    		Intent i = new Intent(Intent.ACTION_VIEW);
    		i.putExtra("location", loc);
    		
	    	if ( (prf_accuracy_gps>=loc.getAccuracy()) || (prf_accuracy_gps==0)) {
	    		
	    		i.putExtra("showRangeAccuracy", false);
	    		
				Entity ent = new Entity("locations");
				ent.setValue("route_id", idRoute);
				ent.setValue("latitude", loc.getLatitude());
				ent.setValue("longitude", loc.getLongitude());
				ent.setValue("altitude", loc.getAltitude());
				ent.setValue("accuracy", loc.getAccuracy());
				ent.setValue("position", position);
				
				if (todoPauseInEntity) {
					ent.setValue("pause", 1);
					todoPauseInEntity = false;
				} else {
					ent.setValue("pause", 0);
					
					// la distancia sólo se suma si no es una pausa
					
					if (lastLocationOnDataBase != null) {
						float d = lastLocationOnDataBase.distanceTo(loc);
						distance += d;
					}
				}
		
				int time = (int)((SystemClock.elapsedRealtime() - elapsedRealtime)/1000);
								
				float auxSpeed = (loc.getSpeed()/1000)*3600;
				
				if ( (auxSpeed<200) &&  (Math.abs(speed-auxSpeed)<100) ) {
					speed = auxSpeed;
				}
				
				ent.setValue("distance", distance);
				ent.setValue("speed", speed);
				ent.setValue("bearing", loc.getBearing());
				
				ent.setValue("time", time);
				ent.save();
				
				i.putExtra("distance", distance);
				i.putExtra("speed", speed);
					
		    	// comprobar marca de distancia
				
		    	if (distance>=nextDistance) {
		    		i.putExtra("markDistance", nextDistance);
		    		nextDistance += divideDistance;
		    		if (typeMarksOnMap == MapLocationsManager.TYPE_MARKS_DISTANCE) {
			    		if (prf_vibrate_marks) {
			    			((Vibrator) getSystemService(Context.VIBRATOR_SERVICE)).vibrate(MILLISECONDS_VIBRATE); 
			    		}
			    		if (prf_notification_marks) {
			    			Ringtone rt = RingtoneManager.getRingtone(this, RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_NOTIFICATION));
			    			if (!rt.isPlaying()) rt.play();
			    		}
		    		}
		    	}
		    	
		    	// comprobar marca de tiempo
		    	
		    	if (time>=nextTime) {
		    		i.putExtra("markTime", nextTime);
		    		nextTime += divideTime;
		    		if (typeMarksOnMap == MapLocationsManager.TYPE_MARKS_TIME) {
			    		if (prf_vibrate_marks) {
			    			((Vibrator) getSystemService(Context.VIBRATOR_SERVICE)).vibrate(MILLISECONDS_VIBRATE); 
			    		}
			    		if (prf_notification_marks) {
			    			Ringtone rt = RingtoneManager.getRingtone(this, RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_NOTIFICATION));
			    			if (!rt.isPlaying()) rt.play();
			    		}
		    		}
		    	}
				
				lastLocationOnDataBase = loc;
				
				position++;
		    	
	    	} else {
	    		
	    		i.putExtra("showRangeAccuracy", true);
	    		
	    	}
	    	//if (route!=null) {
	    	//	route.createNewLocation(loc, distance, speed, markDistance, markTime);
	    	//}
			
			this.sendOrderedBroadcast(i, null);
    	
    	}
    	
    }
    
	public static void pauseTrack() {
		pause = true;
	}
	
	public static void continueTrack() {
		pause = false;
		todoPauseInEntity = true;
	}

	public static boolean isPause() {
		return pause;
	}
}
