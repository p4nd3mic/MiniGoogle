(function(lib){
    var view = lib.util.extendNamespace("view");
    
    view.ListItemOffer = view.ListItem.extend({
        
        /**
         * creates the dom for a list item of a book
         * @constructor
         * @param {model.Offer} offer  
         * 
         */
        init : function(offer) {
            var img = lib.dom.create({
                tag : 'div',
                options : {domClass: [lib.constants.css.listOfferItemImage, lib.constants.css.imageBox]},
                css : {
                    'background-image' : 'url("' + offer.merchant.image + '")'
                }
            });

            var price = lib.dom.create({
                tag : 'div',
                options : {domClass : [lib.constants.css.listOfferItemPrice, lib.constants.css.listTextDark]},
                text : lib.constants.strings.listOffersLabels.totalPrice + ": $" + offer.totalPrice
            });

            var shipping = lib.dom.create({
                tag : 'div',
                options : {domClass : [lib.constants.css.listOfferItemShipping, lib.constants.css.listTextMedium]},
                text : "$" + offer.price + " + $" + offer.shippingPrice + " " + lib.constants.strings.listOffersLabels.shipping
            });


            this._super([img, price, shipping]);

            lib.dom.addClass(this._container, lib.constants.css.listOfferItem);
        }
    });

    
})(JSBookSearch);