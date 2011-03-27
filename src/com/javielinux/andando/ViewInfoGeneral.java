package com.javielinux.andando;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.android.dataframework.Entity;
import com.android.dataframework.DataFramework;

public class ViewInfoGeneral extends Activity {
	
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
        
        setContentView(R.layout.view_info_general);
                
        idRoute = savedInstanceState != null ? savedInstanceState.getLong(DataFramework.KEY_ID) : -1;
		if (idRoute < 0) {
			Bundle extras = getIntent().getExtras();            
			idRoute = extras != null ? extras.getLong(DataFramework.KEY_ID) : -1;
		}
		
		currentEntity = new Entity("routes", idRoute);
		
		this.setTitle(currentEntity.getValue("name").toString());
		
		if (currentEntity.getValue("name")!=null) 
			((TextView)findViewById(R.id.view_name)).setText(currentEntity.getValue("name").toString());
		if (currentEntity.getValue("date")!=null) {
			((TextView)findViewById(R.id.view_date)).setText(Utils.formatDate(currentEntity.getValue("date").toString()));
			((TextView)findViewById(R.id.view_hour)).setText(Utils.formatHour(currentEntity.getValue("date").toString()));
		}
		if (currentEntity.getValue("time")!=null)
			((TextView)findViewById(R.id.view_time)).setText(Utils.formatTime(Integer.parseInt(currentEntity.getValue("time").toString())));
		if (currentEntity.getValue("distance")!=null)
			((TextView)findViewById(R.id.view_distance)).setText(Utils.formatDistance(Float.parseFloat(currentEntity.getValue("distance").toString())));
		if (currentEntity.getValue("coordinates_route")!=null)
			((TextView)findViewById(R.id.view_coordinates)).setText(currentEntity.getValue("coordinates_route").toString());

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
