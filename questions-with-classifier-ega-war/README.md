# questions-with-classifier-ega-war

This project builds a deployable WAR file and contains all of the application code.

Both the frontend UI and the backend server code are packaged into a single WAR file.

The frontend UI is built with Node.js, NPM, and Gulp, executed via frontend-maven-plugin.

## Swagger API
The internal API used for communication between the UI and backend is defined in src/main/webapp/api/swagger.json.

### Generating the API classes
The API classes are generated during the maven generate-sources phase and are not delivered to source control.

If you're developing in Eclipse, this step is not automatically run as part of the incremental build.
You need to manually run generate-sources after modifying swagger.json or accepting changes to swagger.json.

### Modifying the API
Open src/main/webapp/api/doc/swagger.json.

You can edit this file directly if you like.

You can also edit it as YAML by going to [http://editor.swagger.io/](http://editor.swagger.io/) and selecting File > Paste JSON...  
When you are done editing, select File > Download JSON and save the new file to src/main/webapp/api/doc/swagger.json.
Be sure to run the maven generate-sources goal to generate the new API classes.
