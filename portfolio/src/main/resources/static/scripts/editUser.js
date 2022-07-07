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

document.getElementById('croppie-test').addEventListener('update', function(ev) {
    var cropData = ev.detail;
    console.log("update")
    var imageResult = vanilla.result({
       type: "html",
       size: "original",
       format: "jpeg",
       circle: false
    });
    console.log(imageResult);
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
        let input = document.getElementById("newinputFile");
        input.value = url;
    });
});

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
