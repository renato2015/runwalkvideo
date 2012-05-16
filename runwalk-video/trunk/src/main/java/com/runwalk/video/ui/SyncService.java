package com.runwalk.video.ui;

import java.util.List;
import java.util.Map;

import com.google.gdata.data.BaseEntry;
import com.runwalk.video.entities.SerializableEntity;

/**
 * A basic interface contract describing the functionality needed to map database {@link SerializableEntity}s with gdata's {@link BaseEntry}s.
 * 
 * @author Jeroen Peelaerts
 *
 * @param <T> The {@link SerializableEntity} subclass
 * @param <E> The {@link BaseEntry} subclass
 */
public interface SyncService<T extends SerializableEntity<? super T>, E extends BaseEntry<E>> {
	
	/**
	 * Prepare the synchronization process by building a {@link Map} that holds a mapping of all {@link SerializableEntity}s 
	 * who need to be synced with their {@link BaseEntry}s counterparts.
	 * 
	 * @return The map of items to synchronize
	 */
	Map<T, E> prepareSyncToDatabase();
	
	/**
	 * Synchronize data with the service implementation.
	 * The result of {@link #prepareSyncToDatabase()} should be passed as argument here.
	 * 
	 * @param eventEntryMapping A mapping of serializableEntities with their corresponding baseEntries
	 * @return The number of items that were synchronized
	 */
	List<T> syncToDatabase(Map<T, E> eventEntryMapping);

	/**
	 * Map the state of a {@link BaseEntry} to it's corresponding {@link SerializableEntity} derived class.
	 * 
	 * @param baseEntry The baseEntry to map
	 * @return The mapped serializableEntity
	 */
	T mapToSerializableEntity(E baseEntry);

}
