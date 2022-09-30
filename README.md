# LensFolio by Octonary

Basic project template using `gradle`, `Spring Boot`, `Thymeleaf` and `Gitlab CI`.

> This should be your project's README (renamed to `README.md`) that your team will continually update as your team progresses throughout the year.
> 
> Update this document as necessary.

## Basic Project Structure

- `systemd/` - This folder includes the systemd service files that will be present on the VM, these can be safely ignored.
- `runner/` - These are the bash scripts used by the VM to execute the application.
- `shared/` - Contains (initially) some `.proto` contracts that are used to generate Java classes and stubs that the following modules will import and build on.
- `identityprovider/` - The Identity Provider (IdP) is built with Spring Boot, and uses gRPC to communicate with other modules. The IdP is where we will store user information (such as usernames, passwords, names, ids, etc.).
- `portfolio/` - The Portfolio module is another fully fledged Java application running Spring Boot. It also uses gRPC to communicate with other modules.

Both the Portfolio and IdentityProvider modules generally follow a Model-View-Controller structure. Both have `model` and `controller` subfolders, as well as utilising `authentication`, `utils`, and `service` packages. The Portfolio module also has a `customthymeleaf` subfolder. The purposes of each of these packages is listed below:
- `model` - represent the data entities such as Users, Sprints, Projects, Events, etc.
- `controller` - handle the requests made to various related endpoints
- `authentication` - authenticate the user and make sure that the application is treating them correctly for what their role is the entire time
- `utils` - provide common functionality such as converting dates between string type and date type
- `service` - to allow communication (by gRPC) between Portfolio and the IdP
- `customthymeleaf` - to allow us to access certain features of the utilities from the thymeleaf front-end.

## Dependencies
The LensFolio application depends on the following libraries and APIs to run:
- Spring Boot
- Thymeleaf
- FullCalendar
- JSON Web Token
- H2 database
- Mariadb
- Java Persistence API
- Spring Websocket
- SockJS
- Stomp Websocket
- Spring Messaging
- Gradle
- gRPC

As well as the following to perform tests:
- Cypress
- Node and the NPM
- JUnit
- Jacoco
- Cucumber

## Running on the VM
Commits to dev and tagged commits to master deploy to the VM, accessible at https://csse-s302g8.canterbury.ac.nz/test/portfolio/ and https://csse-s302g8.canterbury.ac.nz/prod/portfolio respectively. They use the MariaDB database. More information on this topic, including how to access it, can be found here: https://eng-git.canterbury.ac.nz/seng302-2022/team-800/-/wikis/Working-with-the-VM.

## How to run locally
The project can be run from the terminal. Otherwise, the project must be imported into an IDE that supports Gradle, such as IntelliJ. The steps for importing and setting up the project in both cases are given below. When running locally like this, the application uses two H2 in-memory databases (one each for portfolio and IDP).

### Importing into IntelliJ
Importing into IntelliJ takes the following steps:
  - Open IntelliJ IDE
  - Click "Get from VCS" button
  - Copy and paste team-800 HTTPS clone URL into URL in IntelliJ
  - Select the directory to save the project
  - Click "Clone"
  - In project, Go to "File", select "Project Structure"
  - Then, go to "SDKs" and set the language level to 17 

To run the project from the IDE:
  - Open the IntelliJ terminal and run the IDP module, then portfolio module (given below).

### Importing from the terminal
To import from the terminal, follow these steps:
  - Open a terminal instance and navigate to the directory you want the root of the project to appear in
  - Open another instance (such as a tab) and navigate to the same location
  - Run `git clone https://eng-git.canterbury.ac.nz/seng302-2022/team-800.git`

To build the application from the terminal, follow steps 1, 2, and 3 (shared, IDP, and portfolio module) below.

### 1 - Generating Java dependencies from the `shared` class library
The `shared` class library is a dependency of the two main applications, so before you will be able to build either `portfolio` or `identityprovider`, you must make sure the shared library files are available via the local maven repository.

Assuming we start in the project root, the steps are as follows...

On Linux: 
```
cd shared
./gradlew clean
./gradlew publishToMavenLocal
```

On Windows:
```
cd shared
gradlew clean
gradlew publishToMavenLocal
```

*Note: The `gradle clean` step is usually only necessary if there have been changes since the last publishToMavenLocal.*

### 2 - Identity Provider (IdP) Module
Assuming we are starting in the root directory...

On Linux:
```
cd identityprovider
./gradlew bootRun
```

On Windows:
```
cd identityprovider
gradlew bootRun
```

By default, the IdP will run on local port 9002 (`http://localhost:9002`).

### 3 - Portfolio Module
Now that the IdP is up and running, we will be able to use the Portfolio module (note: it is entirely possible to start it up without the IdP running, you just won't be able to get very far).

From the root directory (and likely in a second terminal tab / window)...
On Linux:
```
cd portfolio
./gradlew bootRun
```

On Windows:
```
cd portfolio
gradlew bootRun
```

By default, the Portfolio will run on local port 9000 (`http://localhost:9000`)

### Accessing the running application
  - Open a browser and paste "http://localhost:9000/login" URL to start
  - Click "Register" button to register the user
  - After registering, in login page, login using your registered username and password
  - Click "Edit Profile" to edit the profile details
  - Click "Users" to check the users registered 
  - Click on image button on top right to open the drop down menu, and select "Profile" or "Logout" to go to the respective page.
  - Click "Project Details" to open the default project
  - Click on pencil shaped button under image button to edit the project details
  - Click on "+ Add Sprint" to add a sprint to the project
  - On each sprint, there is pencil shaped button to edit the sprint details
  - On each sprint, there is bin shaped button to delete the sprint.
  - Click "Groups" to show all the groups (There are two default groups)
  - Click on "+ Add Group" to add a group to the groups
  - On each group, there is bin shaped button to delete the group, except for one default group.


#### Accessing the database
In the event that you need to access one of the in-memory databases, make sure you have the correct module running, navigate to `url` for the appropriate module, and make sure the `JDBC_URL`, `username`, and `password` match the values here.

**IdentityProvider**\
url: `localhost:8081/h2-console`\
file: `jdbc:h2:file:./data/userdb`\
username: `sa`\
password: leave blank

**Portfolio**\
_To access this, you will need to be logged into portfolio - otherwise you get redirected to login_
url: `localhost:9000/h2-console`\
file: `jdbc:h2:file:./data/userdb`\
username: `sa`\
password: leave blank

## User Manual
The user manual of the application can be found at <https://docs.google.com/document/d/18i0VFJ5rMoCj69X1UW01uZUI77weBlpzSfD1sygBQ_M/edit?usp=sharing>.  

## 1 Accessing the application

The production version of the application can be found at <https://csse-s302g8.canterbury.ac.nz/prod/portfolio>.

### 1.1 If the server is down

If it is not currently running on the link above, it has likely encountered an error. In this case, contact the team so we can get it operational again.

If you are attempting to test our application, consider also trying the development version at `https://csse-s302g8.canterbury.ac.nz/test/portfolio`. This has the following **development admin login**:\
Username: `admin`\
Password: `SourNose3`

You will be able to create other users to test the other roles and such. These are not provided as testing in this manner is not the best.

If that is also not working, you will need to follow the instructions on running it locally (found in the README).

## 2 Using the application

### 2.1 Personal account management

#### 2.1.1 Logging in/Creating an account

The home of the application (or any page that requires you to be logged in) will redirect you to the login page, where you can sign in or register an account. Registering will automatically log you in, and assign you as a student. You will be taken to your profile page when you successfully log in. You can return to the profile page or log out at any time by clicking the profile photo on the top bar that sits across all logged-in pages.

Log in details for preset accounts with each of the various roles (_these only work on the production server. If that is down, see section 1.1 above_):

**Course Administrator:**\
Username: `teacher`\
Password: `cheeseman`

**Teacher:**\
Username: `gha95`\
Password: `SourNose3`

**Student:**\
Username: `voodooChild`\
Password: `WoodStock1969`

#### 2.1.2 Editing your details

While logged in, your profile can be accessed by clicking on your profile photo in the top right corner of the page. From your profile page, you can select the "Edit Profile" button which will take you to a page where you can edit your details, change your password, and change your profile photo. Profile photos will be cropped to your liking and compressed when they are uploaded.

### 2.2 Managing other users

Clicking the 'Users' tab on the navigation banner will take you to a page containing a list of registered users. You can sort this list by any one of the presented fields by clicking on the field name at the top of the table. Clicking again on the field that the table is currently sorted by reverses the sort order (i.e. from ascending to descending or vice versa).

#### 2.2.3 Adding/Removing user roles

When logged in as a teacher or course administrator you will be able to give users (including yourself) roles. To give a user a new role, click on the plus icon in that user's row, and then click on the role you would like to give the user in the dropdown menu.

To remove a role from a user, click on the 'X' symbol next to the role. Note that every user must have at least one role, meaning you cannot remove someone's role if they have only one.

The **role hierarchy** is Course Administrator > Teacher > Student. Teachers cannot add or remove the Course Administrator role, and students cannot edit any roles.

### 2.3 Managing a project (textual format)

_Also see section 2.4 Managing a project (visual format)._\
Clicking the 'Project Details' tab at the top of the page will take you to a page where you can view details about the current project, including sprints and events. This page outlines content in order of start date.\
Sprints are displayed in a list underneath the project's details.\
Events are displayed in every sprint, as well as in any gaps in/around sprints, provided that the event overlaps with said sprint or gap. Hovering over an event will show you the description of that event. The gradient in the background of an event goes from the sprint colour at the start of the event to the sprint colour at the end of the event (with either or both being replaced by the system default of red if sprints do not overlap the event start/end dates).

#### 2.3.1 Editing projects (textual)

_Note that there is no way to edit projects visually, so this is the only method of doing so._\
When a teacher is on the project details page, they are able to edit the details of the project by clicking on the edit button at the top of the page. The start date of a project cannot be set any earlier than a year before it was created, and if the project contains sprints it cannot be edited such that they are no longer included.

##### 2.3.2 Editing sprints (textual)

Sprints can be edited individually by clicking on the edit icon in the top right of that sprint's panel. Sprints can be deleted by clicking on the rubbish bin button and will be permanently removed after confirmation. New sprints can be created using the "Add sprints" button underneath the list of sprints.

Sprints cannot have dates that overlap with other sprints or fall outside the project dates.

#### 2.3.3 Editing events

New events can be added using the "Add Event" button at the bottom of the page.

Events can be edited by clicking the pencil-like icon on the far right of the event. Opening the edit form for one event will close the edit form of any other event (including the same event, shown in a different place on the page), then open a form in the location clicked. Clicking the button again will close this form, or the cancel button in the form can be used to accomplish the same result.

When editing an event, other accounts with the teacher or course administrator role will be able to see that the event is currently being edited.

If an event is edited or deleted by another user while you are editing the page, the page will automatically update to reflect any changes made.

Events can be deleted by clicking the rubbish bin icon on the far right of the event.

### 2.4 Managing a project (visual format)

_See section 2.3 Managing a project (textual format)._\
Clicking the 'Calendar' tab at the top of the page takes you to the monthly planner. This displays the sprints within the project.

#### 2.4.1 Editing sprints (visual)

The duration of sprints can be edited from the Calendar tab. Select a sprint by clicking on it, and then click and drag at either end of the sprint to alter its duration. The same rules (no overlapping with other sprints, no sprint dates outside the project) still apply.

## Contributors

- SENG302 teaching team
- Darryl Anne Alang
- Max Bastida
- Andrew Hall
- George Hampton
- Sahil Negi
- Jacques Terblanche
- Jonathan Tomlinson

## References

- [Spring Boot Docs](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/)
- [Spring JPA docs](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/)
- [Thymeleaf Docs](https://www.thymeleaf.org/documentation.html)
- [Learn resources](https://learn.canterbury.ac.nz/course/view.php?id=13269&section=9)
