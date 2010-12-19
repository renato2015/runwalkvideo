package com.runwalk.video.test;

import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;

import junit.framework.TestCase;

import org.junit.Ignore;

import ca.odell.glazedlists.CollectionList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.ObservableElementList;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.ObservableElementList.Connector;
import ca.odell.glazedlists.impl.beans.BeanConnector;
import ca.odell.glazedlists.matchers.Matcher;
import ca.odell.glazedlists.matchers.SearchEngineTextMatcherEditor;
import ca.odell.glazedlists.swing.EventSelectionModel;

import com.runwalk.video.entities.Analysis;
import com.runwalk.video.entities.Client;
import com.runwalk.video.entities.Recording;
import com.runwalk.video.entities.RecordingStatus;
import com.runwalk.video.gui.AnalysisConnector;

public class ListRemovalTestCase extends TestCase {

	@Ignore
	public void testListRemoval() throws Exception {
		//invoke code on the EDT, to avoid concurrency issues when building a GUI
		SwingUtilities.invokeAndWait(new Runnable() {

			public void run() {
				//in my normal code this part populates the swing gui, that's why I invoke it from the EDT
				//create the pipeline for list 1
				EventList<Client> clientList = GlazedLists.eventList(new ArrayList<Client>());
				clientList = createList(clientList, new BeanConnector<Client>(Client.class));
				//create list 1 itself, which will be the source list for both list 2 and list 3
				FilterList<Client> filteredClients = new FilterList<Client>(clientList);
				SearchEngineTextMatcherEditor<Client> clientFilterer = new SearchEngineTextMatcherEditor<Client>();
				filteredClients.setMatcherEditor(clientFilterer);
				//create a SelectionModel for list 1
				EventSelectionModel<Client> clientSelectionModel = new EventSelectionModel<Client>(filteredClients);

				//Create the pipeline for list 2
				final EventList<Client> selectedClients = clientSelectionModel.getSelected();
				CollectionList<Client, Analysis> selectedClientAnalyses = new CollectionList<Client, Analysis>(selectedClients, new CollectionList.Model<Client, Analysis>() {

					public List<Analysis> getChildren(Client parent) {
						return parent.getAnalyses();
					}

					@Override
					public String toString() {
						return "selectedAnalyses";
					}

				});
				//create list 2 itself, which will show the Analyses for the selected client according to its SelectionModel
				EventList<Analysis> sortedSelectedClientAnalyses = createList(selectedClientAnalyses, new AnalysisConnector());
				
				//create the pipline for list 3, which will show analyses matching some criteria
				//direct operations will be performed on this list, it just reponds to changes in the other two
				
				final CollectionList<Client, Analysis> analysesOverview = new CollectionList<Client, Analysis>(filteredClients, new CollectionList.Model<Client, Analysis>() {

					public List<Analysis> getChildren(Client parent) {
						return parent.getAnalyses();
					}

					@Override
					public String toString() {
						return "analysesInOverview";
					}

				});
				
				EventList<Analysis> sortedAnalysisOverview = createList(analysesOverview, new AnalysisConnector());
				//Create list 3 itself
				FilterList<Analysis> filteredAnalysisOverview = new FilterList<Analysis>(sortedAnalysisOverview, new Matcher<Analysis>() {

					public boolean matches(Analysis item) {
						for (Recording recording : item.getRecordings()) {
							if (recording.isCompressable()) {
								return true;
							}
						}
						return false;
					}
					
				});
				
				sortedSelectedClientAnalyses.getPublisher().setRelatedSubject(filteredClients, analysesOverview);
				
				//create a SelectionModel for list 2
				EventSelectionModel<Analysis> analysisSelectionModel = new EventSelectionModel<Analysis>(sortedSelectedClientAnalyses);
				//Create a new client with name, so hashcode and equals should be ok
				Client client = new Client("Peelaerts", "Jeroen");
				//add the client to the source list & selection
				filteredClients.add(client);
				
				//FIXME at this point one can only use the toggling selected in combination with a add to its source list
				//changing this with getSelected() doesn't actually add the item to the source list, in contrary to what the javadocs say.
				clientSelectionModel.getTogglingSelected().add(client);
				assertTrue(filteredClients.contains(client));
				assertTrue(clientSelectionModel.getSelected().contains(client));
				
				//Create an analysis for the selected client
				Analysis analysis = new Analysis(client);
				//Create a recording for the analysis
				Recording recording = new Recording(analysis);
				analysis.addRecording(recording);
				recording.setRecordingStatus(RecordingStatus.UNCOMPRESSED);

				//Add an analysis to the selected client, this will trigger a propertychangeevent due to the fired lastanalysisdate PCE in the client
				client.addAnalysis(analysis);

				//the selectedClientAnalyses should now contain the added analysis;
				assertTrue(sortedSelectedClientAnalyses.contains(analysis));
				//as the analysis has a compressable recording, the analysis overview list should contain it, too
				assertTrue(filteredAnalysisOverview.contains(analysis));
				//if the analysis is in the source list, add it to the selection
				analysisSelectionModel.getTogglingSelected().add(analysis);
				
				//now we are going to remove the added analysis from the client, firing a PCE because of the changing lastAnalysisDate
				//as with the add command, the list should update itself because its observable 
//				assertTrue(client.removeAnalysis(analysis));
				
				
				assertTrue(selectedClientAnalyses.remove(analysis));
//				filteredAnalysisOverview.remove(analysis);
				//FIXME firing a PCE on the first list throws an index of out bounds on the third list directly
//				client.removeAnalysis(analysis);
				//Check whether the analysis was removed ok from the client in the first list
				assertFalse(client.getAnalyses().contains(analysis));
				//Check whether the analysis was removed from the second list 2
				assertFalse(sortedSelectedClientAnalyses.contains(analysis));
				//FIXME the deleted analysis should be removed from the last list here.. throws OutOfBoundsException.. same exception as when firing the propertychangeevent
				assertFalse(filteredAnalysisOverview.contains(analysis));
			}
		}
		);
		
	}
	
	public <T extends Comparable<? super T>> EventList<T> createList(EventList<T> itemList, Connector<T> itemConnector) { 
		EventList<T> observedItems = new ObservableElementList<T>(itemList, itemConnector);
		SortedList<T> sortedItems = SortedList.create(observedItems);
		return sortedItems;
	}

}
