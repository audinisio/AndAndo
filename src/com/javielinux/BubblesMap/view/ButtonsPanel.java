package com.javielinux.BubblesMap.view;

import android.view.Gravity;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.maps.MapActivity;
import com.javielinux.andando.CreateRoute;
import com.javielinux.andando.R;
import com.javielinux.andando.ShowRoute;
import com.javielinux.andando.Utils;

public class ButtonsPanel extends LinearLayout {

	private ImageButton btNext;
	private ImageButton btPrevious;
	private ImageView imgTopIcons;
	
	private LinearLayout layoutButtonsOverlay;
	
	private MapActivity mMapActivity;
	private int typeActivity;
	
	public ButtonsPanel(MapActivity ma, int ta) {
		super(ma);

		mMapActivity = ma;
		
		typeActivity = ta;
		
        this.setVisibility(View.GONE);
        this.setOrientation(LinearLayout.VERTICAL);
        this.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,
                LayoutParams.WRAP_CONTENT, Gravity.FILL_HORIZONTAL+Gravity.CENTER_HORIZONTAL));
        
        imgTopIcons = new ImageView(mMapActivity);
        imgTopIcons.setPadding(0, 5, 0, 0);
        imgTopIcons.setImageResource(R.drawable.icon_photo);
        imgTopIcons.setVisibility(View.INVISIBLE);
        
        this.addView(imgTopIcons);
                                
        // Layout Botones
        
        layoutButtonsOverlay = new LinearLayout(mMapActivity);
        layoutButtonsOverlay.setOrientation(LinearLayout.HORIZONTAL);
        layoutButtonsOverlay.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,
                LayoutParams.WRAP_CONTENT, Gravity.CENTER_VERTICAL));
        layoutButtonsOverlay.setPadding(0, (mMapActivity.getResources().getConfiguration().orientation==2)?35:90, 0, 0);
        
        btPrevious = new ImageButton(mMapActivity);
        btPrevious.setBackgroundResource(R.drawable.empty);
        btPrevious.setImageResource(R.drawable.previous1_distance);
        
        btPrevious.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (typeActivity == Utils.ACTIVITY_CREATE) {
					((CreateRoute)mMapActivity).previous();
				} else {
					((ShowRoute)mMapActivity).previous();
				}
			}
			
		});
        
        layoutButtonsOverlay.addView(btPrevious);
        
    	LinearLayout.LayoutParams lSpace = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
    	lSpace.weight = 1;
    	TextView tSpace = new TextView(mMapActivity);
    	tSpace.setLayoutParams(lSpace);
    	layoutButtonsOverlay.addView(tSpace);
        
        btNext = new ImageButton(mMapActivity);
        btNext.setBackgroundResource(R.drawable.empty);
        btNext.setImageResource(R.drawable.next1_clock);
        
        btNext.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (typeActivity == Utils.ACTIVITY_CREATE) {
					((CreateRoute)mMapActivity).next();
				} else {
					((ShowRoute)mMapActivity).next();
				}
			}
			
		});
        
        layoutButtonsOverlay.addView(btNext);
        
        this.addView(layoutButtonsOverlay);
		
	}	
	
    /**
     * Cambia la imagen segun el tipo
     * 
     * @param type Tipo de imagen
     * 
     */
	
	public void setImageButtons(int type, int typeMarksOnMap) {
    	if (type == ShowRoute.TYPE_BUTTONS_OUT_MARKS) {
    		imgTopIcons.setVisibility(View.VISIBLE);
    		if (typeMarksOnMap == MapLocationsManager.TYPE_MARKS_RESOURCES) {
    			btPrevious.setImageResource(R.drawable.previous1_distance);
    			btNext.setImageResource(R.drawable.next1_clock);
    			imgTopIcons.setImageResource(R.drawable.icon_photo);
    		}
    		if (typeMarksOnMap == MapLocationsManager.TYPE_MARKS_TIME) {
    			btPrevious.setImageResource(R.drawable.previous1_resources);
    			btNext.setImageResource(R.drawable.next1_distance);
    			imgTopIcons.setImageResource(R.drawable.icon_clock);
    		}
    		if (typeMarksOnMap == MapLocationsManager.TYPE_MARKS_DISTANCE) {
    			btPrevious.setImageResource(R.drawable.previous1_clock);
    			btNext.setImageResource(R.drawable.next1_resources);
    			imgTopIcons.setImageResource(R.drawable.icon_distance);
    		}
    	} else {
    		imgTopIcons.setVisibility(View.INVISIBLE);
    		btPrevious.setImageResource(R.drawable.previous2);
        	btNext.setImageResource(R.drawable.next2);
    	}
	}
	
	
    /**
     * Muestra los botones
     * 
     */
    
    public void showButtons() {
   		this.setVisibility(View.VISIBLE);
    	this.startAnimation(AnimationUtils.loadAnimation(mMapActivity, android.R.anim.fade_in));
	}

    /**
     * Oculta los botones
     * 
     */
    
    public void hideButtons() {
    	this.setVisibility(View.GONE);
    	this.startAnimation(AnimationUtils.loadAnimation(mMapActivity, android.R.anim.fade_out));
	}
    
}
