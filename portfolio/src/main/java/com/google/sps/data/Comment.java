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

  private final String comment;
  private final long timestamp;

  public Comment(String comment, long timestamp) {
    this.comment = comment;
    this.timestamp = timestamp;
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
    return (c.comment == this.comment && c.timestamp == this.timestamp); 
  } 
      
  @Override
  public int hashCode() { 
    return comment.hashCode(); 
  }
}
