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
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

public class BatteryGraph extends Activity {

	private final static String TAG = "BATDOG.graph";
	private long width = 300;
	private long height = 300;
	private long mDeltaTime = 24*60*60*1000;
	private long mOffset = 0;
	private GraphView mGraphView;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGraphView = new GraphView(this);
        setContentView(mGraphView);
    }


    private final static int MENU_8H    = 1;
    private final static int MENU_24H   = 2;
    private final static int MENU_7DAYS = 3;
    private final static int MENU_ALL   = 4;
    /**
     * Called when your activity's options menu needs to be created.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(Menu.NONE, MENU_8H,    Menu.NONE, "8h");
        menu.add(Menu.NONE, MENU_24H,   Menu.NONE, "24h");
        menu.add(Menu.NONE, MENU_7DAYS, Menu.NONE, "7 days");
        menu.add(Menu.NONE, MENU_ALL,   Menu.NONE, "all");
        return true;
    }
    
    /**
     * Called when a menu item is selected.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	if (item.getItemId() == MENU_8H) {
    		mDeltaTime = 8*60*60*1000;
			mOffset = 0;
    		mGraphView.invalidate();
    	}
    	else if (item.getItemId() == MENU_24H) {
    		mDeltaTime = 24*60*60*1000;
			mOffset = 0;
    		mGraphView.invalidate();
    	}
    	else if (item.getItemId() == MENU_7DAYS) {
    		mDeltaTime = 7*24*60*60*1000;
			mOffset = 0;
    		mGraphView.invalidate();
    	}
    	else if (item.getItemId() == MENU_ALL) {
    		mDeltaTime = 0;
			mOffset = 0;
    		mGraphView.invalidate();
    	}
        return true;
    }
    
    @Override
    public boolean onTrackballEvent(MotionEvent event) {
    	super.onTrackballEvent(event);
    	if (event.getAction() == MotionEvent.ACTION_DOWN) {
    		mOffset = 0;
			mGraphView.invalidate();
    	}
    	else if (event.getAction() == MotionEvent.ACTION_MOVE) {
    		float x = event.getRawX();
    		if (x < 0) {
    			mOffset -= mDeltaTime/5;
    			mGraphView.invalidate();
    		} 
    		else if (x>0) {
    			mOffset += mDeltaTime/5;
    			if (mOffset > 0)
    				mOffset = 0;
    			mGraphView.invalidate();
    		}
    	}
    	return true;
    }
    
    
    private class GraphView extends View {
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
    
        public GraphView(Context context) {
            super(context);
            readRecords();

            Display display = ((WindowManager) context.getSystemService(WINDOW_SERVICE)).getDefaultDisplay();  
            width = display.getWidth();  
            height = display.getHeight();  
//            int orientation = display.getOrientation();              
        }
        
        @Override protected void onDraw(Canvas canvas) {
			Paint paint = mPaint;
            paint.setStrokeWidth(0);

			Paint paintP = new Paint();
            paintP.setStrokeWidth(0);
            paintP.setColor(Color.YELLOW);
			Paint paintT = new Paint();
            paintT.setStrokeWidth(0);
            paintT.setColor(Color.GREEN);
			Paint paintV = new Paint();
            paintV.setStrokeWidth(0);
            paintV.setColor(Color.RED);
            
			int margX = 5;
			int margYTop = 5;
			int margYBottom = 60;
			long w = width - 2*margX;
			long h = height - margYTop - margYBottom;

			canvas.drawColor(Color.BLACK);

            if ((mRecords == null) || (mRecords.length == 0)) {
                paint.setColor(Color.WHITE);
                canvas.drawText("no data found", 10, 50, paint);
                return;
            }
            for (int i = 0; i <= 10; i++) {
            	if (i == 5)
                    paint.setColor(Color.GRAY);
            	else
                    paint.setColor(Color.DKGRAY);
            	canvas.drawLine(margX, margYTop+h*i/10, margX+w, margYTop+h*i/10, paint);
			}

            int maxRec = mRecords.length;
            long minTime = mRecords[0].timestamp;
            long maxTime = mRecords[maxRec-1].timestamp;
            long dTime = maxTime-minTime;
            if (mDeltaTime != 0) {
            	dTime = mDeltaTime;
            	minTime = maxTime-dTime+mOffset;
            }
            
        	BatteryRecord rec;
        	BatteryRecord oldRec;
			for (int i = 0; i <= maxRec; i++) {
            	if (i == 0)
            		oldRec = new BatteryRecord(0, minTime, 0, 100, 0, 0);
            	else
            		oldRec = mRecords[i-1];
            	if (i == maxRec)
            		rec = new BatteryRecord(0, maxTime, 0, 100, 0, 0);
            	else
            		rec = mRecords[i];

    			drawRecordLine(canvas, rec, oldRec, minTime, dTime, paintP, paintT, paintV);
			}
        }

		private void drawRecordLine(Canvas canvas, 
				BatteryRecord rec, BatteryRecord oldRec,
				long minTime, long dTime,
				Paint paintP, Paint paintT, Paint paintV 
				) {
			int margX = 5;
			int margYTop = 5;
			int margYBottom = 60;
			long w = width - 2*margX;
			long h = height - margYTop - margYBottom;

			float x1 = margX+(w*(oldRec.timestamp-minTime)) / dTime; 
			float yP1 = margYTop+h-(h*oldRec.level) / rec.scale; 
			float yT1 = margYTop+h-(h*oldRec.temperature) / 1000; 
			float yV1 = margYTop+h-(h*oldRec.voltage) / 10000; 
			float x2 = margX+(w*(   rec.timestamp-minTime)) / dTime; 
			float yP2 = margYTop+h-(h*   rec.level) / rec.scale;
			float yT2 = margYTop+h-(h*   rec.temperature) / 1000;
			float yV2 = margYTop+h-(h*   rec.voltage) / 10000;
			
			if (rec.count == 1) {
				canvas.drawLine(x1, yP1, x1, margYTop+h, paintP);
				canvas.drawLine(x1, yV1, x1, margYTop+h, paintV);
				canvas.drawLine(x1, yT1, x1, margYTop+h, paintT);
				canvas.drawLine(x2, yP2, x2, margYTop+h, paintP);
				canvas.drawLine(x2, yV2, x2, margYTop+h, paintV);
				canvas.drawLine(x2, yT2, x2, margYTop+h, paintT);
			}
			else {
				canvas.drawLine(x1, yP1, x2, yP2, paintP);
				canvas.drawLine(x1, yV1, x2, yV2, paintV);
				canvas.drawLine(x1, yT1, x2, yT2, paintT);
			}
		}
    }

    class BatteryRecord {
		int count;
    	long timestamp;
    	int level;
    	int scale;
    	int voltage;
    	int temperature;
    	public BatteryRecord(int count, long timestamp, int level, int scale, int voltage, int temperature) {
    		this.count = count;
    		this.timestamp = timestamp;
    		this.level = level;
    		this.scale = scale;
    		this.voltage = voltage;
    		this.temperature = temperature;
		}
    }
    
    private BatteryRecord[] readLog() throws Exception {
    	ArrayList<BatteryRecord> result = new ArrayList<BatteryRecord>();
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
		if (split.length < 6)
			return null;
		if (split[0].equals("Nr"))
			return null;
		try {
			int count = Integer.parseInt(split[0]);
			long timestamp = Long.parseLong(split[1]);
			int level = Integer.parseInt(split[2]);
			int scale = Integer.parseInt(split[3]);
			int voltage = Integer.parseInt(split[4]);
			int temperature = Integer.parseInt(split[5]);
			return new BatteryRecord(count, timestamp, level, scale, voltage, temperature);
		}
		catch (Exception e) {
			Log.e(TAG,"Invalid format in line '"+line+"'");
			return null;
		}
	}
    
    
}

