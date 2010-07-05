package com.runwalk.video.gui.panels;

import java.awt.Component;
import java.beans.Beans;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.Query;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.event.UndoableEditListener;

import net.miginfocom.swing.MigLayout;

import org.jdesktop.beansbinding.AbstractBindingListener;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Binding;
import org.jdesktop.beansbinding.BindingGroup;
import org.jdesktop.beansbinding.Bindings;
import org.jdesktop.beansbinding.Converter;
import org.jdesktop.beansbinding.ELProperty;
import org.jdesktop.beansbinding.PropertyStateEvent;
import org.jdesktop.beansbinding.Validator;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.swing.AutoCompleteSupport;

import com.runwalk.video.RunwalkVideoApp;
import com.runwalk.video.entities.City;
import com.runwalk.video.entities.Client;
import com.runwalk.video.util.AppSettings;

/**
 * TODO validatie met hibernate validators implementeren
 * verder opkuisen met 
 * 
 * @author Jeroen Peelaerts
 *
 */
@SuppressWarnings("serial")
public class ClientInfoPanel extends AppPanel {

	private JTextField nameField, firstnameField;

	@SuppressWarnings("unchecked")
	public ClientInfoPanel() {
		setLayout(new MigLayout("fill", "[right]rel[grow,fill]", "[]15[]"));
		
		//Create some undo and redo actions
		UndoableEditListener undoListener = RunwalkVideoApp.getApplication().getApplicationActions().getUndoableEditListener();

//		JTable clientTable = RunwalkVideoApp.getApplication().getClientTable();
		ClientTablePanel clientTablePanel = RunwalkVideoApp.getApplication().getClientTablePanel();
		BindingGroup bindingGroup = new BindingGroup();
		
		//component value binding
		BeanProperty<JTextField, String> textFieldValue = BeanProperty.create("text");
		BeanProperty<JTextField, String> textFieldValueOnFocusLost = BeanProperty.create("text_ON_FOCUS_LOST");
		Binding<? extends AbstractTablePanel<Client>, String, JTextField, String> valueBinding = null;

		//component enabling binding
		ELProperty<ClientTablePanel, Boolean> isSelected = ELProperty.create("${selectedItem != null}");
		BeanProperty<JComponent, Boolean> enabled = BeanProperty.create("enabled");
		Binding<? extends AbstractTablePanel<Client>, Boolean, ? extends JComponent, Boolean> enabledBinding = null;
		
		/**
		 * Converts first characters to uppercase
		 */
		class FirstCharacterToUpperCaseConverter extends Converter<String, String> {
			
			@Override
			public String convertForward(String arg0) {
				return arg0.length() > 0 ? Character.toUpperCase(arg0.charAt(0)) + arg0.substring(1) : arg0;
			}
			
			@Override
			public String convertReverse(String arg0) {
				return arg0.length() > 0 ? Character.toUpperCase(arg0.charAt(0)) + arg0.substring(1) : arg0;
			}
			
		};
		
		JLabel nameLabel = new JLabel();
		nameLabel.setFont(AppSettings.MAIN_FONT);
		nameLabel.setText(getResourceMap().getString("nameLabel.text")); // NOI18N
		add(nameLabel);

		firstnameField = new JTextField();
		firstnameField.setFont(AppSettings.MAIN_FONT);
		firstnameField.getDocument().addUndoableEditListener(undoListener);
		
		BeanProperty<ClientTablePanel, String> firstname = BeanProperty.create("selectedItem.firstname");
		valueBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, clientTablePanel, firstname, firstnameField, textFieldValue);
		valueBinding.setConverter(new FirstCharacterToUpperCaseConverter());
		bindingGroup.addBinding(valueBinding);
		enabledBinding = Bindings.createAutoBinding(UpdateStrategy.READ, clientTablePanel, isSelected, firstnameField, enabled);
		bindingGroup.addBinding(enabledBinding);
		add(firstnameField, "width :110:");
		
		nameField = new JTextField();
		nameField.getDocument().addUndoableEditListener(undoListener);
		nameField.setFont(AppSettings.MAIN_FONT);
		BeanProperty<ClientTablePanel, String> name = BeanProperty.create("selectedItem.name");
		valueBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, clientTablePanel, name, nameField, textFieldValue);
		valueBinding.setConverter(new FirstCharacterToUpperCaseConverter());

		bindingGroup.addBinding(valueBinding);
		enabledBinding = Bindings.createAutoBinding(UpdateStrategy.READ, clientTablePanel, isSelected, nameField, enabled);
		bindingGroup.addBinding(enabledBinding);
		add(nameField, "wrap");
		
		JLabel organizationLabel = new JLabel();
		organizationLabel.setFont(AppSettings.MAIN_FONT);
		organizationLabel.setText(getResourceMap().getString("organisationLabel.text")); // NOI18N
		add(organizationLabel);

		JTextField organisationField = new JTextField();
		organisationField.getDocument().addUndoableEditListener(undoListener);
		BeanProperty<ClientTablePanel, String> organization = BeanProperty.create("selectedItem.organization");
		valueBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, clientTablePanel, organization, organisationField, textFieldValue);
		valueBinding.setConverter(new FirstCharacterToUpperCaseConverter());
		bindingGroup.addBinding(valueBinding);
		enabledBinding = Bindings.createAutoBinding(UpdateStrategy.READ, clientTablePanel, isSelected, organisationField, enabled);
		bindingGroup.addBinding(enabledBinding);
		organisationField.setFont(AppSettings.MAIN_FONT);
		add(organisationField, "width :110:");
		
		JLabel taxLabel = new JLabel();
		taxLabel.setFont(AppSettings.MAIN_FONT);
		taxLabel.setText(getResourceMap().getString("btwLabel.text"));
		add(taxLabel, "split 2, width :20:");

		JTextField taxField = new JTextField();
		taxField.getDocument().addUndoableEditListener(undoListener);
		taxField.setFont(AppSettings.MAIN_FONT);
		BeanProperty<ClientTablePanel, String> taxNo = BeanProperty.create("selectedItem.btwnr");
		valueBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, clientTablePanel, taxNo, taxField, textFieldValue);
		valueBinding.setValidator(new Validator () {
			
			public Validator.Result validate(Object arg) {
				String regexPattern = "[0-9]{9}";
				Pattern pattern = Pattern.compile(regexPattern);
				Matcher matcher = pattern.matcher(arg.toString());
				if(!matcher.matches()){
					return new Result(null, "Btwnr. moet 9 cijfers bevatten");
				}
				return null;    
			}
		});
		bindingGroup.addBinding(valueBinding);
		enabledBinding = Bindings.createAutoBinding(UpdateStrategy.READ, clientTablePanel, isSelected, taxField, enabled);
		bindingGroup.addBinding(enabledBinding);
		add(taxField, "wrap");
		
		JLabel emailLabel = new JLabel();
		emailLabel.setFont(AppSettings.MAIN_FONT);
		emailLabel.setText(getResourceMap().getString("emailLabel.text")); // NOI18N
		add(emailLabel);

		JTextField emailField = new JTextField();
		emailField.getDocument().addUndoableEditListener(undoListener);
		emailField.setFont(AppSettings.MAIN_FONT);
		BeanProperty<ClientTablePanel, String> email = BeanProperty.create("selectedItem.emailAddress");
		valueBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, clientTablePanel, email, emailField, textFieldValue);
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
		enabledBinding = Bindings.createAutoBinding(UpdateStrategy.READ, clientTablePanel, isSelected, emailField, enabled);
		bindingGroup.addBinding(enabledBinding);
		add(emailField, "span, growx, wrap");
		
		JLabel addressLabel = new JLabel();
		addressLabel.setFont(AppSettings.MAIN_FONT);
		addressLabel.setText(getResourceMap().getString("addressLabel.text")); // NOI18N
		add(addressLabel);

		JTextField addressField = new JTextField();
		addressField.getDocument().addUndoableEditListener(undoListener);
		addressField.setFont(AppSettings.MAIN_FONT);
		BeanProperty<ClientTablePanel, String> address = BeanProperty.create("selectedItem.address.address");
		valueBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, clientTablePanel, address, addressField, textFieldValue);
		bindingGroup.addBinding(valueBinding);
		enabledBinding = Bindings.createAutoBinding(UpdateStrategy.READ, clientTablePanel, isSelected, addressField, enabled);
		bindingGroup.addBinding(enabledBinding);
		add(addressField, "span, growx, wrap");
		
		JLabel telephoneLabel = new JLabel();
		telephoneLabel.setFont(AppSettings.MAIN_FONT);
		telephoneLabel.setText(getResourceMap().getString("telephoneLabel.text")); // NOI18N
		add(telephoneLabel);

		JTextField phoneField = new JTextField();
		phoneField.getDocument().addUndoableEditListener(undoListener);
		phoneField.setFont(AppSettings.MAIN_FONT);
		BeanProperty<ClientTablePanel, String> phone = BeanProperty.create("selectedItem.phone");
		valueBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, clientTablePanel, phone, phoneField, textFieldValue);
		bindingGroup.addBinding(valueBinding);
		enabledBinding = Bindings.createAutoBinding(UpdateStrategy.READ, clientTablePanel, isSelected, phoneField, enabled);
		bindingGroup.addBinding(enabledBinding);
		add(phoneField, "wrap, span, growx");

		Query cityQuery = Beans.isDesignTime() ? null : RunwalkVideoApp.getApplication().getEntityManagerFactory().createEntityManager().createNamedQuery("findAllCities");
		EventList<City> cityList = GlazedLists.eventList(cityQuery.getResultList());
		
		BeanProperty<ClientTablePanel, City> city = BeanProperty.create("selectedItem.address.city");
		BeanProperty<JComboBox, City> selectedItem = BeanProperty.create("selectedItem");
		
		JLabel locationLabel = new JLabel();
		locationLabel.setFont(AppSettings.MAIN_FONT);
		locationLabel.setText(getResourceMap().getString("locationLabel.text")); // NOI18N
		add(locationLabel);		

		final JComboBox locationField = new JComboBox();
		Binding<ClientTablePanel, City, JComboBox, City> comboBoxBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, clientTablePanel, city, locationField, selectedItem);
		comboBoxBinding.setSourceNullValue(null);
		comboBoxBinding.setSourceUnreadableValue(null);
		bindingGroup.addBinding(comboBoxBinding);
		enabledBinding = Bindings.createAutoBinding(UpdateStrategy.READ, clientTablePanel, isSelected, locationField, enabled);
		bindingGroup.addBinding(enabledBinding);

		final JComboBox zipCodeField = new JComboBox();
		comboBoxBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, clientTablePanel, city, zipCodeField, selectedItem);
		comboBoxBinding.setSourceNullValue(null);
		comboBoxBinding.setSourceUnreadableValue(null);
		bindingGroup.addBinding(comboBoxBinding);
		enabledBinding = Bindings.createAutoBinding(UpdateStrategy.READ, clientTablePanel, isSelected, zipCodeField, enabled);
		bindingGroup.addBinding(enabledBinding);
		
		class CityInfoRenderer extends DefaultListCellRenderer {

			@Override
			public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
				JLabel result = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				City city = (City) value;
				result.setText(city.getCode() + " " + city.getName());
				return result;
			}
		}
		
		AutoCompleteSupport<City> locationCompletion = AutoCompleteSupport.install(locationField, cityList, GlazedLists.textFilterator("name"), new Format() {

			public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
				StringBuffer result = new StringBuffer();
				if (obj instanceof City) {
					result.append(((City) obj).getName());
				}
				return result;
			}

			public Object parseObject(String value, ParsePosition pos) {
				City selectedCity = (City) locationField.getSelectedItem();
				Query query = RunwalkVideoApp.getApplication().getEntityManagerFactory().createEntityManager().createNamedQuery("findByName");
				query.setParameter("name", value);
				List<?> resultList = query.getResultList();
				return resultList != null && !resultList.isEmpty() ? resultList.get(0) : selectedCity;
			}
			
		});
		locationCompletion.setStrict(true);
		locationCompletion.setBeepOnStrictViolation(false);
		locationField.setRenderer(new CityInfoRenderer());
		locationField.setFont(AppSettings.MAIN_FONT);
		add(locationField, "growx");

		AutoCompleteSupport<City> zipCodeCompletion = AutoCompleteSupport.install(zipCodeField, cityList, GlazedLists.textFilterator("code"), new Format() {

			public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
				StringBuffer result = new StringBuffer();
				if (obj instanceof City) {
					result.append(((City) obj).getCode());
				}
				return result;
			}

			public Object parseObject(String value, ParsePosition pos) {
				City selectedCity = (City) zipCodeField.getSelectedItem();
				List<?> resultList = null;
				if (selectedCity != null && !value.equals(selectedCity.getCode())) {
					Query query = RunwalkVideoApp.getApplication().getEntityManagerFactory().createEntityManager().createNamedQuery("findByZipCode");
					query.setParameter("zipCode", value);
					resultList = query.getResultList();
				}
				return resultList != null && resultList.size() > 0 ? resultList.get(0) : selectedCity;
			}
			
		});
		zipCodeCompletion.setStrict(true);
		zipCodeCompletion.setBeepOnStrictViolation(false);
		
		zipCodeField.setRenderer(new CityInfoRenderer());
		zipCodeField.setFont(AppSettings.MAIN_FONT);
		add(zipCodeField, "growx");

		bindingGroup.addBindingListener(new AbstractBindingListener() {

			@Override
			public void targetChanged(Binding binding, PropertyStateEvent event) {
				AbstractTablePanel tablePanel = (AbstractTablePanel) binding.getSourceObject();
				tablePanel.getSelectedItem().setDirty(true);
				getApplication().setSaveNeeded(true);
			}
			
		});
		bindingGroup.bind();
		
	}

	public void requestFocus() {
		firstnameField.requestFocus();
	}

}
