(function(lib){
    var controller = lib.util.extendNamespace('controller');
    
    
    controller.ListingOpener = lib.Class.extend({

        openUrl : function(url) {
            var customParams = [];
            if(lib.config.preOpenOfferFn) {
                customParams = lib.config.preOpenOfferFn();
                if(customParams.length > 3) {
                    customParams = customParams.slice(0,3);
                }
            }
            for(var i = 0; i < customParams.length; i++) {
                url += '&c' + (i + 1) + encodeURI(customParams[i]); 
            }

            window.open(url);
        }
    });
    
    
})(JSBookSearch);