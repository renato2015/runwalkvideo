package com.runwalk.video.dao.gdata;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.google.gdata.data.BaseEntry;
import com.google.gdata.data.BaseFeed;
import com.google.gdata.util.ServiceException;
import com.runwalk.video.dao.AbstractDao;

public class BaseEntryDao<F extends BaseFeed<F, E>, E extends BaseEntry<E>> extends AbstractDao<E> {

	private BaseFeed<F, E> feed;
	
	public BaseEntryDao(BaseFeed<F, E> feed, Class<E> typeParameter) {
		super(typeParameter);
		this.feed = feed;
	}
	
	public BaseEntryDao(Class<E> typeParameter) {
		super(typeParameter);
	}

	public List<E> getAll() {
		return feed.getEntries();
	}

	public E getById(Object id) {
		for (E entry : feed.getEntries()) {
			if (id == entry.getId()) {
				return entry;
			}
		}
			
		return null;
	}

	public List<E> getByIds(Set<?> ids) {
		List<E> result = new ArrayList<E>();
		for (E entry : feed.getEntries()) {
			if (ids.contains(entry.getId())) {
				result.add(entry);
			}
		}
		return result;
	}

	public E merge(E item) {
		try {
			return item.update();
		} catch (IOException e1) {
			getLogger().info("Merge failed for " + item, e1);
		} catch (ServiceException e2) {
			getLogger().info("Merge failed for " + item, e2);
		}
		return item;
	}

	public List<E> merge(Iterable<E> items) {
		List<E> result = new ArrayList<E>();
		for (E item : items) {
			merge(item);
		}
		return result;
	}

	public void delete(E item) {
		try {
			item.delete();
		} catch (IOException e1) {
			getLogger().error("Delete failed for " + item, e1);
		} catch (ServiceException e2) {
			getLogger().error("Delete failed for " + item, e2);
		}
	}

	public void persist(E item) {
		try {
			feed.insert(item);
		} catch (ServiceException e1) {
			getLogger().error("Persist failed for " + item, e1);
		} catch (IOException e2) {
			getLogger().error("Persist failed for " + item, e2);
		}
	}

	public void deleteById(Object id) {
		E item = getById(id);
		delete(item);
	}

	@Override
	public <T> List<E> getNewEntities(T id) {
		// TODO Auto-generated method stub
		// empty implementation for now..
		return null;
	}
	
	
	protected BaseFeed<F, E> getFeed() {
		return feed;
	}

	protected void setFeed(BaseFeed<F, E> feed) {
		this.feed = feed;
	}

}
