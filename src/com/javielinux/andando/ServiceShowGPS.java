package com.javielinux.andando;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;

import com.android.dataframework.DataFramework;

public class ServiceShowGPS extends Service {
		
	private LocationManager mLocationManager;
	private MyLocationListener mLocationListener;
    
    
    private boolean running = false;
    
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();

		mLocationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
	}
	
	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
			
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
			long timeRegister = 1000;
			float distanceRegister = 5;
						
			mLocationListener = new MyLocationListener();
			mLocationManager.removeUpdates(mLocationListener);
			
			mLocationManager.requestLocationUpdates(
	                LocationManager.GPS_PROVIDER, timeRegister, distanceRegister, mLocationListener);
		}
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
            		sendLocation(loc);
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
    
    public void sendLocation(Location loc) {
    	Intent i = new Intent(Intent.ACTION_VIEW);
		i.putExtra("location", loc);
		this.sendOrderedBroadcast(i, null);
    }
    	

}
