(function(lib){
    var view = lib.util.extendNamespace("view");
    
    view.OfferList = view.ListView.extend({
        init : function(selectedCallback, offerType) {
            var callback = function(index) {
                selectedCallback(offerType, index);
            };
            this._super({
                clickCallback: callback,
                emptyLabel : lib.constants.strings.listOffersLabels.emptyList,
                scroll : true
            });
            lib.dom.addClass(this._container, lib.constants.css.listBooks);
        },

        /**
         * @param {model.Offer[]} offers array of offer objects
         */
        addOffers : function(offers) {
            if(this._loading) {
                this.setLoading(false);
            }

            var listItems = [];
            for(var i in offers) {
                if(offers.hasOwnProperty(i)) {
                    listItems.push(new view.ListItemOffer(offers[i]));
                }
            }
            this.addElements(listItems);
        }
    });

})(JSBookSearch);

