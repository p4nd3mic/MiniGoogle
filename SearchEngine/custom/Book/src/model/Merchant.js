// require mAPIObject
(function(lib){
    var model = lib.util.extendNamespace("model");
    /**
     * takes a merchant json object (refer to campusbooks documentation)
     * @param {object} merchant merchants json object
     */
    model.Merchant = model.APIObject.extend({
        init : function(merchant) {
            var propertyNames = {
                image:null,
                merchant_id:'id',
                name:null
            };
            this.loadProperties(merchant, propertyNames);

            var coupons = merchant.coupons.coupon;
            if(coupons !== undefined) {
                this.coupons = coupons;
            } else {
                this.coupons = null;
            }


        }
    });

})(JSBookSearch);