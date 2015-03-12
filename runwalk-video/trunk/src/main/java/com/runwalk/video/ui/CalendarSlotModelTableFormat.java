package com.runwalk.video.ui;

import org.apache.log4j.Logger;
import org.jdesktop.application.ResourceMap;

import ca.odell.glazedlists.gui.WritableTableFormat;

import com.runwalk.video.model.AnalysisModel;
import com.runwalk.video.model.CalendarSlotModel;
import com.runwalk.video.model.CalendarSlotModel.CalendarSlotStatus;
import com.runwalk.video.model.CustomerModel;

public class CalendarSlotModelTableFormat<T extends CalendarSlotModel<?>> extends AbstractTableFormat<T> implements WritableTableFormat<T> {

	public CalendarSlotModelTableFormat(ResourceMap resourceMap) {
		super(resourceMap);
	}

	public Object getColumnValue(T calendarSlotModel, int column) {
		if (column == 0) {
			return calendarSlotModel.getStartDate();
		} else if (column == 1) {
			return calendarSlotModel.getStartDate();
		} else if (column == 2) {
			CalendarSlotStatus status = calendarSlotModel.isCancelled() ? CalendarSlotStatus.CANCELLED : CalendarSlotStatus.SYNCHRONIZED;
			return getResourceString(status.getResourceKey());
		} else if (column == 3) {
			return calendarSlotModel.getCustomerModel();
		} else if (column == 4) {
			return calendarSlotModel.getComments();
		}
		return null;
	}

	@Override
	public boolean isEditable(T calendarSlot, int column) {
		return column == 3;
	}

	@Override
	public T setColumnValue(T calendarSlotModel,
			Object editedValue, int column) {
		if (column == 3 && editedValue instanceof CustomerModel) {
			// should remove from previous customer and mark both dirty!!
			CustomerModel customerModel = (CustomerModel) editedValue;
			if (customerModel != calendarSlotModel.getCustomerModel()) {
				if (calendarSlotModel instanceof AnalysisModel) {
					AnalysisModel analysisModel = (AnalysisModel) calendarSlotModel;
					boolean result = calendarSlotModel.getCustomerModel().removeAnalysisModel(analysisModel);
					Logger.getLogger(getClass()).info(analysisModel + " remove success=" + result + ", customer=" + customerModel);
					customerModel.addAnalysisModel(analysisModel);
				} 
				calendarSlotModel.setCustomerModel(customerModel);
			}
		}
		return calendarSlotModel;
	}

}
