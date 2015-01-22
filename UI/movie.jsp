<html>
  <head>
    <title>Movie Search</title>
    <link type="text/css" href="custom/movie.css" rel="stylesheet" />
    <script src="http://ajax.googleapis.com/ajax/libs/jquery/1.9.1/jquery.min.js"></script>
  </head>
  <body>
    <div id="container">
      <br />
      <div id="search-box">
        <form id="form-search" action="">
          <input type="textbox" name="movie-title" id="input-box">
          <button type="submit">Search Movie Title</button>
        </form>
        <p class="hidden error">No Search Results</p>
      </div>
      <br />
      <div id="search-results">
        <ul id="results-list">
        </ul>
      </div>
      <br />
      <div id="selected">
        <div id="selected-result">

        </div>
      </div>
      <br />
    </div>
  <script src="custom/movieHelper.js"></script>
  </body>
</html>
