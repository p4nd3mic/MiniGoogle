// require mAPIObject
(function(lib){
    var model = lib.util.extendNamespace("model");
    model.Offer = model.APIObject.extend({
        init : function(offer) {
            var propertyNames = {
                condition_id:'conditionId',
                link:null,
                merchant_id:'merchant',
                price:null,
                shipping_ground:'shippingPrice',
                total_price:'totalPrice'
            };


            this.loadProperties(offer, propertyNames);
            this.merchant = lib.app.getMerchant(this.merchant);

        }
    });

    
})(JSBookSearch);