// this is the start of the node.js Topdesk portal

var app = require('express')();
var http = require('http').Server(app);
var io = require('socket.io')(http);
var path = require('path');
var data = require('./fetch_topdesk_data');

//var app = express();

//app.use(express.static(path.join(__dirname, 'public')));
// Global variable to store the latest NHL results
var latestData = new Object();

// Load the NHL data for when client's first connect
// This will be updated every 10 minutes
 
/**io.on('connection', function(socket) {

    try {	
		//data.getDataAllOpen(sendToPageAllOpened);
		
		data.getDataOpen(sendToPageOpened);
		
		//data.getDataClosed(sendToPageClosed);
	
		socket.on('receivedFromClient', function (data) {
			console.log(data);
		});
	} catch (e) {
		console.log("App crashed while managing connection " + e.message);
	}
});**/

function getAllData() {
	
	    try {	
		//data.getDataAllOpen(sendToPageAllOpened);
		
		var one = data.getDataOpen(sendToPageOpened);
		var two = data.getDataClosed(sendToPageClosed);
		var three = data.getDataAllOpen(sendToPageAllOpened);
		
		setTimeout(() => {
		data.formatFinalJSON(latestData);
		//console.log(latestData);
		}, 12000);
	
	} catch (e) {
		console.log("App crashed while managing connection " + e.message);
	}
}

getAllData();
 
/**app.get('/', function(req, res){
    res.sendFile(__dirname + '/index.html');
});
 
http.listen(3000, function(){
    console.log('HTTP server started on port 3000');
});**/

function sendToPageAllOpened(allopenedData) {

	console.log('Get all current opened');
	latestData.allopened = allopenedData
}

async function sendToPageOpened(openedData) {

	console.log('Get opened since last week');
	latestData.opened = openedData
}

function sendToPageClosed(closedData) {

	console.log('Get closed since last week');
	latestData.closed = closedData;
	//io.emit('sendToClient', data);
	
	//console.log("What tickets details looks like " + JSON.stringify(latestData));
}


