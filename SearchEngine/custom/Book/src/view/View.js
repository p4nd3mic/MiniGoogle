(function(lib){
    var view = lib.util.extendNamespace("view");
    
    /**
     * base class for all view classes 
     * @class
     * @property {domNode} _container the top level dom node for this view
     */
    view.View = lib.Class.extend({
        /**
         * call when view object has been appended to a container
         */
        build : function(){},
        /**
         * call when dimmensions or visibility of container have changed
         */
        redraw : function(){},
        getDomNode : function () {
            if(this._container) return this._container;
            return null;
        }
    });
    
    
    

})(JSBookSearch);