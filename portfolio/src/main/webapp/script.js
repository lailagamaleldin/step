// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

/**
 * Reads a text file and returns an array of the file contents split on the
 * specified delimiter.
 */
function readFile(filePath, split, delimiter = ' ') {

	var contents = [];

    // Retreiving file contents.
	fetch(filePath)
		.then(
			function (response) {
                // Checking for error status.
				if (response.status != 200) {
				  console.log('Error with status code: ' + response.status);
				  return;
				}

                // Splitting on the parameter.
				response.text().then(function (text) {
                    var splitText;

                    if (split) {
                      splitText = text.split(delimiter);
                    } else {
                      splitText = text.split();
                    }

					splitText.forEach(elem => contents.push(elem));
				});

			}
		).catch(function (err) {
		  console.log('Error: ', err);
		});


	return contents;
}

// Compiling the facts into an array.
let facts = readFile('resources/facts.txt', true, '\n');


/**
 * Adds a random fun fact to the page.
 */
function addRandomFunFact() {

    // Getting a random fact.
	let fact = facts[Math.floor(Math.random() * facts.length)];

	// Add it to the page.
	const factContainer = document.getElementById('fact-container');
	factContainer.innerText = fact;
}
