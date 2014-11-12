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

		// Example arguments: 5 Presidents/AbrahamLincoln.txt summary.txt
		// Format: <number of lines> <input name> <output name>

		String serializedClassifier = "classifiers/english.muc.7class.distsim.crf.ser.gz";
		
		String outfile = "summary.txt";
		String infile = args[1];
		int length_of_summary = Integer.getInteger(args[0]);
		
//		System.out.println(args[0]+"-"+args[1]+"-"+args[2]);
		
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

					System.out.print(line);
				}
				System.out.println();

				// Write to summary file if nerCount is high enough.
				// TODO - tweak this to adjust based on count
				if (nerCount >= 2) {
					writer.println(s);
				}
			}
			writer.close();

		}
	}
}
