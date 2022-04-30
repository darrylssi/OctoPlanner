# Seng302 Example Project

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


## How to run
The project must be imported into an IDE that supports Gradle. The steps for importing and setting up the project into Intellij are given below.
  - Open Intellij IDE
  - Click "Get from VCS" button
  - Copy and paste team-800 HTTPS clone URL into URL in Intellij
  - Select the directory to save the project
  - Click "Clone"
  - In project, Go to "File", select "Project Structure"
  - Then, go to "SDKs" and set the language level to 17 

The steps to run the project from here are:
  - Open terminal and run the IDP module, then portfolio module (given below).
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
  - On each sprint, there is bin shaped button to delete the sprint 


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


## Todo (Sprint 2)

- Update team name into `build.gradle`
- Set up Gitlab CI server (refer to the student guide on learn)

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
