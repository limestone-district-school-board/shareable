
// Attach to topdesk API using app : LDSB Topdesk Reporting
var https = require('https');
var exp = require('express')();
var moment = require('moment');
const fs = require('fs');
const axios = require('axios');

var url = 'https://limestone.topdesk.net';
var host = 'https://limestone.topdesk.net/tas/api/incidents';
var username = 'prv-traised';
var password = 'y7ndw-vzs3l-oxrzv-eb6wn-ie36g';
var apiKeyRaw = 'prv-traised:y7ndw-vzs3l-oxrzv-eb6wn-ie36g';
 
 //setup variables
 let buffer = new Buffer(apiKeyRaw);
 let apiKey = buffer.toString('base64');
 
 let today = moment();
 today.subtract(5, 'hours');
 
 let yesterday = moment();
yesterday.subtract(29, 'hours');
 
 var lastWeek = moment();
 lastWeek.subtract(5, 'hours');
 
 today.format('YYYY-MM-DD');
 lastWeek.format('YYYY-MM-DD');
 lastWeek.subtract('7', 'days');
 
 todaysTicketCLosedCount = 0;
 todaysTicketOpenedCount = 0;
 wekkTicketsClosedCount = 0;
 weeksTicketOpenedCount = 0;
 
 // now to create the filters
 var closedTrue = "&closed=true";
 var closedFalse = "&closed=false";
 //var callDateLastWeek = '&call_date_start=' + lastWeek.get('year') + '-01' + '-02';
 //var closedDateLastWeek = '&closed_date_start=' + lastWeek.get('year') + '-01' + '-02';
 
 var callDateLastWeek = '&call_date_start=' + lastWeek.get('year') + '-' + (lastWeek.get('month') < 09 ? '0' : '') + (parseInt(lastWeek.get('month')) + 1) + '-' + (lastWeek.get('date') < 10 ? '0' : '') + lastWeek.get('date');
 var closedDateLastWeek = '&closed_date_start=' + lastWeek.get('year') + '-' + (lastWeek.get('month') < 09 ? '0' : '') + (parseInt(lastWeek.get('month')) + 1) + '-' + (lastWeek.get('date') < 10 ? '0' : '') + lastWeek.get('date');
 
 //console.log("Closed string " + closedDateLastWeek);
 //console.log(callDateLastWeek);
 
 var parameterStringClosedTickets = '' + closedDateLastWeek;
 var parameterStringOpenTickets = '' + callDateLastWeek;
 var parameterStringAllOpenTickets = '' + closedFalse;
 var paging = 0;
 
 var response = '';

var openTicketsTotal = [];

var ticketsClosedDetails = new Object();

// run a loop here to get through the paging

	const optionsClosed = {
		"method": "GET",
		"hostname": "limestone.topdesk.net",
		"port": 443,
		"path": "/tas/api/incidents?page_size=6000" + parameterStringClosedTickets,
		"headers": {
		"content-type": "application/json",
		"authorization": "BASIC " + apiKey
		}
	}
	
		const optionsOpen = {
		"method": "GET",
		"hostname": "limestone.topdesk.net",
		"port": 443,
		"path": "/tas/api/incidents?page_size=1800" + parameterStringOpenTickets,
		"headers": {
		"content-type": "application/json",
		"authorization": "BASIC " + apiKey
		}
	}
	
	const optionsAllOpen = {
		"method": "GET",
		"hostname": "limestone.topdesk.net",
		"port": 443,
		"path": "/tas/api/incidents?page_size=1800" + parameterStringAllOpenTickets,
		"headers": {
		"content-type": "application/json",
		"authorization": "BASIC " + apiKey
		}
	}
	
	// 
		const sendToTeams = {
		"method": "POST",
		"hostname": "https://limestoneschools.webhook.office.com",
		"port": 443,
		"path": "/webhookb2/23eb701e-70c5-4876-8de6-3bef2d8308b2@e1f4165f-ebae-4d82-a73c-a0de593830be/IncomingWebhook/688136a0ad6e430ab364fa1e41c0585d/ec3736e0-06a6-447f-941a-4aa7418de8dc",
		"headers": {
		"content-type": "application/json",
		"authorization": "BASIC " + apiKey
		}
	}
	
exports.getDataAllOpen = function(sendToPage) {

	var ticketsAllOpenDetails = new Object();
	var totalAllOpenedCount = 0;
	var tripped = false;
	
	//open tickets
	const req = https.request(optionsAllOpen, function(res) {

		var chunks = [];

		res.on("data", function (chunk) {
			chunks.push(chunk);
			//console.log("Data: " + chunk);
		});

		res.on("end", function() {
			var today = moment();
			
			try {
			var body = JSON.parse(Buffer.concat(chunks).toString());
			} catch (e) {
				console.log(e.message);
				console.log(JSON.stringify(body));
				tripped = true;
			}

			if(!tripped)
			{
				// counts
				var totalOpenedCount = 0;
				var thisWeeksOpenedCount = 0;
				var todaysOpenedCount = 0;
				
				for( let prop in  body)
				{
					totalAllOpenedCount++;
				}
				
				ticketsAllOpenDetails.totalAllOpenedCount = totalAllOpenedCount;
				
				//console.log("What tickets details looks like " + JSON.stringify(ticketsDetails));
				
				sendToPage(ticketsAllOpenDetails);
			}
		});
	});
	
	req.end()
	
	return ticketsAllOpenDetails;
	
};

exports.getDataOpen = function(sendToPage) {

	//open tickets
	const req = https.request(optionsOpen, function(res) {
	
	    console.log(JSON.stringify(optionsOpen));
		var ticketsOpenDetails = new Object();
		var tripped = false;

		var chunks = [];

		res.on("data", function (chunk) {
			chunks.push(chunk);
		});

		res.on("end", function() {
			var today = moment();

			try {
				var body = JSON.parse(Buffer.concat(chunks).toString());
				}
				catch(e) {
					console.log(e.message);
					tripped = true;
				}
				
				//console.log("Body: " + JSON.stringify(body));

				if(!tripped)
				{
					// here run through a loop to sort out all incident results
					// for now we just need 4 values
					// this week opened tickets
					// this weeks tickets closed
					// this todays opened ticket
					// today tickets closed
					
					// counts
					var thisWeeksOpenedCount = 0;
					var todaysOpenedCount = 0;
					
					for( let prop in  body)
					{
					
						var callDate = moment(body[prop].callDate);
						var closed = body[prop].closed;
						
						//console.log(body[prop]);
						
						// tickets open/close dates
						// ticketsDetails.callDate = body[prop].callDate;
						// ticketsDetails.status = body[prop].processingStatus.name;
						
						//console.log("call date: " + callDate.month() + callDate.date() + " todays date: " + today.month() + today.date() + " last week " + lastWeek.month() + lastWeek.date());
						
						//getToday's tickets
						//if((callDate.getDate() == today.getDate()) && (callDate.getMonth() == today.getMonth()) && (callDate.getFullYear() == today.getFullYear()))
						if(callDate.isAfter(yesterday))
						{
							//console.log(body[prop]);
							todaysOpenedCount++;
						}
							
						//if((parseInt(lastWeek.getDate()) <= parseInt(callDate.getDate())) && (parseInt(lastWeek.getMonth())+1 <= parseInt(callDate.getMonth())+1 &&
						//	(parseInt(lastWeek.getFullYear()) <= parseInt(callDate.getFullYear()))))
						if(lastWeek.isBefore(callDate) || lastWeek.isSame(callDate, 'day'))
							thisWeeksOpenedCount++;
					}
				
				ticketsOpenDetails.thisWeeksOpenedCount = thisWeeksOpenedCount;
				ticketsOpenDetails.todaysOpenedCount = todaysOpenedCount;
				
				sendToPage(ticketsOpenDetails);
				
				console.log("What tickets details looks like " + JSON.stringify(ticketsOpenDetails));
			}
		});
	});
	
	req.end();
};

exports.getDataClosed = function(sendToPage) {

	//open tickets
	const req = https.request(optionsClosed, function(res) {
	
		var ticketsClosedDetails = new Object();

		var tripped = false;

		var chunks = [];

		res.on("data", function (chunk) {
			chunks.push(chunk);
		});

		res.on("end", function() {
			var today = moment();
			try {
				var body = JSON.parse(Buffer.concat(chunks).toString());
				}
			catch (e)
			{
				console.log(e.message);
				tripped = true;
			}

			if(!tripped)
			{
				// here run through a loop to sort out all incident results
				// for now we just need 4 values
				// this week opened tickets
				// this weeks tickets closed
				// this todays opened ticket
				// today tickets closed
				
				// counts
				var thisWeeksClosedCount = 0;
				var todaysClosedCount = 0;
				
				for( let prop in body)
				{
				
					var closedDate = moment(body[prop].closedDate);
					var closed = body[prop].closed;
					
					// tickets open/close dates
					// ticketsDetails.callDate = body[prop].callDate;
					// ticketsDetails.status = body[prop].processingStatus.name;
					
					//console.log("week date: " + parseInt(lastWeek.getDate()) + parseInt(lastWeek.getMonth()) + parseInt(lastWeek.getFullYear()) +
					//" todays date: " + parseInt(closedDate.getDate()) + parseInt(closedDate.getMonth()) + parseInt(closedDate.getFullYear()));
					
					//getToday's tickets
					//if((closedDate.getDate() == today.getDate()) && (closedDate.getMonth() == today.getMonth()) && (closedDate.getFullYear() == today.getFullYear()))
					if(closedDate.isAfter(yesterday))
						todaysClosedCount++;
						
					//if((parseInt(lastWeek.getDate()) <= parseInt(closedDate.getDate())) && (parseInt(lastWeek.getMonth())+1 <= parseInt(closedDate.getMonth())+1 &&
					//	(parseInt(lastWeek.getFullYear()) <= parseInt(closedDate.getFullYear()))))
					if(lastWeek.isBefore(closedDate) || lastWeek.isSame(closedDate, 'day'))
						{
						thisWeeksClosedCount++;
						//console.log("Closed " + JSON.stringify(body[prop].closedDate + " " + JSON.stringify(body[prop].number)));
						}
				}
			
			ticketsClosedDetails.thisWeeksClosedCount = thisWeeksClosedCount;
			ticketsClosedDetails.todaysClosedCount = todaysClosedCount;
			
			sendToPage(ticketsClosedDetails);
			
			//console.log("What closed tickets details looks like " + JSON.stringify(ticketsClosedDetails));
			
		}
		});
	});
	
	req.end()
	
};

exports.formatFinalJSON = function(ticketData) {
	
	let opened = ticketData.opened;
	let closed = ticketData.closed;
	let allopened = ticketData.allopened;
	
	console.log("Look into opened data " + JSON.stringify(opened));
	console.log("Look into opened data " + JSON.stringify(closed));
	
	// test group
	//const webhookURL = "https://limestoneschools.webhook.office.com/webhookb2/23eb701e-70c5-4876-8de6-3bef2d8308b2@e1f4165f-ebae-4d82-a73c-a0de593830be/IncomingWebhook/688136a0ad6e430ab364fa1e41c0585d/ec3736e0-06a6-447f-941a-4aa7418de8dc";
	
	// its group
	const webhookURL = "https://limestoneschools.webhook.office.com/webhookb2/91459428-9d47-4d2a-ba05-12fbdd17e71b@e1f4165f-ebae-4d82-a73c-a0de593830be/IncomingWebhook/95226cbe591c4e5cb0e3ca6ae0c7a372/ec3736e0-06a6-447f-941a-4aa7418de8dc";
	
	let jsonTemplateRaw = fs.readFileSync('.\\teamsupdate.json');
	let jsonTemplate = JSON.parse(jsonTemplateRaw);
	
	jsonTemplate.text = "<table style='width: 100%'><tr><td style='width: 30%'><h2> Total tickets currently open:</td><td style='width: 10%'> " + allopened.totalAllOpenedCount + " </h2></td><td style='width: 30%'></td><td style='width: 30%'></td></tr>" + 
	"<tr><td style='width: 30%'>Tickets opened in past 24 hours: </td><td style='width: 10%'>" + opened.todaysOpenedCount + "</td><td style='width: 30%'> Tickets closed in past 24 hours: </td><td style='width: 30%'>" + closed.todaysClosedCount + "</td></tr>" +
	"<tr><td style='width: 30%'>Tickets opened in past 7 days: </td><td style='width: 10%'>" + opened.thisWeeksOpenedCount + "</td><td style='width: 30%'>Tickets closed in past 7 days: </td><td style='width: 30%'>" + closed.thisWeeksClosedCount + "</td></tr></table>" ;
	
	//console.log(jsonTemplate);
	
	 try {
      const response = axios.post(webhookURL, jsonTemplate, {
        headers: {
          'content-type': 'application/vnd.microsoft.teams.card.o365connector'
        },
      });
      return `${response.status} - ${response.statusText}`;
    } catch (err) {
      return err;
    }

}