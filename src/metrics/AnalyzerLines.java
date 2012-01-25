package metrics;

import java.io.*;

import tools.*;

/**
 * @author Tuomas Tanner
 * 
 */
public class AnalyzerLines extends Analyzer {

	public AnalyzerLines(Logger logger) {
		super(logger);
	}

	public String analyzeFile(File file, Object additionalParam) {
		
		BufferedReader fileBuf = Tools.openTextFile(file);
		if (file == null) {
			System.out.println("ERROR: Could not open file: " + file.getPath());
			return null;
		}
		try {
			int codeLinecount = 0;
			int realLinecount = 0; // testing counting

			String row = null;
			boolean inCommentBlock = false;
			while ((row = fileBuf.readLine()) != null) {
				++realLinecount;
				logger.log(realLinecount + ": " + row, 3);
				if (inCommentBlock) {
					int commentEnd = findToken("*/", row, 0);
					if (commentEnd == -1) { // comment doesn't end - read
											// another line
						continue;
					} else { // comment ends
						inCommentBlock = false;
						row = row.substring(commentEnd + 2); // get
																// non-commented
																// portion of
																// line
					}
				} else { // check for comment block beginning
					int commentStart = findToken("/*", row, 0);
					if (commentStart != -1) {
						// check that comment doesn't end in same row
						int commentEnd = findToken("*/", row, commentStart + 2);
						if (commentEnd != -1) { // comment ends in same row
							// strip out the comment block and leave rest for
							// counting
							row = row.substring(0, commentStart)
									+ row.substring(commentEnd + 2);
						} else { // comment block does not end, set flag +
									// continue
							inCommentBlock = true;
							continue;
						}
					}
				}

				/* // comment checking here */
				int commentStart = row.indexOf("//");
				if (commentStart != -1) {
					row = row.substring(0, commentStart); // strip end comment
				}

				row = row.trim();
				if (row.length() < 3) {
					continue;
				}

				// count semicolons
				int semicount = 0;
				int pos = row.indexOf(";");
				while (pos > 0) {
					++semicount;
					pos = findToken(";", row, pos + 1);
				}

				if (semicount == 0) {
					++codeLinecount;
					logger.log("^ 1 LOC", 3);
				} else {
					codeLinecount += semicount;
					logger.log("^ " + semicount + " LOC", 3);
				}

			} // end file reading loop
			return file.getPath() + "\t" + realLinecount + "\t" + codeLinecount + "\n";
		} catch (IOException e) {
			logger.log("Exception when reading file:", e, 1);
			return null;
		}
	}

	/**
	 * Finds position of specified token if not inside a string literal
	 * 
	 * @param token
	 * @param row
	 * @param startFrom
	 * @return position of token or -1 if not found
	 */
	public static int findToken(String token, String row, int startFrom) {
		char[] tokenArr = token.toCharArray();
		char[] rowArr = row.toCharArray();
		int tokenPos = 0;
		boolean stringLiteral = false;
		for (int i = startFrom; i < rowArr.length; ++i) {
			if (stringLiteral) {
				if (rowArr[i] == '"') {
					stringLiteral = false;
				} else {
					continue;
				}
			} else if (rowArr[i] == '"') {
				stringLiteral = true;
				tokenPos = 0;
				continue;
			}

			if (rowArr[i] == tokenArr[tokenPos]) {
				++tokenPos;
				if (tokenPos == tokenArr.length) { // found complete token
					return i - tokenArr.length + 1;
				}
			} else { // wasn't it, reset tokenpos
				tokenPos = 0;
			}

		}// end for
		return -1;
	}
}
