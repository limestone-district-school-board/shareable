const fs = require('fs');

let compareValue;

// Read the CSV file with the compare value
fs.readFile('/compare_value_file.csv', 'utf-8', (err, data) => {
  if (err) throw err;

  // Split the data by new line to get an array of rows
  const rows = data.split('\n');

  // Get the first row and split it by comma to get an array of cells
  const cells = rows[0].split(',');

  // Get the compare value from the first cell
  compareValue = cells[0];
});

// Read the CSV file with the data you want to compare
fs.readFile('path/to/data_file.csv', 'utf-8', (err, data) => {
  if (err) throw err;

  // Split the data by new line to get an array of rows
  const rows = data.split('\n');

  // Iterate through the rows and split each row by comma to get an array of cells
  for (const row of rows) {
    const cells = row.split(',');

    // Get the first and second cells
    const firstCell = cells[0];
    const secondCell = cells[1];

    // Check if the first cell matches the compare value
    if (firstCell === compareValue) {
      console.log(secondCell);
    }
  }
});
