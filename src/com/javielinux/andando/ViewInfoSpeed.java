package com.javielinux.andando;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;

import com.android.dataframework.Entity;
import com.android.dataframework.DataFramework;

public class ViewInfoSpeed extends Activity {
	
	private static final int BACK_ID = Menu.FIRST;
	
    private Entity currentEntity;    
    private long idRoute;
    
    private int currentKm;
    
    private List<DataByKilometers> dataByKilometers;
    
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
		
        setContentView(R.layout.view_info_speed);
        
        currentKm = 0;
        
        dataByKilometers = new ArrayList<DataByKilometers>();
        
        idRoute = savedInstanceState != null ? savedInstanceState.getLong(DataFramework.KEY_ID) : -1;
		if (idRoute < 0) {
			Bundle extras = getIntent().getExtras();            
			idRoute = extras != null ? extras.getLong(DataFramework.KEY_ID) : -1;
		}
		
		currentEntity = new Entity("routes", idRoute);
		
		this.setTitle(currentEntity.getValue("name").toString());
		
		if (currentEntity.getValue("average_speed")!=null)
			((TextView)findViewById(R.id.view_average_speed_general)).setText(Utils.formatSpeed(currentEntity.getFloat("average_speed")));
		if (currentEntity.getValue("max_speed")!=null)
			((TextView)findViewById(R.id.view_max_speed_general)).setText(Utils.formatSpeed(currentEntity.getFloat("max_speed")));

		((TextView)findViewById(R.id.average_time_bykm)).setText(Utils.formatMinutesByDistance(currentEntity.getInt("time"),currentEntity.getFloat("distance")));
		
		int nextDistance;
		int sumDistance;
		
		if (Utils.preference.getString("prf_units", "km").equals("km")) {
			((TextView)findViewById(R.id.title_average_time_bykm)).setText(R.string.average_time_kilometer);
			((TextView)findViewById(R.id.title_bykm)).setText(R.string.bykm);
			nextDistance = 1000;
			sumDistance = 1000;
		} else {
			((TextView)findViewById(R.id.title_average_time_bykm)).setText(R.string.average_time_mile);
			((TextView)findViewById(R.id.title_bykm)).setText(R.string.bymile);
			nextDistance = 1609;
			sumDistance = 1609;
		}
				
		Cursor c = DataFramework.getInstance().getCursor("locations", "route_id = " + idRoute, "position asc");
    	c.moveToFirst();
    	
    	
    	float max_speed = 0;
    	float sum_speed = 0;
    	int count = 0;
    	int oldTime = 0;
    	
    	int time = 0;
    	int distance = 0;
    	
    	int countLap = 0;
    	
    	while (!c.isAfterLast()) {
    		
    		float speed = c.getFloat(c.getColumnIndex("speed"));
    		distance = c.getInt(c.getColumnIndex("distance"));
    		time = c.getInt(c.getColumnIndex("time"));
    		
    		if (speed > max_speed) max_speed = speed;
    		sum_speed += speed;
    		
    		
			if (distance>=nextDistance) {
				DataByKilometers d = new DataByKilometers();
				d.time = time - oldTime;
				oldTime = time;
				d.average_speed = sum_speed/countLap;
				d.maximun_speed = max_speed;
				d.meters = sumDistance;
				
				dataByKilometers.add(d);
				
				max_speed = 0;
		    	sum_speed = 0;
		    	countLap = 0;
		    	nextDistance += sumDistance;		    	
			}
    		
			countLap++;
			count++;
    		c.moveToNext();
    	}	
    	
		DataByKilometers d = new DataByKilometers();
		d.time = time - oldTime;
		d.average_speed = sum_speed/countLap;
		d.maximun_speed = max_speed;
		d.meters = distance - (dataByKilometers.size()*sumDistance);
		
		dataByKilometers.add(d);
	    	
    	c.close();
    	
    	writeValues();
    	
    	ImageButton btNext = (ImageButton) findViewById(R.id.viewdata_next);
    	btNext.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				nextKm();
			}
        	
        });
    	
    	ImageButton btPrevious = (ImageButton) findViewById(R.id.viewdata_previous);
    	btPrevious.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				previousKm();
			}
        	
        });
		
    }
    

    private void nextKm() {
    	if (currentKm+1<dataByKilometers.size()) {
    		currentKm++;
    		writeValues();
    	}
    }
    
    private void previousKm() {
    	if (currentKm-1>=0) {
    		currentKm--;
    		writeValues();
    	}
    }
    
    private void writeValues() {
    	DataByKilometers d = dataByKilometers.get(currentKm);
    	
    	((TextView)findViewById(R.id.minutesbykm)).setText(Utils.formatTime(d.time));
    	((TextView)findViewById(R.id.view_average_speed)).setText(Utils.formatSpeed(d.average_speed));
    	((TextView)findViewById(R.id.view_max_speed)).setText(Utils.formatSpeed(d.maximun_speed));
    	if (currentKm+1<dataByKilometers.size()) {
    		String unit;
    		if (Utils.preference.getString("prf_units", "km").equals("km")) {
    			unit = this.getString(R.string.title_kilometer);
    		} else {
    			unit = this.getString(R.string.title_mile);
    		}
    		((TextView)findViewById(R.id.kilometer_count)).setText(unit + ": "+(currentKm+1));
    	} else 
    		((TextView)findViewById(R.id.kilometer_count)).setText(this.getString(R.string.last) + " " + Utils.formatMetres(d.meters));
    	
    }
    
    /**
     * Crea el menu
     * 
     * @param menu Menu
     * @return Booleano
     * 
     */
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, BACK_ID, 0,  R.string.back)
			.setIcon(android.R.drawable.ic_menu_revert);
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
        case BACK_ID:
        	setResult(RESULT_OK);
            finish();
            return true;
        }
       
        return super.onMenuItemSelected(featureId, item);
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(DataFramework.KEY_ID, idRoute);
    }
    
    private class DataByKilometers {
    	int time;
    	float average_speed;
    	float maximun_speed;
    	int meters;
    }
    
    @Override
    protected void onPause() {
        super.onPause();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
    }
    
	@Override
	protected void onDestroy() {
		super.onDestroy();
		DataFramework.getInstance().close();
	}
}
