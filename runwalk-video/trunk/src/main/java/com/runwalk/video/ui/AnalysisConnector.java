package com.runwalk.video.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.EventListener;

import ca.odell.glazedlists.ObservableElementList;
import ca.odell.glazedlists.ObservableElementList.Connector;

import com.google.common.collect.Iterables;
import com.runwalk.video.entities.Analysis;
import com.runwalk.video.entities.Recording;
import com.runwalk.video.entities.RecordingStatus;

public class AnalysisConnector implements Connector<Analysis> {

	/** The list which contains the elements being observed via this {@link ObservableElementList.Connector}. */
	private ObservableElementList<? extends Analysis> list;

	/** The PropertyChangeListener to install on each list element. */
	protected final PropertyChangeListener propertyChangeListener = createPropertyChangeListener();

	public EventListener installListener(Analysis element) {
		element.addPropertyChangeListener(propertyChangeListener);
		for (Recording recording : element.getRecordings()) {
			recording.addPropertyChangeListener(propertyChangeListener);
		}
		return this.propertyChangeListener;
	}

	public void uninstallListener(Analysis element, EventListener listener) {
		element.removePropertyChangeListener(propertyChangeListener);
		for (Recording recording : element.getRecordings()) {
			recording.removePropertyChangeListener(propertyChangeListener);
		}
	}

	public void setObservableElementList(ObservableElementList<? extends Analysis> list) {
		this.list = list;
	}

	/**
	 * A local factory method to produce the PropertyChangeListener which will
	 * be installed on list elements.
	 */
	protected PropertyChangeListener createPropertyChangeListener() {
		return new PropertyChangeHandler();
	}

	/**
	 * This inner class notifies the {@link ObservableElementList} about changes to list elements.
	 */
	public class PropertyChangeHandler implements PropertyChangeListener {
		@SuppressWarnings("unchecked")
		public void propertyChange(PropertyChangeEvent event) {
			Analysis analysis = null;
			boolean dirty = true;
			if (event.getSource() instanceof Recording) {
				analysis = ((Recording) event.getSource()).getAnalysis();
				if (event.getPropertyName().equals(Recording.RECORDING_STATUS)) {
					RecordingStatus newState = (RecordingStatus) event.getNewValue();
					// events that set a recording's state to an erroneous state should not make it too dirty
					dirty = !newState.isErroneous();
				}
			} else {
				analysis = (Analysis) event.getSource();
				if (event.getPropertyName().equals(Analysis.RECORDING_COUNT)) {
					// a recording was added, listen for changes..
					Recording lastRecording = Iterables.getLast(analysis.getRecordings());
					lastRecording.addPropertyChangeListener(propertyChangeListener);
				}
			}
			analysis.getClient().setDirty(dirty);
			((ObservableElementList<Analysis>) list).elementChanged(analysis);
		}
	}

}
