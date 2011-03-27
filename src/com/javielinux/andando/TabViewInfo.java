package com.javielinux.andando;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;

import com.android.dataframework.DataFramework;

public class TabViewInfo extends TabActivity {
	
	private long idRoute;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final TabHost tabHost = getTabHost();

        idRoute = savedInstanceState != null ? savedInstanceState.getLong(DataFramework.KEY_ID) : -1;
		if (idRoute < 0) {
			Bundle extras = getIntent().getExtras();            
			idRoute = extras != null ? extras.getLong(DataFramework.KEY_ID) : -1;
		}
        
	    Intent iViewInfo = new Intent(this, ViewInfoGeneral.class);
	    iViewInfo.putExtra(DataFramework.KEY_ID, idRoute);
        
        tabHost.addTab(tabHost.newTabSpec("tab1")
                .setIndicator(this.getText(R.string.general), this.getResources().getDrawable(R.drawable.icon_info))
                .setContent(iViewInfo));

        Intent iViewSpeed = new Intent(this, ViewInfoSpeed.class);
        iViewSpeed.putExtra(DataFramework.KEY_ID, idRoute);
        
        tabHost.addTab(tabHost.newTabSpec("tab2")
                .setIndicator(this.getText(R.string.speed), this.getResources().getDrawable(R.drawable.icon_view))
                .setContent(iViewSpeed));
        
        Intent iViewChart = new Intent(this, ViewInfoChart.class);
        iViewChart.putExtra(DataFramework.KEY_ID, idRoute);
        
        tabHost.addTab(tabHost.newTabSpec("tab3")
                .setIndicator(this.getText(R.string.charts), this.getResources().getDrawable(R.drawable.icon_chart))
                .setContent(iViewChart));
        
    }
}
