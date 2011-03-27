package com.javielinux.BubblesMap.view;

import android.content.Context;
import android.view.MotionEvent;

import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.javielinux.andando.CreateRoute;
import com.javielinux.andando.ShowRoute;
import com.javielinux.andando.Utils;

public class MapLocationViewer extends MapView {
	
	private int typeActivity;
	
	private BubblesOverlay overlay;
	private MapActivity mMapActivity;
	
    /**
     * Devuelve el MapLocationsManager
     * @return MapLocationsManager
     */
	
    public MapLocationsManager getManager() {
    	return overlay.getManager();
    }
    
    /**
     * Constructor - Mapa que contiene las rutas
     * 
     * @param context Context
     * @param apiKey Clave de la API
     */
    
    public MapLocationViewer(Context context, int type, String apiKey) {
		super(context, apiKey);
		mMapActivity = (MapActivity) context;
		typeActivity = type;
		overlay = new BubblesOverlay(this);
    	getOverlays().add(overlay);
    	
    	setClickable(true);
	}
		
	
    /**
     * Refresca el mapa
     */
	
	public void refresh() {
		this.invalidate();
	}
	
    /**
     * Devuelve el MapActivity
     * 
     * @return MapActivity
     * 
     */
	
	public MapActivity getMapActivity() {
		return mMapActivity;
	}
	
    /**
     * Devuelve el tipo de actividad
     * 
     * @return Tipo de Actividad
     * 
     */

	public int getTypeActivity() {
		return typeActivity;
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if (ev.getAction() == MotionEvent.ACTION_DOWN) {
			if (getTypeActivity() == Utils.ACTIVITY_SHOW) {
				((ShowRoute)mMapActivity).showButtons();
			}
			if (getTypeActivity() == Utils.ACTIVITY_CREATE) {
				((CreateRoute)mMapActivity).showButtons();
			}
		}
		return super.onTouchEvent(ev);
	}
	
}