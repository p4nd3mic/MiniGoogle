// settings

var FILE_ENCODING = 'utf-8',
    EOL = '\n',
    DIST_FILE_PATH = '../dist',
    CSS_PATH = 'css';
    IMG_PATH = 'img';
    DIST_FILE_NAME = 'BookSearch',
    SRC_PATH = '../src';
    
var fs = require('fs.extra');
var path = require('path');
var rm = require('rimraf');


function build() {
    createDirectories();
    concat();
    compileCSS();
    copyImages();
    copyDistIndexFile();
}

function createDirectories() {
    if(fs.existsSync(DIST_FILE_PATH)) {
        rm.sync(DIST_FILE_PATH);
    }
    
    fs.mkdirSync(DIST_FILE_PATH);
    fs.mkdirSync(path.join(DIST_FILE_PATH, CSS_PATH));
    fs.mkdirSync(path.join(DIST_FILE_PATH, IMG_PATH));
}





function concat () {
    var coreFile = 'lib/core';
    
    var fileList = [
        'plugins/json2',
        'lib/util',
        'lib/Class',
        'lib/Constants',
        'lib/config',
        'lib/DOM',
        'lib/App',
        'lib/API',
        'model/APIObject',
        'model/Book',
        'model/BookOffers',
        'model/Merchant',
        'model/Offer',
        'view/View',
        'view/ListItem',
        'view/ListItemBook',
        'view/ListItemOffer',
        'view/ListView',
        'view/BookList',
        'view/OfferList',
        'view/KeywordInputView',
        'view/ScrollBar',
        'view/TabView',
        'view/TwoColumnView',
        'view/BookDetails',
        'controller/BookData',
        'controller/ListingOpener',
        'controller/MainPage'
    ];
    
    var coreContent = readFile(coreFile);
    var coreParts = splitCore(coreContent);
    var start = coreParts[0];
    var end = coreParts[1];
    
    fileList.map(function(fileName){
        var content = readFile(fileName);
        content = content.replace(/\(\s*?function\s*?\(\s*?lib\s*?\)\s*?{/,"");
        content = content.replace(/}\s*?\)\s*?\(\s*?JSBookSearch\s*?\)\s*?;/,"");
        start += content;
    });
    
   
    
    fs.writeFileSync(path.join(DIST_FILE_PATH, DIST_FILE_NAME + '.js'), start + end, FILE_ENCODING);
    
    var uglify = require("uglify-js"); 
    var jsp = uglify.parser;
    var pro = uglify.uglify;

    
    var ast = jsp.parse(start + end); // parse code and get the initial AST
    ast = pro.ast_mangle(ast); // get a new AST with mangled names
    ast = pro.ast_squeeze(ast); // get an AST with compression optimizations
    var final_code = pro.gen_code(ast); // compressed code here
    
    fs.writeFileSync(path.join(DIST_FILE_PATH, DIST_FILE_NAME + '.min.js'), final_code, FILE_ENCODING);
}


function readFile(fileName, extension) {
    extension = extension ? extension : '.js';
    fileName = path.join(SRC_PATH, fileName + extension);
    return fs.readFileSync(fileName).toString();
}

function splitCore(core) {
    core = core.replace(/lib/,"");
    var splitPoint = core.lastIndexOf("return");
    splitPoint = core.lastIndexOf(EOL, splitPoint);
    
    var start = core.slice(0, splitPoint);
    var end = core.slice(splitPoint);
    end = end.replace(/JSBookSearch/,"");
    return [start, end];
}




function compileCSS() {
    
    var less = require('less');
    var reset = readFile('css/reset','.less');
    var css = readFile('css/styles','.less');
  
    
    var parser = new(less.Parser);
    parser.parse(reset + css, function (err, tree) {
        if (err) { 
            return console.error(err) 
        }
        fs.writeFileSync(path.join(DIST_FILE_PATH, 'css/BookSearch.css'), tree.toCSS());
        fs.writeFileSync(path.join(DIST_FILE_PATH, 'css/BookSearch.min.css'), tree.toCSS({ compress: true }));
        
    });
    
}

function copyDistIndexFile () {
    fs.copy(path.join(SRC_PATH, 'dist.html'), path.join(DIST_FILE_PATH, 'index.html'));
    
}

function copyImages() {
    var images = fs.readdirSync(path.join(SRC_PATH, IMG_PATH));
    images.map(function(filename){
        fs.copy(path.join(SRC_PATH, IMG_PATH, filename), path.join(DIST_FILE_PATH, IMG_PATH, filename));
    });
}


build();
