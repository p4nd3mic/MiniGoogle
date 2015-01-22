 
<html lang="en">  
  <head>  
    <title>CIS455/555 Search Engine--Team 02</title>
    <meta charset="utf-8">  
    <meta name="viewport" content="width=device-width, initial-scale=1.0">  
    
    <!-- Bootstrap --> 
    <link href="public/dist/css/bootstrap.css" rel="stylesheet">
	<link href="public/dist/css/bootstrap.min.css" rel="stylesheet" media="screen"> 
	
    <!-- Custom styles for this template -->
    <link href="custom/index.css" rel="stylesheet"> 
   
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
   	 	document.getElementById("msgid").style.size="10pt";
   }
   
   function ShowMsg()
	{
		document.getElementById("msgid").innerHTML="Change City Name here";
		document.getElementById("msgid").style.color="black";
		document.getElementById("msgid").style.size="12pt";
	}

	function ClearMsg()
	{
		document.getElementById("msgid").innerHTML=""
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
   
   <div class="container">
		<h2 class="searching-heading" align="middle">GOgoGO</h2>
      	
      	<div  align="left">
      		<table><tr>
      		<td><button type="button" ID="Ebay" ><a href="Ebay.jsp" target="_blank"> <font color="black">EBAY</font> </a></button></td>
      		<td></td>
      		<td><button type="button" ID="Weather"> <a href="weather.jsp" target="_blank"><font color="black">WEATHER</font> </a></button></td>
      		<td></td>
      		<td><button type="button" ID="Movie"> <a href="movie.jsp" target="_blank"><font color="black">MOVIE</font> </a></button></td>
      		<td></td>
      		<td><button type="button" ID="Book" ><a href="book.jsp" target="_blank"> <font color="black">AmazonBooks</font> </a></button></td>
      		</tr><table>
      		<P></P>
      	</div>	
      	
      	<form name="searchForm" class="form-search" action="result.jsp">
          <div align="left">
      		<table><tr>
      		<td><label id="msgid"></label></td>
      		<td></td>
      		<td><input type="text" width="50px" id="cityNameID" name="cityName" onmouseOver="ShowMsg();" onmouseOut="ClearMsg()" autofocus ></td>
      		</tr></table>
      	</div>	
        
          <P></P>
			<script src="http://int.dpool.sina.com.cn/iplookup/iplookup.php?format=js" type="text/javascript"></script>
			<script>
	    		document.getElementById("cityNameID").value=remote_ip_info["city"];
			</script>
			
			<P></P><P></P>
        		<!--<input type="text" class="form-control" id="STextID" name="searchText" onkeyup="setsearchvalue();" placeholder="Enter whatever you want to search" autofocus >
        		-->
        		<input type="text"  class="form-control" id="STextID" name="searchText" x-webkit-speech="x-webkit-speech" />
        		<P></P><P></P><P></P>
        		<P><label class="label label-warning" type="text" id="searchLabelID" name="searchLabel"><font color="blue"> GOgoGO </font></label></P>
 				<P align="right">
 					<select class="selectpicker bla bla bli" font-size="9" id="specialSearch" onchange="change();" name="specialSearch">
            			<option value="0" selected="selected">GOgoGO</option>
            			<option value="1">IMAGE</option>
            			<option value="2">VIDEO</option>
          	  			</select>
          	  	</P>
          	  	<P></P><P></P>
          	  	<P align="middle">
        			<button type="submit" class="btn btn-lg btn-primary btn-block" align="middle" ID="SButtonID" ><font color="black"><B>  G          O  </B></font></button>
        		</P>
      		</form>
      		
      	<footer>
        <p class="pull-right"><a href="index.jsp">Back to top &middot;</a></p>
        <p>&copy; CIS455/555 --Team 02 &middot; <a href="http://www.cis.upenn.edu/~cis455/" target="_blank">Class Page &middot;</a></p>
      </footer>
      
      </div><!-- /.container -->
      
      <script type="text/javascript">
      	if (document.createElement("input").webkitSpeech === undefined) {
			alert("Speech input is not supported in your browser.");
		}
      </script>

  </body>  
</html>  
