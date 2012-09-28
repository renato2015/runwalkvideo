package com.runwalk.video.glazedlists;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.TransformedList;

import com.runwalk.video.entities.SerializableEntity;

public abstract class TransformedListAssembler<V extends SerializableEntity<? super V>, E extends TransformedList<V, T>, T extends SerializableEntity<? super T>> extends EventListAssembler<E, T> {

	// this kind of list will have a dependency on another eventListAssembler
	private final EventListAssembler<?, V> parentEventListAssembler;
	
	private E transformedList;
	
	public TransformedListAssembler(EventListAssembler<?, V> parentEventListAssembler) {
		this.parentEventListAssembler = parentEventListAssembler;
	}
	
	// implement the transformation here..
	protected abstract E transformList(EventList<V> eventList);
	
	public TransformedListAssembler<V, E, T> addTransformedList(EventList<V> eventList) {
		this.transformedList = transformList(eventList);
		return this;
	}
	
	protected EventListAssembler<?, V> getParentEventListAssembler() {
		return parentEventListAssembler;
	}

	public TransformedList<V, T> getTransformedList() {
		return transformedList;
	}

	public int compareTo(EventListAssembler<?, ?> o) {
		int result = super.compareTo(o);
		if (o != null) {
			if (equals(o)) {
				result = 0;
			} else if (o == getParentEventListAssembler()) {
				result = -1;
			}
		}
		return result;
	}
	
}