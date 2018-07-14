package com.dima.csvprocessor.threads.processors;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import com.dima.csvprocessor.Util;
import com.dima.csvprocessor.model.Review;

public abstract class ReviewProcessor implements Runnable {

	private final LinkedBlockingQueue<String[]> input;
	private final ConcurrentMap<String, AtomicLong> counters;
	private final AtomicBoolean isDone;

	protected ReviewProcessor(LinkedBlockingQueue<String[]> input, ConcurrentMap<String, AtomicLong> cnt, AtomicBoolean isDone) {
		this.input = input;
		this.isDone = isDone;
		this.counters = cnt;
	}

	protected abstract void updateCounters(final ConcurrentMap<String, AtomicLong> counters, final Review review);

	@Override
	public void run() {
		while (!isDone.get()) {
			try {
				String[] line = input.poll(1, TimeUnit.SECONDS);
				if (line != null) {
					Review review = Util.toReview(line);
					if (review != null) {
						updateCounters(counters, review);
					}
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
	}
}
