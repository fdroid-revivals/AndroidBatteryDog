/*******************************************************************************
 * Copyright (c) 2009 Ferenc Hechler - ferenc_hechler@users.sourceforge.net
 * 
 * This file is part of the Android Battery Dog
 *
 * The Android Battery Dog is free software;
 * you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation;
 * either version 2 of the License, or (at your option) any later version.
 * 
 * The Android Battery Dog is distributed
 * in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with the Android Battery Dog;
 * if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *  
 *******************************************************************************/
package de.hechler.batterydog;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class BatteryDog extends Activity {

	private static final int MAX_OUTPUT_LINES = 100;

	private static final String TAG = "BATDOG";
	
	private Button btStart;
	private Button btStop;
	private Button btRefresh;
	private Button btGraph;
	private EditText mOutput;
	

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.battery_dog);
        mOutput= (EditText) findViewById(R.id.output);
        
        // find buttons in view
        btStart = ((Button) findViewById(R.id.btStart));
        btStop = ((Button) findViewById(R.id.btStop));
        btRefresh = ((Button) findViewById(R.id.btRefresh));
        btGraph = ((Button) findViewById(R.id.btGraph));

        // set actions for buttons
        btStart.setOnClickListener(StartServiceListener);
        btStop.setOnClickListener(StopServiceListener);
        btRefresh.setOnClickListener(RefreshListener);
        btGraph.setOnClickListener(GraphListener);
	}

    @Override
    public void onPause() {
         super.onPause();
    }
    
    @Override
    public void onResume() {
         super.onResume();
    }
    
    @Override
    protected void onDestroy() {
    	// TODO Auto-generated method stub
    	super.onDestroy();
    }

    OnClickListener StartServiceListener = new OnClickListener() {
        public void onClick(View v) {
            try {
	            startService(new Intent(BatteryDog.this, BatteryDog_Service.class));
			} catch (Exception e) {
				Log.e(TAG, e.getMessage(), e);
                Toast.makeText(BatteryDog.this, "Start Service failed: "+e.getMessage(), Toast.LENGTH_SHORT).show();
			}
        }
    };

    
    
    OnClickListener StopServiceListener = new OnClickListener() {
        public void onClick(View v) {
        	try {
	            stopService(new Intent(BatteryDog.this, BatteryDog_Service.class));
			} catch (Exception e) {
				Log.e(TAG, e.getMessage(), e);
	            Toast.makeText(BatteryDog.this, "Stop Service failed: "+e.getMessage(), Toast.LENGTH_SHORT).show();
			}
       	}
    };

    OnClickListener RefreshListener = new OnClickListener() {
        public void onClick(View v) {
        	updateLog();
       	}
    };

    OnClickListener GraphListener = new OnClickListener() {
        public void onClick(View v) {
        	startActivity(new Intent(BatteryDog.this, BatteryGraph.class));
       	}
    };


    private void updateLog() {
		try {
			File root = Environment.getExternalStorageDirectory();
			if (root == null)
		    	throw new Exception("external storage dir not found");
			File batteryLogFile = new File(root,"BatteryDog/battery.log");
			if (!batteryLogFile.exists())
				throw new Exception("logfile '"+batteryLogFile+"' not found");
			if (!batteryLogFile.canRead())
				throw new Exception("logfile '"+batteryLogFile+"' not readable");
			FileReader reader = new FileReader(batteryLogFile);
			BufferedReader in = new BufferedReader(reader);
			ArrayList<String> lines = new ArrayList<String>(); 
			String line = in.readLine();
			while (line != null) {
				lines.add(line);
				line = in.readLine();
			}
			in.close();
			int maxLine = lines.size();
			int minLine = Math.max(0, maxLine-MAX_OUTPUT_LINES);
			StringBuffer text = new StringBuffer();
			for (int i = maxLine-1; i >= minLine; i--) {
				text.append(parseLine(lines.get(i))).append("\n");
			}
			mOutput.setText(text.toString());
		} 
		catch (Exception e) {
			Log.e(TAG,e.getMessage(),e);
	    	mOutput.setText(e.getMessage());
		}
    }

	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private String parseLine(String line) {
		if (line == null)
			return line;
		String[] split = line.split("[;]");
		if (split.length != 3)
			return line;
		Long time = Long.parseLong(split[1]);
//		Calendar cal = Calendar.getInstance();
//		cal.setTimeInMillis(time);
		String timestamp = sdf.format(new Date(time));
		return split[0]+" "+timestamp + " " + split[2] + "%";
	}
    
}