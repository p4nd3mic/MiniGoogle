<!DOCTYPE html>
<html>
    <head>
        <meta charset="utf-8" />
        <title>Weather Forecast</title>

        <style type="text/css">body { font-family: arial,sans-serif; background-color:#BFDFFF;} </style>
    
        <!-- Google Fonts -->
        <link rel="stylesheet" href="http://fonts.googleapis.com/css?family=Playball|Open+Sans+Condensed:300,700" />

        <!--[if lt IE 9]>
          <script src="http://html5shiv.googlecode.com/svn/trunk/html5.js"></script>
        <![endif]-->
    </head>

    <body>

        <header align="middle">
            <h1>View Weather Forecast</h1>
        </header>

		<p class="location" align="right"></p>
        <div id="weather">

            <p id="scroller" align="middle">
                <!-- The forecast items will go here -->
            </p>

        </div>

		<P align="right"><a href="weather.jsp" > Back to Top</a></P>
        <!--<div id="clouds"></div>-->

        <!-- JavaScript includes - jQuery, moment.js and our own script.js -->
        <script src="//cdnjs.cloudflare.com/ajax/libs/jquery/1.9.1/jquery.min.js"></script>
        <script src="//cdnjs.cloudflare.com/ajax/libs/moment.js/2.0.0/moment.min.js"></script>
        <script src="custom/assets/js/script.js"></script>

    </body>
</html>