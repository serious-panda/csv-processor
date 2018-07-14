package com.dima.csvprocessor.threads.translation;

import java.util.List;

import com.dima.csvprocessor.model.Review;

class ProcessedReview {
	private final Review review;
	private final Fragment[] fragments;
	
	public ProcessedReview(Review review, List<Fragment> fragments) {
		this.review = review;
		this.fragments = fragments.toArray(new Fragment[0]);
	}

	public Review getReview() {
		return review;
	}

	public Fragment[] getFragments() {
		return fragments;
	}
	
	/**
	 * Combines all translated fragments into new review
	 * instance
	 * @return
	 * @throws IllegalStateException if review is not yet translated
	 */
	public Review getTranslatedReview() {
		if (!isTranslated()) {
			throw new IllegalStateException("review is not translated yet");
		}
		StringBuilder b = new StringBuilder();		
		for(Fragment f : fragments) {
			b.append(' ').append(f.getText());
		}
		// TODO probably creating new review should be better
		review.setText(b.length() == 0 ? "" : b.substring(1));
		return review;
	}
	
	/**
	 * checks that all fragments have been translated
	 * @return
	 */
	public boolean isTranslated() {
		boolean completed = true;
		for(Fragment f : fragments) {
			if (f == null) {
				completed = false;
				break;
			}
		}
		return completed;
	}

	/**
	 * replaces text of a fragment (a translation)
	 * although there will be no two threads changing the same fragment,
	 * it should be synchronized so all threads see all changes when 
	 * checking review for translation completion
	 * @param fragment
	 */
	public synchronized boolean replaceFragment(Fragment fragment) {
		if (fragment.getKey().getReviewId() != review.getId()) {
			throw new IllegalArgumentException("Review id does not match to fragment");
		}
		fragments[fragment.getKey().getFragmentId()] = fragment;
		return isTranslated();
	}
}
