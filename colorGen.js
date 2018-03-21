// $("head").append('<script type="text/javascript" src="' + script + '"></script>');

// Generate colors (as Chroma.js objects)
console = {
    log: print,
    warn: print,
    error: print
};
// load("https://cdnjs.cloudflare.com/ajax/libs/chroma-js/1.3.4/chroma.min.js")
// load("http://tools.medialab.sciences-po.fr/iwanthue/js/libs/chroma.palette-gen.js")
load("/Users/evar/Downloads/- Java Code/- Resources/chroma.js-master/chroma.js")
var colorGenerator = function(colorNum){
    var colors = paletteGenerator.generate(
        colorNum, // Colors
        function(color){ // This function filters valid colors
            var hcl = color.hcl();
            return hcl[0]>=0 && hcl[0]<=360
                && hcl[1]>=0 && hcl[1]<=30
                && hcl[2]>=70 && hcl[2]<=100; //probably faulty?
        },
        true, // Using Force Vector instead of k-Means
        50, // Steps (quality)
        false, // Ultra precision
        'Default' // Color distance type (colorblindness)
    );
    colors = paletteGenerator.diffSort(colors, 'Default'); // Sort colors by differenciation first
    return colors
}

var happyP = function(colorNum){
    var colors = paletteGenerator.generate(
        colorNum, // Colors
        function(color){ // This function filters valid colors
            var hcl = color.hcl();
            return hcl[0]>=0 && hcl[0]<=360
                && hcl[1]>=0 && hcl[1]<=100
                && hcl[2]>=90 && hcl[2]<=100;
        },
        true, // Using Force Vector instead of k-Means
        50, // Steps (quality)
        false, // Ultra precision
        'Default' // Color distance type (colorblindness)
    );
    colors = paletteGenerator.diffSort(colors, 'Default'); // Sort colors by differenciation first
    return colors
}

var sadP = function(colorNum){
    var colors = paletteGenerator.generate(
        colorNum, // Colors
        function(color){ // This function filters valid colors
            var hcl = color.hcl();
            return hcl[0]>=0 && hcl[0]<=360
                && hcl[1]>=0 && hcl[1]<=100
                && hcl[2]>=0 && hcl[2]<=25;
        },
        true, // Using Force Vector instead of k-Means
        50, // Steps (quality)
        false, // Ultra precision
        'Default' // Color distance type (colorblindness)
    );
    colors = paletteGenerator.diffSort(colors, 'Default'); // Sort colors by differenciation first
    return colors
}

