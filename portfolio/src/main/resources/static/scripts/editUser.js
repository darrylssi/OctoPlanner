// for image compression
const MAX_WIDTH = 3000;
const MAX_HEIGHT = 3000;
const QUALITY = .9;

const input = document.getElementById("inputFile");
const photoFormSubmitButton = document.getElementById('photo-upload-submit-button');
const croppieLoader = document.getElementById("croppieLoader");
const errorSpan = document.getElementById("invalidPhotoJS");

// this creates the image cropping widget on the page
// docs are here https://foliotek.github.io/Croppie/
const croppieWindow = document.getElementById('croppieWindow');
const croppieObject = new Croppie(croppieWindow, {
    viewport: {width: 200, height: 200, type: 'circle'},
    boundary: {width: 300, height: 300},
    showZoomer: true,
    enableOrientation: true
});

function showCroppieLoader() {
    croppieLoader.style.display = "block";
}
function hideCroppieLoader() {
    croppieLoader.style.display = "none";
}
hideCroppieLoader(); // hidden by default
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
    const context = this;
    // this image is only used for validation, and isn't displayed anywhere
    if (input.files && input.files[0]) {
        errorSpan.textContent="";
        showCroppieLoader();
        const image = new Image();
        image.onload = function() {
            // valid image
            hidePhotoObjects();
            readURL();
        };
        image.onerror = function() {
           context.value = "";
           hidePhotoObjects();
           hideCroppieLoader();
           errorSpan.textContent="That image file is invalid or corrupted.";

        };
        image.src = URL.createObjectURL(input.files[0]);
    }
});

// converts the photo to a base64 string, shoves it into a hidden input, then submits the form
document.getElementById('photoForm').addEventListener('submit', function(ev) {
    ev.preventDefault();
    const context = this;

    croppieObject.result({
        type: 'blob',
       format: "jpeg",
        circle: false
    }).then(function (blob) {
        const reader = new FileReader();
        reader.readAsDataURL(blob);
        reader.onloadend = function () {
            const base64String = reader.result;
            let stringInput = document.getElementById("imageString");
            stringInput.value = base64String;
            context.submit();
        }
    });
});


// This will take the provided input image and show it in the croppie window
// This function (as well as calculateSize()) copied and modified from https://stackoverflow.com/a/68956880
// Answer by Eyni Kave, licensed under https://creativecommons.org/licenses/by-sa/4.0/
function readURL() {
    if (input.files && input.files[0]) {
        const file = input.files[0];
        const blobURL = URL.createObjectURL(file);
        const img = new Image();
        img.src = blobURL;
        img.onload = function () {
            URL.revokeObjectURL(this.src)
            const [newWidth, newHeight] = calculateSize(img, MAX_WIDTH, MAX_HEIGHT);
            const canvas = document.createElement("canvas");
            canvas.width = newWidth;
            canvas.height = newHeight;
            const canvasContext = canvas.getContext("2d");
            canvasContext.drawImage(img, 0, 0, newWidth, newHeight);
            showPhotoObjects();
            bindUrlToCroppie(canvas.toDataURL("image/jpeg", QUALITY))
            hideCroppieLoader();
        }
    }
}

// Changes the largest dimension of the image to match the max, and scales the other dimension proportionally
// This effectively calculates a width and height to resize the image
function calculateSize(img, maxWidth, maxHeight) {
    let width = img.width;
    let height = img.height;

    // calculate the width and height, constraining the proportions
    if (width > height) {
        if (width > maxWidth) {
            height = Math.round((height * maxWidth) / width);
            width = maxWidth;
        }
    } else {
        if (height > maxHeight) {
            width = Math.round((width * maxHeight) / height);
            height = maxHeight;
        }
    }
    return [width, height];
}

// Binds the specified data URL to the croppie instance
// Note that the croppie window must be visible when bind() is called!
function bindUrlToCroppie(url) {
    croppieObject.bind({
        url: url
    });
}