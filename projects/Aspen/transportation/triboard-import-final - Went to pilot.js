const fs = require('fs');

var compareValue = {};

function convertDate(dateString) 
{ 

	const dateParts = dateString.slice(0, 10).split('-'); 
	console.log(dateParts[2]);
	const month = dateParts[1]; 
	const day = dateParts[2]; 
	const year = dateParts[0]; 
	const date = new Date(year, month - 1, day); 
	const yearString = date.getFullYear().toString(); 
	let monthString = (date.getMonth() + 1).toString(); 
	let dayString = date.getDate().toString(); 
	//if (monthString.length < 2) 
	//	{ monthString = '0' + monthString; } 
	//if (dayString.length < 2) 
	//	{ dayString = '0' + dayString; } 
	
	return monthString + '/' + dayString + '/' + yearString; 
}

// Read the CSV file with the compare value
fs.readFile('./schools2.csv', 'utf-8', (err, data) => {
  if (err) throw err;

  // Split the data by new line to get an array of rows
  const rows = data.split('\n');

 for (const row of rows) {
  // Get the first row and split it by comma to get an array of cells
  const cells = row.split(',');
  
  let schoolName = new String(cells[1]);

  // Get the compare value from the first cell
  compareValue[cells[0]] = schoolName.trim();
 }
});

// Read the CSV file with the data you want to compare
fs.readFile('./TriBoard to LDSB Aspen_latest.csv', 'utf-8', (err, data) => {
  if (err) throw err;
  
  const InboundWriteStream = fs.createWriteStream('triboard_import_file_inbound.csv');
  const OutboundWriteStream = fs.createWriteStream('triboard_import_file_outbound.csv');

  // Split the data by new line to get an array of rows
  const rows = data.split('\n');
  let iter = 0;

  // Iterate through the rows and split each row by comma to get an array of cells
  for (const row of rows) {
	  
	//if(iter === 0) {
		
	//	continue;
	//}
	  
    const cells = row.split(',');

	var convertedOutboundStartDate;
	var convertedOutboundEndDate;
	
	var convertedInboundStartDate;
	var convertedInboundEndDate;
	
	var inboundDestination;
	var outboundDestination;
	
	var inboundFlag = true;
	var outboundFlag = true;
	
	if((cells[2] === null || cells[2] === undefined || cells[2] === ''))
		inboundFlag = false;
	if((cells[11] === null || cells[11] === undefined || cells[11] === ''))
		outboundFlag = false;
		
    // Get the first and second cells
    let firstCell = cells[0];
    const secondCell = cells[1];
	let inboundTransportationMode = cells[3];
	let outboundTransportationMode = cells[12];


	if(cells[3] !== null && cells[3] !== undefined && cells[3] !== '')
		switch (inboundTransportationMode.trim()) {
			case 'Bussed':
				inboundTransportationMode = 'Bus';
				break;
			case 'Walker':
				inboundTransportationMode = 'Walk';
				break;
			default:
				inboundTransportationMode = cells[3].trim();
		}
		
	if(cells[12] !== null && cells[12] !== undefined && cells[12] !== '')
		switch (outboundTransportationMode.trim()) {
			case 'Bussed':
				outboundTransportationMode = 'Bus';
				break;
			case 'Walker':
				outboundTransportationMode = 'Walk';
				break;
			default:
				outboundTransportationMode = cells[12].trim();
		}
	
	if(secondCell != '483540')
		continue;
	
	//Convert inbound dates
	if(cells[9] !== null && cells[9] !== undefined && cells[9] !== '')
		convertedInboundStartDate = convertDate(cells[9]);
	else
		convertedInboundStartDate = '09/06/2022';
	
	if(cells[10] !== null && cells[10] !== undefined && cells[10] !== '')
		convertedInboundEndDate = convertDate(cells[10]);
	else
		convertedInboundEndDate = '';
	
	// Convert outbound dates
	if(cells[18] !== null && cells[18] !== undefined && cells[18] !== '')
		convertedOutboundStartDate = convertDate(cells[18]);
	else
		convertedOutboundStartDate = '09/06/2022';
	
	if(cells[19] !== null && cells[19] !== undefined && cells[19] !== '')
		convertedOutboundEndDate = convertDate(cells[19]);
	else
		convertedOutboundEndDate = '';

    // Add a dash every 3 characters in the first cell
    firstCell = firstCell.replace(/(\d{3})/g, '$1-');

    // Remove the trailing dash if it exists
    firstCell = firstCell.replace(/-$/, '');

	// Write Inbound import file
	if(inboundFlag) {
		InboundWriteStream.write(firstCell + ',' + '2023' + ',' + compareValue[secondCell] + ',' + 'Inbound' + ',' + convertedInboundStartDate + ','
		+ cells[2] + ',' +  cells[8] + ',' +  cells[4] + ',' + cells[5] + ',' + cells[6] +
		',' + convertedInboundEndDate + ',' +  inboundTransportationMode + '\n');
	}

	if(outboundFlag) {
		OutboundWriteStream.write(firstCell + ',' + '2023' + ',' + compareValue[secondCell] + ',' + 'Outbound' + ',' + convertedOutboundStartDate + ','
		+ cells[11] + ',' +  cells[17] + ',' +  cells[13] + ',' + cells[10] + ',' + cells[11] +
		',' + convertedOutboundEndDate + ',' +  outboundTransportationMode + '\n');
	}
	
	  //console.log(firstCell);
      //console.log(compareValue[secondCell]);
	  //console.log(convertedStartDate);
	  //console.log(convertedEndDate);
  }
  
  	InboundWriteStream.end();
	OutboundWriteStream.end();
});
