package com.dima.csvprocessor.threads.processors;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import com.dima.csvprocessor.model.Review;

/**
 * For processing most commented products
 *
 */
public class ProductsProcessor extends ReviewProcessor {
	public ProductsProcessor(LinkedBlockingQueue<String[]> input, ConcurrentMap<String, AtomicLong> cnt, AtomicBoolean isDone) {
		super(input, cnt, isDone);
	}

	@Override
	protected void updateCounters(ConcurrentMap<String, AtomicLong> counters, Review review) {
		AtomicLong previous = counters.putIfAbsent(review.getProductId(), new AtomicLong(1));
		if (previous != null) {
			previous.incrementAndGet();
		}
	}		
}