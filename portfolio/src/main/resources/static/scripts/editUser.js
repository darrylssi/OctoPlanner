import "./croppie.js";

var el = document.getElementById('croppie-test');
var vanilla = new Croppie(el, {
    viewport: { width: 100, height: 100, type: 'circle'},
    boundary: { width: 300, height: 300 },
    showZoomer: true,
    enableOrientation: true
});

document.querySelector('#inputFile').addEventListener('change',function(){
    readURL()
});
document.querySelector('#photo-upload-submit-button').addEventListener('change',function(){
    console.log("result");
    getResult();
});

//TODO tidy this up so it doesn't look like code vomit
document.getElementById('croppie-test').addEventListener('update', function(ev) {
    var cropData = ev.detail;
    console.log("update")
    var imageResult = vanilla.result({
       type: "html",
       size: "original",
       format: "jpeg",
       circle: false
    });
    console.log("imageresult" + imageResult);


    vanilla.result({
        type: 'blob',
       format: "jpeg",
        circle: false
    }).then(function (blob) {
        let url = URL.createObjectURL(blob);
        console.log(url);
        var reader = new FileReader();
        reader.readAsDataURL(blob);
        reader.onloadend = function () {
            var base64String = reader.result;
            console.log('Base64 String - ', base64String);

            // Simply Print the Base64 Encoded String,
            // without additional data: Attributes.
            // doesn't work!
            console.log('Base64 String without Tags- ', base64String.substr(base64String.indexOf(', ') + 1));
            let input = document.getElementById("imageString");
            input.value = base64String;
        }
    });

});


document.querySelector('#testlink').addEventListener('click', function (ev) {
    vanilla.result({
        type: 'blob',
       format: "jpeg",
        circle: false
    }).then(function (blob) {
        let url = URL.createObjectURL(blob);

//        const img = document.getElementById('image-preview');
//        img.src = url;
        console.log(url);
    });
});

//const blobToBase64 = blob => {
//  const reader = new FileReader();
//  reader.readAsDataURL(blob);
//  return new Promise(resolve => {
//    reader.onloadend = () => {
//      resolve(reader.result);
//    };
//  });
//};

// https://www.geeksforgeeks.org/how-to-convert-blob-to-base64-encoding-using-javascript/
function blobToBase64(blob) {
    var result;
    var reader = new FileReader();
    reader.readAsDataURL(blob);
    reader.onloadend = function () {
        var base64String = reader.result;
        console.log('Base64 String - ', base64String);

        // Simply Print the Base64 Encoded String,
        // without additional data: Attributes.
        // doesn't work!
        console.log('Base64 String without Tags- ',
        base64String.substr(base64String.indexOf(', ') + 1));
        result = base64String;
        let input = document.getElementById("imageString");
        input.value = base64String;
        return base64String;
    }
    return result;
}

function getResult() {
    var imageResult = vanilla.result({
       type: "html",
       size: "original",
       format: "jpeg",
       circle: false
    });
}

function readURL() {
    var input = document.getElementById("inputFile");
    if (input.files && input.files[0]) {
        var reader = new FileReader();
        reader.onload = function (e) {
            console.log("changed");
            vanilla.bind({
            url: e.target.result
            });
        }
        reader.readAsDataURL(input.files[0]);
    }
}
