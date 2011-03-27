package com.javielinux.andando;

import android.database.Cursor;
import android.os.Handler;
import android.os.Message;

import com.android.dataframework.Entity;
import com.android.dataframework.DataFramework;

public class ThreadSimplify implements Runnable {

	private long idRoute;
	private RoutesList mActivity;
	private String msgToWrite;
	
    /**
     * Constructor - Este Thread exporta a diferentes formatos
     * 
     * @param mActivity Actividad
     * @param idRoute Id de la ruta
     */
	
	public ThreadSimplify(RoutesList mActivity, long idRoute) {
		this.mActivity = mActivity;
		this.idRoute = idRoute;
	}
	
	@Override
	public void run() {
		simplify();
		handler.sendEmptyMessage(1);
	}
	
    /**
     * Crea el contenido del archivo XML
     * 
     */
    
    private void simplify() {
    	
    	setMessage(R.string.dialog_simplify);
    	
    	Cursor c = DataFramework.getInstance().getCursor("locations", "route_id = " + idRoute, "position asc");
		c.moveToFirst();
		int count = 0;
		int pos = 0;
		while (!c.isAfterLast()) {
			long id = c.getLong(c.getColumnIndex(DataFramework.KEY_ID));
			Entity ent = new Entity("locations", id);
			if ( (count%2==0) || c.isLast() || c.getInt(c.getColumnIndex("pause"))==1 ) {
				ent.setValue("position", pos);
				ent.save();
				pos++;
			} else {
				ent.delete();
			}
			count++;
			c.moveToNext();
			handler.sendEmptyMessage(2);
		}
		
		Entity ent = new Entity("routes", idRoute);
		ent.setValue("coordinates_route", pos);
		ent.save();
		
    }
    
	public void setMessage(int identifier) {
		msgToWrite = mActivity.getResources().getString(identifier);
		handler.sendEmptyMessage(0);
	}

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == 0) {
				mActivity.writeInProgressDialog(msgToWrite);
			}
			if (msg.what == 1) {
				mActivity.endSimplify();
			}
			if (msg.what == 2) {
				mActivity.incrementProgress();
			}
		}
	};
	
}
