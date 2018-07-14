package com.dima.csvprocessor;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

import com.dima.csvprocessor.streams.StreamCsvProcessor;
import com.dima.csvprocessor.threads.ThreadBasedProcessor;

public class Main {

	public static final int TOP = 10000;
	
	public static ConcurrentMap<String, AtomicLong> cnt = new ConcurrentHashMap<>();
	public static LinkedBlockingQueue<String> queue = new LinkedBlockingQueue<>(100);

	public static void main(String[] args) throws InterruptedException, IOException {
		
		Path path;
		int top = TOP;
		
		if (args.length >=1) {
			path = Paths.get(args[0]).toAbsolutePath();
		} else {
			// opening the input stream will eliminate the need to reference as src/main etc.
			// but this way we can obtain path on both windows and linux.
			// also, it is just an exercise
			path = Paths.get("src/main/resources/LocalReviews.csv").toAbsolutePath();						
		}
		
		if (args.length >=2) {
			top = Integer.parseInt(args[1]);
		}
		
		String tmp = System.getProperty("translate");
		
		ThreadBasedProcessor p = new ThreadBasedProcessor();
		
		if (Boolean.valueOf(tmp).booleanValue()) {
			System.out.println("Translating reviews");
			p.translateRevires(path);
		} else {
//			essentially performs event better then explicit multithreading
//			with significantly less code			
//			even though not explicitly configured to do so
//			
			StreamCsvProcessor streamProcessor = new StreamCsvProcessor();
			System.out.println(streamProcessor.findMostActiveUsers(path, top));
			
			System.out.println(top + " most active users (total number of reviews)");
			System.out.println(p.mostActiveUsers(path, top));
			
			System.out.println(top + " most reviewed products");
			System.out.println(p.mostReviewedItems(path, top));		
			
			System.out.println(top + " most frequent words (lenght > 2)");
			System.out.println(p.mostUsedWords(path, top));
		}
	}
}
