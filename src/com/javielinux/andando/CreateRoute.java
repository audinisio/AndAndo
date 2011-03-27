package com.javielinux.andando;

import java.io.File;
import java.io.IOException;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.dataframework.DataFramework;
import com.android.dataframework.Entity;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.javielinux.BubblesMap.view.ButtonsPanel;
import com.javielinux.BubblesMap.view.MapLocationViewer;

public class CreateRoute extends MapActivity {
	
	private static final int DIALOG_GEO_TEXT = 0;
	private static final int DIALOG_GPS_NOFOUND = 1;
	private static final int DIALOG_BACK_SURE = 2;
	private static final int DIALOG_SELECT_IMAGE = 3;
	
    private static final int SATELLITE_ID = Menu.FIRST;
    private static final int PHOTO_ID = Menu.FIRST + 1;
    private static final int TEXT_ID = Menu.FIRST + 2;
    private static final int EDIT_ID = Menu.FIRST + 3;
    private static final int SOUND_ID = Menu.FIRST + 4;
    private static final int LOCK_ID = Menu.FIRST + 5;
    
    private static final int ACTIVITY_EDITROUTE = 0;
    private static final int ACTIVITY_SELECTIMAGE = 1;
    private static final int ACTIVITY_RECORD_SOUND = 2;
    private static final int ACTIVITY_CAMERA = 3;
    
    private static final int STATE_WAIT = 0;
    private static final int STATE_RUNNING = 1;
    private static final int STATE_FINISH = 2;
    
    private String fileNamePhoto;
    private boolean savePhoto = false;
    
    private boolean lockButtons = false;
    
    private NotificationManager mNotificationManager;
	
    private boolean startChronometerFoundLocation = true;
    private boolean autoLock = false;
    
    private boolean isTraffic = true;
    private Menu menu = null;
    
    private int btStatus = STATE_WAIT;
    private boolean running = false; 
    private long timeOnPause = 0;
    
	private MapLocationViewer mMapView;
    private MapController mMapController;
    public Chronometer mChronometer;
    public TextView mDistance;
    public TextView mDistanceUnit;
    public TextView mSpeed;
    public TextView mSpeedUnit;
    
    private float distance = 0;
    private float speed = 0;
        
	private ButtonsPanel layoutButtonsOverlay;
		
	private ThreadTimeButtons mManagerTimeButtons;
	private Thread threadTimeButtons;
	
	private ImageView btPlay, btEnd;
	private long idRoute;
	
	private boolean todoMarkContinue = false;
	
	EditText editGeoText;
	
	
	private BroadcastReceiver receiver = new BroadcastReceiver() {
        
        @Override
        public void onReceive(Context context, Intent intent) {
        	Bundle extras = intent.getExtras();
        	if (extras!=null) {
        		if (extras.containsKey("location") && extras.containsKey("showRangeAccuracy")) {
        			Location loc = (Location)extras.get("location");
        			if (extras.getBoolean("showRangeAccuracy")) {
    					showAccuracyOnMap(loc);
    				} else {
	        			float d = extras.getFloat("distance");
	        			float s = extras.getFloat("speed");
	        			boolean showRangeAccuracy = extras.getBoolean("showRangeAccuracy");
	        			int markDistance = -1;
	        			if (extras.containsKey("markDistance")) markDistance = extras.getInt("markDistance");
	        			int markTime = -1;
	        			if (extras.containsKey("markTime")) markTime = extras.getInt("markTime");
	        			createNewLocation(loc, d, s, markDistance, markTime, showRangeAccuracy);
    				}
        		}
        	}
        }
    };
	
    /**
     * Crea una nueva ventana de dialogo
     * 
     * @param id Identificador
     * @return Dialog dialogo
     */
	
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
        case DIALOG_BACK_SURE:
            return new AlertDialog.Builder(this)
                .setIcon(R.drawable.alert_dialog_icon)
                .setTitle(R.string.attention)
                .setMessage(R.string.back_sure)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    	stop();
                    	back();
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                })
                .create();
        case DIALOG_GPS_NOFOUND:
            return new AlertDialog.Builder(this)
                .setIcon(R.drawable.alert_dialog_icon)
                .setTitle(R.string.error_gps)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    	Intent settingsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        				settingsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        				startActivity(settingsIntent);
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                })
                .create();
        case DIALOG_GEO_TEXT:
        	editGeoText = new EditText(this);
            editGeoText.setPadding(10, 10, 10, 10);
            return new AlertDialog.Builder(this)
                .setIcon(R.drawable.alert_dialog_icon)
                .setTitle(R.string.alert_dialog_geo_text)
                .setView(editGeoText)
                .setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    	createResource_Text(editGeoText.getText().toString());
                    }
                })
                .setNegativeButton(R.string.alert_dialog_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                })
                .create();
        case DIALOG_SELECT_IMAGE:        	
            return new AlertDialog.Builder(CreateRoute.this)
                .setTitle(R.string.select_action)
                .setItems(R.array.select_type_image, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (which==0) {
                        	fileNamePhoto = SystemClock.elapsedRealtime() + ".jpg";
                        	File f = new File(Utils.appDirectory+fileNamePhoto);
        	    			if( f.exists() ) f.delete();
                        	
                        	Intent intendCapture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        	intendCapture.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
                        	intendCapture.putExtra("return-data", true);
                        	startActivityForResult(intendCapture, ACTIVITY_CAMERA);
                        } else if (which==1) {
                        	Utils.showMessage(CreateRoute.this.getString(R.string.take_photo));
                        	Intent i = new Intent(Intent.ACTION_PICK) ;
                        	i.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, 
                        					 MediaStore.Images.Media.CONTENT_TYPE);
                        	startActivityForResult(i, ACTIVITY_SELECTIMAGE);
                        }
                    }
                })
                .create();
        }
        return null;
    }
	
    /**
     * Primer metodo que se ejecuta al crear una Actividad
     * 
     * @param savedInstanceState
     */
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
        Utils.setActivity(this);
        
        try {
        	DataFramework.getInstance().open(this, Utils.getPackage());
		} catch (Exception e) {
			e.printStackTrace();
		}
						                
		setContentView(R.layout.route);
				
		int typeMarksOnMap = -1;
		
		if (savedInstanceState != null) {
			if (savedInstanceState.containsKey(DataFramework.KEY_ID)) idRoute = savedInstanceState.getLong(DataFramework.KEY_ID);
			if (savedInstanceState.containsKey("btStatus")) btStatus = savedInstanceState.getInt("btStatus");
			if (savedInstanceState.containsKey("running")) running = savedInstanceState.getBoolean("running");
			if (savedInstanceState.containsKey("timeOnPause")) timeOnPause = savedInstanceState.getLong("timeOnPause");
			if (savedInstanceState.containsKey("typeMarksOnMap")) typeMarksOnMap = savedInstanceState.getInt("typeMarksOnMap");
			if (savedInstanceState.containsKey("fileNamePhoto")) fileNamePhoto = savedInstanceState.getString("fileNamePhoto");
		} else {
			Bundle extras = getIntent().getExtras();  
			if (extras != null) {
				idRoute = (extras.containsKey(DataFramework.KEY_ID)) ? extras.getLong(DataFramework.KEY_ID) : -1;
				btStatus = (extras.containsKey("btStatus")) ? extras.getInt("btStatus") : STATE_WAIT;
				running = (extras.containsKey("running")) ? extras.getBoolean("running") : false;
				timeOnPause = (extras.containsKey("timeOnPause")) ? extras.getLong("timeOnPause") : 0;
				typeMarksOnMap = (extras.containsKey("typeMarksOnMap")) ? extras.getInt("typeMarksOnMap") : -1;
				fileNamePhoto = (extras.containsKey("fileNamePhoto")) ? extras.getString("fileNamePhoto") : "";
			} else {
				idRoute = -1;
				btStatus = STATE_WAIT;
				running = false;
				timeOnPause = 0;
				typeMarksOnMap = -1;
				fileNamePhoto = "";
			}
		}
		
		mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		
		if (Utils.preference!=null) {
			startChronometerFoundLocation = Utils.preference.getBoolean("prf_start", true);
			autoLock = Utils.preference.getBoolean("prf_autolock", false);
		}
		
		Entity route = new Entity("routes", idRoute);
		
		this.setTitle(getString(R.string.route) + ": " + route.getValue("name").toString());
		
		ServiceGPS.divideDistance = route.getInt("divide_distance");
		ServiceGPS.nextDistance = ServiceGPS.divideDistance;
		ServiceGPS.divideTime = route.getInt("divide_time");
		ServiceGPS.nextTime = ServiceGPS.divideTime;
			
		mChronometer = (Chronometer) findViewById(R.id.chronometer);
		
		mDistance = (TextView) findViewById(R.id.distanceroute);
		mSpeed = (TextView) findViewById(R.id.speedroute);
		
		mDistanceUnit = (TextView) findViewById(R.id.distanceroute_unit);
		mDistanceUnit.setText(Utils.formatDistanceUnit(0));
		
		mSpeedUnit = (TextView) findViewById(R.id.speedroute_unit);
		mSpeedUnit.setText(Utils.formatSpeedUnit());
		
		if (running) {
			mChronometer.setBase(timeOnPause);
			mChronometer.start();
		} 
		
		btPlay = (ImageView) findViewById(R.id.bt_play);
        btPlay.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (btStatus == STATE_WAIT) {
			        if (!Utils.getGPSStatus(CreateRoute.this)) {
			        	showDialog(DIALOG_GPS_NOFOUND);
			        } else {
			        	btPlay.setImageResource(R.drawable.bt_pause);
			        	btEnd.setImageResource(R.drawable.bt_stop);
			        	Utils.showMessage(CreateRoute.this.getString(R.string.route_start));
			        	start();
			        }
				} else if (btStatus == STATE_RUNNING) {
					if (!lockButtons) {
						if (mMapView.getManager().getEndGeoPoint()!=null) {
							if (ServiceGPS.isPause()) {
								btPlay.setImageResource(R.drawable.bt_pause);
								todoMarkContinue = true;
								Utils.showMessage(CreateRoute.this.getString(R.string.route_continue));
								ServiceGPS.continueTrack();
							} else {
								btPlay.setImageResource(R.drawable.bt_play);
								mMapView.getManager().addMarkPause(Utils.Geopoint2Location(mMapView.getManager().getEndGeoPoint()));
								Utils.showMessage(CreateRoute.this.getString(R.string.route_pause));
								ServiceGPS.pauseTrack();
							}
						}
					}
				}
			}
        	
        });
        
		btEnd = (ImageView) findViewById(R.id.bt_end);
        btEnd.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (btStatus == STATE_WAIT) {
					back();
				} else if (btStatus == STATE_RUNNING) {
					if (!lockButtons) {
						btEnd.setImageResource(R.drawable.bt_back);
						Utils.showMessage(CreateRoute.this.getString(R.string.route_stop));
						stop();
					}
				} else if (btStatus == STATE_FINISH) {
					back();
				}
			}
        	
        });
        
        LinearLayout layoutMapContainer = (LinearLayout) this.findViewById(R.id.map_container);
        
		mMapView = new MapLocationViewer(this, Utils.ACTIVITY_CREATE, Utils.KEY_GOOGLE_MAPS);
		// Layout Zoom
		mMapView.setBuiltInZoomControls(true);
		
		layoutMapContainer.addView(mMapView);
				
		mMapController = mMapView.getController();
		
		Location loc = Utils.getLastLocation(this);
		
		if (loc!=null) mMapController.setCenter(Utils.Location2Geopoint(loc));
		
		if (savedInstanceState == null) {
			mMapView.setTraffic(true);
	    	mMapView.setSatellite(false);
			mMapController.setZoom(15);
		}
		        
        // Layout iconos
		
		FrameLayout frameLayoutContainer = (FrameLayout) this.findViewById(R.id.frame_layout);
        
        layoutButtonsOverlay = new ButtonsPanel(this, Utils.ACTIVITY_CREATE);
        
        frameLayoutContainer.addView(layoutButtonsOverlay);
        
		mManagerTimeButtons = new ThreadTimeButtons(Utils.ACTIVITY_CREATE);
		threadTimeButtons = new Thread(mManagerTimeButtons);
		threadTimeButtons.start();
        
        mManagerTimeButtons.setMMapActivity(this);
             
        /*
        mMapView.getManager().createRoute(idRoute, (btStatus == BT_BACK));
        
        distance = mMapView.getManager().getDistance();
        speed = mMapView.getManager().getSpeed();
        
        mDistance.setText(Utils.formatDistance(distance));
		mSpeed.setText(Utils.formatSpeed(speed));*/
		
		if (typeMarksOnMap>=0) {
	   		for (int i=0; i<typeMarksOnMap; i++) {
	   			next();
	   		}
		}
        
    }
    
        
    /**
     * Boton siguiente
     * 
     */
    
    public void next() {
   		mMapView.getManager().nextLayerMarks();
   		mManagerTimeButtons.startTime();
   		layoutButtonsOverlay.setImageButtons(ShowRoute.TYPE_BUTTONS_OUT_MARKS, mMapView.getManager().getTypeMarksOnMap());
   		ServiceGPS.typeMarksOnMap = mMapView.getManager().getTypeMarksOnMap();
	}
    
    /**
     * Boton anterior
     * 
     */
    
    public void previous() {
   		mMapView.getManager().previousLayerMarks();
   		mManagerTimeButtons.startTime();
   		layoutButtonsOverlay.setImageButtons(ShowRoute.TYPE_BUTTONS_OUT_MARKS, mMapView.getManager().getTypeMarksOnMap());
   		ServiceGPS.typeMarksOnMap = mMapView.getManager().getTypeMarksOnMap();
	}
    
    /**
     * Activa o desactiva los botones del menu segun el estado de la ruta
     * 
     */
    
    public void enableButtons() {
    	if (menu!=null) {
			if (btStatus == STATE_WAIT) {
				menu.findItem(LOCK_ID).setEnabled(false);
				menu.findItem(PHOTO_ID).setEnabled(false);
		    	menu.findItem(TEXT_ID).setEnabled(false);
			} else if (btStatus == STATE_RUNNING) {
				menu.findItem(LOCK_ID).setEnabled(true);
				menu.findItem(PHOTO_ID).setEnabled(true);
		    	menu.findItem(TEXT_ID).setEnabled(true);
			} else if (btStatus == STATE_FINISH) {
				menu.findItem(LOCK_ID).setEnabled(false);
		    	menu.findItem(PHOTO_ID).setEnabled(false);
		    	menu.findItem(TEXT_ID).setEnabled(false);
			}
    	}
    }
        
    /**
     * Crear nueva localizacion
     * 
     * @param loc Location
     * 
     */
    
    public void createNewLocation(Location loc, float distance, float speed, int markDistance, int markTime, boolean showRangeAccuracy) {
    	    	
    	if ( (mMapView.getManager().getNLocations()<=0) && (startChronometerFoundLocation)) {
    		mChronometer.setBase(SystemClock.elapsedRealtime());
    		mChronometer.start();
    		ServiceGPS.startTime();
    	}
    	    	
    	if (markDistance>0) mMapView.getManager().addMarkDistance(loc, Utils.formatDistance(markDistance));
    	
    	if (markTime>0) mMapView.getManager().addMarkTime(loc, Utils.formatShortTime(markTime));
					
    	this.distance= distance;
    	this.speed= speed;
		
		mDistance.setText(Utils.formatDistanceNumber(distance));
		mDistanceUnit.setText(Utils.formatDistanceUnit(distance));
		mSpeed.setText(Utils.formatSpeedNumber(speed));
    	
    	mMapView.getManager().addLocationOnPathRoute(loc, 0, -1);
    	
    	mMapView.getManager().setShowRangeAccuracy(showRangeAccuracy, (int)loc.getAccuracy());
    	
    	if (todoMarkContinue) {
    		mMapView.getManager().addMarkContinue(loc);
    		todoMarkContinue = false;
    	}
    	
    	mMapView.refresh();
    }
    
    public void showAccuracyOnMap(Location loc) {
    	mMapView.getManager().setShowRangeAccuracy(true, (int)loc.getAccuracy());
    	mMapView.refresh();
    }
    
    /**
     * Muestra los botones
     * 
     */
    
    public void showButtons() {
    	if (layoutButtonsOverlay.getVisibility() == View.GONE) {
    		mManagerTimeButtons.startTime();
    		layoutButtonsOverlay.showButtons();
    	}
	}
    
    /**
     * Oculta los botones
     * 
     */
    
    public void hideButtons() {
    	if (layoutButtonsOverlay.getVisibility() == View.VISIBLE) {
    		mManagerTimeButtons.stopTime();
    		layoutButtonsOverlay.hideButtons();
    	}
	}
    
    private void startService() {
    	Intent svc = new Intent(this, ServiceGPS.class);
    	svc.putExtra(DataFramework.KEY_ID, idRoute);
        startService(svc);
    }
    
    private void stopService() {
    	Intent svc = new Intent(this, ServiceGPS.class);
		stopService(svc);
    }
            
    /**
     * Comienza a buscar las localizaciones del GPS para crear la ruta
     * 
     */
    
    private void start() {
    	running = true; 
    	btStatus = STATE_RUNNING;
    	enableButtons();
    	setMood(R.drawable.icon, R.string.notificacion_msg, false);
    	
    	if (autoLock) {
    		swapLockButtons();
    	}
    	
    	if (!startChronometerFoundLocation) {
    		mChronometer.setBase(SystemClock.elapsedRealtime());
			mChronometer.start();
			ServiceGPS.startTime();
    	}
    	startService();
    }
    
    /**
     * Para de buscar las localizaciones del GPS
     * 
     */
    
    private void stop() {
    	running = false; 
    	btStatus = STATE_FINISH;
    	enableButtons();
    	
    	mNotificationManager.cancel(R.layout.route);
    	
    	Entity route = new Entity("routes", idRoute);
    	route.setValue("distance", distance);
    	int elapsedSeconds = (int)((SystemClock.elapsedRealtime() - mChronometer.getBase())/1000);   
    	route.setValue("time", elapsedSeconds);
    	
    	Cursor c = DataFramework.getInstance().getCursor("locations", "route_id = " + idRoute, "position asc");
    	if (c.getCount()>0) {
	    	c.moveToFirst();
	    	float max_speed = 0;
	    	while (!c.isAfterLast()) {
	    		float speed = c.getFloat(c.getColumnIndex("speed"));
	    		if (speed > max_speed) max_speed = speed;
	    		c.moveToNext();
	    	}
	    	float kms = distance/1000;
	    	float hours = ((float)elapsedSeconds/60)/60;
	    	route.setValue("average_speed", Utils.round(kms/hours, 2));
	    	route.setValue("max_speed", max_speed);
	    	route.setValue("coordinates_route", c.getCount());
    	}
    	c.close();
    	
    	route.save();
    	
    	mChronometer.stop();
    	mMapView.getManager().endRoute();
    	stopService();
    }
    
    /**
     * Crea el menu
     * 
     * @param menu Menu
     * @return Boleano
     * 
     */
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        
        this.menu = menu;
        
        menu.add(0, SATELLITE_ID, 0, R.string.menu_satellite)
    		.setIcon(android.R.drawable.ic_menu_mapmode);
        menu.add(0, LOCK_ID, 0, (lockButtons)?R.string.unlock:R.string.lock)
			.setIcon(android.R.drawable.ic_lock_lock)
			.setEnabled(false);
        menu.add(0, EDIT_ID, 0, R.string.menu_edit)
			.setIcon(android.R.drawable.ic_menu_edit);
        menu.add(0, PHOTO_ID, 0, R.string.menu_photo)
			.setIcon(android.R.drawable.ic_menu_camera)
			.setEnabled(false);
        menu.add(0, TEXT_ID, 0, R.string.menu_text)
			.setIcon(android.R.drawable.ic_menu_sort_alphabetically)
			.setEnabled(false);
        menu.add(0, SOUND_ID, 0, R.string.menu_sound)
			.setIcon(android.R.drawable.ic_lock_silent_mode_off);
        
        enableButtons();
        return true;
    }
    
    /**
     * Se ejecuta al pulsar un boton del menu
     * 
     * @param featureId
     * @param item boton pulsado del menu
     * @return Si se ha pulsado una opcion
     * 
     */
    
    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch(item.getItemId()) {
        case SATELLITE_ID:
        	if (isTraffic) {
        		isTraffic = false;
        		item.setTitle(R.string.menu_traffic);
            	mMapView.setTraffic(false);
            	mMapView.setSatellite(true);
        	} else {
        		isTraffic = true;
        		item.setTitle(R.string.menu_satellite);
        		mMapView.setTraffic(true);
            	mMapView.setSatellite(false);
        	}
            return true;
        case PHOTO_ID:
        	showDialog(DIALOG_SELECT_IMAGE);
        	return true;
        case LOCK_ID:
        	swapLockButtons();
        	return true;
        case TEXT_ID:
        	showDialog(DIALOG_GEO_TEXT);
        	return true;
        case EDIT_ID:
            Intent intendRoute = new Intent(this, RouteEdit.class);
            intendRoute.putExtra(DataFramework.KEY_ID, idRoute);
            startActivityForResult(intendRoute, ACTIVITY_EDITROUTE);
        	return true;
        case SOUND_ID:
        	Intent intent = new Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION);
        	startActivityForResult(intent, ACTIVITY_RECORD_SOUND);
        	return true;
        }
       
        return super.onMenuItemSelected(featureId, item);
    }
    
    /**
     * Crea un nuevo recurso de texto en la ruta
     * 
     * @param text Texto
     */
    
	public void createResource_Text(String text) {
		if (mMapView.getManager().getLastLocation()!=null) {
			Location loc = mMapView.getManager().getLastLocation();
			Entity e = new Entity("resource");
	    	e.setValue("text", text);
	    	e.setValue("type_resource_id", 1);
			e.setValue("latitude", loc.getLatitude());
			e.setValue("longitude", loc.getLongitude());
			e.setValue("altitude", loc.getAltitude());
	    	e.setValue("route_id", idRoute);
	    	e.save();
	    	mMapView.getManager().addResource_Text(loc, text);
	    	mMapView.refresh();
	    	
	    	Utils.showMessage(Utils.context.getResources().getString(R.string.text_recorded_successfully));
	    	
		} else {
			Utils.showMessage(Utils.context.getResources().getString(R.string.no_location));
		}
	}
	
    /**
     * Crea un nuevo recurso imagen en la ruta
     * 
     * @param text Texto
     */
    
	public void createResource_Image(String fileName) {

		if (mMapView.getManager().getLastLocation()!=null) {
			Location loc = mMapView.getManager().getLastLocation();
			Entity ent = new Entity("resource");
			ent.setValue("type_resource_id", 2);
			ent.setValue("latitude", loc.getLatitude());
			ent.setValue("longitude", loc.getLongitude());
			ent.setValue("altitude", loc.getAltitude());
			ent.setValue("route_id", idRoute);
			ent.setValue("file", fileName);
			ent.save();
						
	    	mMapView.getManager().addResource_Image(loc, fileName);
	    	mMapView.refresh();
	    	
	    	Utils.showMessage(Utils.context.getResources().getString(R.string.image_recorded_successfully));
	    	
		} else {
			Utils.showMessage(Utils.context.getResources().getString(R.string.no_location));
		}
	}
	
	public void createResource_Sound(String fileName) {
		if (mMapView.getManager().getLastLocation()!=null) {
			Location loc = mMapView.getManager().getLastLocation();
			Entity ent = new Entity("resource");
			ent.setValue("type_resource_id", 3);
			ent.setValue("latitude", loc.getLatitude());
			ent.setValue("longitude", loc.getLongitude());
			ent.setValue("altitude", loc.getAltitude());
			ent.setValue("route_id", idRoute);
			ent.setValue("file", fileName);
			ent.save();
						
	    	mMapView.getManager().addResource_Sound(loc, fileName);
	    	mMapView.refresh();
	    	
	    	Utils.showMessage(Utils.context.getResources().getString(R.string.sound_recorded_successfully));
	    	
		} else {
			Utils.showMessage(Utils.context.getResources().getString(R.string.no_location));
		}
	}
	        
	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}
	
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        
        if (running) timeOnPause = mChronometer.getBase(); 
        
        outState.putLong(DataFramework.KEY_ID, idRoute);
        outState.putInt("btStatus", btStatus);
        outState.putBoolean("running", running);
        outState.putLong("timeOnPause", timeOnPause);
        outState.putInt("typeMarksOnMap", mMapView.getManager().getTypeMarksOnMap());
        outState.putString("fileNamePhoto", fileNamePhoto);
        
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        this.unregisterReceiver(receiver);
    }
    
    @Override
    protected void onResume() {
        super.onResume();        
        mMapView.getManager().createRoute(idRoute, (btStatus == STATE_FINISH));
        distance = mMapView.getManager().getDistance();
        speed = mMapView.getManager().getSpeed();
        mDistance.setText(Utils.formatDistanceNumber(distance));
        mDistanceUnit.setText(Utils.formatDistanceUnit(distance));
		mSpeed.setText(Utils.formatSpeedNumber(speed));
		
		this.registerReceiver(receiver, new IntentFilter(Intent.ACTION_VIEW));
		
		if (savePhoto) {
			File f = new File(Utils.appDirectory+fileNamePhoto);
			if( f.exists() ) createResource_Image(fileNamePhoto);
			savePhoto = false;
		}
    }
    
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (btStatus == STATE_RUNNING) {
				showDialog(DIALOG_BACK_SURE);
				return false;
			} else {
				back();
			}
			back();
		}
		return super.onKeyDown(keyCode, event);
	}

    /**
     * Vuelve a la actividad principal
     * 
     */
    
    public void back() {
    	mManagerTimeButtons.stop();
    	setResult(RESULT_OK);
        finish();
    }
    
    /**
     * Llamada cuando volvemos de una Actividad
     * 
     * @param requestCode Codigo con el que se lanzo el Intend
     * @param resultCode Codigo de regreso
     * @param intent Intend
     */
	
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, 
                                    Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
               
		switch (requestCode){
    	case ACTIVITY_EDITROUTE:
    		Entity route = new Entity("routes", idRoute);
    		
    		this.setTitle(getString(R.string.route) + ": " + route.getValue("name").toString());
    		
    		if (ServiceGPS.divideDistance != route.getInt("divide_distance")) {
    			mMapView.getManager().createMarksDistanceOnRoute(idRoute);
    			
    			ServiceGPS.divideDistance = route.getInt("divide_distance");
    			
    			ServiceGPS.nextDistance = 0;
        	    while (ServiceGPS.nextDistance<distance) {
        	    	ServiceGPS.nextDistance += ServiceGPS.divideDistance;
        	    }
    			
    		}
        	if (ServiceGPS.divideTime != route.getInt("divide_time")) {
        		mMapView.getManager().createMarksTimeOnRoute(idRoute);
        		
        		ServiceGPS.divideTime = route.getInt("divide_time");
        		
        	    int time = (int)((SystemClock.elapsedRealtime() - mChronometer.getBase())/1000);
        	    
        	    ServiceGPS.nextTime = 0;
        	    while (ServiceGPS.nextTime<time) {
        	    	ServiceGPS.nextTime += ServiceGPS.divideTime;
        	    }
        		
        	}
    	    
    		break ;
    	case ACTIVITY_RECORD_SOUND:
    		if ( resultCode == -1 ) {
    			if ( intent.getData() != null ) {
        			Uri mRecordingUri = intent.getData();
            		String mRecordingFilename = null;
            		mRecordingFilename = getFilenameFromUri(mRecordingUri);
            		String fileName = SystemClock.elapsedRealtime() + ".3gp";
            		try {
            			Utils.copy(mRecordingFilename, Utils.appDirectory+fileName);
            			createResource_Sound(fileName);
            		} catch (IOException e) {
            			Toast.makeText(this, 
                	    		e.getMessage(), 
                	            Toast.LENGTH_LONG).show();   
            		}
        		}
    		}    		
    		break;
		case ACTIVITY_CAMERA:
    		if( resultCode != 0 ) {
    			savePhoto = true;
    		}
    		break ;
    	case ACTIVITY_SELECTIMAGE:
    		if( resultCode != 0 ) {
	    		Cursor c = managedQuery(intent.getData(),null,null,null,null);
	    		if( c.moveToFirst() ) {
	    			String media_path = c.getString(1);
	    			String fileName = SystemClock.elapsedRealtime() + ".jpg";
	    			try {
	    				Utils.copy(media_path, Utils.appDirectory+fileName);
	    				fileNamePhoto = fileName;
	    				savePhoto = true;
	    			} catch (Exception e) {
	    				Utils.showMessage("Error al copiar la imagen");
	    			}
	    		}
	    		c.close();
    		}
    		break;
        }
    }
    
	private String getFilenameFromUri(Uri uri) {
        Cursor c = managedQuery(uri, null, "", null, null);
        if (c.getCount() == 0) {
            return null;
        }
        c.moveToFirst();
        int dataIndex = c.getColumnIndexOrThrow(
            MediaStore.Audio.Media.DATA);

        return c.getString(dataIndex);
    }
    
    private void setMood(int moodId, int textId, boolean showTicker) {
        CharSequence text = getText(textId);

        String tickerText = showTicker ? getString(textId) : null;

        Notification notification = new Notification(moodId, tickerText,
                System.currentTimeMillis());
        
        Intent i = new Intent(this, Andando.class);
        i.putExtra("back", "yes");
        
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, i, 0);

        notification.setLatestEventInfo(this, getText(R.string.app_name),
                       text, contentIntent);

        mNotificationManager.notify(R.layout.route, notification);
    }

	@Override
	protected void onDestroy() {
		super.onDestroy();
		DataFramework.getInstance().close();
	}

	public void swapLockButtons() {
		lockButtons = !lockButtons;
		if (lockButtons) {
			if (menu!=null) menu.findItem(LOCK_ID).setTitle(R.string.unlock);
			btPlay.setImageResource(R.drawable.bt_play_lock);
			btEnd.setImageResource(R.drawable.bt_stop_lock);
		} else {
			if (menu!=null) menu.findItem(LOCK_ID).setTitle(R.string.lock);
			btEnd.setImageResource(R.drawable.bt_stop);
			if (ServiceGPS.isPause()) {
				btPlay.setImageResource(R.drawable.bt_pause);
			} else {
				btPlay.setImageResource(R.drawable.bt_play);
			}
		}
	}

	public boolean isLockButtons() {
		return lockButtons;
	}
    
}
