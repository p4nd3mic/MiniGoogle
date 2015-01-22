(function(lib){
    var view = lib.util.extendNamespace("view");
    view.BookList = view.ListView.extend({
        init : function(selectedCallback, loadMoreCallback) {
    

            this._loadMoreBtn = this._makeLoadMoreBtn();

            this._loadMoreCallback = loadMoreCallback;

            this._super({
                clickCallback: selectedCallback,
                emptyLabel : lib.constants.strings.listBookLabels.emptyList,
                footer : this._loadMoreBtn,
                scroll : true
            });

            lib.dom.addClass(this._container, lib.constants.css.listBooks);
            
        },


        _makeLoadMoreBtn : function() {
            var btn = lib.dom.create({
                tag : 'li',
                options : {domClass : lib.constants.css.listLoadMore},
                text : lib.constants.strings.listBookLabels.loadMore,
                jquery : true
            });
            var self = this;
            lib.dom.click(btn, function() {
                self._loadMoreClick();
            });
            return btn;
        },

        _loadMoreClick : function() {
            if(this._loading) return;
            this._loading = true;

            this.setLoading(true);
            this._loadMoreCallback();
        },


        /**
         * @param {model.Book[]} elements array of book elements
         * @param {boolean} hasMore if there are more items that can be loaded
         */
        addBooks : function(elements, hasMore) {
            var listSize = this.size();
            if(this._loading) {
                this.setLoading(false);
                this._loading = false;
            }

            var listItems = [];
            for(var i in elements) {
                if(elements.hasOwnProperty(i)) {
                    listItems.push(new view.ListItemBook(elements[i]));
                }
            }
            this.addElements(listItems);


            if(!hasMore) {
                this._hideFooter();
            }


        }
    });
    
    

})(JSBookSearch);

