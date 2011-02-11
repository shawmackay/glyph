/*
 * Created on 14-Jul-06
 *
 */
package org.jini.glyph;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jini.glyph.postprocessing.BasicPostProcessingItem;

/**
 * 
 * Holds a content template used for the body text of an email message. The file
 * content will contain variable place holders in the format of ${<i>varname</i>}.
 * Allowable characters for variables are
 * <ul>
 * <li>Uppercase and lowercase letters (A-Z & a-z)</li>
 * <li>Hyphen (-)</li>
 * <li>Digits (0-9)</li>
 * <li>. and _ </li>
 * </ul>
 * <br/> Nested replacements can be placed inside the option variables for
 * instance having an option of salutation = ${title} ${initial} ${surname},
 * will expand as well.<br/>
 * 
 * <pre>
 * ContentTemplate template = new ContentTemplate(&quot;template.txt&quot;);
 * HashMap options = new HashMap();
 * options.put(&quot;title&quot;, &quot;Mr&quot;);
 * options.put(&quot;initial&quot;, &quot;J&quot;);
 * options.put(&quot;surname&quot;, &quot;Bloggs&quot;);
 * options.put(&quot;salutation&quot;, &quot;${title} ${initial} ${surname}&quot;);
 * System.out.println(template.getContent(options));
 * </pre>
 * 
 * <br/><br/> When merged with a file containing the following
 * <code>Dear ${salutation}</code> will yield <code>Dear Mr J Bloggs</code>
 * 
 */
public class ContentTemplate {

	private static final Logger logger = Logger.getLogger("net.dev.java.jini.exportmgr");

	private URL filename;

	private StringBuffer content;

	//\$[\[\$!:\,=a-zA-Z0-9._\ ]*]*
	
	private Pattern varPattern = Pattern.compile("\\$\\[[?\\$\\[!:\\,=a-zA-Z0-9._\\ ]*]*");

	/**
	 * Creates a ContentTemplate and associates the template content to a
	 * filename
	 * 
	 * @param file
	 */
	public ContentTemplate(URL filename) throws ContentTemplateException {
		logger.finest("creating template from file: " + filename);
		this.filename = filename;
		try {
			loadFile();
		} catch (IOException e) {
			throw new ContentTemplateException(e);
		}
		logger.finest("template created: " + filename);
	}

	private void loadFile() throws IOException {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(filename.openStream()));
			content = new StringBuffer();
			String line = reader.readLine();
			while (line != null) {
				content.append(line + "\n");
				line = reader.readLine();
			}
		} catch (IOException ex) {
			logger.log(Level.SEVERE, "Unable to load template", ex);
		} finally {
			try {
				reader.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}

	public String getRawContent() {
		return content.toString();
	}

	/**
	 * Merges the template with the passed variable options.
	 * 
	 * @param options
	 *            the variables that will be merged with the template
	 * @return the merged content data
	 * @throws InvalidContentOptionsException
	 *             if a variable is needed by a template, but not available in
	 *             the options
	 */
	public String getContent(Map options) throws ContentTemplateException {

		StringBuffer merge = new StringBuffer(content.toString());
		Matcher m = varPattern.matcher(merge);

		handleContentMerge(options, merge, m);
		return merge.toString();
	}

	private void handleContentMerge(Map options, StringBuffer merge, Matcher m) throws ContentTemplateException {
		int startvalue = 0;
		while (m.find(startvalue)) {
			// Check that the group matches an item in the options
			String decl = m.group();
			//System.out.println("Decl: " + decl);
			String var = decl.substring(2, decl.length() - 1);
			StringBuffer additionalBuffer = new StringBuffer(var);
			Matcher subMatcher = varPattern.matcher(additionalBuffer);
			handleContentMerge(options, additionalBuffer, subMatcher);
			//System.out.println("Additional Buffer: " + additionalBuffer.toString());
			if (var.startsWith("loop:")) {

				doLoopProcessing(options, merge, m, startvalue, var);
			} else if (var.startsWith("if:")) {
				doIfProcessing(options, merge, m, startvalue, var);
			} else if (var.startsWith("upcase:")) {
				startvalue=doCaseProcessing(options, merge, m, startvalue, var, "up");
			} else if (var.startsWith("downcase:")) {
				startvalue = doCaseProcessing(options, merge, m, startvalue, var, "down");
			}else if (var.startsWith("substring:")) {
					System.out.println("Substring: " + var);
					startvalue = doSubstringProcessing(options, merge, m, startvalue, var);
			} else if (var.startsWith("var:")) {
				startvalue = doVarProcessing(options, merge, m, startvalue, var);	
			} else {
				startvalue = doSubstitution(options, merge, m, startvalue, var);
			}
		}
	}

	private int doVarProcessing(Map options, StringBuffer merge, Matcher m, int startvalue, String var) throws ContentTemplateException {
		// TODO Auto-generated method stub
		String substr =merge.substring(m.start(), m.end());
		substr = substr.substring(substr.indexOf(":") + 1, substr.length() - 1);
		int equalPos=substr.indexOf('=');
		
		String value = substr.substring(0,equalPos);
		String assignment = substr.substring(equalPos+1);
		System.out.println("Assigning : " + value + " to result of " + assignment);
		StringBuffer b = handleInternalContent(options, assignment);
		options.put(value,b.toString());	
		merge.replace(m.start(), m.end(), "");
		return startvalue;
	}

	private StringBuffer handleInternalContent(Map options, String assignment) throws ContentTemplateException {
		StringBuffer b = new StringBuffer(assignment);
		Matcher subMatcher = varPattern.matcher(b);
		handleContentMerge(options, b, subMatcher);
		System.out.println("Assignment result:" + b.toString());
		return b;
	}

	private int doSubstringProcessing(Map options, StringBuffer merge, Matcher m, int startvalue, String var) throws NumberFormatException, ContentTemplateException {
		// TODO Auto-generated method stub
		String substr =merge.substring(m.start(), m.end());
		System.out.println("substr:" + substr);
		substr = substr.substring(substr.indexOf(":") + 1, substr.length() - 1);
		String[] parts = substr.split(",");
		System.out.println("substr:" + substr);
		String value = (String) options.get(parts[0]);
		System.out.println("StartPos: " + parts[1]);
		System.out.println("EndPos: " + parts[2]);
		int beginpos = Integer.parseInt(handleInternalContent(options, parts[1]).toString());
		int endpos = value.length();
		if(parts.length>2)
			endpos =  Integer.parseInt(handleInternalContent(options, parts[2]).toString());
		merge.replace(m.start(), m.end(), value.substring(beginpos,endpos));
		return startvalue;
	}

	private int doCaseProcessing(Map options, StringBuffer merge, Matcher m, int startvalue, String var, String casetype) throws ContentTemplateException{
	
		String casevar = merge.substring(m.start(), m.end());
		System.out.println("CaseVar: " + casevar);
		casevar = casevar.substring(casevar.indexOf(":") + 1, casevar.length() - 1);
		String change;
		Object value = options.get(casevar);
		if(casevar.startsWith("$[")){
			StringBuffer b = new StringBuffer(casevar);
			Matcher subMatcher = varPattern.matcher(b);
			handleContentMerge(options, b, subMatcher);
			System.out.println("Additional Buffer:" + b.toString());
			value = b.toString();
		}
		if (value != null) {
			String casedValue = (String) value;

			if (casetype.equals("up")) {
				merge.replace(m.start(), m.end(), casedValue.toUpperCase());
			} else if (casetype.equals("down"))
				merge.replace(m.start(), m.end(), casedValue.toLowerCase());
			else
				merge.replace(m.start(), m.end(), "");
		} else{
			merge.replace(m.start(), m.end(), "");
		
		}
		return startvalue;
	}

	private int doIfProcessing(Map options, StringBuffer merge, Matcher m, int startvalue, String var) {
		// TODO Auto-generated method stub
		Matcher ifMatch = Pattern.compile("\\$\\[endif\\]").matcher(merge);
		ifMatch.find(startvalue);
		System.out.println("Matching " + ifMatch.group());
		String ifBlock = merge.substring(m.start(), ifMatch.end());
		String condition = merge.substring(m.start(), m.end());
		condition = condition.substring(condition.indexOf(":") + 1, condition.length() - 1);
		String onConditionMerge = merge.substring(m.end(), ifMatch.start());

		System.out.println("Condition is: " + condition);
		System.out.println("On Condition merge: " + onConditionMerge);
		if (processCondition(condition, options)) {
			merge.replace(m.start(), ifMatch.end(), onConditionMerge);
		} else {
			System.out.println("Condition returned false");
			merge.replace(m.start(), ifMatch.end(), "");

		}
		return startvalue;
	}

	private boolean processCondition(String condition, Map options) {
		// TODO Auto-generated method stub
		boolean negateResult = false;
		String operators = "[\\+\\-\\*\\/]";
		Matcher opMatch = Pattern.compile(operators).matcher(condition);
		if (condition.startsWith("!")) {
			negateResult = true;
			System.out.println("Negating condition result");
			condition = condition.substring(1);
		}
		if (opMatch.find())
			System.out.println("Operator:" + condition.substring(opMatch.start(), opMatch.end()));

		else {
			System.out.println("Checking existence");
			if (negateResult)
				return !(options.containsKey(condition));
			else
				return options.containsKey(condition);
		}
		return false;
	}

	private int doSubstitution(Map options, StringBuffer merge, Matcher m, int startvalue, String var) {
		if (!options.containsKey(var)) {
			System.out.println("Variable " + var + " is in template but not in value options");
			// throw new ContentTemplateException("Variable " + var + "
			// is in template but not in value options");
			merge.replace(m.start(), m.end(), "");
			startvalue = m.start();
		} else {
			String value = (String) options.get(var);
			merge.replace(m.start(), m.end(), value);
			startvalue = m.start();
		}
		return startvalue;
	}

	private void doLoopProcessing(Map options, StringBuffer merge, Matcher m, int startvalue, String var) throws ContentTemplateException {
		Matcher loopMatch = Pattern.compile("\\$\\[endloop\\]").matcher(merge);
		loopMatch.find(startvalue);
		String loopArea = merge.substring(m.end(), loopMatch.start());
		String[] loopDecl = var.split(" ");
		merge.replace(m.start(), loopMatch.end(), handleLoop(loopDecl[1], loopDecl[3], loopArea, options, m.start(), loopMatch.end()));
	}

	private String handleLoop(String set, String var, String body, Map options, int start, int end) throws ContentTemplateException {
		// TODO Auto-generated method stub
		// System.out.println("Loop Set: [" + set + "]");
		// System.out.println("Loop var: [" + var + "]");
		// System.out.println("Loop body: [" + body + "]");
		// System.out.println("Loop opt: [" + options + "]");
		// System.out.println("Loop start: [" + start + "]");
		// System.out.println("Loop end: [" + end + "]");
		Collection c = (Collection) options.get(set);
		// System.out.println("Item size is: " + c.size());
		StringBuffer expandBuffer = new StringBuffer();
		for (Iterator iter = c.iterator(); iter.hasNext();) {
			Map loopOptions = new HashMap();
			loopOptions.putAll(options);

			loopOptions.put(var, iter.next());
			BasicPostProcessingItem bppi = (BasicPostProcessingItem) loopOptions.get(var);
			if (bppi.getOptions() != null)
				loopOptions.putAll(bppi.getOptions());
			// System.out.println("BPPI Opts: " +bppi.getOptions());
			expandBuffer.append(body);
			Matcher m = varPattern.matcher(expandBuffer);
			int startvalue = 0;
			while (m.find(startvalue)) {
				// Check that the group matches an item in the options
				String decl = m.group();
				String loopvar = decl.substring(2, decl.length() - 1);
				// System.out.println("Var: " + loopvar);

				if (!loopOptions.containsKey(loopvar)) {
					// System.out.println("Variable " + loopvar + " is in
					// template but not in value options");
					// System.out.println(expandBuffer.toString());
					throw new ContentTemplateException("Variable " + var + " is in template but not in value options");
				} else {
					Object o = loopOptions.get(loopvar);
					String value = null;
					if (o instanceof BasicPostProcessingItem)
						value = ((BasicPostProcessingItem) o).getContent();
					else
						value = (String) o;
					// System.out.println("Replacing " +m.group() + " with " +
					// value);
					expandBuffer.replace(m.start(), m.end(), value);
					startvalue = m.start();
				}
			}
		}
		options.remove(var);

		return expandBuffer.toString();

	}

}
