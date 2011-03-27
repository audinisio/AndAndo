package com.javielinux.andando;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;

import com.android.dataframework.Entity;
import com.android.dataframework.DataFramework;

public class RouteEdit extends Activity {
	
	private static final int DIALOG_ERROR = 1;
	private static final int DIALOG_REMOVE_GROUP = 2;
	private static final int DIALOG_GROUP = 3;
	
	private static final int SAVE_ID = Menu.FIRST;
	private static final int BACK_ID = Menu.FIRST + 1;
		
	private EditText mTitleText;
    private EditText mBodyText;
    private Spinner mCategory;
    private Spinner mGroup;
    private Spinner mDistance;
    private Spinner mTime;
    private Entity currentEntity;
    private List<Entity> categories;
    private List<Entity> groups;
    private long auxEditGroup = -1;
    
    long mRowId;
    
    /**
     * Crea una nueva ventana de dialogo
     * 
     * @param id Identificador del dialogo
     * @return Objeto Dialog
     * 
     */
    
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
        case DIALOG_ERROR:
            return new AlertDialog.Builder(this)
                .setIcon(R.drawable.alert_dialog_icon)
                .setTitle(R.string.error_empty)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                })
                .create();
        case DIALOG_REMOVE_GROUP:
            return new AlertDialog.Builder(RouteEdit.this)
	            .setIcon(R.drawable.alert_dialog_icon)
	            .setTitle(R.string.alert_dialog_title_remove_group)
	            .setMessage(R.string.alert_dialog_text_remove_group)
	            .setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {
	                public void onClick(DialogInterface dialog, int whichButton) {
	                	Entity e = (Entity)mGroup.getSelectedItem();
	                	if (e.getId()!=1) {
	                    	List<Entity> routes = DataFramework.getInstance().getEntityList("routes", "group_id = " + e.getId());
	                    	for (int i=0; i<routes.size(); i++){
	                    		routes.get(i).setValue("group_id", 1);
	                    		routes.get(i).save();
	                    	}
	                    	e.delete();
	                    	fillSpinnerGroups();
	                	}
	                }
	            })
                .setNegativeButton(R.string.alert_dialog_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                })
            .create();
        case DIALOG_GROUP:
            return new AlertDialog.Builder(RouteEdit.this)
                .setTitle(R.string.select_action)
                .setItems(R.array.select_group_actions, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (which==0) {
                        	final EditText textEntryView = new EditText(RouteEdit.this);
                            textEntryView.setPadding(10, 10, 10, 10);
                        	textEntryView.setText("");
                        	AlertDialog a = new AlertDialog.Builder(RouteEdit.this)
	                            .setIcon(R.drawable.alert_dialog_icon)
	                            .setTitle(R.string.alert_dialog_text_add_group)
	                            .setView(textEntryView)
	                            .setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {
	                                public void onClick(DialogInterface dialog, int whichButton) {
	                                	Entity e = new Entity("groups");
	                                	e.setValue("name", textEntryView.getText().toString());
	                                	e.save();
	                                	currentEntity.setValue("group_id", e);
	                                	fillSpinnerGroups();
	                                }
	                            })
	                            .setNegativeButton(R.string.alert_dialog_cancel, new DialogInterface.OnClickListener() {
	                                public void onClick(DialogInterface dialog, int whichButton) {
	                                }
	                            })
	                            .create();
                        	a.show();
                        	
                        } else if (which==1) {
                        	Entity e = (Entity)mGroup.getSelectedItem();
                        	if (e.getId()!=1) {
                        		auxEditGroup = e.getId();
                        		final EditText textEntryView = new EditText(RouteEdit.this);
                                textEntryView.setPadding(10, 10, 10, 10);
                                textEntryView.setText(e.getValue("name").toString());
                            	AlertDialog a = new AlertDialog.Builder(RouteEdit.this)
    	                            .setIcon(R.drawable.alert_dialog_icon)
    	                            .setTitle(R.string.alert_dialog_text_add_group)
    	                            .setView(textEntryView)
    	                            .setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {
    	                                public void onClick(DialogInterface dialog, int whichButton) {
    	                                	Entity e = new Entity("groups", auxEditGroup);
    	                                	e.setValue("name", textEntryView.getText().toString());
    	                                	e.save();
    	                                	currentEntity.setValue("group_id", e);
    	                                	fillSpinnerGroups();
    	                                }
    	                            })
    	                            .setNegativeButton(R.string.alert_dialog_cancel, new DialogInterface.OnClickListener() {
    	                                public void onClick(DialogInterface dialog, int whichButton) {
    	                                }
    	                            })
    	                            .create();
                            	a.show();
                        	} else {
                        		Utils.showMessage(RouteEdit.this.getString(R.string.no_remove_group));
                        	}
                        } else if (which==2) {
                        	Entity e = (Entity)mGroup.getSelectedItem();
                        	if (e.getId()!=1) {
                        		showDialog(DIALOG_REMOVE_GROUP);
                        	} else {
                        		Utils.showMessage(RouteEdit.this.getString(R.string.no_remove_group));
                        	}
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
                
        this.setTitle(this.getString(R.string.route));
        
        setContentView(R.layout.route_edit);
                
        mTitleText = (EditText) findViewById(R.id.title);
        mBodyText = (EditText) findViewById(R.id.description);
        mCategory = (Spinner) findViewById(R.id.category);
        mGroup = (Spinner) findViewById(R.id.group);
        mDistance = (Spinner) findViewById(R.id.distance_mark);
        mTime = (Spinner) findViewById(R.id.time_mark);
                
        mRowId = savedInstanceState != null ? savedInstanceState.getLong(DataFramework.KEY_ID) : -1;
		if (mRowId < 0) {
			Bundle extras = getIntent().getExtras();            
			mRowId = extras != null ? extras.getLong(DataFramework.KEY_ID) : -1;
		}
		
		ImageButton groupButton = (ImageButton) findViewById(R.id.btn_group);        
		groupButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
            	showDialog(DIALOG_GROUP);
            }
          
        });

		currentEntity = new Entity("routes", mRowId);

		fillSpinners();
		
		populateFields();
        
        
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
        menu.add(0, SAVE_ID, 0,  R.string.save)
        	.setIcon(android.R.drawable.ic_menu_save);
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
        case SAVE_ID:
        	saveEntity();
            setResult(RESULT_OK);
            finish();
            return true;
        case BACK_ID:
            finish();
            return true;
        }
       
        return super.onMenuItemSelected(featureId, item);
    }
    
    /**
     * Guarda la entidad
     *  
     */
    
    private void saveEntity() {
		String title = "Sin nombre";
		if (!mTitleText.getText().toString().equals("")) {
			title = mTitleText.getText().toString();
		}
	    currentEntity.setValue("name", title);
	    currentEntity.setValue("description", mBodyText.getText().toString());
	    currentEntity.setValue("category_id", (Entity)mCategory.getSelectedItem());
	    currentEntity.setValue("group_id", (Entity)mGroup.getSelectedItem());
	    int divide_distance = Integer.parseInt(getResources().getStringArray(R.array.entryvalues_every_distance)[mDistance.getSelectedItemPosition()]);
	    currentEntity.setValue("divide_distance", divide_distance);
	    int divide_time = Integer.parseInt(getResources().getStringArray(R.array.entryvalues_every_time)[mTime.getSelectedItemPosition()]);
	    currentEntity.setValue("divide_time", divide_time);
	    currentEntity.save();
    }
    
    /**
     * Rellena los campos con los datos necesarios segun sea
     * una actualizacion del registro o uno nuevo
     * 
     */
    
    private void populateFields() {
        if (currentEntity.isUpdate()) {
            Cursor note = currentEntity.getCursor();
            startManagingCursor(note);
            mTitleText.setText(currentEntity.getValue("name").toString());
            mBodyText.setText(currentEntity.getValue("description").toString());
            mCategory.setSelection(DataFramework.getInstance().getPosition((ArrayList<Entity>) categories, currentEntity.getEntity("category_id").getId()));
            mGroup.setSelection(DataFramework.getInstance().getPosition((ArrayList<Entity>) groups, currentEntity.getEntity("group_id").getId()));
            String[] strDistance = getResources().getStringArray(R.array.entryvalues_every_distance);
            for (int i=0; i<strDistance.length; i++) {
            	if (strDistance[i].equals(currentEntity.getValue("divide_distance").toString())) {
            		mDistance.setSelection(i);
            	}
            }
            String[] strTime = getResources().getStringArray(R.array.entryvalues_every_time);
            for (int i=0; i<strTime.length; i++) {
            	if (strTime[i].equals(currentEntity.getValue("divide_time").toString())) {
            		mTime.setSelection(i);
            	}
            }
            note.close();
        } else {
        	Date date = new Date();
        	mTitleText.setText(date.toLocaleString());
        }
    }
    
    /**
     * Rellena todos los combos
     * 
     */
    
    public void fillSpinners() {
	    categories = DataFramework.getInstance().getEntityList("categories", "", "position asc");
	    ArrayAdapter<Entity> adapter = new ArrayAdapter<Entity>(this,
	            android.R.layout.simple_spinner_item, categories);
	    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    mCategory.setAdapter(adapter);
	    
	    fillSpinnerGroups();
	    
	    String[] strDistance = getResources().getStringArray(R.array.entries_every_distance);
	    ArrayAdapter<String> adapterDistance = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, strDistance);
	    adapterDistance.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    mDistance.setAdapter(adapterDistance);
	    
	    String[] strTime = getResources().getStringArray(R.array.entries_every_time);
	    ArrayAdapter<String> adapterTime = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, strTime);
	    adapterTime.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    mTime.setAdapter(adapterTime);
	    
    }
    
    /**
     * Rellena el combo de grupos
     * 
     */
    
    public void fillSpinnerGroups() {
	    groups = DataFramework.getInstance().getEntityList("groups");
	    ArrayAdapter<Entity> adapterGroup = new ArrayAdapter<Entity>(this,
	            android.R.layout.simple_spinner_item, groups);
	    adapterGroup.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    mGroup.setAdapter(adapterGroup);
	    mGroup.setSelection(DataFramework.getInstance().getPosition((ArrayList<Entity>) groups, currentEntity.getEntity("group_id").getId()));
    }
    
    /**
     * Guarda el estado de la instancia
     * 
     * @param outState Bundle
     * 
     */
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(DataFramework.KEY_ID, mRowId);
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        saveEntity();
    }
    
    @Override
    protected void onResume() {
        super.onResume();        
        populateFields();
    }
    
	@Override
	protected void onDestroy() {
		super.onDestroy();
		DataFramework.getInstance().close();
	}
    
    
}
