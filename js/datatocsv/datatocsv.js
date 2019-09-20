var util = require('util');
var http = require('https');

var GoogleSpreadsheet = require('google-spreadsheet');
var async = require('async');

const topdesk_config = require('../../config/topdesk_config.json');
const sql = require('mssql');

var flattened_result = [];

async function beforeRender() {

    console.log("Start");
    sql.close();
    await sql.connect(topdesk_config);
    const sqlReq = new sql.Request();

    //const recordset = await sqlReq.query('select count(*) AS Count, ' +
    //'preferred_first_name AS Name from Persons group by preferred_first_name');

    //const recordset = await sqlReq.query('select ' +
    //"ref_vestiging AS School, naam AS Ticket, korteomschrijving AS Description from incident where " +
    //"ref_status = 'In progress' group by " +
    //"ref_vestiging, naam, korteomschrijving");

    /** for incident tickets **/
    const recordset = await sqlReq.query('select ' +
        "ref_operatorgroup AS Region, " +
        "ref_vestiging AS School, " +
        "naam AS Ticket, " +
        "korteomschrijving AS Description, " +
        "ref_status AS Status, " +
        "ref_soortmelding AS Type, " +
        "ref_operatordynanaam AS Assigned, " +
        //"datumaangemeld, " +
        "convert(VARCHAR, datumaangemeld, 110) AS Date " +
        "from incident " +
        "where ref_status <> 'closed'" +
        //"where ref_status in ('In progress', 'New', 'On-site visit required', " +
        //"'Waiting for user', 'Updated by user') " +
        "AND ref_operatorgroup in ('North Team', 'Central Team', 'West Team')" +
        "group by " +
        "ref_operatorgroup, datumaangemeld, ref_vestiging, ref_status, naam, korteomschrijving, " +
        "ref_soortmelding, ref_operatordynanaam order by ref_operatorgroup, datumaangemeld desc");

    var printArray = function(arr) {

        var resultArr = [];
        if ( arr instanceof Array) {
            for (var i = 0; i < arr.length; i++) {
                printArray(arr[i]);
            }
        }
        else flattened_result.push(arr);
    }

    printArray(recordset.recordset);

    console.log("Msg 1: ", flattened_result);

    sql.close();
}

beforeRender();

var doc = new GoogleSpreadsheet('1qXMEgJj_1sJUZ2-_NpBT5XFPDPRZj7D73Ij9CHQekPY');
var sheet;

async.series([
    function setAuth(step) {
        // see notes below for authentication instructions!
        var creds = require('./ldsbsheets-c1eb87a5ba10.json');
        // OR, if you cannot save the file locally (like on heroku)
        var creds_json = {
            client_email: 'traised@ldsbsheets.iam.gserviceaccount.com',
            private_key: 'MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQDFT7+PeiHyMAak\\naVSCPIsdR6XIrjfi4SbmOOfSEN9wwwkLvXYQLPlgUNA4d6dZ5uyVfMTU4vZGgNol\\nFiI4dQoV+qZnY6ZsASWEdpQcLg+ycUWCq7cl6sa10BWr2aiWI19AS6HyxjzCCV62\\ndQ/vhdDgxF8neMX8mbTVmxpqgN/ME4mou01nmwIvJ1Nm1/Nuo9lJOMSKL3TjcQpB\\nw9P6fFTrKfinCAbz0df+IyPsO/lrs2K+ObwZOSaDOnq1YCMr3Q9h/3tgifRtNtxP\\nxJgjSXHsBmb49JyPwtXczM1XQe96LcUNiDV0Y9u79JwXvEyAQEh4wX1GBQs1Arao\\n1JfqOCCrAgMBAAECggEASdaxw9I7UORrH+zw/LoGf4MYBfU4odWLVp2WF3VMAOlk\\nAbo5rA0Al+w2N+ODQ7AY+kpSaK605iaDPFy/mqwmBZqi2k8e8Q0uypcLj04nfqpI\\nDoIPTIfDLDf+XCzcm68KDsgfB3jrNNwHzzUYrRTBWdPzHhTi1sh4tXVRWej3l2lS\\nIiAjGBaHYqmhJR/fyN0F2zyfSgE4Mz7V5OWUYBs1DhuLD2n856GUeiNnmMFAMQOn\\nE4bEaKb3DL4y2EMT5VDHhBcXWwCpg6nfALgYU85PUBxa9JorYT29TtLQAFbvsrGg\\nZPvWX7Fw3ABaX+jQgesI4u0WTOItvZEz2uByQ/qjuQKBgQDjBs2CQd4p3IRMgpPV\\nX44QjJiRLlCeWtdupATYCIaTaHBX/g+VeG/da1wN7GnzcC7w3mc+gJgz9Tnj9/6B\\nTIQpf4paRfGU24DrWH3w64pO7RmEStcjF8kIcmhrarVmSsh9OWyhS4d7hpoqrBhb\\ntXzdkvjs/AW0WP7VkQEPlJUr6QKBgQDefh+SFHNscAp0epHMf4wfvF10LaEoNP2H\\nzrTPM0KNeoO2hJUsaWXkV9W6aIwwDka6gDdIH2GApDDuml9sShyX8c5AIVU0MLCL\\nBBJ0+D6zHZ3qS5FNHOGQiN1suyljmJvC6zsfzOcpvjCGXfWSn0XCprd1WpLIFuxa\\nQgTsmpTPcwKBgCjoU+WRutduLcSfjsXW5wFiZCdc4hf2pHUCRwEqYB11pzCjzekP\\n/5A1RSwZGmpMH5k1agMgJszN9jV2vTxNLk9P9P0ZN1OsTcmac28S8iQQoxVsauGe\\n6pGEE/6vBqq89O1jiIhz1KDUezkPM4Bq7mn6jrIdI2YA10JiU3+cNVjpAoGAR4D1\\nGHdmGOVxFroFfVlIaTKaPioj7s4I5MpzpVtt+hXzmobFgj5lNptb3sqWsyMDbBCk\\ns5MSPMuHxkbqf9zZj6Nwi3+q6Fj1g9fNLrZRQDM2ewPKMKQlgdPahYUo6g8zj9MV\\nb0Tkza+H3Mb5kyOIwePedeKj5ZYoY2FwP7H2PS0CgYAy6MvyGHC/ndMHV5A3Fbg0\\nzQonaI5gpE3ckyWXayLqJ0u+7Y6qIvST/sFE0bNNhSp7S+940HQ6sh5pqvJdt/kD\\no09JrFy5BZfs3qLcwuXiOTnN3pNgZjtBHi8VU5X67fMWzxXarXOf1I3VcXQJz3E9\\nFOI4Ii4EGyLrKZPc8YSNaw'
        }

        doc.useServiceAccountAuth(creds, step);
    },
    function addNewWorksheet(step) {

        var headings = [];
        // get headings
        var firstRow = flattened_result[0];

        console.log(firstRow);

        for (var column_headings in firstRow) {
            headings.push(column_headings);
            //console.log(column_headings);
        }

        var newsheet_options = {
            "title": "New Worksheet from API",
            "rowCount": 500,
            "colCount": 500,
            "headers": headings
        };

        doc.addWorksheet(newsheet_options, function(err, info) {
            if(err)
                console.log(err);
            else
                console.log(info);

            //console.log(info.worksheets.getRows(1, function(err, rows) {
            //    console.log(rows);
            //}))
            //console.log('Loaded doc: '+info.title+' by '+info.author.email);
            //sheet = info.worksheets[0];
            //console.log('sheet 1: '+sheet.title+' '+sheet.rowCount+'x'+sheet.colCount);
            step();
        });
    },
    function addDataToSheet(step) {
    // this will eventually count the rows and columns in the recordset

        var counter = 0;

        function doSetTimeout(i) {
            setTimeout(function() { console.log("tick: "); }, 1000);
        }

        //get all data
        for(var newrow in flattened_result) {

            doc.addRow(3, flattened_result[newrow], function(err, info) {
                console.log(err);
                //console.log(info);
            //console.log(flattened_result.recordset[newrow]);
            });

            //doSetTimeout(newrow);

            counter++;
        }
    }

    /**function getInfoAndWorksheets(step) {
        doc.getInfo(function(err, info) {
            if(err)
                console.log(err);
            else
                console.log(info);

            //console.log(info.worksheets.getRows(1, function(err, rows) {
            //    console.log(rows);
            //}))
            //console.log('Loaded doc: '+info.title+' by '+info.author.email);
            //sheet = info.worksheets[0];
            //console.log('sheet 1: '+sheet.title+' '+sheet.rowCount+'x'+sheet.colCount);
            step();
        });
    } **/
], function(err){
        if( err ) {
            console.log(err);
        }
});

    /**
     *
     * {
     *"spreadsheetId": string,
     *"properties": {
     *  object(SpreadsheetProperties)
     *},
     *"sheets": [
     *  {
     *    object(Sheet)
     *  }
     *],
     *"namedRanges": [
     *  {
     *    object(NamedRange)
     *  }
     *],
     *"spreadsheetUrl": string,
     *"developerMetadata": [
     *  {
     *    object(DeveloperMetadata)
     *  }
     *]
     *}
     */
