package com.runwalk.video.panels;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import javax.persistence.NoResultException;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;

import net.miginfocom.swing.MigLayout;

import org.jdesktop.application.Action;
import org.jdesktop.application.Task;
import org.jdesktop.application.Task.BlockingScope;
import org.jdesktop.application.TaskEvent;
import org.jdesktop.application.TaskListener;
import org.jdesktop.swingx.calendar.DateUtils;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.swing.TextComponentMatcherEditor;

import com.runwalk.video.dao.Dao;
import com.runwalk.video.dao.DaoService;
import com.runwalk.video.dao.jpa.AnalysisDao;
import com.runwalk.video.entities.Analysis;
import com.runwalk.video.entities.Customer;
import com.runwalk.video.io.VideoFileManager;
import com.runwalk.video.model.AnalysisModel;
import com.runwalk.video.model.CustomerModel;
import com.runwalk.video.settings.SettingsManager;
import com.runwalk.video.tasks.CalendarSlotModelSyncTask;
import com.runwalk.video.tasks.DeleteTask;
import com.runwalk.video.tasks.PersistTask;
import com.runwalk.video.tasks.RefreshEntityTask;
import com.runwalk.video.ui.table.DateTableCellRenderer;
import com.runwalk.video.util.AppUtil;

@SuppressWarnings("serial")
public class CustomerTablePanel extends AbstractTablePanel<CustomerModel> {

	public static final String SAVE_ACTION = "save";

	private static final String REFRESH_CLIENT_ACTION = "refreshCustomer";

	private static final String DELETE_CLIENT_ACTION = "deleteCustomer";

	private static final String ADD_CLIENT_ACTION = "addCustomer";

//	private static final String SYNC_CALENDAR_SLOTS_ACTION = "syncCalendarSlots";

	private final JTextField searchField;
	private final TextComponentMatcherEditor<CustomerModel> matcherEditor;

	private final VideoFileManager videoFileManager;
	private final DaoService daoService;

	public CustomerTablePanel(VideoFileManager videoFileManager, DaoService daoManager) {
		super(new MigLayout("nogrid, fill"));
		this.videoFileManager = videoFileManager;
		this.daoService = daoManager;

		String borderTitle = getResourceMap().getString("customerTablePanel.border.title");
		setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), 
				borderTitle, TitledBorder.LEFT, TitledBorder.TOP, SettingsManager.MAIN_FONT.deriveFont(12))); // NOI18N
		getTable().getTableHeader().setVisible(true);
		getTable().setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportView(getTable());
		add(scrollPane, "wrap, grow");

		setSecondButton(new JButton(getAction(ADD_CLIENT_ACTION)));
		getSecondButton().setFont(SettingsManager.MAIN_FONT);
		add(getSecondButton());

		setFirstButton(new JButton(getAction(DELETE_CLIENT_ACTION)));
		getFirstButton().setFont(SettingsManager.MAIN_FONT);
		add(getFirstButton());

		JButton refreshCustomerButton = new JButton(getAction(REFRESH_CLIENT_ACTION));
		refreshCustomerButton.setFont(SettingsManager.MAIN_FONT);
		add(refreshCustomerButton);

		JButton saveButton = new JButton(getAction(SAVE_ACTION));
		saveButton.setFont(SettingsManager.MAIN_FONT);
		add(saveButton);
		
		final Icon search = getResourceMap().getIcon("searchPanel.searchIcon");
		final Icon searchOverlay = getResourceMap().getIcon("searchPanel.searchOverlayIcon");

		searchField = new JTextField();
		searchField.setFont(SettingsManager.MAIN_FONT);
		matcherEditor = new TextComponentMatcherEditor<CustomerModel>(searchField.getDocument(), GlazedLists.toStringTextFilterator());

		final JLabel theLabel = new JLabel(getResourceMap().getString("searchPanel.searchFieldLabel.text"));
		theLabel.setFont(SettingsManager.MAIN_FONT);
		theLabel.setIcon(search);
		theLabel.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent arg0) {
				theLabel.setIcon(search);
				clearSearchField();
				getItemList().getReadWriteLock().readLock().lock();
				try {
					setSelectedItemRow(getTable().getSelectedRow());
				} finally {
					getItemList().getReadWriteLock().readLock().unlock();
				}
			}
			public void mouseEntered(MouseEvent arg0) {
				theLabel.setIcon(searchOverlay);
			}
			public void mouseExited(MouseEvent arg0) {
				theLabel.setIcon(search);
			}
		});

		add(theLabel, "gapleft push");
		add(searchField, "width :100:, growy");
	}

	public void initialiseTableColumnModel() {
		getTable().getColumnModel().getColumn(0).setPreferredWidth(35);
		getTable().getColumnModel().getColumn(0).setMaxWidth(35);
		getTable().getColumnModel().getColumn(1).setPreferredWidth(120);
		getTable().getColumnModel().getColumn(1).setMaxWidth(160);
		getTable().getColumnModel().getColumn(3).setMinWidth(80);
		getTable().getColumnModel().getColumn(3).setPreferredWidth(130);
		getTable().getColumnModel().getColumn(3).setMaxWidth(130);
		getTable().getColumnModel().getColumn(3).setCellRenderer(new DateTableCellRenderer(AppUtil.EXTENDED_DATE_FORMATTER));
	}

	@Override
	protected EventList<CustomerModel> specializeItemList(EventList<CustomerModel> eventList) {
		FilterList<CustomerModel> filterList = new FilterList<CustomerModel>(eventList);
		filterList.setMatcherEditor(getSearchEngineTextFieldMatcherEditor());
		return filterList;
	}

	private TextComponentMatcherEditor<CustomerModel> getSearchEngineTextFieldMatcherEditor() {
		return matcherEditor;
	}

	public boolean save() {
		getItemList().getReadWriteLock().readLock().lock();
		try {
			// advantage of dirty checking on the customer is that we don't need to serialize the complete list for saving just a few items
			Dao<Customer> dao = getDaoService().getDao(Customer.class);
			for(CustomerModel customerModel : getItemList()) {
				if (customerModel.isDirty()) {
					Customer customer = customerModel.getEntity();
					Customer mergedCustomer = dao.merge(customer);
					// set dirty flag to false again
					customerModel.setDirty(false);
					// set version field on old customer
					if (mergedCustomer.getVersion() != customer.getVersion()) {
						customer.incrementVersion();
					}
				}
			}
		} finally {
			getItemList().getReadWriteLock().readLock().unlock();
		}
		return true;
	}

	@Action(block = BlockingScope.ACTION)
	public PersistTask<Customer> addCustomer() {
		clearSearchField();
		Customer customer = new Customer();
		PersistTask<Customer> result = new PersistTask<Customer>(getDaoService(), Customer.class, customer);
		result.addTaskListener(new TaskListener.Adapter<Customer, Void>() {

			@Override
			public void succeeded(TaskEvent<Customer> event) {
				addCustomerModel(new CustomerModel(event.getValue()));
			}

		});
		return result;
	}
	
	/**
	 * Add the given {@link Customer} to the table's list.
	 * 
	 * @param customer The customer to remove
	 */
	private void addCustomerModel(CustomerModel customerModel) {
		getItemList().getReadWriteLock().writeLock().lock();
		try {
			getItemList().add(customerModel);
			setSelectedItemRow(customerModel);
			CustomerTablePanel.this.transferFocus();
			setDirty(true);
		} finally {
			getItemList().getReadWriteLock().writeLock().unlock();
		}
	}

	@Action(enabledProperty = ROW_SELECTED, block = BlockingScope.ACTION)
	public DeleteTask<Customer> deleteCustomer() {
		int n = JOptionPane.showConfirmDialog(
				SwingUtilities.windowForComponent(this),
				getResourceMap().getString("deleteCustomer.confirmDialog.text"),
				getResourceMap().getString("deleteCustomer.Action.text"),
				JOptionPane.WARNING_MESSAGE,
				JOptionPane.OK_CANCEL_OPTION);
		if (n == JOptionPane.CANCEL_OPTION || n == JOptionPane.CLOSED_OPTION) return null;
		final CustomerModel selectedModel = getSelectedItem();
		DeleteTask<Customer> result = new DeleteTask<Customer>(getDaoService(), Customer.class, selectedModel.getEntity());
		result.addTaskListener(new TaskListener.Adapter<Customer, Void>() {

			@Override
			public void succeeded(TaskEvent<Customer> event) {
				deleteCustomerModel(selectedModel);
			}

		});
		return result;
	}
	
	/**
	 * Remove the given Customer from the table's list.
	 * 
	 * @param customer The customer to remove
	 */
	private void deleteCustomerModel(CustomerModel customerModel) {
		getItemList().getReadWriteLock().writeLock().lock();
		try {
			int lastSelectedRowIndex = getEventSelectionModel().getMinSelectionIndex();
			// delete all video files for the selected customer
			getVideoFileManager().deleteVideoFiles(customerModel.getEntity());
			getItemList().remove(customerModel);
			// select previous record
			if (lastSelectedRowIndex > 0) {
				setSelectedItemRow(lastSelectedRowIndex - 1);
			}
		} finally {
			getItemList().getReadWriteLock().writeLock().unlock();
		}
	}

	@Action(enabledProperty = ROW_SELECTED, block = BlockingScope.APPLICATION)
	public RefreshEntityTask<Customer> refreshCustomer() {
		final CustomerModel selectedModel = getSelectedItem();
		if (selectedModel.isDirty() && isDirty()) {
			int n = JOptionPane.showConfirmDialog(
					SwingUtilities.windowForComponent(this),
					getResourceMap().getString("refreshCustomer.confirmDialog.text"),
					getResourceMap().getString("refreshCustomer.Action.text"),
					JOptionPane.WARNING_MESSAGE,
					JOptionPane.OK_CANCEL_OPTION);
			if (n == JOptionPane.CANCEL_OPTION || n == JOptionPane.CLOSED_OPTION) return null;
		}
		return new RefreshEntityTask<Customer>(getDaoService(), Customer.class, getSelectedItem().getEntity()) {

			@Override
			protected Customer doInBackground() throws Exception {
				Customer customer = super.doInBackground();
				// refresh file cache for newly added customers
				getItemList().getReadWriteLock().writeLock().lock();
				try {
					selectedModel.setEntity(customer);
					selectedModel.setDirty(false);
				} finally {
					getItemList().getReadWriteLock().writeLock().unlock();
				}
				return customer;
			}

			@Override
			protected void failed(Throwable throwable) {
				if (throwable instanceof NoResultException) {
					CustomerTablePanel outerInstance = CustomerTablePanel.this;
					JOptionPane.showMessageDialog( 
						SwingUtilities.windowForComponent(outerInstance),
						outerInstance.getResourceMap().getString("refreshCustomer.errorDialog.text"),
						outerInstance.getResourceMap().getString("refreshCustomer.errorDialog.title"),
						JOptionPane.WARNING_MESSAGE
					);
					deleteCustomerModel(selectedModel);
				} else {
					super.failed(throwable);
				}
			}

		};
	}
	
	@Action(block=BlockingScope.ACTION)
	public Task<?, ?> syncCalendarSlots() throws InterruptedException, ExecutionException {
		clearSearchField();
		return new CalendarSlotModelSyncTask<AnalysisModel>(AnalysisModel.class, new Callable<List<AnalysisModel>>() {

					// callback function so task can remain generic
					public List<AnalysisModel> call() throws Exception {
						Date date = DateUtils.startOfDay(new Date());
						AnalysisDao dao = getDaoService().getDao(Analysis.class);
						List<AnalysisModel> analysisModels = dao.getAnalysesAfterStartDateAsModels(date);
						// replace with existing object references
						for (AnalysisModel analysisModel : analysisModels) {
							for (CustomerModel customerModel : getObservableElementList()) {
								if (customerModel.equals(analysisModel.getCustomerModel())) {
									// link with customerModel in the list
									analysisModel.setCustomerModel(customerModel);
								}
							}
						}
						return analysisModels;
					}
					
				}) {

			@Override
			public void succeeded(final List<AnalysisModel> analysisModelList) {
				if (!analysisModelList.isEmpty()) {
					showCalendarSlotDialog(GlazedLists.eventList(analysisModelList), CustomerTablePanel.this);
				}
			}

		};
	}
	
	private void clearSearchField() {
		searchField.setText("");
	}

	public VideoFileManager getVideoFileManager() {
		return videoFileManager;
	}

	public DaoService getDaoService() {
		return daoService;
	}

}
