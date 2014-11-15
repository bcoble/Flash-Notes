import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;

/**
 * @author coblebj
 *
 */
public class FlashNotes {

	/**
	 * @param args
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws ClassCastException
	 */
	public static void main(String[] args) throws ClassCastException,
			ClassNotFoundException, IOException {

		// Example arguments: 5 Presidents/AbrahamLincoln.txt summary.txt
		// Format: <number of lines> <input name> <output name>

		String serializedClassifier = "classifiers/english.muc.7class.distsim.crf.ser.gz";

		String outfile = "summary.txt";
		String infile = args[1];
		String num = args[0];
		int length_of_summary = Integer.valueOf(num);

		if (args.length > 2) {
			outfile = args[2];
		}

		// Set classifier
		AbstractSequenceClassifier<CoreLabel> classifier = CRFClassifier
				.getClassifier(serializedClassifier);

		// How to classify an input file
		if (args.length > 1) {
			// Parse file to a string
			String fileContents = IOUtils.slurpFile(infile);

			// Apply the classifier
			List<List<CoreLabel>> out = classifier.classify(fileContents);

			// Run the summarizer
			ArrayList<String> results = summarize(out, length_of_summary,
					fileContents);

			// Write results to designated file
			writeResults(results, outfile);

		}
	}

	/**
	 * Performs the summarizing function on a text that has been classified.
	 * 
	 * @param text
	 * @param cap
	 * @param doc
	 * @return
	 */
	public static ArrayList<String> summarize(List<List<CoreLabel>> text,
			int cap, String doc) {

		ArrayList<List<CoreLabel>> summary = new ArrayList<List<CoreLabel>>();

		// Finds who the bio is about.
		String bio_person = "Harry Potter";
		int current_count = 0;
		for (List<CoreLabel> sentence : text) {
			for (CoreLabel word : sentence) {
				int freq = 0;
				String ner = word.word();
				String wClass = word
						.get(CoreAnnotations.AnswerAnnotation.class);
				if (wClass.equals("PERSON")) {
					freq = countTerm(ner, doc);
					if (freq > current_count) {
						current_count = freq;
						bio_person = ner;
					}
				}
			}
		}
		String pregen = "This is a biography about " + bio_person + ".";

		summary.addAll(text);

		// booleans to control how "harsh" the summarizer is
		boolean ner2_done = true;
		boolean no_quotes = false;
		boolean only_dates = false;
		int iterations = 0;

		// Loops until summary is under the cap
		while (summary.size() > cap) {
			iterations++;
			System.out.println("Iteration: " + iterations);
			ArrayList<List<CoreLabel>> temp = new ArrayList<List<CoreLabel>>();

			for (List<CoreLabel> sentence : summary) {
				int nerCount = 0;
				boolean has_date = false;
				boolean is_quote = false;

				for (CoreLabel word : sentence) {
					String ner = word.word();
					String wClass = word
							.get(CoreAnnotations.AnswerAnnotation.class);

					// Set booleans based on terms located in phrase
					if (!wClass.equals("O")) {
						nerCount++;
					}
					if (wClass.equals("DATE")) {
						has_date = true;
					}
					if (ner.equals("``")) {
						is_quote = true;
					}

				}
				// boolean checks - this decides if a phrase is included in the
				// summary
				if (ner2_done) { // ner 2 check
					if (nerCount >= 2) {
						if (no_quotes) { // quotes check
							if (!is_quote) {
								if (only_dates) { // dates check
									if (has_date) {
										temp.add(sentence);
									}
								} else {
									temp.add(sentence);
								}
							}
						} else {
							temp.add(sentence);
						}
					}
				}
			}

			summary = temp;

			// Sets the next level of summarizing based on the current level
			// ie. if ner2 is done but quotes haven't been removed and it is
			// still over the cap, it will set that boolean for the next loop.
			// Also breaks out if all the ways of summarizing are exhausted.
			if (ner2_done) {
				if (no_quotes) {
					if (only_dates) {
						break;
					} else {
						only_dates = true;
					}
				} else {
					no_quotes = true;
				}
			}
		}

		ArrayList<String> results = new ArrayList<String>();
		results = prettyFormatter(summary, pregen, cap);
		return results;
	}

	/**
	 * Writes the text summary to a file
	 * 
	 * @param text
	 * @param outfile
	 * @throws FileNotFoundException
	 * @throws UnsupportedEncodingException
	 */
	public static void writeResults(ArrayList<String> text, String outfile)
			throws FileNotFoundException, UnsupportedEncodingException {
		PrintWriter writer = new PrintWriter(outfile, "UTF-8");
		for (String line : text) {
			writer.println(line);
		}
		writer.close();
	}

	/**
	 * Counts the number of occurrences of a term in the document.
	 * 
	 * @param term
	 * @param document
	 * @return
	 */
	public static int countTerm(String term, String document) {
		int count = 0;
		String[] wordArray;
		if (document.toLowerCase().contains(term.toLowerCase())) {
			wordArray = document.split(" ");
			for (int i = 0; i < wordArray.length; i++) {
				if (wordArray[i].toLowerCase().equals(term.toLowerCase())) {
					count++;
				}
			}
		} else {
			return 0;
		}
		return count;

	}

	/**
	 * Re-formats the summary for writing to a file
	 * 
	 * @param summary
	 * @param pregen
	 * @return
	 */
	public static ArrayList<String> prettyFormatter(ArrayList<List<CoreLabel>> summary, String pregen, int cap){
		ArrayList<String> results = new ArrayList<String>();
		results.add(pregen);
		
		int count = 0;
		for (List<CoreLabel> sentence : summary){
			String line = "";
			count++;
			for (CoreLabel word : sentence){
				String w = word.word();
				line = w.equals(".") || w.equals(",") || w.equals("!") || w.equals("?") || w.equals("'") ? line + w : line + " " + w;
			}
			if (count <= cap){
				results.add(line);
			}
		}
		
		return results;
	}
}
