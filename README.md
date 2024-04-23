# Splitty - OOP Project
Splitty is an expense management app that can be used to create and join events and manage expenses.

<img src="client/src/main/resources/images/Splitty_Icon2.png" alt="App icon">

## ToDo
- ToDos from the project
  - show error messages
- Bugs
  - solve all warnings (hide)
  - fix expense limit
- Theme
  - right click on text filed
- Refactor
  - language switching (use bind)
  - undo (use stack)
  - navigation (use stack)
- Testing
  - Java with Gradle
- looking at other projects for best practices
- all pages
  - add right click context
- admin
  - rename to export
  - add default value to change server
  - make impossible to change to non-existing server
- navigation bar
  - currency
  - language
  - settings
- event overview
  - make layout better
- debts
  - see all debts
  - settle debts: close event before settling debts
- statics
  - add tag
  - when remove tag if used change to no tag
- add participant
  - romanian translation termite
  - email verification
  - show that email was send
- add expense
  - don't let people get here without having participants
  - layout
- home
  - remove recent event
- ReadMe
  - add link to backlog (task vs result)


## Done
- remove print statements
- move to permanent database

## Project Execution

### Starting the Server
To start the server go to the project path on your terminal and write the following commands:

For linux and Mac:
```
./gradlew bootRun
```
For Windows
```
gradle bootRun
```

When starting the server you will see an admin password in the terminal which can be used to access the admin page on the client.


### Start the Client
To start the client go to the project path on your terminal and write the following command:

For Linux and Mac:
```
./gradlew run
```
For Windows:
```
gradle run
```

Once the client is started you should end up on the following interface:


## Features

### Websockets

Websockets has been implemented on the event overview page allowing for live view of changes that are made to a particular event.

### Long Polling
Long polling has been added to the admin page to be able to see when events are created, deleted and modified in real time.

## HCI Features
There are countless visible HCI features implemented, but also some less obvious ones. Here are some of the most important ones:
### 1. Full keyboard navigation
The app can be used without a mouse. If you ever need help with the keyboard shortcuts, you can press `ctrl + ?` or `ctrl + /` to see a list of all the shortcuts available to you.

Here is a list of some of the most important shortcuts:
- You can use `tab` navigation to navigate through the app:
    - Press `tab` to go to the next element, `shift + tab` to go to the previous element.
    - Press `enter` or `space` to select an element.
    - Use the arrow keys (`up`, `down`) to change the value of a drop-down menu or to navigate through tables.
    - Press `esc` to exit a text field.
- To go back to the previous page, press `alt + left arrow`.
- For the event overview page:
    - To create a new participant, press `ctrl + p`.
    - To create a new expense, press `ctrl + e`.

### 2. Icons and colours
This one is pretty obvious, but we have used icons and colours to make the app more visually appealing and easier to use. 
For example, the tags have a coloured background, the buttons have icons and colours, important information is highlighted in red, etc.
### 3. Undo Actions
Each field in the add/edit expense page supports individual undo actions via the corresponding button to the right of the field. This way, if you've been 
editing an expense and accidentally change something you didn't want to, but don't want to undo all the changes you've made to other fields, you can simply 
undo the specific field that you made the mistake in.
### 4. Bonus
There is a hidden feature! We don't want to spoil the surprise, but to find it turn the volume up and explore the app (hint: it's on the event overview page).



## Extensions
### Live Language

On this app 3 languages are currently supported: English, Dutch and Romanian.

Changing the language can be done in the settings, the home overview and the event overview. You will see a language indicator with a flag, so you know which language is currently configured. Changing the language is live, so you don't have to worry about restarting the app. If you have changed the language and restart the app, this language will also be persisted, so you don't have to worry about changing the language everytime you use the app.

If you would like to add your own language to this project, there is a language template that you can download from the settings page. You can then fill the template in your own language and send it to us, so we can add it in a future update.


### Detailed Expenses

Expenses can be given a date. Expenses can be split between everyone or just a small group of people. You can select who. To make it easy for you, we made some buttons to select/unselect everyone quickly. For settling debts, payments can also be made simply by choosing the person who pays and who receives the money.

In the event overview you can see all of these expenses. Those expenses can also be filtered.


### Foreign Currency

Our app supports multiple different currencies. For the moment the following currencies are supported. 
- EUR - Euro
- USD - United States Dollar
- CHF - Swiss Franc
- GBP - Great British Pound

You can specify your default currency in the settings page, so that you only see any amounts you see will only be displayed in that currency.

When creating an expense you can select the currency it was originally paid in, and it will be stored this way by the server. 
When viewing the expense later, you will see the amount converted into your preferred currency. When editing the expense, it will
be shown in the original currency, so that you can manage expenses in the currency they were paid in.

### Open Debts

On the open debts screen within an expense you can see the fastest way to settle your debts. Just select your name in the drop-down and only debts related to you will be shown. If someone owes you money, clicking on their name will show you're their email if they have one. If you owe them money, clicking on their name will show you their bank details if they have either a BIC or an IBAN.

To settle your debts you can click on the add payment button at the bottom of the page and add a payment to whoever you owe money.

### Statistics

Tags can be added to expenses, so you can organise expenses by type. You will be able to see those tags in the event overview. There are three standard tags per event: food, entrance fees, and travel to simplify your life. When creating an expense, if you don't find any tags that suit your needs, you can just create a new one.

Once you have added expenses you will be able to see how much you have spent on each type of expense in the statistics page in a table and in a pie chart. There you can also edit the name and colour of the tags you have made as well as delete them.

In this statistics page, you will also be able to see how much you have spent in total for this event.

For aesthetics, throughout the app tags will have a coloured background (the colour you chose).

### Email Notifications
Email notification are embedded into the app so that added participants automatically receive an email with the event details.
That way they can easily join the event that they are part of.

For using this feature you need to add your credentials to the user config file ("client/src/main/java/client/config/user_configs.properties").
We recommend using gmail, since other email providers might not work.
Also for gmail, the following needs to be done to make it pass google security measures:
- Go to your Google account settings and then to security.
- Turn on 2-step verification.
- Within the 2-step verification settings, scroll down to "App passwords" and generate a new password for the app.
- Copy this password and paste it in the user config file as the password, use your mail address (ending with gmail.com) as the .
- The email can be tested in the settings page by clicking on the send test email button.

For now, we also added a dummy email address in the user config file so that you can test the invitation email feature without having to add your own email.


