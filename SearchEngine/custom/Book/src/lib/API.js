// require mBook, mBookOffers, mMerchant

(function(lib){
    var api = lib.util.extendNamespace("api");
    api.API_KEY = null;
    api.IMG_SIZE_SM = null;
    api.IMG_SIZE_LG = null;
    api.BASE_URL = 'http://api2.campusbooks.com/12/rest/';

    api.init = function(API_KEY, IMG_SIZE_SM, IMG_SIZE_LG) {
        api.API_KEY = API_KEY;
        api.IMG_SIZE_SM = IMG_SIZE_SM;
        api.IMG_SIZE_LG = IMG_SIZE_LG;
        this._requestId = 0;
    }
    
    


    
    api.bookPrices = function(isbn, callback) {
        return api._genericSystem('prices', {'isbn':isbn}, api._bookPrices, callback, 20000);
    }

    api.bookInfo = function(isbn, imgSize, callback) {
        // not using because returns inferior results to search function
        //this._genericSystem('bookinfo', {'isbn':isbn,'image_width':imgWidth}, this._bookInfo, callback);

        return api._genericSystem(
            'search',
            {'keywords':isbn,'page':1, image_height : imgSize}, 
            api._bookInfo, 
            callback,
            10000,
            imgSize
        );
    }

    
    /**
     * performes a search using the provided keywords and page
     * @param {string} keywords the search terms
     * @param {IMG_SIZE_SM|IMG_SIZE_LG} imgSize determines the size of the image that will be returned for the results
     * @param {int} page the page of results to return
     * @param {function({boolean status, obj data, int id})} callback the callback function to be called when the request finishes
     * @return {int} the unique id for this request
     */
    api.search = function(keywords, imgSize, page, callback) {
        return api._genericSystem(
            'search', 
            {'keywords':keywords, 'page':page, image_height : imgSize}, 
            api._search, 
            callback,
            10000
        );
    }

    /*
    api.bookPricesAndInfo = function(isbn, imgWidth, callback) {
        var self = this;
        this._genericSystem(
            'bookprices', 
            {'isbn':isbn, 'img_width':imgWidth}, 
            function(data){
                self._bookPricesAndInfo.call(self, data);
            },
            callback
        );
    }
    */


    api.merchants = function(callback) {
        return api._genericSystem('merchants', {'coupons':''}, api._merchants, callback, 15000);
    }


    api._bookPrices = function(data) {
        if(data.offers === undefined) return null;
        var offers = new lib.model.BookOffers(data.offers.condition);
        return offers;
    }

    // based off data returned from search function
    api._bookInfo = function(data, optionalData) {
        if(data.results === undefined) return null;
        var tmp = data.results.book;
        if(tmp.length == 0) return null;
        var book = new lib.model.Book(tmp[0]);
        
        if(optionalData == api.IMG_SIZE_LG) {
            book.imageLarge = book.imageSmall;
            book.imageSmall = null;
        }
        
        
        return book;
    }

    // based off data returned from bookInfo function
    api._bookInfoHelper = function(data) {
        if(data.book === undefined) return null;
        return new lib.model.Book(data.book);
    }


    /*
     * return object
     * @param {object} data
     * @return {object} [int] obj.pages, [int] obj.page, [Book] obj.books
     * 
     */
    api._search = function(data) {
        if(data.results == undefined) return null;
        var results = {
            pages : parseInt(data.pages),
            page : parseInt(data.current_page)
        };
        var books = [];

        var tmp = data.results.book;

        for(var i = 0; i < tmp.length; i++) {
            books.push(new lib.model.Book(tmp[i]));
        }

        results.books = books;
        return results;
    }


    api._bookPricesAndInfo = function(data) {
        var book = this._bookInfoHelper(data);
        if(book == null) return null;
        book.offers = this._bookPrices(data);
        return book;
    }


    api._merchants = function(data) {
        var merchants = [];
        for(var i = 0; i < data.merchant.length; i++) {
            merchants.push(new lib.model.Merchant(data.merchant[i]));
        }

        return merchants;
    }

    api._genericSystem = function(func, params, dataFunc, callback, timeout, optionalData) {
        var requestId = this._requestId++;
        this._buildQuery(func, params, function(data, status) {
            var output = {status:true, data:null, id : requestId};
            if(status !== 'success' || data.response['@attributes'].status !== "ok") {
                output.status = false;
            } else {
            
                output.data = dataFunc(data.response.page, optionalData);
            }
            
            callback(output);
        }, timeout);
        return requestId;
    }

    api._buildQuery = function(func, params, callback, timeout) {
        var full = this.BASE_URL + func + "?";
        full += "key=" + this.API_KEY + "&format=json";
        for(var key in params) {
            if (params.hasOwnProperty(key)) {
                full += "&" + key + '=' + params[key];
            }
        }

        return this._loadQuery(full, callback, timeout);
    }

    api._loadQuery = function(url, callback, timeout) {
       
        $.ajax({
            url: url,
            dataType: 'jsonp',
            data: '',
            success: callback,
            error : callback,
            timeout : timeout
        });
    }
    
    
})(JSBookSearch);