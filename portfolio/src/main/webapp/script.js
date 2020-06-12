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

/** Fetching the user's comment. */
function addComment() {
    const responsePromise = fetch('/comment');
    responsePromise.then(handleResponse);
}

/** Parses the JSON and calls the function to print it. */
function handleResponse(response) {
    json = response.json();
    json.then(addCommentsToDom);
}

/** Adds a random quote to the DOM. */
function addCommentsToDom(comments) {
    const quoteContainer = document.getElementById('comment-container');

    i = 0;

    while (i < comments.length - 1) {

      // printing the name
      const nameElement = document.createElement('li');
      nameElement.className = 'name';
      nameElement.innerText = comments[i];
      quoteContainer.appendChild(nameElement);

      // printing the comment
      const commentElement = document.createElement('li');
      commentElement.className = 'comment';
      commentElement.innerText = comments[i + 1];
      quoteContainer.appendChild(commentElement);

      // printing the horizontal divider
      const lineElement = document.createElement('hr');
      lineElement.className = 'horizontal-line';
      quoteContainer.appendChild(lineElement);

      // adding spaces after the comment
      const spaceElement = document.createElement('br');
      quoteContainer.appendChild(spaceElement);
      
      i += 2;
    }
}

/** Recieved the url to post to as well as the images to print and handles
    adding them to the HTML */
function fetchBlobstoreUrlAndShowForm() {
  fetch('/blobstore-upload-url')
      .then((response) => {
        console.log('response');
        console.log(response);  
        return response.text();
      })
       .then((imageUploadUrl) => {
        console.log('imageUploadUrl');   
        console.log(imageUploadUrl);    
        const messageForm = document.getElementById('my-form');
        messageForm.action = imageUploadUrl;
        messageForm.classList.remove('hidden');
        const container = document.getElementById('container');

        const commentElement = document.createElement('li');
        commentElement.className = 'comment';
        commentElement.innerText = imageUploadUrl;

        const imgElement = document.createElement('img');
        imgElement.setAttribute('src', imageUploadUrl);

        container.appendChild(commentElement);
        container.appendChild(imgElement);
     });
}

/** Function for displaying a map. */
function createMap() {
  const map = new google.maps.Map(
    document.getElementById('map'),
    {center: {lat: 41.822055, lng: -71.396270}, zoom: 15});

  addLandmark(
      map, 41.822861, -71.392315, 'Like No Udder',
      'Vegan ice cream joint with amazing flavors and rainbow decorations.')
  addLandmark(
      map, 41.817512, -71.390816, 'India Point Park',
      'A lovely park to picnic in.') 
  addLandmark(
      map, 41.819559, -71.399020, 'Amy\'s Place',
      'My favorite breakfast place. The yummiest sandwiches you\'ll ever have')
    addLandmark(
      map, 41.825753, -71.406373, 'The Providence Athaneum',
      'Super aesthetic library. Can be difficult to focus because of how pretty it is...')
      
}

/** Adds a marker that shows an info window when clicked. */
function addLandmark(map, lat, lng, title, description) {
  const marker = new google.maps.Marker(
      {position: {lat: lat, lng: lng}, map: map, title: title});

  const infoWindow = new google.maps.InfoWindow({content: description});
  marker.addListener('click', () => {
    infoWindow.open(map, marker);
  });
}