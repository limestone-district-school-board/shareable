import pyodbc
import requests
import os

server = '10.101.201.183\TRILLIUMTEST'
database = 'TRILQA'
username = 'read_user'
password = 'testaccount$'
cnxn = pyodbc.connect('DRIVER={ODBC Driver 13 for SQL Server};SERVER='+server+';DATABASE='+database+';UID='+username+';PWD='+ password)
cursor = cnxn.cursor()

print ('Reading data from table')
tsql = "SELECT TOP 10 * FROM Persons;"
with cursor.execute(tsql):
    row = cursor.fetchone()
    while row:
        print (str(row[0]) + " " + str(row[1]))
        row = cursor.fetchone()
		
		
## now do this
## the qualtrics api key: GPibOzHNVoxwoXBQWd0ZKFYalmDBVOGkrAxFlh5I
## data center: ldsb.ca1.qualtrics.com


## get a survey completely.
apiToken = 'GPibOzHNVoxwoXBQWd0ZKFYalmDBVOGkrAxFlh5I'

dataCenter = 'ldsb'
surveyId = "SV_54sbhmDJG90uHu5";

baseUrl = "https://{0}.ca1.qualtrics.com/API/v3/survey-definitions/{1}/metadata".format(dataCenter, surveyId)
headers = {
    "x-api-token": apiToken,
    }

response = requests.get(baseUrl, headers=headers)
print(response.text)

## create a survey using demographics data

data = { 
    "SurveyName": "LDSB Demographics", 
    "isActive": True, 
    "projectCategory": "CORE"
	}

baseUrl2 = "https://{0}.ca1.qualtrics.com/API/v3/surveysAPI/v3/survey-definitions/".format(dataCenter)
headers = {
    "x-api-token": apiToken,
    }

response = requests.post(baseUrl2, json=data, headers=headers)
print(response.text)