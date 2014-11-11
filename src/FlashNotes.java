import java.io.IOException;
import java.io.PrintWriter;
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

		String serializedClassifier = "classifiers/english.muc.7class.distsim.crf.ser.gz";

		if (args.length > 0) {
			serializedClassifier = args[0];
		}
		
		// Set output file - TODO improve
		PrintWriter writer = new PrintWriter("summary.txt", "UTF-8");

		// Set classifier
		AbstractSequenceClassifier<CoreLabel> classifier = CRFClassifier
				.getClassifier(serializedClassifier);

		// How to classify an input file
		if (args.length > 1) {
			// Parse file to a string
			String fileContents = IOUtils.slurpFile(args[1]);
			
			// Apply the classifier
			List<List<CoreLabel>> out = classifier.classify(fileContents);
			
			// Print out to console - could replace with file writing
			for (List<CoreLabel> sentence : out) {
				int nerCount = 0;
				String s = "";
				for (CoreLabel word : sentence) {
					String line = "";
					s = sentence.toString();
					line += word.word();
					String wClass = word.get(CoreAnnotations.AnswerAnnotation.class);
					if (!wClass.equals("O")){
						nerCount++;
						line += "/"+wClass+" ";
					} else {
						line += " ";
					}
//					line += wClass.equals("O") ? " " : "/" + wClass + " ";
//					System.out.print(word.word() + '/'
//							+ word.get(CoreAnnotations.AnswerAnnotation.class)
//							+ ' ');
					System.out.print(line);
				}
				System.out.println();
				
				// Write to summary file if nerCount is high enough.
				if (nerCount >= 2){
					writer.println(s);
				}
			}
			writer.close();
			
			
			
			
			
			
			
			
			
			
			
//			System.out.println("-*-*-");
//			out = classifier.classifyFile(args[1]);
//			for (List<CoreLabel> sentence : out) {
//				for (CoreLabel word : sentence) {
//					System.out.print(word.word() + '/'
//							+ word.get(CoreAnnotations.AnswerAnnotation.class)
//							+ ' ');
//				}
//				System.out.println();
//			}

		}
		// Run on a single string
		else {
			String[] example = {
					"Good afternoon Rajat Raina, how are you today?",
					"I go to school at Stanford University, which is located in California." };
			for (String str : example) {
				System.out.println(classifier.classifyToString(str));
			}
			System.out.println("---");

			for (String str : example) {
				// This one puts in spaces and newlines between tokens, so just
				// print not println.
				System.out.print(classifier.classifyToString(str, "slashTags",
						false));
			}
			System.out.println("---");

			for (String str : example) {
				System.out.println(classifier.classifyWithInlineXML(str));
			}
			System.out.println("---");

			for (String str : example) {
				System.out.println(classifier
						.classifyToString(str, "xml", true));
			}
			System.out.println("---");

			int i = 0;
			for (String str : example) {
				for (List<CoreLabel> lcl : classifier.classify(str)) {
					for (CoreLabel cl : lcl) {
						System.out.print(i++ + ": ");
						System.out.println(cl.toShorterString());
					}
				}
			}
		}

	}

}
