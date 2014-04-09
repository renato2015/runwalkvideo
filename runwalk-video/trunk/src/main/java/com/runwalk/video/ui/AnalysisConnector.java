package com.runwalk.video.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.EventListener;

import ca.odell.glazedlists.ObservableElementList;
import ca.odell.glazedlists.ObservableElementList.Connector;

import com.runwalk.video.model.AnalysisModel;

public class AnalysisConnector implements Connector<AnalysisModel> {

	/** The list which contains the elements being observed via this {@link ObservableElementList.Connector}. */
	private ObservableElementList<? extends AnalysisModel> list;

	/** The PropertyChangeListener to install on each list element. */
	protected final PropertyChangeListener propertyChangeListener = createPropertyChangeListener();

	public EventListener installListener(AnalysisModel element) {
		element.addPropertyChangeListener(propertyChangeListener);
		return propertyChangeListener;
	}

	public void uninstallListener(AnalysisModel element, EventListener listener) {
		element.removePropertyChangeListener(propertyChangeListener);
	}

	public void setObservableElementList(ObservableElementList<? extends AnalysisModel> list) {
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
		@SuppressWarnings( "rawtypes" )
		public void propertyChange(PropertyChangeEvent event) {
			AnalysisModel analysisModel = null;
			analysisModel = (AnalysisModel) event.getSource();
			analysisModel.setDirty(true);
			((ObservableElementList) list).elementChanged(analysisModel);
		}
	}

}
