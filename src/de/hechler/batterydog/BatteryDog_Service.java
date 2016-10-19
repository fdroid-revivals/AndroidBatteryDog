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

import java.io.File;
import java.io.FileWriter;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class BatteryDog_Service extends Service {

	private final static String TAG = "BATDOG.service";

	private File mBatteryLogFile;
	private int mCount;
	private int mLastLevel;

	
	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		mCount = 0;
		registerReceiver(mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        Toast.makeText(this, "BatteryDog Service started", Toast.LENGTH_SHORT).show();
	}
	
	@Override
	public void onDestroy() {
    	Log.i(TAG, "onDestroy");
    	super.onDestroy();
    	unregisterReceiver(mBatInfoReceiver);
    	mCount += 1;
    	logLevel(mLastLevel);
		mCount = 0;
        Toast.makeText(this, "BatteryDog Service stopped", Toast.LENGTH_SHORT).show();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	

	private BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context ctx, Intent intent) {
			mCount += 1;
			try {
				int level = intent.getIntExtra("level", 0);
				mLastLevel = level;
				logLevel(level);
			}
			catch (Exception e) {
				Log.e(TAG,e.getMessage(), e);
			}
		}

	};

	private void logLevel(int level) {
		try {
			FileWriter out = null;
			if (mBatteryLogFile != null) {
				try {
					out = new FileWriter(mBatteryLogFile, true);
				}
				catch (Exception e) {}
			}
			if (out == null) {
				File root = Environment.getExternalStorageDirectory();
				if (root == null)
					throw new Exception("external storage dir not found");
				mBatteryLogFile = new File(root,"BatteryDog/battery.log");
				if (!mBatteryLogFile.exists()) {
					mBatteryLogFile.getParentFile().mkdirs();
					mBatteryLogFile.createNewFile();
				}
				if (!mBatteryLogFile.exists()) 
					throw new Exception("creation of file '"+mBatteryLogFile.toString()+"' failed");
				if (!mBatteryLogFile.canWrite()) 
					throw new Exception("file '"+mBatteryLogFile.toString()+"' is not writable");
				out = new FileWriter(mBatteryLogFile, true);
			}
			out.write(mCount+";"+System.currentTimeMillis()+";"+level+"\n");
			out.close();
		} catch (Exception e) {
			Log.e(TAG,e.getMessage(),e);
		}
	}

}

