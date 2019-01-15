package com.example.mywifimanager.base;

import android.net.wifi.ScanResult;

public class MScanWifi {
	private int level;
	private String WifiName;
	public ScanResult scanResult;
	private boolean isLock;
	private boolean isExsit;
	public MScanWifi(){
		
	}
	public MScanWifi(ScanResult scanResult,String WifiName,int level,Boolean isLock){
		this.WifiName=WifiName;
		this.level=level;
		this.isLock=isLock;
		this.scanResult=scanResult;
	}
	public int getLevel() {
		return level;
	}
	public void setLevel(int level) {
		this.level = level;
	}
	public String getWifiName() {
		return WifiName;
	}
	public void setWifiName(String wifiName) {
		WifiName = wifiName;
	}
	public Boolean getIsLock() {
		return isLock;
	}
	public void setIsLock(boolean isLock) {
		this.isLock = isLock;
	}
	public boolean getIsExsit() {
		return isExsit;
	}
	public void setIsExsit(boolean isExsit) {
		this.isExsit = isExsit;
	}
	
}
