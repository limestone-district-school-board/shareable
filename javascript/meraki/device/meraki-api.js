var request = require('http');
var request = require('request');
var async = require('async');

var async = require('async');

var fs = require('fs');

var csvFilename = ".\Meraki_devices.csv";

var grp1 = ["L_566327653141848496",
"L_566327653141852781",
"L_566327653141852955",
"L_566327653141847477",
"L_566327653141853002"]

var grp2 = ["N_566327653141876837",
"N_566327653141877198",
"L_566327653141852984",
"L_566327653141853000",
"L_566327653141852780"]

var grp3 = ["L_566327653141852784",
"L_566327653141852992",
"L_566327653141852994",
"N_566327653141882341",
"L_566327653141852975"]

var grp4 = ["L_566327653141852792",
"L_566327653141852790",
"L_566327653141852953",
"L_566327653141852988",
"L_566327653141852974"]

var grp5 = ["L_566327653141852957",
"L_566327653141852973",
"L_566327653141853006",
"L_566327653141852985",
"L_566327653141852979"]

var grp6 = ["N_566327653141883719",
"L_566327653141852793",
"L_566327653141853003",
"L_566327653141853005",
"L_566327653141852782"]

var grp7 = ["L_566327653141852990",
"L_566327653141852977",
"L_566327653141852783",
"N_566327653141884320",
"L_566327653141852791"]

var grp8 = ["L_566327653141852795",
"L_566327653141852644",
"N_566327653141884461",
"L_566327653141852954",
"L_566327653141853606"]

var grp9 = ["L_566327653141852982",
"L_566327653141852989",
"N_566327653141885849",
"L_566327653141852978",
"L_566327653141852983"]

var grp10 = ["L_566327653141852986",
"L_566327653141852987",
"N_566327653141885903",
"L_566327653141852999",
"L_566327653141853004"]

var grp11 = ["L_566327653141853009",
"L_566327653141852998",
"L_566327653141852777",
"L_566327653141852996",
"L_566327653141852997"]

var grp12 = ["L_566327653141852995",
"L_566327653141852785",
"L_566327653141852993",
"L_566327653141852991",
"L_566327653141852778"]

var grp13 = ["L_566327653141853007",
"L_566327653141852776",
"L_566327653141852773",
"L_566327653141852976",
"N_566327653141887149"]

var grp14 = ["L_566327653141852972",
"L_566327653141852981",
"N_566327653141887267",
"N_566327653141887271",
"L_566327653141852980"]

var grp15 = ["L_566327653141853001",
"N_566327653141904027"];

var grp01 = ["L_566327653141852972"];

var grp00 = ["N_566327653141877198", "N_566327653141887267", "N_566327653141884320","N_566327653141885849","N_566327653141885903","N_566327653141887271"];

var logger = fs.createWriteStream(csvFilename, {
    flags: 'a' // 'a' means appending (old data will be preserved)
})

function writeAllToFile(networkId) {
    // this block

    let data = [];

    //console.log(networkId);
    request({
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
            'X-Cisco-Meraki-API-Key': '5cb471edcce8482cee8a5a49793836d593a55ad0'
        },
        //uri: 'https://api.meraki.com/api/v0/organizations/658499/networks',
        uri: 'https://dashboard.meraki.com/api/v0/networks/' + networkId + '/sm/devices' +
        "?fields=ip,systemType,lastUser,publicIp,userSuppliedAddress,imei,ownerEmail,",
        //body: formData,
        method: 'GET'
    }, function (err, res, devicebody) {

        //console.log(devicebody);
        if(err) {
            console.log(err.toString());
        }
        else {
            try {
                //console.log("returned content: " + devicebody);
                var devices = JSON.parse(devicebody);
                console.log("devicebody type " + typeof devices);
                //console.log("Device: " + JSON.stringify(devices.toString()));

                if(devices.devices instanceof Array)
                    devices.devices.forEach(function(element) {
                        console.log("ELement info: " + element.id + " " + element.name);

                        if(element.systemModel.includes("iPad"))
                            logger.write(JSON.stringify(element));
                    });

            } catch (e) {
                console.log(e);
            }
        }
    });
}
// end this block

function spin(t) {
    var start = Date.now();
    while (Date.now() < start + t) {}
}

function one (callback) {

    var networkIds = [];

    request({
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
            'X-Cisco-Meraki-API-Key': '5cb471edcce8482cee8a5a49793836d593a55ad0'
        },
        uri: 'https://api.meraki.com/api/v0/organizations/658499/networks',
        //uri: 'https://api.meraki.com/api/v0/networks/L_566327653141852993/devices',
        //body: formData,
        method: 'GET'
    }, function (err, res, body) {
        var netwks = [];

        console.log("Network info: " + body);

        var networks = JSON.parse(body);

        for (var net in networks) {
            console.log("|" + networks[net].id + "|");

            networkIds.push(networks[net].id);
        }

        if (err)
            console.log(err.toString());

        //console.log("network 1: " + networkIds);

        /**spin(3000);

        callback(grp2);

        spin(3000);

        callback(grp3);

        spin(3000);

        callback(grp4);

        spin(3000);

        callback(grp5);

        spin(3000);

        callback(grp6);

        spin(3000);

        callback(grp7);

        spin(3000);

        callback(grp8);

        spin(3000);

        callback(grp9);

        spin(3000);

        callback(grp10);

        spin(3000);

        callback(grp11);

        spin(3000);

        callback(grp12);

        spin(3000);

        callback(grp13);

        spin(3000);

        callback(grp14);

        spin(3000);

        callback(grp15);**/

    });
}

function two(networkIds) {

    console.log("network: " + networkIds);
    for (var network in networkIds) {
        console.log("network id: " + network);
        writeAllToFile(networkIds[network]);
    }
}


two(grp00);
//one(two);

