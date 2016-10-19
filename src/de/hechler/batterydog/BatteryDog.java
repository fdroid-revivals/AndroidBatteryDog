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
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewStub.OnInflateListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
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
			File batteryLogFile = new File(root,BatteryDog_Service.LOGFILEPATH);
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

	private SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
	private DecimalFormat dfT = new DecimalFormat("###.#");
	private DecimalFormat dfV = new DecimalFormat("##.###");

	private String parseLine(String line) {
		if (line == null)
			return line;
		String[] split = line.split("[;]");
		if (split.length < 6)
			return line;
		if (split[0].equals("Nr"))
			return line;
		try {
			int count = Integer.parseInt(split[0]);
			long time = Long.parseLong(split[1]);
			int level = Integer.parseInt(split[2]);
			int scale = Integer.parseInt(split[3]);
			int percent = level*100/scale;
			int voltage = Integer.parseInt(split[4]);
			int temperature = Integer.parseInt(split[5]);
			double v = 0.001*voltage;
			double t = 0.1*temperature;
			String timestamp = sdf.format(new Date(time));
			StringBuffer result = new StringBuffer();
			result.append(Integer.toString(count)).append(". ")
					.append(timestamp).append(" ")
					.append(percent).append("% ")
					.append(dfV.format(v)).append("V ")
					.append(dfT.format(t)).append("° ")
					;
//			for (int i = 6; i < split.length; i++) {
//				result.append(" ").append(split[i]);
//			}
			return result.toString();
		}
		catch (Exception e) {
			Log.e(TAG, e.getMessage(), e);
			return line;
		}
	}
    
}