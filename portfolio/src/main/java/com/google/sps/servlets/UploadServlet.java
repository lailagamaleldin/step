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

package com.google.sps.servlets;

import com.google.appengine.api.blobstore.BlobInfo;
import com.google.appengine.api.blobstore.BlobInfoFactory;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.ServingUrlOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.gson.Gson;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Arrays;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.sps.data.Comment;

/**
 * When the fetch() function requests the /blobstore-upload-url URL, the content of the response is
 * the URL that allows a user to upload a file to Blobstore. If this sounds confusing, try running a
 * dev server and navigating to /blobstore-upload-url to see the Blobstore URL.
 */
@WebServlet("/blobstore-upload-url")
public class UploadServlet extends HttpServlet {

  private static DatastoreService datastore =
         DatastoreServiceFactory.getDatastoreService();

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

    // Setting up blobstore
    BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
    String uploadUrl = blobstoreService.createUploadUrl("/blobstore-upload-url");

    // Querying all Comment objects in the datastore.
    Query query = new Query("Comment").addSort("timestamp",SortDirection.DESCENDING);
    PreparedQuery results = datastore.prepare(query);

    Gson gson = new Gson();
    List<String> jsonContents = new ArrayList<>();
    jsonContents.add(uploadUrl);

    // Populating the JSON
    for (Entity entity : results.asIterable()) {
      String name = (String) entity.getProperty("name");  
      String commentText = (String) entity.getProperty("comment");
      String imgUrl = (String) entity.getProperty("imgUrl");

      jsonContents.add(name);
      jsonContents.add(commentText);
      jsonContents.add(imgUrl);
    }

    String json = gson.toJson(jsonContents);
    response.setContentType("text/html");
    response.getWriter().println(json);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

    // Get the URL of the image that the user uploaded to Blobstore and add 
    // it to the list of URLs.
    String name = request.getParameter("name-input");
    String commentText = request.getParameter("text-input");
    String imageUrl = getUploadedFileUrl(request, "image");
    long timestamp = System.currentTimeMillis();

    // Creating an enitity and setting 
    Entity commentEntity = new Entity("Comment");
    commentEntity.setProperty("name", name);
    commentEntity.setProperty("comment", commentText);
    commentEntity.setProperty("timestamp", timestamp);
    
    if (imageUrl == null) {
      commentEntity.setProperty("imgUrl", "");      
    } else {
      commentEntity.setProperty("imgUrl", imageUrl);
    }

    datastore.put(commentEntity);
    response.sendRedirect("/gallery.html");
  }

  /** Returns a URL that points to the uploaded file, or null if the user didn't upload a file. */
  private String getUploadedFileUrl(HttpServletRequest request, String formInputElementName) {
    BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
    Map<String, List<BlobKey>> blobs = blobstoreService.getUploads(request);
    List<BlobKey> blobKeys = blobs.get(formInputElementName);

    // User submitted form without selecting a file, so we can't get a URL. (dev server)
    if (blobKeys == null || blobKeys.isEmpty()) {
      return null;
    }

    // Our form only contains a single file input, so get the first index.
    BlobKey blobKey = blobKeys.get(0);

    // User submitted form without selecting a file, so we can't get a URL. (live server)
    BlobInfo blobInfo = new BlobInfoFactory().loadBlobInfo(blobKey);
    if (blobInfo.getSize() == 0) {
      blobstoreService.delete(blobKey);
      return null;
    }

    // Use ImagesService to get a URL that points to the uploaded file.
    ImagesService imagesService = ImagesServiceFactory.getImagesService();
    ServingUrlOptions options = ServingUrlOptions.Builder.withBlobKey(blobKey);

    // To support running in Google Cloud Shell with AppEngine's devserver, we must use the relative
    // path to the image, rather than the path returned by imagesService which contains a host.
    try {
      URL url = new URL(imagesService.getServingUrl(options));
      return url.getPath();
    } catch (MalformedURLException e) { 
      return imagesService.getServingUrl(options);
    }
  }
}
