package com.runwalk.video.dao.gdata;

import com.google.gdata.data.extensions.BaseEventEntry;
import com.runwalk.video.dao.AbstractDaoService;
import com.runwalk.video.dao.Dao;
import com.runwalk.video.settings.AuthenticationSettings;

public class BaseEntryDaoService extends AbstractDaoService {

	public BaseEntryDaoService(AuthenticationSettings authenticationSettings, String applicationName) {
		addSepcializedDaos(authenticationSettings, applicationName);
	}

	private void addSepcializedDaos(AuthenticationSettings authenticationSettings,
			String applicationName) {
		addDao(new CalendarEventEntryDao(authenticationSettings, applicationName));
	}

	/**
	 * This method will find a correct {@link Dao} for the given class, only
	 * if the class inherits from {@link BaseEventEntry}. If no suitable {@link Dao} is found,
	 * null will be returned
	 * 
	 * @return The dao for the given class
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <E, D extends Dao<E>> D getDao(Class<E> type) {
		D result = null;
		if (type.isAssignableFrom(BaseEventEntry.class)) {
			result = (D) getDaos().get(type);
		}
		return result;
	}

}
