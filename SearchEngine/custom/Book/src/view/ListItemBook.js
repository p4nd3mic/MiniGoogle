(function(lib){
    var view = lib.util.extendNamespace("view");
    view.ListItemBook = view.ListItem.extend({
        /**
         * creates the dom for a list item of a book
         * @constructor
         * @param {model.Book} book 
         */
        init : function(book) {
            var img = lib.dom.create({
                tag : 'div',
                options : {domClass: [
                    lib.constants.css.imageBox,
                    lib.constants.css.bookImage, 
                    lib.constants.css.bookSizeSmall
                ]},
                css : {
                    'background-image' : 'url("' + book.imageSmall + '")'
                }
            });

            var title = lib.dom.create({
                tag : 'div',
                options : {domClass : [lib.constants.css.listBookItemTitle, lib.constants.css.listTextDark]},
                text : book.title
            });

            var keys = lib.constants.strings.bookMetaNames;
            var metaKeys = [keys.author, keys.isbn10, keys.isbn13];
            var metaVals = [book.author, book.isbn10, book.isbn13];
            var elements = [img, title];

            // build elements for meta info, and add the to elements array
            for(var i = 0; i < metaKeys.length; i++) {
                if ( !lib.util.empty(metaVals[i]) ) {
                    var keyElem = lib.dom.create({
                        tag : 'span',
                        options : {domClass : lib.constants.css.listTextLight},
                        text : metaKeys[i] + ':'
                    });
                    var whole = lib.dom.create({
                        tag : 'div',
                        options : {domClass : [lib.constants.css.listBookItemMetaText, lib.constants.css.listTextMedium]},
                        children : [keyElem, metaVals[i]]
                    });
                    elements.push(whole);
                }
            }


            this._super(elements, true);
            lib.dom.addClass(this._container, lib.constants.css.listBookItem);


        }
    });


})(JSBookSearch);