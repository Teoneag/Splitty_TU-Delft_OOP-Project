|              |                                 |
|--------------|---------------------------------|
| Date:        | 19.03.2024                      |
| Time:        | 15:45                           |
| Location:    | Delft Drebbelweg PC 1 cubicle 4 |
| Chair        | Nicolas Laplagne                |
| Minute Taker | Teodor Neagoe                   |
| Attendees:   | Everyone (hopefully)            |

# Agenda Items

## Opening (10 min)
- Checkup (2 min)
    * Did everyone meet their requirements these last two weeks?
    * How were midterms (balance between OOPP and other courses?)
- Approval of the agenda - Does anyone have any additions? (1 min)
- Approval of last minutes - Did everyone read the minutes from the previous meeting? (1 min)
- TA announcements (5 min)
- Present app to TA (1 min)

## Points of Action (5 min)
- Feature/technology
    - Websockets
    - Extensions
- Aesthetic
    - Cohesive app-wide theme
    - Fix visual bugs - text overflow, window size

## Agenda (20 min)
- Feedback on Tasks & Planning assignment (5 min)
    * 7.3/10
    * Doing well
        * Good use of labels, time estimation, feature branches
        * Full-stack development
    * Not doing so well
        * **Make issues larger** - *10 min issues???*
        * Be more careful/consistent with milestones
            * Expired milestones/issues
            * Issues without milestones
        * Nested feature branches?
        * Time tracking *after* estimation
- Buddy Check (5 min)
    * Has everyone read their feedback?
    * Does everyone understand the feedback?
- Technology Assignment (2 min)
    * We'd likely be at good/very good - how to get to excellent?
        * Dependency injection?
        * @Services for business logic, shared state
            * Should business logic be client-side, not in commons?
        * Jersey/Stomp endpoints?
        * Do we have any explicit jackson calls (ideally all implicit)
- Code Contributions and Code Reviews Assignment (2 min)
    * focused commits - small commits for each little bit
    * isolated features - feature branches
    * MR reliability - few formatting changes, few commits, no conflicts
    * reviews - timely, lead to changes, involve more than two people
    * build server - main pipeline ideally never fails, fixed quickly
- HCI (3 min) - continue discussion on wednesday
    * Accessibility
        * Colour contrast (colourblind accessibility)
        * Keyboard shortcuts for back, create event, add expense, etc
        * Multi-modal visualisation - icons, colour coding
    * Navigation
        * Logical navigation - ideally already
        * Keyboard only - use tab to navigate fields, implement enter to submit
        * Undo - definitely not there yet
    * User feedback
        * Errors
            * Catch invalid expense values (-$10), unavailable server
            * Pop-up/marker errors (rather than console)
            * Descriptive error messages - mostly for user input
        * Informative feedback - change screen content, notifications, etc
        * Confirmation for delete operations (are you sure you want to delete this?)
- Start working on extensions? (5 min)
    * Week(s) 4 (& 5) for finishing/polishing main features
    * No need to panic - 4 weeks left

## Closing (10 min)
- Other questions for the TA (5 min)
- Technical questions (5 min)
    * File path?
