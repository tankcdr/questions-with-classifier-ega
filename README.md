# Natural Language Classifier Engagement Gallery Application Overview
The Natural Language Classifier Engagement Gallery Application uses the Watson Natural Language Classifier Service to show how to build a question-and-answer application that uses minimal ground truth and to demonstrate some best practices for using the service.

To function correctly this application requires the following items:

  * A trained classifier.
  * A populated answer store.
  * Training data and answer data. Samples of these data types are provided.

Complete the following instructions to set up these items.

***

## Before you begin
Ensure that you have the following prerequisites before you start:
  * You need an IBM Bluemix account. If you don't have one, [sign up](https://apps.admin.ibmcloud.com/manage/trial/bluemix.html?cm_mmc=WatsonDeveloperCloud-_-LandingSiteGetStarted-_-x-_-CreateAnAccountOnBluemixCLI). For more information about the process, see [Developing Watson applications with Bluemix](http://www.ibm.com/smarterplanet/us/en/ibmwatson/developercloud/doc/getting_started/gs-bluemix.shtml).
  * [Java Development Kit](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
  * [Apache Maven](https://maven.apache.org/download.cgi) 3.1 or later releases
  * [Git](https://git-scm.com/downloads)
  
***

## Stages
To get started, complete each of the following stages in order:
  1. [Clone the app and framework repositories from GitHub and build them](#stage-1-clone-the-app-and-framework-repositories-from-GitHub-and-build-them)
  2. [Set up and deploy the application in BlueMix](#stage-2-set-up-and-deploy-the-application-in-bluemix)
  3. [Determine the data you want to use](#stage-3-determine-the-data-you-want-to-use)
  4. [Train the classifier](#stage-4-train-the-classifier)
  5. [Populate the answer store](#stage-5-populate-the-answer-store)

***

### Stage 1: Clone the app and framework repositories from GitHub and build them
In this stage, download the source code for the app and build it.

  1. Clone the framework-ega repository by issuing one of the following commands:
     ```
     git clone https://github.com/watson-developer-cloud/framework-ega.git
     ```
     ```
     git clone git@github.com:watson-developer-cloud/framework-ega.git
     ```

  2. Run `mvn install` in the root of the framework-ega repository to build and install the components to your local Maven repository.
  3. Clone the questions-with-classifier-ega repository by issuing one of the following commands:
     ```
     git clone https://github.com/watson-developer-cloud/questions-with-classifier-ega.git
     ```
     ```
     git clone git@github.com:watson-developer-cloud/questions-with-classifier-ega.git
     ```
        
  4. Run `mvn install` in the root of the questions-with-classifier-ega repository to build and install the components to your local Maven repository.

  The `questions-with-classifier-ega-war.war` file is in the `/questions-with-classifier-ega/questions-with-classifier-ega-war/target` directory.

**Note: Once in public github the framework-ega dependencies will be in maven central and step #1 and #2 will no longer be necessary**

***

### Stage 2: Set up and deploy the application in Bluemix
In this stage, create your application in Bluemix, bind the necessary services to it, and deploy the application code that you built in Stage 1.
  1. [Log in to Bluemix](https://console.ng.bluemix.net/) and navigate to the Dashboard.
  2. Create your app.
	  1. Click **CREATE AN APP**.
	  2. Select **WEB**.
      3. Select the starter **Liberty for Java**, and click **CONTINUE**.
      4. Type a unique name for your app, such as `qaclassifier-sample-app`, and click **Finish**.
      5. Select **CF Command Line Interface**. If you do not already have it, click **Download CF Command Line Interface** and install it.
      6. Click **OVERVIEW**.
  3. Add the Natural Language Classifier service to your app. To use an instance of the service that is bound to another app, skip this step.
      1. Click **ADD A SERVICE OR API**. 
      2. Select the **Watson** category, and select the **Natural Language Classifier** service.
      3. Ensure that your app is specified in the **App** dropdown.
      4. In the **Service name** field, type a unique name for your service, such as `qaclassifier-sample-classifier`.
      5. Click **CREATE**. The **Restage Application** window is displayed. 
      6. Click **RESTAGE** to restage your app.
  4. Add SQL database service to your app. To use an instance of the service that is bound to another app, skip this step.
      1. Click **ADD A SERVICE OR API**.
      2. Select the **Data Management** category, and select the **SQL Database** service.
      3. Ensure that your app is specified in the **App** dropdown.
      4. In the **Service name** field, type a unique name for your service, such as `qaclassifier-sample-db`.
      5. Click **CREATE**. The **Restage Application** window is displayed. 
      6. Click **RESTAGE** to restage your app.
  5. Bind instances of services to your app. If this step is not applicable, skip it.
      1. Click **BIND A SERVICE OR API**.
	  2. Select the services that you want to bind to your app, and click **ADD**. The **Restage Application** window is displayed. 
	  3. Click **RESTAGE** to restage your app.
  6. Deploy the application code that you built in Stage 1 by using the Cloud Foundry commands.
      1. Open the Command Prompt.
      2. Navigate to the directory that contains the WAR file you that you generated in Stage 1 by running the following command:
      
        ```
        cd /questions-with-classifier-ega/questions-with-classifier-ega-war/target
        ```
      3. Connect to Bluemix by running the following command:
      
        ```
        cf api https://api.ng.bluemix.net
        ```
      4. Log in to Bluemix by running the following command. Replace <yourUsername> with your Bluemix id, <yourOrg> with your organization name, and <yourSpace> with your space name.
      
        ```
        cf login -u <yourUsername> -o <yourOrg> -s <yourSpace>
        ```
      5. Deploy the app to Bluemix by running the following command. Replace <yourAppName> with the name of your app.
        
        ```
        cf push <yourAppName> -p questions-with-classifier-ega-war.war
        ```
  7. If the app is not started, click **START**.
  8. To view the home page of the app, open (https://*yourAppName*.mybluemix.net), where *yourAppname* is the specific name of your app. 
  
**The app and its bound services are deployed. However, you must complete the following stages for the app to function correctly.**

If you want to secure the answer store endpoints, see [Deploying with security](#deploying-with-security).
***

### Stage 3: Choose which data you want to use
In this stage, understand the types of data that the app requires and choose whether you want to use sample data or your own data. 

The app requires a trained classifier to work properly. To train the classifier, you need training data, which maps a question to a class. The question-to-class mapping must be in JSON format.
    
    question --> class

Additionally, the app requires a populated answer store. The answer store is populated by answers data, which maps the classes from the training data to answers. The class-to-answer mapping must be in JSON format.
    
    class --> answer
	
Choose whether you want to use sample data or your own data.

#### **Sample data**
The sample data is in the `training.json` and `answers.json` files in the `questions-with-classifier-ega-war > src > main > resources` directory. To use the sample data, go to [Stage 4: Train the classifier](#stage-4-train-the-classifier). 
  
#### **Your own data**
To use your own data instead of the sample data, see [Prepare your own data for training the classifier and populating the answer store](#prepare-your-own-data-for-training-the-classifier-and-populating-the-answer-store).

***

### Stage 4: Train the classifier
In this stage, train the classifier by using curl. To train the classifier in Eclipse, see [Training the classifier in Eclipse](#training-the-classifier-in-eclipse).

For more information about training the classifier, see the [Classifier API](https://watson.stage1.mybluemix.net/apis/#!/natural-language-classifier).

  1. [Log in to Bluemix](https://console.ng.bluemix.net/) and navigate to your app.
  2. Click **Show Credentials** for the Natural Language Classifier service that is bound to your app.
  3. Copy the values of the `url`, `username`, and `password` parameters in the `**credentials**` section.
  4. From a command prompt, run the following curl command. Replace `<username>`, `<password>`, and `<url>` with the credentials you copied. The train.csv file is assumed to be in the directory from which you run the command. If necessary, change the path to the file.
  
    `curl -u <username>:<password> -F training_data=@train.csv -F training_metadata="{\"language\":\"en\",\"name\":\"my_classifier\"}" "https://<url>/v1/classifiers"`

***

### Stage 5: Populate the answer store
In this stage, populate the answer store by using curl. To populate the answer store in Eclipse, see [Populating the answer store in Eclipse](#populating-the-answer-store-in-eclipse)

To see the API that populates the answer store, open https://yourAppName.mybluemix.net/api, and see **Manage**.

  1. Ensure that your app is running. If it's not running, open your app in Bluemix and click **START**.
  2. Ensure that your answers.json file matches your training.json file.
  3. From a command prompt, run the following curl command. The answers.json file is assumed to be in the directory from which you run the command. If necessary, change the path to the file.

    `curl -X POST -H "Content-Type: application/json" -d @answers.json http://yourAppName.mybluemix.net/api/v1/manage/answer`

### What to do next

  * Celebrate! You successfully built an app that uses a trained classifier. To see it live, open https://*yourAppName*.mybluemix.net, where *yourAppName* is the specific name of your app.
  * Explore the Advanced development section to learn how to use your own data and how to use Eclipse to train the classifier and populate the answer store.

***

## Advanced development
Use the following information to use Eclipse for training the classifier and populating the answer store and to train a classifier on your own data.

### Training the classifier in Eclipse
If you have loaded the code into Eclipse, you can use an included main class to help with training the Classifier. The program makes REST API calls to the [Classifier API](https://watson.stage1.mybluemix.net/apis/#!/natural-language-classifier), which can be done through any REST client. Although you can train multiple classifier instances, the app does not provide a way to specify which instance to use. By design, the app asks for a list of instances and selects the the first instance in the list. To ensure that you are using the correct instance for your app, train and keep only one instance at a time.

  1. Locate the `training.json` file. You can use one of the following files:
    * The file that you generated during the [Prepare your own data for training the classifier and populating the answer store](#prepare-your-own-data-for-training-the-classifier-and-populating-the-answer-store) process.
    * The sample file in the `questions-with-classifier-ega-war > src > main > resources` directory.
  2. Run the **TrainClassifier.java** command-line program. Use the following parameters:
  
    ```
       usage: java com.ibm.watson.app.common.tools.services.classifier.TrainClassifier
         -d,--delete                If specified, the classifier instance will be deleted if training is not successful
         -f,--file <file>           The filepath to be used as training data
         -l,--url <url>             The absolute URL of the NL classifier service to connect to. If omitted, the default will be
                                    used (https://gateway-d.watsonplatform.net/natural-language-classifier-alpha/api)
         -p,--password <password>   The password to use during authentication to the NL classifier service
         -u,--username <username>   The username to use during authentication to the NL classifier service
    ```
    
    You can run the program without specifying a file, or you can specify a file, and the program will launch a training instance. To see all of the commands, type `h` for help.

***

### Populating the answer store in Eclipse
If you have loaded the code into Eclipse, you can use an included main class to help with populating the answer store. The program makes a REST call to the same /manage/answer API that the curl command uses.
  1. Locate the `answers.json` file. You can use one of the following files:
    * The file that you generated during the [Prepare your own data for training the classifier and populating the answer store](#prepare-your-own-data-for-training-the-classifier-and-populating-the-answer-store) process.
    * The sample file in the `questions-with-classifier-ega-war > src > main > resources` directory.
  2. Run the **PopulateAnswerStore.java** command-line program. Use the following parameters:
  
    ```
       usage: java com.ibm.watson.app.qaclassifier.tools.PopulateAnswerStore
         -l,--url <url>     The root URL of the application to connect to. If omitted, the default will be used
                            (http://localhost:9080)
         -p,--path <path>   The path to be used as training data, can point to the file system or the class path
    ```

The program checks the answer store for existing entries before it adds new entries. If existing entries are found, the program stops.

***

### Prepare your own data for training the classifier and populating the answer store
Use the following information to train the classifier on your own data.

A command-line program that trains a classifier in included in the app. It does the following tasks:
  * Generates the `training.json` file for training the classifier.
  * Generates the `answers.json` file for populating the answer store.  

After these files are generated, you replace the sample .json files with them.

**Prerequisites**

The following information assumes that you have collected and curated ground truth for the application. To use this data in the app, the data must be in a specific format, and it must contain all of the following elements:
  * Representative questions.
  * Answers to all representative questions.
  * A unique associated class name for each answer.
  * An associated canonical question for each answer. The canonical question can be an actual question or a paraphrase of one.
  * An answer associated by a unique class name with each question.

The methodology for acquiring this data and ensuring that it meets the requirements is outside of the scope of these instructions.

  1. Generate a CSV file for questions. The command-line program uses a .csv file as input to create the training JSON file to be uploaded to the classifier by using the REST API. Use the following format in the questions.csv: QuestionText, LabelId.

    | Term  | Description |
    | ------------- | ------------- |
    | QuestionText  | The text of the question.  |
    | LabelId  | The unique id of the class that a question corresponds to. It matches the LabelId in the answers file.  |  

  2. Generate a CSV file for answers. When you call the 'classify' REST API and pass a question or text string, the classifier responds with a list of classes that are best associated with that text string based on its training data and algorithms. If you want to show the user an answer for those classes, you must associate each class with some answer text. The command-line program uses a .csv file as input to create and populate an answer store. Use the following format in the answers.csv: LabelId, AnswerValue, CanonicalQuestion.

    | Term  | Description |
    | ------------- | ------------- |
    | LabelId  | The unique id of the label for an answer. It matches the LabelId in the questions file.  |
    | AnswerValue  | The text of the answer.  |
    | CanonicalQuestion  | The canonical question that is associated with an answer.  |  

     Classes that have no answer value are excluded and are not added to the answer store.

  3. Generate your own training and answers JSON files.

     A command-line program creates a training JSON file and an answers JSON file that you can use in the previous stages for training the classifier and populating the answer store. In the `com.ibm.watson.app.classifier.tools` package of the `questions-with-classifier-ega-war` project, a class PopulateAnswerStore.java can be run and supplied with the .csv input files.  Use the following command-line parameters for this program:
	
	```
       usage: java com.ibm.watson.app.classifier.tools.GenerateTrainingAndPopulationData
        -ain,--answerInput <answerInput>          input csv file containing answers data
        -aout,--answerOutput <answerOutput>       filename and location for the answer store population data
        -qin,--questionInput <questionInput>      input csv file containing questions and labels
        -qout,--questionOutput <questionOutput>   filename and location for the classifier training data
	```  
  4. Replace the sample JSON files with your own files. The sample `answers.json` and `training.json` files are in the `questions-with-classifier-ega-war > src > main > resources` directory.

**What to do next**

Use your JSON files to train the classifier and populate the answer store.
  * Use curl commands
	* [Train the classifier by using curl](#stage-4-train-the-classifier)
	* [Populate the answer store by using curl](#stage-5-populate-the-answer-store)
  * Use Eclipse
    * [Train the classifier in Eclipse](#training-the-classifier-in-eclipse)
	* [Populate the answer store in Eclipse](#populating-the-answer-store)

***

### Deploying with security
If you completed the previous stages, the app is deployed without security around any of the internal API endpoints. No security is acceptable for development but not for production, particularly for the `/manage` endpoint, which allows access to the answer store.

To deploy the app with security enabled, complete these steps:
1. Find the .zip file that contains the .war and a server.xml with security configured by running the following command:

    ```
    cd /questions-with-classifier-ega/questions-with-classifier-ega-war/target
    ```
2. Re-run the following `cf push` command:

    ```
    cf push <yourAppName> -p questions-with-classifier-ega-war.war
    ```

3. In your Bluemix application, define the following environment variable and set the value to the password you want:

    ```
    MANAGE_API_PASSWORD
    ```