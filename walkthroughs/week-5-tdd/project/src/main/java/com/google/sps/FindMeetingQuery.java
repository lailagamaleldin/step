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

public final class FindMeetingQuery {

  public final static long DAY_LENGTH = 24 * 60;  

  private Boolean attendeesOverlap(List<String> eventOneAttendees, List<String> eventTwoAttendees) {
    for (String attendee: eventOneAttendees) {
      if (eventTwoAttendees.contains(attendee)) {
        return true;
      }
    }
    return false;
  }

  private Collection<TimeRange> removeOverlap(TimeRange conflictingEvent, TimeRange event, long duration) {
    Collection<TimeRange> alteredEvents = new ArrayList<>();

    // conflicting event starts before the event to be chopped
    if (conflictingEvent.start() < event.start()) {
      
      // if the conflicting event ends before, reduce the start to when the conflicting event ends
        // check if the new duration is long enough, in which case add it
      if (conflictingEvent.end() < event.end()) {
        // 4 - 6
        // 5 - 7
        long newDuration = event.end() - conflictingEvent.end();
        if (newDuration => duration) {
          TimeRange alteredEvent = new TimeRange(conflictingEvent.end(), newDuration);
          alteredEvents.add(alteredEvent);  
        }      
      }   

    // conflicting event starts after the event to be chopped
    } else if (conflictingEvent.start() > event.start()) {

      // case where event ends after conflicting events (surrounding it)
        // here, you want to split the event in two    
      if (event.end() > conflictingEvent.end()) {

      // case where event ends before or at the same time as conflicting event 
        // in this case, you want to end the event whenever the new one starts
      } else {
        long newDuration = event.start() - conflictingEvent.start();
        if (newDuration >= duration) {
          TimeRange alteredEvent = new TimeRange(event.start(), newDuration);
          alteredEvents.add(alteredEvent);    
        }
      }
    // both events start at the same time
    } else {

    }

    return alteredEvent;
  }

  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {    
    // You're given a request which has a duration and a collection of attendees
    // You're also given a list of all the events. Each event has a time range and a list of attendees    
    long duration = request.getDuration();
    Collection<String> attendees = request.getAttendees();

    if (duration() <= 0 || duartion > )

    Collection<TimeRange> proposedTimes = new ArrayList<>(Arrays.asList(new TimeRange(0, 24 * 60)));
    
    for (Event event: events) {
      // there are attendees of the event who are going to another event so we want to remove that timeframe from the validTimes  
      if (attendeesOverlap(attendees, event.getAttendees())) {
        TimeRange eventTimeRange = event.getWhen();

        Collection<TimeRange> timeRangeCopy = validTimes;

        for (TimeRange proposedTimeRange: proposedTimes) { 

          if (eventTimeRange.overlaps(proposedTimeRange) || eventTimeRange.equals(proposedTimeRange)) {
            timeRangeCopy.remove(proposedTimeRange);
            List<TimeRange> updatedTimeRanges = removeOverlap(eventTimeRange, proposedTimeRange, duration);

            for (TimeRange timeRange: updatedTimeRanges) {
              timeRangeCopy.add(timeRange);  
            }
          }
        }
        validTimes = timeRangeCopy;
      }
    }

    return validTimes;
  }
}
