/**
* Derek T. read full assessments file.
* \\\\10.227.4.100\\c$\\SFTP-Root\\Aspen\\reading-assessment\\ldsb-reading-assessment.csv
*
* for each row (should be ordered by student id), if the reading composite score is not there, calculate and add it.
* Then write same row into import file ldsb-reading_assessment_import.csv. Write this file directly back to sftp server.
*
*
*
*/

const fs = require('fs');

function compareScore()
{ 

}

// hashmap containing scores comparison values


// Read the CSV file with the data you want to compare
fs.readFile('\\\\10.227.4.100\\c$\\SFTP-Root\\Aspen\\reading_assessment\\export\\ldsb_reading_assessment_GR_SK.csv', 'utf-8', (err, data) => {
  if (err) throw err;
   
  	let scores_map = {};
  		  // Read the CSV file with the compare values
	fs.readFile('./Screener_Benchmarks.csv', 'utf-8', (err, data) => {
	  if (err) throw err;
	  
	  let incr = 0;

	  // Split the data by new line to get an array of rows
	  const rows = data.split('\n');
	  
	 for (const row of rows) {
	  // Get the first row and split it by comma to get an array of cells
	  const cells = row.split(',');
	  
	  // we'll need to take the heading for each column and 
	  
	  //console.log(row);
	  
	  let lookup_str = new Object();
	  let low_score = new String(cells[5]).replace(/\r/g, "");
	  let seq = cells[2];
	  let grade = cells[1];
	  let assessment_name = cells[0];
	  
	  lookup_str["NAME"] = assessment_name;
	  lookup_str["GRADE"] = grade;
	  lookup_str["SEQ"] = seq
	  lookup_str["SCORE"] = low_score;
	  
	  scores_map[incr] = lookup_str;
	  //scores_map[assessment_name] = grade;
	  
	  incr++;
	 }
	 
	 //console.log("scores_map: " + JSON.stringify(scores_map));

	});
	
	
	
	setTimeout(() => {
		console.log(scores_map);
	}, "2000");
  
  //const aspenImportAssessmentWriteStream = fs.createWriteStream('\\\\10.227.4.100\\c$\\SFTP-Root\\Aspen\\reading_assessment\\ldsb_reading_assessemnt_import.csv');
  const aspenImportAlertWriteStream = fs.createWriteStream('\\\\10.227.4.100\\c$\\SFTP-Root\\Aspen\\reading_assessment\\ldsb_alert_assessment_import.csv');

  // Split the data by new line to get an array of rows
  const rows = data.split('\n');

  // Iterate through the rows and split each row by comma to get an array of cells
  for (const row of rows) {
	  
	//if(iter === 0) {
		
	//	continue;
	//}
	
	const cells = row.split(',');
	
	/**
  <field id="asmStdOID" />
  <field id="asmGradeLevel" />
  <field id="asmFieldA001" /> <!-- BEG/MED/END -->
  <field id="asmFieldA002" />
  <field id="asmFieldA003" />
  <field id="asmFieldA004" />
  <field id="asmFieldA005" />
  <field id="asmFieldA006" />
  <field id="asmFieldA007" />
  <field id="asmFieldA008" />
  <field id="asmFieldA009" />
  <field id="asmFieldA010" />
  **/
	
	//for each row, read in values for scores
	var studentOID = cells[0];
	var gradeLevel = cells[1];
	var scoreLevel = cells[2]; // <!-- BEG/MED/END -->
	var score1 = cells[3];
	var score1 = cells[4];
	var score1 = cells[5];
	var score1 = cells[6];
	var score1 = cells[7];
	var score1 = cells[8];
	var score1 = cells[9];
	var score1 = cells[10];
	var score1 = cells[11];
	var score1 = cells[12];
	
	//for(var i = 1; i < cells.length; i++)
	//	console.log(i + " " + cells[i].toString());
	
	var inboundDestination;
	var outboundDestination;
	
	var inboundFlag = true;
	var outboundFlag = true;
	
	if((cells[2] === null || cells[2] === undefined || cells[2] === ''))
		inboundFlag = false;
	if((cells[11] === null || cells[11] === undefined || cells[11] === ''))
		outboundFlag = false;
		
	// Write Inbound import file
	/**
	if(inboundFlag) {
		InboundWriteStream.write(firstCell + ',' + '2023' + ',' + compareValue[secondCell] + ',' + 'Inbound' + ',' + convertedInboundStartDate + ','
		+ cells[2] + ',' + cells[8] + ',' +  cells[4] + ',' + cells[5] + ',' + cells[6] +
		',' + convertedInboundEndDate + ',' +  inboundTransportationMode + '\n');
	}

	if(outboundFlag) {
		OutboundWriteStream.write(firstCell + ',' + '2023' + ',' + compareValue[secondCell] + ',' + 'Outbound' + ',' + convertedOutboundStartDate + ','
		+ cells[11] + ',' + cells[17] + ',' +  cells[13] + ',' + cells[14] + ',' + cells[15] +
		',' + convertedOutboundEndDate + ',' +  outboundTransportationMode + '\n');
	}
	**/
	
	  //console.log(firstCell);
      //console.log(compareValue[secondCell]);
	  //console.log(convertedStartDate);
	  //console.log(convertedEndDate);
  }
  
  	//InboundWriteStream.end();
	//OutboundWriteStream.end();
});
