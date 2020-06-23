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

  public final static int DAY_LENGTH = 24 * 60;  

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

//   private Collection<TimeRange> splitEventInTwo(TimeRange conflictingEvent,
//                                                 TimeRange proposedEvent, long neededDuration) {

//   }

  private Collection<TimeRange> removeOverlap(TimeRange conflictingEvent, 
                                              TimeRange proposedEvent, long neededDuration) {
    
    Collection<TimeRange> alteredEvents = new ArrayList<>();

    // event attendee is already registered for starts before the event to be chopped
    if (conflictingEvent.start() < proposedEvent.start()) {  

      TimeRange alteredEvent = cutEventFromStart(conflictingEvent, proposedEvent, neededDuration);
      
      if (alteredEvent != null) {
        alteredEvents.add(alteredEvent);
      }

    // event attendee is already registered for starts after the event to be chopped
    } else if (conflictingEvent.start() > proposedEvent.start()) { 

      // case where the prposed event starts before and ends after the conflicting event,
      // meaning it needs to be split in two   
      if (proposedEvent.end() > conflictingEvent.end()) {

        // split into before    
        int firstDuration = conflictingEvent.start() - proposedEvent.start();
        if (firstDuration >= neededDuration) {
          TimeRange firstEvent =
            TimeRange.fromStartDuration(proposedEvent.start(), firstDuration);
          alteredEvents.add(firstEvent);    
        }

        // split into after
        int secondDuration = proposedEvent.end() - conflictingEvent.end();
        if (secondDuration >= neededDuration) {
          TimeRange secondEvent =
            TimeRange.fromStartDuration(conflictingEvent.end(), secondDuration);
          alteredEvents.add(secondEvent);  
        }

      /** case where event ends before or at the same time as conflicting event 
         in this case, you want to end the event whenever the new one starts 
      */
      } else {
        int newDuration = conflictingEvent.start() - proposedEvent.start();

        if (newDuration >= neededDuration) {
          TimeRange alteredEvent =
            TimeRange.fromStartDuration(proposedEvent.start(), newDuration);
          alteredEvents.add(alteredEvent);    
        }
      }
    // both events start at the same time
    } else {

      if (proposedEvent.duration() > conflictingEvent.duration()) {
        int newDuration = proposedEvent.end() - conflictingEvent.end();
        if (newDuration >= neededDuration) {
          TimeRange alteredEvent =
            TimeRange.fromStartDuration(conflictingEvent.end(), newDuration);
          alteredEvents.add(alteredEvent);    
        }
      }
    }

    return alteredEvents;
  }

  /**
   * Method for querying appropriate time slots based on the request length and attendees'
   * schedules.
   */
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {    

    long duration = request.getDuration();
    Collection<String> attendees = request.getAttendees();

    // checking for invalid meeting requests
    if (duration <= 0 || duration > DAY_LENGTH) {
      return new ArrayList<>();  
    }

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
          if (eventTimeRange.overlaps(proposedTimeRange) ||
                eventTimeRange.equals(proposedTimeRange)) {
            
            // removing overlapping event and reconfiguring it to remove overlap
            proposedTimesCopy.remove(proposedTimeRange);
            Collection<TimeRange> updatedTimeRanges = 
             removeOverlap(eventTimeRange, proposedTimeRange, duration);
            
            for (TimeRange timeRange: updatedTimeRanges) { 
              proposedTimesCopy.add(timeRange);
            }

            // returning when done looping through attendee's events, meaning when all conflicts
            // have been resolved
            if (event.equals(events.toArray()[events.size() - 1])) {
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
