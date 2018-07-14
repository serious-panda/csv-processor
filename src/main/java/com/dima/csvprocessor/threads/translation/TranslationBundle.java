package com.dima.csvprocessor.threads.translation;

import java.util.Collections;
import java.util.List;

class TranslationBundle {
	public final List<FragmentKey> index;
	public final String text;
	
	public TranslationBundle(List<FragmentKey> index, String text) {
		this.index = Collections.unmodifiableList(index);
		this.text = text;
	}

	public List<FragmentKey> getIndex() {
		return index;
	}

	public String getText() {
		return text;
	}	
}
