package com.nikhil.SparkStreamingProject;

import java.io.Serializable;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
/*
 * This Model class will hold the details particular stock
 * This is Implementing Serializable, So that we can share the data among partitions without any error
 * */
public class Stock implements Serializable{
private String timestamp;
private String symbol;
private PriceData priceData;

public String getTimestamp() {
	return timestamp;
}
public void setTimestamp(String timestamp) {
	this.timestamp = timestamp;
}
public String getSymbol() {
	return symbol;
}
public void setSymbol(String symbol) {
	this.symbol = symbol;
}
public PriceData getPriceData() {
	return priceData;
}
public void setPriceData(PriceData priceData) {
	this.priceData = priceData;
}
}
