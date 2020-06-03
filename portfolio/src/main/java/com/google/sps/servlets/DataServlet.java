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

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.gson.Gson;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;

/** Servlet that handles functionality for commenting. */
@WebServlet("/comment")
public class DataServlet extends HttpServlet {

  // Arraylist storing user comments.
  private List<String> comments = new ArrayList<>();

  private static DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();  

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
        throws IOException {

    Query query = new Query("Comment");

    PreparedQuery results = datastore.prepare(query);

    for (Entity entity : results.asIterable()) { 
      String comment = (String) entity.getProperty("comment");
      comments.add(comment);
    }

    System.out.println(comments.size());

    // Creating and parsing the JSON object.
    Gson gson = new Gson();
    String json = gson.toJson(comments);
    response.getWriter().println(json);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response)
        throws IOException {

    // Parsing the user's input into the text box.        
    String comment = request.getParameter("text-input");
    long timestamp = System.currentTimeMillis();

    Entity commentEntity = new Entity("Comment");
    commentEntity.setProperty("comment", comment);
    commentEntity.setProperty("timestamp", timestamp);

    datastore.put(commentEntity);

    // Redirect back to the page where the user entered their comment.
    response.sendRedirect("/comments.html");
  }
}
