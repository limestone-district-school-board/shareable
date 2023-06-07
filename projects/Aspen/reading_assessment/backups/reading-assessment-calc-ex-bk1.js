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
		
			if((cells[0] === null || cells[0] === undefined || cells[0] === ""))
				continue;

			let assessment_values = [];
			
			//for each row, read in values for scores
			var studentOID = cells[0].replace(/^"(.*)"$/, '$1'); // StudentId
			var assessmentId = cells[1].replace(/^"(.*)"$/, '$1'); // Assessment ID
			var gradeLevel = cells[2].replace(/^"(.*)"$/, '$1'); // Grade
			var scoreLevel = cells[3].replace(/^"(.*)"$/, '$1'); // <!-- BEG/MED/END -->
			
			var score1 = {name: 'First Sound Fluency', value: cells[4].length > 2 ? parseInt(cells[4].replace(/^"(.*)"$/, '$1')) : 0};   // First Sound Fluency
			assessment_values.push(score1);
			var score2 = {name: 'Phoneme Segmentation Fluency', value: cells[5].length > 2 ? parseInt(cells[5].replace(/^"(.*)"$/, '$1')) : 0};   // Phoneme Segmentation Fluency
			assessment_values.push(score2);
			var score3 = {name: 'Letter Naming Fluency', value: cells[6].length > 2 ? parseInt(cells[6].replace(/^"(.*)"$/, '$1')) : 0};   // Letter Naming Fluency
			assessment_values.push(score3);
			var score4 = {name: 'Nonsense Word Fluency - Correct Letter Sounds', value: cells[7].length > 2 ? parseInt(cells[7].replace(/^"(.*)"$/, '$1')) : 0};   // Nonsense Word Fluency - Correct Letter Sounds
			assessment_values.push(score4);
			var score5 = {name: 'Nonsense Word Fluency - Whole Words Read', value: cells[8].length > 2 ? parseInt(cells[8].replace(/^"(.*)"$/, '$1')) : 0};   // Nonsense Word Fluency - Whole Words Read
			assessment_values.push(score5);
			var score6 = {name: 'Oral Reading Fluency - Words Correct', value: cells[9].length > 2 ? parseInt(cells[9].replace(/^"(.*)"$/, '$1')) : 0};   // Oral Reading Fluency - Words Correct
			assessment_values.push(score6);
			var score7 = {name: 'Oral Reading Fluency - Accuracy', value: cells[10].length > 2 ? parseInt(cells[10].replace(/^"(.*)"$/, '$1')) : 0};  // Oral Reading Fluency - Accuracy
			assessment_values.push(score7);
			var score8 = {name: 'Retell', value: cells[11].length > 2 ? parseInt(cells[11].replace(/^"(.*)"$/, '$1')) : 0};  // Retell
			assessment_values.push(score8);
			var score9 = {name: 'Retell Quality of Response', value: cells[12].length > 2 ? parseInt(cells[12].replace(/^"(.*)"$/, '$1')) : 0};  // Retell Quality of Response
			assessment_values.push(score9);
			var score10 = {name: 'Maze Adjusted Score', value: cells[13].length > 2 ? parseInt(cells[13].replace(/^"(.*)"$/, '$1')) : 0}; // Maze Adjusted Score
			assessment_values.push(score10);
			//var score11 = {name: 'Reading Composite Score', value: cells[14].length > 2 ? parseInt(cells[14].replace(/^"(.*)"$/, '$1')) : 0}; // Reading Composite Score
			//assessment_values.push(score11);
			
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
		   
		   	for(let k in scores_map) {

				let score_item_reading = scores_map[k]; 
					
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

							const rcSKMIDScore = score1.value + score2.value + score3.value;
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

							const rc01MIDScore = score3.value + score2.value + score4.value;
							final_score = Math.floor(rc01MIDScore);
						break;
						case ((score_item_reading.GRADE === '01') && (gradeLevel === '01') && (score_item_reading.SEQ === 'END') && (scoreLevel === 'END')):

							const rc01ENDScore = score4.value + score5.value + score6.value;
							final_score = Math.floor(rc01ENDScore);

						break;
						case ((score_item_reading.GRADE === '02') && (gradeLevel === '02') && (score_item_reading.SEQ === 'BEG') && (scoreLevel === 'BEG')):
							const rc02BEGScore = score4.value + score5.value + score6.value;
							final_score = Math.floor(rc02BEGScore);
						break;
						case ((score_item_reading.GRADE === '02') && (gradeLevel === '02') && (score_item_reading.SEQ === 'MID') && (scoreLevel === 'MID')):
							rc_benchmark_score_0306 = rc_benchmark_score;
						case ((score_item_reading.GRADE === '02') && (gradeLevel === '02') && (score_item_reading.SEQ === 'END') && (scoreLevel === 'END')):
							rc_benchmark_score_0306 = rc_benchmark_score;
							
							const rc02MIDScore = score4.value + score5.value + score6.value;
							final_score = Math.floor(rc02MIDScore);
						break;
						case ((score_item_reading.GRADE === '02') && (gradeLevel === '02') && (score_item_reading.SEQ === 'END') && (scoreLevel === 'END')):
							const rc02ENDScore = score4.value + score5.value + score6.value;
							final_score = Math.floor(rc02ENDScore);
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

							var adjustedScore7 = 0;
						
							if(score7.value <= 85)
								adjustedScore7 = 85;
							else
								adjustedScore7 = score7.value;
													
							let accDiff = (adjustedScore7 - 85);
													
							accFinal = accDiff * 8;
								
							//console.log("Accuracy Points " + " " + accDiff + " " + orfPercent + " " + accFinal);

							const rc0306Score = score6.value + (score8.value * 2) + (score10.value * 4) + accFinal;
							
							final_score = rc0306Score;
							
							console.log("RC score " + " " + gradeLevel + " " + final_score);

						break;
						default:
							final_score = 0;
					}
					
					if(final_score != 0) {
						
						// write back the Reading Composite Score for students
						aspenImportAssessmentWriteStream.write(studentOID + ',' + score_item_reading.GRADE + ',' + scoreLevel + ',' + final_score + '\n');
						
						
						// Will need to have a collection that is updated with all assessment failing benchmark. Also, if none are failed, will be set to invalidate alert.
						if(final_score <= rc_benchmark_score_0306) {
							// flag as low benchmark for reading composite
							console.log(studentOID + " " + score_item_reading.NAME + " " + gradeLevel + " " + score_item_reading.SEQ + " " + student_score + " " + Screener_bench + " " + score_item_reading.GRADE + " " + final_score + " " + rc_benchmark_score_0306);
								
							aspenImportAlertWriteStream.write(studentOID + ',2,' + 'alertIcons/cl_study.png' + ',2023-08-30,N,' + 
							'Student scored below benchmark in Reading Composite Score for grade ' + score_item_reading.GRADE + ' sequence period ' + scoreLevel + '\n');
						}
						else {
							// set an alert update to remove previous alert, if it exists
							aspenImportAlertWriteStream.write(studentOID + ',2,' + 'alertIcons/cl_study.png' + ',2022-08-30,Y,' + 
							'Alert invalidated. No longer applies' + '\n');
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
			}, "2000");
  

	//OutboundWriteStream.end();
});
