package com.javielinux.andando;

import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.dataframework.DataFramework;
import com.android.dataframework.Entity;

public class RowRouteAdapter extends BaseAdapter {

    private Context mContext;
    private List<Entity> elements;
    private long selectId = -1;
    private View viewSelectId = null;
	
    /**
     * Constructor - Adaptador que crea la vista de cada una de las
     * filas de la lista de rutas
     * 
     * @param mContext Context
     * @param elements Lista de elementos
     */
    
    public RowRouteAdapter(Context mContext, List<Entity> elements)
    {
        this.mContext = mContext;
        this.elements = elements;
    }
    
    /**
     * Numero de elementos en la lista
     * 
     * @return Numero de elementos
     */
    
	@Override
	public int getCount() {
		return elements.size();
	}
	
	public int getPositionById(long id) {
        for (int i=0; i<getCount(); i++) {
        	if ( ((Entity)getItem(i)).getId() == id ) {
        		return i;
        	}
        }
        return -1;
	}
	
    /**
     * Devuelve un objeto de la lista
     * 
     * @param position Posicion del elemento en la lista
     * @return Objeto
     */

	@Override
	public Object getItem(int position) {
        return elements.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
	
    /**
     * Devuelve la vista de la fila
     * 
     * @param position Posicion del elemento en la lista
     * @param convertView View
     * @param parent ViewGroup
     * @return Vista
     */

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Entity item = elements.get(position);
		
		long id = item.getId();
		long category_id = item.getEntity("category_id").getId();
		
		View v = null;
		
		if (null == convertView) {
			v = View.inflate(mContext, R.layout.routes_row, null);
		} else {
			v = convertView;
		}

        Drawable d = mContext.getResources().getDrawable(mContext.getResources().getIdentifier("com.javielinux.andando:drawable/category_" + category_id, null, null));
        
        ImageView img = (ImageView)v.findViewById(R.id.icon);
        img.setImageDrawable(d);
        
        TextView title = (TextView)v.findViewById(R.id.title);       
        title.setText(item.getString("name"));
        
        TextView distance = (TextView)v.findViewById(R.id.distance);       
        distance.setText(Utils.formatDistance(item.getFloat("distance")));
        
        TextView time = (TextView)v.findViewById(R.id.time);    
        time.setText(Utils.formatShortTime(item.getInt("time")));
        
        TextView group = (TextView)v.findViewById(R.id.group);    
        group.setText(item.getEntity("group_id").getString("name"));
        
        TextView ncoor = (TextView)v.findViewById(R.id.coordinates);    
        ncoor.setText(item.getInt("coordinates_route") + " " + Utils.context.getResources().getString(R.string.locations));
        
        TextView date = (TextView)v.findViewById(R.id.date);    
        date.setText(Utils.formatHumanDate(item.getString("date")));
        
        TextView marks = (TextView)v.findViewById(R.id.marks);
        Cursor c = DataFramework.getInstance().getCursor("resource", "route_id = "+item.getId(), "");
        marks.setText(c.getCount() + " " + Utils.context.getResources().getString(R.string.marks) );
        c.close();
        
        TextView in_web = (TextView)v.findViewById(R.id.in_web);
        if (item.getString("url_yourtrainings").equals("")) {
        	in_web.setText("");
        } else {
        	in_web.setText(mContext.getString(R.string.on_the_web));
        }
        
        if (selectId==id) {
        	viewSelectId = v;
        	title.setTextColor(Color.rgb(0xea, 0xea,0x9c));
        }
        return v;
	}

	public void setSelectId(long selectId) {
		this.selectId = selectId;
	}

	public void setViewSelectId(View viewSelectId) {
		this.viewSelectId = viewSelectId;
	}
	
    /**
     * Limpiar fila seleccionada
     * 
     */
    
    public void clearSelectId() {
    	if ( (selectId>=0) && (viewSelectId!=null)) {
   			TextView t = (TextView) viewSelectId.findViewById(R.id.title);
    		t.setTextColor(Color.WHITE);
    		viewSelectId = null;
    		selectId = -1;
    	}
    }

}
