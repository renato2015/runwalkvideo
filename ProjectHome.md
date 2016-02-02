# Introduction #

runwalk-video is a project started in 2008, developed in Java (Swing) It allows you to track customer info and record video for performing gait analysis. Currently this application is used in a setup, where clients are filmed in order to improve the process of insole creation and shoe recommendation.

Our setup consists of two workstations which each have two monitors connected. One monitor is used to control video recording and customer data input, while the second monitor is a 42" showing a live camera image of our hallway (12m). After a recording is made, it is replayed in slow motion. Here we use a [UI-5240SE-C](http://www.ids-imaging.com/frontend/products.php?cam_id=130) camera, which provides us about 50fps at a resolution of 720x1280, compressed in MJPEG by the camera's driver.

**0.5** is being devleoped now, and is stable enough for daily use.

# Features #
  * Manage client info and persist to database
  * Work in a distributed fashion (one workstation for recording, one for playback)
  * Create analyses for each patient and index them by date
  * Link articles (eg. created insole type) to each analysis' outcome
  * Capture video from different (high speed) capture devices (DirectShow & uEye cameras)
  * Open and play back captured video in slow motion
  * Add keyframes to recordings highlighting an interesting frame
  * Manage recordings easily by creating custom directory structures
  * Find and remove duplicate recordings
  * Compress recordings (requires dsj) using a configurable encoder
  * Deploy using Java Webstart, which enables you to manage updates of new versions easily
  * Works on Windows 32 and 64bit architectures
  * Optimized threading design to work well with newer multi core processors
  * Complete decoupling of the video subsystem implementation. You will need to configure (in an external settings XML file) which implementation you would like to use for both video capture and playback
  * English translations for clients & analyses **NEW in 0.5**

# Screenshot #

![http://www.runwalk.be/uploads/media/screencap.png](http://www.runwalk.be/uploads/media/screencap.png)

# Project installation #

## ueye-nativelib ##

  * To get uEye camera support, one will need to check out the ueye-nativelib subproject, as well, and build the solution using Visual Studio 2010.
  * Therefore, you will need to install the uEye 4.30 drivers, available from [IDS](http://www.ids-imaging.com).
  * After building the DLLs for both the 32 and 64bit arhitecture, one can simply run the mvn package goal from the subproject's root directory. This will create the jarred native artifacts that can be used by the maven build of the runwalk-video subproject.
  * Deploy the created jar files to your local repository using the mvn deploy:deploy-file command. The exact arguments for this command can be found in the pom.xml project declaration.
  * If you want to use the uEye drivers without Visual Studio 2010 installed, then you will need to install the Visual Studio 2010 binary redistributable package (vcredist.exe). This package contains DLLs needed to run applications developed with VC++.

## runwalk-video ##

  * To get the project compiled and running, check out the latest **0.5** in the runwalk-video subproject.
  * Install the AJDT and m2e plugin for Eclipse. After installing m2e, install the aspectj-connector for m2e. If this is not properly installed, Eclipse will give you a lifecycle mapping exception which will prevent you from building the project.
  * The Maven build system is used for project compilation. User version 2.2.1 (3.0 support will be there for 0.4). Firstly, you will need to take a look at the properties in the pom.xml file and define them according to your configuration
  * Once you have all the properties set in the pom.xml, Fire up a prompt in the project  directory and hit 'mvn install'
  * Maven will try to package the build into a Webstart application. Therefore I had to customize the maven-webstart-plugin to [support](http://jira.codehaus.org/browse/MWEBSTART-8) the deployment of native libraries. Patches for this issue were posted in the aformentioned JIRA issue and exports the list of dependencies with a minimal modification to the code to the velocity context. If you don't need Webstart, just remove this plugin from the build.
  * You will probably find that some dependencies won't be resolved because of license restrictions. To resolve this culprit, download and deploy the missing artifacts to your own local repository using mvn deploy:deploy-file and start working from there
    * Libraries to be deployed are: dsj, [scrollabledesktop 1.01](http://www.javaworld.com/javaworld/jw-11-2001/jw-1130-jscroll.html)
    * Note: Three unlock codes aze required for DSJ to work, get them at their website.
  * After the first startup, a settings file will be created in %APPDATA%/Runwalk/Runwalk Herentals. There you can set your db credentials.
  * By default there will be no implementation available. Edit the xml file and add the fully qualified name of your video capturer factory implementation of choice
  * Once connected to your databaze, the DDL for the tables can be [generated](http://stackoverflow.com/questions/779479/reverse-engineer-ddl-from-jpa-entities) using EclipseLink.

# System Requirements #

  * Capturing uncompressed video requires you to have fast disk access: An SSD or RAID0 disk setup can speed things up a bit here
  * The application can be deployed using Java Webstart, so it can easily be installed on a remote ftp server from where it is downloaded and installed on a client PC. Just hit mvn -Premote deploy from  a console opened in the root of the runwalk-video directory.
    * NOTE: to deploy to ftp, you need to have the server credentials configured in the maven settings.xml file
  * The core functionality was tested on OSX, however no video capture or playback functionality is available there

# Technologies used #

## Data Persistence ##

The application uses JPA (Eclipselink) as persistence framework. The application has been developed with MySQL as database, but care has been taken to not use any specific MySQL functionality there. Using a different database should be no problem, although this has never been tested before. The persistence implementation has been optimized to be able to work with remote databases, hosted on the internet.

Using JPA together with Swing is not really an easy combination. For further reference one can read [this](http://blog.ringerc.id.au/2010/07/jpa-and-hibernateeclipselinkopenjpaetc.html) and [this](http://blog.schauderhaft.de/2008/09/28/hibernate-sessions-in-two-tier-rich-client-applications/) article.

## User Interface ##

The application uses Java Swing in combination with the [bsaf](http://kenai.com/projects/bsaf/pages/Home) and [better beans bindings](http://kenai.com/projects/betterbeansbinding/pages/Home) for data binding. It's user interface consists of a master detail list pipeline. The excellent [GlazedLists](http://www.glazedlists.com/) library helped to solve some of the complexities involved here

## Video Capturing & Playback ##

Currently capturing video is only supported on Windows.

Implementations for different frameworks can be added easily. At this moment, we provide implementations for
  * directshow ([dsj](http://www.humatic.de/htools/dsj.htm)) and
  * [uEye sdk](http://www.ids-imaging.com/) integration using [jna](http://jna.java.net/).

By default, any camera that is working with either of the two frameworks can be used to capture video.
