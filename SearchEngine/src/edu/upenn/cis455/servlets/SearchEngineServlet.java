package edu.upenn.cis455.servlets;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.simple.JSONArray;

import edu.upenn.cis455.crawler.VideoPageParser;
import edu.upenn.cis455.mapreduce.master.WorkerInfo;
import edu.upenn.cis455.searchalgo.GetResult;
import edu.upenn.cis455.searchalgo.ResultSet;
import edu.upenn.cis455.spellchecker.AddWordsToDic;
import edu.upenn.cis455.spellchecker.DBSingleton;
import edu.upenn.cis455.spellchecker.DatabaseWrapper;
import edu.upenn.cis455.spellchecker.SuggestionWords;
import edu.upenn.cis455.util.StringUtil;
import edu.upenn.cis455.util.URLCodec;

public class SearchEngineServlet extends CrawlerMasterServlet {

	public static final String TAG = SearchEngineServlet.class.getSimpleName();
	private static Logger logger = Logger.getLogger(TAG);

	private static final long serialVersionUID = 1L;

	// private static final String SPELLCHECKER_DB_PATH = "checker_db";
	private static final String WORDLIST_FILE = "wordlist.txt";
	private static final int MAX_MEDIA_TITLE_LEN = 30;

	@Override
	public void init() throws ServletException {
		super.init();
		String homeDir = System.getProperty("user.home");
		logger.info("Home directory:" + homeDir);
		String checkerDbPath = getInitParameter("checkerdbdir");
		if (checkerDbPath == null) {
			logger.error("Cannot find checkerdbpath");
		} else {
			if (checkerDbPath.startsWith("~")) {
				checkerDbPath = checkerDbPath.replaceFirst("~", homeDir);
			}
			logger.info("Checker DB path: " + checkerDbPath);
			File checkerDbDir = new File(checkerDbPath);
			if (!checkerDbDir.exists()) {
				checkerDbDir.mkdirs();
			}
			DBSingleton.setDbPath(checkerDbPath);
			File wordlistFile = new File(homeDir, WORDLIST_FILE);
			if (wordlistFile.exists()) {
				logger.info("Fount wordlist.txt in "
						+ wordlistFile.getAbsolutePath()
						+ ", adding to checker db");
				// Add checker words
				DatabaseWrapper wrapper = DBSingleton.getInstance()
						.getWrapper();
				try {
					AddWordsToDic.addWordToDb(wrapper, wordlistFile);
					DBSingleton.getInstance().sync();
					logger.info("Adding spell check words finished");
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
				// Delete after added
				wordlistFile.delete();
			}
		}
	}

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		String path = request.getPathInfo();
		if ("/spellcheck".equals(path)) {
			handleSpellcheck(request, response);
		} else if ("/result".equals(path)) {
			handleResult(request, response);
		} else {
			super.doGet(request, response);
		}
	}

	@Override
	public void destroy() {
		super.destroy();
		DBSingleton.getInstance().closeBDBstore();
		logger.debug("Checker DB closed");
	}

	private void handleSpellcheck(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		// System.out.println(request.getRequestURI());
		PrintWriter out = response.getWriter();
		response.setContentType("text/html");
		response.setHeader("Cache-control", "no-cache, no-store");
		response.setHeader("Pragma", "no-cache");
		response.setHeader("Expires", "-1");

		JSONArray arrayObj = new JSONArray();

		String query = request.getParameter("term");
		query = URLCodec.decode(query).toLowerCase();

		String[] queryArr = query.split(" ");
		String targetStr = "";
		permuteWordCombination(arrayObj, 0, queryArr, targetStr);
		out.println(arrayObj.toString());
		out.close();
	}

	private void handleResult(HttpServletRequest request,
			HttpServletResponse response) {
		try {
			response.setContentType("text/html");
			PrintWriter out = response.getWriter();
			out.write("\n");
			out.write("\n");
			out.write("\n");
			out.write("<html lang=\"en\">  \n");
			out.write("  <head>  \n");
			out.write("    <title>CIS455/555 Search Engine--Team 02</title>\n");
			out.write("    <meta charset=\"utf-8\">  \n");
			out.write("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">  \n");
			out.write("    \n");
			out.write("    <!-- Bootstrap --> \n");
			out.write("    <link href=\"public/dist/css/bootstrap.css\" rel=\"stylesheet\">\n");
			out.write("\t<link href=\"public/dist/css/bootstrap.min.css\" rel=\"stylesheet\" media=\"screen\"> \n");
			out.write("\t\n");
			out.write("    <!-- Custom styles for this template -->\n");
			out.write("    <link href=\"custom/result.css\" rel=\"stylesheet\"> \n");
			out.write("    \n");
			out.write("    <!--<script src=\"public/assets/js/jquery.js\"></script>-->\n");
			out.write("    <script src=\"public/assets/js/script.js\"></script>\n");
			out.write("    <script src=\"public/dist/js/bootstrap.min.js\"></script>\n");
			out.write("    \n");
			out.write("    <script type=\"text/javascript\"> \n");
			out.write("   function change(){\n");
			out.write("   \t \tvar s= document.getElementById(\"specialSearch\");\n");
			out.write("   \t \tdocument.getElementById(\"searchLabelID\").innerHTML = s.options[s.selectedIndex].text;\n");
			out.write("   \t \tdocument.getElementById(\"searchLabelID\").style.color=\"blue\";\n");
			out.write("   }\n");
			out.write("   \n");
			out.write("    </script>\n");
			out.write("    \n");
			out.write("    <script src=\"http://code.jquery.com/jquery-1.7.js\"\n");
			out.write("    type=\"text/javascript\"></script>\n");
			out.write("\t<script\n");
			out.write("    src=\"http://ajax.googleapis.com/ajax/libs/jqueryui/1.8/jquery-ui.min.js\"\n");
			out.write("    type=\"text/javascript\"></script>\n");
			out.write("\t<link\n");
			out.write("    href=\"http://ajax.googleapis.com/ajax/libs/jqueryui/1.8/themes/base/jquery-ui.css\"\n");
			out.write("    rel=\"stylesheet\" type=\"text/css\" />\n");
			out.write("\n");
			out.write(" \n");
			out.write("<script type=\"text/javascript\">\n");
			out.write("$(document).ready(function() {\n");
			out.write("    $(\"input#STextID\").autocomplete({\n");
			out.write("        width: 300,\n");
			out.write("        max: 10,\n");
			out.write("        delay: 30,\n");
			out.write("        minLength: 1,\n");
			out.write("        autoFocus: true,\n");
			out.write("        cacheLength: 1,\n");
			out.write("        scroll: true,\n");
			out.write("        highlight: false,\n");
			out.write("        source: function(request, response) {\n");
			out.write("            $.ajax({\n");
			out.write("                url: \"spellcheck\",\n");
			out.write("                dataType: \"json\",\n");
			out.write("                data: request,\n");
			out.write("                success: function( data, textStatus, jqXHR) {\n");
			out.write("                    console.log( data);\n");
			out.write("                    var items = data;\n");
			out.write("                    response(items);\n");
			out.write("                },\n");
			out.write("                error: function(jqXHR, textStatus, errorThrown){\n");
			out.write("                     console.log( textStatus);\n");
			out.write("                }\n");
			out.write("            });\n");
			out.write("        }\n");
			out.write(" \n");
			out.write("    });\n");
			out.write("});\n");
			out.write("    \n");
			out.write("\t</script>\n");
			out.write("    <!-- HTML5 shim and Respond.js IE8 support of HTML5 elements and media queries -->  \n");
			out.write("    <!--[if lt IE 9]>  \n");
			out.write("      <script src=\"public/assets/js/html5shiv.js\"></script>  \n");
			out.write("      <script src=\"public/assets/js/respond.min.js\"></script>  \n");
			out.write("    <![endif]--> \n");
			out.write("    \n");
			out.write("  </head>  \n");
			out.write("  \n");
			out.write("  <body> \n");
			out.write("  \n");
			out.write(" <div class=\"navbar navbar-default navbar-fixed-top\">\n");
			out.write("      <div class=\"container\">\n");
			out.write("        <div class=\"navbar-collapse collapse\">\n");
			out.write("          <a class=\"navbar-brand\" href=\"index.html\"><font size=\"18px\" color=\"black\"><B>GOgoGO </font></B></a>\n");
			out.write("          <form class=\"navbar-form navbar-right\"  action=\"result\">\n");
			out.write("          \t\n");
			out.write("          \t<div class=\"form-group\">\n");
			out.write("          \t\t<label class=\"label label-warning\" height=\"200px\" type=\"text\" id=\"searchLabelID\" name=\"searchLabel\"> <font color=\"blue\">DOCUMENT</font></label>\n");
			out.write("          \t</div>\n");
			out.write("            <div class=\"form-group\">\n");
			out.write("              <input type=\"text\" ID=\"STextID\" width=\"600px\" name=\"searchText\" />\n");
			out.write("            </div>\n");
			out.write("            \n");
			out.write("            <div class=\"form-group\">\n");
			out.write("            \t<select id=\"specialSearch\" onchange=\"change();\" name=\"specialSearch\">\n");
			out.write("            \t\t\t<option value=\"0\" selected=\"selected\">DOCUMENT</option>\n");
			out.write("            \t\t\t<option value=\"1\">IMAGE</option>\n");
			out.write("            \t\t\t<option value=\"2\">VIDEO</option>\n");
			out.write("          \t  \t\t\t</select>\n");
			out.write("            </div>\n");
			out.write("            <button type=\"submit\" class=\"btn btn-success\" ID=\"SButtonID\" >GO</button>\n");
			out.write("          </form>\n");
			out.write("          \n");
			out.write("        </div><!--/.navbar-collapse -->\n");
			out.write("      </div>\n");
			out.write("    </div>\n");
			out.write("  \t<P></P> <P></P>\n");
			out.write("  ");

			String searchText = request.getParameter("searchText");
			String originalTextString=searchText;
			// no input for searching, return no results
			if (searchText == null) {
				searchText = "";
				originalTextString="";
			} else {
				searchText = URLCodec
						.decode(request.getParameter("searchText"))
						.toLowerCase();
			}

			DatabaseWrapper wrapper = DBSingleton.getInstance().getWrapper();
			SuggestionWords suggest = new SuggestionWords();
			String[] words = searchText.split(" ");

			StringBuffer sb = new StringBuffer();
			int kk = 0;
			boolean notFullMatch = false;
			for (kk = 0; kk < words.length; kk++) {
				wrapper.increaseHit(words[kk]);
				if (suggest.getWordsResult(words[kk]) == null
						|| suggest.getWordsResult(words[kk]).size() == 0) {
					sb.append(words[kk]+" ");
				} else {
					if (!words[kk].equals(suggest.getWordsResult(words[kk])
							.get(0))) {
						notFullMatch = true;
						sb.append(suggest.getWordsResult(words[kk]).get(0)
								+ " ");
					} else {
						sb.append(words[kk]+" ");
					}
				}
			}

			int specialSearch;
			if (request.getParameter("specialSearch") == null)
				specialSearch = 0;
			else
				specialSearch = Integer.parseInt(request
						.getParameter("specialSearch"));

			List<WorkerInfo> workers = getActiveWorkers();
			// Aggregate document counts
			int docCount = 0;
			for (WorkerInfo worker : workers) {
				String countStr = null;
				switch (specialSearch) {
				case 0:
					countStr = worker
							.getParameter(CrawlerNodeServlet.PARAM_DOCUMENT_COUNT);
					break;
				case 1:
					countStr = worker
							.getParameter(CrawlerNodeServlet.PARAM_IMAGE_COUNT);
					break;
				case 2:
					countStr = worker
							.getParameter(CrawlerNodeServlet.PARAM_VIDEO_COUNT);
					break;
				}
				int count = StringUtil.parseInt(countStr, 0);
				docCount += count;
			}

			String cityName;

			if (request.getParameter("cityName") == null) {
				cityName = "philadelphia";
			} else {
				cityName = URLCodec.decode(request.getParameter("cityName"));
				cityName = cityName.toLowerCase();
			}

			int locationPrio;
			if (request.getParameter("LocPrioValue") == null)
				locationPrio = 0;
			else
				locationPrio = Integer.parseInt(request
						.getParameter("LocPrioValue"));

			GetResult ge = new GetResult(searchText, cityName, workers,
					specialSearch, docCount, locationPrio);
			
			//add searching time here
			Long beginTime= System.currentTimeMillis();
			ArrayList<ResultSet> results = ge.getResults();
			Long endTime=System.currentTimeMillis();
			ArrayList<String> StemmerWords = ge.getAllStemmerWords();

			int nextPage;
			if (request.getParameter("nextPage") == null
					|| request.getParameter("nextPage") == "")
				nextPage = 1;
			else
				nextPage = Integer.parseInt(request.getParameter("nextPage"));

			int currentPage = nextPage;
			int entryNum = 0;
			int pagination_length = 5;
			int n_doc_per_page = 15;
			int resLength;
			if(results==null){
				resLength=0;
			}
			else{
				resLength= results.size();
			}
			int n_pages;

			if (resLength == 0) {
				n_pages = 1;
			} else if (resLength % n_doc_per_page == 0) {
				n_pages = resLength / n_doc_per_page;
			} else {
				n_pages = (resLength / n_doc_per_page) + 1;
			}

			out.write("\n");
			out.write("  \n");
			out.write("  <div class=\"container\" id=\"resultID\">\n");
			out.write("  ");

			// Search suggestions
			if (notFullMatch && nextPage == 1) {
				out.write("\n");
				out.write("  <P><a href=\"result?nextPage=1&searchText=");
				out.print(sb.toString().trim());
				out.write("&specialSearch=");
				out.print(specialSearch);
				out.write("&cityName=");
				out.print(cityName);
				out.write("\"><Font size=\"4\" color=\"red\" ><i>Did you mean ");
				out.print(sb.toString());
				out.write("?</i></Font></a></P>\n");
				out.write("  ");
			}

			// added for no available results
			if (resLength == 0) {
				out.write("<P><font color=\"black\"><B>Searching time approximately: ");
				double interval=(endTime-beginTime)/1000.0;
				DecimalFormat df = new DecimalFormat("#.000");		
				out.print(df.format(interval));
				out.write(" seconds.</B></font></P>\n");
				
				out.write("\n");
				out.write("   \t\t\t<div class=\"row\"  align=\"middle\">\n");
				out.write("        \t\t<div class=\"col-6 col-sm-6 col-lg-11\" >\n");
				out.write("            \t\t<div class=\"context\">\n");
				out.write("              \t\t\t<P><font size=\"4\" color=\"black\">");
				if (specialSearch == 0) {
					out.print("Sorry, we are unable to achieve useful documents related with "
							+ searchText);
				} else {
					out.print("Sorry, we are unable to achieve useful media information related with "
							+ searchText);
				}
				out.write("</font></P>\n");
				out.write("            \t\t</div>\n");
				out.write("            \t");
				out.write("\n");
				out.write("          </div>\n");
				out.write("        </div>\n");
			} else {
				
				out.write("<P align=\"middle\"><font size=\"5\" color=\"black\"> Results for: ");
				out.print(originalTextString);
				out.write("</font></P><P></P>\n");
				out.write("<P><font color=\"black\"><B>Searching time approximately: ");
				double interval=(endTime-beginTime)/1000.0;
				DecimalFormat df = new DecimalFormat("#.000");		
				out.print(df.format(interval));
				out.write(" seconds.</B></font></P>\n");
				
				if (specialSearch == 0) {
					for (entryNum = ((currentPage - 1) * n_doc_per_page); entryNum < currentPage
							* n_doc_per_page; entryNum++) {
						if (entryNum >= resLength)
							break;

						out.write("\n");
						out.write("   \t\t\t<div class=\"row\"  align=\"middle\">\n");
						out.write("        \t\t<div class=\"col-6 col-sm-6 col-lg-11\" >\n");
						out.write("          \t\t\t<div class=\"panel panel-info\">\n");
						out.write("            \t\t\t<div class=\"panel-heading\" >\n");
						//out.write("              \t\t\t\t<h3 class=\"panel-title\"> <font color=\"black\" size=\"3\"><B>");
						out.write("              \t\t\t\t<h3 class=\"panel-title\"><a href=\"");
						out.print(results.get(entryNum).url);
						out.write("\" target=\"_blank\"><font color=\"blue\" size=\"3\"><B>");
						
						if(results.get(entryNum).title.equals("")){
							out.write("Title information not available");
						}
						else{
							out.print(results.get(entryNum).title);
						}
						out.write("</B></font></a> </h3>\n");
						out.write("              \t\t\t\t<label >");
						out.write("<font size=\"1.5\" color=\"black\">");
						out.print(results.get(entryNum).url);
						out.write("</font></label>\n");
						
						out.write("              \t\t\t\t<label >");
						out.write("<font size=\"1.5\" color=\"black\">");
						out.write("location: ");
						out.print(results.get(entryNum).location);
						out.write("</font></label>\n");
						
						out.write("            \t\t\t</div>\n");
						out.write("            \t\t");
						if (!results.get(entryNum).summary.equals("")) {
							out.write("\n");
							out.write("            \t\t<div class=\"context\">\n");
							out.write("              \t\t\t<p><font size=\"3\" color=\"black\">");
							out.print(results.get(entryNum).summary);
							out.write("</font></p>\n");
							out.write("            \t\t</div>\n");
							out.write("            \t");
						}
						out.write("\n");
						out.write("          </div>\n");
						out.write("        </div>\n");
						out.write("      </div>      \n");
						out.write("    ");
					}
				} else {
					int rowNum = 0;
					if (resLength >= (currentPage * n_doc_per_page)) {

						for (rowNum = ((currentPage - 1) * n_doc_per_page); rowNum < currentPage
								* n_doc_per_page; rowNum += 3) {

							out.write("\n");
							out.write("   \t\t\t  <div class=\"row\">\n");
							out.write("   \t\t\t\t");
							for (int i = 0; i < 3; i++) {
								if (rowNum + i <= resLength - 1) {

									out.write("\n");
									out.write("        \t\t\t  <div class=\"col-6 col-sm-6 col-lg-4\">\n");
									out.write("          \t\t\t\t<div class=\"panel panel-info\">\n");
									out.write("            \t\t\t  <div class=\"panel-heading\">\n");
									out.write("              \t\t        <h3 class=\"panel-title\"> <a href=\"");

									String title = results.get(rowNum + i).title;
									if (title != null && title.length() > 0) {
										String[] titleArr = title.split(" ");
										if (titleArr.length > MAX_MEDIA_TITLE_LEN) {
											StringBuffer strbuf = new StringBuffer();
											for (int len = 0; len < MAX_MEDIA_TITLE_LEN; len++) {
												strbuf.append(titleArr[len]);
												strbuf.append(" ");
											}
											strbuf.append("...");
											title = strbuf.toString();

										}
									}
									
									if (specialSearch == 1) {
										out.print(results.get(rowNum + i).pageUrl);
										out.write("\" target=\"_blank\"><font color=\"blue\" size=\"3\">");
										if(title.equals("")){
											out.write("Title information not available");
										}
										else{
											out.print(title);
										}
										
										out.write(" </font></a> </h3>\n");
										
										out.write("            \t\t\t    <label >");
										//out.print(results.get(rowNum + i).pageUrl);
										out.write("<font size=\"1.5\" color=\"black\">");
										out.print(results.get(rowNum + i).pageUrl);
										out.write("</font></label>\n");
										out.write("            \t\t\t  </div>\n");
										out.write("                          <div align=\"middle\" class=\"context\">\n");
										out.write("              \t\t\t    <p><object width=\"350\" height=\"180\" data=\"");
										out.print(results.get(rowNum + i).url);
										out.write("\"> </object></p>\n");
										out.write("            \t\t      </div>\n");
										out.write("          \t\t\t\t</div>\n");
										out.write("        \t\t\t  </div>\n");
										out.write("        \n");
										
									} else if (specialSearch == 2) {
										String url = results.get(rowNum + i).url;
										out.print(url);
										out.write("\" target=\"_blank\"> <font color=\"blue\" size=\"3\">");
										if(title.equals("")){
											out.write("Title information not available");
										}
										else{
											out.print(title);
										}
										
										out.write(" </font></a> </h3>\n");
										
										out.write("            \t\t\t    <label>");
										out.write("<font size=\"1.5\" color=\"black\">");
										out.print(url);
										out.write("</font></label>\n");
										out.write("            \t\t\t  </div>\n");
										out.write("                          <div align=\"middle\" class=\"context\">\n");
										out.write("              \t\t\t    <p><object width=\"350\" height=\"262\" data=\"");
										out.print(getThumbnailUrl(url));
										out.write("\"> </object></p>\n");
										out.write("            \t\t      </div>\n");
										out.write("          \t\t\t\t</div>\n");
										out.write("        \t\t\t  </div>\n");
										out.write("        \n");
									}
								}
							}

							out.write("\n");
							out.write("   \t\t\t\t</div>\n");
							out.write("     \n");
							out.write("   \t\t\t ");
						}
					} else {
						for (rowNum = ((currentPage - 1) * n_doc_per_page); rowNum < resLength; rowNum++) {

							out.write(" \n");
							out.write("      \t\t\t<div align=\"middle\" class=\"col-6 col-sm-6 col-lg-8\">\n");
							out.write("          \t\t  <div class=\"panel panel-info\">\n");
							out.write("            \t\t<div class=\"panel-heading\">\n");
							out.write("             \t\t  <h3 class=\"panel-title\"> <a href=\"");

							String title = results.get(rowNum).title;
							if (title != null && title.length() > 0) {
								String[] titleArr = title.split(" ");
								if (titleArr.length > MAX_MEDIA_TITLE_LEN) {
									StringBuffer strbuf = new StringBuffer();
									for (int len = 0; len < MAX_MEDIA_TITLE_LEN; len++) {
										strbuf.append(titleArr[len]);
										strbuf.append(" ");
									}
									strbuf.append("...");
									title = strbuf.toString();

								}
							}
						
							//for image
							if (specialSearch == 1) {
								out.print(results.get(rowNum).pageUrl);
								out.write("\" target=\"_blank\"> <font color=\"blue\" size=\"3\"> ");
								//out.print(title);
								if(title.equals("")){
									out.write("Title information not available");
								}
								else{
									out.print(title);
								}
								
								out.write(" </font></a> </h3>\n");
								out.write("            \t\t\t    <label >");
								out.write("<font size=\"1.5\" color=\"black\">");
								out.print(results.get(rowNum).pageUrl);
								out.write("</font></label>\n");
								out.write("            \t    </div>\n");
								out.write("                    <div class=\"context\">\n");
								out.write("              \t\t  <p><object width=\"350\" height=\"180\" data=\"");
								out.print(results.get(rowNum).url);
								out.write("\"> </object></p>\n");
								out.write("            \t\t</div>\n");
								out.write("        \t\t</div>\n");
								out.write("     \t\t  </div>\n");
								out.write("     \n");
								out.write("     \t");
							} 
							//for video
							else if (specialSearch == 2) {
								String url = results.get(rowNum).url;
								out.print(url);
								out.write("\" target=\"_blank\"><font color=\"blue\" size=\"3\"> ");
								//out.print(title);
								if(title.equals("")){
									out.write("Title information not available");
								}
								else{
									out.print(title);
								}
								
								out.write(" </font></a> </h3>\n");
								
								out.write("            \t\t\t    <label >");
								//out.print(url);
								out.write("<font size=\"1.5\" color=\"black\">");
								out.print(url);
								out.write("</font></label>\n");
								out.write("            \t    </div>\n");
								out.write("                    <div class=\"context\">\n");
								out.write("              \t\t  <p><object width=\"480\" height=\"360\" data=\"");
								out.print(getThumbnailUrl(url));
								out.write("\"> </object></p>\n");
								out.write("            \t\t</div>\n");
								out.write("        \t\t</div>\n");
								out.write("     \t\t  </div>\n");
								out.write("     \n");
								out.write("     \t");
							}

						}
					}
					// }

					
				}
				
				out.write("\n");
				out.write("   \n");
				out.write("  \t<script type=\"text/javascript\" src=\"custom/hilitor.js\"></script> \n");
				out.write("  \t<script type=\"text/javascript\"> \n");
				out.write("  \t\tvar myHilitor; \n");
				out.write("  \t\tmyHilitor= new Hilitor(\"resultID\"); \n");
				 out.write("  \t\tmyHilitor.setMatchType(\"left\"); \n");
				out.write("  \t\t");

				String HighlightingWords=searchText;
				for (int i = 0; i < StemmerWords.size(); i++) {
					
					 HighlightingWords = HighlightingWords + " " +
					 StemmerWords.get(i);
					 }

					out.write("\n");
					out.write("  \t\tvar keyword='");
					out.print(HighlightingWords);
					out.write("';\n");
					out.write("  \t\tmyHilitor.apply(keyword);\n");
				out.write("  \t\t\n");
				out.write("  \t</script>\n");
				out.write("  \t\n");
				out.write("     ");
			}
			
			int prevPage;
			if (currentPage > 1)
				prevPage = currentPage - 1;
			else
				prevPage = currentPage;

			if (currentPage == n_pages)
				nextPage = currentPage;
			else
				nextPage = currentPage + 1;

			int pageSection;
			if (currentPage % pagination_length == 0)
				pageSection = currentPage / pagination_length;
			else
				pageSection = (currentPage / pagination_length) + 1;

			out.write("\n");
			out.write("     \n");
			out.write("     <div class=\"container\" align=\"middle\">\n");
			out.write("      <ul class=\"pager\">\n");
			out.write(" \t\t\t<li><a href=\"result?nextPage=1&searchText=");
			out.print(searchText);
			out.write("&specialSearch=");
			out.print(specialSearch);
			out.write("&cityName=");
			out.print(cityName);
			out.write("\"> <font color=\"purple\">First </font></a></li>\n");
			out.write("         \t<li><a href=\"result?nextPage=");
			out.print(prevPage);
			out.write("&searchText=");
			out.print(searchText);
			out.write("&specialSearch=");
			out.print(specialSearch);
			out.write("&cityName=");
			out.print(cityName);
			out.write("\"> <font color=\"purple\">Prev </font></a></li>\n");
			out.write("         \t\n");
			out.write("         \t");

			for (int i = ((pageSection - 1) * pagination_length + 1); i <= (pageSection * pagination_length); i++) {
				if (i > n_pages)
					break;
				if (i == currentPage) {

					out.write("\n");
					out.write("         \t<li><a href=\"result?nextPage=");
					out.print(i);
					out.write("&searchText=");
					out.print(searchText);
					out.write("&specialSearch=");
					out.print(specialSearch);
					out.write("&cityName=");
					out.print(cityName);
					out.write("\"> <font color=\"orange\">");
					out.print(i);
					out.write("</font> </a></li>\n");
					out.write("         \t");
				} else {

					out.write("\n");
					out.write("         \t\t<li><a href=\"result?nextPage=");
					out.print(i);
					out.write("&searchText=");
					out.print(searchText);
					out.write("&specialSearch=");
					out.print(specialSearch);
					out.write("&cityName=");
					out.print(cityName);
					out.write("\"> <font color=\"purple\">");
					out.print(i);
					out.write("</font> </a></li>\n");
					out.write("         \t");

				}
			}

			out.write("\n");
			out.write("         \t<li><a href=\"result?nextPage=");
			out.print(nextPage);
			out.write("&searchText=");
			out.print(searchText);
			out.write("&specialSearch=");
			out.print(specialSearch);
			out.write("&cityName=");
			out.print(cityName);
			out.write("\"> <font color=\"purple\">Next </font> </a></li>\n");
			out.write("         \t<li><a href=\"result?nextPage=");
			out.print(n_pages);
			out.write("&searchText=");
			out.print(searchText);
			out.write("&specialSearch=");
			out.print(specialSearch);
			out.write("&cityName=");
			out.print(cityName);
			out.write("\"> <font color=\"purple\">Last </font></a></li>\n");
			out.write("         \t</ul>     \n");
			out.write("     </div>\n");
			out.write("     \n");
			out.write("     <div class=\"container\" align=\"left\">\n");
			out.write("<div class=\"navbar-collapse collapse\">\n");
			out.write("<ul class=\"nav navbar-nav\">\n");
			out.write("<li class=\"active\"><a href=\"Ebay.html\" target=\"_blank\"><font color=\"black\">Ebay</font></a></li>\n");
			out.write("<li><a href=\"/custom/WorldWeather/weather.html\" target=\"_blank\"><font color=\"black\">Weather</font></a></li>\n");
			out.write("<li><a href=\"custom/Book/src/index.html\" target=\"_blank\"><font color=\"black\">Book</font></a></li>\n");
			out.write("<li><a href=\"flight.html\" target=\"_blank\"><font color=\"black\">Flight</font></a></li>\n");
			out.write("<li><a href=\"custom/Events/index.html\" target=\"_blank\"><font color=\"black\">Events</font></a></li>\n");
			out.write("<li><a href=\"custom/movie/Index.html\" target=\"_blank\"><font color=\"black\">Movie</font></a></li>\n");
			out.write("<li><a href=\"custom/translate/translate.html\" target=\"_blank\"><font color=\"black\">Translation</font></a></li>\n");
			out.write("<li><a href=\"custom/News/www/index.html\" target=\"_blank\"><font color=\"black\">News</font></a></li>\n");
			out.write("<li><a href=\"maps.html\" target=\"_blank\"><font color=\"black\">Map</font></a></li>\n");
			out.write("</ul></div>\n");

			out.write("      \t\t<P></P>\n");
			out.write("     </div>\t\n");
			out.write("      \n");
			out.write("  <footer>\n");
			out.write("      <p class=\"pull-right\"><a href=\"result?nextPage=");
			out.print(currentPage);
			out.write("&searchText=");
			out.print(searchText);
			out.write("&specialSearch=");
			out.print(specialSearch);
			out.write("&cityName=");
			out.print(cityName);
			out.write("\">Back to top &middot;</a></p> \n");
			out.write("      <p>&copy; CIS455/555 --Team 02 &middot; <a href=\"http://www.cis.upenn.edu/~cis455/\" target=\"_blank\">Class Page &middot;</a></p>\n");
			out.write("      </footer> \n");
			out.write("      \n");
			out.write("  </body>  \n");
			out.write("</html>  ");
			out.flush();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	private void permuteWordCombination(JSONArray arrayObj, int currentPos,
			String[] queryArr, String targetStr) {
		if (currentPos == queryArr.length) {
			if (arrayObj.size() >= 7)
				return;
			arrayObj.add(targetStr.trim());
		} else {
			SuggestionWords suggest = new SuggestionWords();
			ArrayList<String> currentRes = suggest
					.getWordsResult(queryArr[currentPos]);

			// fix spellcheck only shows one word
			if (currentRes == null || currentRes.size() == 0) {
				String temp = new String(targetStr);
				temp += queryArr[currentPos] + " ";
				permuteWordCombination(arrayObj, currentPos + 1, queryArr, temp);
			} else {
				for (int i = 0; i < currentRes.size(); i++) {
					String temp = new String(targetStr);
					temp += currentRes.get(i) + " ";
					permuteWordCombination(arrayObj, currentPos + 1, queryArr,
							temp);
				}
			}
		}
	}
	
	private String getThumbnailUrl(String url) {
		String tnUrl = VideoPageParser.getThumbnailUrl(url);
		if(tnUrl == null) {
			tnUrl="http://carleton.ca/edc/wp-content/plugins/video-thumbnails/default.jpg";
		}
		return tnUrl;
	}
}
