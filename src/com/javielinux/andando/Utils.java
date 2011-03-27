package com.javielinux.andando;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.SimpleTimeZone;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.dataframework.Entity;
import com.google.android.maps.GeoPoint;

public class Utils {
	
	public static final int ACTIVITY_CREATE = 0;
	public static final int ACTIVITY_SHOW = 1;
	
	public static final int LOG_LEVEL_NONE = 0;
	public static final int LOG_LEVEL_USER = 1;
	public static final int LOG_LEVEL_DEBUG = 2;
	
	public static final String APPLICATION_PREFERENCES = "app_prefs";
	
	private static int logLevel = LOG_LEVEL_DEBUG;
	
	static public Context context = null;
	static public SharedPreferences preference = null;
	static public String appDirectory = "/sdcard/andando/";
	static public String appDirectoryThumb = "/sdcard/andando/thumb/";
	
	static private String mPackage = "com.javielinux.andando";
	
	static public String KEY_GOOGLE_MAPS = "KEY_MAPS";
	
	
	static public Location getLastLocation(Context cnt) {
		LocationManager locationmanager = (LocationManager)cnt.getSystemService(Context.LOCATION_SERVICE);
		Criteria criteria = new Criteria(); 
		criteria.setAccuracy(Criteria.ACCURACY_COARSE); 
		criteria.setAltitudeRequired(false); 
		criteria.setBearingRequired(false); 
		criteria.setCostAllowed(true); 
		criteria.setPowerRequirement(Criteria.POWER_LOW);
		String provider = locationmanager.getBestProvider(criteria,true);
		if (provider != null) { 
			return locationmanager.getLastKnownLocation(provider);
		}
		return null;
	}
	
	static public void setActivity(Context cnt)
    {
    	try {
    		File dir = new File(appDirectory);
    		if (!dir.exists()) dir.mkdir();
    		File fileNomedia = new File(appDirectory+".nomedia");
            if (!fileNomedia.exists()) fileNomedia.createNewFile();
            
    		File dirThumb = new File(appDirectoryThumb);
    		if (!dirThumb.exists()) dirThumb.mkdir();
    		File fileNomediaThumb = new File(appDirectoryThumb+".nomedia");
            if (!fileNomediaThumb.exists()) fileNomediaThumb.createNewFile();
            
    	} catch (Exception ioe) {
    		ioe.printStackTrace();
    	}
    	
        context = cnt;
        
		PreferenceManager.setDefaultValues(cnt, R.xml.preferences, false);
		preference = PreferenceManager.getDefaultSharedPreferences(cnt);
		
    }
	
	static public String getPackage()
    {
		return mPackage;
    }
	
    public static String getPathSaveRoutes(Context cnt) {
    	SharedPreferences prefs = cnt.getSharedPreferences(Utils.APPLICATION_PREFERENCES, Context.MODE_PRIVATE);
        if (prefs.contains("path_save_routes")) {
        	return prefs.getString("path_save_routes", "");
        } else {
            Editor editor = prefs.edit();
           	editor.putString("path_save_routes", appDirectory);
            editor.commit();
        	return appDirectory;
        }
    }
    
    public static void setPathSaveRoutes(Context cnt, String path) {
    	SharedPreferences prefs = cnt.getSharedPreferences(Utils.APPLICATION_PREFERENCES, Context.MODE_PRIVATE);
    	Editor editor = prefs.edit();
    	editor.putString("path_save_routes", path);
    	editor.commit();
    }
	
	
    public static String getUsernameYourTrainings(Context cnt) {
    	SharedPreferences prefs = cnt.getSharedPreferences(Utils.APPLICATION_PREFERENCES, Context.MODE_PRIVATE);
        if (prefs.contains("username_yourtrainings")) {
        	return prefs.getString("username_yourtrainings", "");
        } else {
            Editor editor = prefs.edit();
           	editor.putString("username_yourtrainings", "");
            editor.commit();
        	return "";
        }
    }
    
    public static void setUsernameYourTrainings(Context cnt, String username) {
    	SharedPreferences prefs = cnt.getSharedPreferences(Utils.APPLICATION_PREFERENCES, Context.MODE_PRIVATE);
    	Editor editor = prefs.edit();
    	editor.putString("username_yourtrainings", username);
    	editor.commit();
    }
    
    public static String getKeyYourTrainings(Context cnt) {
    	SharedPreferences prefs = cnt.getSharedPreferences(Utils.APPLICATION_PREFERENCES, Context.MODE_PRIVATE);
        if (prefs.contains("key_yourtrainings")) {
        	return prefs.getString("key_yourtrainings", "");
        } else {
            Editor editor = prefs.edit();
           	editor.putString("key_yourtrainings", "");
            editor.commit();
        	return "";
        }
    }
    
    public static void setKeyYourTrainings(Context cnt, String key) {
    	SharedPreferences prefs = cnt.getSharedPreferences(Utils.APPLICATION_PREFERENCES, Context.MODE_PRIVATE);
    	Editor editor = prefs.edit();
    	editor.putString("key_yourtrainings", key);
    	editor.commit();
    }

    /**
     * Comprueba si el GPS esta activido en el dispositivo
     * 
     * @return savedInstanceState
     */
    
    static public boolean getGPSStatus(Activity act)
    {
    	String allowedLocationProviders =
    		Settings.System.getString(act.getContentResolver(),
    		Settings.System.LOCATION_PROVIDERS_ALLOWED);
     
    	if (allowedLocationProviders == null) {
    		allowedLocationProviders = "";
    	}
     
    	return allowedLocationProviders.contains(LocationManager.GPS_PROVIDER);// || allowedLocationProviders.contains(LocationManager.NETWORK_PROVIDER);
    }	

    /**
     * Mete un texto en el log
     * 
     */
    
    static public void toLog_User(String msg)
    {
    	toLog(Utils.LOG_LEVEL_USER, msg);
    }
    
    static public void toLog_Debug(String msg)
    {
    	toLog(Utils.LOG_LEVEL_DEBUG, msg);
    }
    
    static public void toLog(int level, String msg)
    {
    	try {
    		
    		boolean todo = false;
    		
    		if (logLevel != Utils.LOG_LEVEL_NONE) {
	    		if (level == Utils.LOG_LEVEL_USER) {
	    			todo = true;
	    		} else {
	    			if (logLevel == Utils.LOG_LEVEL_DEBUG) todo = true;
	    		}
    		} else {
    			todo = false;
    		}
    		
    		if (todo) {
	    		String fileLog = Utils.appDirectory+"error-log.txt";
	    		
	    		File file = new File(fileLog);
	    		String data = getContents(file);
	    			
	    		BufferedWriter bw = new BufferedWriter(new FileWriter(file));
	    		
	    		bw.write(data + Utils.now() + ": " + msg + "\n");
	    		
	    		bw.close();

    		}
    		
    	} catch (Exception e) {
    		
    	}
    	
    }
    
    static public String getContents(File aFile) {
        StringBuilder contents = new StringBuilder();
        
        try {
          BufferedReader input =  new BufferedReader(new FileReader(aFile));
          try {
            String line = null;
            while (( line = input.readLine()) != null){
              contents.append(line);
              contents.append(System.getProperty("line.separator"));
            }
          }
          finally {
            input.close();
          }
        }
        catch (IOException ex){
          ex.printStackTrace();
        }
        
        return contents.toString();
      }

    
    /**
     * Copiar un fichero
     * 
     * @param source Fuente
     * @param destiny Destino
     * 
     */
    
   	public static void copy(String source, String destiny) throws IOException {
   		try
    	{
	    	FileInputStream fis = new FileInputStream(source);
	    	FileOutputStream fos = new FileOutputStream(destiny);
	    	FileChannel channelSource = fis.getChannel();
	    	FileChannel channelDestiny = fos.getChannel();
	    	channelSource.transferTo(0, channelSource.size(), channelDestiny);
	    	fis.close();
	    	fos.close();
    	}
    	catch(Exception e)
    	{
    		System.out.println("ERROR: " + e.getMessage());
    		Log.e("Andando: ", e.getClass().getName());
    	}
    }
	
    /**
     * Formatea el nombre de una ruta
     * 
     * @param idRoute Identificador
     * 
     */
	
	public static String formatNameRoute(long idRoute) {
		Entity route = new Entity("routes", idRoute);
		String name = deleteSpecialsChar(route.getValue("name").toString());
		if (name.length()>32) {
			return name.substring(0, 31);
		}
		return name;
	}
	
    /**
     * Elimina caracteres especiales
     * 
     * @param name Palabra a cambiar
     * 
     */
    
	public static String deleteSpecialsChar(String name) {
    	String out = name.replace(" ","_");
    	out = out.replace("á","a");
    	out = out.replace("é","e");
    	out = out.replace("í","i");
    	out = out.replace("ó","o");
    	out = out.replace("ú","u");
    	out = out.replace("Á","A");
    	out = out.replace("É","E");
    	out = out.replace("Í","I");
    	out = out.replace("Ó","O");
    	out = out.replace("Ú","U");
    	out = out.replace("/","_");
    	out = out.replace("\\","_");
    	out = out.replace(";","_");
    	out = out.replace(",","_");
    	out = out.replace(":","_");
    	out = out.replace(".","_");
    	out = out.replace("(","_");
    	out = out.replace("!","_");
    	out = out.replace(")","_");
    	out = out.replace("?","_");
	    return out;
    }
    
    /**
     * Muestra un mensaje
     * 
     * @param msg Mensaje
     * 
     */
    
    public static void showMessage(String msg) {
	    Toast.makeText(context, 
	    		msg, 
	            Toast.LENGTH_LONG).show();
    }
    
    /**
     * Muestra un mensaje
     * 
     * @param msg Mensaje
     * 
     */
    
    public static void showShortMessage(String msg) {
	    Toast.makeText(context, 
	    		msg, 
	            Toast.LENGTH_SHORT).show();
    }
    
    /**
     * Muestra un mensaje con un icono
     * 
     * @param icon Icono
     * @param msg Mensaje
     * 
     */
    
    public static void showShortMessageWithIcon(int icon, String msg) {
        LayoutInflater vi = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = vi.inflate(R.layout.toast_msg, null);

        ImageView iv = (ImageView)view.findViewById(R.id.toast_icon);
        iv.setImageResource(icon);
        
        TextView tv = (TextView)view.findViewById(R.id.toast_message);
        tv.setText(msg);

        Toast toast = new Toast(context);
        toast.setView(view);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.show();
    }
            
    /**
     * Devuelve una distancia en formato metros/km
     * 
     * @param distance Distancia
     * @retrun Texto
     * 
     */
	
	static public String formatSpeed(float speed) {
		if (preference.getString("prf_units", "km").equals("km")) {
			return Utils.round(speed, 2) + " km/h";
		} else {
			return Utils.round((speed/Float.parseFloat("1.61")), 2) + " mph";
		}
	}
	
	static public String formatSpeedNumber(float speed) {
		if (preference.getString("prf_units", "km").equals("km")) {
			if (speed>=100)
				return Utils.round(speed, 0) + "";
			else if (speed>=10)
				return Utils.round(speed, 1) + "";
			else
				return Utils.round(speed, 2) + "";
		} else {
			return Utils.round((speed/Float.parseFloat("1.61")), 2) + "";
		}
	}
	
	static public String formatSpeedUnit() {
		if (preference.getString("prf_units", "km").equals("km")) {
			return "km/h";
		} else {
			return "mph";
		}
	}
    
    /**
     * Devuelve una distancia en formato metros/km
     * 
     * @param distance Distancia
     * @retrun Texto
     * 
     */
	
	static public String formatDistance(float distance) {
		if (preference.getString("prf_units", "km").equals("km")) {
			if (distance<500) {
				return Utils.round(distance, 2) + " " + context.getResources().getString(R.string.metres);
			}
			return Utils.round(distance/1000, 2) + " " + context.getResources().getString(R.string.kilometres);
		} else {
			
			return Utils.round((distance*Float.parseFloat("0.000621")), 2) + " " + context.getResources().getString(R.string.miles);
		}
	}
	
	static public String formatDistanceNumber(float distance) {
		if (preference.getString("prf_units", "km").equals("km")) {
			if (distance<500) {
				return Utils.round(distance, 0) + "";
			}
			if (distance>=100)
				return Utils.round(distance/1000, 1) + "";
			else
				return Utils.round(distance/1000, 2) + "";
		} else {
			
			return Utils.round((distance*Float.parseFloat("0.000621")), 2) + "";
		}
	}
	
	static public String formatDistanceUnit(float distance) {
		if (preference.getString("prf_units", "km").equals("km")) {
			if (distance<500) {
				return context.getResources().getString(R.string.metres);
			}
			return context.getResources().getString(R.string.kilometres);
		} else {
			
			return context.getResources().getString(R.string.miles);
		}
	}
	
    /**
     * Devuelve una distancia en formato metros/km
     * 
     * @param distance Distancia
     * @retrun Texto
     * 
     */
	
	static public String formatMetres(float distance) {
		if (preference.getString("prf_units", "km").equals("km")) {
			return ((int)distance) + " " + context.getResources().getString(R.string.metres);
		} else {
			
			return Utils.round((distance*Float.parseFloat("0.000621")), 2) + " " + context.getResources().getString(R.string.miles);
		}
	}
	
    /**
     * Devuelve una distancia en formato metros/km
     * 
     * @param distance Distancia
     * @retrun Texto
     * 
     */
	
	static public String formatMinutesByDistance(int seconds, float distance) {
		int s;
		if (preference.getString("prf_units", "km").equals("km")) {
			s = (int) (seconds/ (distance/1000));
		} else {
			s = (int) (seconds/ Utils.round((distance*Float.parseFloat("0.000621")), 2));
		}
		return formatTime(s);
	}
	
    /**
     * Devuelve la fecha actual
     * @retrun Fecha
     * 
     */
	
	static public String now() {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date currentTime = new Date();
		return formatter.format(currentTime);
	}
	
	static public String plusDate(int days) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd 00:00:00");
		Date fch = new Date();
		Calendar cal = new GregorianCalendar();
		cal.setTimeInMillis(fch.getTime());
		cal.add(Calendar.DATE, days);
		
		Date end = new Date(cal.getTimeInMillis());
		
		return formatter.format(end);
	} 
	
	static public String plusSeconds(String date, int seconds) throws ParseException {
		
		GregorianCalendar timeUTC = new GregorianCalendar(new SimpleTimeZone(0,"GMT+0")); 
		GregorianCalendar timeNow = new GregorianCalendar();
		int difference = timeNow.get(Calendar.HOUR_OF_DAY) - timeUTC.get(Calendar.HOUR_OF_DAY);
		
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		Date fch = formatter.parse(date);
		
		Calendar cal = new GregorianCalendar();
		cal.setTimeInMillis(fch.getTime());
		cal.add(Calendar.SECOND, seconds);
		
		cal.add(Calendar.HOUR_OF_DAY, -difference);
		
		Date end = new Date(cal.getTimeInMillis());
		
		String d = formatter.format(end);
		String[] chains = d.split(" ");
		String out = chains[0] + "T" + chains[1] + "Z";
		
		return  out;
	} 
	
    /**
     * Devuelve la fecha actual
     * @param format Formato
     * @retrun Fecha
     * 
     */
	
	static public String now(String format) {
		SimpleDateFormat formatter = new SimpleDateFormat(format);
		Date currentTime = new Date();
		return formatter.format(currentTime);
	}
	


    /**
     * Devuelve un tiempo en formato minutos/segudos
     * 
     * @param seconds Segundos
     * @retrun Texto
     * 
     */
	
	static public String formatTime(long seconds) {
		String out = seconds/60 + " " + context.getResources().getString(R.string.minutes);
		if (seconds%60!=0) out += " " + context.getResources().getString(R.string.and) + " " + seconds%60 + " " + context.getResources().getString(R.string.seconds);
		return out;
	}
	
    /**
     * Devuelve un tiempo en formato minutos:segudos
     * 
     * @param seconds Segundos
     * @retrun Texto
     * 
     */
	
	static public String formatShortTime(long seconds) {
		String sec = "";
		long s = seconds%60;
		sec = s+"";
		if (s<10) sec = "0"+s; 
		return seconds/60 + "' " + sec + " '' ";
	}
	
    /**
     * Devuelve fecha formatada
     * 
     * @param date Fecha
     * @retrun Texto
     * 
     */
	
	static public String formatDateHour(String date) {
		String out = "";
		String[] pieces = date.split(" ");
		out = formatDate(pieces[0]) + " " + pieces[1]; 
		return out;
	}
	
    /**
     * Devuelve fecha formatada
     * 
     * @param date Fecha
     * @retrun Texto
     * 
     */
	
	static public String formatDate(String date) {
		String out = "";
		String[] pieces = date.split(" ");
		String[] pieceDate = pieces[0].split("-");
		String day = pieceDate[2];
		String month = pieceDate[1];
		String year = pieceDate[0];
		int posDay = Integer.parseInt(context.getResources().getString(R.string.position_date_day));
		int posMonth = Integer.parseInt(context.getResources().getString(R.string.position_date_month));
		int posYear = Integer.parseInt(context.getResources().getString(R.string.position_date_year));
		for (int i=0; i<=2; i++) {
			if (i==posDay) out += day;
			if (i==posMonth) out += month;
			if (i==posYear) out += year;
			if (i<2) out += context.getResources().getString(R.string.separate_date);
		}
		return out;
	}
	
    /**
     * Devuelve fecha formatada
     * 
     * @param date Fecha
     * @retrun Texto
     * 
     */
	
	static public String formatHumanDate(String date) {
		if (formatDate(date).equals(formatDate(now()))) {
			return context.getResources().getString(R.string.today);
		} else if (formatDate(date).equals(formatDate(plusDate(-1)))) {
			return context.getResources().getString(R.string.yesterday);			
		} else if (formatDate(date).equals(formatDate(plusDate(-2)))) {
			return context.getResources().getString(R.string.two_days_ago);
		} else if (formatDate(date).equals(formatDate(plusDate(-3)))) {
			return context.getResources().getString(R.string.three_days_ago);
		}
		return formatDate(date);
	}
	
    /**
     * Devuelve fecha formatada
     * 
     * @param date Fecha
     * @retrun Texto
     * 
     */
	
	static public String formatHour(String date) {
		String out = "";
		String[] pieces = date.split(" ");
		out = pieces[1]; 
		return out;
	}
	
    /**
     * Convierte de Location a Geopoint
     * 
     */
    
    static public GeoPoint Location2Geopoint(Location loc) {
    	return new GeoPoint((int)(loc.getLatitude()*1E6), (int)(loc.getLongitude()*1E6));
    }
    
    /**
     * Convierte de Geopoint a Location 
     * 
     */
    
    static public Location Geopoint2Location(GeoPoint geo) {
    	Location loc = new Location(LocationManager.GPS_PROVIDER);
    	loc.setLatitude((double)geo.getLatitudeE6()/1E6);
		loc.setLongitude((double)geo.getLongitudeE6()/1E6);
    	return loc;
    }
        
    /**
     * Redondea a un numeto de digitos determinado un valor
     * 
     * @param val Valor
     * @param places Numero de digitos
     * @return Nuevo valor
     * 
     */
	
	static public float round(float val, int places) {
		long factor = (long)Math.pow(10,places);
		val = val * factor;
		long tmp = Math.round(val);
		return (float)tmp / factor;
	} 

	

    
}
