package com.javielinux.andando;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.android.dataframework.DataFramework;
import com.android.dataframework.Entity;

public class RoutesList extends ListActivity {
	
	private static final int DIALOG_ITEM = 0;
	private static final int DIALOG_DELETE = 1;
	private static final int DIALOG_DELETE_ALL = 2;
	private static final int DIALOG_IMPORT = 3;
	private static final int DIALOG_FILTER = 4;
	private static final int DIALOG_EXPORT = 5;
	private static final int DIALOG_WRITE_SDCARD = 6;
	private static final int DIALOG_SIMPLIFY = 7;
	private static final int DIALOG_FILTER_CATEGORY = 8;
	private static final int DIALOG_FILTER_GROUP = 9;
	private static final int DIALOG_FILTER_DATE = 10;
	private static final int DIALOG_FILTER_ORDER = 11;
	//private static final int DIALOG_IMPORT_ANDANDO = 12;
	//private static final int DIALOG_IMPORT_KML = 13;

	private static final int ACTIVITY_SHOW = 0;
	private static final int ACTIVITY_EDITROUTE = 1;
	
	private static final int IMPORT_ID = Menu.FIRST;
	private static final int DELETEALLROUTES_ID = Menu.FIRST + 1;
	private static final int FILTER_ID = Menu.FIRST + 2;
	//private static final int BACKUP_ID = Menu.FIRST + 4;
	private RowRouteAdapter routes;
	private long selectId=-1;
	private int selectPostion=-1;
	
	private View viewFilter;
	private TextView filterTextCategory;
	private TextView filterTextGroup;
	private TextView filterTextDate;
	private TextView filterTextOrder;
	
	private ProgressDialog pd = null;
	
	private long searchIdCategory = -1;
	private long searchIdGroup = -1;
	private long searchDate = -1;
	private long orderList = -1;
	
	private ThreadExport threadExport = null;
	private ThreadSimplify threadSimplify = null;
	
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
        
        this.setTitle(this.getString(R.string.list_routes));
        
        setContentView(R.layout.routes_list);
        
        if (savedInstanceState != null) {
			if (savedInstanceState.containsKey("searchIdCategory")) searchIdCategory = savedInstanceState.getLong("searchIdCategory");
			if (savedInstanceState.containsKey("searchIdGroup")) searchIdGroup = savedInstanceState.getLong("searchIdGroup");
			if (savedInstanceState.containsKey("searchDate")) searchDate = savedInstanceState.getLong("searchDate");
			if (savedInstanceState.containsKey("selectId")) selectId = savedInstanceState.getLong("selectId");
			if (savedInstanceState.containsKey("selectPostion")) selectPostion = savedInstanceState.getInt("selectPostion");
			if (savedInstanceState.containsKey("orderList")) orderList = savedInstanceState.getLong("orderList");
        } else {
        	Bundle extras = getIntent().getExtras();  
			if (extras != null) {
				searchIdCategory = (extras.containsKey("searchIdCategory")) ? extras.getLong("searchIdCategory") : -1;
				searchIdGroup = (extras.containsKey("searchIdGroup")) ? extras.getLong("searchIdGroup") : -1;
				searchDate = (extras.containsKey("searchDate")) ? extras.getLong("searchDate") : -1;
				selectId = (extras.containsKey("selectId")) ? extras.getLong("selectId") : -1;
				selectPostion = (extras.containsKey("selectPostion")) ? extras.getInt("selectPostion") : -1;
				orderList = (extras.containsKey("orderList")) ? extras.getLong("orderList") : -1;
			} else {
				searchIdCategory = -1;
				searchIdGroup = -1;
				searchDate = -1;
				selectId = -1;
				selectPostion = -1;
				orderList = -1;
			}
        }
        
        viewFilter = View.inflate(this, R.layout.filter_routes, null);
        
        filterTextCategory = (TextView)viewFilter.findViewById(R.id.text_category);
        filterTextGroup = (TextView)viewFilter.findViewById(R.id.text_group);
        filterTextDate = (TextView)viewFilter.findViewById(R.id.text_date);
        filterTextOrder = (TextView)viewFilter.findViewById(R.id.text_order);
        
        ImageButton changeCategory = (ImageButton)viewFilter.findViewById(R.id.btn_change_category);
    	
    	changeCategory.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				showDialog(DIALOG_FILTER_CATEGORY);
			}
			
		});
    	
    	ImageButton removeCategory = (ImageButton)viewFilter.findViewById(R.id.btn_remove_category);
    	
    	removeCategory.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				searchIdCategory = -1; 
            	filterTextCategory.setText(RoutesList.this.getString(R.string.alert_dialog_show_all));
			}
			
		});
    	
    	ImageButton changeGroup = (ImageButton)viewFilter.findViewById(R.id.btn_change_group);
    	
    	changeGroup.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				showDialog(DIALOG_FILTER_GROUP);
			}
			
		});
    	
    	ImageButton removeGroup = (ImageButton)viewFilter.findViewById(R.id.btn_remove_group);
    	
    	removeGroup.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				searchIdGroup = -1; 
            	filterTextGroup.setText(RoutesList.this.getString(R.string.alert_dialog_show_all));
			}
			
		});
    	
    	ImageButton changeDate = (ImageButton)viewFilter.findViewById(R.id.btn_change_date);
    	
    	changeDate.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				showDialog(DIALOG_FILTER_DATE);
			}
			
		});
    	
    	ImageButton removeDate = (ImageButton)viewFilter.findViewById(R.id.btn_remove_date);
    	
    	removeDate.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				searchDate= -1; 
            	filterTextDate.setText(RoutesList.this.getString(R.string.alert_dialog_show_all));
			}
			
		});
    	
    	ImageButton changeOrder = (ImageButton)viewFilter.findViewById(R.id.btn_change_order);
    	
    	changeOrder.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				showDialog(DIALOG_FILTER_ORDER);
			}
			
		});
    	
    	ImageButton removeOrder = (ImageButton)viewFilter.findViewById(R.id.btn_remove_order);
    	
    	removeOrder.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				orderList= -1; 
				filterTextOrder.setText(RoutesList.this.getString(R.string.alert_dialog_order_name));
			}
			
		});
        
    }
    
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
        case DIALOG_FILTER:        	
	        return new AlertDialog.Builder(this)
		        .setIcon(R.drawable.alert_dialog_icon)
		        .setTitle(R.string.alert_dialog_filter)
		        .setView(viewFilter)
		        .setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {
		            public void onClick(DialogInterface dialog, int whichButton) {
		            	fillData();
		            }
		        })
		        .setNegativeButton(R.string.alert_dialog_cancel, new DialogInterface.OnClickListener() {
		            public void onClick(DialogInterface dialog, int whichButton) {
		            }
		        })
		       .create();
        case DIALOG_FILTER_DATE:
            return new AlertDialog.Builder(RoutesList.this)
                .setTitle(R.string.select_action)
                .setItems(R.array.filter_date, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    	searchDate= which; 
		            	filterTextDate.setText(RoutesList.this.getResources().getStringArray(R.array.filter_date)[which]);
                    }
                })
                .create();
        case DIALOG_FILTER_ORDER:
            return new AlertDialog.Builder(RoutesList.this)
                .setTitle(R.string.select_action)
                .setItems(R.array.filter_order, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    	orderList= which; 
                    	filterTextOrder.setText(RoutesList.this.getResources().getStringArray(R.array.filter_order)[which]);
                    }
                })
                .create();
        case DIALOG_FILTER_GROUP:
        	final List<Entity> groups = new ArrayList<Entity>();
        	groups.addAll(DataFramework.getInstance().getEntityList("groups", "", "_id asc"));
        	RowCategoryAdapter adapterGroup = new RowCategoryAdapter(this, groups, false); 
        	
	        return new AlertDialog.Builder(this)
		        .setIcon(R.drawable.alert_dialog_icon)
		        .setTitle(R.string.alert_dialog_filter)
		        .setAdapter(adapterGroup, new DialogInterface.OnClickListener() {
		            public void onClick(DialogInterface dialog, int whichButton) {
		            	searchIdGroup = groups.get(whichButton).getId(); 
		            	filterTextGroup.setText(groups.get(whichButton).getString("name"));
		            }
		        })
		        .setNegativeButton(R.string.alert_dialog_cancel, new DialogInterface.OnClickListener() {
		            public void onClick(DialogInterface dialog, int whichButton) {
		            }
		        })
		       .create();
        case DIALOG_FILTER_CATEGORY:
        	final List<Entity> categories = new ArrayList<Entity>();
        	categories.addAll(DataFramework.getInstance().getEntityList("categories", "", "position asc"));
        	RowCategoryAdapter adapter = new RowCategoryAdapter(this, categories, true); 
        	
	        return new AlertDialog.Builder(this)
		        .setIcon(R.drawable.alert_dialog_icon)
		        .setTitle(R.string.alert_dialog_filter)
		        .setAdapter(adapter, new DialogInterface.OnClickListener() {
		            public void onClick(DialogInterface dialog, int whichButton) {
		            	searchIdCategory = categories.get(whichButton).getId(); 
		            	filterTextCategory.setText(categories.get(whichButton).getString("name"));
		            }
		        })
		        .setNegativeButton(R.string.alert_dialog_cancel, new DialogInterface.OnClickListener() {
		            public void onClick(DialogInterface dialog, int whichButton) {
		            }
		        })
		       .create();
        case DIALOG_ITEM:
            return new AlertDialog.Builder(RoutesList.this)
                .setTitle(R.string.select_action)
                .setItems(R.array.select_route_actions, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    	if (selectId>=0) {
	                        if (which==0) {
	                            showRoute(selectId);
	                        } else if (which==1) {
	                        	editRoute(selectId);
	                        } else if (which==2) {
	                        	showDialog(DIALOG_EXPORT);
	                        } else if (which==3) {
	                        	export(ThreadExport.TYPE_YOURTRAININGS, false, false);
	                        } else if (which==4) {
	                        	showRouteInWeb(selectId);
	                        } else if (which==5) {
	                        	showDialog(DIALOG_WRITE_SDCARD);
	                        } else if (which==6) {
	                        	showDialog(DIALOG_SIMPLIFY);
	                        } else if (which==7) {
	                        	viewMoreDataRoute(selectId);
	                        } else if (which==8) {
	                        	recalculateTotals(selectId);
	                        } else if (which==9) {
	                        	showDialog(DIALOG_DELETE);
	                        }
                    	}
                    }
                })
                .create();
        case DIALOG_DELETE_ALL:
            return new AlertDialog.Builder(this)
                .setIcon(R.drawable.alert_dialog_icon)
                .setTitle(R.string.title_question_delete_all)
                .setMessage(R.string.question_delete_all)
                .setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    	deleteAllRoutes();
                    }
                })
                .setNegativeButton(R.string.alert_dialog_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                })
                .create();
        case DIALOG_SIMPLIFY:
            return new AlertDialog.Builder(this)
                .setIcon(R.drawable.alert_dialog_icon)
                .setTitle(R.string.alert_dialog_simplify)
                .setMessage(R.string.alert_dialog_text_simplify)
                .setNeutralButton(R.string.alert_dialog_show, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    	if (selectId>=0) {
                    		showSimplifyRoute(selectId);
                    	}
                    }
                })
                .setPositiveButton(R.string.alert_dialog_simplify, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    	simplify();
                    }
                })
                .setNegativeButton(R.string.alert_dialog_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                })
                .create();
        case DIALOG_DELETE:
            return new AlertDialog.Builder(this)
                .setIcon(R.drawable.alert_dialog_icon)
                .setTitle(R.string.title_question_delete)
                .setMessage(R.string.question_delete)
                .setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    	deleteSelectedRoute();
                    }
                })
                .setNegativeButton(R.string.alert_dialog_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                })
                .create();
        case DIALOG_IMPORT:
            return new AlertDialog.Builder(RoutesList.this)
                .setTitle(R.string.select_action)
                .setItems(R.array.select_route_import, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (which==0) {
                        	//showDialog(DIALOG_IMPORT_KML);
                        	showDialogImportKML();
                        } else if (which==1) {
                        	//showDialog(DIALOG_IMPORT_ANDANDO);
                        	showDialogImportAndAndo();
                        }
                    }
                })
                .create();
        case DIALOG_EXPORT:
        	final CheckBox cbWriteDefault = new CheckBox(this);
        	cbWriteDefault.setText(R.string.dialog_write_default);
        	cbWriteDefault.setChecked(true);
            return new AlertDialog.Builder(RoutesList.this)
                .setTitle(R.string.select_share_friends)
                .setView(cbWriteDefault)
                .setItems(R.array.select_route_export, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (which==0) {
                        	export(ThreadExport.TYPE_KML, true, cbWriteDefault.isChecked());
                        } else if (which==1) {
                        	export(ThreadExport.TYPE_KMZ, true, cbWriteDefault.isChecked());
                        } else if (which==2) {
                        	export(ThreadExport.TYPE_GPX, true, cbWriteDefault.isChecked());
                        } else if (which==3) {
                        	export(ThreadExport.TYPE_ANDANDO, true, cbWriteDefault.isChecked());
                        }
                    }
                })
                .create();
        case DIALOG_WRITE_SDCARD:
            return new AlertDialog.Builder(RoutesList.this)
                .setTitle(R.string.select_write_sdcard)
                .setItems(R.array.select_route_export, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (which==0) {
                        	export(ThreadExport.TYPE_KML, false, false);
                        } else if (which==1) {
                        	export(ThreadExport.TYPE_KMZ, false, false);
                        } else if (which==2) {
                        	export(ThreadExport.TYPE_GPX, false, false);
                        } else if (which==3) {
                        	export(ThreadExport.TYPE_ANDANDO, false, false);
                        }
                    }
                })
                .create();
        }
        return null;
    }
    
    private void showDialogImportKML() {
    	final ArrayList<CharSequence> ar = new ArrayList<CharSequence>();
    	ar.clear();
    	File f = new File(Utils.appDirectory);
    	if (f.isDirectory()) {
			File [] files = f.listFiles();
			int i;
			for (i=0;i<files.length;i++) {
				if (files[i].isFile()) {
					if (files[i].getName().substring(files[i].getName().length()-3, files[i].getName().length()).equals("kml")) {
						ar.add(files[i].getName());
					}
				}
			}
    	}
    	AlertDialog alert; 
    	
    	if (ar.size()>0) {
        	CharSequence[] cs = new CharSequence[ar.size()];
        	for (int i=0; i<ar.size(); i++) {
        		cs[i] = ar.get(i);
        	}
        	
        	LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
    				LayoutParams.WRAP_CONTENT);
        	params.setMargins(5, 5, 5, 5);
        	final CheckBox cbDelete = new CheckBox(this);
        	cbDelete.setLayoutParams(params);
        	cbDelete.setText(R.string.dialog_import_delete);
        	cbDelete.setChecked(false);
        	
        	alert = new AlertDialog.Builder(this)
		        .setIcon(R.drawable.alert_dialog_icon)
		        .setTitle(R.string.alert_dialog_import)
		        .setView(cbDelete)
		        .setItems(cs, new DialogInterface.OnClickListener() {
		            public void onClick(DialogInterface dialog, int whichButton) {
		            	importRoute(ThreadImport.TYPE_KML, ar.get(whichButton).toString(), cbDelete.isChecked());
		            }
		        })
		        .setNegativeButton(R.string.alert_dialog_cancel, new DialogInterface.OnClickListener() {
		            public void onClick(DialogInterface dialog, int whichButton) {
		            }
		        })
		       .create();
    	} else {
    		TextView l = new TextView(this);
    		l.setText(getResources().getString(R.string.alert_dialog_nofiles_import));
            l.setPadding(10, 10, 10, 10);
            alert = new AlertDialog.Builder(this)
                .setIcon(R.drawable.alert_dialog_icon)
                .setTitle(R.string.attention)
                .setView(l)
                .setNegativeButton(R.string.alert_dialog_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                })
                .create();
    	}
    	alert.show();
    }
    
    private void showDialogImportAndAndo() {
    	final ArrayList<CharSequence> arImpAnd = new ArrayList<CharSequence>();
    	arImpAnd.clear();
    	File fImpAnd = new File(Utils.appDirectory);
    	if (fImpAnd.isDirectory()) {
			File [] files = fImpAnd.listFiles();
			int i;
			for (i=0;i<files.length;i++) {
				if (files[i].isFile()) {
					if ( (files[i].getName().substring(files[i].getName().length()-3, files[i].getName().length()).equals("ndn"))
							|| (files[i].getName().substring(files[i].getName().length()-3, files[i].getName().length()).equals("zip")) ) {
						arImpAnd.add(files[i].getName());
					}
				}
			}
    	}
    	AlertDialog alert; 
    	
    	if (arImpAnd.size()>0) {
        	CharSequence[] cs = new CharSequence[arImpAnd.size()];
        	for (int i=0; i<arImpAnd.size(); i++) {
        		cs[i] = arImpAnd.get(i);
        	}
        	
        	LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
    				LayoutParams.WRAP_CONTENT);
        	params.setMargins(5, 5, 5, 5);
        	final CheckBox cbDelete = new CheckBox(this);
        	cbDelete.setLayoutParams(params);
        	cbDelete.setText(R.string.dialog_import_delete);
        	cbDelete.setChecked(false);
        	
        	alert = new AlertDialog.Builder(this)
		        .setIcon(R.drawable.alert_dialog_icon)
		        .setTitle(R.string.alert_dialog_import)
		        .setView(cbDelete)
		        .setItems(cs, new DialogInterface.OnClickListener() {
		            public void onClick(DialogInterface dialog, int whichButton) {
		            	importRoute(ThreadImport.TYPE_ANDANDO, arImpAnd.get(whichButton).toString(), cbDelete.isChecked());
		            }
		        })
		        .setNegativeButton(R.string.alert_dialog_cancel, new DialogInterface.OnClickListener() {
		            public void onClick(DialogInterface dialog, int whichButton) {
		            }
		        })
		       .create();
    	} else {
    		TextView l = new TextView(this);
    		l.setText(getResources().getString(R.string.alert_dialog_nofiles_import));
            l.setPadding(10, 10, 10, 10);
            alert = new AlertDialog.Builder(this)
                .setIcon(R.drawable.alert_dialog_icon)
                .setTitle(R.string.attention)
                .setView(l)
                .setNegativeButton(R.string.alert_dialog_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                })
                .create();
    	}
    	alert.show();
    }
        
    /**
     * Comienza a importar la ruta
     * 
     */
    
    public void importRoute(int type, String file, boolean deleteToFinish) {
       
		pd = ProgressDialog.show(this, getResources().getString(R.string.dialog_import_importing), 
				getResources().getString(R.string.dialog_import_text), true, false);
    	
    	ThreadImport ti = new ThreadImport(this, type, file, deleteToFinish);
    	
    	Thread thread = new Thread(ti);
		thread.start();
    }
    
    /**
     * Finaliza de importar la ruta
     * 
     */
    
    public void endImport(int resourceText, String file, boolean deleteToFinish) {
    	pd.dismiss();
    	pd = null;
    	Utils.showMessage(getString(resourceText));
    	fillData();
    	if (deleteToFinish) {
    		String filename = Utils.appDirectory+file;
    		File f = new File(filename);
    		if (f.exists()) f.delete();
    	}
    }
    
    /**
     * Exporta la ruta
     * 
     */
    
    public void simplify() {
    	        
        long idRoute = selectId;
        Entity e = new Entity("routes", selectId);
        int nloc = Integer.parseInt(e.getValue("coordinates_route").toString());
    	
        pd = new ProgressDialog(this);
        pd.setIcon(R.drawable.alert_dialog_icon);
        pd.setTitle(R.string.dialog_simplify_simplifying);
        pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pd.setMax(nloc);
        pd.setCancelable(false);
        pd.setMessage(getResources().getString(R.string.dialog_export_preparing));
        pd.show();

        threadSimplify = new ThreadSimplify(this, idRoute);
    	
    	Thread thread = new Thread(threadSimplify);
		thread.start();

    }
    
    /**
     * Finaliza de exportar la ruta
     * 
     * @param type Tipo de archivo a exportar
     * @param sendByEmail Si se env�a por correo
     * @param file Archivo exportado
     * 
     */
    
    public void endSimplify() {
    	pd.dismiss();
    	pd = null;
    	fillData();
   		Utils.showMessage(getResources().getString(R.string.route_simplify));
    }
    
    /**
     * Exporta la ruta
     * 
     * @param type Tipo de archivo a exportar
     * @param sendByEmail Si se envia por correo
     * 
     */
    
    public void export(int type, boolean sendByEmail, boolean writeDefaultDataInEmail) {
    	
    	boolean todo = true;
    	
    	if (type==ThreadExport.TYPE_YOURTRAININGS) {
    		if (Utils.getUsernameYourTrainings(this).equals("") || Utils.getKeyYourTrainings(this).equals("")) {
    			todo = false;
    		}
    	}
    	
    	if (todo) {
    	
	    	DialogInterface.OnCancelListener dialogCancel = new DialogInterface.OnCancelListener() {
	
	            public void onCancel(DialogInterface dialog) {
	                Utils.showMessage(getResources().getString(R.string.dialog_export_cancel));
	                if (threadExport != null) threadExport.cancel();
	            }
	          
	        };
	        
	        long idRoute = selectId;
	        Entity e = new Entity("routes", selectId);
	        int nloc = Integer.parseInt(e.getValue("coordinates_route").toString());
	    	
	        pd = new ProgressDialog(this);
	        pd.setIcon(R.drawable.alert_dialog_icon);
	        pd.setTitle(R.string.dialog_export_exporting);
	        pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
	        pd.setMax(nloc);
	        pd.setOnCancelListener(dialogCancel);    	
	        pd.setMessage(getResources().getString(R.string.dialog_export_preparing));
	        pd.show();
	
	    	threadExport = new ThreadExport(this, type, idRoute, sendByEmail, writeDefaultDataInEmail);
	    	
	    	Thread thread = new Thread(threadExport);
			thread.start();
			
    	} else {
    		Utils.showMessage(getString(R.string.no_key_yourtrainings));
    	}

    }
    
    /**
     * Finaliza de exportar la ruta
     * 
     * @param type Tipo de archivo a exportar
     * @param sendByEmail Si se envia por correo
     * @param file Archivo exportado
     * 
     */
    
    public void incrementProgress() {
    	if (pd!=null) pd.incrementProgressBy(1);
    }
    
    /**
     * Finaliza de exportar la ruta
     * 
     * @param type Tipo de archivo a exportar
     * @param sendByEmail Si se env�a por correo
     * @param file Archivo exportado
     * 
     */
    
    public void endExport(int type, boolean sendByEmail, String file, boolean writeDefaultDataInEmail) {
    	pd.dismiss();
    	pd = null;
    	if (sendByEmail) {
    		sendRouteByEmail(type, file, writeDefaultDataInEmail);
    	} else {
    		Utils.showMessage(getResources().getString(R.string.export_route) + file);
    	}
    }
    
    public void endExportYourTrainings(boolean ok) {
    	pd.dismiss();
    	pd = null;
    	if (ok) {
    		Utils.showMessage(getResources().getString(R.string.dialog_export_finish));
    	} else {
    		Utils.showMessage(getResources().getString(R.string.dialog_export_no_finish));
    	}
    }
       
    /**
     * Escribe en el di�logo de proceso de exportaci�n
     * 
     * @param msg Mensaje a escribir
     * 
     */
    
    public void writeInProgressDialog(String msg) {
    	if (pd!=null) pd.setMessage(msg);
    }
    
    /**
     * Envia la ruta por email
     * 
     * @param type Tipo de archivo a exportar
     * @param sendByEmail Si se env�a por correo
     * 
     */
    
    public void sendRouteByEmail(int type, String file, boolean writeDefaultDataInEmail) {
    	String typeMime;
    	String body;
    	if (type == ThreadExport.TYPE_KML) {
    		typeMime = "application/vnd.google-earth.kml+xml";
    		body = getResources().getString(R.string.email_kml_body);
    	} else if (type == ThreadExport.TYPE_KMZ) {
    		typeMime = "application/vnd.google-earth.kmz";
    		body = getResources().getString(R.string.email_kml_body);
    	} else if (type == ThreadExport.TYPE_GPX) {
    		typeMime = "application/xml";
    		body = getResources().getString(R.string.email_kml_body);
    	} else {
    		typeMime = "application/zip";
    		body = getResources().getString(R.string.email_andando_body);
    	}
    	
    	Intent msg=new Intent(Intent.ACTION_SEND);  
    	/*String[] recipients={"javi.pacheco@gmail.com"};  
    	msg.putExtra(Intent.EXTRA_EMAIL, recipients);*/
    	if (writeDefaultDataInEmail) {
    		msg.putExtra(Intent.EXTRA_TEXT, body);  
    		msg.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.email_kml_subject));
    	}
    	msg.setType(typeMime);
    	msg.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://"+file));
    	    	
    	startActivity(Intent.createChooser(msg, "Export"));
    	
    	/*if (type.equals("kml")) {
        	startActivity(Intent.createChooser(msg, "Export"));
        } else {
        	startActivity(Intent.createChooser(msg, "Export"));
        }*/
    	
    }
	
    /**
     * Borra la ruta seleccionada
     * 
     */
    
    private void deleteSelectedRoute() {
    	
		Cursor c = DataFramework.getInstance().getCursor("resource", "route_id = " + selectId, "");
		c.moveToFirst();
		while (!c.isAfterLast()) {
			String file = c.getString(c.getColumnIndex("file"));
			File f = new File(Utils.appDirectory+file);
			if( f.exists() ) f.delete();
			c.moveToNext();
		}
    	
		c.close();
		
		DataFramework.getInstance().getDB().execSQL("DELETE FROM locations WHERE route_id = " + selectId);
		DataFramework.getInstance().getDB().execSQL("DELETE FROM resource WHERE route_id = " + selectId);
    	Entity e = new Entity("routes", selectId);
    	e.delete();
    	selectId = -1;
    	selectPostion = -1;
    	fillData();
    }
    
    /**
     * Borra todas las rutas
     * 
     */
    
    private void deleteAllRoutes() {
    	DataFramework.getInstance().emptyTable("routes");
    	DataFramework.getInstance().emptyTable("locations");
    	DataFramework.getInstance().emptyTable("resource");
    	fillData();
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
        menu.add(0, IMPORT_ID, 0,  R.string.menu_import)
		.setIcon(android.R.drawable.ic_menu_add);
        menu.add(0, DELETEALLROUTES_ID, 0,  R.string.menu_delete_all)
			.setIcon(android.R.drawable.ic_menu_delete);
        menu.add(0, FILTER_ID, 0,  R.string.menu_filter)
			.setIcon(android.R.drawable.ic_menu_view);
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
        case IMPORT_ID:
        	showDialog(DIALOG_IMPORT);
            return true;
        case FILTER_ID:
        	showDialog(DIALOG_FILTER);
            return true;
        case DELETEALLROUTES_ID:
        	showDialog(DIALOG_DELETE_ALL);
            return true;
        }
       
        return super.onMenuItemSelected(featureId, item);
    }
    
    /**
     * Rellena la lista con las rutas de la base de datos
     * 
     */
    
    private void fillData() {
    	try {
	    	String where = "";
	    	if (searchIdCategory>=0) {
	    		where += "category_id = " + searchIdCategory; 
	    	} 
	    	if (searchIdGroup>=0) {
	    		if (!where.equals("")) where += " AND ";
	    		where += "group_id = " + searchIdGroup;
	    	} 
	    	if (searchDate>=0) {
	    		String whereDate="";
	    		if (!whereDate.equals("")) where += " AND ";
	    		if (searchDate == 0) {
	    			whereDate += "date > '" + Utils.plusDate(-1) + "'";
	    		} else if (searchDate == 1) {
	    			whereDate += "date > '" + Utils.plusDate(-3) + "'";
	    		} else if (searchDate == 2) {
	    			whereDate += "date > '" + Utils.plusDate(-7) + "'";
	    		} else if (searchDate == 3) {
	    			whereDate += "date > '" + Utils.plusDate(-31) + "'";
	    		} else {
	    			whereDate = "";
	    		}
	    		where += whereDate;
	    	} 
	    	String order = "date";
	    	if (orderList>=0) {
	    		if (orderList == 0) {
	    			order = "date";
	    		} else if (orderList == 1) {
	    			order = "name";
	    		} else if (orderList == 2) {
	    			order = "average_speed";
	    		} else if (orderList == 3) {
	    			order = "distance";
	    		} else if (orderList == 4) {
	    			order = "time";
	    		}
	    	}
	    	RadioButton radAsc = (RadioButton)viewFilter.findViewById(R.id.type_order1);
	    	if (radAsc.isChecked()) {
	    		order += " asc";
	    	} else {
	    		order += " desc";
	    	}
	    	routes = new RowRouteAdapter(this, DataFramework.getInstance().getEntityList("routes", where, order));
	    	routes.setSelectId(selectId);
	        setListAdapter(routes);
	        if (selectId>=0) {
	        	this.getListView().setSelection(routes.getPositionById(selectId));
	        }
	        
	        //TextView total = (TextView)findViewById(R.id.total_routes);
	        this.setTitle(this.getString(R.string.list_routes) + " (" + routes.getCount() + " " +  this.getString(R.string.of) + " " + DataFramework.getInstance().getEntityList("routes").size() + ")");
	        	        
    	} catch (Exception e) {
    		System.out.println("ERROR: "+e.getMessage());
    	}
    	        
    }
    
    /**
     * Se ejecuta la nueva Actividad para editar la ruta
     * 
     */
    
    private void editRoute(long idSelected) {
    	if (idSelected>=0) {
    	    Intent iRouteEdit = new Intent(this, RouteEdit.class);
    	    iRouteEdit.putExtra(DataFramework.KEY_ID, idSelected);
    	    startActivityForResult(iRouteEdit, ACTIVITY_EDITROUTE);
    	}
    }
    
    private void recalculateTotals(long idSelected) {
    	Entity route = new Entity("routes", idSelected);
    	
    	Cursor c = DataFramework.getInstance().getCursor("locations", "route_id = " + idSelected, "position asc");
    	if (c.getCount()>0) {
	    	c.moveToFirst();
	    	float max_speed = 0;
	    	float distance = 0;
	    	int time = 0;
	    	Location lastLocation = null;
	    	while (!c.isAfterLast()) {
	    		
	    		Location loc = new Location(LocationManager.GPS_PROVIDER);
	    		loc.setLatitude(c.getFloat(c.getColumnIndex("latitude")));
	    		loc.setLongitude(c.getFloat(c.getColumnIndex("longitude")));
	    		loc.setAltitude(c.getFloat(c.getColumnIndex("altitude")));
	    		loc.setBearing(c.getFloat(c.getColumnIndex("bearing")));
	    		
	    		float speed = c.getFloat(c.getColumnIndex("speed"));
	    		if (speed > max_speed) max_speed = speed;
	    		
	    		if ( (lastLocation!=null) && (c.getInt(c.getColumnIndex("pause"))==0) ) {
	    			distance += lastLocation.distanceTo(loc);
	    		}
	    		
	    		time = c.getInt(c.getColumnIndex("time"));
	    		
	    		c.moveToNext();
	    		
	    		lastLocation = loc;
	    	}
	    	float kms = distance/1000;
	    	float hours = ((float)time/60)/60;
	    	route.setValue("average_speed", Utils.round(kms/hours, 2));
	    	route.setValue("max_speed", max_speed);
	    	route.setValue("coordinates_route", c.getCount());
	    	
	    	route.setValue("distance", distance);
	    	route.setValue("time", time);
    	}
    	c.close();

    	route.save();
    	
    	fillData();
    	
    	Utils.showMessage(getString(R.string.recalculate_rotals));
    	
    }
    
    /**
     * Se ejecuta la nueva Actividad para ver mas informacio de la ruta
     * 
     */
    
    private void viewMoreDataRoute(long idSelected) {
    	if (idSelected>=0) {
    	    Intent iRouteEdit = new Intent(this, TabViewInfo.class);
    	    iRouteEdit.putExtra(DataFramework.KEY_ID, idSelected);
    	    startActivity(iRouteEdit);
    	}
    }
    
    /**
     * Se ejecuta la nueva Actividad para mostrar la ruta
     * 
     */
    
    private void showRouteInWeb(long idSelected) {
    	if (idSelected>=0) {
    		Entity ent = new Entity("routes", idSelected);
    		if (ent.getString("url_yourtrainings").equals("")) {
    			Utils.showMessage(getString(R.string.route_no_web));
    		} else {
    			String name = ent.getEntity("category_id").getValue("name").toString() + "_txt";
    			String sport = "";
    			int id = getResources().getIdentifier(Utils.getPackage() + ":string/"+name, null, null);
    			if (id!=0) sport = getResources().getString(id);
    			
    			String text = this.getString(R.string.share_url, Utils.formatDistance(ent.getFloat("distance")), sport, ent.getString("url_yourtrainings"));
    			Intent msg=new Intent(Intent.ACTION_SEND);
	            msg.putExtra(Intent.EXTRA_TEXT, text);
	            msg.setType("text/plain");
	            startActivity(msg);
    		}
    	}
    }
    
    /**
     * Se ejecuta la nueva Actividad para mostrar la ruta
     * 
     */
    
    private void showRoute(long idSelected) {
    	if (idSelected>=0) {
    		Intent i = new Intent(this, ShowRoute.class);
    		i.putExtra(DataFramework.KEY_ID, idSelected);
    		this.startActivityForResult(i, ACTIVITY_SHOW);
    	}
    }
    
    /**
     * Se ejecuta la nueva Actividad para mostrar la ruta
     * 
     */
    
    private void showSimplifyRoute(long idSelected) {
    	if (idSelected>=0) {
    		Intent i = new Intent(this, ShowRoute.class);
    		i.putExtra(DataFramework.KEY_ID, idSelected);
    		i.putExtra("simplify", true);
    		this.startActivityForResult(i, ACTIVITY_SHOW);
    	}
    }
    
    /**
     * Se ejecuta cuando se pulsa sobre un registro de la lista
     * 
     * @param l ListView
     * @param v View
     * @param position Posicion
     * @param id Identificador
     * 
     */
    
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        
       	routes.clearSelectId();
        
        selectId = ((Entity)routes.getItem(position)).getId();
        selectPostion = position;
        
        routes.setSelectId(selectId);
        routes.setViewSelectId(v);
        
        TextView t = (TextView) v.findViewById(R.id.title);
		t.setTextColor(Color.rgb(0xea, 0xea,0x9c));
        
        showDialog(DIALOG_ITEM);
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        
        outState.putLong("searchIdCategory", searchIdCategory);
        outState.putLong("searchIdGroup", searchIdGroup);
        outState.putLong("searchDate", searchDate);
        outState.putLong("selectId", selectId);
        outState.putLong("selectPostion", selectPostion);
        outState.putLong("orderList", orderList);
        
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
        case ACTIVITY_SHOW:
    	case ACTIVITY_EDITROUTE:
    		if (resultCode == RESULT_OK) {
    			fillData();
    		}
    		break;
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
    }
    
    @Override
    protected void onResume() {
        super.onResume();		
		fillData();
		
    }
    
	@Override
	protected void onDestroy() {
		super.onDestroy();
		DataFramework.getInstance().close();
	}
    
}
