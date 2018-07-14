# csv-processor

Csv processor exercise. Load file and:
1. Find N most active users (total number of reviews)
2. Find N most reviewed products
3. Find N most frequent words (lenght > 2)
4. Translate reviews

## Version
0.0.1-SNAPSHOT

## Prerequisites
* Maven 3.x
* Java 8

## Build
```
mvn clean package
```
## Run
To run on embedded example of reviews (500 reviews) with top set to 10
```sh
java -jar target\review-processor-0.0.1-SNAPSHOT.jar
```
To run on custom file and threshold
```sh
java -jar target\review-processor-0.0.1-SNAPSHOT.jar <file> <threshold>
```
To translate the reviews
```sh
java -Dtranslate=true -jar target\review-processor-0.0.1-SNAPSHOT.jar <file>
```

### Usage note:
 - Points 1 to 3 and translation are mutually exclusive.
 - Due to the nature of an exercise there is a limited user input validation
 - There is no logging. All errors printed to system out/err

## Implementation details
 - It process the records one by one, without loading entire file into memory, so even with huge input files the memory consumption is not significant. To further restrict memory usage one can apply -Xmx flag.
 - Some functionality is also implemeted using Java Streams. Although it is not configured to be parralel, looks like it outperforms the explicit multithreading which takes about the same time but streams take 20% less CPU power.
 - Project creates executable jar bundled with all necessary dependencies.
 - Call to translation is mocked. It returns the same text while simulating 200 ms of web request delay.
 - Translator service uses two queues as input and output. Consumes Review from input stream and put translated review to output stream. So it is possible to chain the aggregation from points 1,2 and 3 with translation.
 
## Translation service utilization
In order to fully utilize the translation API, which accepts up tp 1000 characters, all reviews are split to sentences and then these sentences are combined to chunks of up to 1000 characters before sent for tranlation. (Sentence is a smallest logical units that cannot be split any further without potentially changing words meaning.)

## To Do
1. Introduce custom token for combining sentences from different reviews. Currently not all reviews are ended properly with punctuation and this introduces an issues when rearranging these sentences back to the reviews. The problem here is to introduce a token that will not be a legitimate review content.
2. Add filitering of duplicates in input file. Introduce a hash map that will hold a hash of a review line. This will allow validating fairly large files without consuming too much memory. O very large input files collision mayu occur so perhaps a more sophisticated function will be required.
