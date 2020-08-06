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

package com.google.sps;

import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import com.google.sps.TimeRange;

public final class FindMeetingQuery {

  private final int DAY_LENGTH = 24 * 60;  
  private final int MIN_DURATION = 0;
  private final int MAX_DURATION = DAY_LENGTH;

  // Method for checking of there is overlap between the guests of two events
  private Boolean attendeesOverlap(Collection<String> eventOneAttendees, 
                                   Set<String> eventTwoAttendees) {
    for (String attendee: eventOneAttendees) {
      if (eventTwoAttendees.contains(attendee)) {
        return true;
      }
    }
    return false;
  }
  
  // Method for removing overlap by starting the event later
  private TimeRange cutEventFromStart(TimeRange conflictingEvent,
                                      TimeRange proposedEvent, long neededDuration) {
    
    // checking that the overlapping event isn't longer than the proposed event
    if (conflictingEvent.end() < proposedEvent.end()) {

      int newDuration = proposedEvent.end() - conflictingEvent.end();
      if (newDuration >= neededDuration) { 
        return TimeRange.fromStartDuration(conflictingEvent.end(), newDuration); 
      }      
    }

    return null;                                        
  }

   // Method for removing overlap by evending the event earlier
   private TimeRange cutEventFromEnd(TimeRange conflictingEvent,
                                       TimeRange proposedEvent, long neededDuration) {

     int newDuration = conflictingEvent.start() - proposedEvent.start();
     if (newDuration >= neededDuration) {
       return TimeRange.fromStartDuration(proposedEvent.start(), newDuration);    
     }

     return null;
   }


  // Method for checking is one event starts after another event
  private boolean startsAfter(TimeRange rangeOne, TimeRange rangeTwo) {
    return rangeOne.start() > rangeTwo.start();
  }

  private boolean endsAfter(TimeRange rangeOne, TimeRange rangeTwo) {
    return rangeOne.end() > rangeTwo.end();
  }

  // Method for removing the overlap between two events
  private Collection<TimeRange> removeOverlap(TimeRange conflictingEvent, 
                                              TimeRange proposedEvent, long neededDuration) {
    
    Collection<TimeRange> alteredEvents = new ArrayList<>();

    // event attendee is already registered for starts before or concurrently with the
    // event to be chopped
    if (startsAfter(conflictingEvent, proposedEvent)) {  
      TimeRange alteredEvent = cutEventFromEnd(conflictingEvent, proposedEvent, neededDuration);

      if (alteredEvent != null) {
        alteredEvents.add(alteredEvent);    
      }   

      // case where the prposed event also ends after the original event, meaning a second event
      // needs to be created, one that starts after the conflicting event
      if (endsAfter(proposedEvent, conflictingEvent)) {
        TimeRange secondEvent = cutEventFromStart(conflictingEvent, proposedEvent, neededDuration);

        if (secondEvent != null) {
          alteredEvents.add(secondEvent);  
        }
      }
    // event attendee is already registered for starts after the event to be chopped
    } else { 
      TimeRange alteredEvent = cutEventFromStart(conflictingEvent, proposedEvent, neededDuration); 
      
      if (alteredEvent != null) {
        alteredEvents.add(alteredEvent);
      }      
    } 

    return alteredEvents;
  }


  // method for updating the proposed times by cutting out conflicts
  private Collection<TimeRange> updateProposedTimes(Collection<TimeRange> proposedTimesCopy, 
      TimeRange eventTimeRange, TimeRange proposedTimeRange, long duration) {
    // removing overlapping event and reconfiguring it to remove overlap
    proposedTimesCopy.remove(proposedTimeRange);
    Collection<TimeRange> updatedTimeRanges = 
      removeOverlap(eventTimeRange, proposedTimeRange, duration);

    // for loop because the event might get split in half and become two events        
    for (TimeRange timeRange: updatedTimeRanges) { 
      proposedTimesCopy.add(timeRange);
    }
    return proposedTimesCopy;
  }

  // method for indicating whether all conflicts have been resolved yet
  private boolean allConflictsResolved(Collection<Event> events, Event currEvent) {
    // checks that the end of the list has been reached, meaning all conflicts have been resolved
    return currEvent.equals(events.toArray()[events.size() - 1]);
  }

  // method for indicating whether or not a proposed duration is invalid
  private boolean invalidDuration(long duration) {
    return duration <= MIN_DURATION || duration > MAX_DURATION;
  }

  /**
   * Method for querying appropriate time slots based on the request length and attendees'
   * schedules.
   */
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {    

    long duration = request.getDuration();

    // checking for invalid meeting requests
    if (invalidDuration(duration)) {
      return new ArrayList<>();  
    }

    Collection<String> attendees = request.getAttendees();

    // setting the initial proposed times to be the entire day
    //  this implementation is built upon eliminating conflicts as they come up
    TimeRange allDay = TimeRange.fromStartDuration(0, DAY_LENGTH);
    Collection<TimeRange> proposedTimes = new ArrayList<>(Arrays.asList(allDay));
    Collection<TimeRange> proposedTimesCopy = new ArrayList<>();

    // looping through existing events
    for (Event event: events) {
      // checking for attendee overlap   
      if (attendeesOverlap(attendees, event.getAttendees())) {

        TimeRange eventTimeRange = event.getWhen();
        for (TimeRange proposedTimeRange: proposedTimes) {
          // checking for time overlap
          if (eventTimeRange.overlaps(proposedTimeRange)) {
            proposedTimes = 
              updateProposedTimes(proposedTimesCopy, eventTimeRange, proposedTimeRange, duration);

            if (allConflictsResolved(events, event)) {
              return proposedTimesCopy;   
            }
          }
        }
        // updating the proposed so that the time conflicts that have been detected are removed
        proposedTimes = proposedTimesCopy;
      }
    }
    return proposedTimes;
  }
}
