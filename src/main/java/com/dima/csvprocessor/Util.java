package com.dima.csvprocessor;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.dima.csvprocessor.model.Review;
import com.opencsv.CSVParser;

public class Util {

	public static CSVParser getCsvParser() {
		// the escape char is a kind of hack. this char is not expected to be in review
		// in this library it is not possible to not to use escape character 
		return new CSVParser(',', '"', 'ש');
	}

	// CSV indexes to test for content
	private static final int[] indexesToTest = new int[] {0,1,2,3};
	
	public static Review toReview(String[] line) {
		
		Review item = null;
		try {
			if (isNotBlank(line)) {
				item = new Review(Long.parseLong(line[0].trim()));
				item.setProductId(line[1].trim());
				item.setUserId(line[2].trim());
				item.setProfileName(line[3].trim());
				String text = line[9].trim();
				if (!text.endsWith(".") && !text.endsWith("!") && !text.endsWith("?")) {
					text = text + ".";
				}
				
				item.setText(text);
			}
		} catch (Exception e) {
			item = null;
		}
		return item;
	}

	/**
	 * Make sure the input is not null and all relevant indices are not blank
	 * @param line
	 * @return
	 */
	private static boolean isNotBlank(String[] line) {
		boolean notBlank = false;
		if (line != null) {
			notBlank = true;
			for(int i : indexesToTest) {
				notBlank  &= StringUtils.isNotBlank(line[i]);
			};
		}
		return notBlank;
	}

	public static List<String> splitSentences(String text){
		List<String> sentences = new ArrayList<>();
		Locale locale = Locale.US;
		BreakIterator breakIterator =
		        BreakIterator.getSentenceInstance(locale);
		breakIterator.setText(text);

		int lastIndex = breakIterator.first();
        while (lastIndex != BreakIterator.DONE) {
            int firstIndex = lastIndex;
            lastIndex = breakIterator.next();
            
            if (lastIndex != BreakIterator.DONE) {
            	sentences.add(text.substring(firstIndex, lastIndex).trim());
            }
        }
        return sentences.isEmpty() ? Collections.emptyList() : sentences;
	}
	
	public static String concatSentences(List<String> sentences){		
		StringBuilder txt = new StringBuilder(sentences.size());
		for(String s : sentences) {
			txt.append(s).append(' ');
		}
		if (txt.length() > 0 ) {
			txt.deleteCharAt(txt.length()-1); // remove last 'space' char
		}
		return txt.toString();
	}
	
	/**
	 * Try to check if review can be safely split to sentences and
	 * concatenated back without sticking to another splited review.
	 * Unfortunately it does not cover for all the cases. 
	 * Util.toReview should handle more situations correctly. At least
	 * a comma char in the review caption.
	 * @param review
	 * @return
	 */
	public static boolean isSplitable(Review review){
		List<String> origSentences = Util.splitSentences(review.getText());
		List<String> twiceSentences = new ArrayList<String>(origSentences);
		twiceSentences.addAll(origSentences);
		String tmp = Util.concatSentences(twiceSentences);
		List<String> twiceSplit = Util.splitSentences(tmp);
		boolean equals = twiceSplit.equals(twiceSentences) &&
				origSentences.equals(twiceSplit.subList(0, origSentences.size()));
		return equals;
	}
	
	/**
	 * Sort provided map by values in reverse order (Desc.) and return N keys 
	 * representing top entries/
	 * @param map
	 * @param count
	 * @return
	 */
	public static List<String> getTopByValue(Map<String, Long> map, int count){
		return map.entrySet().stream()
							.sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
							.limit(count)
							.map(Map.Entry::getKey)
							.collect(Collectors.toList());
	}
}
