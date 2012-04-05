package com.runwalk.video.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.EventListener;

import ca.odell.glazedlists.ObservableElementList;
import ca.odell.glazedlists.ObservableElementList.Connector;

import com.runwalk.video.entities.CalendarSlot;
import com.runwalk.video.entities.RedcordExercise;
import com.runwalk.video.entities.RedcordTableElement;

public class RedcordTableElementConnector implements Connector<RedcordTableElement> {

	/** The list which contains the elements being observed via this {@link ObservableElementList.Connector}. */
	private ObservableElementList<? extends RedcordTableElement> list;

	/** The PropertyChangeListener to install on each list element. */
	protected final PropertyChangeListener propertyChangeListener = createPropertyChangeListener();

	public EventListener installListener(RedcordTableElement element) {
		element.addPropertyChangeListener(propertyChangeListener);
		return propertyChangeListener;
	}

	public void uninstallListener(RedcordTableElement element, EventListener listener) {
		element.removePropertyChangeListener(propertyChangeListener);
	}

	public void setObservableElementList(ObservableElementList<? extends RedcordTableElement> list) {
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
		
		@SuppressWarnings({ "rawtypes", "unchecked" })
		public void propertyChange(PropertyChangeEvent event) {
			if (event.getSource() instanceof RedcordTableElement) {
				CalendarSlot<RedcordTableElement> redcordSession = null;
				if (event.getSource() instanceof RedcordExercise) {
					redcordSession = ((RedcordExercise) event.getSource()).getRedcordSession();
				} else {
					redcordSession = (CalendarSlot<RedcordTableElement>) event.getSource();
				}
				redcordSession.getClient().setDirty(true);
				((ObservableElementList) list).elementChanged(event.getSource());
			}
		}
	}
	
}
