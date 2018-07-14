package com.dima.csvprocessor.threads;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.dima.csvprocessor.Util;
import com.dima.csvprocessor.threads.processors.ProcessorFactory;
import com.dima.csvprocessor.threads.processors.ProcessorFactory.ProcessorType;
import com.opencsv.CSVParser;

class Aggregator {

	private final ConcurrentMap<String, AtomicLong> cnt = new ConcurrentHashMap<>();
	private final LinkedBlockingQueue<String[]> queue = new LinkedBlockingQueue<>(100);
	private final AtomicBoolean isDone = new AtomicBoolean(false);
	
	List<String> aggregate(Path source, ProcessorType type, int top) {
		List<String>  result = Collections.emptyList();
		CSVParser p = Util.getCsvParser();
		List<Thread> workers = getWorkers(type);
		
		try (Stream<String> lines = Files.lines(source)) {
			lines.skip(1)				
				.forEach(s -> {
				try {
					String[] tokens = p.parseLine(s);
					queue.put(tokens);
				} catch (InterruptedException | IOException e) {
					e.printStackTrace();
				}
			});
			
			isDone.set(true);

			for (Thread t : workers) {
				  t.join();
			}
					
			result = getTop(cnt, top);

		} catch (InterruptedException | IOException e) {
			e.printStackTrace();
			result = Collections.emptyList();
		}
		return result;
	}

	private List<Thread> getWorkers(ProcessorType type){
		int count = Runtime.getRuntime().availableProcessors();
		List<Thread> workers = new ArrayList<>(count);
		for(int i=0; i<count; i++) {
			Thread t = new Thread(ProcessorFactory.createProcessor(type, cnt, queue, isDone));
			t.start();
			workers.add(t);
		}
		return workers;
	}

	private List<String> getTop(Map<String, AtomicLong> map, int count){
		// convert from map of String->AtomicLong to String->Long
		// so the same utility method will be used.
		Map<String, Long> convertedMap = map.entrySet().stream().collect(
				Collectors.toMap(	Map.Entry::getKey, 
									e -> Long.valueOf(e.getValue().get())));
		
		return Util.getTopByValue(convertedMap, count);
	}
}
