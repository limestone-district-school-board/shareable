/** Attach to topdesk API using app : LDSB Topdesk Processing
 * Live API password: xydgl-34vso-p6dus-s2mje-kuarq
 * Test API password: 7zgfz-ubkol-peu6m-cjdvv-3ngwh
 *
 * @type {{Agent: Agent, ServerOptions: tls.SecureContextOptions & tls.TlsOptions & http.ServerOptions, AgentOptions: AgentOptions, RequestOptions: http.RequestOptions & tls.SecureContextOptions & {rejectUnauthorized?: boolean | undefined, servername?: string | undefined}, globalAgent: Agent, Server: Server, createServer: (requestListener?: http.RequestListener) => Server, get: (options: (RequestOptions | string | URL), callback?: (res: http.IncomingMessage) => void) => http.ClientRequest, request: (options: (RequestOptions | string | URL), callback?: (res: http.IncomingMessage) => void) => http.ClientRequest}}
 */

var https = require('https');
var exp = require('express')();
var moment = require('moment');
const fs = require('fs');

let ITConfig = require('./Topdesk_Info.json');

//var update = require('./update_sheets');

var url = 'limestone.topdesk.net';
var host = 'limestone.topdesk.net/tas/api/incidents';
var username = 'prv-traised';
var password = 'ees46-2k4xc-o5iuj-m4dxx-jw4ew';
var apiKeyRaw = 'prv-traised:ees46-2k4xc-o5iuj-m4dxx-jw4ew';

//setup variables
let apiKey = Buffer.from(apiKeyRaw, 'utf-8').toString('base64');

let today = moment();
var lastWeek = moment();
today.format('YYYY-MM-DD');
lastWeek.format('YYYY-MM-DD');
lastWeek.subtract('7', 'days');

// now to create the filters
var closedTrue = "&closed=true";
var closedFalse = "&closed=false";

var parameterStringClosedTickets;
var parameterStringOpenTickets;
var parameterStringAllOpenTickets;
var paging = 0;
var ticketCount1 = 0;
var ticketCount2 = 0;

var response = '';

var openTicketsTotal = [];

var ticketsClosedDetails = new Object();

// run a loop here to get through the paging

function sleep(milliseconds) {
    const date = Date.now();
    let currentDate = null;
    do {
        currentDate = Date.now();
    } while (currentDate - date < milliseconds);
}


const optionsClosed = {
    "method": "GET",
    "hostname": url,
    "port": 443,
    "path": "/tas/api/incidents?page_size=800" + closedTrue,
    "headers": {
        "content-type": "application/json",
        "authorization": "BASIC " + apiKey
    }
}

const optionsOpen = {
    "method": "GET",
    "hostname": url,
    "port": 443,
    "agent": https.Agent({keepAlive:true}),
    "path": "/tas/api/incidents?page_size=800" + closedFalse,
    "headers": {
        "content-type": "application/json",
        "authorization": "BASIC " + apiKey
    }
}

exports.routeNewTickets = function() {
    //open tickets

    try {
        const req = https.request(optionsOpen, function(res) {
        var ticketsOpenDetails = new Object();
        var tripped = false;

        var chunks = [];

        res.on("data", function (chunk) {
            chunks.push(chunk);
        });

        res.on("end", function () {

            try {
                var body = JSON.parse(Buffer.concat(chunks).toString());
            } catch (e) {
                console.log(e.message);
                tripped = true;
            }

            if (!tripped) {
                // here run through a loop to sort out all incident results

                for (let prop in body) {
                    var currentCall = body[prop];
                    var status = body[prop].processingStatus;
                    //console.log(status.name);
                    if(status.name == 'Waiting for user: Auto Close') {
                        updateWaitingTickets(currentCall);
                    }
                    if (status.name == 'New') {

                        // check if cat or sub-cat is Projectors?

                        /**if (currentCall.subcategory && currentCall.subcategory.name == "Projector") {

                            let formattedOperatorGroup = '{"operatorGroup": ' + JSON.stringify(ITConfig.Teams.projectorInstallation) + '}';

                            //updateTicket(currentCall, formattedOperatorGroup);

                            //sleep(2000);

                            let formattedCallStatus = '{"processingStatus": ' + JSON.stringify(ITConfig.Statuses.inprogress) + '}';
                            // patch the ticket with in progress status
                            //updateTicket(currentCall, formattedCallStatus);

                            //sleep(2000);

                            let formattedOperatorInfo = '{"operator": ' + JSON.stringify(ITConfig.Teams.projectorInstallation) + '}';

                            // patch the ticket in TD
                            //updateTicket(currentCall, formattedOperatorInfo);

                            continue;
                        }**/

                        //console.log("Operators " + JSON.stringify(currentCall.operator));
                        // compare locations to IT Info object
                        for(var i = 0; i < ITConfig.Routes.length; i++) {
                            if (currentCall.callerBranch) {
                                if (ITConfig.Routes[i].Locations.includes(currentCall.callerBranch.name)) {
                                    // found the team, now get the correct operator, based on sub cat
                                    if(currentCall.subcategory) {
                                        if ((ITConfig.Routes[i].AOR.includes(currentCall.subcategory.name)) ||
                                            (ITConfig.Routes[i].AOR.includes(currentCall.category.name))) {

                                            ticketCount1++;
                                            fs.appendFile("topdesk_process.log", "New Routing update: " + ticketCount1 + ": " + JSON.stringify(currentCall.number) + "\n", (err) => {});

                                            let formattedCallInfo = '{"operator":' + JSON.stringify(ITConfig.Routes[i].Operators[0]) + ',';
                                            // patch the ticket, assign operator. set timeout to keep from smashing the api server

                                            formattedCallInfo += '"operatorGroup": ' + JSON.stringify(ITConfig.Teams.itTeam) + ',';
                                            // patch the ticket, assign operator group

                                            formattedCallInfo += '"processingStatus": ' + JSON.stringify(ITConfig.Statuses.assigned) + '}';
                                                // patch the ticket with in progress status
                                            updateTicket(currentCall, formattedCallInfo);

                                            //setTimeout(function() {
                                            //    console.log("Updating " + JSON.stringify(currentCall.number));
                                            //}, 1000);
                                        }
                                    }
                                }
                                //else
                                //    console.log("Didn't find the location " + currentCall.callerBranch.name)
                            }

                            // Go in to json object and find this location and accociated operator
                        };
                    }
                }
            }

        });
    });

    req.end();

    } catch (ex) {
        console.log("Exception called");
        req.end();
        fs.close();
        console.log(ex.message);
    }
};

function updateWaitingTickets(currentTicket) {
    var today = moment();
    var holdDate = moment(currentTicket.onHoldDate);
    var lastWeek = moment();
    var ticketCaller = "Newer";

    let closedObj = JSON.stringify(ITConfig.Statuses.closed);

    today.format('YYYY-MM-DD');
    lastWeek.format('YYYY-MM-DD');
    lastWeek.subtract('6', 'days');

    if(lastWeek.isAfter(holdDate)) {

        ticketCount2++;
        ticketCaller = JSON.stringify(currentTicket.processingStatus);
        //console.log("Wait period closure: " + ticketCount2 + ": " + currentTicket.number);
		fs.appendFile("topdesk_process.log", "Wait period close ticket: " + ticketCount2 + ": " + currentTicket.number + "\n", (err) => {});

        let formattedCallInfo = '{"processingStatus":' + closedObj + '}';
        updateTicket(currentTicket, formattedCallInfo);

        //sleep(1000);
    }
}

// send patched ticket to TD
//returns the operator id given an operator name
function updateTicket(currentTicket, formattedCallInfo) {
    // create operator update
    let ticketId = JSON.stringify(currentTicket.id);

    if(!currentTicket.operator.id)
        return;

    const optionsUpdate = {
        "method": "PATCH",
        "hostname": url,
        "port": 443,
        "path": "/tas/api/incidents/id/" + ticketId.split('"').join(''),
        "headers": {
            "content-type": "application/json",
            "authorization": "BASIC " + apiKey,
            "Content-Length": formattedCallInfo.length
        }
    }

    const req = https.request(optionsUpdate, res => {

        let data = '';
        //console.log('Status: ', res.statusCode)
        //console.log('Headers: ', JSON.stringify(res.headers))
        res.setEncoding('utf8');
        res.on('data', chunk => {
            data += chunk;
        });
        res.on('end', () => {
            //console.log('Body: ', JSON.parse(data));
        })
    }).on('error', e => {
        console.error(e);
    })

    req.write(formattedCallInfo);
    req.end();
}


//returns the operator id given an operator name
function getAllOperators(operatorname) {
    //

}

// The below functions are for testing and inspecting ticket contents

// THis will get tickets based on certain criteria. Usually to see internal id's and such
exports.getTicketsToInspect = function() {

    const req = https.request(optionsOpen, function(res) {

        var ticketsClosedDetails = new Object();

        var tripped = false;

        var chunks = [];

        res.on("data", function (chunk) {
            chunks.push(chunk);
        });

        res.on("end", function() {
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
                for( let prop in body) {
                    let thisTicket = body[prop];

                    //console.log("status: " + JSON.stringify(body[prop].processingStatus));
                    //console.log("team: " + JSON.stringify(body[prop].operatorGroup));
                    console.log("operator: " + JSON.stringify(body[prop].operator));
                    //console.log("operator: " + JSON.stringify(body[prop].operator));
                }

            }
        });
    });

    req.end();
};

exports.getDataClosed = function(sendToPage) {

    //closed tickets
    const req = https.request(optionsClosed, function(res) {

        var ticketsClosedDetails = new Object();

        var tripped = false;

        var chunks = [];

        res.on("data", function (chunk) {
            chunks.push(chunk);
        });

        res.on("end", function() {
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

                for( let prop in body) {
                        console.log("Closed " + JSON.stringify(body[prop].processingStatus));
                }

            }
        });
    });

    req.end();
};