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

package com.google.sps.data;

/** A user comment. */
public final class Comment {

  private final String name;
  private final String comment;
  private final String imgUrl;
  private final long timestamp;

  public Comment(String name, String comment, String imgUrl, long timestamp) {
    this.name = name;
    this.comment = comment;
    this.imgUrl = imgUrl;
    this.timestamp = timestamp;
  }

  public String getName() {
    return name;
  }

  public String getComment() {
    return comment;  
  }

  public String getImgUrl() {
    return imgUrl;  
  }

  public long getTimestamp() {
    return timestamp;  
  }

  @Override
  public boolean equals(Object obj) { 
    if(this == obj) {
      return true; 
    } 
 
    if(obj == null || obj.getClass()!= this.getClass()) {
      return false;
    }
     
    Comment c = (Comment) obj; 
    return (c.name.equals(this.name) && c.comment.equals(this.comment) &&
        c.timestamp == this.timestamp); 
  } 
      
  @Override
  public int hashCode() { 
    return comment.hashCode(); 
  }
}
