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

// calculates orf accuracy points for grades 3-6
async function calculateAccuracyPoints(orfPercent) {
	var adjustedScore7 = 0;
							
	if(orfPercent <= 85)
		adjustedScore7 = 85;
	else
		adjustedScore7 = orfPercent;
							
	let accDiff = (adjustedScore7 - 85);
							
	let accFinal = accDiff * 8;
	
	//console.log("Accuracy Points " + " " + accDiff + " " + orfPercent + " " + accFinal);
	
	return accFinal;
}

function round(num, decimalPlaces = 0) {
    var p = Math.pow(10, decimalPlaces);
    var n = (num * p) * (1 + Number.EPSILON);
    return Math.round(n) / p;
}

// Read the CSV file with the data you want to compare
fs.readFile('\\\\10.227.4.100\\c$\\SFTP-Root\\Aspen\\reading_assessment\\export\\ldsb_reading_assessment.csv', 'utf-8', (err, data) => {
  if (err) throw err;
  
  function doesStudentExist()
	{ 

	   // check the student_assessment collection to see if student exists. 

	}
   
  	let scores_map = [];
  		  // Read the CSV file with the compare values
	fs.readFile('./Screener_Benchmarks.csv', 'utf-8', (err, data) => {
	  if (err) throw err;
	  
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
	  
	  scores_map.push(lookup_str);
	  //scores_map[assessment_name] = grade;
	 }
	 
	 //console.log("scores_map: " + JSON.stringify(scores_map));

	});
	
	let student_collection = [];
	
	// this is a nasty hack, but I need to get around scope timing issues to get that file in a lookup object
	setTimeout(() => {
		//console.log(scores_map);
		
		var iter = 0;
		
		// set score names, in array?
		
		let score_titles = [];
		
		score_titles[0] = 'First Sound Fluency';
		score_titles[1] = 'Phoneme Segmentation Fluency';
		score_titles[2] = 'Letter Naming Fluency';
		score_titles[3] = 'Nonsense Word Fluency - Correct Letter Sounds';
		score_titles[4] = 'Nonsense Word Fluency - Whole Words Read';
		score_titles[5] = 'Oral Reading Fluency - Words Correct';
		score_titles[6] = 'Oral Reading Fluency - Accuracy';
		score_titles[7] = 'Retell';
		score_titles[8] = 'Retell Quality of Response';
		score_titles[9] = 'Maze Adjusted Score';
		//score_titles[10] = 'Reading Composite Score';
			 
		  const aspenImportAssessmentWriteStream = fs.createWriteStream('\\\\10.227.4.100\\c$\\SFTP-Root\\Aspen\\reading_assessment\\import\\ldsb_reading_assessment_import.csv');
		  const aspenImportAlertWriteStream = fs.createWriteStream('\\\\10.227.4.100\\c$\\SFTP-Root\\Aspen\\reading_assessment\\import\\ldsb_alert_assessment_import.csv');
		  
		  // DIBELS assessment Aspen import
		  const aspenImportDibelsAssessmentWriteStream = fs.createWriteStream('\\\\10.227.4.100\\c$\\SFTP-Root\\Aspen\\reading_assessment\\import\\ldsb_dibels_ reading_assessment_import.csv');

		// Split the data by new line to get an array of rows
		const rows = data.split('\n');

		    // Iterate through the rows and split each row by comma to get an array of cells
		  for (const row of rows) {
			  
			if(iter === 0) {
				iter++;
				continue;
			}
			
			const cells = row.split(',');
			
			//if((cells[14] !== "0000000000" || cells[14] !== undefined || lells[14] !== ""))
			//	continue;
			
			//console.log("Modified: " + dateModifiedCompare
			// + " Today: " + todaysDateCompare);
		
			if((cells[0] === null || cells[0] === undefined || cells[0] === ""))
				continue;
			if((cells[1] === null || cells[1] === undefined || cells[1] === ""))
				continue;
			if((cells[3] === null || cells[3] === undefined || cells[3] === ""))
				continue;
			if((cells[4] === null || cells[4] === undefined || cells[4] === ""))
				continue;
			if((cells[17] === null || cells[17] === undefined || cells[17] === ""))
				continue;
			
			// only process row if current date matches either create date or date last updated
			var currentDateMinus1 = new Date();
			
			// create get current date stringify in format MM/DD/YYYY
			var currentDateFormatted = (currentDateMinus1.getMonth() - 1) + "/" + currentDateMinus1.getDate() + "/" + currentDateMinus1.getFullYear()
			
			currentDateMinus1.setDate(currentDateMinus1.getDate() - 1);
			
			var modifiedDate = new Date(parseInt(cells[17].replace(/\"/g, "")));

			//console.log("Today minus 3: " + currentDateMinus3.getTime()
			//+ " Modified: " + modifiedDate.getTime());
			 
			 if (modifiedDate.getTime() < currentDateMinus1.getTime()) {
				continue;
			 }

			let assessment_values = [];
			
			//for each row, read in values for scores
			var studentOID = cells[0].replace(/^"(.*)"$/, '$1'); // StudentId
			var assessmentId = cells[1].replace(/^"(.*)"$/, '$1'); // Assessment ID
			var prevGradeLevel = cells[2].replace(/^"(.*)"$/, '$1'); // Grade
			var gradeLevel = cells[3].replace(/^"(.*)"$/, '$1'); // Real Grade
			var scoreLevel = cells[4].replace(/^"(.*)"$/, '$1'); // <!-- BEG/MED/END -->
			
			console.log("Processing " + studentOID + " " + gradeLevel  + " " + gradeLevel);
			
			var score1 = {name: 'First Sound Fluency', value: cells[5].length > 2 ? parseInt(cells[5].replace(/^"(.*)"$/, '$1')) : ""};   // First Sound Fluency
			assessment_values.push(score1);
			var score2 = {name: 'Phoneme Segmentation Fluency', value: cells[6].length > 2 ? parseInt(cells[6].replace(/^"(.*)"$/, '$1')) : 0};   // Phoneme Segmentation Fluency
			assessment_values.push(score2);
			var score3 = {name: 'Letter Naming Fluency', value: cells[7].length > 2 ? parseInt(cells[7].replace(/^"(.*)"$/, '$1')) : 0};   // Letter Naming Fluency
			assessment_values.push(score3);
			var score4 = {name: 'Nonsense Word Fluency - Correct Letter Sounds', value: cells[8].length > 2 ? parseInt(cells[8].replace(/^"(.*)"$/, '$1')) : 0};   // Nonsense Word Fluency - Correct Letter Sounds
			assessment_values.push(score4);
			var score5 = {name: 'Nonsense Word Fluency - Whole Words Read', value: cells[9].length > 2 ? parseInt(cells[9].replace(/^"(.*)"$/, '$1')) : 0};   // Nonsense Word Fluency - Whole Words Read
			assessment_values.push(score5);
			var score6 = {name: 'Oral Reading Fluency - Words Correct', value: cells[10].length > 2 ? parseInt(cells[10].replace(/^"(.*)"$/, '$1')) : 0};   // Oral Reading Fluency - Words Correct
			assessment_values.push(score6);
			var score7 = {name: 'Oral Reading Fluency - Accuracy', value: cells[11].length > 2 ? parseInt(cells[11].replace(/^"(.*)"$/, '$1')) : 0};  // Oral Reading Fluency - Accuracy
			assessment_values.push(score7);
			var score8 = {name: 'Retell', value: cells[12].length > 2 ? parseInt(cells[12].replace(/^"(.*)"$/, '$1')) : 0};  // Retell
			assessment_values.push(score8);
			var score9 = {name: 'Retell Quality of Response', value: cells[13].length > 2 ? parseInt(cells[13].replace(/^"(.*)"$/, '$1')) : 0};  // Retell Quality of Response
			assessment_values.push(score9);
			var score10 = {name: 'Maze Adjusted Score', value: cells[14].length > 2 ? parseInt(cells[14].replace(/^"(.*)"$/, '$1')) : 0}; // Maze Adjusted Score
			assessment_values.push(score10);
			//var score11 = {name: 'Reading Composite Score', value: cells[14].length > 2 ? parseInt(cells[14].replace(/^"(.*)"$/, '$1')) : 0}; // Reading Composite Score
			//assessment_values.push(score11);
			
			// check type of assessment. Acadience or DIBELS for now?
			
			// assume dibels
            if (gradeLevel == '08') {

                // calculate ORF accuracy (words correct / (words correct + Errors) * 100)
                let ORFAccuracy = (score6.value / (score6.value + score7.value)) * 100;
                let MazeAdjustedScore = score10.value - (0.5 * score8.value);

                let orfWRCweight = 37.69 * score6.value;

                let orfWRCErrors = 0.03 * ORFAccuracy;
                let MAZEweight = 6.75 * MazeAdjustedScore;

                let stepTwo = orfWRCweight + orfWRCErrors + MAZEweight;

                let stepThree = stepTwo - 4824;
                let stepFour = round(stepThree / 1506, 2);
                let stepFive = round(stepFour * 40, 0);

                var scalingConstant = 0;

                switch (scoreLevel) {
                    case 'BEG':
                        scalingConstant = 360;
                        break;
                    case 'MID':
                        console.log('scorelevel ' + scoreLevel);
                        scalingConstant = 400;
                        break;
                    case 'END':
                        scalingConstant = 440;
				}


                let finalCompositeScore = Math.ceil(stepFive + scalingConstant);

                finalCompositeScore = finalCompositeScore || 0;
				ORFAccuracy = ORFAccuracy || 0;
				MazeAdjustedScore = MazeAdjustedScore || 0;

                aspenImportDibelsAssessmentWriteStream.write(studentOID + ',' + assessmentId + ',' + scoreLevel + ',' + gradeLevel + ',' + round(ORFAccuracy) + ',' + round(MazeAdjustedScore) + ','  + finalCompositeScore + '\n');
                }
                else if (gradeLevel == '07') {

                // calculate ORF accuracy (words correct / (words correct + Errors) * 100)
                let ORFAccuracy = (score6.value / (score6.value + score7.value)) * 100;
                let MazeAdjustedScore = score10.value - (0.5 * score8.value);

                let orfWRCweight = 40.55 * score6.value;

                let orfWRCErrors = 0.06 * ORFAccuracy;
                let MAZEweight = 7.34 * MazeAdjustedScore;

                let stepTwo = orfWRCweight + orfWRCErrors + MAZEweight;

                let stepThree = stepTwo - 6444;
                let stepFour = round(stepThree / 1960, 2);
                let stepFive = round(stepFour * 40, 0);

                var scalingConstant = 0;

                switch (scoreLevel) {
                    case 'BEG':
                        scalingConstant = 360;
                        break;
                    case 'MID':
                        console.log('scorelevel ' + scoreLevel);
                        scalingConstant = 400;
                        break;
                    case 'END':
                        scalingConstant = 440;
                }

                let finalCompositeScore = Math.ceil(stepFive + scalingConstant);

                finalCompositeScore = finalCompositeScore || 0;
				ORFAccuracy = ORFAccuracy || 0;
				MazeAdjustedScore = MazeAdjustedScore || 0;

                aspenImportDibelsAssessmentWriteStream.write(studentOID + ',' + assessmentId + ',' + scoreLevel + ',' + gradeLevel + ',' + round(ORFAccuracy) + ',' + round(MazeAdjustedScore) + ','  + finalCompositeScore + '\n');
            }
			else {
				for(let i in assessment_values) {
					let assessment_score_data = assessment_values[i];
					
					var score = new Object();
					
					for(let j in scores_map) {
						let score_item = scores_map[j];
						
						
						// check for all benchmark alerts
						if((score_item.NAME == assessment_score_data.name) && (score_item.GRADE == gradeLevel) && (score_item.SEQ == scoreLevel))
						{
							//let correct_score = assessment_values.find(obj => (obj.name = assessment_score_data.name));
							
							const pos = assessment_values.map(e => e.name).indexOf(score_item.NAME);
							
							let correct_score = assessment_values[pos];
							
							var student_score = parseInt(correct_score.value);
							var Screener_bench = parseInt(score_item.SCORE);
							
							//console.log("Scores " + student_score + " " + Screener_bench);
							if(student_score <= Screener_bench) {
							   //console.log(studentOID + " " + score_item.NAME + " " + student_score + " " + Screener_bench + " " + score_item.GRADE + " " + score_item.SEQ + " " + student_score);
							   
							   // flag for benchmark alert here

							   
							   // here we get the test name and all other details, update the data in the alert and reading comprehension Aspen import file
							   // check for reading composite benchmark alerts
								   
							}
						
						}	
					}
					
					//score = scores_map.find(obj => ((obj.NAME = assessment_score_data.name) && (obj.GRADE = gradeLevel) && (obj.SEQ = scoreLevel)));
					
					//console.log(assessment_score_data.name + " " + gradeLevel + " " + scoreLevel)
					
					//if(score)
					//   console.log(score.SCORE + " ");
				}
				
				/** This is where we calculate for each student the reading Composite score.
				The breakdown is score will be STUDENT/SEQ/GRADE. 
				
			   /** will need to do the calculations here. IE. switch
			   *			
				var score1 // First Sound Fluency
				var score2 // Phoneme Segmentation Fluency
				var score3 // Letter Naming Fluency
				var score4 // Nonsense Word Fluency - Correct Letter Sounds
				var score5 // Nonsense Word Fluency - Whole Words Read
				var score6 = // Oral Reading Fluency - Words Correct
				var score7 = // Oral Reading Fluency - Accuracy
				var score8 = // Retell
				var score9 = // Retell Quality of Response
				var score10 = // Maze Adjusted Score
			   *
			   **/
			   
				var final_score = 0;
				var adjustedScore = 0;
				var accDiff = 0;
			   
				for(let k in scores_map) {

					let score_item_reading = scores_map[k]; 
					
					// determine grade
					let gradePoint = "JK-1";
						
					if(score_item_reading.NAME == 'Reading Composite Score') {
						
						let raw_benchmark_score = score_item_reading.SCORE;
						var rc_benchmark_score = parseInt(raw_benchmark_score);
						var rc_benchmark_score_0306  = rc_benchmark_score;

						
						switch(true) {
							case ((score_item_reading.GRADE === 'SK') && (gradeLevel === 'SK') && (score_item_reading.SEQ === 'BEG') && (scoreLevel === 'BEG')):
								//var FSFScore = parseInt(score1.value);
								var LNFScore = parseInt(score3.value);
								final_score = score1.value + LNFScore;
							break;
							case ((score_item_reading.GRADE === 'SK') && (gradeLevel === 'SK') && (score_item_reading.SEQ === 'MID') && (scoreLevel === 'MID')):

								const rcSKMIDScore = score1.value + score2.value + score3.value + score4.value;
								final_score = Math.floor(rcSKMIDScore);

							break;
							case ((score_item_reading.GRADE === 'SK') && (gradeLevel === 'SK') && (score_item_reading.SEQ === 'END') && (scoreLevel === 'END')):

								const rcSKENDScore = score3.value + score2.value + score4.value;
								final_score = Math.floor(rcSKENDScore);
							break;
							case ((score_item_reading.GRADE === '01') && (gradeLevel === '01') && (score_item_reading.SEQ === 'BEG') && (scoreLevel === 'BEG')):

								const rc01BEGScore = score3.value + score2.value + score4.value;
								final_score = Math.floor(rc01BEGScore);
							break;
							case ((score_item_reading.GRADE === '01') && (gradeLevel === '01') && (score_item_reading.SEQ === 'MID') && (scoreLevel === 'MID')):

								//const rc01MIDScore = score3.value + score2.value + score4.value;
								//final_score = Math.floor(rc01MIDScore);
								rc_benchmark_score_0306 = rc_benchmark_score;
								
								let rc01MIDScore = 0;
								var adjustedScore01MID = score7.value;
								
								if(adjustedScore01MID < 50)
									accDiff = 0;
								else if(adjustedScore01MID < 53)
									accDiff = 2;
								else if(adjustedScore01MID < 56)
									accDiff = 8;
								else if(adjustedScore01MID < 59)
									accDiff = 14;
								else if(adjustedScore01MID < 62)
									accDiff = 20;
								else if(adjustedScore01MID < 65)
									accDiff = 26;
								else if(adjustedScore01MID < 68)
									accDiff = 32;
								else if(adjustedScore01MID < 71)
									accDiff = 38;
								else if(adjustedScore01MID < 74)
									accDiff = 44;
								else if(adjustedScore01MID < 77)
									accDiff = 50;
								else if(adjustedScore01MID < 80)
									accDiff = 56;
								else if(adjustedScore01MID < 83)
									accDiff = 62;
								else if(adjustedScore01MID < 86)
									accDiff = 68;
								else if(adjustedScore01MID < 89)
									accDiff = 74;
								else if(adjustedScore01MID < 92)
									accDiff = 80;
								else if(adjustedScore01MID < 95)
									accDiff = 86;
								else if(adjustedScore01MID < 98)
									accDiff = 92;
								else if(adjustedScore01MID <= 100)
									accDiff = 98;

								rc01MidScore = score4.value + score5.value + score6.value + accDiff;
								
								final_score = rc01MidScore;
							break;
							case ((score_item_reading.GRADE === '01') && (gradeLevel === '01') && (score_item_reading.SEQ === 'END') && (scoreLevel === 'END')):

								//const rc01ENDScore = score4.value + score5.value + score6.value;
								//final_score = Math.floor(rc01ENDScore);

							case ((score_item_reading.GRADE === '02') && (gradeLevel === '02') && (score_item_reading.SEQ === 'BEG') && (scoreLevel === 'BEG')):
								//const rc02BEGScore = score4.value + score5.value + score6.value;
								//final_score = Math.floor(rc02BEGScore);
								
								
								rc_benchmark_score_0306 = rc_benchmark_score;
								
								let rc02BEGScore = 0;
								
								//adjustedScore = score7.value;
								let adjustedScore = score7.value;
								
								/**if(score7.value <= 64)
									adjustedScore7 = 64;
								else
									adjustedScore7 = 
														
								accDiff = (adjustedScore7 - 64);
														
								accFinal = accDiff * 6;**/
								
								if(adjustedScore < 65)
									accDiff = 0;
								else if(adjustedScore < 67)
									accDiff = 3;
								else if(adjustedScore < 69)
									accDiff = 9;
								else if(adjustedScore < 71)
									accDiff = 15;
								else if(adjustedScore < 73)
									accDiff = 21;
								else if(adjustedScore < 75)
									accDiff = 27;
								else if(adjustedScore < 77)
									accDiff = 33;
								else if(adjustedScore < 79)
									accDiff = 39;
								else if(adjustedScore < 81)
									accDiff = 45;
								else if(adjustedScore < 83)
									accDiff = 51;
								else if(adjustedScore < 85)
									accDiff = 57;
								else if(adjustedScore < 87)
									accDiff = 63;
								else if(adjustedScore < 89)
									accDiff = 69;
								else if(adjustedScore < 91)
									accDiff = 75;
								else if(adjustedScore < 93)
									accDiff = 81;
								else if(adjustedScore < 95)
									accDiff = 87;
								else if(adjustedScore < 97)
									accDiff = 93;
								else if(adjustedScore < 99)
									accDiff = 99;
								else if(adjustedScore <= 100)
									accDiff = 105;
									
								//console.log("Accuracy Points " + " " + accDiff + " " + orfPercent + " " + accFinal);

								rc02BEGScore = (score5.value * 2) + (score6.value) + accDiff;
								
								final_score = rc02BEGScore;
								
								//console.log(studentOID + " " + score_item_reading.GRADE + " " + score_item_reading.SEQ + " " + final_score);
							break;
							case ((score_item_reading.GRADE === '02') && (gradeLevel === '02') && (score_item_reading.SEQ === 'MID') && (scoreLevel === 'MID')):

							case ((score_item_reading.GRADE === '02') && (gradeLevel === '02') && (score_item_reading.SEQ === 'END') && (scoreLevel === 'END')):

								rc_benchmark_score_0306 = rc_benchmark_score;
								
								let rc02MIDScore = 0;
								
								/**adjustedScore7 = 0;
								
								if(score7.value <= 85)
									adjustedScore7 = 85;
								else
									adjustedScore7 = score7.value;
														
								accDiff = (adjustedScore7 - 85);
														
								accFinal = accDiff * 8;**/
								
								let adjustedScore02MID = score7.value;
								
								if(adjustedScore02MID < 85)
									accDiff = 0;
								else if(adjustedScore02MID = 86)
									accDiff = 8;
								else if(adjustedScore02MID = 87)
									accDiff = 16;
								else if(adjustedScore02MID = 88)
									accDiff = 24;
								else if(adjustedScore02MID = 89)
									accDiff = 32;
								else if(adjustedScore02MID = 90)
									accDiff = 40;
								else if(adjustedScore02MID = 91)
									accDiff = 48;
								else if(adjustedScore02MID = 92)
									accDiff = 56;
								else if(adjustedScore02MID = 93)
									accDiff = 64;
								else if(adjustedScore02MID = 94)
									accDiff = 72;
								else if(adjustedScore02MID = 95)
									accDiff = 80;
								else if(adjustedScore02MID = 96)
									accDiff = 88;
								else if(adjustedScore02MID = 97)
									accDiff = 96;
								else if(adjustedScore02MID = 98)
									accDiff = 104;
								else if(adjustedScore02MID = 99)
									accDiff = 112;
								else if(adjustedScore02MID = 100)
									accDiff = 105;
								
								rc02MIDScore = score6.value + (score8.value * 2) + accDiff;
								
								final_score = rc02MIDScore;

							break;
							case ((score_item_reading.GRADE === '03') && (gradeLevel === '03') && (score_item_reading.SEQ === 'BEG') && (scoreLevel === 'BEG')):
								rc_benchmark_score_0306 = rc_benchmark_score;
							case ((score_item_reading.GRADE === '03') && (gradeLevel === '03') && (score_item_reading.SEQ === 'MID') && (scoreLevel === 'MID')):
								rc_benchmark_score_0306 = rc_benchmark_score;
							case ((score_item_reading.GRADE === '03') && (gradeLevel === '03') && (score_item_reading.SEQ === 'END') && (scoreLevel === 'END')):
								rc_benchmark_score_0306 = rc_benchmark_score;
							case ((score_item_reading.GRADE === '04') && (gradeLevel === '04') && (score_item_reading.SEQ === 'BEG') && (scoreLevel === 'BEG')):
								rc_benchmark_score_0306 = rc_benchmark_score;
							case ((score_item_reading.GRADE === '04') && (gradeLevel === '04') && (score_item_reading.SEQ === 'MID') && (scoreLevel === 'MID')):
								rc_benchmark_score_0306 = rc_benchmark_score;
							case ((score_item_reading.GRADE === '04') && (gradeLevel === '04') && (score_item_reading.SEQ === 'END') && (scoreLevel === 'END')):
								rc_benchmark_score_0306 = rc_benchmark_score;
							case ((score_item_reading.GRADE === '05') && (gradeLevel === '05') && (score_item_reading.SEQ === 'BEG') && (scoreLevel === 'BEG')):
								rc_benchmark_score_0306 = rc_benchmark_score;
							case ((score_item_reading.GRADE === '05') && (gradeLevel === '05') && (score_item_reading.SEQ === 'MID') && (scoreLevel === 'MID')):
								rc_benchmark_score_0306 = rc_benchmark_score;
							case ((score_item_reading.GRADE === '05') && (gradeLevel === '05') && (score_item_reading.SEQ === 'END') && (scoreLevel === 'END')):
								rc_benchmark_score_0306 = rc_benchmark_score;
							case ((score_item_reading.GRADE === '06') && (gradeLevel === '06') && (score_item_reading.SEQ === 'BEG') && (scoreLevel === 'BEG')):
								rc_benchmark_score_0306 = rc_benchmark_score;
							case ((score_item_reading.GRADE === '06') && (gradeLevel === '06') && (score_item_reading.SEQ === 'MID') && (scoreLevel === 'MID')):
								rc_benchmark_score_0306 = rc_benchmark_score;
							case ((score_item_reading.GRADE === '06') && (gradeLevel === '06') && (score_item_reading.SEQ === 'END') && (scoreLevel === 'END')):
								rc_benchmark_score_0306 = rc_benchmark_score;

								// here is where we have to look up the accuracy value based on the percent given
								//accFinal = calculateAccuracyPoints(score7.value);

							// place this in function for 1, 2, 3+
								adjustedScore7 = 0;
								
								let adjustedScore02END = score7.value;
								
								if(adjustedScore02END < 85)
									accDiff = 0;
								else if(adjustedScore02END = 86)
									accDiff = 8;
								else if(adjustedScore02END = 87)
									accDiff = 16;
								else if(adjustedScore02END = 88)
									accDiff = 24;
								else if(adjustedScore02END = 89)
									accDiff = 32;
								else if(adjustedScore02END = 90)
									accDiff = 40;
								else if(adjustedScore02END = 91)
									accDiff = 48;
								else if(adjustedScore02END = 92)
									accDiff = 56;
								else if(adjustedScore02END = 93)
									accDiff = 64;
								else if(adjustedScore02END = 94)
									accDiff = 72;
								else if(adjustedScore02END = 95)
									accDiff = 80;
								else if(adjustedScore02END = 96)
									accDiff = 88;
								else if(adjustedScore02END = 97)
									accDiff = 96;
								else if(adjustedScore02END = 98)
									accDiff = 104;
								else if(adjustedScore02END = 99)
									accDiff = 112;
								else if(adjustedScore02END = 100)
									accDiff = 105;
								
								const rc0306Score = score6.value + (score8.value * 2) + (score10.value * 4) + accDiff;
								
								//console.log(studentOID + " " + gradeLevel + " " + score_item_reading.SEQ + " " + score6.value + " " + score8.value + " " + accDiff + " " + score10.value + " " + rc0306Score);
							
								final_score = rc0306Score;
								
								//console.log("RC score " + " " + gradeLevel + " " + final_score);

							break;
							default:
								final_score = 0;
						}
						
						if(final_score != 0) {
							
							//console.log(studentOID + " " + score_item_reading.NAME + " " + gradeLevel + " " + score_item_reading.SEQ + " " + student_score + " " + Screener_bench + " " + score_item_reading.GRADE + " " + final_score);
							
							// write back the Reading Composite Score for students
							aspenImportAssessmentWriteStream.write(studentOID + ',' + assessmentId + ',' + scoreLevel + ',' + gradeLevel + ',' + final_score + '\n');
							
							/** I thought we will need to have a collection that is updated with all assessment failing benchmark. Also, if none are failed, will be set to invalidate alert.
							* On second thought, maybe not. If there are only one assessment for each grade/seq per student, then new scores will be checked, and alert validated or invalidated. There should be 
							* no assessments that remain validated that have been retaken. They will auto update to be validated if passed. For old assessment that have an alert, if no retest has occurred, the alert will stay.
							* So, the below method of validating an alert if below benchmark, other wise invalidate should be good.
							*
							**/
							
							if(final_score <= rc_benchmark_score_0306) {
								// flag as low benchmark for reading composite
		
								aspenImportAlertWriteStream.write(studentOID + ',2,' + 'alertIcons/cl_study.png' + ',' + currentDateFormatted + ',0,' + 
								'Student scored below benchmark in Reading Composite Score for grade ' + score_item_reading.GRADE + ' sequence period ' + scoreLevel + '\n');
							}
							else {
								// set an alert update to remove previous alert, if it exists
								aspenImportAlertWriteStream.write(studentOID + ',2,' + 'alertIcons/cl_study.png' + ',' + currentDateFormatted + ',1,' + 
								'Alert invalidated. No longer applies' + '\n');
							}
						}
					}
				}
			}
			
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
				
			aspenImportAssessmentWriteStream.end();
			aspenImportDibelsAssessmentWriteStream.end();
			aspenImportAlertWriteStream.end();
			}, "2000");
  

	//OutboundWriteStream.end();
});
