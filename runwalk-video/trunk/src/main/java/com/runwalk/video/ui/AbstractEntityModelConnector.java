package com.runwalk.video.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.EventListener;

import ca.odell.glazedlists.ObservableElementList;
import ca.odell.glazedlists.ObservableElementList.Connector;

import com.runwalk.video.entities.SerializableEntity;
import com.runwalk.video.model.AbstractEntityModel;

public class AbstractEntityModelConnector<T extends AbstractEntityModel<? extends SerializableEntity<?>>> implements Connector<T> {

	/** The list which contains the elements being observed via this {@link ObservableElementList.Connector}. */
	private ObservableElementList<? extends T> list;

	/** The PropertyChangeListener to install on each list element. */
	protected final PropertyChangeListener propertyChangeListener = createPropertyChangeListener();

	public EventListener installListener(T element) {
		element.addPropertyChangeListener(propertyChangeListener);
		return propertyChangeListener;
	}

	public void uninstallListener(T element, EventListener listener) {
		element.removePropertyChangeListener(propertyChangeListener);
	}

	public void setObservableElementList(ObservableElementList<? extends T> list) {
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
		
		public void propertyChange(PropertyChangeEvent event) {
			AbstractEntityModel<?> sourceModel = null;
			sourceModel = (AbstractEntityModel<?>) event.getSource();
			sourceModel.setDirty(true);
			list.elementChanged(sourceModel);
		}
		
	}

}
