package com.nikhil.SparkStreamingProject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.AbstractJavaRDDLike;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.Optional;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.VoidFunction;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;

import scala.Tuple2;

import org.json.JSONArray;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

/**
 * Hello world!
 *
 */
public class ProjectSolution 
{
    public static void main( String[] args ) throws InterruptedException
    {
    	//The Spark Configuration is declared with Local with all of it's core as Master.The Name given for this 
    	//is StreamingProjectApplication.
        SparkConf conf = new SparkConf().setMaster("local[*]").setAppName("StreamingProjectApplication");
        //Creating Java Streaming Context from Spark Conf with 60 seconds as Batch Interval, because
        //we need to convert each file generated by python script need to be converted into JavaDStream and
        //the rate at which the files are generated is 1 per 60 seconds.
        JavaStreamingContext jssc = new JavaStreamingContext(conf,Durations.seconds(60));
		//Setting logger configuration to display only Error level messages in console.
        Logger.getRootLogger().setLevel(Level.ERROR);
        //The JavaDStream is created from text files and the location is as specified.
		JavaDStream<String> stream = jssc.textFileStream("C://Users//narendra//Desktop//Bluj//SparkStreamingData");
 //------START OF QUESTION 1 PROCESSING------------------------->
		/*The process of extracting Simple Moving Average using DStreams is followed as:
         * 1. We parsed json and assigned/mapped it into Stock model.After flatMap transformation we are having list of stocks
         * 2. For calculating SMA we only needed the Closing price of that particular stock,So created a pair with Symbol(Stock Name) and the closing price using MaptoPair transformation.
         * 3. As specified in the question the calculation should happen with 5 min sliding window and 10 min interval to be considered.So using reduceKeyByWindow transformation calculated sum of all closing prices in the interval.
         * 4. Now we have the sum of all closing prices to get average we divided it by 10 as we predefined the interval could be 10 mins,So the 10 records would be available at every interval.
         * 5. Now we have the specified output with us, we have used repartition with 1 because to store the values of 4 stocks in the same file.
         * 6. The saveAsTextFiles will create a file in the project local directory with the required output in it.
         * Assumption: One output file is generated for each sliding interval.Total of 6 files will be generated in 30 mins window         */
			stream.flatMap(line -> { 
        	JsonParser jParser = new JsonParser();	
            JsonElement stockJSON = jParser.parse(line);
            Gson gson = new Gson();
            Stock[] stockArray = gson.fromJson(stockJSON, Stock[].class);
            ArrayList<Stock> stocks = new ArrayList<Stock>();
           for(Stock s : stockArray ){
        	   stocks.add(s);
        	}
           return stocks.iterator();
        })
        .mapToPair(stock -> 
        	 new Tuple2<>(stock.getSymbol(),stock.getPriceData().getClose()))
        .reduceByKeyAndWindow(new Function2<Double,Double,Double>(){
			@Override
			public Double call(Double arg0, Double arg1) throws Exception {
				return arg0+arg1;
			}
        }, Durations.minutes(10), Durations.minutes(5), 1)
        .mapValues(new Function<Double,Double>(){
			@Override
			public Double call(Double arg0) throws Exception {
				return arg0/10;
			}
        })
        .repartition(1)
        .dstream().saveAsTextFiles("sma", "txt");
//------END OF QUESTION 1 PROCESSING------------------------->
//------START OF QUESTION 2 PROCESSING------------------------->
			/*The process of extracting Simple Moving Average using DStreams is followed as:
	         * 1. We parsed json and assigned/mapped it into Stock model.After flatMap transformation we are having list of stocks
	         * 2. For calculating the stock out of the four stocks giving maximum profit we need the price data of that particular stock and the symbol of that stock,So created a pair with Symbol(Stock Name) and the pricedata using MaptoPair transformation.
	         * 3. As specified in the question the calculation should happen with 5 min sliding window and 10 min interval to be considered.So using reduceKeyByWindow transformation calculated sum of all closing prices and opening prices in the interval.
	         * 4. Now we have the sum of all closing prices/opening prices to get average we divided it by 10 as we predefined the interval could be 10 mins,So the 10 records would be available at every interval.
	         * 5. The subsequent transformations are performed inorder to acheive the sorted list of the (average closing price - average opening price)  
	         * 6. The only top stock need to be picked
	         *Doubt about this line --Now we have the specified output with us, we have used repartition with 1 because to store the values of 4 stocks in the same file.
	         * 6. The saveAsTextFiles will create a file in the project local directory with the required output in it.
	         * Assumption: One output file is generated for each sliding interval.Total of 6 files will be generated in 30 mins window         */
			stream.flatMap(line -> {
	        	JsonParser jParser = new JsonParser();	
	            JsonElement stockJSON = jParser.parse(line);
	            Gson gson = new Gson();
	            Stock[] stockArray = gson.fromJson(stockJSON, Stock[].class);
	            ArrayList<Stock> stocks = new ArrayList<Stock>();
	           for(Stock s : stockArray ){
	        	   stocks.add(s);
	        	}
	           return stocks.iterator();
	        })
	        .mapToPair(stock -> 
	        	 new Tuple2<>(stock.getSymbol(),stock.getPriceData()))
	        .reduceByKeyAndWindow(new Function2<PriceData,PriceData,PriceData>(){
	        	@Override
				public PriceData call(PriceData arg0, PriceData arg1)
						throws Exception {
					PriceData ret = new PriceData(); 
					ret.setOpen(arg0.getOpen() + arg1.getOpen());
					ret.setClose(arg0.getClose() + arg1.getClose());
					return ret;
				}
	        }, Durations.minutes(10), Durations.minutes(5), 1)
	        .mapValues(new Function<PriceData,PriceData>(){
	        	@Override
				public PriceData call(PriceData arg0) throws Exception {
					PriceData ret = new PriceData();
					ret.setClose(arg0.getClose()/10);
					ret.setOpen(arg0.getOpen()/10);
	        		return ret;
				}
	        })
	        .mapValues(new Function<PriceData,Double>(){
				@Override
				public Double call(PriceData arg0) throws Exception {
					return arg0.getClose() - arg0.getOpen();
				}
	        }).map(new Function<Tuple2<String,Double>,Tuple2<Double,String>>(){
				@Override
				public Tuple2<Double, String> call(
						Tuple2<String, Double> arg0) throws Exception {
					return new Tuple2<Double,String>(arg0._2,arg0._1);
				}
			}).foreachRDD(new VoidFunction<JavaRDD<Tuple2<Double,String>>>(){
				@Override
				public void call(JavaRDD<Tuple2<Double, String>> arg0)
						throws Exception {
				 Tuple2<Double,String> output = arg0.sortBy(new Function<Tuple2<Double,String>,Double>(){

						@Override
						public Double call(Tuple2<Double, String> arg0)
								throws Exception {
							return arg0._1;
						}
					}, false, 1).take(1).get(0);
				 System.out.println("Question2.Top Stock with more profits ::"+output._2);
				}
	        });/*.print();//Extract Top column and print it.
*/	       /* .repartition(1)
	        .dstream().saveAsTextFiles("top-stock", "txt");*/
//------END OF QUESTION 2 PROCESSING------------------------->
//------START OF QUESTION 4 PROCESSING------------------------->
			/*The process of extracting Simple Moving Average using DStreams is followed as:
	         * 1. We parsed json and assigned/mapped it into Stock model.After flatMap transformation we are having list of stocks
	         * 2. For calculating the trading volume of the four stocks we need the volume data of that particular stock and the symbol of that stock,So created a pair with Symbol(Stock Name) and the pricedata using MaptoPair transformation.
	         * 3. As specified in the question the calculation should happen with 5 min sliding window and 10 min interval to be considered.So using reduceKeyByWindow transformation calculated sum of all volumes in the interval.
	         * 4. Now we have the sum of all volumes.
	         * 5. The subsequent transformations are performed inorder to achieve the sorted list of the volumes.  
	         * 6. The only top stock need to be picked
	         *Doubt about this line --Now we have the specified output with us, we have used repartition with 1 because to store the values of 4 stocks in the same file.
	         * 6. The saveAsTextFiles will create a file in the project local directory with the required output in it.
	         * Assumption: One output file is generated for each sliding interval.Total of 6 files will be generated in 30 mins window         */
			
			stream.flatMap(line -> {
	        	JsonParser jParser = new JsonParser();	
	            JsonElement stockJSON = jParser.parse(line);
	            Gson gson = new Gson();
	            Stock[] stockArray = gson.fromJson(stockJSON, Stock[].class);
	            ArrayList<Stock> stocks = new ArrayList<Stock>();
	           for(Stock s : stockArray ){
	        	   stocks.add(s);
	        	}
	           return stocks.iterator();
	        })
	        .mapToPair(stock -> new Tuple2<>(stock.getSymbol(),stock.getPriceData().getVolume()))
	        .reduceByKeyAndWindow(new Function2<Double,Double,Double>(){
				@Override
				public Double call(Double arg0, Double arg1) throws Exception {
					return arg0+arg1;
				}
	        }, Durations.minutes(10), Durations.minutes(5), 1)
	        .map(new Function<Tuple2<String,Double>,Tuple2<Double,String>>(){
				@Override
				public Tuple2<Double, String> call(
						Tuple2<String, Double> arg0) throws Exception {
					return new Tuple2<Double,String>(arg0._2,arg0._1);
				}
			})
	        .foreachRDD(new VoidFunction<JavaRDD<Tuple2<Double,String>>>(){
				@Override
				public void call(JavaRDD<Tuple2<Double, String>> arg0)
						throws Exception {
				 Tuple2<Double,String> output = arg0.sortBy(new Function<Tuple2<Double,String>,Double>(){

						@Override
						public Double call(Tuple2<Double, String> arg0)
								throws Exception {
							return arg0._1;
						}
					}, false, 1).take(1).get(0);
				 System.out.println("Question4.Highest Volumes traded by ::"+output._2);
				}
	        });
			/*.repartition(1)
	        .dstream().saveAsTextFiles("volume", "txt");*/
//------END OF QUESTION 4 PROCESSING------------------------->

			jssc.start();
	jssc.awaitTermination();
	jssc.close();
    }	
}
