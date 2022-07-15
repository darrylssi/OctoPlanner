import "./croppie.js";

var input = document.getElementById("inputFile");
var photoFormSubmitButton = document.getElementById('photo-upload-submit-button');

// this creates the image cropping widget on the page
// docs are here https://foliotek.github.io/Croppie/
var croppieWindow = document.getElementById('croppieWindow');
var croppieObject = new Croppie(croppieWindow, {
    viewport: { width: 200, height: 200, type: 'circle'},
    boundary: { width: 300, height: 300 },
    showZoomer: true,
    enableOrientation: true
});

function hidePhotoObjects() {
    photoFormSubmitButton.style.display = "none";
    croppieWindow.style.display = "none";
}
function showPhotoObjects() {
    photoFormSubmitButton.style.display = "block";
    croppieWindow.style.display = "block";
}
hidePhotoObjects(); // hidden by default

// Called when the input file is changed
// a bit of code is from https://stackoverflow.com/questions/5697605/limit-the-size-of-a-file-upload-html-input-element
// some other code is from https://stackoverflow.com/questions/32222786/file-upload-check-if-valid-image
document.querySelector('#inputFile').addEventListener('change',function() {
    var context = this;
    if (context.files[0].size > 31457280) {
        context.value = "";
        hidePhotoObjects();
        alert("File is too big! Max size is 30MB.");
    } else {
        if (input.files && input.files[0]) {
            var image = new Image();
            image.onload = function() {
                // valid image
                showPhotoObjects();
                readURL();
            };
            image.onerror = function() {
               context.value = "";
               hidePhotoObjects();
               alert('That image is invalid or corrupted.');
            };
            image.src = URL.createObjectURL(input.files[0]);
        }
    }
});

// converts the photo to a base64 string, shoves it into a hidden input, then submits the form
document.getElementById('photoForm').addEventListener('submit', function(ev) {
    ev.preventDefault();
    var context = this;

    croppieObject.result({
        type: 'blob',
       format: "jpeg",
        circle: false
    }).then(function (blob) {
        let url = URL.createObjectURL(blob);
        var reader = new FileReader();
        reader.readAsDataURL(blob);
        reader.onloadend = function () {
            var base64String = reader.result;
            let input = document.getElementById("imageString");
            input.value = base64String;
            context.submit();
        }
    });
});


// This will take the provided input image and show it in the croppie window
// Note that the croppie window must be visible when bind() is called!
function readURL() {
    if (input.files && input.files[0]) {
        var reader = new FileReader();
        reader.onload = function (e) {
            console.log("changed");
            croppieObject.bind({
            url: e.target.result
            });
        }
        reader.readAsDataURL(input.files[0]);
    }
}
