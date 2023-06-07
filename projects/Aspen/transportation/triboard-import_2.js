const fs = require('fs');

var compareValue = {};

function convertDate(dateString) 
{ 
	const dateParts = dateString.split('/'); 
	const month = dateParts[0]; 
	const day = dateParts[1]; 
	const year = dateParts[2]; 
	const date = new Date(year, month - 1, day); 
	const yearString = date.getFullYear().toString(); 
	let monthString = (date.getMonth() + 1).toString(); 
	let dayString = date.getDate().toString(); 
	if (monthString.length < 2) 
		{ monthString = '0' + monthString; } 
	if (dayString.length < 2) 
		{ dayString = '0' + dayString; } 
	return yearString + '-' + monthString + '-' + dayString; 
}


// Read the CSV file with the compare value
fs.readFile('./schools1.csv', 'utf-8', (err, data) => {
  if (err) throw err;

  // Split the data by new line to get an array of rows
  const rows = data.split('\n');

 for (const row of rows) {
  // Get the first row and split it by comma to get an array of cells
  const cells = row.split(',');
  
  let schoolName = new String(cells[1]);

  // Get the compare value from the first cell
  compareValue[cells[0]] = schoolName.trim();
  
  //console.log(compareValue[cells[0]]);
 }
});

// Read the CSV file with the data you want to compare
fs.readFile('./CustomStudentExportLDSB_Jan12.csv', 'utf-8', (err, data) => {
  if (err) throw err;
  
  const InboundWriteStream = fs.createWriteStream('triboard_import_file_inbound.csv');
  const OutboundWriteStream = fs.createWriteStream('triboard_import_file_outbound.csv');

  // Split the data by new line to get an array of rows
  const rows = data.split('\n');
  let iter = 0;

  // Iterate through the rows and split each row by comma to get an array of cells
  for (const row of rows) {
	  
	if(iter === 0) {
		iter++;
		continue;
	}
	  
    const cells = row.split(',');

	var convertedOutboundStartDate;
	var convertedOutboundEndDate;
	
	var convertedInboundStartDate;
	var convertedInboundEndDate;
	
	if(cells[3] === null && cells[3] === undefined && cells[3] === '')
		continue;

    // Get the first and second cells
    let firstCell = cells[0];
    const secondCell = cells[1];
	
	//Convert Outbound dates
	if(cells[8] !== null && cells[8] !== undefined && cells[8] !== '')
		convertedOutboundStartDate = convertDate(cells[8]);
	else
		convertedOutboundStartDate = '';
	
	if(cells[9] !== null && cells[9] !== undefined && cells[9] !== '')
		convertedOutboundEndDate = convertDate(cells[9]);
	else
		convertedOutboundEndDate = '';
	
	// Convert Inbound dates
	if(cells[8] !== null && cells[8] !== undefined && cells[16] !== '')
		convertedInboundStartDate = convertDate(cells[16]);
	else
		convertedInboundStartDate = '';
	
	if(cells[9] !== null && cells[9] !== undefined && cells[17] !== '')
		convertedInboundEndDate = convertDate(cells[17]);
	else
		convertedInboundEndDate = '';

    // Add a dash every 3 characters in the first cell
    firstCell = firstCell.replace(/(\d{3})/g, '$1-');

    // Remove the trailing dash if it exists
    firstCell = firstCell.replace(/-$/, '');

	// Write Inbound import file
	InboundWriteStream.write(firstCell + ',' + '2023' + ',' + compareValue[secondCell] + ',' + 'Inbound' + ',' 
	+ cells[7] + ',' +  cells[2] + ',' +  cells[3] + ',' + cells[4] + ',' + cells[6] + ',' +
	convertedOutboundStartDate + ',' + convertedOutboundEndDate + ',' +  cells[18] + '\n');
	
	OutboundWriteStream.write(firstCell + ',' + '2023' + ',' + compareValue[secondCell] + ',' + 'Outbound' + ','
	+ cells[15] + ',' +  cells[9] + ',' +  cells[10] + ',' + cells[11] + ',' + cells[13] + ',' +
	convertedInboundStartDate + ',' + convertedInboundEndDate + ',' +  cells[18] + '\n');
	
	  //console.log(firstCell);
      //console.log(compareValue[secondCell]);
	  //console.log(convertedStartDate);
	  //console.log(convertedEndDate);
  }
  
  	InboundWriteStream.end();
	OutboundWriteStream.end();
});
