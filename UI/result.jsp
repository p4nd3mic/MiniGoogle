<%@ page contentType="text/html; charset=gb2312" language="java" %>
<%@ page  import="java.util.*, edu.upenn.cis455.*, edu.upenn.cis455.spellchecker.*" %>

<html lang="en">  
  <head>  
    <title>CIS455/555 Search Engine--Team 02</title>
    <meta charset="utf-8">  
    <meta name="viewport" content="width=device-width, initial-scale=1.0">  
    
    <!-- Bootstrap --> 
    <link href="public/dist/css/bootstrap.css" rel="stylesheet">
	<link href="public/dist/css/bootstrap.min.css" rel="stylesheet" media="screen"> 
	
    <!-- Custom styles for this template -->
    <link href="custom/result.css" rel="stylesheet"> 
    
    <!--<script src="public/assets/js/jquery.js"></script>-->
    <script src="public/assets/js/script.js"></script>
    <script src="public/dist/js/bootstrap.min.js"></script>
    
    <script type="text/javascript"> 
    function setsearchvalue() {
      var Searchtxt = document.getElementById("STextID");
      var submit = document.getElementById("SButtonID");
                
      if (Searchtxt.value !="" && Searchtxt.value !=null) {
            submit.style.visibility = 'visible';
        }
      else {
           	submit.style.visibility = 'hidden';

        }
   }
   
   function change(){
   	 	var s= document.getElementById("specialSearch");
   	 	document.getElementById("searchLabelID").innerHTML = s.options[s.selectedIndex].text;
   	 	document.getElementById("searchLabelID").style.color="blue";
   }
   
    </script>
    
    <script src="http://code.jquery.com/jquery-1.7.js"
    type="text/javascript"></script>
	<script
    src="http://ajax.googleapis.com/ajax/libs/jqueryui/1.8/jquery-ui.min.js"
    type="text/javascript"></script>
	<link
    href="http://ajax.googleapis.com/ajax/libs/jqueryui/1.8/themes/base/jquery-ui.css"
    rel="stylesheet" type="text/css" />

 
<script type="text/javascript">
$(document).ready(function() {
    $("input#STextID").autocomplete({
        width: 300,
        max: 10,
        delay: 30,
        minLength: 1,
        autoFocus: true,
        cacheLength: 1,
        scroll: true,
        highlight: false,
        source: function(request, response) {
            $.ajax({
                url: "JSON/AjaxRequest",
                dataType: "json",
                data: request,
                success: function( data, textStatus, jqXHR) {
                    console.log( data);
                    var items = data;
                    response(items);
                },
                error: function(jqXHR, textStatus, errorThrown){
                     console.log( textStatus);
                }
            });
        }
 
    });
});
    
	</script>
    <!-- HTML5 shim and Respond.js IE8 support of HTML5 elements and media queries -->  
    <!--[if lt IE 9]>  
      <script src="public/assets/js/html5shiv.js"></script>  
      <script src="public/assets/js/respond.min.js"></script>  
    <![endif]--> 
    
  </head>  
  
  <body> 
  
 <div class="navbar navbar-default navbar-fixed-top">
      <div class="container">
        <div class="navbar-collapse collapse">
          <a class="navbar-brand" href="index.jsp"><font size="18px" color="black"><B>GOgoGO </font></B></a>
          <form class="navbar-form navbar-right"  action="result.jsp">
          	
          	<div class="form-group">
          		<label class="label label-warning" height="200px" type="text" id="searchLabelID" name="searchLabel"> <font color="blue">GOgoGO</font></label>
          	</div>
            <div class="form-group">
              <!--<input type="text" ID="STextID" width="600px" name="searchText" onkeyup="setsearchvalue();" placeholder="Enter whatever you want to search" autofocus >-->
              <input type="text" ID="STextID" width="600px" name="searchText" />
            </div>
            
            <div class="form-group">
            	<select id="specialSearch" onchange="change();" name="specialSearch">
            			<option value="0" selected="selected">GOgoGO</option>
            			<option value="1">IMAGE</option>
            			<option value="2">VIDEO</option>
          	  			</select>
            </div>
            <button type="submit" class="btn btn-success" ID="SButtonID" >GO</button>
          </form>
          
        </div><!--/.navbar-collapse -->
      </div>
    </div>
  	<P></P> <P></P>
  <%
  	String searchText=request.getParameter("searchText");
  	DatabaseWrapper wrapper = DBSingleton.getInstance().getWrapper();
  	SuggestionWords suggest = new SuggestionWords();
  	String [] words=searchText.toLowerCase().split(" ");
  	
  	StringBuffer sb= new StringBuffer();
  	int kk=0;
  	boolean notFullMatch=false;
  	for(kk=0;kk<words.length;kk++){
  		wrapper.increaseHit(words[kk]);
  		if(!words[kk].equals(suggest.getWordsResult(words[kk]).get(0))){
  			notFullMatch=true;
  			sb.append(suggest.getWordsResult(words[kk]).get(0)+" ");
  		}
  		else{
  			sb.append(words[kk]);
  		}
  	}	
  	
  	String cityName=request.getParameter("cityName");
  	GetResult ge= new GetResult(searchText, cityName);
  	ArrayList<ResultSet> results= ge.getResults();
  	ArrayList<String> StemmerWords=ge.getAllStemmerWords();
  	
  	int specialSearch;
  	if(request.getParameter("specialSearch")==null)
  		specialSearch=0;
  	else
  		specialSearch= Integer.parseInt(request.getParameter("specialSearch"));
  	
  	int nextPage;
  	if(request.getParameter("nextPage")==null||request.getParameter("nextPage")=="")
  		nextPage=1;
  	else
  		nextPage= Integer.parseInt(request.getParameter("nextPage"));
  		
  	int currentPage=nextPage;
  	int entryNum=0;
  	int pagination_length=5;
  	int n_doc_per_page=10;
  	int resLength=results.size();
  	int n_pages;
  	
  	if(resLength%n_doc_per_page==0)
  		n_pages=resLength/n_doc_per_page;
  	else
  		n_pages=(resLength/n_doc_per_page)+1;
  	
  %>
  
  <div class="container" id="resultID">
  <%
  	if(notFullMatch){
  %>
  <P><a href="result.jsp?nextPage=1&searchText=<%=sb.toString().trim()%>&specialSearch=<%=specialSearch%>&cityName=<%=cityName%>"><FONT size="5" color="red" ><B><i>Did you mean <%=sb.toString()%>?</i></B></a></P>
  <% }
  	if(specialSearch==0 || specialSearch==3||specialSearch==4){
  	  for (entryNum=((currentPage-1)*n_doc_per_page); entryNum<currentPage*n_doc_per_page; entryNum++){
  	  	if(entryNum>resLength)
  	  		break;
  %>
   			<div class="row"  align="middle">
        		<div class="col-6 col-sm-6 col-lg-12" >
          			<div class="panel panel-info">
            			<div class="panel-heading" >
              				<h3 class="panel-title" align="middle"> <font color="black" size="3"><B><%=results.get(entryNum).title%></B></a> </h3>
              				<label align="right"> <a href="<%=results.get(entryNum).url%>" target="_blank"><font size="1.5" color="blue"><%=results.get(entryNum).url%></a></label>
            			</div>
            		<%if(!results.get(entryNum).summary.equals("")){%>
            		<div class="context">
              			<p><font size="3" color="black"><%=results.get(entryNum).summary%></font></p>
            		</div>
            	<% } %>
          </div>
        </div>
      </div>      
    <% }
    }
    else {
  	 	if(resLength>=(currentPage*n_doc_per_page)){
  	   		for (int rowNum=((currentPage-1)*n_doc_per_page); rowNum<currentPage*n_doc_per_page; rowNum+=2){
  	%>
   			  <div class="row">
   				<% for (int i=0; i<2;i++){
   					if(rowNum+i<=resLength-1){
   				%>
        			  <div class="col-6 col-sm-6 col-lg-6">
          				<div class="panel panel-info">
            			  <div class="panel-heading">
              		        <h3 class="panel-title" align="middle"> <font color="black" size="3"><%=results.get(rowNum+i).title%> </a> </h3>
            			    <label align="right"> <a href="<%=results.get(rowNum+i).url%>" target="_blank"><font size="1.5" color="blue"><%=results.get(rowNum+i).url%></a></label>
            			  </div>
                          <div class="context">
              			    <p><object data="<%=results.get(rowNum+i).url%>"> </font></p>
            		      </div>
          				</div>
        			  </div>
        
    				<%}
    				}
    				%>
   				</div>
     
   			 <% }
     		}
     	else{
     		for (int rowNum=((currentPage-1)*n_doc_per_page); rowNum<resLength; rowNum++){
    	%> 
      			<div class="col-6 col-sm-6 col-lg-11">
          		  <div class="panel panel-info">
            		<div class="panel-heading">
             		  <h3 class="panel-title" align="middle"> <font color="black" size="3"><%=results.get(rowNum).title%> </a> </h3>
            	      <label align="right"> <a href="<%=results.get(rowNum).url%>" target="_blank"><font size="1.5" color="blue"><%=results.get(rowNum).url%></a></label>
            	    </div>
                    <div class="context">
              		  <p><object data="<%=results.get(rowNum).url%>"> </font></p>
            		</div>
        		</div>
     		  </div>
     
     	<% }
     	}
      }
     %>
   
  	<script type="text/javascript" src="custom/hilitor.js"></script> 
  	<script type="text/javascript"> 
  		var myHilitor; 
  		myHilitor= new Hilitor("resultID"); 
  		myHilitor.setMatchType("left"); 
  		<%
  		String HighlightingWords=searchText;
  		for(int i=0;i<StemmerWords.size();i++){
  			HighlightingWords=HighlightingWords+" "+StemmerWords.get(i);
  		}
  		%>
  		var keyword='<%=HighlightingWords%>';
  		myHilitor.apply(keyword);
  		
  	</script>
  	
     <%
     	int prevPage;
     	if(currentPage>1)
     		prevPage=currentPage-1;
     	else
     		prevPage=currentPage;
     		
     	if(currentPage==n_pages)
     		nextPage=currentPage;
     	else
     		nextPage=currentPage+1;
     	
     	int pageSection;
     	if(currentPage%pagination_length==0)
     		pageSection=currentPage/pagination_length;
     	else
     		pageSection=(currentPage/pagination_length)+1;
     %>
     
     <div class="container" align="middle">
      <ul class="pager">
 			<li><a href="result.jsp?nextPage=1&searchText=<%=searchText%>&specialSearch=<%=specialSearch%>&cityName=<%=cityName%>"> <font color="purple">First </font></a></li>
         	<li><a href="result.jsp?nextPage=<%=prevPage%>&searchText=<%=searchText%>&specialSearch=<%=specialSearch%>&cityName=<%=cityName%>"> <font color="purple">Prev </font></a></li>
         	
         	<%
         		for(int i=((pageSection-1)*pagination_length+1);i<=(pageSection*pagination_length);i++){
         			if(i>n_pages)
         				break;
         			if(i==currentPage){
         	%>
         	<li><a href="result.jsp?nextPage=<%=i%>&searchText=<%=searchText%>&specialSearch=<%=specialSearch%>&cityName=<%=cityName%>"> <font color="orange"><%=i%></font> </a></li>
         	<% }
         		else{
         	%>
         		<li><a href="result.jsp?nextPage=<%=i%>&searchText=<%=searchText%>&specialSearch=<%=specialSearch%>&cityName=<%=cityName%>"> <font color="purple"><%=i%></font> </a></li>
         	<%
         		}
         	  }
         	%>
         	<li><a href="result.jsp?nextPage=<%=nextPage%>&searchText=<%=searchText%>&specialSearch=<%=specialSearch%>&cityName=<%=cityName%>"> <font color="purple">Next </font> </a></li>
         	<li><a href="result.jsp?nextPage=<%=n_pages%>&searchText=<%=searchText%>&specialSearch=<%=specialSearch%>&cityName=<%=cityName%>"> <font color="purple">Last </font></a></li>
         	</ul>     
     </div>
     
     <div class="container" align="left">
      		<table><tr>
      		<td><button type="button" ID="Ebay" ><a href="Ebay.jsp" target="_blank"> <font color="black">EBAY</font> </a></button></td>
      		<td></td>
      		<td><button type="button" ID="Weather"> <a href="weather.jsp" target="_blank"><font color="black">WEATHER</font> </a></button></td>
      		<td></td>
      		<td><button type="button" ID="Book" ><a href="book.jsp" target="_blank"> <font color="black">Amazon-Books</font> </a></button></td>
      		</tr></table>
      		<P></P>
     </div>	
      
  <footer>
      <p class="pull-right"><a href="result.jsp?nextPage=<%=currentPage%>&searchText=<%=searchText%>&specialSearch=<%=specialSearch%>&cityName=<%=cityName%>">Back to top &middot;</a></p> 
      <p>&copy; CIS455/555 --Team 02 &middot; <a href="http://www.cis.upenn.edu/~cis455/" target="_blank">Class Page &middot;</a></p>
      </footer> 
      
  </body>  
</html>  