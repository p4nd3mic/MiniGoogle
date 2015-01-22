(function(lib){
    var app = lib.util.extendNamespace("app");

    var merchants = {},
        useStorage = window.localStorage === undefined ? false : true;
    if(useStorage) {
        var tmp = JSON.parse(localStorage.getItem('merchants'));
        merchants = (tmp === null) ? merchants : tmp;
    }

    app.mobile = false;

    app.addMerchants = function(merchantsXHR) {
        if(merchantsXHR.status) {
            var locMerchants = merchantsXHR.data;
            var modified = false;
            for(var i = 0; i < locMerchants.length; i++) {
                var temp = locMerchants[i];
                if(merchants[temp.id] === undefined) {
                    merchants[temp.id] = temp;
                    modified = true;
                }
            }

            if(useStorage && modified) {
                localStorage.setItem('merchants', JSON.stringify(merchants));
            }
        }
    };
    
    app.getMerchant = function(id) {
        return merchants[id];
    };

})(JSBookSearch);