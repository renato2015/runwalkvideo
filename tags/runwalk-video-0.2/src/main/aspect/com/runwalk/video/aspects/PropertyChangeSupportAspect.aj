package com.runwalk.video.aspects;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.event.SwingPropertyChangeSupport;

import org.jdesktop.application.AbstractBean;

import com.runwalk.video.entities.SerializableEntity;
import com.runwalk.video.ui.PropertyChangeSupport;

/**
 * This aspect will weave {@link SwingPropertyChangeSupport} into all {@link SerializableEntity}'s without requiring them to inherit
 * from any specific class. Most of this code was taken from the {@link AbstractBean} class of the BSAF project, and only differs in
 * the fact that the change support object itself is made transient here.
 * 
 * @author Jeroen Peelaerts
 *
 */
public aspect PropertyChangeSupportAspect {
	
	declare parents: SerializableEntity<?> implements PropertyChangeSupport;
	
	private transient final SwingPropertyChangeSupport PropertyChangeSupport.pcs = new SwingPropertyChangeSupport(this, true);

    /**
     * Add a PropertyChangeListener to the listener list.
     * The listener is registered for all properties and its 
     * {@code propertyChange} method will run on the event dispatching
     * thread.
     * <p>
     * If {@code listener} is null, no exception is thrown and no action
     * is taken.
     *
     * @param listener the PropertyChangeListener to be added.
     * @see #removePropertyChangeListener
     * @see java.beans.PropertyChangeSupport#addPropertyChangeListener
     */
    public void PropertyChangeSupport.addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    /**
     * Remove a PropertyChangeListener from the listener list.
     * <p>
     * If {@code listener} is null, no exception is thrown and no action
     * is taken.
     *
     * @param listener the PropertyChangeListener to be removed.
     * @see #addPropertyChangeListener
     * @see java.beans.PropertyChangeSupport#removePropertyChangeListener
     */
    public void PropertyChangeSupport.removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    /**
     * Add a PropertyChangeListener for a specific property.  The listener
     * will be invoked only when a call on firePropertyChange names that
     * specific property.
     * The same listener object may be added more than once.  For each
     * property,  the listener will be invoked the number of times it was added
     * for that property.
     * If <code>propertyName</code> or <code>listener</code> is null, no
     * exception is thrown and no action is taken.
     *
     * @param propertyName  The name of the property to listen on.
     * @param listener  the PropertyChangeListener to be added
     * @see java.beans.PropertyChangeSupport#addPropertyChangeListener(String, PropertyChangeListener)
     */
    public void PropertyChangeSupport.addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(propertyName, listener);
    }

    /**
     * Remove a PropertyChangeListener for a specific property.
     * If <code>listener</code> was added more than once to the same event
     * source for the specified property, it will be notified one less time
     * after being removed.
     * If <code>propertyName</code> is null,  no exception is thrown and no
     * action is taken.
     * If <code>listener</code> is null, or was never added for the specified
     * property, no exception is thrown and no action is taken.
     *
     * @param propertyName  The name of the property that was listened on.
     * @param listener  The PropertyChangeListener to be removed
     * @see java.beans.PropertyChangeSupport#removePropertyChangeListener(String, PropertyChangeListener)
     */
    public synchronized void PropertyChangeSupport.removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(propertyName, listener);
    }

    /**
     * An array of all of the {@code PropertyChangeListeners} added so far.
     * 
     * @return all of the {@code PropertyChangeListeners} added so far.
     * @see java.beans.PropertyChangeSupport#getPropertyChangeListeners
     */
    public PropertyChangeListener[] PropertyChangeSupport.getPropertyChangeListeners() {
        return pcs.getPropertyChangeListeners();
    }

    /**
     * Called whenever the value of a bound property is set.
     * <p>
     * If oldValue is not equal to newValue, invoke the {@code
     * propertyChange} method on all of the {@code
     * PropertyChangeListeners} added so far, on the event
     * dispatching thread.
     * 
     * @see #addPropertyChangeListener
     * @see #removePropertyChangeListener
     * @see java.beans.PropertyChangeSupport#firePropertyChange(String, Object, Object)
     */
    public void PropertyChangeSupport.firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        if (oldValue != null && newValue != null && oldValue.equals(newValue)) {
            return;
        }
        pcs.firePropertyChange(propertyName, oldValue, newValue);
    }

    /**
     * Fire an existing PropertyChangeEvent 
     * <p>
     * If the event's oldValue property is not equal to newValue, 
     * invoke the {@code propertyChange} method on all of the {@code
     * PropertyChangeListeners} added so far, on the event
     * dispatching thread.
     * 
     * @see #addPropertyChangeListener
     * @see #removePropertyChangeListener
     * @see java.beans.PropertyChangeSupport#firePropertyChange(PropertyChangeEvent e)
     */
    public void PropertyChangeSupport.firePropertyChange(PropertyChangeEvent e) {
        pcs.firePropertyChange(e);
    }
}
