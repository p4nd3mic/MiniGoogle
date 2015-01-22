<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
  <head>
    <title>Welcome to Amazon Book Search</title>
    <link href='http://fonts.googleapis.com/css?family=Cabin:400,400italic,500,500italic,600,600italic,bold,bolditalic' rel='stylesheet' type='text/css' />
    <link href="custom/book/stylesheets/screen.css" media="all" rel="stylesheet" type="text/css"/>
    <style type="text/css">body { font-family: arial,sans-serif; background-color:#BFDFFF;} </style>
    <script src='https://ajax.googleapis.com/ajax/libs/jquery/1.6.0/jquery.min.js' type='text/javascript'></script>
    <script language="javascript" src="custom/book/javascripts/app.js" type="text/javascript"></script><script language="javascript" src="custom/book/javascripts/isbn.js" type="text/javascript"></script>
    <script type='text/javascript'>
      //<![CDATA[
        $(document).ready(function() {
          $("textarea").focus(function() {
            if ($(this).val() == "Your books.") {
              $(this).val("");
              $(this).toggleClass("edited", true);
            }
          })
          .blur(function() {
            if ($(this).val() == "" || $(this).val() == "Your books.") {
              $(this).val("Your books.");
              $(this).toggleClass("edited", false);
            }
          });
          $(":submit").click(go);
          $("a[href='#']").click(function() {
            $("#instructions").slideToggle();
          });
        });
      //]]>
    </script>
  </head>
  <body>
    <h3 id='logo'><Font color="#68798A">Search books on Amazon</font></h3>
    <div id='form'>
      <p>
        <textarea id="q" name="q">Your books.</textarea>
        <br/>
        <input name='kindle' type='checkbox' />
        Search for just Kindle books
      </p>
      <p>
        <input type='submit' value="Get item" />
      </p>
    </div>
    <div id='instructions'>
      <ol>
      <li>Books</li>
      <li>Get them now</li>
      <li>Amazon opens a search or a product page for every single book title or ISBN.</li>
      
     
      </ol>
    </div>
    <div id='explain'>
      
     
    </div>
    
    <script type="text/javascript">
    var sc_project=6879667;
    var sc_invisible=1;
    var sc_security="358e0ca9";
    </script>
    
  </body>
</html>
