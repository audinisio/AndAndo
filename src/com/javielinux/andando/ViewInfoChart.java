package com.javielinux.andando;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.android.dataframework.DataFramework;
import com.android.dataframework.Entity;
import com.javielinux.chart.AltitudeChart;
import com.javielinux.chart.SpeedChart;

public class ViewInfoChart extends Activity {
	
	private static final int BACK_ID = Menu.FIRST;
	
    private Entity currentEntity;    
    private long idRoute;
        
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
		
        setContentView(R.layout.view_info_chart);
                
        idRoute = savedInstanceState != null ? savedInstanceState.getLong(DataFramework.KEY_ID) : -1;
		if (idRoute < 0) {
			Bundle extras = getIntent().getExtras();            
			idRoute = extras != null ? extras.getLong(DataFramework.KEY_ID) : -1;
		}
		
		currentEntity = new Entity("routes", idRoute);
		
		this.setTitle(currentEntity.getValue("name").toString());
    	
		Button btAltDis = (Button) findViewById(R.id.btn_altitude);
    	btAltDis.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				AltitudeChart atc = new AltitudeChart();
            	Intent intent = atc.execute(ViewInfoChart.this, idRoute);
                startActivity(intent);
			}
        	
        });
    	    	
    	Button btSpdDis = (Button) findViewById(R.id.btn_speed);
    	btSpdDis.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				SpeedChart atc = new SpeedChart();
            	Intent intent = atc.execute(ViewInfoChart.this, idRoute);
                startActivity(intent);
			}
        	
        });
		
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
