var BookSearch = (function(lib){
     var lib = lib ? lib : {};
     
     lib.init = function(options) {
        config('apiKey', options.apiKey);
        config('bookImageSmall', options.bookImageSmall);
        config('bookImageLarge', options.bookImageLarge);
        
        lib.api.init(lib.config.apiKey, lib.config.bookImageSmall, lib.config.bookImageLarge);
        var api = lib.api;
        
        api.merchants(addMerchants);

        new lib.controller.MainPage(options.container);  
        
        
        function config(key, val) {
            if(val) {
                lib.config[key] = val;
            }  
        }

        function addMerchants(data) {
            lib.app.addMerchants(data);
        }
    };




    return {
        init : lib.init
    };
})(JSBookSearch);