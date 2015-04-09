package com.runwalk.video.panels;

import java.awt.Component;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.DefaultListCellRenderer;
import javax.swing.InputVerifier;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.DateFormatter;
import javax.swing.text.DefaultFormatterFactory;

import net.miginfocom.swing.MigLayout;

import org.jdesktop.beansbinding.AbstractBindingListener;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Binding;
import org.jdesktop.beansbinding.BindingGroup;
import org.jdesktop.beansbinding.BindingListener;
import org.jdesktop.beansbinding.Bindings;
import org.jdesktop.beansbinding.Converter;
import org.jdesktop.beansbinding.ELProperty;
import org.jdesktop.beansbinding.PropertyStateEvent;
import org.jdesktop.beansbinding.Validator;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.matchers.TextMatcherEditor;
import ca.odell.glazedlists.swing.AutoCompleteSupport;

import com.runwalk.video.entities.City;
import com.runwalk.video.entities.Customer;
import com.runwalk.video.entities.Person.Gender;
import com.runwalk.video.model.AbstractEntityModel;
import com.runwalk.video.model.CustomerModel;
import com.runwalk.video.settings.SettingsManager;
import com.runwalk.video.ui.EnumButtonGroup;

@SuppressWarnings("serial")
public class CustomerInfoPanel extends AbstractPanel {

	private final static BeanProperty<JComponent, Boolean> ENABLED = BeanProperty.create("enabled");
	private final static ELProperty<CustomerTablePanel, Boolean> ITEM_SELECTED = ELProperty.create("${selectedItem != null}");
	private final static BeanProperty<JComponent, Boolean> SELECTED = BeanProperty.create("selected");
	private final static BeanProperty<JTextField, String> TEXT = BeanProperty.create("text");

	private EventList<City> itemList;

	private final JTextField firstnameField;
	private final JComboBox<City> zipCodeField;
	private final JComboBox<City> locationField;
	private AutoCompleteSupport<City> zipCodeCompletion;
	private AutoCompleteSupport<City> locationCompletion;
	private BindingGroup locationBindingGroup;

	/** This listener can be added to each binding group that contains bindings that have a {@link CustomerTablePanel} as source. */
	private final BindingListener changeListener = new AbstractBindingListener() {
		
		@Override
		public void targetChanged(@SuppressWarnings("rawtypes") Binding binding, PropertyStateEvent event) {
			CustomerTablePanel customerTablePanel = (CustomerTablePanel) binding.getSourceObject();
			AbstractEntityModel<Customer> selectedItem = customerTablePanel.getSelectedItem();
			if (selectedItem != null) {
				selectedItem.setDirty(true);
				customerTablePanel.setDirty(true);
			}
		}

	};

	private final CustomerTablePanel customerTablePanel;

	public CustomerInfoPanel(final CustomerTablePanel customerTablePanel, UndoableEditListener undoListener) {
		this.customerTablePanel = customerTablePanel;
		// set layout constraints
		setLayout(new MigLayout("fill", "[right]10[grow,fill]", "10[grow,fill]"));
		BindingGroup bindingGroup = new BindingGroup();
		// component value binding
		Binding<? extends AbstractTablePanel<CustomerModel>, String, JTextField, String> valueBinding = null;
		// component enabling binding
		Binding<? extends AbstractTablePanel<CustomerModel>, Boolean, ? extends JComponent, Boolean> enabledBinding = null;

		/**
		 * Converts first characters to uppercase
		 */
		class FirstCharacterToUpperCaseConverter extends Converter<String, String> {

			public String convertForward(String arg0) {
				return arg0.length() > 0 ? Character.toUpperCase(arg0.charAt(0)) + arg0.substring(1) : arg0;
			}

			public String convertReverse(String arg0) {
				return arg0.length() > 0 ? Character.toUpperCase(arg0.charAt(0)) + arg0.substring(1) : arg0;
			}

		}

		JLabel nameLabel = new JLabel();
		nameLabel.setFont(SettingsManager.MAIN_FONT);
		nameLabel.setText(getResourceMap().getString("nameLabel.text")); // NOI18N
		add(nameLabel);

		firstnameField = new JTextField();
		firstnameField.setFont(SettingsManager.MAIN_FONT);
		firstnameField.getDocument().addUndoableEditListener(undoListener);

		BeanProperty<CustomerTablePanel, String> firstname = BeanProperty.create("selectedItem.firstname");
		valueBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, customerTablePanel, firstname, firstnameField, TEXT, "firstNameBinding");
		valueBinding.setConverter(new FirstCharacterToUpperCaseConverter());
		bindingGroup.addBinding(valueBinding);
		enabledBinding = Bindings.createAutoBinding(UpdateStrategy.READ, customerTablePanel, ITEM_SELECTED, firstnameField, ENABLED);
		bindingGroup.addBinding(enabledBinding);
		add(firstnameField);

		JTextField nameField = new JTextField();
		nameField.getDocument().addUndoableEditListener(undoListener);
		nameField.setFont(SettingsManager.MAIN_FONT);
		BeanProperty<CustomerTablePanel, String> name = BeanProperty.create("selectedItem.name");
		valueBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, customerTablePanel, name, nameField, TEXT, "lastNameBinding");
		valueBinding.setConverter(new FirstCharacterToUpperCaseConverter());

		bindingGroup.addBinding(valueBinding);
		enabledBinding = Bindings.createAutoBinding(UpdateStrategy.READ, customerTablePanel, ITEM_SELECTED, nameField, ENABLED);
		bindingGroup.addBinding(enabledBinding);
		add(nameField, "wrap");

		JLabel companyNameLabel = new JLabel();
		companyNameLabel.setFont(SettingsManager.MAIN_FONT);
		companyNameLabel.setText(getResourceMap().getString("companyNameLabel.text")); // NOI18N
		add(companyNameLabel);

		JTextField companyNameField = new JTextField();
		companyNameField.getDocument().addUndoableEditListener(undoListener);
		BeanProperty<CustomerTablePanel, String> companyName = BeanProperty.create("selectedItem.entity.companyName");
		valueBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, customerTablePanel, companyName, companyNameField, TEXT);
		valueBinding.setConverter(new FirstCharacterToUpperCaseConverter());
		bindingGroup.addBinding(valueBinding);
		enabledBinding = Bindings.createAutoBinding(UpdateStrategy.READ, customerTablePanel, ITEM_SELECTED, companyNameField, ENABLED);
		bindingGroup.addBinding(enabledBinding);
		companyNameField.setFont(SettingsManager.MAIN_FONT);
		add(companyNameField);

		JLabel accountNumberLabel = new JLabel();
		accountNumberLabel.setFont(SettingsManager.MAIN_FONT);
		accountNumberLabel.setText(getResourceMap().getString("accountNumberLabel.text"));
		add(accountNumberLabel, "split");

		JTextField accountNumberField = new JTextField();
		accountNumberField.getDocument().addUndoableEditListener(undoListener);
		accountNumberField.setFont(SettingsManager.MAIN_FONT);
		BeanProperty<CustomerTablePanel, String> accountNumber = BeanProperty.create("selectedItem.entity.accountNumber");
		valueBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, customerTablePanel, accountNumber, accountNumberField, TEXT);
		valueBinding.setValidator(new Validator<String> () {

			public Validator<String>.Result validate(String arg) {
				String regexPattern = "[0-9]{9}";
				Pattern pattern = Pattern.compile(regexPattern);
				Matcher matcher = pattern.matcher(arg);
				if(!matcher.matches()){
					return new Result(null, "Btwnr. moet 9 cijfers bevatten");
				}
				return null;    
			}
		});
		bindingGroup.addBinding(valueBinding);
		enabledBinding = Bindings.createAutoBinding(UpdateStrategy.READ, customerTablePanel, ITEM_SELECTED, accountNumberField, ENABLED);
		bindingGroup.addBinding(enabledBinding);
		add(accountNumberField, "wrap, grow");

		JLabel emailLabel = new JLabel();
		emailLabel.setFont(SettingsManager.MAIN_FONT);
		emailLabel.setText(getResourceMap().getString("emailLabel.text")); // NOI18N
		add(emailLabel);

		JTextField emailField = new JTextField();
		emailField.getDocument().addUndoableEditListener(undoListener);
		emailField.setFont(SettingsManager.MAIN_FONT);
		BeanProperty<CustomerTablePanel, String> email = BeanProperty.create("selectedItem.emailAddress");
		valueBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, customerTablePanel, email, emailField, TEXT);
		/*valueBinding.setValidator(new Validator() {

					public Validator.Result validate(Object arg) {        
						String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
						Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
						Matcher matcher = pattern.matcher(arg.toString());
						if(!matcher.matches()){
							return new Result(null, "Email adres is niet correct geformatteerd");
						}
						return null;
					}
				}
		);*/
		bindingGroup.addBinding(valueBinding);
		enabledBinding = Bindings.createAutoBinding(UpdateStrategy.READ, customerTablePanel, ITEM_SELECTED, emailField, ENABLED);
		bindingGroup.addBinding(enabledBinding);
		add(emailField, "span, split");

		JLabel mailingListLabel = new JLabel();
		mailingListLabel.setFont(SettingsManager.MAIN_FONT);
		mailingListLabel.setText(getResourceMap().getString("mailingListLabel.text")); // NOI18N
		add(mailingListLabel, "grow 0");

		JCheckBox mailingListCheckbox = new JCheckBox();
		BeanProperty<CustomerTablePanel, Boolean> inMailingList = BeanProperty.create("selectedItem.entity.inMailingList");
		enabledBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, customerTablePanel, inMailingList, mailingListCheckbox, SELECTED);
		bindingGroup.addBinding(enabledBinding);
		enabledBinding = Bindings.createAutoBinding(UpdateStrategy.READ, customerTablePanel, ITEM_SELECTED, mailingListCheckbox, ENABLED);
		bindingGroup.addBinding(enabledBinding);
		add(mailingListCheckbox, "wrap, grow 0");

		JLabel birthdateLabel = new JLabel();
		birthdateLabel.setFont(SettingsManager.MAIN_FONT);
		birthdateLabel.setText(getResourceMap().getString("birthdateLabel.text")); // NOI18N
		add(birthdateLabel);

		JFormattedTextField birthdateField = new JFormattedTextField();
		DateFormatter dateFormatter = new DateFormatter(new SimpleDateFormat("dd/MM/yyyy"));
		birthdateField.setFormatterFactory(new DefaultFormatterFactory(dateFormatter));
		birthdateField.setInputVerifier(new InputVerifier() {

			@Override
			public boolean shouldYieldFocus(JComponent input) {
				boolean inputOK = verify(input);
				if (inputOK) {
					return true;
				}
				String failedVerificationMessage = getResourceMap().getString("birthdateField.errorDialog.text");
				JOptionPane.showMessageDialog(null, failedVerificationMessage, getResourceMap().getString("birthdateField.errorDialog.title"), JOptionPane.WARNING_MESSAGE);
				//Reinstall the input verifier.
				input.setInputVerifier(this);
				return false;
			}

			public boolean verify(JComponent input) {
				boolean result = ((JFormattedTextField) input).isEditValid();
				if (!(input instanceof JFormattedTextField)) {
					result = true; 
				}
				return result;
			}

		});
		BeanProperty<CustomerTablePanel, Date> birthDate = BeanProperty.create("selectedItem.entity.birthdate");
		BeanProperty<JFormattedTextField, ?> formattedValue = BeanProperty.create("value"); 
		Binding<? extends AbstractTablePanel<CustomerModel>, Date, JFormattedTextField, ?> birthDateBinding = 
			Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, customerTablePanel, birthDate, birthdateField, formattedValue);
		bindingGroup.addBinding(birthDateBinding);
		enabledBinding = Bindings.createAutoBinding(UpdateStrategy.READ, customerTablePanel, ITEM_SELECTED, birthdateField, ENABLED);
		bindingGroup.addBinding(enabledBinding);
		add(birthdateField);

		JLabel genderLabel = new JLabel();
		genderLabel.setFont(SettingsManager.MAIN_FONT);
		genderLabel.setText(getResourceMap().getString("genderLabel.text")); // NOI18N
		add(genderLabel, "split");

		JRadioButton maleRadioButton = new JRadioButton();
		maleRadioButton.setText(getResourceMap().getString("maleRadioButton.text")); // NOI18N
		enabledBinding = Bindings.createAutoBinding(UpdateStrategy.READ, customerTablePanel, ITEM_SELECTED, maleRadioButton, ENABLED);
		bindingGroup.addBinding(enabledBinding);
		add(maleRadioButton);

		JRadioButton femaleRadioButton = new JRadioButton();
		femaleRadioButton.setText(getResourceMap().getString("femaleRadioButton.text")); // NOI18N
		enabledBinding = Bindings.createAutoBinding(UpdateStrategy.READ, customerTablePanel, ITEM_SELECTED, femaleRadioButton, ENABLED);
		bindingGroup.addBinding(enabledBinding);
		add(femaleRadioButton, "wrap, gapright push");
		//create a model for the radio buttons
		EnumButtonGroup<Gender> ebg = new EnumButtonGroup<Gender>(Gender.class);
		ebg.add(Gender.FEMALE, femaleRadioButton);
		ebg.add(Gender.MALE, maleRadioButton);
		ebg.assertButtonGroupCoversAllEnumConstants();
		//add the binding for the radio buttons
		BeanProperty<CustomerTablePanel, Gender> gender = BeanProperty.create("selectedItem.entity.gender");
		Binding<? extends AbstractTablePanel<CustomerModel>, Gender, EnumButtonGroup<? extends Enum<?>>, ? extends Enum<?>> genderBinding = 
			Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, customerTablePanel, gender, ebg, EnumButtonGroup.SELECTED_ENUM_PROPERTY);
		bindingGroup.addBinding(genderBinding);

		JLabel addressLabel = new JLabel();
		addressLabel.setFont(SettingsManager.MAIN_FONT);
		addressLabel.setText(getResourceMap().getString("addressLabel.text")); // NOI18N
		add(addressLabel);

		JTextField addressField = new JTextField();
		addressField.getDocument().addUndoableEditListener(undoListener);
		addressField.setFont(SettingsManager.MAIN_FONT);
		BeanProperty<CustomerTablePanel, String> address = BeanProperty.create("selectedItem.entity.address.address");
		valueBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, customerTablePanel, address, addressField, TEXT);
		bindingGroup.addBinding(valueBinding);
		enabledBinding = Bindings.createAutoBinding(UpdateStrategy.READ, customerTablePanel, ITEM_SELECTED, addressField, ENABLED);
		bindingGroup.addBinding(enabledBinding);
		add(addressField, "span, wrap");

		JLabel telephoneLabel = new JLabel();
		telephoneLabel.setFont(SettingsManager.MAIN_FONT);
		telephoneLabel.setText(getResourceMap().getString("telephoneLabel.text")); // NOI18N
		add(telephoneLabel);

		JTextField phoneField = new JTextField();
		phoneField.getDocument().addUndoableEditListener(undoListener);
		phoneField.setFont(SettingsManager.MAIN_FONT);
		BeanProperty<CustomerTablePanel, String> phone = BeanProperty.create("selectedItem.entity.phoneNumber");
		valueBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, customerTablePanel, phone, phoneField, TEXT);
		bindingGroup.addBinding(valueBinding);
		enabledBinding = Bindings.createAutoBinding(UpdateStrategy.READ, customerTablePanel, ITEM_SELECTED, phoneField, ENABLED);
		bindingGroup.addBinding(enabledBinding);
		add(phoneField, "wrap, span");

		JLabel locationLabel = new JLabel();
		locationLabel.setFont(SettingsManager.MAIN_FONT);
		locationLabel.setText(getResourceMap().getString("locationLabel.text")); // NOI18N
		add(locationLabel);		

		zipCodeField = new JComboBox<City>();
		zipCodeField.setFont(SettingsManager.MAIN_FONT);
		add(zipCodeField, "width max(150, 40%)");

		locationField = new JComboBox<City>();
		locationField.setFont(SettingsManager.MAIN_FONT);
		add(locationField, "width max(150, 40%)");

		bindingGroup.addBindingListener(changeListener);
		bindingGroup.bind();
	}

	public EventList<City> getItemList() {
		return itemList;
	}

	public void setItemList(final EventList<City> itemList) {
		disposeItemList();
		this.itemList = itemList;
		installLocationCompletion(itemList);
	}

	private void installLocationCompletion(final EventList<City> itemList) {
		final TextMatcherEditor<City> matcherEditor = new TextMatcherEditor<City>(GlazedLists.toStringTextFilterator());
		final FilterList<City> filterList = new FilterList<City>(itemList, matcherEditor);
		uninstallLocationCompletion();
		locationBindingGroup = new BindingGroup();
		zipCodeCompletion = AutoCompleteSupport.install(zipCodeField, itemList, GlazedLists.textFilterator("code"), new Format() {

			public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
				StringBuffer result = new StringBuffer();
				if (obj instanceof City) {
					result.append(((City) obj).getCode());
				}
				return result;
			}

			public Object parseObject(String value, ParsePosition pos) {
				City selectedCity = (City) zipCodeField.getSelectedItem();
				if (selectedCity != null && !value.equals(selectedCity.getCode())) {
					matcherEditor.setFilterText(new String[] {value});
				}
				return !filterList.isEmpty() ? filterList.get(0) : selectedCity;
			}

		});
		zipCodeCompletion.setFirstItem(null);
		zipCodeCompletion.setStrict(false);
		zipCodeField.setRenderer(new CityInfoRenderer());

		BeanProperty<CustomerTablePanel, City> city = BeanProperty.create("selectedItem.city");
		BeanProperty<JComboBox<City>, City> selectedItem = BeanProperty.create("selectedItem");
		

		Binding<CustomerTablePanel, City, JComboBox<City>, City> comboBoxBinding = 
			Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, getCustomerTablePanel(), city, zipCodeField, selectedItem);
		comboBoxBinding.setSourceNullValue(null);
		comboBoxBinding.setSourceUnreadableValue(null);
		locationBindingGroup.addBinding(comboBoxBinding);
		Binding<? extends AbstractTablePanel<CustomerModel>, Boolean, ? extends JComponent, Boolean> enabledBinding = null;
		enabledBinding = Bindings.createAutoBinding(UpdateStrategy.READ, getCustomerTablePanel(), ITEM_SELECTED, zipCodeField, ENABLED);
		locationBindingGroup.addBinding(enabledBinding);
		locationCompletion = AutoCompleteSupport.install(locationField, itemList, GlazedLists.textFilterator("name"), new Format() {

			public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
				StringBuffer result = new StringBuffer();
				if (obj instanceof City) {
					result.append(((City) obj).getName());
				}
				return result;
			}

			public Object parseObject(String value, ParsePosition pos) {
				City selectedCity = (City) locationField.getSelectedItem();
				if (selectedCity != null) {
					matcherEditor.setFilterText(new String[] {value});
				}
				return !filterList.isEmpty() ? filterList.get(0) : selectedCity;
			}

		});
		locationCompletion.setFirstItem(null);
		locationCompletion.setStrict(false);				
		locationField.setRenderer(new CityInfoRenderer());

		comboBoxBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, getCustomerTablePanel(), city, locationField, selectedItem);
		comboBoxBinding.setSourceNullValue(null);
		comboBoxBinding.setSourceUnreadableValue(null);
		locationBindingGroup.addBinding(comboBoxBinding);
		enabledBinding = Bindings.createAutoBinding(UpdateStrategy.READ, getCustomerTablePanel(), ITEM_SELECTED, locationField, ENABLED);
		locationBindingGroup.addBinding(enabledBinding);
		locationBindingGroup.addBindingListener(changeListener);
		locationBindingGroup.bind();
	}

	private void disposeItemList() {
		if (this.itemList != null) {
			// dispose the current list, so it can be garbage collected
			this.itemList.dispose();
		}
	}

	private void uninstallLocationCompletion() {
		if (zipCodeCompletion != null) {
			zipCodeCompletion.uninstall();
		}
		if (locationBindingGroup != null) {
			locationBindingGroup.unbind();
		}
		if (locationCompletion != null) {
			locationCompletion.uninstall();
		}
	}

	private CustomerTablePanel getCustomerTablePanel() {
		return customerTablePanel;
	}

	@Override
	public void requestFocus() {
		firstnameField.requestFocus();
	}

	public static class CityInfoRenderer extends DefaultListCellRenderer {

		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			JLabel result = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			if (value != null) {
				City city = (City) value;
				result.setText(city.getCode() + " " + city.getName());
			}
			return result;
		}
	}

}
