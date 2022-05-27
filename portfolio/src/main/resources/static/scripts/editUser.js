function getFileNameWithExt(event) {

    var outputFile = document.getElementById("outputFile");
    var extension = document.getElementById("extension");

    if (!event || !event.target || !event.target.files || event.target.files.length === 0) {
        extension.value = "ext";
        return;
    }


    const name = event.target.files[0].name;
    const lastDot = name.lastIndexOf('.');

    const fileName = name.substring(0, lastDot);
    const ext = name.substring(lastDot + 1);

    outputFile.value = fileName;
    extension.value = ext;

}


function myFunction() {
    alert("Hello\nHow are you?");
}