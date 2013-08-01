package com.wenhui.taskmanager;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class InstallAppActivity extends ListActivity {

	private ArrayList<File> apkFiles = new ArrayList<File>();
	private Handler myHandler;
	private FileAdapter adapter;
	private Resources resource;
	private static final int MESSAGE_FINISH_LOADING = 100;
	public static final int MESSAGE_ICON_CHANGED = 101;
	private ArrayList<ApkInfo> apkInfo = new ArrayList<ApkInfo>();
	private ListView listview;
	private static final int DIALOG_DELETE_CONFIRM = 1;
	private ProgressBar progressbar;
	private TextView textSd;
	private int position;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.list_apk_from_sd);
		listview = getListView();
		progressbar = (ProgressBar)findViewById(R.id.progressBar_sd);
		textSd = (TextView)findViewById(R.id.textView_sd);
		listview.setOnCreateContextMenuListener(this);
		myHandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				if (msg.what == MESSAGE_FINISH_LOADING) {
					fillList();
				} else
					adapter.notifyDataSetChanged();
			}

		};
		resource = getResources();
		progressbar.setVisibility(View.VISIBLE);
		if (!isExternalStorageAval()) {
			Toast.makeText(this, R.string.can_not_read_sdcard,
					Toast.LENGTH_LONG).show();
			progressbar.setVisibility(View.GONE);
		} else
			new GatherApkFilesTask().execute();
	}

	private boolean isExternalStorageAval() {
		return Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED);
	}

	private void fillList() {
		adapter = new FileAdapter(InstallAppActivity.this, apkInfo);
		listview.setAdapter(adapter);
		new UpdateIconTask().execute();

	}

	private FileFilter apkFilter = new FileFilter() {

		@Override
		public boolean accept(File file) {
			if (file.getName().toLowerCase().endsWith(".apk"))
				return true;
			if (file.isDirectory())
				return true;
			return false;
		}

	};

	private void gatherApkFiles(File file) {
		if (file.isFile()) {
			apkFiles.add(file);
		} else {
			try {
				File[] files = file.listFiles(apkFilter);
				for (File f : files) {
					gatherApkFiles(f);
				}
			} catch (Exception e) {

			}

		}

	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		position = info.position;
		switch (item.getItemId()) {
		case R.id.delete:
			showDialog(DIALOG_DELETE_CONFIRM);
			break;
		case R.id.path:
			String apkPath = adapter.getItem(position).getPackageName();
			showPath(apkPath);
			break;
		case R.id.share:
			String apkFile = adapter.getItem(position).getPackageName();
			File fileToShare = new File(apkFile);
			share(fileToShare);
			break;
		}
		return true;

	}

	@Override
	protected Dialog onCreateDialog(int id) {

		switch (id) {
		case DIALOG_DELETE_CONFIRM:
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			return builder.setTitle(R.string.confirm)
					.setMessage(R.string.delete_confirm)
					.setPositiveButton(R.string.yes, new OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							String apkFile = adapter.getItem(position)
									.getPackageName();
							File file = new File(apkFile);
							file.delete();
							apkInfo.remove(position);
							fillList();
						}
					}).setNegativeButton(R.string.no, new OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
						}
					}).create();

		}
		return null;
	}
	private void showPath(String apkPath){
		AlertDialog.Builder builder1 = new AlertDialog.Builder(this);

		builder1.setMessage(apkPath)
				.setPositiveButton(R.string.ok, new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				}).create().show();
	}

	// //// share file
	private void share(File file) {
		Intent mailIntent = new Intent();
		mailIntent.setAction(android.content.Intent.ACTION_SEND);
		mailIntent.setType("application/mail");
		mailIntent.putExtra(Intent.EXTRA_BCC, "");
		mailIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
		startActivity(mailIntent);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		getMenuInflater().inflate(R.menu.install_app, menu);
		menu.setHeaderTitle(R.string.options);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		try {
			File apkFile = new File(adapter.getItem(position).getPackageName());
			Intent apkIntent = new Intent();
			apkIntent.setAction(android.content.Intent.ACTION_VIEW);
			apkIntent.setDataAndType(Uri.fromFile(apkFile),
					"application/vnd.android.package-archive");
			startActivity(apkIntent);
		} catch (ActivityNotFoundException e) {
			e.printStackTrace();
		}

	}

	private class GatherApkFilesTask extends AsyncTask<File, ApkInfo, Boolean> {

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			if(progressbar.isShown()){
				progressbar.setVisibility(View.GONE);
				textSd.setVisibility(View.GONE);
			}
			Message msg = myHandler.obtainMessage(MESSAGE_FINISH_LOADING);
			msg.sendToTarget();
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected Boolean doInBackground(File... params) {
			apkFiles.clear();
			File root = Environment.getExternalStorageDirectory();
			gatherApkFiles(root);
			apkInfo.clear();
			for (File file : apkFiles) {
				Drawable icon = resource.getDrawable(R.drawable.appicon);
				apkInfo.add(new ApkInfo(icon, file.getPath(), file.getName(),
						file.length(), ""));
			}
			return true;
		}

	}

	private class UpdateIconTask extends AsyncTask<File, ApkInfo, Boolean> {

		@Override
		protected Boolean doInBackground(File... params) {
			try {
				for (ApkInfo fi : apkInfo) {
					try {
						String apkpath = fi.getPackageName();
						final PackageManager pm = InstallAppActivity.this
								.getPackageManager();
						PackageInfo pkg = pm.getPackageArchiveInfo(apkpath, 0);
						ApplicationInfo appInfo = pkg.applicationInfo;
						appInfo.sourceDir = apkpath;
						appInfo.publicSourceDir = apkpath;
						Drawable icon = pm.getApplicationIcon(appInfo);
						String label = (String) pm.getApplicationLabel(appInfo);
						String version = (String) pkg.versionName;
						fi.setDrawable(icon);
						fi.setLabel(label);
						if (version != null)
							fi.setVersion(version);
						publishProgress(fi);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			} catch (Exception e) {

			}
			return true;
		}

		@Override
		protected void onProgressUpdate(ApkInfo... values) {
			super.onProgressUpdate(values);
			adapter.notifyDataSetChanged();
		}

	}

}
