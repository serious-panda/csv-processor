package com.dima.csvprocessor.streams;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.dima.csvprocessor.Util;
import com.dima.csvprocessor.model.Review;
import com.opencsv.CSVParser;

public class StreamCsvProcessor {

	static CSVParser parser = new CSVParser();
	
	public List<String> findMostCommentedItems(Path inputFilePath, int top) throws IOException {
		List<String> itemsList = Collections.emptyList();
		
		try (Stream<Review> reviews = getReviewStream(inputFilePath)) {
			Map<String, Long> aggregatedItems = reviews.collect(
					Collectors.groupingBy(Review::getProductId, Collectors.counting()));

			itemsList = Util.getTopByValue(aggregatedItems, top);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return itemsList;
	}
	
	public List<String> findMostActiveUsers(Path inputFilePath, int top) throws IOException {
		List<String> usersList = Collections.emptyList();
		try (Stream<Review> reviews = getReviewStream(inputFilePath)) {
			Map<String, Long> aggregatedUsers = reviews.collect(
							Collectors.groupingBy(Review::getProfileName, Collectors.counting()));
			usersList = Util.getTopByValue(aggregatedUsers, top);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return usersList;
	}

	private static String[] parse(String line) {
		String [] s = null;
		try {
			s = parser.parseLine(line);
		} catch (IOException e) {			
			e.printStackTrace();
		}
		return s;
	}
	
	private Stream<Review> getReviewStream(Path path) throws IOException{
		return Files.lines(path)
					.skip(1) 						// csv header
					.map(StreamCsvProcessor::parse) // split CSV to tokens
					.map(Util::toReview)			// convert to Review object
					.filter(Objects::nonNull);
	}

}
