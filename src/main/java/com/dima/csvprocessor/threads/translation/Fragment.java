package com.dima.csvprocessor.threads.translation;

class Fragment{
	private final FragmentKey key;
	private final String text;
	
	public Fragment(long reviewId, int fragmentId, String text) {
		this.key = new FragmentKey(reviewId, fragmentId);
		this.text = text;
	}

	public Fragment(FragmentKey key, String text) {
		this.key = key;
		this.text = text;
	}

	public FragmentKey getKey() {
		return key;
	}

	public String getText() {
		return text;
	}
	@Override
	public String toString() {		
		return "[" + key + ", " + text + "]";
	}
}
