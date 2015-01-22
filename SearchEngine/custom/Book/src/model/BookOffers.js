// require lib.constants
(function(lib){
    var model = lib.util.extendNamespace("model");
    model.BookOffers = lib.Class.extend({
        init : function (offerList) {
    
            this.types = [[],[],[],[]];

            for(var i = 0; i < offerList.length; i++) {
                var offers = offerList[i];
                var type = this.getOfferCondition(offers['@attributes'].id);
                if(type !== undefined) {
                    for(var y = 0; y < offers.offer.length; y++) {
                        this.types[type].push(new model.Offer(offers.offer[y]));
                    }
                    //this.types[type] = this.types[type].concat(offers.offer);
                }

            }

            for(i = 0; i < this.types.length; i++) {
                this.types[i].sort(compare);
            }

            function compare(a, b) {
                a = a.total_price;
                b = b.total_price;
                return a > b ? 1 : (a < b ? -1 : 0);
            }

            return this;
        },

        getOfferCondition : function (condition) {
            
            var cons = lib.constants.Condition;

            if(condition == 1) return cons.NEW;
            if(condition == 0 || condition == 2 || condition == 4 || condition == 7) return cons.USED;
            if(condition == 5) return cons.EBOOK;
            if(condition == 6) return cons.RENTAL;
            return cons.USED;

        }
    });
})(JSBookSearch);