package com.javielinux.andando;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.location.Location;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.dataframework.DataFramework;
import com.android.dataframework.Entity;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.javielinux.BubblesMap.view.ButtonsPanel;
import com.javielinux.BubblesMap.view.MapLocation;
import com.javielinux.BubblesMap.view.MapLocationViewer;

public class ShowRoute extends MapActivity {
		
	private static final int DIALOG_GPS_NOFOUND = 0;
	
	public static final int TYPE_BUTTONS_OUT_MARKS = 0;
	public static final int TYPE_BUTTONS_IN_MARKS = 1;
	
	private static final int HEIGHT_IMAGE_INFORMATION_SMALL = 80;
	
    private static final int SATELLITE_ID = Menu.FIRST + 1;
    private static final int VIEWINFO_ID = Menu.FIRST + 2;
    private static final int MYLOCATION_ID = Menu.FIRST + 3;
    private static final int GOTOSTART_ID = Menu.FIRST + 4;
    private static final int EDIT_ID = Menu.FIRST + 5;
    
    private static final int ACTIVITY_EDITROUTE = 0;
    
    private boolean isTraffic = true;
    
    private boolean editRoute = false;
    
	private MapLocationViewer mMapView;
    private MapController mMapController;
    		
	private ThreadTimeButtons mManagerTimeButtons = null;
	private Thread threadTimeButtons;
				
	public long idRoute;
	public boolean isSimplify = false;
			
	private ButtonsPanel layoutButtonsOverlay;
	
	public int typeButtons = TYPE_BUTTONS_OUT_MARKS;
	
	private int lastDivideDistance;
	private int lastDivideTime;
	
	private LinearLayout llInfo;
	private TextView tvInfo;
	
	private LinearLayout llPhoto;
	private ImageView largePhoto;
		
	private ImageView btIconInfo;
	private ImageView photo;
	private ImageButton button;
	private int idCategory;
	private boolean showPhoto;
	private boolean showButton;
	
	private boolean isRunningService = false;
	
	private BroadcastReceiver receiver = new BroadcastReceiver() {
        
        @Override
        public void onReceive(Context context, Intent intent) {
        	Bundle extras = intent.getExtras();
        	if (extras!=null) {
        		if (extras.containsKey("location")) {
        			Location loc = (Location)extras.get("location");
        			newLocation(loc);
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
		
       	setContentView(R.layout.route_show);
		
        editRoute = false;
        
		int typeMarksOnMap = -1;
		
		if (savedInstanceState != null) {
			if (savedInstanceState.containsKey(DataFramework.KEY_ID)) idRoute = savedInstanceState.getLong(DataFramework.KEY_ID);
			if (savedInstanceState.containsKey("simplify")) isSimplify = savedInstanceState.getBoolean("simplify");
			if (savedInstanceState.containsKey("editRoute")) editRoute = savedInstanceState.getBoolean("editRoute");
			if (savedInstanceState.containsKey("typeMarksOnMap")) typeMarksOnMap = savedInstanceState.getInt("typeMarksOnMap");
		} else {
			Bundle extras = getIntent().getExtras();  
			if (extras != null) {
				idRoute = (extras.containsKey(DataFramework.KEY_ID)) ? extras.getLong(DataFramework.KEY_ID) : -1;
				isSimplify = (extras.containsKey("simplify")) ? extras.getBoolean("simplify") : false;
				editRoute = (extras.containsKey("editRoute")) ? extras.getBoolean("editRoute") : false;
				typeMarksOnMap = (extras.containsKey("typeMarksOnMap")) ? extras.getInt("typeMarksOnMap") : -1;
			} else {
				idRoute = -1;
				isSimplify = false;
				editRoute = false;
				typeMarksOnMap = -1;
			}
		}
		
		llPhoto = (LinearLayout) this.findViewById(R.id.ll_photo);
		
		llPhoto.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				layoutButtonsOverlay.setVisibility(View.VISIBLE);
				llPhoto.setVisibility(View.GONE);
			}
		});
		
		largePhoto = (ImageView) this.findViewById(R.id.img_photo);
		
		largePhoto.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				layoutButtonsOverlay.setVisibility(View.VISIBLE);
				llPhoto.setVisibility(View.GONE);
			}
		});
		
		llInfo = (LinearLayout) this.findViewById(R.id.ll_info);
		tvInfo = (TextView) this.findViewById(R.id.text_info);
		btIconInfo = (ImageView) this.findViewById(R.id.bt_info);
		photo = (ImageView) this.findViewById(R.id.photo);
		button = (ImageButton) this.findViewById(R.id.button);
		
		btIconInfo.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				hideInformation();
			}
			
		});
		
		Entity route = new Entity("routes", idRoute);
		
		idCategory = this.getResources().getIdentifier("com.javielinux.andando:drawable/info_icon_cat_" + route.getInt("category_id"), null, null);
		btIconInfo.setImageResource(idCategory);
		
		String name = "";
		if (isSimplify) name += getString(R.string.title_route_simplify) + ": ";
		else name += getString(R.string.route) + ": ";
		name += route.getValue("name").toString();
		this.setTitle(name);
		
		TextView pointsRoute = (TextView) findViewById(R.id.pointsroute);
		pointsRoute.setText(route.getValue("coordinates_route").toString());
		
		TextView timeRoute = (TextView) findViewById(R.id.time);
		timeRoute.setText(Utils.formatShortTime(Long.parseLong(route.getValue("time").toString())));
		
		TextView distanceRoute = (TextView) findViewById(R.id.distanceroute);
		distanceRoute.setText(Utils.formatDistanceNumber(Float.parseFloat(route.getValue("distance").toString())));
		
		TextView distanceRouteUnit = (TextView) findViewById(R.id.distanceroute_unit);
		distanceRouteUnit.setText(Utils.formatDistanceUnit(Float.parseFloat(route.getValue("distance").toString())));
				
		LinearLayout layoutMapContainer = (LinearLayout) this.findViewById(R.id.map_container);
	        
		mMapView = new MapLocationViewer(this, Utils.ACTIVITY_SHOW, Utils.KEY_GOOGLE_MAPS);
	    // Layout Zoom
	    mMapView.setBuiltInZoomControls(true);
		
	    layoutMapContainer.addView(mMapView);
				
		mMapController = mMapView.getController();
				
		if (savedInstanceState == null) {
			mMapView.setTraffic(true);
			mMapView.setSatellite(false);
			mMapController.setZoom(15);
		}
            	
        // Layout iconos
    	
    	FrameLayout frameMapContainer = (FrameLayout) this.findViewById(R.id.frame_layout);
        
        layoutButtonsOverlay = new ButtonsPanel(this, Utils.ACTIVITY_SHOW);
        
        frameMapContainer.addView(layoutButtonsOverlay);
                        
        // crear ruta
        
        if (isSimplify) {
        	mMapView.getManager().createRouteSimplified(idRoute, true);
        } else {
        	mMapView.getManager().createRoute(idRoute, true);
        }
                
        // Threads
        
		mManagerTimeButtons = new ThreadTimeButtons(Utils.ACTIVITY_SHOW);
		threadTimeButtons = new Thread(mManagerTimeButtons);
		threadTimeButtons.start();
        
        mManagerTimeButtons.setMMapActivity(this);

        hideInformation();
        
		if (typeMarksOnMap>=0) {
	   		for (int i=0; i<typeMarksOnMap; i++) {
	   			next();
	   		}
		}
		
    }

    
    /**
     * Cambia el tipo de botones
     * 
     * @param type Tipo de botones
     * 
     */
    
    public void setTypeButtons(int type) {
    	typeButtons = type;
    	mManagerTimeButtons.stopTime();
    	
    	layoutButtonsOverlay.setImageButtons(type, mMapView.getManager().getTypeMarksOnMap());
    	
	}
    
    /**
     * Boton siguiente
     * 
     */
       
    public void next() {
    	if (typeButtons == TYPE_BUTTONS_OUT_MARKS) {
    		mMapView.getManager().nextLayerMarks();
    		mManagerTimeButtons.startTime();
    		layoutButtonsOverlay.setImageButtons(TYPE_BUTTONS_OUT_MARKS, mMapView.getManager().getTypeMarksOnMap());
    	} else {
    		mMapView.getManager().nextMark();
    	}
	}
    
    /**
     * Boton anterior
     * 
     */
    
    public void previous() {
    	if (typeButtons == TYPE_BUTTONS_OUT_MARKS) {
    		mMapView.getManager().previousLayerMarks();
    		mManagerTimeButtons.startTime();
    		layoutButtonsOverlay.setImageButtons(TYPE_BUTTONS_OUT_MARKS, mMapView.getManager().getTypeMarksOnMap());
    	} else {
    		mMapView.getManager().previousMark();
    	}
	}
    
    /**
     * Muestra reproductor
     * 
     
    
    public void showPlayer() {
    	hideButtons();
    	llPlayer.setVisibility(View.VISIBLE);
    	//mMapView.getManager().setTypeMarksOnMap(MapLocationsManager.TYPE_MARKS_NONE);
	}
    */
    /**
     * Ocultar reproductor
     * 
     
    
    public void hidePlayer() {
    	llPlayer.setVisibility(View.GONE);
    	//mMapView.getManager().setTypeMarksOnMap(MapLocationsManager.TYPE_MARKS_RESOURCES);
	}
       */     
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
    
    /**
     * Muestra la caja de informacion
     * 
     * @param type Tipo de informacion
     * @param info Informacion
     */
    
    public void showInformation(final int type, final String info) {
    	setTypeButtons(TYPE_BUTTONS_IN_MARKS);
    	
    	photo.setVisibility(View.GONE);
    	button.setVisibility(View.GONE);
    	
    	showPhoto = false;
    	showButton = false;
    	    	
		switch (type) {
		case MapLocation.TYPE_BUBBLE_START:
			btIconInfo.setImageResource(R.drawable.info_icon_start);
			tvInfo.setTextSize(20);
			tvInfo.setText(info);
			break;
		case MapLocation.TYPE_BUBBLE_END:
			btIconInfo.setImageResource(R.drawable.info_icon_end);
			tvInfo.setTextSize(20);
			tvInfo.setText(info);
			break;
		case MapLocation.TYPE_BUBBLE_PAUSE:
			btIconInfo.setImageResource(R.drawable.info_icon_pause);
			tvInfo.setTextSize(20);
			tvInfo.setText(info);
			break;
		case MapLocation.TYPE_BUBBLE_CONTINUE:
			btIconInfo.setImageResource(R.drawable.info_icon_continue);
			tvInfo.setTextSize(20);
			tvInfo.setText(info);
			break;
		case MapLocation.TYPE_BUBBLE_PHOTO:
			btIconInfo.setImageResource(R.drawable.info_icon_photo);
			tvInfo.setTextSize(14);
			File f = new File(Utils.appDirectory + info);
    		if (f.exists()) {
    			tvInfo.setText(this.getText(R.string.click_image_to_large));
    			File fThumb = new File(Utils.appDirectoryThumb + info);
    			if (!fThumb.exists()) {
    				Options opt = new Options();
    				opt.inSampleSize = 2;
    				Bitmap bm = BitmapFactory.decodeFile(Utils.appDirectory + info, opt);
    				int height = HEIGHT_IMAGE_INFORMATION_SMALL;
    				int width = (HEIGHT_IMAGE_INFORMATION_SMALL * bm.getWidth()) / bm.getHeight();
    				
    				Bitmap bm2;
    				
    				String nameNewPath = Utils.appDirectoryThumb + info;
    				
    				if (width>height) {
    					width = HEIGHT_IMAGE_INFORMATION_SMALL;
    					height = (HEIGHT_IMAGE_INFORMATION_SMALL * bm.getHeight()) / bm.getWidth();
    					bm2 = Bitmap.createScaledBitmap(bm, width, height, false);
    				} else {
    					bm2 = Bitmap.createScaledBitmap(bm, width, height, false);
    				}
    				try {
    					FileOutputStream out = new FileOutputStream(nameNewPath);
   						bm2.compress(CompressFormat.JPEG, 90, out) ;
    	    			out.close() ;
    				} catch (FileNotFoundException e) {
    					Log.e("ImageResize","FileNotFoundException generated when resized") ;
    				} catch (IOException e) {
    					Log.e("ImageResize","IOException generated when resized") ;
    				}
    			}
    			
    			Bitmap bmThumb = BitmapFactory.decodeFile(Utils.appDirectoryThumb + info, null);  

				photo.setImageBitmap(bmThumb);

				photo.setOnClickListener(new OnClickListener() {
	
					@Override
					public void onClick(View v) {
						layoutButtonsOverlay.setVisibility(View.INVISIBLE);
						llPhoto.setVisibility(View.VISIBLE);
						File file = new File(Utils.appDirectory + info);
						largePhoto.setImageURI(Uri.fromFile(file));
						
						/*Intent intent = new Intent();
						intent.setAction(android.content.Intent.ACTION_VIEW);
						File file = new File(Utils.appDirectory + info);
						intent.setDataAndType(Uri.fromFile(file), "image/jpeg");
						startActivity(intent); */
					}
					
				});
				showPhoto = true;
				
    		} else {
    			tvInfo.setText(this.getString(R.string.no_imagen_in_phone));
    		}
			break;
		case MapLocation.TYPE_BUBBLE_SOUND:
			btIconInfo.setImageResource(R.drawable.info_icon_sound);
			tvInfo.setTextSize(14);
			File fSound = new File(Utils.appDirectory + info);
    		if (fSound.exists()) {
    			tvInfo.setText(this.getText(R.string.click_play_to_listen));
    			
				button.setOnClickListener(new OnClickListener() {
	
					@Override
					public void onClick(View v) {
						MediaPlayer mp = new MediaPlayer();
			    		try {
			    			mp.setDataSource(Utils.appDirectory + info);
			    			mp.prepare();
			    			mp.start();
			    		} catch (IOException e) {
			    			Utils.showMessage(e.toString());
			    		}
					}
					
				});
				showButton = true;
    		} else {
    			tvInfo.setText(this.getString(R.string.no_sound_in_phone));
    		}
			break;
		case MapLocation.TYPE_BUBBLE_DISTANCE:
			btIconInfo.setImageResource(R.drawable.info_icon_distance);
			tvInfo.setTextSize(26);
			tvInfo.setText(info);
			break;
		case MapLocation.TYPE_BUBBLE_TIME:
			btIconInfo.setImageResource(R.drawable.info_icon_clock);
			tvInfo.setTextSize(26);
			tvInfo.setText(info);
			break;
		default:
			btIconInfo.setImageResource(R.drawable.info_icon_msg);
			tvInfo.setTextSize(14);
			tvInfo.setText(info);
			break;
		}
		
        if (tvInfo.getVisibility()==View.GONE) { 
	        LayoutAnimationController lac = AnimationUtils.loadLayoutAnimation(this, R.anim.outlayout);
	        llInfo.setLayoutAnimation(lac);
	        llInfo.setLayoutAnimationListener(new AnimationListener() {
		        @Override
		        public void onAnimationEnd(Animation lac) {
		        	tvInfo.setVisibility(View.VISIBLE);
		        	if (showPhoto) photo.setVisibility(View.VISIBLE);
		        	if (showButton) button.setVisibility(View.VISIBLE);
		        }
		
		        @Override
		        public void onAnimationRepeat(Animation animation) { }
		
		        @Override
		        public void onAnimationStart(Animation animation) { }
		
		    });
	        llInfo.startLayoutAnimation();
        } else {
        	if (showPhoto) photo.setVisibility(View.VISIBLE);
        	if (showButton) button.setVisibility(View.VISIBLE);
        }

    }
    
    /**
     * Oculta la caja de informacion
     * 
     */
    
    public void hideInformation() {
    	if (tvInfo.getVisibility() == View.VISIBLE) {
	    	hideButtons();
	    	btIconInfo.setImageResource(idCategory);
	    	setTypeButtons(TYPE_BUTTONS_OUT_MARKS);
	    	
	    	tvInfo.setVisibility(View.GONE);
	    	photo.setVisibility(View.GONE);
	    	button.setVisibility(View.GONE);
	    	
	        LayoutAnimationController lac = AnimationUtils.loadLayoutAnimation(this, R.anim.inlayout);
	        llInfo.setLayoutAnimation(lac);
	        
	        llInfo.setLayoutAnimationListener(null);
	        
	        llInfo.startLayoutAnimation();
	
	    	mMapView.getManager().unSelectedLocation();
	    	mMapView.refresh();
    	}
    }
    
    /**
     * Nueva localizacion encontrada por GPS
     * 
     */
    
    public void newLocation(Location loc) {
    	mMapView.getManager().setCurrentLocation(loc);
    	mMapView.getManager().centerCurrentLocation();
    	mMapView.refresh();
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
        
        menu.add(0, SATELLITE_ID, 0, R.string.menu_satellite)
    		.setIcon(android.R.drawable.ic_menu_mapmode);
        menu.add(0, VIEWINFO_ID, 0, R.string.menu_view_info)
    		.setIcon(android.R.drawable.ic_input_get);
        menu.add(0, MYLOCATION_ID, 0, this.getString(R.string.my_location))
    		.setIcon(android.R.drawable.ic_menu_mylocation);
        menu.add(0, GOTOSTART_ID, 0, R.string.menu_gotostart)
			.setIcon(R.drawable.ic_menu_goto);
        menu.add(0, EDIT_ID, 0, R.string.menu_edit)
			.setIcon(android.R.drawable.ic_menu_edit);
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
	        case VIEWINFO_ID:
	    		Intent iViewInfo = new Intent(this, TabViewInfo.class);
	    		iViewInfo.putExtra(DataFramework.KEY_ID, idRoute);
	    		startActivity(iViewInfo);
	        	return true;
	        case MYLOCATION_ID:
	        	if (isRunningService) {
	        		item.setTitle(R.string.my_location);
	        		stopService();
	        	} else {
	                if (!Utils.getGPSStatus(this)) {
	                	showDialog(DIALOG_GPS_NOFOUND);
	                } else {
	                	item.setTitle(R.string.no_my_location);
	                	startService();
	                }
	        	}
	        	return true;
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
	        case EDIT_ID:
	        	Entity route = new Entity("routes", idRoute);
	        	lastDivideDistance = route.getInt("divide_distance");
	        	lastDivideTime = route.getInt("divide_time");
	            Intent iRouteEdit = new Intent(this, RouteEdit.class);
	            iRouteEdit.putExtra(DataFramework.KEY_ID, idRoute);
	            startActivityForResult(iRouteEdit, ACTIVITY_EDITROUTE);
	        	return true;
	        case GOTOSTART_ID:
	        	mMapView.getManager().goToStart();
	        	return true;
        }
       
        return super.onMenuItemSelected(featureId, item);
    }
    
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			back();
		}
		return super.onKeyDown(keyCode, event);
	}
    
    /**
     * Vuelve a la actividad principal
     * 
     */
    
    public void back() {
		if (isRunningService) {
			stopService();
		}
    	mManagerTimeButtons.stop();
    	if (editRoute) setResult(RESULT_OK);
        finish();
    }
    
	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}
	
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("simplify", isSimplify);
        outState.putLong(DataFramework.KEY_ID, idRoute);
        outState.putBoolean("editRoute", editRoute);
        outState.putInt("typeMarksOnMap", mMapView.getManager().getTypeMarksOnMap());
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
        if (requestCode == ACTIVITY_EDITROUTE) {
        	if (resultCode == RESULT_OK) {
        		editRoute = true;
	    		Entity route = new Entity("routes", idRoute);
	    		String name = "";
	    		if (isSimplify) name += getString(R.string.title_route_simplify) + ": ";
	    		else name += getString(R.string.route) + ": ";
	    		name += route.getValue("name").toString();
	    		this.setTitle(name);
	    		
	    		if (lastDivideDistance != route.getInt("divide_distance")) {
	    			mMapView.getManager().createMarksDistanceOnRoute(idRoute);
	    		}
	        	if (lastDivideTime != route.getInt("divide_time")) {
	        		mMapView.getManager().createMarksTimeOnRoute(idRoute);
	        	}
        	}
        	
        	hideButtons();
        	hideInformation();
    		
        }
    }


	public MapLocationViewer getMapView() {
		return mMapView;
	}
	
    private void startService() {
       	isRunningService = true;
       	Intent svc = new Intent(this, ServiceShowGPS.class);
        startService(svc);
        mMapView.getManager().showCurrentLocation();
        mMapView.getManager().setDoingPath(true);
        Utils.showMessage(this.getString(R.string.searching_gps));
    }
    
    private void stopService() {
    	isRunningService = false;
    	Intent svc = new Intent(this, ServiceShowGPS.class);
		stopService(svc);
		mMapView.getManager().hideCurrentLocation();
		mMapView.getManager().setDoingPath(false);
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.unregisterReceiver(receiver);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        this.registerReceiver(receiver, new IntentFilter(Intent.ACTION_VIEW));
    }

	@Override
	protected void onDestroy() {
		super.onDestroy();
		DataFramework.getInstance().close();
	}

}
