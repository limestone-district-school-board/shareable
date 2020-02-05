// this is the start of the node.js Topdesk portal

var app = require('express')();
var http = require('http').Server(app);
 
app.get('/', function(req, res){
    res.sendFile(__dirname + '/index.html');
});
 
http.listen(3001, function(){
    console.log('HTTP server started on port 3001');
});
