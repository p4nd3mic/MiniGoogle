(function(lib){
    var view = lib.util.extendNamespace("view");
    view.ListView = view.View.extend({
        
        init : function(options) {
            var self = this;
            this._elements = [];
            this._selectedIndex = null;
            this._loading = false;
            this._clickCallback = options.clickCallback;
            this._footer = options.footer;
            this._scrollBar = null;

            this._container = lib.dom.create({
                tag : 'div',
                options : {domClass : lib.constants.css.list},
                jquery : true
            });

            this._list = lib.dom.create({
                tag : 'ul',
                jquery : true
            });


            this._emptyIndicator = lib.dom.create({
                tag : 'li',
                options : {domClass : lib.constants.css.emptyIndicator},
                text : options.emptyLabel,
                jquery : true
            });
            this._list.append(this._emptyIndicator);
            this._container.append(this._list);

            if(this._footer) {
                this._list.append(this._footer);
                this._hideFooter();
            } 

            lib.dom.onClick(this._list, "li", function(event){
                var index = $(this).index();
                self._listItemClick(index);
            });


            if(options.scroll) {
                this._scrollBar = new view.ScrollBar(this._container);
            }
        },

        build : function() {
            if(this._scrollBar) this._scrollBar.build();
        },

        redraw : function() {
            if(this._scrollBar) this._scrollBar.redraw();
        },

        /**
         * returns the number of elements in the list, excluding footer.
         */
        size : function() {
            return this._elements.length;
        },

        /**
         * adds books to list
         * @param {view.ListItem[]} elements an array of ListItems
         * 
         */
        addElements : function(elements) {
            // switch/remove loading indicators and buttons depending on states

            if(this._elements.length === 0) {
                if(this._loading) {
                    this.setLoading(false);
                }

                if(elements.length !== 0) {
                    this._hideEmptyIndicator();
                    this._showFooter();
                }
            }

            for (var node in elements) {
                if (elements.hasOwnProperty(node)) {
                    this._elements.push(elements[node]);
                    this._emptyIndicator.before(elements[node].getDomNode());
                }  
            }
            if(this._scrollBar) this._scrollBar.redraw();
        },

        clearElements : function() {
            this._elements = [];
            this._selectedIndex = null;
            if(this._footer) {
                this._list.children().slice(0, -2).remove();
            } else {
                this._list.children().slice(0, -1).remove();
            }

            this._hideFooter();
            this._showEmptyIndicator();
            if(this._scrollBar) this._scrollBar.redraw();
        },

        setLoading : function(isLoading) {
            this._loading = isLoading;
            var func;
            if(isLoading) {
                func = lib.dom.addClass;
            } else {
                func = lib.dom.removeClass;
            }
            func(this._container, lib.constants.css.loading);

        },

        _showFooter : function() {
            if(this._footer) this._footer.css('display','block');
        },

        _hideFooter : function() {
            if(this._footer) this._footer.css('display','none');
        },

        _showEmptyIndicator : function() {
            this._emptyIndicator.css('display','block');
        },

        _hideEmptyIndicator : function() {
            this._emptyIndicator.css('display','none');
        },

        _listItemClick : function(index) {

            var numElements = this._elements.length;
            if (numElements === 0 || index >= numElements) return;


            if(this._selectedIndex !== null) {
                this._elements[this._selectedIndex].deselect();
            } 
            this._selectedIndex = index;
            this._elements[index].select();
            this._clickCallback(index);
        }
    });
    

})(JSBookSearch);