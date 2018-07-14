package com.dima.csvprocessor.threads;

import java.nio.file.Path;
import java.util.List;

import com.dima.csvprocessor.threads.processors.ProcessorFactory.ProcessorType;

public class ThreadBasedProcessor {
	
	public List<String> mostActiveUsers(Path inputFilePath, int top) {
		Aggregator aggregastor = new Aggregator();
		return aggregastor.aggregate(inputFilePath, ProcessorType.Users, top);
	}

	public List<String> mostReviewedItems(Path inputFilePath, int top) {
		Aggregator aggregastor = new Aggregator();
		return aggregastor.aggregate(inputFilePath, ProcessorType.Products, top);
	}
	
	public List<String> mostUsedWords(Path inputFilePath, int top) {
		Aggregator aggregastor = new Aggregator();
		return aggregastor.aggregate(inputFilePath, ProcessorType.Words, top);		
	}

	public void translateRevires(Path inputFilePath) {
		Translator translator = new Translator();
		translator.translate(inputFilePath);
	}
}
