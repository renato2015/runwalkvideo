# Introduction #

For the redcord module, I will implement a google calendar synchronisation with the application. Some usecases and their realistation are described here below.

# Description #

## Use Cases ##

  * Data needs to be synced from google calendar to the db
  * Data needs to be synced from the db to google calendar

## Implementation ##

### From google calendar to the db ###

Will show a syncing dialog with a small table inside, listing all the items to be synced from google with the db.

At this moment there is the CalendarEventEntryDialog that takes care of this. The backing beans for the table will be changed to regular RedcordSessions. The eventlist with all the clients has to be available to relate a client to each event.

  * New items
    * Data preparation
      * Filter out the events with a startDate later than today
      * Find in the filtered set the calendarIds that do not have an entry in the db yet
      * startDate, endDate, lastModified, calendarId and decription should be set on the RedcordSession before adding it to the dialog
    * Data syncing (dialog closed)
      * When there is was a client selected, add the session to the client's sessions collection
        * Update the calendar entry by changing the event name
        * Update the calendar entry by adding the selected client as a participant (EventWho object)
        * Set the lastModified field back on the RedcordSession and save it to the db
      * When there was no client selected, discard the item

  * Existing items
    * Data preparation
      * Find the set of RedcordSessions to be updated using the lastModified fields (Query object using the google api?)
      * Check whether the startDate of the session needs to be adapted
        * If yes, then add them to the dialog
        * If no, discard
          * Update the lastModified field of the session
    * Data syncing (dialog closed)
      * Update the startDate/endDate field of the session
      * Update the lastModified field of the session

### From the db to google calendar ###

Should happen transparently when saving

  * New Items
    * Query for RedcordSessions that do not have a calendarId yet.
    * Create a calendar entry (startDate, endDate, decription, participants) for this entry
    * Set the lastModified and calendarId fields found on the created calendar entries back on the RedcordSession
    * Save the RedcordSession
  * Existing Items
    * Items should be saved first (to have their latest values)
    * Query for dates not found in the calendar event entries

## Technical Implementation ##

### From google calendar to the db ###

  * Get a set of IDs for the chosen entity type, passed to the contstructor at init time, with a startingDate later than today
  * Iterate over all the found CalendarEntries later than today and check if their calId is in the Set
    * If not, create a new CalendarSlot from the entry and add it to the list to be shown in the dialog
    * If yes, get its corresponding CalendarSlot and check its lastModified date with that of the corresponding CalendarSlot
      * If lastModified is not the same, then copy startDate and endDate to the entity
      * If lastModified is the same, discard the item, as it is already synced

After closing the CalendarSlotDialog

  * Check whether a Client was selected for each CalendarSlot
    * If not, discard the item (set a flag to ignore it)
    * If yes, check if the CalendarSlot was already persisted
      * If not, persist the CalendarSlot