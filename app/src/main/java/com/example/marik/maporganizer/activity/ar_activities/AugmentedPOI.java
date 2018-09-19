package com.example.marik.maporganizer.activity.ar_activities;
/**
 * Created by krzysztofjackowski on 24/09/15.
 */
public class AugmentedPOI {
//	private int mId;
//	private String mName;
	private String mDescription;
	private double mLatitude;
	private double mLongitude;
	
	public AugmentedPOI(String newDescription,
                        double newLatitude, double newLongitude) {

        this.mDescription = newDescription;
        this.mLatitude = newLatitude;
        this.mLongitude = newLongitude;
	}
	
//	public int getPoiId() {
//		return mId;
//	}
//	public void setPoiId(int poiId) {
//		this.mId = poiId;
//	}
//	public String getPoiName() {
//		return mName;
//	}
//	public void setPoiName(String poiName) {
//		this.mName = poiName;
//	}
	public String getPoiDescription() {
		return mDescription;
	}
	public void setPoiDescription(String poiDescription) {
		this.mDescription = poiDescription;
	}
	public double getPoiLatitude() {
		return mLatitude;
	}
	public void setPoiLatitude(double poiLatitude) {
		this.mLatitude = poiLatitude;
	}
	public double getPoiLongitude() {
		return mLongitude;
	}
	public void setPoiLongitude(double poiLongitude) {
		this.mLongitude = poiLongitude;
	}
}
