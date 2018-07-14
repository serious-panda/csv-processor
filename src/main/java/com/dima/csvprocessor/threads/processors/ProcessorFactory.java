package com.dima.csvprocessor.threads.processors;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class ProcessorFactory {

	public static enum ProcessorType {
		Users, Products, Words;
	}
	
	public static ReviewProcessor createProcessor(ProcessorType type, ConcurrentMap<String, AtomicLong> cnt, LinkedBlockingQueue<String[]> input, AtomicBoolean isDone) {
		switch (type) {
		case Users:
			return new UsersProcessor(input, cnt, isDone);
		case Products:
			return new ProductsProcessor(input, cnt, isDone);
		case Words:
			return new WordsProcessor(input, cnt, isDone);
		default:
			throw new IllegalArgumentException("type " + type + " not supported.");
		}
	}
	 
}
