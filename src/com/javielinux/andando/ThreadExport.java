package com.javielinux.andando;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import android.database.Cursor;
import android.os.Handler;
import android.os.Message;

import com.android.dataframework.DataFramework;
import com.android.dataframework.Entity;
import com.javielinux.BubblesMap.view.MapLocation;

public class ThreadExport implements Runnable {

	private static final int BUFFER = 16384;
	
	public static final int TYPE_KML = 0;
	public static final int TYPE_ANDANDO = 1;
	public static final int TYPE_GPX = 2;
	public static final int TYPE_KMZ = 3;
	public static final int TYPE_YOURTRAININGS = 4;
	
	private long idRoute;
	private int type = TYPE_KML;
	private boolean sendByEmail;
	private boolean writeDefaultDataInEmail;
	private RoutesList mActivity;
	
	private boolean cancel = false;
	
	private String msgToWrite;
	
	private String file;
	
    /**
     * Constructor - Este Thread exporta a diferentes formatos
     * 
     * @param mActivity Actividad
     * @param type Tipo
     * @param idRoute Id de la ruta
     * @param sendByEmail Si se envia por email
     */
	
	public ThreadExport(RoutesList mActivity, int type, long idRoute, boolean sendByEmail, boolean writeDefaultDataInEmail) {
		this.mActivity = mActivity;
		this.type = type;
		this.idRoute = idRoute;
		this.sendByEmail = sendByEmail;
		this.writeDefaultDataInEmail = writeDefaultDataInEmail;
		cancel = false;
	}
	
	@Override
	public void run() {
		if (type == TYPE_KML) {
			file = export_KML(false);
			handler.sendEmptyMessage(1);
		} else if (type == TYPE_GPX) {
			try {
				file = export_GPX();
			} catch (ParseException e) {
				e.printStackTrace();
			}
			handler.sendEmptyMessage(1);
		} else if (type == TYPE_KMZ) {
			file = export_KMZ();
			handler.sendEmptyMessage(1);
		} else if (type == TYPE_YOURTRAININGS) {
			try {
				if (export_YourTrainings()) {
					handler.sendEmptyMessage(3);
					return;
				}
			} catch (ParseException e) {
				e.printStackTrace();
			}
			handler.sendEmptyMessage(4);
		} else {
			file = export_Andando();
			handler.sendEmptyMessage(1);
		}
	}
	
    /**
     * Cancela el proceso
     * 
     */
	
	public void cancel (){
		cancel = true;
	}
	
	/**
     * Exporta una ruta a ZIP en el directorio de la aplicacion
	 * @throws ParseException  
     * 
     * 
     */

	public boolean export_YourTrainings() throws ParseException {
		
    	
    		return false;
        
	}

    /**
     * Exporta una ruta a ZIP en el directorio de la aplicacion
     * 
     * 
     */

	public String export_Andando(){
		String fZip = Utils.appDirectory+Utils.formatNameRoute(idRoute)+".ndn";
		try {
						
			String fXml = "route.xml";
			
			setMessage(R.string.dialog_export_creating_files);
			
			BufferedInputStream origin = null;
			FileOutputStream dest = new FileOutputStream(fZip);
			ZipOutputStream out = new ZipOutputStream( new BufferedOutputStream(dest));
			
			byte data[] = new byte[BUFFER];
			
    		File file = new File(Utils.appDirectory+fXml);
    		if (file.exists()) file.delete();
    		FileOutputStream fOut = new FileOutputStream(Utils.appDirectory+fXml); 
    		OutputStreamWriter osw = new OutputStreamWriter(fOut); 
    		
    		setMessage(R.string.dialog_export_creating_xml);
    		
    		Entity route = new Entity("routes", idRoute);
    		
            osw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
        			+ "<andando>\n"
        			+ "<data>\n"
        			+ "\t<name value=\"" + route.getValue("name").toString() + "\" />\n"
        			+ "\t<description value=\"" + route.getValue("description").toString() + "\" />\n"
        			+ "\t<category value=\"" + route.getEntity("category_id").getId() + "\" />\n"
        			+ "\t<date value=\"" + route.getValue("date").toString() + "\" />\n"
        			+ "\t<distance value=\"" + route.getValue("distance").toString() + "\" />\n"
        			+ "\t<time value=\"" + route.getValue("time").toString() + "\" />\n"
        			+ "\t<average-speed value=\"" + route.getValue("average_speed").toString() + "\" />\n"
        			+ "\t<max-speed value=\"" + route.getValue("max_speed").toString() + "\" />\n"
        			+ "\t<coordinates-route value=\"" + route.getValue("coordinates_route").toString() + "\" />\n"
        			+ "\t<divide-time value=\"" + route.getValue("divide_time").toString() + "\" />\n"
        			+ "\t<divide-distance value=\"" + route.getValue("divide_distance").toString() + "\" />\n"
        			+ "</data>\n");
            
            Cursor c = DataFramework.getInstance().getCursor("locations", "route_id = " + idRoute, "position asc");
    		c.moveToFirst();
        	
        	if (!c.isAfterLast()) {
        		    	
    	    	// coordenadas
        		osw.append("<locations>\n");
    			while (!c.isAfterLast()) {
    				osw.append("\t<location>\n"
    						+ "\t\t<latitude value=\"" + c.getFloat(c.getColumnIndex("latitude")) + "\" />\n"
    						+ "\t\t<longitude value=\"" + c.getFloat(c.getColumnIndex("longitude")) + "\" />\n"
    						+ "\t\t<altitude value=\"" + c.getFloat(c.getColumnIndex("altitude")) + "\" />\n"
    						+ "\t\t<position value=\"" + c.getInt(c.getColumnIndex("position")) + "\" />\n"						
    						+ "\t\t<distance value=\"" + c.getFloat(c.getColumnIndex("distance")) + "\" />\n"
    						+ "\t\t<speed value=\"" + c.getFloat(c.getColumnIndex("speed")) + "\" />\n"
    						+ "\t\t<bearing value=\"" + c.getInt(c.getColumnIndex("bearing")) + "\" />\n"
    						+ "\t\t<accuracy value=\"" + c.getFloat(c.getColumnIndex("accuracy")) + "\" />\n"
    						+ "\t\t<time value=\"" + c.getInt(c.getColumnIndex("time")) + "\" />\n"
    						+ "\t\t<pause value=\"" + c.getInt(c.getColumnIndex("pause")) + "\" />\n"
    						+ "\t</location>\n");
    				c.moveToNext();
    				handler.sendEmptyMessage(2);
    			}
    			osw.append("</locations>\n");
    			c.close();
    			
    	    	// recursos
    	    	
    			c = DataFramework.getInstance().getCursor("resource", "route_id = " + idRoute, "");
    			c.moveToFirst();
    			
    			osw.append("<resources>\n");
    			while (!c.isAfterLast()) {
    				osw.append("\t<resource>\n"
    					+ "\t\t<text value=\"" + c.getString(c.getColumnIndex("text")) + "\" />\n"
    					+ "\t\t<file value=\"" + c.getString(c.getColumnIndex("file")) + "\" />\n"
    					+ "\t\t<type-resource-id value=\"" + c.getInt(c.getColumnIndex("type_resource_id")) + "\" />\n"
    					+ "\t\t<latitude value=\"" + c.getFloat(c.getColumnIndex("latitude")) + "\" />\n"
    					+ "\t\t<longitude value=\"" + c.getFloat(c.getColumnIndex("longitude")) + "\" />\n"
    					+ "\t\t<altitude value=\"" + c.getFloat(c.getColumnIndex("altitude")) + "\" />\n"
    					+ "\t</resource>\n");
    				c.moveToNext();
    			}
    			osw.append("</resources>\n");
    			c.close();
        	}
        	            
            osw.append("</andando>\n");
            
            osw.flush();
            
            osw.close();
            
            setMessage(R.string.dialog_export_packaging);
            
			FileInputStream fi = new FileInputStream(Utils.appDirectory+fXml);
			origin = new BufferedInputStream(fi, BUFFER);
			ZipEntry entry = new ZipEntry(fXml);
			out.putNextEntry(entry);
			int count;
			while((count = origin.read(data, 0, BUFFER))!= -1){
				out.write(data, 0, count);                        
			}
			origin.close();

			// borro archivo XML
    		if (file.exists()) file.delete();
    		
	    	// recursos
	    	
			c = DataFramework.getInstance().getCursor("resource", "route_id = " + idRoute, "");
			c.moveToFirst();
			
			if (!c.isAfterLast()) {
				setMessage(R.string.dialog_export_packaging_images);
			}
			
			while (!c.isAfterLast()) {
				int type = c.getInt(c.getColumnIndex("type_resource_id"));
				String filec = c.getString(c.getColumnIndex("file"));
				
				if (type != MapLocation.TYPE_BUBBLE_MSG) {
					fi = new FileInputStream(Utils.appDirectory+filec);
					origin = new BufferedInputStream(fi, BUFFER);
					entry = new ZipEntry(filec);
					out.putNextEntry(entry);
					//int count;
					while((count = origin.read(data, 0, BUFFER))!= -1){
						out.write(data, 0, count);                        
					}
					origin.close();
				}
				
				c.moveToNext();
			}
			
			setMessage(R.string.dialog_export_finish);
			
			c.close();
						
			out.close();

		} catch(Exception e) {
			e.printStackTrace();
		}   
		return fZip;
	}
	
    /**
     * Exporta una ruta a KMZ
     * 
     * 
     */

	public String export_KMZ(){		
		String fZip = Utils.appDirectory+Utils.formatNameRoute(idRoute)+".kmz";
		try {
						
			String fXml = "doc.kml";
			
			setMessage(R.string.dialog_export_creating_files);
			
			BufferedInputStream origin = null;
			FileOutputStream dest = new FileOutputStream(fZip);
			ZipOutputStream out = new ZipOutputStream( new BufferedOutputStream(dest));
			
			byte data[] = new byte[BUFFER];
			
			export_KML(true);
            
            setMessage(R.string.dialog_export_packaging);
                       
			FileInputStream fi = new FileInputStream(Utils.appDirectory+fXml);
			origin = new BufferedInputStream(fi, BUFFER);
			ZipEntry entry = new ZipEntry(fXml);
			out.putNextEntry(entry);
			int count;
			while((count = origin.read(data, 0, BUFFER))!= -1){
				out.write(data, 0, count);                        
			}
			origin.close();

			// borro archivo XML
			File file = new File(Utils.appDirectory+fXml);
    		if (file.exists()) file.delete();
    		
	    	// recursos
	    	
			Cursor c = DataFramework.getInstance().getCursor("resource", "route_id = " + idRoute, "");
			c.moveToFirst();
			
			if (!c.isAfterLast()) {
				setMessage(R.string.dialog_export_packaging_images);
			}
			
			while (!c.isAfterLast()) {
				int type = c.getInt(c.getColumnIndex("type_resource_id"));
				String filec = c.getString(c.getColumnIndex("file"));
				
				if (type == MapLocation.TYPE_BUBBLE_PHOTO) {
					fi = new FileInputStream(Utils.appDirectory+filec);
					origin = new BufferedInputStream(fi, BUFFER);
					entry = new ZipEntry(filec);
					out.putNextEntry(entry);
					//int count;
					while((count = origin.read(data, 0, BUFFER))!= -1){
						out.write(data, 0, count);                        
					}
					origin.close();
				}
				
				c.moveToNext();
			}
			
			setMessage(R.string.dialog_export_finish);
			
			c.close();
						
			out.close();

		} catch(Exception e) {
			e.printStackTrace();
		}   
		return fZip;
	}

    /**
     * Exporta una ruta a KML en el directorio de la aplicacion
     * 
     * @param idRoute Codigo de la ruta a exportar
     * 
     */

	public String export_KML(boolean forKMZ){
		
		String f = "";
		if (forKMZ)		
			f = Utils.appDirectory+"doc.kml";
		else
			f = Utils.appDirectory+Utils.formatNameRoute(idRoute)+".kml";
		
		try {
			setMessage(R.string.dialog_export_creating_files);
			
    		File file = new File(f);
    		if (file.exists()) file.delete();
    		FileOutputStream fOut = new FileOutputStream(f); 
    		OutputStreamWriter osw = new OutputStreamWriter(fOut); 
    		
        	setMessage(R.string.dialog_export_creating_kml);
        	
    		Entity route = new Entity("routes", idRoute);
        	
    		osw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
        			+ "<kml xmlns=\"http://earth.google.com/kml/2.0\">\n"
        			+ "<Document>\n"
        			+ "\t<name><![CDATA[" + route.getValue("name").toString() + "]]></name>\n"
        			+ "\t<description><![CDATA[" + route.getValue("description").toString() + "]]></description>\n"
        			+ "\t<Style id=\"track\"><LineStyle><color>7f0000ff</color><width>4</width></LineStyle></Style>\n"
        			+ "\t<Style id=\"sh_grn-circle\"><IconStyle><scale>1.3</scale><Icon><href>http://maps.google.com/mapfiles/kml/paddle/grn-circle.png</href></Icon><hotSpot x=\"32\" y=\"1\" xunits=\"pixels\" yunits=\"pixels\"/></IconStyle></Style>\n"
        			+ "\t<Style id=\"sh_red-circle\"><IconStyle><scale>1.3</scale><Icon><href>http://maps.google.com/mapfiles/kml/paddle/red-circle.png</href></Icon><hotSpot x=\"32\" y=\"1\" xunits=\"pixels\" yunits=\"pixels\"/></IconStyle></Style>\n"
        			+ "\t<Style id=\"sh_blue-circle\"><IconStyle><scale>1.3</scale><Icon><href>http://maps.google.com/mapfiles/kml/paddle/blu-circle.png</href></Icon><hotSpot x=\"32\" y=\"1\" xunits=\"pixels\" yunits=\"pixels\"/></IconStyle></Style>\n"
        			+ "\t<Style id=\"sh_ylw-pushpin\"><IconStyle><scale>1.3</scale><Icon><href>http://maps.google.com/mapfiles/kml/pushpin/ylw-pushpin.png</href></Icon><hotSpot x=\"20\" y=\"2\" xunits=\"pixels\" yunits=\"pixels\"/></IconStyle></Style>\n"
        			+ "\t<Style id=\"sh_blue-pushpin\"><IconStyle><scale>1.3</scale><Icon><href>http://maps.google.com/mapfiles/kml/pushpin/blue-pushpin.png</href></Icon><hotSpot x=\"20\" y=\"2\" xunits=\"pixels\" yunits=\"pixels\"/></IconStyle></Style>\n");

    		Cursor cursorRoute = DataFramework.getInstance().getCursor("locations", "route_id = " + idRoute, "position asc");
        	cursorRoute.moveToFirst();
        	
        	if (!cursorRoute.isAfterLast()) {
        	
    	    	// marca para empezar
        		
        		cursorRoute.moveToFirst();
    	    		    	
    	    	String coords = cursorRoute.getFloat(cursorRoute.getColumnIndex("longitude")) + "," + cursorRoute.getFloat(cursorRoute.getColumnIndex("latitude")) + "," + cursorRoute.getFloat(cursorRoute.getColumnIndex("altitude"));
    	    	
    	    	osw.append("\t<Placemark>\n"
    	    			+ "\t\t<name><![CDATA[(" + mActivity.getString(R.string.start) + ")]]></name>\n"
    	    			+ "\t\t<description><![CDATA[" + mActivity.getString(R.string.route_start) + "]]></description>\n"
    	    			+ "\t\t<styleUrl>#sh_grn-circle</styleUrl>\n"
    	    			+ "\t\t<Point>\n"
    	    			+ "\t\t<coordinates>" + coords + "</coordinates>\n"
    	    			+ "\t\t</Point>\n"
    	    			+ "\t</Placemark>\n");
    	    	
    	    	// coordenadas
    	    	
    	    	osw.append("\t<Placemark>\n"
    	    			+ "\t\t<name><![CDATA[" + route.getValue("name").toString() + "]]></name>\n"
    	    			+ "\t\t<description><![CDATA[" + route.getValue("description").toString() + "]]></description>\n"
    	    			+ "\t\t<styleUrl>#track</styleUrl>\n"
    	    			+ "\t\t<MultiGeometry>\n"
    	    			+ "\t\t<LineString><coordinates>\n");
    	    	
    			while (!cursorRoute.isAfterLast()) {
    				osw.append(cursorRoute.getFloat(cursorRoute.getColumnIndex("longitude")) + "," + cursorRoute.getFloat(cursorRoute.getColumnIndex("latitude")) + "," + cursorRoute.getFloat(cursorRoute.getColumnIndex("altitude")) + " ");
    				cursorRoute.moveToNext();
    				handler.sendEmptyMessage(2);
    			}
    	    		
    	    	
    			osw.append("\t\t</coordinates></LineString>\n"
    	    			+ "\t\t</MultiGeometry>\n"
    	    			+ "\t</Placemark>\n");
    			
    			// pausas
    			
    			cursorRoute.moveToFirst();
    			
    			while (!cursorRoute.isAfterLast()) {

    				String coordsPause = cursorRoute.getFloat(cursorRoute.getColumnIndex("longitude")) + "," + cursorRoute.getFloat(cursorRoute.getColumnIndex("latitude")) + "," + cursorRoute.getFloat(cursorRoute.getColumnIndex("altitude"));
    				if (cursorRoute.getInt(cursorRoute.getColumnIndex("pause")) == 1) {
		    	    	osw.append("\t<Placemark>\n"
		    	    			+ "\t\t<name><![CDATA[(" + mActivity.getString(R.string.continued) + ")]]></name>\n"
		    	    			+ "\t\t<description><![CDATA[" + mActivity.getString(R.string.route_continue) + "]]></description>\n"
		    	    			+ "\t\t<styleUrl>#sh_blue-circle</styleUrl>\n"
		    	    			+ "\t\t<Point>\n"
		    	    			+ "\t\t<coordinates>" + coordsPause + "</coordinates>\n"
		    	    			+ "\t\t</Point>\n"
		    	    			+ "\t</Placemark>\n");
    				}
    				
	    	    	cursorRoute.moveToNext();
	    	    	
	    	    	if (!cursorRoute.isAfterLast()) {
	    				if (cursorRoute.getInt(cursorRoute.getColumnIndex("pause")) == 1) {
			    	    	osw.append("\t<Placemark>\n"
			    	    			+ "\t\t<name><![CDATA[(" + mActivity.getString(R.string.pause) + ")]]></name>\n"
			    	    			+ "\t\t<description><![CDATA[" + mActivity.getString(R.string.route_pause) + "]]></description>\n"
			    	    			+ "\t\t<styleUrl>#sh_blue-circle</styleUrl>\n"
			    	    			+ "\t\t<Point>\n"
			    	    			+ "\t\t<coordinates>" + coordsPause + "</coordinates>\n"
			    	    			+ "\t\t</Point>\n"
			    	    			+ "\t</Placemark>\n");
	    				}
	    	    	}
    			}
    	    	
    	    	// recursos
    	    	
    			Cursor c = DataFramework.getInstance().getCursor("resource", "route_id = " + idRoute, "");
    			c.moveToFirst();
    			while (!c.isAfterLast()) {
    				int type = c.getInt(c.getColumnIndex("type_resource_id"));
    				String text = c.getString(c.getColumnIndex("text"));
    				
    				float lat = c.getFloat(c.getColumnIndex("latitude"));
    	    		float lon = c.getFloat(c.getColumnIndex("longitude"));
    	    		float alt = c.getFloat(c.getColumnIndex("altitude"));
    	    		
    	    		osw.append("\t<Placemark>\n"
    		    			+ "\t\t<name><![CDATA[]]></name>\n"
    		    			+ "\t\t<description><![CDATA[");
    		    	if (type == MapLocation.TYPE_BUBBLE_MSG) {
    		    		osw.append(text);
    		    	} else if (type == MapLocation.TYPE_BUBBLE_PHOTO) {
    		    		if (forKMZ)
    		    			osw.append("<img src=\""+ c.getString(c.getColumnIndex("file")) +"\" width=\"192px\"/><br/>");
    		    		else
    		    			osw.append(mActivity.getString(R.string.original_route_image));
    		    	}
    		    	osw.append("]]></description>\n"
    		    			+ "\t\t<styleUrl>#sh_blue-pushpin</styleUrl>\n"
    		    			+ "\t\t<Point>\n"
    		    			+ "\t\t<coordinates>" + lon + "," + lat + "," + alt + "</coordinates>\n"
    		    			+ "\t\t</Point>\n"
    		    			+ "\t</Placemark>\n");

    				c.moveToNext();
    			}
    			c.close();
    	    	// marca para finalizar
    	    	
        		cursorRoute.moveToLast();
    	    		    	
    	    	coords = cursorRoute.getFloat(cursorRoute.getColumnIndex("longitude")) + "," + cursorRoute.getFloat(cursorRoute.getColumnIndex("latitude")) + "," + cursorRoute.getFloat(cursorRoute.getColumnIndex("altitude"));
    	    	
    	    	osw.append("\t<Placemark>\n"
    	    			+ "\t\t<name><![CDATA[(" + mActivity.getString(R.string.end) + ")]]></name>\n"
    	    			+ "\t\t<description><![CDATA["+ Utils.formatDistance(Float.parseFloat(route.getValue("distance").toString())) + " " + mActivity.getString(R.string.rides)
    	    			+ " " + Utils.formatTime(Long.parseLong(route.getValue("time").toString())) + "]]></description>\n"
    	    			+ "\t\t<styleUrl>#sh_red-circle</styleUrl>\n"
    	    			+ "\t\t<Point>\n"
    	    			+ "\t\t<coordinates>" + coords + "</coordinates>\n"
    	    			+ "\t\t</Point>\n"
    	    			+ "\t</Placemark>\n");
        	
        	}
        	cursorRoute.close();
        	
        	osw.append("</Document>\n");
        	osw.append("</kml>\n");
        	
            
            osw.flush();
            
            setMessage(R.string.dialog_export_finish);

			osw.close();
    	} catch (IOException ioe) {
    		ioe.printStackTrace();
    	}
    	return f;
	}
	
	/**
     * Exporta una ruta a KML en el directorio de la aplicacion
     * 
     * @param idRoute Codigo de la ruta a exportar
	 * @throws ParseException 
     * 
     */

	public String export_GPX() throws ParseException{
		String f = Utils.appDirectory+Utils.formatNameRoute(idRoute)+".gpx";
		try {
			setMessage(R.string.dialog_export_creating_files);
			
    		File file = new File(f);
    		if (file.exists()) file.delete();
    		FileOutputStream fOut = new FileOutputStream(f); 
    		OutputStreamWriter osw = new OutputStreamWriter(fOut);
    		
        	setMessage(R.string.dialog_export_creating_gpx);
        	
    		Entity route = new Entity("routes", idRoute);
        	
    		osw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
    				+ "<?xml-stylesheet type=\"text/xsl\" href=\"details.xsl\"?>\n"
        			+ "<gpx version=\"1.0\""
        			+ " creator=\"AndAndo for Android\""
        			+ " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
        			+ " xmlns=\"http://www.topografix.com/GPX/1/0\""
        			+ " xmlns:topografix=\"http://www.topografix.com/GPX/Private/TopoGrafix/0/1\""
        			+ " xsi:schemaLocation=\"http://www.topografix.com/GPX/1/0 http://www.topografix.com/GPX/1/0/gpx.xsd http://www.topografix.com/GPX/Private/TopoGrafix/0/1 http://www.topografix.com/GPX/Private/TopoGrafix/0/1/topografix.xsd\">\n"
        			+ "<trk>\n"
        			+ "<name><![CDATA[" + route.getValue("name").toString() + "]]></name>\n"
        			+ "<desc><![CDATA[" + route.getValue("description").toString() + "]]></desc>\n"
        			+ "<number></number>\n"
        			+ "<topografix:color>ff0000</topografix:color>\n"
        			+ "<trkseg>\n");

    		Cursor cursorRoute = DataFramework.getInstance().getCursor("locations", "route_id = " + idRoute, "position asc");
        	cursorRoute.moveToFirst();
        	
        	if (!cursorRoute.isAfterLast()) {
        	
    	    	// marca para empezar
        		
        		cursorRoute.moveToFirst();
    	    		    	    	    	
    			while (!cursorRoute.isAfterLast()) {
    				osw.append("<trkpt lat=\"" + cursorRoute.getFloat(cursorRoute.getColumnIndex("latitude")) + "\" lon=\"" + cursorRoute.getFloat(cursorRoute.getColumnIndex("longitude")) + "\">\n"
    						+ "<ele>" + cursorRoute.getFloat(cursorRoute.getColumnIndex("altitude")) + "</ele>\n"
    						+ "<time>" + Utils.plusSeconds(route.getString("date"), cursorRoute.getInt(cursorRoute.getColumnIndex("time"))) + "</time>\n"
    						+ "</trkpt>\n");
    				cursorRoute.moveToNext();
    				handler.sendEmptyMessage(2);
    			}
    	    		
    	    	
    			osw.append("\t\t</trkseg>\n"
    	    			+ "\t</trk>\n"
    	    			+ "</gpx>\n");
    	    	
        	}        	
            
            osw.flush();
            
            setMessage(R.string.dialog_export_finish);

			osw.close();
    	} catch (IOException ioe) {
    		ioe.printStackTrace();
    	}
    	return f;
	}

	
	public String getFile() {
		return file;
	}
	
	public void setMessage(int identifier) {
		msgToWrite = mActivity.getResources().getString(identifier);
		handler.sendEmptyMessage(0);
	}
	
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (!cancel) {
				if (msg.what == 0) {
					mActivity.writeInProgressDialog(msgToWrite);
				}
				if (msg.what == 1) {
					mActivity.endExport(type, sendByEmail, file, writeDefaultDataInEmail);
				}
				if (msg.what == 2) {
					mActivity.incrementProgress();
				}
				
				if (msg.what == 3) { // exportada a YourTrainings bien
					mActivity.endExportYourTrainings(true);
				}
				
				if (msg.what == 4) { // exportada a YourTrainings mal
					mActivity.endExportYourTrainings(false);
				}
			}
		}
	};
	
}
