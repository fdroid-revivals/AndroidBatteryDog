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
import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

public class BatteryGraph extends Activity {

	private final static String TAG = "BATDOG.graph";
	private long width = 300;
	private long height = 300;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new SampleView(this));
    }
    
    private class SampleView extends View {
        private Paint   mPaint = new Paint();
    	private BatteryRecord[] mRecords;
        
        private void readRecords() {
        	try {
        		mRecords = readLog();
        	}
        	catch (Exception e) {
        		Log.e(TAG,e.getMessage(), e);
			}
        }
    
        public SampleView(Context context) {
            super(context);
            readRecords();

            Display display = ((WindowManager) context.getSystemService(WINDOW_SERVICE)).getDefaultDisplay();  
            width = display.getWidth();  
            height = display.getHeight();  
//            int orientation = display.getOrientation();              
        }
        
        @Override protected void onDraw(Canvas canvas) {
			int margX = 5;
			int margYTop = 5;
			int margYBottom = 60;
			long w = width - 2*margX;
			long h = height - margYTop - margYBottom;

			Paint paint = mPaint;
            paint.setStrokeWidth(0);
            
            canvas.drawColor(Color.WHITE);

            if ((mRecords == null) || (mRecords.length == 0)) {
                paint.setColor(Color.BLACK);
                canvas.drawText("no data found", 10, 50, paint);
                return;
            }
            paint.setColor(Color.GREEN);
            for (int i = 0; i <= 10; i++) {
            	if (i == 5)
                    paint.setColor(Color.GREEN);
            	else
                    paint.setColor(Color.YELLOW);
            	canvas.drawLine(margX, margYTop+h*i/10, margX+w, margYTop+h*i/10, paint);
			}
            
            
            paint.setColor(Color.RED);
            
            int maxRec = mRecords.length;
            long minTime = mRecords[0].timestamp;
            long maxTime = mRecords[maxRec-1].timestamp;
            long dTime = maxTime-minTime;
            

			for (int i = 0; i <= maxRec; i++) {
            	if (i%2 == 0)
                    paint.setColor(Color.RED);
            	else
                    paint.setColor(Color.BLUE);
            	BatteryRecord rec;
            	BatteryRecord oldRec;
            	if (i == 0)
            		oldRec = new BatteryRecord(0, minTime, 0);
            	else
            		oldRec = mRecords[i-1];
            	if (i == maxRec)
            		rec = new BatteryRecord(0, maxTime, 0);
            	else
            		rec = mRecords[i];
            	
				float x1 = margX+(w*(oldRec.timestamp-minTime)) / dTime; 
				float y1 = margYTop+h-(h*oldRec.level) / 100; 
				float x2 = margX+(w*(   rec.timestamp-minTime)) / dTime; 
				float y2 = margYTop+h-(h*   rec.level) / 100;
            	
            	if (rec.count == 1) {
    				canvas.drawLine(x1, y1, x1, margYTop+h, paint);
    				canvas.drawLine(x2, y2, x2, margYTop+h, paint);
            	}
            	else {
    				canvas.drawLine(x1, y1, x2, y2, paint);
            	}
			}
        }
    }

    class BatteryRecord {
		int count;
    	long timestamp;
    	int level;
    	public BatteryRecord(int count, long timestamp, int level) {
    		this.count = count;
    		this.timestamp = timestamp;
    		this.level = level;
		}
    }
    
    private BatteryRecord[] readLog() throws Exception {
    	ArrayList<BatteryRecord> result = new ArrayList<BatteryRecord>();
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
		String line = in.readLine();
		while (line != null) {
			BatteryRecord rec = parseLine(line);
			if (rec == null)
				Log.e(TAG, "could not parse line: '"+line+"'");
			else 
				result.add(rec);
			line = in.readLine();
		}
		in.close();
		return (BatteryRecord[]) result.toArray(new BatteryRecord[result.size()]);
    }

	private BatteryRecord parseLine(String line) {
		if (line == null)
			return null;
		String[] split = line.split("[;]");
		if (split.length != 3)
			return null;
		int count = Integer.parseInt(split[0]);
		long timestamp = Long.parseLong(split[1]);
		int level = Integer.parseInt(split[2]);
		return new BatteryRecord(count, timestamp, level);
	}
    
    
}

