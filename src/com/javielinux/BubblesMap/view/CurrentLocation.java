package com.javielinux.BubblesMap.view;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.location.Location;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.javielinux.andando.R;

public class CurrentLocation {
	public static final int TYPE_BLUE = 0;
	public static final int TYPE_GREEN = 1;
	public static final int TYPE_RED = 2;
	
	private Bitmap drawBlue, drawGreen, drawRed;
	private MapLocationViewer mapLocationView;
	private Location location = null;
	
	private int type = TYPE_BLUE;
	
	private boolean visible = false;
	
	
	public CurrentLocation(MapLocationViewer mapView) {
		mapLocationView = mapView;
		drawBlue = BitmapFactory.decodeResource(mapLocationView.getResources(),R.drawable.current_position_blue);
		drawGreen = BitmapFactory.decodeResource(mapLocationView.getResources(),R.drawable.current_position_green);
		drawRed = BitmapFactory.decodeResource(mapLocationView.getResources(),R.drawable.current_position_red);
	}


	public void setType(int type) {
		this.type = type;
	}


	public int getType() {
		return type;
	}
	
	public void draw(Canvas canvas, MapView mapView) {
		if (visible && location!=null) {
			Point p = new Point();
			mapView.getProjection().toPixels(this.getGeoPoint(), p);
			
			if (type == TYPE_BLUE) {
				canvas.drawBitmap(drawBlue, p.x -drawBlue.getWidth()/2, p.y -drawBlue.getHeight()/2, null);
			} else if (type == TYPE_GREEN) {
				canvas.drawBitmap(drawGreen, p.x -drawGreen.getWidth()/2, p.y -drawGreen.getHeight()/2, null);
			} else if (type == TYPE_RED) {
				canvas.drawBitmap(drawRed, p.x -drawRed.getWidth()/2, p.y -drawRed.getHeight()/2, null);
			}
		}
	}


	public void setLocation(Location location) {
		this.location = location;
	}


	public Location getLocation() {
		return location;
	}
	
	public GeoPoint getGeoPoint() {
		return new GeoPoint((int)(location.getLatitude()*1E6), (int)(location.getLongitude()*1E6));
	}


	public void show() {
		this.visible = true;
	}

	public void hide() {
		this.visible = false;
	}

	public boolean isVisible() {
		return visible;
	}
	
}
