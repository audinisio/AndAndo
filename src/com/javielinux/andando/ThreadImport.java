package com.javielinux.andando;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.os.Message;

import com.android.dataframework.Entity;

public class ThreadImport implements Runnable {

	public static final int TYPE_KML = 0;
	public static final int TYPE_ANDANDO = 1;
	
	static final int BUFFER = 16384;
	
	private String file;
	private int type = TYPE_KML;
	
	private RoutesList mActivity;
	
	private int identifierOutText;
	
	private boolean deleteToFinish;
	
    /**
     * Constructor - Este Thread exporta a diferentes formatos
     * 
     * @param mActivity Actividad
     * @param type Tipo
     * @param idRoute Id de la ruta
     * @param sendByEmail Si se envía por email
     */
	
	public ThreadImport(RoutesList mActivity, int type, String file, boolean deleteToFinish) {
		this.mActivity = mActivity;
		this.type = type;
		this.file = file;
		this.deleteToFinish = deleteToFinish;
	}
	
	@Override
	public void run() {
		if (type == TYPE_KML) {
			import_KML();
		} else {
			import_Andando();
		}
	}
	
    /**
     * Importa una ruta a ZIP
     * 
     * @param filename Archivo ZIP a importar
     * 
     */
    
	public void import_KML() {
		try {
			String filename = Utils.appDirectory+file;
			try {
				XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
				XmlPullParser x = (XmlPullParser)factory.newPullParser();
				x.setInput(new FileReader(filename));
				int eventType = x.getEventType();
				
				boolean hasName = false;
				boolean isLineString = false;
								
				Entity routeEntity = new Entity("routes");
				routeEntity.setValue("name", mActivity.getString(R.string.route) + " KML");
				routeEntity.setValue("date", Utils.now());
				routeEntity.setValue("description", "");
				routeEntity.setValue("divide_time", Integer.parseInt(Utils.preference.getString("prf_time_marks", "120")));
				routeEntity.setValue("divide_distance", Integer.parseInt(Utils.preference.getString("prf_distance_marks", "250")));
				routeEntity.setValue("category_id", 7);
				routeEntity.setValue("group_id", 1);
				routeEntity.save();
								
				while (eventType != XmlPullParser.END_DOCUMENT) {
					if (x.getName()==null) {
						eventType = x.next();
						continue;
					}
					
					System.out.println("NAME: " + x.getName());
					
					if ( eventType == XmlPullParser.START_TAG ) {
						if ( (x.getName().toLowerCase().equals("name")) && !hasName) {
							routeEntity.setValue("name", x.nextText());
							routeEntity.save();
							hasName = true;
						}
						if (x.getName().toLowerCase().equals("linestring")) {
							isLineString = true;
						}
						if ( (x.getName().toLowerCase().equals("coordinates")) && isLineString ) {
							String[] str = x.nextText().split("[ \t\n]");
							int count = 0;
							float distance = 0;
							Location auxLoc = null;
							for (int i=0;i<str.length; i++) {
								String chain = str[i].trim();
								if (!chain.equals("")) {
									String[] coords = chain.split(",");
									Entity locationEntity = new Entity("locations");
									locationEntity.setValue("longitude", Float.parseFloat(coords[0]));
									locationEntity.setValue("latitude", Float.parseFloat(coords[1]));
									if (coords.length>1) locationEntity.setValue("altitude", Float.parseFloat(coords[2]));
									locationEntity.setValue("position", i);
									locationEntity.setValue("route_id", routeEntity);
									
									Location loc = new Location(LocationManager.GPS_PROVIDER);
									loc.setLatitude(Float.parseFloat(coords[1]));
						    		loc.setLongitude(Float.parseFloat(coords[0]));
						    		if (coords.length>1) loc.setAltitude(Float.parseFloat(coords[2]));
						    		
						    		if (auxLoc!=null) distance += auxLoc.distanceTo(loc);
						    		
						    		locationEntity.setValue("distance", distance);
						    		
						    		
									locationEntity.save();
									count++;
									auxLoc = loc;
								}
							}
							routeEntity.setValue("coordinates_route", count);
							routeEntity.setValue("distance", distance);
							routeEntity.save();
						}
					}
					
					if ( eventType == XmlPullParser.END_TAG ) {
						if (x.getName().toLowerCase().equals("linestring")) {
							isLineString = false;
						}
					}
					
					eventType = x.next();
					
				}
				
				identifierOutText = R.string.import_correctly;
				
			} catch (Exception e) {
				e.printStackTrace();
				identifierOutText = R.string.import_error;
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			identifierOutText = R.string.import_error;
		}
		
		handler.sendEmptyMessage(0);
	}
	
    /**
     * Importa una ruta a ZIP
     * 
     * @param filename Archivo ZIP a importar
     * 
     */
    
	public void import_Andando() {
		try {
			String filename = Utils.appDirectory+file;
			ZipFile zip = new ZipFile(filename);
			Enumeration<? extends ZipEntry> zippedFiles = zip.entries();
			while (zippedFiles.hasMoreElements()) {
				ZipEntry entry = zippedFiles.nextElement();
				InputStream is = zip.getInputStream(entry);
				String name = entry.getName();
				File outputFile = new File(Utils.appDirectory + name);
				/*String outputPath = outputFile.getCanonicalPath();
				name = outputPath.substring(outputPath.lastIndexOf("/") + 1);
				outputPath = outputPath.substring(0, outputPath.lastIndexOf("/"));
				File outputDir = new File(outputPath);
				outputDir.mkdirs();
				outputFile = new File(outputPath, name);*/
				outputFile.createNewFile();
				FileOutputStream out = new FileOutputStream(outputFile);
				
				byte buf[] = new byte[BUFFER];
				do {
					int numread = is.read(buf);
					if (numread <= 0) {
						break;
					} else {
						out.write(buf, 0, numread);
					}
				} while (true);
				
				is.close();
				out.close();
			}
			//File theZipFile = new File(filename);
			//theZipFile.delete();
			
			String filenameRoute = Utils.appDirectory+"route.xml";
			try {
				XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
				XmlPullParser x = (XmlPullParser)factory.newPullParser();
				x.setInput(new FileReader(filenameRoute));
				int eventType = x.getEventType();
				
				boolean dataElement = false;
				boolean locationElement = false;
				boolean resourceElement = false;
				Entity routeEntity = new Entity("routes");
				Entity locationEntity = new Entity("locations");
				Entity resourceEntity = new Entity("resource");
				
				while (eventType != XmlPullParser.END_DOCUMENT) {
					if (x.getName()==null) {
						eventType = x.next();
						continue;
					}
					if ( eventType == XmlPullParser.START_TAG ) {
						if (x.getName().equals("data")) {
							dataElement = true;
							routeEntity = new Entity("routes");
							routeEntity.setValue("group_id", new Entity("groups", Long.parseLong("1")));
						}
						if (x.getName().equals("location")) {
							locationElement = true;
							locationEntity = new Entity("locations");
						}
						if (x.getName().equals("resource")) {
							resourceElement = true;
							resourceEntity = new Entity("resource");
						}
						
						if (dataElement) {
							if (x.getName().equals("name")) {
								routeEntity.setValue("name", x.getAttributeValue(null, "value").toString());
							}
							if (x.getName().equals("description")) {
								routeEntity.setValue("description", x.getAttributeValue(null, "value").toString() );
							}
							if (x.getName().equals("category")) {
								routeEntity.setValue("category_id", new Entity("categories", Long.parseLong(x.getAttributeValue(null, "value").toString())));
							}
							if (x.getName().equals("date")) {
								routeEntity.setValue("date", x.getAttributeValue(null, "value").toString() );
							}
							if (x.getName().equals("distance")) {
								routeEntity.setValue("distance", Float.parseFloat(x.getAttributeValue(null, "value").toString()));
							}
							if (x.getName().equals("time")) {
								routeEntity.setValue("time", Integer.parseInt(x.getAttributeValue(null, "value").toString()));
							}
							if (x.getName().equals("average-speed")) {
								routeEntity.setValue("average_speed", Float.parseFloat(x.getAttributeValue(null, "value").toString()));
							}
							if (x.getName().equals("max-speed")) {
								routeEntity.setValue("max_speed", Float.parseFloat(x.getAttributeValue(null, "value").toString()));
							}
							if (x.getName().equals("coordinates-route")) {
								routeEntity.setValue("coordinates_route", Integer.parseInt(x.getAttributeValue(null, "value").toString()));
							}
							if (x.getName().equals("divide-time")) {
								routeEntity.setValue("divide_time", Integer.parseInt(x.getAttributeValue(null, "value").toString()));
							}
							if (x.getName().equals("divide-distance")) {
								routeEntity.setValue("divide_distance", Integer.parseInt(x.getAttributeValue(null, "value").toString()));
							}
						}
						
						if (locationElement) {
							if (x.getName().equals("latitude")) {
								locationEntity.setValue("latitude", Float.parseFloat(x.getAttributeValue(null, "value").toString()));
							}
							if (x.getName().equals("longitude")) {
								locationEntity.setValue("longitude", Float.parseFloat(x.getAttributeValue(null, "value").toString()));
							}
							if (x.getName().equals("altitude")) {
								locationEntity.setValue("altitude", Float.parseFloat(x.getAttributeValue(null, "value").toString()));
							}
							if (x.getName().equals("position")) {
								locationEntity.setValue("position", Integer.parseInt(x.getAttributeValue(null, "value").toString()));
							}
							if (x.getName().equals("distance")) {
								locationEntity.setValue("distance", Float.parseFloat(x.getAttributeValue(null, "value").toString()));
							}
							if (x.getName().equals("speed")) {
								locationEntity.setValue("speed", Float.parseFloat(x.getAttributeValue(null, "value").toString()));
							}
							if (x.getName().equals("bearing")) {
								locationEntity.setValue("bearing", Integer.parseInt(x.getAttributeValue(null, "value").toString()));
							}
							if (x.getName().equals("accuracy")) {
								locationEntity.setValue("accuracy", Float.parseFloat(x.getAttributeValue(null, "value").toString()));
							}
							if (x.getName().equals("time")) {
								locationEntity.setValue("time", Integer.parseInt(x.getAttributeValue(null, "value").toString()));
							}
							if (x.getName().equals("pause")) {
								locationEntity.setValue("pause", Integer.parseInt(x.getAttributeValue(null, "value").toString()));
							}
						}
						
						if (resourceElement) {
							if (x.getName().equals("text")) {
								resourceEntity.setValue("text", x.getAttributeValue(null, "value").toString());
							}
							if (x.getName().equals("file")) {
								resourceEntity.setValue("file", x.getAttributeValue(null, "value").toString());
							}
							if (x.getName().equals("type-resource-id")) {
								resourceEntity.setValue("type_resource_id", new Entity("types_resource", Long.parseLong(x.getAttributeValue(null, "value").toString())));
							}
							if (x.getName().equals("latitude")) {
								resourceEntity.setValue("latitude", Float.parseFloat(x.getAttributeValue(null, "value").toString()));
							}
							if (x.getName().equals("longitude")) {
								resourceEntity.setValue("longitude", Float.parseFloat(x.getAttributeValue(null, "value").toString()));
							}
							if (x.getName().equals("altitude")) {
								resourceEntity.setValue("altitude", Float.parseFloat(x.getAttributeValue(null, "value").toString()));
							}
						}
						
					}
					
					if ( eventType == XmlPullParser.END_TAG ) {
						if (x.getName().equals("data")) {
							dataElement = false;
							routeEntity.save();
						}
						if (x.getName().equals("location")) {
							locationElement = false;
							locationEntity.setValue("route_id",routeEntity);
							locationEntity.save();
						}
						if (x.getName().equals("resource")) {
							resourceElement = false;
							resourceEntity.setValue("route_id",routeEntity);
							resourceEntity.save();
						}
					}
					
					eventType = x.next();
				}

				identifierOutText = R.string.import_correctly;
				File theXMLFile = new File(filenameRoute);
				theXMLFile.delete();
				
			} catch (Exception e) {
				e.printStackTrace();
				identifierOutText = R.string.import_error;
			}

		} catch (IOException e) {
			e.printStackTrace();
			identifierOutText = R.string.import_error;
		}
		handler.sendEmptyMessage(0);
	}
	
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			mActivity.endImport(identifierOutText, file, deleteToFinish);
		}
	};

}
