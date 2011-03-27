package com.javielinux.andando;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public class Preferences extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        addPreferencesFromResource(R.xml.preferences);
        
        Preference serviceYourtrainings = (Preference) findPreference("prf_service_yourtrainings");
        
        serviceYourtrainings.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {
	            LayoutInflater factory = LayoutInflater.from(Preferences.this);
	            final View textEntryView = factory.inflate(R.layout.alert_dialog_username, null);
	            ((TextView)textEntryView.findViewById(R.id.username_edit)).setText(Utils.getUsernameYourTrainings(Preferences.this));
	            ((TextView)textEntryView.findViewById(R.id.key_edit)).setText(Utils.getKeyYourTrainings(Preferences.this));
	            
	            AlertDialog.Builder builder = new AlertDialog.Builder(Preferences.this);
	            builder.setTitle(R.string.title_prf_service_yourtrainings);
	            builder.setView(textEntryView);
	            builder.setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {
	            	public void onClick(DialogInterface dialog, int whichButton) {
	            		Utils.setUsernameYourTrainings(Preferences.this, ((TextView)textEntryView.findViewById(R.id.username_edit)).getText().toString());
	            		Utils.setKeyYourTrainings(Preferences.this, ((TextView)textEntryView.findViewById(R.id.key_edit)).getText().toString());
	            		Utils.showMessage(getString(R.string.keys_save));
	            	}
	            });
	            builder.setNeutralButton(R.string.sign_up, new DialogInterface.OnClickListener() {
	            	public void onClick(DialogInterface dialog, int whichButton) {
	            		Uri uri = Uri.parse("http://www.yourtrainings.com/m_signup.php");
	            		Intent intent = new Intent(android.content.Intent.ACTION_VIEW, uri);
			    		startActivity(intent);	
	            	}
	            });
	            builder.setNegativeButton(R.string.alert_dialog_cancel, new DialogInterface.OnClickListener() {
	            	public void onClick(DialogInterface dialog, int whichButton) {
	            		
	            	}
	            });
	            builder.create();
	            builder.show();
				return false;
			}
        	
        });
        /*
        Preference pathSaveRoutes = (Preference) findPreference("prf_path_save_routes");
        
        pathSaveRoutes.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {
	            LayoutInflater factory = LayoutInflater.from(Preferences.this);
	            final View textEntryView = factory.inflate(R.layout.alert_dialog_directory, null);
	            ((TextView)textEntryView.findViewById(R.id.directory_edit)).setText(Utils.getPathSaveRoutes(Preferences.this));
	            
	            AlertDialog.Builder builder = new AlertDialog.Builder(Preferences.this);
	            builder.setTitle(R.string.title_prf_path_save_routes);
	            builder.setView(textEntryView);
	            builder.setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {
	            	public void onClick(DialogInterface dialog, int whichButton) {
	            		String path = ((TextView)textEntryView.findViewById(R.id.directory_edit)).getText().toString();
	            		if (!path.endsWith("/")) path += "/";
	            		File f = new File(path);
	            		if (f.isDirectory()) {
	            			Utils.setPathSaveRoutes(Preferences.this, path);
	            			Utils.showMessage(getString(R.string.directory_save));
	            		} else {
	            			Utils.showMessage(getString(R.string.directory_no_exist));
	            		}
	            	}
	            });
	            builder.create();
	            builder.show();
				return false;
			}
        	
        });
        */
    }
	
}
