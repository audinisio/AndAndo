/*
 * Copyright (C) 2009  Javier Perez Pacheco
 *
 * AndAndo: Programa para trabajar con rutas en Android
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Javier Perez Pacheco
 * Cadiz (Spain)
 * javielinux@gmail.com
 *
 */

package com.javielinux.andando;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.android.dataframework.DataFramework;
import com.android.dataframework.Entity;

public class Andando extends Activity {
	
	private static final int DIALOG_CREATEROUTE = 0;
	
	private static final int ACTIVITY_NEWROUTE = 1;
	private static final int ACTIVITY_LISTROUTES = 2;
	private static final int ACTIVITY_PREFERENCES = 3;
	private static final int ACTIVITY_GPS = 4;
		
	private ImageButton btnGPS;
	
    /**
     * Crea una nueva ventana de dialogo
     * 
     * @param id Identificador
     * @return Dialog dialogo
     */
	
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
        case DIALOG_CREATEROUTE:
        	
        	final List<Entity> categories = DataFramework.getInstance().getEntityList("categories", "", "position asc");
        	RowCategoryAdapter adapter = new RowCategoryAdapter(this, categories, true); 
        	
	        return new AlertDialog.Builder(this)
		        .setIcon(R.drawable.alert_dialog_icon)
		        .setTitle(R.string.select_category)
		        .setAdapter(adapter, new DialogInterface.OnClickListener() {
		            public void onClick(DialogInterface dialog, int whichButton) {
		            	createRoute(categories.get(whichButton).getId());
		            }
		        })
		        .setNegativeButton(R.string.alert_dialog_cancel, new DialogInterface.OnClickListener() {
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
        
        this.setTitle(R.string.app_name);
                
        boolean back = false;
		if (savedInstanceState != null) {
			if (savedInstanceState.containsKey("back")) back = savedInstanceState.getString("back").equals("yes");
		} else {
			Bundle extras = getIntent().getExtras();  
			if (extras != null) {
				back = (extras.containsKey("back")) ? extras.getString("back").equals("yes") : false;
			}
		}
		
		if (back) back();
                
       	setContentView(R.layout.main);
		
		ImageButton btnNew = (ImageButton) this.findViewById(R.id.newroute);
		
		btnNew.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				showDialog(DIALOG_CREATEROUTE);			
			}
			
		});
		
		btnNew.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				ImageButton img = (ImageButton) Andando.this.findViewById(R.id.newroute);
				switch (event.getAction()) {
                	case MotionEvent.ACTION_DOWN:
                		img.setImageResource(R.drawable.btn_new_route_on);
                		break;
                	case MotionEvent.ACTION_UP:
                		img.setImageResource(R.drawable.btn_new_route_off);
                		break;
				}
				return false;
			}
			
		});
		
		ImageButton btnList = (ImageButton) this.findViewById(R.id.listroutes);
		
		btnList.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				listRoutes();				
			}
			
		});
		
		btnList.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				ImageButton img = (ImageButton) Andando.this.findViewById(R.id.listroutes);
				switch (event.getAction()) {
                	case MotionEvent.ACTION_DOWN:
                		img.setImageResource(R.drawable.btn_track_list_on);
                		break;
                	case MotionEvent.ACTION_UP:
                		img.setImageResource(R.drawable.btn_track_list_off);
                		break;
				}
				return false;
			}
			
		});
		
		ImageButton btnPreferences = (ImageButton) this.findViewById(R.id.preferences);
		
		btnPreferences.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				preferences();
			}
			
		});
		
		btnPreferences.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				ImageButton img = (ImageButton) Andando.this.findViewById(R.id.preferences);
				switch (event.getAction()) {
                	case MotionEvent.ACTION_DOWN:
                		img.setImageResource(R.drawable.btn_preferences_on);
                		break;
                	case MotionEvent.ACTION_UP:
                		img.setImageResource(R.drawable.btn_preferences_off);
                		break;
				}
				return false;
			}
			
		});
        
		btnGPS = (ImageButton) this.findViewById(R.id.gps);
		
		if (Utils.getGPSStatus(this)) {
			btnGPS.setImageResource(R.drawable.btn_gps_active_off);
		} else {
			btnGPS.setImageResource(R.drawable.btn_gps_noactive_off);
		}
		
		btnGPS.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent settingsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
				settingsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
				startActivityForResult(settingsIntent, Andando.ACTIVITY_GPS);
			}
			
		});
		
		btnGPS.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				ImageButton img = (ImageButton) Andando.this.findViewById(R.id.gps);
				switch (event.getAction()) {
                	case MotionEvent.ACTION_DOWN:
                		img.setImageResource(R.drawable.btn_gps_active_on);
                		break;
                	case MotionEvent.ACTION_UP:
                		if (Utils.getGPSStatus(Andando.this)) {
                			btnGPS.setImageResource(R.drawable.btn_gps_active_off);
                		} else {
                			btnGPS.setImageResource(R.drawable.btn_gps_noactive_off);
                		}
                		break;
				}
				return false;
			}
			
		});
		
		/*DisplayMetrics dm = new DisplayMetrics(); 
		getWindowManager().getDefaultDisplay().getMetrics(dm); 
		int height = dm.heightPixels; 
		int width = dm.widthPixels; */
		
		Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay(); 
		int width = display.getWidth(); 
		int height = display.getHeight(); 
				
		if ( (width!=320) || (height!=480) ) {
			RelativeLayout.LayoutParams llNew = new RelativeLayout.LayoutParams (LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			llNew.topMargin = 138*height/480;
			llNew.leftMargin = 8*width/320;
			btnNew.setLayoutParams(llNew);
			
			RelativeLayout.LayoutParams llList = new RelativeLayout.LayoutParams (LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			llList.topMargin = 115*height/480;
			llList.leftMargin = 190*width/320;
			btnList.setLayoutParams(llList);
			
			RelativeLayout.LayoutParams llPreferences = new RelativeLayout.LayoutParams (LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			llPreferences.topMargin = 226*height/480;
			llPreferences.leftMargin = 108*width/320;
			btnPreferences.setLayoutParams(llPreferences);
			
			RelativeLayout.LayoutParams llGPS = new RelativeLayout.LayoutParams (LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			llGPS.topMargin = 320*height/480;
			llGPS.leftMargin = 205*width/320;
			btnGPS.setLayoutParams(llGPS);
		}
		
    }
    
    /**
     * Envia a una nueva Actividad para crear una ruta nueva
     * 
     * @param category Categoria de la ruta a crear
     */
    
    private void createRoute(long category) {
    	Entity entity = new Entity("routes");
    	entity.setValue("name", Utils.now(this.getResources().getString(R.string.format_date)));
    	entity.setValue("date", Utils.now());
    	entity.setValue("description", "");
    	entity.setValue("divide_time", Integer.parseInt(Utils.preference.getString("prf_time_marks", "120")));
    	entity.setValue("divide_distance", Integer.parseInt(Utils.preference.getString("prf_distance_marks", "250")));
    	entity.setValue("category_id", category);
    	entity.setValue("group_id", 1);
    	entity.save();
        Intent i = new Intent(this, CreateRoute.class);
        i.putExtra(DataFramework.KEY_ID, entity.getId());
        startActivityForResult(i, ACTIVITY_NEWROUTE);
    }
    
    /**
     * Envia a una nueva Actividad que muestra las rutas almacenadas
     * 
     */
    
    private void listRoutes() {
        Intent i = new Intent(this, RoutesList.class);
        startActivityForResult(i, ACTIVITY_LISTROUTES);
    }
    
    /**
     * Envia a una nueva Actividad que muestra las preferencias del programa
     * 
     */
    
    private void preferences() {
        Intent i = new Intent(this, Preferences.class);
        startActivityForResult(i, ACTIVITY_PREFERENCES);
    }
    
    /**
     * Vuelve a la actividad anterior
     * 
     */
    
    public void back() {
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
    }
    
	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		if (Utils.getGPSStatus(this)) {
			btnGPS.setImageResource(R.drawable.btn_gps_active_off);
		} else {
			btnGPS.setImageResource(R.drawable.btn_gps_noactive_off);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		DataFramework.getInstance().close();
	}

}