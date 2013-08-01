package com.wenhui.taskmanager;

import android.graphics.drawable.Drawable;

public class ApkInfo {

	private Drawable drawable;
	private String packageName;
	private String label;
	private long size;
	private String version;
	private boolean check;
	public ApkInfo(Drawable drawable, String file, String label, long size, String version){
		this.drawable = drawable;
		this.packageName=file;
		this.label = label;
		this.size = size;
		this.version=version;
		this.check=false;
	}
	
	public void setDrawable(Drawable drawable){
		this.drawable = drawable;
	}
	
	public void setFile(String file){
		this.packageName = file;
	}
	
	public void setLabel(String label){
		this.label = label;
	}
	
	public Drawable getDrawable(){
		return drawable;
	}
	
	public String getPackageName(){
		return packageName;
	}
	
	public String getLabel(){
		return label;
	}
	
	public void setSize(long size){
		this.size = size;
	}
	public long getSize(){
		return size;
	}
	
	public void setVersion(String newV){
		version = newV;
	}
	public String getVersion(){
		return version;
	}
	public void setChecked(boolean check){
		this.check = check;
	}


	public boolean isChecked(){
		return check;
	}

	@Override
	public boolean equals(Object o) {
		if(this == o) 
			return true;
		if(!(o instanceof ApkInfo)){
			return false;
		}
		
		ApkInfo that = (ApkInfo)o;
		return packageName.equals(that.getPackageName());
	}
	
	@Override
	public int hashCode() {
//		Log.i("hashcode method", "is called");
		int result;
		result = (packageName != null)? packageName.hashCode() : 0;
		result = 31*result + (label != null ? label.hashCode():0);
		return result;
	}
	
}
