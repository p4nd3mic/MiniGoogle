function go(e) {
    var links = $("textarea").val();
    links = links.split(/[\n|;]+/);
    var justKindle = $(":checkbox")[0].checked;
    if (justKindle)
        var searchIndex = "digital-text";
    else
        var searchIndex = "books";

    var searchType = "digit";
    for (var i = 0; i < links.length; i++) {
        var url = "http://www.amazon.com/gp/";
        var args = [
            {name:"ie",value:"UTF8"},
            {name:"tag",value:"amubose-20"},
        ]
        var isbn = ISBN.parse(links[i]);
        if (isbn != null && isbn.isValid()) {
            url += "product/" + isbn.asIsbn10() + "/ref=as_li_ss_tl";
        } else {
            url += "search";
            args = args.concat([
                {name:"url",value:"search-alias=aps"},
                {name:"index",value:searchIndex},
                {name:"keywords",value:$.trim(links[i])},
            ]);
        }

        var link = url + "?" + $.param(args);
        window.open(link);
    }
}
