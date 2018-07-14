package com.dima.csvprocessor.threads.translation;

class FragmentKey {
	private final long reviewId;
	private final int fragmentId;
	
	public FragmentKey(long reviewId, int fragmentId) {
		this.reviewId = reviewId;
		this.fragmentId = fragmentId;
	}

	public long getReviewId() {
		return reviewId;
	}

	public int getFragmentId() {
		return fragmentId;
	}

	@Override
	public String toString() {		
		return "[ rev:" + reviewId + ", frag:" + fragmentId + "]";
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + fragmentId;
		result = prime * result + (int) (reviewId ^ (reviewId >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FragmentKey other = (FragmentKey) obj;
		if (fragmentId != other.fragmentId)
			return false;
		if (reviewId != other.reviewId)
			return false;
		return true;
	}
}
