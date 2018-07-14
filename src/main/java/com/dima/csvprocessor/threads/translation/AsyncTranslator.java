package com.dima.csvprocessor.threads.translation;

import java.util.function.Consumer;

/**
 * Translator (Google API or alternative) client
 *
 */
class AsyncTranslator implements Runnable {

	public final long DELAY = 200;
	
	private final TranslationBundle bundle;
	private Consumer<TranslationBundle> callback;
	
	public AsyncTranslator(TranslationBundle translationBundle, Consumer<TranslationBundle> callback) {
		this.bundle = translationBundle;
		this.callback = callback;
	}

	@Override
	public void run() {
		String translated = translate(bundle.getText());		
		callback.accept(new TranslationBundle(bundle.getIndex(), translated));
	}	
	
	/**
	 * Could call a real remote endpoint.
	 * Mock implementation just simulates a network delay and returns the same text
	 * @param src string to translate
	 * @return translated string
	 */
	private String translate(String src) {
		try {
			Thread.sleep(DELAY);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return src;
	}

}
