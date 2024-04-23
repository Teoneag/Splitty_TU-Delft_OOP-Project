# **Main topics**

## **TA announcements**
- Implemented features & product pitch formative deadline this week
- Don't need to use mockito or stubs for testing, but it could make your life easier 
- Check brights pace for presentation schedule (we are on friday)
- look at template slides on brights pace and submit our own before friday
- about testing as you go so add it to merge requests
- make services for all controllers before we test them in the backend most important
- Regarding HCI feedback is explicit. Do specific rubric stuff. Catch when the server stops running
- Implement extensions correctly immediately to get from a good to excellent easily

## **Showing Ta application**
- looks pretty good (didn't show new UI a lot) 

## **General**
- UI for event (suggestion by Teo)
- we made participants consistent throughout the app
- keyboard shortcuts are done 
- start looking at feedback we got and adjust course to match our goal of a 7 -> (9)

## **Basic Requirements**
- almost done
- calculating expenses missing (Nico)
- added websockets (long-polling)
- server switching (kinda works)
- UI updated (not merged at the time of meeting)

## **Extensions**
- Divide extensions in milestones
- Focus merge requests on first part of the week because of easter break
- Live language switching should be close to done (only need flag icons)
- Foreign currency in progress, when creating an expense you can change currency
- For statistics, we need to make tags and relate them to expenses, rest should be easy.
- Not focus on all extensions and just do 2-3 at a time (email should be last)

# **Assignments Formative/Summative**

## **Testing**
- We SHOULD focus on testing 
- while writing code we should also write tests as good practice
- need to test API endpoints
- No end-to-end testing just test if they work through stubs or mockito

## **Implemented Features**
- Very close to done with basic requirements, focus this week on extensions
- Pick extensions that are closest to done and finish them first

## **Project Pitch**
- we focus on a certain set of slides, so we don't need to be together and have a meeting to discuss flow of presentation
- use google slides
- we can have a mockup presentation in week 9 (TA recommendation)

# **Ending**

## **Small discussions**
- expense will persist removal of a participant
- we need to figure out what to do when a user tries to leave an event when they have debt
  - give a popup when someone tries leaving an event while having debt
- UI event: errors only should show when typing. 


## **Questions for TA**
- Question how to do tests for services for business logic?
  - you have controllers in front-end and back-end
  - have a util function in the server in controller
  - stubs less complicated than mockito
  - test if certain functions are called
- Question: recommend different util classes for servers?
  - TA recommends that
- Question: about buttons some translations are too big or small
  - when you hover it could show a tooltip

## **Other**
- for the undo we should load the current state of the event and have a button to go back to that state
