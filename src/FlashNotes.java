import java.io.IOException;
import java.io.PrintWriter;
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

		// System.out.println(args[0]+"-"+args[1]+"-"+args[2]);

		if (args.length > 2) {
			outfile = args[2];
		}

		// Set output file
		PrintWriter writer = new PrintWriter(outfile, "UTF-8");

		// Set classifier
		AbstractSequenceClassifier<CoreLabel> classifier = CRFClassifier
				.getClassifier(serializedClassifier);

		// How to classify an input file
		if (args.length > 1) {
			// Parse file to a string
			String fileContents = IOUtils.slurpFile(infile);

			// Apply the classifier
			List<List<CoreLabel>> out = classifier.classify(fileContents);

			summarize(out, length_of_summary, fileContents);
			
			
			// Print out to console - could replace with file writing
			for (List<CoreLabel> sentence : out) {
				int nerCount = 0;
				String s = "";
				for (CoreLabel word : sentence) {
					String line = "";
					s = sentence.toString();
					line += word.word();
					String wClass = word
							.get(CoreAnnotations.AnswerAnnotation.class);
					if (!wClass.equals("O")) {
						nerCount++;
						line += "/" + wClass + " ";
					} else {
						line += " ";
					}

//					System.out.print(line);
				}
//				System.out.println();

				// Write to summary file if nerCount is high enough.
				// TODO - tweak this to adjust based on count
				if (nerCount >= 2) {
					writer.println(s);
				}
			}
			writer.close();

		}
	}

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
	
	public static void summarize(List<List<CoreLabel>> text, int cap, String doc){

		ArrayList<List<CoreLabel>> summary = new ArrayList<List<CoreLabel>>();
				
		// Finds who the bio is about.
		// TODO - make it only count ner's of type PERSON
		String bio_person = "Harry Potter";
		int current_count = 0;
		for (List<CoreLabel> sentence : text) {
			for (CoreLabel word : sentence) {
				int freq = 0;
				String ner = word.word();
				String wClass = word
						.get(CoreAnnotations.AnswerAnnotation.class);
				if (!wClass.equals("O")) {
					freq = countTerm(ner, doc);
					if (freq > current_count){
						bio_person = ner;
					}
				} 
			}
		}
		String pregen = "This is a biography about " + bio_person + ".";

		
//		for(int i=0;i<text.size();i++){
//			String phrase = "";
//			for(int j=0;j<text.get(i).size();j++){
//				phrase += text.get(i).get(j).word() + " ";
//			}
//			summary.add(phrase);
//		}

		summary.addAll(text);
		
		// while loop - check summary size to wanted size
		boolean ner2_done = true;
		boolean no_quotes = false;
		boolean only_dates = false;		
		
		while(summary.size() > cap){
			ArrayList<List<CoreLabel>> temp = new ArrayList<List<CoreLabel>>();
			
			for (List<CoreLabel> sentence : summary) {
				int nerCount = 0;
				boolean has_date = false;
				boolean is_quote = false;
				
				for (CoreLabel word : sentence) {
					String ner = word.word();
					String wClass = word
							.get(CoreAnnotations.AnswerAnnotation.class);
					
					if (!wClass.equals("O")){
						nerCount++;
					}
					if (wClass.equals("DATE")){
						has_date = true;
					}
					if (ner.equals("``")){
						is_quote = true;
					}
					
				}
				// boolean checks
				if (ner2_done){ // ner 2 check
					if (nerCount >= 2){
						if (no_quotes){ // quotes check
							if (!is_quote){
								if (only_dates){ // dates check
									if (has_date){
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
			// Break out if all the ways of summarizing are exhausted

			if (ner2_done){
				if (no_quotes){
					if (only_dates){
						break;
					} else {
						only_dates = true;
					}
				} else {
					no_quotes = true;
				}
			}
		}
			
		
		for (List<CoreLabel> sentence : summary) {
			System.out.println(sentence);
		}
			// NER pairs - tough
		
		// format summarizer by date and to pretty print
		
	}
}
