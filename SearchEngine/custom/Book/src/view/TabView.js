(function(lib){
    var view = lib.util.extendNamespace("view");
    view.TabView = view.View.extend({
        init : function() {
            var self = this;
            this._tabs = [];
            this._tabContent = [];
            this._tabWell = this._makeTabWell();
            this._selectedIndex = -1;
            this._contentContainer = this._makeContentContainer();
            this._emptyIndicator = lib.dom.create({
                tag : 'div',
                options : {domClass : lib.constants.css.emptyIndicator},
                text : lib.constants.strings.tabLabels.empty,
                jquery : true
            });

            this._container = lib.dom.create({
                tag : 'div',
                options : {domClass : lib.constants.css.tabView},
                children : [this._emptyIndicator, this._tabWell, this._contentContainer]

            });
            this.setEmpty(true);


            this._tabWell.on("click", "li", function(event){
                var index = $(this).index();
                self.showTab(index);

            });

        },

        
        addTab : function(label, view) {
            var btn = lib.dom.create({
                tag : 'li',
                options : {domClass : lib.constants.css.tabButton},
                text : label
            });
            this._tabWell.append(btn);
            var domNode = view.getDomNode();
            lib.dom.addClass(domNode, lib.constants.css.tabContent);
            this._contentContainer.append(domNode);
            this._tabs.push(btn);
            this._tabContent.push(view);


        },

        setEmpty : function(bool) {
            if(bool) {
                this._emptyIndicator.show();
                this._tabWell.hide();
                this._contentContainer.hide();
            } else {
                this._emptyIndicator.hide();
                this._tabWell.show();
                this._contentContainer.show();
            }
        },

        showTab : function(index) {
            if(index === this._selectedIndex) return;
            if(this._selectedIndex >= 0) this._deselectTab(this._selectedIndex);
            this._selectTab(index);
            this._selectedIndex = index;

        },

        _makeTabWell : function() {
            return lib.dom.create({
                tag : 'ul',
                options : {domClass : lib.constants.css.tabWell},
                jquery : true
            });
        },

        _makeContentContainer : function() {
            return lib.dom.create({
                tag : 'div',
                options : {domClass : lib.constants.css.tabContentContainer},
                jquery : true
            });
        },

        _deselectTab : function(index) {
            var view = this._tabContent[index];
            lib.dom.removeClass(this._tabs[index], lib.constants.css.selected);
            lib.dom.removeClass(view.getDomNode(), lib.constants.css.selected);
            view.redraw();
        },

        _selectTab : function(index) {
            var view = this._tabContent[index];
            lib.dom.addClass(this._tabs[index], lib.constants.css.selected);
            lib.dom.addClass(view.getDomNode(), lib.constants.css.selected);
            view.redraw();
        },

        build : function() {
            for(var i = 0; i < this._tabContent.length; i++) {
                this._tabContent[i].build();
            }
        },

        redraw : function() {
            for(var i = 0; i < this._tabContent.length; i++) {
                this._tabContent[i].redraw();
            }
        }
    });
    

})(JSBookSearch);

