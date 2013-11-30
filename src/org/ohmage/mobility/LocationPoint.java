package org.ohmage.mobility;

public class LocationPoint {
	private double latitude;
	private double longitude;
	private long time;
	
	public LocationPoint (double latitude, double longitude, long time)
	{
		this.setLatitude(latitude);
		this.setLongitude(longitude);
		this.time = time;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	@Override
	public String toString() {
		return time + "," + latitude + "," + longitude;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}
	
	
	
}
