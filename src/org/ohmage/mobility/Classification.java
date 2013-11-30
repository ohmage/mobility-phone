/*******************************************************************************
 * Copyright 2012 The Regents of the University of California
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.ohmage.mobility;

import java.util.List;

/**
 * A classification of sensor data into features and a mobility mode.
 */
public class Classification {
	private String mode;
	private List<Double> fft;
	private String wifiMode;
	private Double average;
	private Double variance;
	private boolean hasFeatures;
	private int wifiTotal;
	private int wifiRecogTotal;
	private double radius;
	private double travelled;
	private String locationMode;
	
//	private ArrayList<Double> N95Fft;
//	private Double N95Variance;
	
	/**
	 * Creates a mutable Classification instance.
	 */
	public Classification() {
		
	}
	
	public String getMode() {
		return mode;
	}
	
	public void setMode(String mode) {
		this.mode = mode;
	}
	
	public List<Double> getFft() {
		return fft;
	}
	
	public void setFft(List<Double> fft) {
		this.fft = fft;
	}
	
	public Double getAverage() {
		return average;
	}
	
	public void setAverage(Double average) {
		this.average = average;
	}
	
	public Double getVariance() {
		return variance;
	}
	
	public void setVariance(Double variance) {
		this.variance = variance;
	}
	
//	public List<Double> getN95Fft() {
//		return N95Fft;
//	}
//	
//	public void setN95Fft(List<Double> n95Fft) {
//		N95Fft = (ArrayList<Double>) n95Fft;
//	}

//	public Double getN95Variance() {
//		return N95Variance;
//	}
//	
//	public void setN95Variance(Double n95Variance) {
//		N95Variance = n95Variance;
//	}

	public boolean hasFeatures() {
		return hasFeatures;
	}
	
	public void setHasFeatures(boolean hasFeatures) {
		this.hasFeatures = hasFeatures;
	}
	
	public String getWifiMode() {
		return wifiMode;
	}
	
	public void setWifiMode(String wifiMode) {
		this.wifiMode = wifiMode;
	}

	@Override
	public String toString() {
		return "Classification [mode=" + mode + ", fft=" + fft + ", wifiMode="
				+ wifiMode + ", average=" + average + ", variance=" + variance
				+ ", hasFeatures=" + hasFeatures + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((average == null) ? 0 : average.hashCode());
		result = prime * result + ((fft == null) ? 0 : fft.hashCode());
		result = prime * result + (hasFeatures ? 1231 : 1237);
		result = prime * result + ((mode == null) ? 0 : mode.hashCode());
		result = prime * result
				+ ((variance == null) ? 0 : variance.hashCode());
		result = prime * result
				+ ((wifiMode == null) ? 0 : wifiMode.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Classification other = (Classification) obj;
		if (average == null) {
			if (other.average != null)
				return false;
		} else if (!average.equals(other.average))
			return false;
		if (fft == null) {
			if (other.fft != null)
				return false;
		} else if (!fft.equals(other.fft))
			return false;
		if (hasFeatures != other.hasFeatures)
			return false;
		if (mode == null) {
			if (other.mode != null)
				return false;
		} else if (!mode.equals(other.mode))
			return false;
		if (variance == null) {
			if (other.variance != null)
				return false;
		} else if (!variance.equals(other.variance))
			return false;
		if (wifiMode == null) {
			if (other.wifiMode != null)
				return false;
		} else if (!wifiMode.equals(other.wifiMode))
			return false;
		return true;
	}

	public int getWifiRecogTotal() {
		return wifiRecogTotal;
	}
	
	public void setWifiRecogTotal(int total)
	{
		this.wifiRecogTotal = total;
	}
	
	public void setWifiTotal(int total)
	{
		this.wifiTotal = total;
	}

	public int getWifiTotal() {
		// TODO Auto-generated method stub
		return wifiTotal;
	}

	public double getWifiRecogRatio() {
		if (wifiTotal > 0)
			return (double)wifiRecogTotal / wifiTotal;
		else 
			return Double.NaN;
	}
	
	public double getRadius() {
		return radius;
	}

	public void setRadius(double r) {
		this.radius = r;
		
	}

	public void setTravelled(double t) {
		this.travelled = t;
		
	}

	public void updateWifi(Classification wifiClassification) {
		this.wifiMode = wifiClassification.wifiMode;
		this.wifiRecogTotal = wifiClassification.wifiRecogTotal;
		this.wifiTotal = wifiClassification.wifiTotal;
		
	}

	public void updateLocation(Classification locationClassification) {
		this.radius = locationClassification.getRadius();
		this.travelled = locationClassification.getTravelled();
		this.locationMode = locationClassification.getLocationMode();
		
		
	}

	public double getTravelled() {
		return travelled;
	}

	public void setLocationMode(String locMode) {
		this.locationMode = locMode;
	}
	
	public String getLocationMode()
	{
		return locationMode;
	}
}
