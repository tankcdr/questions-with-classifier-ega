# Copyright IBM Corp. 2015
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# this script is for adding metadata for all answers in the answer store in your Box repository
#
# Note: This script requires the non-standard Requests module http://docs.python-requests.org/en/latest/user/install/
#
# Author: Stephan J Roorda

import argparse
import csv
import urllib2
import json

def run(answers_file, token):	
	# read the data from the csv file
	classes = []
	questions = []
	read_data(answers_file, classes, questions)
	counter = 0
	
	# for each class we search for its file, if found update its metadata
	for index, className in enumerate(classes):
		print "Processing metadata for: " + className
		fileId = search_for_answer(className, token)
		if fileId :
			exists = check_for_metadata(fileId, token)
			if not exists:
				add_metadata(fileId, className, questions[index], token)
			else:
				update_metadata(fileId, className, questions[index], token)
		else:
			print "  NO ADD/UPDATE"
			counter += 1
	
	print counter, "classes NOT updated"
	
def read_data(answers_file, classes, questions):
	# first, read in the answers file to get the class --> canonical mapping
	with open(answers_file) as answers:
		data = csv.reader(answers)
		for row in data:
			classes.append(row[0])
			questions.append(row[1])
	
def search_for_answer(className, token):
	result = ''
	
	# url for finding the answer file for the class
	# add quotes around the class name for the query so it does an exact search on that phrase
	url = "https://api.box.com/2.0/search?query=\"" + className + "\"&content_types=name"
	print "  check for answer: " + url
		
	# authorization token for accessing the Box API
	header = {'Authorization':'Bearer %s' % token}
	
	# build and execute the request
	request = urllib2.Request(url, data=None, headers=header)
	response = urllib2.urlopen(request)
	json_response = json.loads(response.read())
	
	# we should get 1 and only 1 result, if anything else skip
	if json_response['total_count'] == 1:
		result = json_response['entries'][0]['id']
	else:
		print "  found entries:",json_response['total_count']
		print json.dumps(json_response)
		
	return result
	
def check_for_metadata(fileId, token):
	result = False
	
	# url for checking all metadata
	url = "https://api.box.com/2.0/files/" + fileId + "/metadata"
	print "  check for metadata " + url
	
	# authorization token for accessing the Box API
	header = {'Authorization':'Bearer %s' % token}
		
	# build and execute the request
	request = urllib2.Request(url, data=None, headers=header)
	response = urllib2.urlopen(request)
	json_response = json.loads(response.read())
	
	# TODO - there could be >1 entry in entries, we should iterate over all and check
	entries = json_response['entries']
	if len(entries) > 0:
		result = True
		
	return result
	
def add_metadata(fileId, className, question, token):
	# url for posting metadata properties
	url = "https://api.box.com/2.0/files/" + fileId + "/metadata/global/properties"
	print "  adding properties: " + url
	
	# authorization headers for accessing the Box API
	headers = {'Content-type': 'application/json', 'Accept': 'application/json', 'Authorization':'Bearer %s' % token}
	data = json.dumps({'className':className, 'canonicalQuestion':question})
	
	# build and execute the request
	request = urllib2.Request(url, data=data, headers=headers)
	response = urllib2.urlopen(request)
	json_response = json.loads(response.read())
	
	#print json.dumps(json_response, indent=2)
	
def update_metadata(fileId, className, question, token):
	# url for posting metadata properties
	url = "https://api.box.com/2.0/files/" + fileId + "/metadata/global/properties"
	print "  updating properties: " + url
	
	# authorization headers for accessing the Box API
	headers = {'Content-type': 'application/json-patch+json', 'Accept': 'application/json', 'Authorization':'Bearer %s' % token}
	data = json.dumps([{'op':'replace', 'path':'/className', 'value':className},{'op':'replace', 'path':'/canonicalQuestion', 'value':question}])
	
	# build and execute the request
	opener = urllib2.build_opener(urllib2.HTTPHandler)
	request = urllib2.Request(url, data=data, headers=headers)
	request.get_method = lambda: 'PUT'
	response = opener.open(request)
	json_response = json.loads(response.read())
	
if __name__ == "__main__":
	parser = argparse.ArgumentParser()
	parser.add_argument("answers_file", help="json file containing the class to canonical question mappings")
	parser.add_argument("token", help="box access token needed to access the Box APIs")
	args = parser.parse_args()
	run(args.answers_file, args.token)
