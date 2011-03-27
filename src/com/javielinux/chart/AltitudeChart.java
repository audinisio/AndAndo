/**
 * Copyright (C) 2009 SC 4ViewSoft SRL
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.javielinux.chart;

import java.util.ArrayList;
import java.util.List;

import org.achartengine.ChartFactory;
import org.achartengine.chart.PointStyle;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import com.javielinux.andando.R;
import com.javielinux.andando.Utils;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;

import com.android.dataframework.DataFramework;
import com.android.dataframework.Entity;

public class AltitudeChart extends AbstractChart {
	
	public String getName() {
		return "Altitud";
	}
	  
	public String getDesc() {
		return "Gráfica de la altitud durante el recorrido";
	}
  
	public Intent execute(Context context, long idRoute) {
		
		Entity ent = new Entity("routes", idRoute);
		int coords = ent.getInt("coordinates_route");
		
		String[] titles = new String[] { Utils.context.getString(R.string.altitude), Utils.context.getString(R.string.average_altitude) };
		
		List<double[]> x = new ArrayList<double[]>();
		double[] valuesX = new double[coords];
		
    	int j = 0;
    	
    	for (j=0; j<coords; j++) {
    		valuesX[j] = j+1;
    	}
		
		x.add(valuesX);
		x.add(valuesX);

		List<double[]> y = new ArrayList<double[]>();
		
		double[] valuesY = new double[coords];
		double[] valuesYAverage = new double[coords];
		
		double maxAltitude = 0;
		
		Cursor c = DataFramework.getInstance().getCursor("locations", "route_id = " + idRoute, "position asc");
    	c.moveToFirst();
    	
    	j = 0;
    	
    	double sumAltitude = 0;
    	
    	while (!c.isAfterLast()) {
    		if (j<coords) {
	    		double altitude = c.getDouble(c.getColumnIndex("altitude"));
	    		valuesY[j] = altitude;
	    		
	    		sumAltitude += altitude;
	    		
	    		if (altitude > maxAltitude) maxAltitude = altitude; 
    		}
    		j++;
    		c.moveToNext();
    	}
    	
    	double aveAltitude = sumAltitude/coords;
    	
    	for (int k=0; k<coords; k++) {
    		valuesYAverage[k] = aveAltitude;
    	}
    	
    	maxAltitude += 20;
		
		y.add(valuesY);
		y.add(valuesYAverage);

		int[] colors = new int[] { Color.YELLOW, Color.RED };
		PointStyle[] styles = new PointStyle[] { PointStyle.POINT, PointStyle.POINT };
		XYMultipleSeriesRenderer renderer = buildRenderer(colors, styles);
		int length = renderer.getSeriesRendererCount();
		for (int i = 0; i < length; i++) {
			((XYSeriesRenderer) renderer.getSeriesRendererAt(i)).setFillPoints(true);
		}

		setChartSettings(renderer, Utils.context.getString(R.string.altidude_of_track), Utils.context.getString(R.string.position), Utils.context.getString(R.string.altitude), 0, coords, 0, maxAltitude, Color.LTGRAY, Color.GRAY);
		renderer.setXLabels(8);
		renderer.setYLabels(10);
		return ChartFactory.getLineChartIntent(context, buildDataset(titles, x, y), renderer);
	}

}
