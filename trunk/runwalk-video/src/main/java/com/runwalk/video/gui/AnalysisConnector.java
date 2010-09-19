package com.runwalk.video.gui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.EventListener;

import ca.odell.glazedlists.ObservableElementList;
import ca.odell.glazedlists.ObservableElementList.Connector;

import com.google.common.collect.Iterables;
import com.runwalk.video.entities.Analysis;
import com.runwalk.video.entities.Recording;

public class AnalysisConnector implements Connector<Analysis> {

	/** The list which contains the elements being observed via this {@link ObservableElementList.Connector}. */
	private ObservableElementList<? extends Analysis> list;

	/** The PropertyChangeListener to install on each list element. */
	protected PropertyChangeListener propertyChangeListener = this.createPropertyChangeListener();

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
	 * The PropertyChangeListener which notifies the {@link ObservableElementList} within this
	 * Connector of changes to list elements.
	 */
	public class PropertyChangeHandler implements PropertyChangeListener {
		@SuppressWarnings("unchecked")
		public void propertyChange(PropertyChangeEvent event) {
			Analysis analysis = null;
			if (event.getSource() instanceof Recording) {
				analysis = ((Recording) event.getSource()).getAnalysis();
				analysis.getClient().setDirty(true);
			} else if (event.getPropertyName().equals(Analysis.RECORDING_COUNT)) {
				analysis = (Analysis) event.getSource();
				// a recording was added, listen for changes..
				Recording lastRecording = Iterables.getLast(analysis.getRecordings());
				lastRecording.addPropertyChangeListener(propertyChangeListener);
			}
			((ObservableElementList<Analysis>) list).elementChanged(analysis);
		}
	}

}
