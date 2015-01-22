(function(lib) {
    lib.util = {};
	var util = lib.util;
    

    util.extendNamespace = function (ns_string) {  
        var parts = ns_string.split('.'),  
            parent = lib,  
            pl, i;  
        if (parts[0] === "lib") {  
            parts = parts.slice(1);  
        }  
        pl = parts.length;  
        for (i = 0; i < pl; i++) {  
            //create a property if it doesnt exist  
            if (typeof parent[parts[i]] === 'undefined') {  
                parent[parts[i]] = {};  
            }  
            parent = parent[parts[i]];  
        }  
        return parent;  
    } ;

	
	util.empty = function(val) {
		return (val === undefined || val === null || val == "");
	}

	
	util.bind = function(obj, fn) {
		return function() {
			return fn.apply(obj, arguments);
		}
	}
	
	

})(JSBookSearch);


