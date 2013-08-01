package com.wenhui.taskmanager;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class DisplaySummaryActivity extends Activity {

	private TextView textMemInfo;
	private TextView textBatteryInfo;
	private TextView textInternal;
	private TextView textExternal;
	private ProgressBar progressMemInfo;
	private ProgressBar progressBatteryInfo;
	private ProgressBar progressInternal;
	private ProgressBar progressExternal;
	private final int ERROR = -1;
	private volatile boolean isRunning = true;
	private ImageView imageBattery;
	
	private final int GREEN_STATE = 1;
	private final int YELLOW_STATE = 2;
	private final int RED_STATE = 3;
	private int currentState = 0;
	
	private final int CHARGING=1;
	private final int CHARGED= 2;
	private int BATTERY_STATE=0;
	private BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context arg0, Intent intent) {
			int charging = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0);
			int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
			textBatteryInfo.setText(String.valueOf(level) + "%");
			if (level > 30) {
				if (currentState != GREEN_STATE) {
					currentState = GREEN_STATE;
					Rect bound = progressBatteryInfo.getProgressDrawable().getBounds();
					progressBatteryInfo.setProgressDrawable(getResources().getDrawable(R.drawable.custom_progressbar));
					progressBatteryInfo.getProgressDrawable().setBounds(bound);
				}
			} else if (level <=30  && level > 15) {
				if (currentState != YELLOW_STATE) {
					currentState = YELLOW_STATE;
					Rect bound = progressBatteryInfo.getProgressDrawable().getBounds();
					progressBatteryInfo.setProgressDrawable(getResources().getDrawable(R.drawable.custom_progressbar1));
					progressBatteryInfo.getProgressDrawable().setBounds(bound);
				}
			} else {
				if (currentState != RED_STATE) {
					currentState = RED_STATE;
					Rect bound = progressBatteryInfo.getProgressDrawable().getBounds();
					progressBatteryInfo.setProgressDrawable(getResources().getDrawable(R.drawable.custom_progressbar2));
					progressBatteryInfo.getProgressDrawable().setBounds(bound);
				}
			}
			progressBatteryInfo.setProgress(level);

			if (charging != 0) {
				if (level < 100) {
					if(BATTERY_STATE != CHARGING){
						BATTERY_STATE = CHARGING;
						imageBattery.setVisibility(View.VISIBLE);
						imageBattery.setImageResource(R.drawable.battery_charging);
					}
				} else {
					if(BATTERY_STATE != CHARGED){
						BATTERY_STATE = CHARGED;
						imageBattery.setVisibility(View.VISIBLE);
						imageBattery.setImageResource(R.drawable.battery_charged);
					}
				}
			} else {
				BATTERY_STATE=0;
				if(imageBattery.isShown())
					imageBattery.setVisibility(View.GONE);
			}
		}

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.summary);

		textMemInfo = (TextView) findViewById(R.id.textView_memInfo);
		textBatteryInfo = (TextView) findViewById(R.id.textView_batteryinfo);
		textInternal = (TextView) findViewById(R.id.textView_internal);
		textExternal = (TextView) findViewById(R.id.textView_external);

		progressMemInfo = (ProgressBar) findViewById(R.id.progressBar1);
		progressBatteryInfo = (ProgressBar) findViewById(R.id.progressBar2);
		progressInternal = (ProgressBar) findViewById(R.id.progressBar3);
		progressExternal = (ProgressBar) findViewById(R.id.progressBar4);

		Resources res = getResources();
		progressInternal.setProgressDrawable(res.getDrawable(R.drawable.custom_progressbar));
		progressExternal.setProgressDrawable(res.getDrawable(R.drawable.custom_progressbar));
		progressMemInfo.setProgressDrawable(res.getDrawable(R.drawable.custom_progressbar));

		imageBattery = (ImageView) findViewById(R.id.imageView_battery);

		displayStorageInfo();
		this.registerReceiver(this.mBatInfoReceiver, new IntentFilter(
				Intent.ACTION_BATTERY_CHANGED));

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		isRunning = false;
	}

	private void displayStorageInfo() {
		long totalInternal = getTotalInternalMemorySize();
		long usedInternal = totalInternal - getAvailableInternalMemorySize();
		String storageInternal = byteToB(usedInternal) + "/"
				+ byteToB(totalInternal);
		int perc1 = (int) (usedInternal * 100 / totalInternal);
		textInternal.setText(storageInternal);
		progressInternal.setProgress(perc1);

		long totalExternal = getTotalExternalMemorySize();
		long usedExternal = totalExternal - getAvailableExternalMemorySize();
		String storageExternal = byteToB(usedExternal) + "/"
				+ byteToB(totalExternal);
		int perc2 = (int) (usedExternal * 100 / totalExternal);
		textExternal.setText(storageExternal);
		progressExternal.setProgress(perc2);

	}

	public long getAvailableInternalMemorySize() {
		File path = Environment.getDataDirectory();
		StatFs stat = new StatFs(path.getPath());
		long blockSize = stat.getBlockSize();
		long availableBlocks = stat.getAvailableBlocks();
		return availableBlocks * blockSize;
	}

	public long getTotalInternalMemorySize() {
		File path = Environment.getDataDirectory();
		StatFs stat = new StatFs(path.getPath());
		long blockSize = stat.getBlockSize();
		long totalBlocks = stat.getBlockCount();
		return totalBlocks * blockSize;
	}

	@Override
	protected void onResume() {
		super.onResume();
		isRunning = true;
		new UpdateInfoTask().execute();
	}

	@Override
	protected void onPause() {
		super.onPause();
//		Log.i("DisplaySummary onPause:", "here");
		isRunning = false;
	}

	public long getAvailableExternalMemorySize() {
		if (externalMemoryAvailable()) {
			File path = Environment.getExternalStorageDirectory();
			StatFs stat = new StatFs(path.getPath());
			long blockSize = stat.getBlockSize();
			long availableBlocks = stat.getAvailableBlocks();
			return availableBlocks * blockSize;
		} else {
			return ERROR;
		}
	}

	public final long KB = 1024;
	public final long MB = 1024 * 1024;
	public final long GB = 1024 * MB;

	public String byteToB(long bytes) {
		if (bytes > KB && bytes < MB) {
			float size = (float) bytes / KB;
			return String.format("%.2f", size) + "KB";
		} else if (bytes > MB && bytes < GB) {
			float size = (float) bytes / MB;
			return String.format("%.2f", size) + "MB";
		} else if (bytes < KB) {
			return Long.toString(bytes) + "B";
		} else {
			float size = (float) bytes / GB;
			return String.format("%.2f", size) + "GB";

		}
	}

	public long getTotalExternalMemorySize() {
		if (externalMemoryAvailable()) {
			File path = Environment.getExternalStorageDirectory();
			StatFs stat = new StatFs(path.getPath());
			long blockSize = stat.getBlockSize();
			long totalBlocks = stat.getBlockCount();
			return totalBlocks * blockSize;
		} else {
			return ERROR;
		}
	}

	public boolean externalMemoryAvailable() {
		return android.os.Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED);
	}

	class UpdateInfoTask extends AsyncTask<Boolean, Integer, Boolean> {

		private long totalMem;
		private long avaMem;

		@Override
		protected Boolean doInBackground(Boolean... params) {
			totalMem = getTotalAvaMemory();
			progressMemInfo.setMax((int)totalMem);
			while (isRunning) {
				avaMem = getAvailableMemory();
//				Log.i("Available memory", Long.toString(avaMem));
				publishProgress(0);
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			return true;
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
			updateMemInfo();
		}

		private void updateMemInfo() {
			int memPercentage =(int)(totalMem - avaMem);
			String mem = Long.toString(toMB(avaMem)) + "MB Free";
			textMemInfo.setText(mem);
			progressMemInfo.setProgress(memPercentage);
		}

		public long toMB(long kb) {
			long mb = (long) kb / 1024;
			return mb;
		}

		private CharSequence removeDuplicateWhitespace(CharSequence inputStr) {
			String patternStr = "\\s+";
			String replaceStr = " ";
			Pattern pattern = Pattern.compile(patternStr);
			Matcher matcher = pattern.matcher(inputStr);
			return matcher.replaceAll(replaceStr);
		}

		private long getTotalAvaMemory() {
			try {
				RandomAccessFile reader = new RandomAccessFile("/proc/meminfo",
						"r");
				String line1 = reader.readLine();
				line1 = (String) removeDuplicateWhitespace(line1);
//				Log.i("/proc/stat line1:", line1);
				String[] toks1 = line1.split(" ");
				String totalMemory = toks1[1]; // total memory
//				Log.i("total memory: ", totalMemory + " KB");

				long total = Long.parseLong(totalMemory);
				reader.close();
				return total;
			} catch (Exception e) {
				e.printStackTrace();
			}
			return 1;
		}

		private long getAvailableMemory() {
			ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
			ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
			am.getMemoryInfo(mi);
			return mi.availMem / 1024;
		}
	}

}
