/*
 Function to copy the URL to the clipboard
*/
function copyURL() {
    // Select the email link anchor text
    var urlLabel = document.getElementById('url_label');
    var range = document.createRange();
    range.selectNode(urlLabel);
    window.getSelection().addRange(range);

    try {
        // Now that we've selected the anchor text, execute the copy command
        var successful = document.execCommand('copy');
        var msg = successful ? 'successful' : 'unsuccessful';
        console.log('Copy URL command was ' + msg);
        document.getElementById("copy_notice").innerHTML = "<button class='submit'>Copied!</button>";
    } catch(err) {
        console.log('Oops, unable to copy');
    }
    // Remove the selections - NOTE: Should use
    // removeRange(range) when it is supported
    window.getSelection().removeAllRanges();
}