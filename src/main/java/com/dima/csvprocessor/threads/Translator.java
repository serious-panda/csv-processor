package com.dima.csvprocessor.threads;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import com.dima.csvprocessor.Util;
import com.dima.csvprocessor.model.Review;
import com.dima.csvprocessor.threads.translation.TranslatorService;
import com.opencsv.CSVReader;

public class Translator {
	
	private LinkedBlockingQueue<Review> input = new LinkedBlockingQueue<>(100);
	private LinkedBlockingQueue<Review> output = new LinkedBlockingQueue<>(100);
	
	public void translate(Path source) {
		Thread translator = new Thread(new TranslatorService(input, output));
		translator.start();
		
		Thread printer = new Thread(new Printer());
		printer.start();
		
		try (CSVReader csvReader = new CSVReader(new FileReader(source.toFile()),1, Util.getCsvParser())){
			String [] nextLine;
			while ((nextLine = csvReader.readNext()) != null)
			{ 
			    Review r = Util.toReview(nextLine);
			    if (r != null) {
			    	try {
			    		input.put(r);
			    	}catch (Exception e) {
						// TODO: handle exception
					}
			    }
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				input.put(Review.EMPTY);
				translator.join();
				printer.join();
			} catch (InterruptedException e) {				
				e.printStackTrace();
			}
		}		
	}	
	
	private class Printer implements Runnable {
		
		@Override
		public void run() {
			while (true) {
				try {
					Review translated = output.poll(5, TimeUnit.SECONDS);
					if (translated != null) {
						if (translated.getId() == -1) {							
							break;
						}
						System.out.println(translated);
					}					
				} catch (InterruptedException e) {
					// do nothing
				}
			}
		}
	}
}
