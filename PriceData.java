package com.nikhil.SparkStreamingProject;

import java.io.Serializable;
/*
 * This Model class will hold the details of date related prices of particular stock
 * This is Implementing Serializable, So that we can share the data among partitions without any error
 * */
public class PriceData implements Serializable{
	private Double high;
	private Double close;
	private Double open;
	private Double low;
	private Double volume;
	public Double getHigh() {
		return high;
	}
	public void setHigh(Double high) {
		this.high = high;
	}
	public Double getClose() {
		return close;
	}
	public void setClose(Double close) {
		this.close = close;
	}
	public Double getOpen() {
		return open;
	}
	public void setOpen(Double open) {
		this.open = open;
	}
	public Double getLow() {
		return low;
	}
	public void setLow(Double low) {
		this.low = low;
	}
	public Double getVolume() {
		return volume;
	}
	public void setVolume(Double volume) {
		this.volume = volume;
	}

}
