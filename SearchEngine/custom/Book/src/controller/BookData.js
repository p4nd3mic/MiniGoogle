(function(lib){
    var controller = lib.util.extendNamespace('controller');
    
    controller.BookData = lib.Class.extend({
        init :function() {

            this._requestSet = {};
            this._bookSetId = 0;
            this._books = [];
            this._searchTerms = null;
            this._currentPage = 1;
            this._maxPages = null;
            this._currentBookIndex = 0;
            lib.api.init(lib.config.apiKey, 66, 130);
            this._api = lib.api;

            var self = this;

            this._fnWrappers = {
                addBooks : function(o) {
                    self._addBooks(o);
                },
                addLargeBookImage : function(o) {
                    self._addLargeBookImage(o);
                },
                loadOffers : function(o) {
                    self._loadOffers(o);
                }
            };
        },

        /**
         * loads books from the search terms
         * @param {string} searchTerms the search terms
         * @param {function(Book[], boolean hasMore)} callback the callback that gets called when the request has finished
         *
         */
        search : function(searchTerms, callback) {
            this._bookSetId++;
            this._searchTerms = searchTerms;
            this._currentPage = 1;
            this._maxPages = 1;
            this._books = [];

            var requestId = this._api.search(searchTerms, this._api.IMG_SIZE_SM, this._currentPage, this._fnWrappers.addBooks);
            this._createRequestSet(requestId, null, callback);
        },

        loadMore : function(callback) {
            this._currentPage++;
            var requestId = this._api.search(this._searchTerms, this._api.IMG_SIZE_SM, this._currentPage, this._fnWrappers.addBooks);
            this._createRequestSet(requestId, null, callback);
        },

        hasMore : function() {
            return this._currentPage < this._maxPages;
        },

        getBook : function(index) {
            if(index < this._books.length) {
                this._currentBookIndex = index;
                return this._books[index];
            }
            return null;
        },

        getCurrentBook : function() {
            return this._books[this._currentBookIndex];
        },

        loadLargeBookImage : function(index, callback) {
            var book = this._books[index];
            if(book.imageLarge) {
                callback(book);
            } else {
                var requestId = this._api.bookInfo(book.isbn10, this._api.IMG_SIZE_LG, this._fnWrappers.addLargeBookImage);
                this._createRequestSet(requestId, index, callback);
            }

        },

        loadOffers : function(index, callback) {
            var book = this._books[index];
            if(book.offers) {
                callback(book);
            } else {
                var requestId = this._api.bookPrices(book.isbn10, this._fnWrappers.loadOffers);
                this._createRequestSet(requestId, index, callback);
            }

        },

        _getRequestSet : function(requestId) {
            var request = this._requestSet[requestId];
            delete this._requestSet[requestId];
            if(request.bookSet === this._bookSetId) {
                return request;
            } else {
                return null;
            }


        },

        /**
         * creates a requestSet object for keeping track of jsonp requests so that we correctly update the right information
         * @param {int} requestId the number returned from any API calls
         * @param {int} [bookIndex] optional param of the book this request is for. If this is loading more books for instance you would not provide a bookIndex
         * @param {function} callback the callback function that should be called when the request is completed
         */
        _createRequestSet : function(requestId, bookIndex, callback) {
            this._requestSet[requestId] = {
                bookSet : this._bookSetId,
                bookIndex : bookIndex,
                callback : callback
            };
        },

        _addLargeBookImage : function(ajax) {
            var request = this._getRequestSet(ajax.id);
            var book;
            if(request) {
                book = this._books[request.bookIndex];
                if(!ajax.status) {
                    if(this._currentBookIndex === request.bookIndex) {
                        this._logError("There was an error connecting to the server");
                    }
                } else {
                    book.imageLarge = ajax.data.imageLarge;
                }

                if(this._currentBookIndex === request.bookIndex) {
                    request.callback(book);
                }
            }
        },


        _addBooks : function(ajax) {
            var request = this._getRequestSet(ajax.id);
            var books = null;
            var more = false;
            if(request) {
                if(!ajax.status) {
                    this._logError("There was an error connecting to the server");
                    this._currentPage = this._currentPage === 1 ? 1 : this._currentPage - 1;
                    more = true;
                } else if(ajax.data === null) {
                    more = false;


                } else {
                    this._maxPages = ajax.data.pages;

                    if(ajax.data.page < ajax.data.pages) {
                        more = true;
                    }
                    this._books = this._books.concat(ajax.data.books);
                    books = ajax.data.books;
                }

                request.callback(books, more);
            }
        },

        _loadOffers : function(ajax) {
            var request = this._getRequestSet(ajax.id);
            var book;
            if(request) {
                if(!ajax.status) {
                    this._logError("There was an error connecting to the server");
                } else if(ajax.data === null) {
                    book = null; 
                } else {

                    book = this._books[request.bookIndex];
                    book.offers = ajax.data;
                }

                if(this._currentBookIndex === request.bookIndex) {
                    request.callback(book);
                }
            }
        },

        _logError : function(msg) {
            alert(msg);
        }
    });


    
})(JSBookSearch);
