function copyURL() {
    var copyText = document.getElementById("url_label");
    copyText.select();
    document.execCommand("Copy");
    var copyNotice = document.getElementById("copy_notice");
    copyNotice.innerHTML = "Copied!";
}