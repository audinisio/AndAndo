package com.javielinux.andando;

import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;

import com.google.android.maps.MapActivity;


public class ThreadTimeButtons implements Runnable {
	
	private boolean running;
	private boolean todo;
	private long elapsedRealtime;
	
	private MapActivity mMapActivity;
	private int typeActivity;
	
    /**
     * Constructor - Este Thread controla el tiempo que aparecen
     * los botones en pantalla
     * 
     * @param showRoute Actividad
     */
	
	public ThreadTimeButtons(int type) {
		typeActivity = type;
		running = true;
		todo = false;
	}
	
    /**
     * Metodo principal del Thread
     * 
     */
	
	@Override
	public void run() {
        while (running) {
        	if (todo) {
	        	try {
	        		Thread.sleep(1000);
		        	int seconds = (int)((SystemClock.elapsedRealtime() - elapsedRealtime)/1000);
		        	if (seconds>=4) {
		        		handler.sendEmptyMessage(0);
		        	}
	        	} catch (Exception e) {
	        		
	        	}
        	}
        }
	}
	
	
    /**
     * Termina el thread
     * 
     */
	
	public void stop() {
		running = false;
	}
		
	
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
    		if (typeActivity == Utils.ACTIVITY_CREATE) {
    			((CreateRoute)mMapActivity).hideButtons();  			
    		}
    		if (typeActivity == Utils.ACTIVITY_SHOW) {
    			((ShowRoute)mMapActivity).hideButtons();  			
    		}
		}
	};
	
    /**
     * Comienza el tiempo
     * 
     */

	public void startTime() {
		todo = true;
		elapsedRealtime = SystemClock.elapsedRealtime();
	}
	
    /**
     * Termina el tiempo
     * 
     */
	
	public void stopTime() {
		todo = false;
	}
	
    /**
     * Establece el MapActivity
     * 
     * @param mapActivity MapActivity
     * 
     */
	
	public void setMMapActivity(MapActivity mapActivity) {
		mMapActivity = mapActivity;
	}

}
