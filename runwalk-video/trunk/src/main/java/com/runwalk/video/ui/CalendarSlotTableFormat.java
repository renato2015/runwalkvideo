package com.runwalk.video.ui;

import java.util.Arrays;

import org.jdesktop.application.ResourceMap;

import ca.odell.glazedlists.gui.WritableTableFormat;

import com.runwalk.video.entities.Analysis;
import com.runwalk.video.entities.CalendarSlot;
import com.runwalk.video.entities.CalendarSlotStatus;
import com.runwalk.video.model.CustomerModel;

public class CalendarSlotTableFormat extends AbstractTableFormat<CalendarSlot<?>> implements WritableTableFormat<CalendarSlot<?>> {

	public CalendarSlotTableFormat(ResourceMap resourceMap) {
		super(resourceMap);
	}

	public Object getColumnValue(CalendarSlot<?> calendarSlot, int column) {
		if (column == 0) {
			return calendarSlot.getStartDate();
		} else if (column == 1) {
			return calendarSlot.getStartDate();
		} else if (column == 2) {
			CalendarSlotStatus status = calendarSlot.isCancelled() ? CalendarSlotStatus.CANCELLED : CalendarSlotStatus.SYNCHRONIZED;
			return getResourceString(status.getResourceKey());
		} else if (column == 3) {
			return new CustomerModel(calendarSlot.getCustomer());
		} else if (column == 4) {
			return calendarSlot.getComments();
		}
		return null;
	}

	@Override
	public boolean isEditable(CalendarSlot<?> calendarSlot, int column) {
		return column == 3;
	}

	@Override
	public CalendarSlot<?> setColumnValue(CalendarSlot<?> calendarSlot,
			Object editedValue, int column) {
		if (column == 3 && editedValue instanceof CustomerModel) {
			// should remove from previous customer and mark both dirty!!
			CustomerModel customerModel = (CustomerModel) editedValue;
			if (calendarSlot instanceof Analysis) {
				Analysis analysis = (Analysis) calendarSlot;
				calendarSlot.getCustomer().removeAnalysis(analysis);				
				customerModel.addAnalysisModels(Arrays.asList((Analysis) calendarSlot));
			} 
			calendarSlot.setCustomer(customerModel.getEntity());
		}
		return calendarSlot;
	}

}
