(function(lib){
	var controller = lib.util.extendNamespace('controller');
	
	controller.MainPage = lib.Class.extend({
        init : function(parent) {
            lib.dom.addClass(parent, 'container');
            this.bookData = new lib.controller.BookData();
            var self = this;

            var offerSelected = function(type, index) {
                self._offerSelected(type, index);
            }
            var bookSelected = function(index) {
                self._bookSelected(index);
            }
            var loadMore = function() {
                self._loadMore();
            }

            this._getHighResImg = function(book) {
                self.views.bookDetails.updateImage(book);
            }

            var startSearch = function(val) {
                return self._startSearch(val);
            }

            this.listingOpener = new lib.controller.ListingOpener();

            this.views = {
                tabView : new lib.view.TabView(),
                bookDetails : new lib.view.BookDetails(),
                newPrices : new lib.view.OfferList(offerSelected, lib.constants.Condition.NEW),
                usedPrices : new lib.view.OfferList(offerSelected, lib.constants.Condition.USED),
                ebookPrices : new lib.view.OfferList(offerSelected, lib.constants.Condition.EBOOK),
                list : new lib.view.BookList(bookSelected, loadMore),
                columnView : null,
                searchView : new lib.view.KeywordInputView(startSearch)
            };

            this.views.tabView.addTab("Info", this.views.bookDetails);
            this.views.tabView.addTab("New", this.views.newPrices);
            this.views.tabView.addTab("Used", this.views.usedPrices);
            this.views.tabView.addTab("eBook", this.views.ebookPrices);

            this.views.columnView = new lib.view.TwoColumnView({
                        view : this.views.list,
                        minWidth : 400,
                        percent : 40
                    },{
                        view : this.views.tabView,
                        minWidth : 400,
                        percent : 60,
                        switchColumnView : lib.dom.create({
                            tag: 'div',
                            options : {
                                domClass : lib.constants.css.twoColumnViewSwitchButton
                                },
                            text : 'Back To All Books',
                            jquery : true
                        })
                    }, 30);





            lib.dom.setId(this.views.searchView.getDomNode(), 'topRow');
            lib.dom.setId(this.views.columnView.getDomNode(), 'bottomRow');
            $(parent).append(this.views.columnView.getDomNode());
            $(parent).append(this.views.searchView.getDomNode());

            this.views.columnView.redraw();
            this.views.tabView.showTab(0);



        },

        _startSearch : function(val) {
             var views = this.views;
             var self = this;
             var search = function(books, hasMore) {
                 self._search(books, hasMore);
             }
             try {


                views.list.clearElements(val);
                views.list.setLoading(true);
                views.tabView.setEmpty(true);

                this.bookData.search(val, search)

                views.columnView.setColumnFocus(lib.view.TwoColumnView.prototype.LEFT);
            }
            catch(e) {
                console.log(e);
            }
            return false;
        },

        _offerSelected : function(type, index) {
            var offers = this.bookData.getCurrentBook().offers;
            this.listingOpener.openUrl(offers.types[type][index].link);
        },

        _getOffers : function(book) {
            var offers = book.offers
            var views = this.views;
            views.newPrices.addOffers(offers.types[lib.constants.Condition.NEW]);
            views.usedPrices.addOffers(offers.types[lib.constants.Condition.USED]);
            views.ebookPrices.addOffers(offers.types[lib.constants.Condition.EBOOK]);
        },

        _bookSelected : function(index) {
            var views = this.views;
            var book = this.bookData.getBook(index)
            var self = this;
            var getOffers = function(book) {
                self._getOffers(book);
            }


            views.bookDetails.setBook(book);
            views.tabView.setEmpty(false);
            views.bookDetails.redraw();
            var prices = [views.newPrices, views.usedPrices, views.ebookPrices];
            if(book.imageLarge == null) {
                this.bookData.loadLargeBookImage(index, this._getHighResImg);
            }
            if(book.offers == null) {

                this.bookData.loadOffers(index, getOffers);
                for(var i in prices) {
                    prices[i].clearElements();
                    prices[i].setLoading(true);
                }
            } else {
                for(var i in prices) {
                    prices[i].clearElements();

                }
                getOffers(book);
            }
            views.columnView.setColumnFocus(lib.view.TwoColumnView.prototype.RIGHT);

        },

        _loadMore : function() {
            var self = this;
            var search = function(books, hasMore) {
                self._search(books, hasMore);
            }
            this.bookData.loadMore(search);
        },

        _search : function(books, hasMore) {
            this.views.list.addBooks(books, hasMore);
        }
    });
	
	
})(JSBookSearch);
