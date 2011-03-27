package com.javielinux.BubblesMap.view;

import android.graphics.Canvas;
import android.view.MotionEvent;

import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

public class BubblesOverlay  extends Overlay {
    
    private MapLocationsManager mManager;  
    MapLocationViewer mMapView;
    
    public MapLocationsManager getManager() {
    	return mManager;
    }
    
    /**
     * Constructor - Crea el overlay donde se dibuja la ruta
     * 
     * @param mlv MapLocationViewer
     */
    
	public BubblesOverlay(MapLocationViewer mlv) {
	
		mManager = new MapLocationsManager(mlv);
		mMapView = mlv;
		
	}
	
    /**
     * Controla los eventos al tocar la pantalla
     * 
     * @param event Tipo de evento
     * @param mapView Mapa
     * @return Boleano
     */
	
	@Override
	public boolean onTouchEvent(MotionEvent event, MapView mapView) {		
		return mManager.verifyHitMapLocation(mapView, event);
	}
	
    /**
     * Dibuja la ruta en el mapa
     * 
     * @param canvas Canvas sobre el que se dibuja
     * @param mapView Mapa
     * @param shadow Si es la sombra
     */
	
    public void draw (Canvas canvas, MapView mapView, boolean shadow) {		
    	mManager.draw(canvas, mapView, shadow);
    }


    
}