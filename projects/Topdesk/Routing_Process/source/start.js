// this is the start of the node.js Topdesk portal

var app = require('express')();
var http = require('http').Server(app);
//var io = require('socket.io')(http);
var path = require('path');
const fs = require('fs');
var data = require('./ldsb_td_process');

//var app = express();

//app.use(express.static(path.join(__dirname, 'public')));
// Global variable to store the latest NHL results
var latestData = new Object();

// This will be updated every 30 minutes?

function startProcess () {

	var currentdate = new Date();

	var datetime = currentdate.getDate() + "/"
		+ (currentdate.getMonth()+1)  + "/"
		+ currentdate.getFullYear() + " @ "
		+ currentdate.getHours() + ":"

	console.log('Topdesk Maintenance Process for date: ', + currentdate.getDate() + "/" + currentdate.getMonth()+1  + "/" + currentdate.getFullYear() + ": "+ currentdate.getHours());

    try {

		fs.appendFile("topdesk_process.log", "Topdesk Maintenance Process for date: " + currentdate.getDate() + "/" + currentdate.getMonth()  + "/" + currentdate.getFullYear() + ": "+ currentdate.getHours() + "\n", (err) => {});

		//data.getDataClosed();
		data.routeNewTickets();

		//For getting TD objects and testing
		//data.getTicketsToInspect();

	} catch (e) {
		console.log("App crashed while managing connection " + e.message);
	}
};

function sendToPageAllOpened(allopenedData) {
	console.log('Get all current opened');
	latestData.allopened = allopenedData
}

function sendToPageOpened(openedData) {

	console.log('Get opened since last week');
	latestData.opened = openedData
}

function sendToPageClosed(closedData) {

	console.log('Get closed since last week');
	latestData.closed = closedData;
	//io.emit('sendToClient', data);

	//console.log("What tickets details looks like " + JSON.stringify(latestData));

	//io.emit('sendToClient', latestData);
}

startProcess();