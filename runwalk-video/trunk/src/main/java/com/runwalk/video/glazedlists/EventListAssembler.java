package com.runwalk.video.glazedlists;

import java.util.Iterator;
import java.util.LinkedList;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.ObservableElementList;
import ca.odell.glazedlists.ObservableElementList.Connector;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.swing.DefaultEventSelectionModel;
import ca.odell.glazedlists.swing.DefaultEventTableModel;

import com.runwalk.video.entities.SerializableEntity;

public abstract class EventListAssembler<E extends EventList<T>, T extends SerializableEntity<? super T>> 
		implements Comparable<EventListAssembler<?, ?>> {
	
	private final LinkedList<EventList<T>> eventListQueue = new LinkedList<EventList<T>>();		
	
	private DefaultEventTableModel<T> eventTableModel;
	
	private DefaultEventSelectionModel<T> eventSelectionModel;
	
	protected EventListAssembler() { }
	
	public EventListAssembler(EventList<T> sourceList) {
		eventListQueue.add(sourceList);
	}
	
	protected LinkedList<EventList<T>> getEventListQueue() {
		return eventListQueue;
	}
	
	protected abstract E outerList(EventList<T> eventList);
	
	public EventListAssembler<E, T> addSpecializedList() {
		eventListQueue.add(outerList(getOuterList()));
		return this;
	}
	
	public EventListAssembler<E, T> addSortedList() {
		return addSortedList(SortedList.AVOID_MOVING_ELEMENTS);
	}
	
	public EventListAssembler<E, T> addSortedList(int mode) {
		SortedList<T> sortedList = SortedList.create(getOuterList());
		sortedList.setMode(mode);
		eventListQueue.add(sortedList);
		return this;
	}
	
	public EventListAssembler<E, T> addObservableElementList(Connector<? super T> connector) {
		ObservableElementList<T> observableElementList = new ObservableElementList<T>(getOuterList(), connector);
		eventListQueue.add(observableElementList);
		return this;
	}
	
	public EventListAssembler<E, T> addEventTableModel(TableFormat<? super T> tableFormat) {
		eventTableModel = new DefaultEventTableModel<T>(getOuterList(), tableFormat);
		return this;
	}
	
	public EventListAssembler<E, T> addEventSelectionModel() {
		eventSelectionModel = new DefaultEventSelectionModel<T>(getOuterList());
		eventSelectionModel.setSelectionMode(DefaultEventSelectionModel.SINGLE_SELECTION);
		return this;
	}
	
	private EventList<T> getOuterList() {
		return eventListQueue.peek();
	}
	
	public <EE> EE findEventListDownstream(Class<EE> theClass) {
		EE result = null;
		Iterator<EventList<T>> descendingIterator = getEventListQueue().descendingIterator();
		while(descendingIterator.hasNext() && result == null) {
			EventList<T> next = descendingIterator.next();
			if (next.getClass() == theClass) {
				result = theClass.cast(next);
			}
		}
		return result;
	}
	
	public void dispose() {
		// first dispose eventSelectionModel and eventTableModel..
		if (eventSelectionModel != null) {
			eventSelectionModel.dispose();
		} 
		if (eventTableModel != null) {
			eventTableModel.dispose();
		}
		EventList<T> eventList;
		while ((eventList = eventListQueue.poll()) != null) {
			eventList.dispose();
		}
	}
	
	public int compareTo(EventListAssembler<?, ?> o) {
		int result = -1;
		if (o != null) {
			if (equals(o)) {
				result = 0;
			} else if (o.getClass() != getClass()) {
				boolean isSuperClass = o.getClass().isAssignableFrom(getClass());
				result = isSuperClass ? 1 : -1;
			}
		}
		return result;
	}


}
