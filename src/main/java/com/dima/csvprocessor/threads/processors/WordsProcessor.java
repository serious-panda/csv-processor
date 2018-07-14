package com.dima.csvprocessor.threads.processors;

import java.util.Arrays;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang3.StringUtils;

import com.dima.csvprocessor.model.Review;

/**
 * for processing most frequent words
 *
 */
public class WordsProcessor extends ReviewProcessor {
	
	public WordsProcessor(LinkedBlockingQueue<String[]> input, ConcurrentMap<String, AtomicLong> cnt, AtomicBoolean isDone) {
		super(input, cnt, isDone);
	}

	protected void updateCounters(final ConcurrentMap<String, AtomicLong> counters, final Review review) {
		Arrays.stream(review.getText()
				.split("\\W"))
				.filter(StringUtils::isNotBlank)
				.filter(s->s.length() > 2)
				.forEach((w)-> {
					AtomicLong previous = counters.putIfAbsent(w.toLowerCase(), new AtomicLong(1));
					if (previous != null) {
						previous.incrementAndGet();
					}
		});			
	}		
}

