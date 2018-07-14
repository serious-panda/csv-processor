package com.dima.csvprocessor.threads.translation;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import com.dima.csvprocessor.Util;
import com.dima.csvprocessor.model.Review;

/**
 * Uses two queues as input and output. Consumes Review from input stream and 
 * put translated review to output stream.
 * Since it is a batch and not infinite processor, the job termination should 
 * be signaled. This service waits for 'EMPTY' review to be pulled from the 
 * queue. Alternatively some 'isDone' or other method can be exposed.
 * 
 * The approach is to split each review to sentences as this is the smallest 
 * logical unit that should be translated and then combine sentences to chunks 
 * of up to 1000 characters to maximize utilization of translation API. 
 *
 */
public class TranslatorService implements Runnable {
	
	private final static int CHUNK_SIZE = 1000;
	private final static int TRANSLATOR_CLENTS = 100;
	private final static int TRANSLATE_JOBS_Q_SIZE = 100;
	
	private LinkedBlockingQueue<Review> input;
	private LinkedBlockingQueue<Review> output;
	
	private final ConcurrentMap<Long, ProcessedReview> processed = new ConcurrentHashMap<>();
	
	private int maxChunkSize = CHUNK_SIZE;
	
	// see executor usage below for explanation on 'why'
	private final BlockingQueue<Runnable> queue = new ArrayBlockingQueue<>(TRANSLATE_JOBS_Q_SIZE);
	private final ExecutorService executor = new ThreadPoolExecutor(TRANSLATOR_CLENTS, TRANSLATOR_CLENTS, 0L, TimeUnit.MILLISECONDS, queue); 
	
	public TranslatorService(LinkedBlockingQueue<Review> input, LinkedBlockingQueue<Review> output) {
		this.input = input;
		this.output = output;
	}
		
	private void enque(Review r) {
		try {
			output.put(r);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		// accumulates next chunk for translation
		List<Fragment> chunk = new ArrayList<>();
		int chunkSize = 0; // in chars
		while (true) {
			Review r = getNextReview();
			if (r == null ) { continue; } // null means poll timed out - queue is empty
			if (r.getId() == -1) { break; } // no more reviews
			if (!Util.isSplitable(r)) {
				enque(r);
			}
			ProcessedReview review = splitToSentences(r);
			processed.put(review.getReview().getId(), review);
			// a bit dodgy part. move fragment from review object
			// to the translation chunk while reserving space 
			Fragment[] copy = Arrays.copyOf(review.getFragments(), review.getFragments().length);
			Arrays.fill(review.getFragments(), null);			
			for(Fragment fragment : copy) {
				// 1 is for a space char after each fragment
				if (chunkSize + fragment.getText().length() + 1 > maxChunkSize) {
					translateAsync(chunk);
					chunk = new ArrayList<>();
					chunkSize = 0;						
				}
				chunk.add(fragment);
				chunkSize += fragment.getText().length() + 1;
			}			
		}
		if (!chunk.isEmpty()) {
			translateAsync(chunk);
		}
		try {
			executor.shutdown();
			executor.awaitTermination(1, TimeUnit.MINUTES);
			enque(Review.EMPTY);
		} catch (InterruptedException e) {
			System.out.println("Termination wait exception");
		}
		
	}
	
	private Review getNextReview() {
		Review ret = null;
		try {
			ret = input.poll(5, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
		}
		return ret;
	}

	private void translateAsync(List<Fragment> chunk) {
		// executor will not block even if queue is blocking because it uses
		// 'offer' method. This loop makes sure to block until queue
		// can accept new jobs
		// one second will not slow down the process
		// as if queue is full it means there are a lot of 
		// pending translation jobs
		while(queue.remainingCapacity() == 0) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {				
				// do nothing
			}
		}
		TranslationBundle bnd = bundle(chunk);
		executor.submit(new AsyncTranslator(bnd, callback));	
	}

	/**
	 * Once chunk have been translated, this callback will update
	 * reviews that are referenced by translated fragments.
	 * If all fragments are translated the review considered 
	 * translated and it will be moved to output queue.
	 */
	private Consumer<TranslationBundle> callback = bundle -> {
		synchronized (processed) {			
			try {
				List<Fragment> fragments = unbundle(bundle);
				for(Fragment fragment : fragments) {
					ProcessedReview review = processed.get(fragment.getKey().getReviewId());
					boolean translated = review.replaceFragment(fragment);
					if (translated) {
						ProcessedReview completed = processed.remove(fragment.getKey().getReviewId());
						if (completed != null) {
							enque(completed.getTranslatedReview());
						}
					}
				}					
			} catch (ParseException e) {
				for(FragmentKey key : bundle.index) {
					ProcessedReview review = processed.remove(key.getReviewId());
					if (review != null) {
						enque(review.getReview()); // not translated, but at least it is not lost
					}
				}
			}
			
		}
	};
	
	ProcessedReview splitToSentences(Review review) {
		List<Fragment> fragments = new ArrayList<>();
		int fragmentId = 0;
		for(String sentence : Util.splitSentences(review.getText())) {
			fragments.add(new Fragment(review.getId(), fragmentId++, sentence));
		}
		return new ProcessedReview(review, fragments);
	}

	List<Fragment> unbundle(TranslationBundle bundle) throws ParseException {
		List<Fragment> fragments = new ArrayList<>(bundle.getIndex().size());
		int i = 0;
		for(String sentence : Util.splitSentences(bundle.getText())) {
			FragmentKey key = bundle.getIndex().get(i++);			
			fragments.add(new Fragment(key, sentence));
		}
		if (bundle.index.size() != fragments.size()) {
			throw new ParseException("Number of parsed sentences do not match the number of chunks sent in.", 0);
		}
		return fragments;
	}
	
	TranslationBundle bundle(List<Fragment> fragments) {
		List<FragmentKey> index = new ArrayList<>(fragments.size());
		StringBuilder txt = new StringBuilder(maxChunkSize);
		for(Fragment f : fragments) {
			txt.append(f.getText()).append(' ');
			index.add(f.getKey());
		}
		txt.deleteCharAt(txt.length()-1); // remove last 'space' char
		return new TranslationBundle(index, txt.toString());
	}
	
	public int getMaxChunkSize() {
		return maxChunkSize;
	}

	public void setMaxChunkSize(int maxChunkSize) {
		// size should be a positive number, the rest is quite arbitrary		
		if (maxChunkSize <=100 || maxChunkSize > 10000) {
			throw new IllegalArgumentException("chunk size should be between 100 and 10_000");
		}
		this.maxChunkSize = maxChunkSize;
	}
}
