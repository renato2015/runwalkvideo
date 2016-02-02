# Panel Subsystem Creation #

The main application class uses a ServiceLocator to load different app module instances on the classspath

  1. main app module => logic for ClientInfoPanel
  1. analysis app module => logic for AnalysisTablePanel and AnalysisOverviewTablePanel

  * An app module contains a method to initialize itself, with all the arguments possibly required there (DaoService, VideoFileManager, AppSettings, ClientTablePanel, main application JFrame)
  * For adding the table panels to the GUI, a simple getter can be used on the AppModule. This one should be called after initialisation has been done
  * app module can contain getters for other dynamically loadable components, as well, like eg. VideoCapturerFactory (and VideoPlayerFactory) implementations
  * The application will use the panels' ActionMaps and add the contained actions to the application's menu bar

## RefreshTask Refactoring ##

  * Pass a list of app module objects that implement a common refresh method. The refresh method will pass one argument: the client eventlist, from which other list can be derived
  * encapsulate logic in a specific DAO to hide its implementation from the Task itself

## Interface Description ##
  * an init method, passing the most commonly used arguments for initializing table panels and tasks
  * getters for tasks,
  * A getName(), which can be used to do a logical grouping of the actions in the module in the application's menu
  * A boolan getter, to check whether the menu added should be top level or added to the default file menu
  * a refresh() method, passing the client event list as an item list.

# Decouple Video Subsystem #

verwijderen van dependencies op DSJ in VideoFIleManager en CompressVideoFilesTask
uit (maak abstracties voor de common functionaliteit, compressie en duration)

  * VideoFileManager (voor getDuration)
  * CompressVideoFilesTask

# Decouple Image Analysis #
  * Voor elke implementatie kan een Action toegevoegd worden (Eerder direct stijl)