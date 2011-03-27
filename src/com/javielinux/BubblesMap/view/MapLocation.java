package com.javielinux.BubblesMap.view;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.RectF;
import android.location.Location;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.javielinux.andando.R;

/** Class to hold our location information */
public class MapLocation {

	public static final int TYPE_BUBBLE_START = 0;
	public static final int TYPE_BUBBLE_MSG = 1;
	public static final int TYPE_BUBBLE_PHOTO = 2;
	public static final int TYPE_BUBBLE_SOUND= 3;
	public static final int TYPE_BUBBLE_TIME = 4;
	public static final int TYPE_BUBBLE_DISTANCE = 5;
	public static final int TYPE_BUBBLE_PAUSE = 6;
	public static final int TYPE_BUBBLE_CONTINUE = 7;
	public static final int TYPE_BUBBLE_END = 8;
	
	public static final int PADDING_X = 10;
	public static final int PADDING_Y = 8;
	public static final int RADIUS_BUBBLES = 5;
	public static final int DISTANCE_BUBBLE = 15;
	public static final int SIZE_SELECTOR_BUBBLE = 10;
	
	private Location location;
	private String name;
	private String text = "";
	private String file = "";
	private MapLocationViewer mapLocationView;
	private int type = -1;
	
	private Bitmap drawIcon, shadowIcon, drawOn;

    /**
     * Constructor - Una marca en el mapa
     * 
     * @param mapView MapLocationViewer
     * @param name Nombre de la marca
     * @param loc Localizacion de la marca en el mapa
     * @param type Tipo de marca
     */
	
	public MapLocation(MapLocationViewer mapView, String name, String text, Location loc, int type) {
		this.name = name;
		this.text = text;
		mapLocationView = mapView;
		this.location = loc;
		setType(type);
		drawOn = BitmapFactory.decodeResource(mapLocationView.getResources(),R.drawable.on);
	}
	
    /**
     * Establece el tipo de la marca
     * 
     * @param type Tipo de marca
     */
	
	private void setType(int type) {
		this.type = type;	
		switch (type) {
		case TYPE_BUBBLE_START:
			drawIcon = BitmapFactory.decodeResource(mapLocationView.getResources(),R.drawable.bubble_start);
			break;
		case TYPE_BUBBLE_END:
			drawIcon = BitmapFactory.decodeResource(mapLocationView.getResources(),R.drawable.bubble_end);
			break;
		case TYPE_BUBBLE_PHOTO:
			drawIcon = BitmapFactory.decodeResource(mapLocationView.getResources(),R.drawable.bubble_photo);
			break;
		case TYPE_BUBBLE_MSG:
			drawIcon = BitmapFactory.decodeResource(mapLocationView.getResources(),R.drawable.bubble_msg);
			break;
		case TYPE_BUBBLE_TIME:
			drawIcon = BitmapFactory.decodeResource(mapLocationView.getResources(),R.drawable.bubble_time);
			break;
		case TYPE_BUBBLE_DISTANCE:
			drawIcon = BitmapFactory.decodeResource(mapLocationView.getResources(),R.drawable.bubble_distance);
			break;
		case TYPE_BUBBLE_PAUSE:
			drawIcon = BitmapFactory.decodeResource(mapLocationView.getResources(),R.drawable.bubble_pause);
			break;
		case TYPE_BUBBLE_CONTINUE:
			drawIcon = BitmapFactory.decodeResource(mapLocationView.getResources(),R.drawable.bubble_continue);
			break;
		case TYPE_BUBBLE_SOUND:
			drawIcon = BitmapFactory.decodeResource(mapLocationView.getResources(),R.drawable.bubble_sound);
			break;
		}
		shadowIcon = BitmapFactory.decodeResource(mapLocationView.getResources(),R.drawable.bubble_shadow);
	}
	
    /**
     * Devuelve el tipo de dibujo
     * 
     * @return Bitmap Dibujo
     */
	
	public Bitmap getDrawIcon() {
		return drawIcon;
	}
	
    /**
     * Devuelve la sombra del dibujo
     * 
     * @return Bitmap Sombra
     */
	
	public Bitmap getShadowIcon() {
		return shadowIcon;
	}
	
    /**
     * Devuelve el tipo
     * 
     * @return int Tipo
     */
	
	public int getType() {
		return type;
	}
	
    /**
     * Devuelve localizacion
     * 
     * @return Location Localizacion
     */
	
	public Location getLocation() {
		return location;
	}
	
    /**
     * Devuelve localizacion
     * 
     * @return GeoPoint Objeto GeoPoint
     */
	
	public GeoPoint getGeoPoint() {
		return new GeoPoint((int)(location.getLatitude()*1E6), (int)(location.getLongitude()*1E6));
	}
	
    /**
     * Devuelve el texto que va en el bocadillo
     * 
     * @return String Texto
     */
	
	public String getText() {
		return text;
	}
		
    /**
     * Devuelve el nombre
     * 
     * @return String Nombre
     */

	public String getName() {
		return name;
	}
	
    /**
     * Devuelve el ancho del dibujo
     * 
     * @return int Ancho
     */
	
	public int getWidthIcon() {
		return drawIcon.getWidth();
	}
	
    /**
     * Devuelve el alto del dibujo
     * 
     * @return int Alto
     */
	
	public int getHeightIcon() {
		return drawIcon.getHeight();
	}
		
    /**
     * Devuelve el objeto RectF asociado al icono en la posicion (0,0)
     * 
     * @return Objeto RectF
     */
	
	public RectF getHRectFIcon() {
		return getHRectFIcon(0, 0);
	}
	
    /**
     * Devuelve el objeto RectF asociado al icono en la posicion 
     * enviada por parametro
     * 
     * @param offsetx Desplazamiento en X
     * @param offsety Desplazamiento en Y
     * @return Objeto RectF
     */
	
	public RectF getHRectFIcon(int offsetx, int offsety) {
		RectF rectf = new RectF();
		rectf.set(-drawIcon.getWidth()/2,-drawIcon.getHeight(),drawIcon.getWidth()/2,0);
		rectf.offset(offsetx, offsety);
		return rectf;
	}
	
    /**
     * Devuelve si ha sido pulsado el icono en el mapa
     * 
     * @param offsetx Desplazamiento en X
     * @param offsety Desplazamiento en Y
     * @param event_x Posicion X
     * @param event_y Posicion Y
     * @return Booleando
     */
	
	public boolean getHit(int offsetx, int offsety, float event_x, float event_y) {
	    if ( getHRectFIcon(offsetx, offsety).contains(event_x,event_y) ) {
	        return true;
	    }
	    return false;
	}
	
    /**
     * Dibuja la locacalizacion en el mapa
     * 
     * @param canvas Canvas sobre el que se dibuja
     * @param mapView Mapa
     * @param shadow Si es la sombra
     */
	
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		Point p = new Point();
		mapView.getProjection().toPixels(this.getGeoPoint(), p);
		
    	if (shadow) {
    		canvas.drawBitmap(this.getShadowIcon(), p.x, p.y - this.getShadowIcon().getHeight(),null);
    	} else {
			canvas.drawBitmap(this.getDrawIcon(), p.x -this.getDrawIcon().getWidth()/2, p.y -this.getDrawIcon().getHeight(),null);
    	}
	}
	
    /**
     * Dibuja un icono para marcar la posicion
     * 
     * @param canvas Canvas sobre el que se dibuja
     * @param mapView Mapa
     * @param shadow Si es la sombra
     */
	
	public void drawOn(Canvas canvas, MapView mapView, boolean shadow) {
		if (!shadow) {
		    Point p = new Point();
		    mapView.getProjection().toPixels(this.getGeoPoint(), p);
		    
		    int wBox = drawOn.getWidth();
		    int hBox = drawOn.getHeight();
		    
		    int offsetX = p.x - wBox/2;
		    int offsetY = p.y - hBox - this.getHeightIcon();
		    
		    canvas.drawBitmap(drawOn, offsetX, offsetY,null);
		    
		}
	}
	
    /**
     * Dibuja el bocadillo con el texto en el mapa
     * 
     * @param canvas Canvas sobre el que se dibuja
     * @param mapView Mapa
     * @param shadow Si es la sombra
     */
	/*
	public void drawBubble(Canvas canvas, MapView mapView, boolean shadow) {
		if (!this.getText().equals("") && !shadow) {
		    Point p = new Point();
		    mapView.getProjection().toPixels(this.getGeoPoint(), p);
		    
		    int wBox = getWidthText()  + (PADDING_X*2);
		    int hBox = getHeightText() + (PADDING_Y*2); 
		    
		    RectF boxRect = new RectF(0, 0, wBox, hBox);
		    int offsetX = p.x - wBox/2;
		    int offsetY = p.y - hBox - this.getHeightIcon() - DISTANCE_BUBBLE;
		    boxRect.offset(offsetX, offsetY);
		    
		    Path pathBubble = new Path();
		    pathBubble.addRoundRect(boxRect, RADIUS_BUBBLES, RADIUS_BUBBLES, Direction.CCW);
		    pathBubble.moveTo(offsetX+(wBox/2)-(SIZE_SELECTOR_BUBBLE/2), offsetY+hBox);
		    pathBubble.lineTo(offsetX+(wBox/2), offsetY+hBox+SIZE_SELECTOR_BUBBLE);
		    pathBubble.lineTo(offsetX+(wBox/2)+(SIZE_SELECTOR_BUBBLE/2), offsetY+hBox);
		    
		    canvas.drawPath(pathBubble, MapLocationsManager.borderPaint);
		    canvas.drawPath(pathBubble, MapLocationsManager.innerPaint);
		
		    canvas.drawText(this.getText(), p.x-(getWidthText()/2),
		    		p.y-MapLocationsManager.textPaint.ascent()-this.getHeightIcon()-hBox+PADDING_Y - DISTANCE_BUBBLE, MapLocationsManager.textPaint);
		}
	}
	*/
    /**
     * Devuelve el nombre del archivo
     * 
     * @return Nombre del archivo
     */

	public String getFile() {
		return file;
	}

    /**
     * Establece el nombre del archivo
     * 
     * @param image Nombre del archivo
     */
	
	public void setFile(String file) {
		this.file = file;
	}
	
}
