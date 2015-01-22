String.prototype.contains = function(it) {
    return this.indexOf(it) !== -1;
};

var takeOutDebug = function() {
	$(".removeInFinalVersion").html("");				/* .removeInFinalVersion is used to take out code-parts completely, */
	$(".removeInFinalVersion2").css("display", "none");	/* .removeInFinalVersion2 to prevent them from being shown, preventing possible errors on sides of jQuery that would occure if the code-part were taken out completely*/
}

var search_form_submit = function() {
    /*Linked to search_div onSubmit event!!!*/
    var view = {};
    var out = Mustache.render("http://www.omdbapi.com/?s={{>title}}&i=&t=&y={{>year}}&r=JSON&plot=short&callback=&tomatoes=true", view, {title: $("#title_input").val(), year: $("#year_input").val()});
    $("#search_resulthtml").html(out);
    return out;
};

var detailed_film_info_submit = function(titleToSearch) {
    /**/
    var view = {};
    var out = Mustache.render("http://www.omdbapi.com/?s=&i=&t={{>title}}&y=&r=JSON&plot=full&callback=&tomatoes=true", view, {title: titleToSearch});
    /**/
    var resHtml = $("#search_resulthtml");
    /*                      *//*                   *//*                                */
    resHtml.html().contains("<hr>") ? resHtml.html(resHtml.html().split("<hr>")[0] + "<hr>" + out) : resHtml.append("<hr>" + out);
    return out;
};

/* http://www.omdbapi.com/?s= &i= &t= &y= &r= &plot= &callback= &tomatoes= */

$(document).ready(function() {
	/*#-#-#-#-#-#-#-#-#-#-#-#-#-#-#-#-#-#-#-#-*/
	/*#-#-#-#-#-*/takeOutDebug(); /*#-#-#-#-#-*/
	/*#-#-#-#-#-#-#-#-#-#-#-#-#-#-#-#-#-#-#-#-*/
    $("input[type=submit]").on("click", function(e) {
        e.preventDefault();
        var url = search_form_submit();
        var out = "";
        $.getJSON(url, function(data) {
            $("table.search_result_table tbody").children().css("display", "");
            var searchArr = data.Search;
            var currElem;
            var $currObjs;
            /*$("table.search_result_table tbody").find("tr:nth-child(1)").children().html(data.search[0].title);*/
            for (var i = 1; i < 11; i++) {
                currElem = searchArr[i - 1];
                /*target td in tr at index i:                                                *//*change contents:*/
                /*$("table.search_result_table tbody").find("tr:nth-child(" + i + ")").children().html(Mustache.render("{{#Title}}Title: \"{{Title}}\"{{/Title}}{{#Year}} | Year: ({{Year}}){{/Year}}{{#Type}} | Type: {{Type}}{{/Type}}{{#imdbID}} | imdbID: {{imdbID}}{{/imdbID}}", currElem, {}));*/

                /*target the tds in tr at index i                                                         */
                $currObjs = $("table.search_result_table tbody").find("tr:nth-child(" + i + ")").children();

                /*deal with title*/
                $currObjs.filter("td.Title").find("a").html(Mustache.render("{{#Title}}{{Title}}{{/Title}}{{^Title}}N/A{{/Title}}", currElem, {}));
                /*deal with title*/
                $currObjs.filter("td.Year").html(Mustache.render("{{#Year}}{{Year}}{{/Year}}{{^Year}}N/A{{/Year}}", currElem, {}));
                /*deal with title*/
                $currObjs.filter("td.Type").html(Mustache.render("{{#Type}}{{Type}}{{/Type}}{{^Type}}N/A{{/Type}}", currElem, {}));
                /*deal with title*/
                $currObjs.filter("td.imdbID").html(Mustache.render("{{#imdbID}}{{imdbID}}{{/imdbID}}{{^imdbID}}N/A{{/imdbID}}", currElem, {}));

                if ($currObjs.filter(".Title").children().html() === "N/A") {
                    $currObjs.filter(".Title").parent().prev().nextAll().css("display", "none");
                    break;
                }
            }

        }).done(function() {
            out += "Successfully Loaded the film data.<br />";
            $("#final_film_info").html(out);
			
			$(".displayAfterSearch").css("display", "");
			$("a.title_link").eq(0).click();
        }).fail(function(jqxhr, textStatus, error) {
            var err = textStatus + ", " + error;
            $("#final_film_info").html("Request Failed: " + err + ".<br />");
        });
    });

    $(".title_link").on("click", function(e) {
        var $this = $(this);
        var url = detailed_film_info_submit($this.text());
        var out = "";
        $.getJSON(url, function(data) {
			$("#detailed_film_info").fadeIn("slow");;
            
            /*poster-----------------*/
			if(data.Poster !== "N/A")
			$("#detailed_film_info_poster").attr("src", data.Poster);
			else
			$("#detailed_film_info_poster").disable();
			$("#detailed_film_info_poster").attr("alt", "Movie poster not found, sorry! Original can be found here: " + data.Poster);
			if(data.Poster != "N/A")
            $("#detailed_film_info_poster_link").attr("href", data.Poster);
            
            /*title, year, type------*/
            $("#detailed_film_info_title").html(data.Title);
            $("#detailed_film_info_year").html(data.Year);
            $("#detailed_film_info_type").html(data.Type);
            
            /*rated, runtime, genre--*/
            $("#detailed_film_info_rated").html(data.Rated);
            $("#detailed_film_info_runtime").html(data.Runtime);
            $("#detailed_film_info_genre").html(data.Genre);
            
            /*released, dvd, language*/
            $("#detailed_film_info_released").html(data.Released);
            $("#detailed_film_info_dvd").html(data.DVD);
            $("#detailed_film_info_language").html(data.Language);
            
            /*plot-------------------*/
            $("#detailed_film_info_plot").html(data.Plot);
            
            /*actors-----------------*/
            $("#detailed_film_info_actors").html(data.Actors);
            
            /*metascore, imdbrating, tomatorating*/
            $("#detailed_film_info_metascore").html(data.Metascore);
            $("#detailed_film_info_imdbrating").html(data.imdbRating);
            $("#detailed_film_info_tomatorating").html(data.tomatoRating);
            
            /*website----------------*/
            $("#detailed_film_info_website").html(data.Website);
            $("#detailed_film_info_website_link").attr("href", data.Website);            
        }).done(function() {
            out += "Successfully Loaded the detailed film data.<br />";
            $("#final_film_info").html(out);
        }).fail(function(jqxhr, textStatus, error) {
            var err = textStatus + ", " + error;
            $("#final_film_info").html("Request Failed: " + err + ".<br />");
        });
    });
});

