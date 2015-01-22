(function(lib){
    var view = lib.util.extendNamespace("view");
    
    /**
     * creates a view object that displays the details of the books
     * 
     */
    view.BookDetails = view.View.extend({
        init : function() {
            this._image = lib.dom.create({
                tag : 'div',
                options : {domClass: [
                    lib.constants.css.bookImage, 
                    lib.constants.css.bookSizeLarge
                ]},
                jquery : true

            });

            var imageWell = lib.dom.create({
                tag : 'div',
                options : {domClass : lib.constants.css.bookDetailsImageWell}/*,
                children : this._image*/
            });



            this._metaData = lib.dom.create({
                tag : 'div',
                options : {domClass : lib.constants.css.bookDetailsData},
                jquery : true
            });

            this._container = lib.dom.create({
                tag : 'div',
                options : {domClass : [lib.constants.css.bookDetails, lib.constants.css.tabContent]},
                children : [this._image, imageWell, this._metaData]
            });

            this._scrollbar = new view.ScrollBar(this._container, this._metaData);




        },  
        /**
         * sets the book to display
         * @param {model.Book} book
         */
        setBook : function(book) {
            this._book = book;
            var url = lib.util.empty(book.imageLarge) ? lib.constants.resources.images.ajaxLoaderOnWhite : book.imageLarge;

            this._image.css({
                    'background-image' : 'url("' + url + '")'
            });

            this._metaData.children().remove();

            var elements = [];
            var keys = [
                'author',
                'isbn10',
                'isbn13',
                'edition',
                'publisher',
                'publishedDate',
                'msrp',
                'pages'
            ];


            elements.push(lib.dom.create({
                tag : 'div',
                options : {domClass : lib.constants.css.bookDetailsTitle},
                text : book.title
            }));

            for(var i in keys) {
                if(keys.hasOwnProperty(i)) {
                    var key = keys[i];
                    var text = book[key];
                    if(lib.util.empty(text)) continue;
                    var label = lib.constants.strings.bookMetaNames[key];
                    elements.push(this._createMetaRow(label, text));
                }
            }

            this._metaData.append(elements);

        },

        updateImage : function(book) {
            var url = lib.util.empty(book.imageLarge) ? book.imageSmall : book.imageLarge;

            this._image.css({
                    'background-image' : 'url("' + url + '")'
            });
        },

        _createMetaRow : function(label, text) {
            var domLabel = lib.dom.create({
                tag : 'div',
                options : {domClass : lib.constants.css.bookDetailsLabel},
                text : label
            });

            var domText = lib.dom.create({
                tag : 'div',
                options : {domClass : lib.constants.css.bookDetailsText},
                text : text
            });

            var row = lib.dom.create({
                tag : 'div',
                options : {domClass : lib.constants.css.bookDetailsRow},
                children : [domLabel, domText]
            });
            return row;
        },

        build : function() {
            this._scrollbar.build();
        },

        redraw : function() {
            this._scrollbar.redraw();
        }
    });

    
})(JSBookSearch);
