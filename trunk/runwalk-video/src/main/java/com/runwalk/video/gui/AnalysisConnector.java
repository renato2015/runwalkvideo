package com.runwalk.video.gui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.EventListener;

import ca.odell.glazedlists.ObservableElementList;
import ca.odell.glazedlists.ObservableElementList.Connector;

import com.runwalk.video.entities.Analysis;
import com.runwalk.video.entities.Recording;

public class AnalysisConnector implements Connector<Analysis> {
	
    /** The list which contains the elements being observed via this {@link ObservableElementList.Connector}. */
    private ObservableElementList<? extends Analysis> list;

    /** The PropertyChangeListener to install on each list element. */
    protected PropertyChangeListener propertyChangeListener = this.createPropertyChangeListener();
    	
	public EventListener installListener(Analysis element) {
		element.addPropertyChangeListener(propertyChangeListener);
		if (element.getRecording() != null) {
			element.getRecording().addPropertyChangeListener(propertyChangeListener);
		}
		return this.propertyChangeListener;
	}

	public void uninstallListener(Analysis element, EventListener listener) {
		element.removePropertyChangeListener(propertyChangeListener);
		if (element.getRecording() != null) {
			element.getRecording().removePropertyChangeListener(propertyChangeListener);
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
        	Object eventSource = event.getSource();
        	if (event.getSource() instanceof Recording) {
        		eventSource = ((Recording) eventSource).getAnalysis();
        	}
        	((ObservableElementList) list).elementChanged(eventSource);
        }
    }

}
