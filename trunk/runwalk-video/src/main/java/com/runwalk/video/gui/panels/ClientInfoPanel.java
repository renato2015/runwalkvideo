package com.runwalk.video.gui.panels;

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
import javax.swing.JPanel;
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
import com.runwalk.video.entities.Client;
import com.runwalk.video.entities.Person.Gender;
import com.runwalk.video.gui.AppComponent;
import com.runwalk.video.gui.EnumButtonGroup;
import com.runwalk.video.util.AppSettings;

/**
 * TODO validatie met hibernate validators implementeren
 * 
 * @author Jeroen Peelaerts
 *
 */
@SuppressWarnings("serial")
@AppComponent
public class ClientInfoPanel extends JPanel {

	private final static BeanProperty<JComponent, Boolean> ENABLED = BeanProperty.create("enabled");
	private final static ELProperty<ClientTablePanel, Boolean> ITEM_SELECTED = ELProperty.create("${selectedItem != null}");
	private final static BeanProperty<JComponent, Boolean> SELECTED = BeanProperty.create("selected");
	private final static BeanProperty<JTextField, String> TEXT = BeanProperty.create("text");
	private final static BeanProperty<JTextField, String> TEXT_ON_FOCUS_LOST = BeanProperty.create("text_ON_FOCUS_LOST");

	private EventList<City> itemList;

	private final JTextField firstnameField;
	private final JComboBox zipCodeField;
	private final JComboBox locationField;
	private AutoCompleteSupport<City> zipCodeCompletion;
	private AutoCompleteSupport<City> locationCompletion;
	private BindingGroup locationBindingGroup;

	/** This listener can be added to each binding group that contains bindings that have a {@link ClientTablePanel} as source. */
	private final BindingListener changeListener = new AbstractBindingListener() {

		@Override
		public void targetChanged(@SuppressWarnings("rawtypes") Binding binding, PropertyStateEvent event) {
			ClientTablePanel tablePanel = (ClientTablePanel) binding.getSourceObject();
			Client selectedItem = tablePanel.getSelectedItem();
			if (selectedItem != null) {
				selectedItem.setDirty(true);
				tablePanel.setSaveNeeded(true);
			}
		}

	};

	private final ClientTablePanel clientTablePanel;

	public ClientInfoPanel(final ClientTablePanel clientTablePanel, UndoableEditListener undoListener) {
		this.clientTablePanel = clientTablePanel;
		// set layout constraints
		setLayout(new MigLayout("fill", "[right]10[grow,fill]", "10[grow,fill]"));
		BindingGroup bindingGroup = new BindingGroup();
		// component value binding
		Binding<? extends AbstractTablePanel<Client>, String, JTextField, String> valueBinding = null;
		// component enabling binding
		Binding<? extends AbstractTablePanel<Client>, Boolean, ? extends JComponent, Boolean> enabledBinding = null;

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
		nameLabel.setFont(AppSettings.MAIN_FONT);
		nameLabel.setText(getResourceMap().getString("nameLabel.text")); // NOI18N
		add(nameLabel);

		firstnameField = new JTextField();
		firstnameField.setFont(AppSettings.MAIN_FONT);
		firstnameField.getDocument().addUndoableEditListener(undoListener);

		BeanProperty<ClientTablePanel, String> firstname = BeanProperty.create("selectedItem.firstname");
		valueBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, clientTablePanel, firstname, firstnameField, TEXT);
		valueBinding.setConverter(new FirstCharacterToUpperCaseConverter());
		bindingGroup.addBinding(valueBinding);
		enabledBinding = Bindings.createAutoBinding(UpdateStrategy.READ, clientTablePanel, ITEM_SELECTED, firstnameField, ENABLED);
		bindingGroup.addBinding(enabledBinding);
		add(firstnameField);

		JTextField nameField = new JTextField();
		nameField.getDocument().addUndoableEditListener(undoListener);
		nameField.setFont(AppSettings.MAIN_FONT);
		BeanProperty<ClientTablePanel, String> name = BeanProperty.create("selectedItem.name");
		valueBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, clientTablePanel, name, nameField, TEXT);
		valueBinding.setConverter(new FirstCharacterToUpperCaseConverter());

		bindingGroup.addBinding(valueBinding);
		enabledBinding = Bindings.createAutoBinding(UpdateStrategy.READ, clientTablePanel, ITEM_SELECTED, nameField, ENABLED);
		bindingGroup.addBinding(enabledBinding);
		add(nameField, "wrap");

		JLabel organizationLabel = new JLabel();
		organizationLabel.setFont(AppSettings.MAIN_FONT);
		organizationLabel.setText(getResourceMap().getString("organisationLabel.text")); // NOI18N
		add(organizationLabel);

		JTextField organisationField = new JTextField();
		organisationField.getDocument().addUndoableEditListener(undoListener);
		BeanProperty<ClientTablePanel, String> organization = BeanProperty.create("selectedItem.organization");
		valueBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, clientTablePanel, organization, organisationField, TEXT);
		valueBinding.setConverter(new FirstCharacterToUpperCaseConverter());
		bindingGroup.addBinding(valueBinding);
		enabledBinding = Bindings.createAutoBinding(UpdateStrategy.READ, clientTablePanel, ITEM_SELECTED, organisationField, ENABLED);
		bindingGroup.addBinding(enabledBinding);
		organisationField.setFont(AppSettings.MAIN_FONT);
		add(organisationField);

		JLabel taxLabel = new JLabel();
		taxLabel.setFont(AppSettings.MAIN_FONT);
		taxLabel.setText(getResourceMap().getString("taxNoLabel.text"));
		add(taxLabel, "split");

		JTextField taxField = new JTextField();
		taxField.getDocument().addUndoableEditListener(undoListener);
		taxField.setFont(AppSettings.MAIN_FONT);
		BeanProperty<ClientTablePanel, String> taxNumber = BeanProperty.create("selectedItem.taxNumber");
		valueBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, clientTablePanel, taxNumber, taxField, TEXT);
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
		enabledBinding = Bindings.createAutoBinding(UpdateStrategy.READ, clientTablePanel, ITEM_SELECTED, taxField, ENABLED);
		bindingGroup.addBinding(enabledBinding);
		add(taxField, "wrap, grow");

		JLabel emailLabel = new JLabel();
		emailLabel.setFont(AppSettings.MAIN_FONT);
		emailLabel.setText(getResourceMap().getString("emailLabel.text")); // NOI18N
		add(emailLabel);

		JTextField emailField = new JTextField();
		emailField.getDocument().addUndoableEditListener(undoListener);
		emailField.setFont(AppSettings.MAIN_FONT);
		BeanProperty<ClientTablePanel, String> email = BeanProperty.create("selectedItem.emailAddress");
		valueBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, clientTablePanel, email, emailField, TEXT);
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
		enabledBinding = Bindings.createAutoBinding(UpdateStrategy.READ, clientTablePanel, ITEM_SELECTED, emailField, ENABLED);
		bindingGroup.addBinding(enabledBinding);
		add(emailField, "span, split");

		JLabel mailingListLabel = new JLabel();
		mailingListLabel.setFont(AppSettings.MAIN_FONT);
		mailingListLabel.setText(getResourceMap().getString("mailingListLabel.text")); // NOI18N
		add(mailingListLabel, "grow 0");

		JCheckBox mailingListCheckbox = new JCheckBox();
		BeanProperty<ClientTablePanel, Boolean> inMailingList = BeanProperty.create("selectedItem.inMailingList");
		enabledBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, clientTablePanel, inMailingList, mailingListCheckbox, SELECTED);
		bindingGroup.addBinding(enabledBinding);
		enabledBinding = Bindings.createAutoBinding(UpdateStrategy.READ, clientTablePanel, ITEM_SELECTED, mailingListCheckbox, ENABLED);
		bindingGroup.addBinding(enabledBinding);
		add(mailingListCheckbox, "wrap, grow 0");

		JLabel birthdateLabel = new JLabel();
		birthdateLabel.setFont(AppSettings.MAIN_FONT);
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
				String failedVerificationMessage = "Date must be in the dd/MM/yyyy format. For example: 17/11/2008";
				JOptionPane.showMessageDialog(null, failedVerificationMessage, "Invalid Date Format", JOptionPane.WARNING_MESSAGE);
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
		BeanProperty<ClientTablePanel, Date> birthDate = BeanProperty.create("selectedItem.birthdate");
		BeanProperty<JFormattedTextField, ?> formattedValue = BeanProperty.create("value"); 
		Binding<? extends AbstractTablePanel<Client>, Date, JFormattedTextField, ?> birthDateBinding = 
			Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, clientTablePanel, birthDate, birthdateField, formattedValue);
		bindingGroup.addBinding(birthDateBinding);
		enabledBinding = Bindings.createAutoBinding(UpdateStrategy.READ, clientTablePanel, ITEM_SELECTED, birthdateField, ENABLED);
		bindingGroup.addBinding(enabledBinding);
		add(birthdateField);

		JLabel genderLabel = new JLabel();
		genderLabel.setFont(AppSettings.MAIN_FONT);
		genderLabel.setText(getResourceMap().getString("genderLabel.text")); // NOI18N
		add(genderLabel, "split");

		JRadioButton maleRadioButton = new JRadioButton();
		maleRadioButton.setText(getResourceMap().getString("maleRadioButton.text")); // NOI18N
		enabledBinding = Bindings.createAutoBinding(UpdateStrategy.READ, clientTablePanel, ITEM_SELECTED, maleRadioButton, ENABLED);
		bindingGroup.addBinding(enabledBinding);
		add(maleRadioButton);

		JRadioButton femaleRadioButton = new JRadioButton();
		femaleRadioButton.setText(getResourceMap().getString("femaleRadioButton.text")); // NOI18N
		enabledBinding = Bindings.createAutoBinding(UpdateStrategy.READ, clientTablePanel, ITEM_SELECTED, femaleRadioButton, ENABLED);
		bindingGroup.addBinding(enabledBinding);
		add(femaleRadioButton, "wrap, gapright push");
		//create a model for the radio buttons
		EnumButtonGroup<Gender> ebg = new EnumButtonGroup<Gender>(Gender.class);
		ebg.add(Gender.FEMALE, femaleRadioButton);
		ebg.add(Gender.MALE, maleRadioButton);
		ebg.assertButtonGroupCoversAllEnumConstants();
		//add the binding for the radio buttons
		BeanProperty<ClientTablePanel, Gender> gender = BeanProperty.create("selectedItem.gender");
		Binding<? extends AbstractTablePanel<Client>, Gender, EnumButtonGroup<? extends Enum<?>>, ? extends Enum<?>> genderBinding = 
			Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, clientTablePanel, gender, ebg, EnumButtonGroup.SELECTED_ENUM_PROPERTY);
		bindingGroup.addBinding(genderBinding);

		JLabel addressLabel = new JLabel();
		addressLabel.setFont(AppSettings.MAIN_FONT);
		addressLabel.setText(getResourceMap().getString("addressLabel.text")); // NOI18N
		add(addressLabel);

		JTextField addressField = new JTextField();
		addressField.getDocument().addUndoableEditListener(undoListener);
		addressField.setFont(AppSettings.MAIN_FONT);
		BeanProperty<ClientTablePanel, String> address = BeanProperty.create("selectedItem.address.address");
		valueBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, clientTablePanel, address, addressField, TEXT);
		bindingGroup.addBinding(valueBinding);
		enabledBinding = Bindings.createAutoBinding(UpdateStrategy.READ, clientTablePanel, ITEM_SELECTED, addressField, ENABLED);
		bindingGroup.addBinding(enabledBinding);
		add(addressField, "span, wrap");

		JLabel telephoneLabel = new JLabel();
		telephoneLabel.setFont(AppSettings.MAIN_FONT);
		telephoneLabel.setText(getResourceMap().getString("telephoneLabel.text")); // NOI18N
		add(telephoneLabel);

		JTextField phoneField = new JTextField();
		phoneField.getDocument().addUndoableEditListener(undoListener);
		phoneField.setFont(AppSettings.MAIN_FONT);
		BeanProperty<ClientTablePanel, String> phone = BeanProperty.create("selectedItem.phoneNumber");
		valueBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, clientTablePanel, phone, phoneField, TEXT);
		bindingGroup.addBinding(valueBinding);
		enabledBinding = Bindings.createAutoBinding(UpdateStrategy.READ, clientTablePanel, ITEM_SELECTED, phoneField, ENABLED);
		bindingGroup.addBinding(enabledBinding);
		add(phoneField, "wrap, span");

		JLabel locationLabel = new JLabel();
		locationLabel.setFont(AppSettings.MAIN_FONT);
		locationLabel.setText(getResourceMap().getString("locationLabel.text")); // NOI18N
		add(locationLabel);		

		zipCodeField = new JComboBox();
		zipCodeField.setFont(AppSettings.MAIN_FONT);
		add(zipCodeField);

		locationField = new JComboBox();
		locationField.setFont(AppSettings.MAIN_FONT);
		add(locationField);

		bindingGroup.addBindingListener(changeListener);
		bindingGroup.bind();
	}

	public EventList<City> getItemList() {
		return itemList;
	}

	public void setItemList(final EventList<City> itemList) {
		if (this.itemList != null) {
			// dispose the current list, so it can be garbage collected
			this.itemList.dispose();
		}
		this.itemList = itemList;
		final TextMatcherEditor<City> matcherEditor = new TextMatcherEditor<City>(GlazedLists.toStringTextFilterator());
		final FilterList<City> filterList = new FilterList<City>(itemList, matcherEditor);
		if (zipCodeCompletion != null) {
			zipCodeCompletion.uninstall();
		}
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
		zipCodeCompletion.setBeepOnStrictViolation(false);
		zipCodeField.setRenderer(new CityInfoRenderer());

		BeanProperty<ClientTablePanel, City> city = BeanProperty.create("selectedItem.address.city");
		BeanProperty<JComboBox, City> selectedItem = BeanProperty.create("selectedItem");
		if (locationBindingGroup != null) {
			locationBindingGroup.unbind();
		}
		locationBindingGroup = new BindingGroup();

		Binding<ClientTablePanel, City, JComboBox, City> comboBoxBinding = 
			Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, getClientTablePanel(), city, zipCodeField, selectedItem);
		comboBoxBinding.setSourceNullValue(null);
		comboBoxBinding.setSourceUnreadableValue(null);
		locationBindingGroup.addBinding(comboBoxBinding);
		Binding<? extends AbstractTablePanel<Client>, Boolean, ? extends JComponent, Boolean> enabledBinding = null;
		enabledBinding = Bindings.createAutoBinding(UpdateStrategy.READ, getClientTablePanel(), ITEM_SELECTED, zipCodeField, ENABLED);
		locationBindingGroup.addBinding(enabledBinding);
		if (locationCompletion != null) {
			locationCompletion.uninstall();
		}
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
		locationCompletion.setBeepOnStrictViolation(false);				
		locationField.setRenderer(new CityInfoRenderer());

		comboBoxBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, getClientTablePanel(), city, locationField, selectedItem);
		comboBoxBinding.setSourceNullValue(null);
		comboBoxBinding.setSourceUnreadableValue(null);
		locationBindingGroup.addBinding(comboBoxBinding);
		enabledBinding = Bindings.createAutoBinding(UpdateStrategy.READ, getClientTablePanel(), ITEM_SELECTED, locationField, ENABLED);
		locationBindingGroup.addBinding(enabledBinding);
		locationBindingGroup.addBindingListener(changeListener);
		locationBindingGroup.bind();
	}

	private ClientTablePanel getClientTablePanel() {
		return clientTablePanel;
	}

	@Override
	public void requestFocus() {
		firstnameField.requestFocus();
	}

	public static class CityInfoRenderer extends DefaultListCellRenderer {

		@Override
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			JLabel result = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			if (value != null) {
				City city = (City) value;
				result.setText(city.getCode() + " " + city.getName());
			}
			return result;
		}
	}

}
