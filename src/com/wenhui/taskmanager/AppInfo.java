package com.wenhui.taskmanager;

import android.graphics.drawable.Drawable;

public class AppInfo {
	
	private Drawable icon;
	private String label;
	private String memoryUsage;
	private String pckname;
	private boolean isSelected;
	private int pid;
	private boolean isIgnored;
	private String status;
	public AppInfo(Drawable icon, String label, String memoryUsage, String pckname, int pid, String status, boolean isIgnored){
		this.icon = icon;
		this.label = label;
		this.memoryUsage = memoryUsage;
		this.isSelected = false;
		this.pckname = pckname;
		this.status = status;
		this.isIgnored = isIgnored;
	}
	
	public void setIcon(Drawable drawable){
		this.icon = drawable;
	}
	
	public Drawable getIcon(){
		return icon;
	}
	
	public String getLabel(){
		return label;
	}
	
	public void setMemoryUsage(String m){
		this.memoryUsage=m;
	}
	public String getMemory(){
		return memoryUsage;
	}
	public String getPckname(){
		return pckname;
	}

	public void setSelected(boolean s){
		isSelected= s;
	}
	public boolean getIsSelected(){
		return isSelected;
	}
	
	public int getPid(){
		return pid;
	}
	
	public String getStatus(){
		return status;
	}
	
	public void setIgnore(boolean b){
		isIgnored= b;
	}
	public boolean isIgnored(){
		return isIgnored;
	}
}