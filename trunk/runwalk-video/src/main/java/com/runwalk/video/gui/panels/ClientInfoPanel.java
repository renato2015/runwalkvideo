package com.runwalk.video.gui.panels;

import java.awt.Component;
import java.beans.Beans;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.Query;
import javax.swing.ComboBoxEditor;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.event.UndoableEditListener;
import javax.swing.plaf.basic.BasicComboBoxEditor;

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
import org.jdesktop.el.impl.util.ReflectionUtil;
import org.jdesktop.observablecollections.ObservableCollections;
import org.jdesktop.swingbinding.JComboBoxBinding;
import org.jdesktop.swingbinding.SwingBindings;
import org.netbeans.lib.awtextra.AbsoluteConstraints;

import com.jidesoft.swing.AutoCompletion;
import com.jidesoft.swing.ComboBoxSearchable;
import com.runwalk.video.RunwalkVideoApp;
import com.runwalk.video.entities.City;
import com.runwalk.video.entities.Client;
import com.runwalk.video.util.AppSettings;

/**
 * TODO validatie met hibernate validators implementeren
 * verder opkuisen met 
 * 
 * @author jekkos
 *
 */
@SuppressWarnings("serial")
public class ClientInfoPanel extends AppPanel {

	private JTextField nameField, firstnameField;

	@SuppressWarnings("unchecked")
	public ClientInfoPanel() {
		setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
		
		JLabel nameLabel = new JLabel();
		nameLabel.setFont(AppSettings.MAIN_FONT);
		nameLabel.setText(getResourceMap().getString("nameLabel.text")); // NOI18N
		add(nameLabel, new  AbsoluteConstraints(20, 20, -1, 20));

		JLabel organizationLabel = new JLabel();
		organizationLabel.setFont(AppSettings.MAIN_FONT);
		organizationLabel.setText(getResourceMap().getString("organisationLabel.text")); // NOI18N
		add(organizationLabel, new AbsoluteConstraints(20, 50, -1, 20));

		JLabel taxLabel = new JLabel();
		taxLabel.setFont(AppSettings.MAIN_FONT);
		taxLabel.setText(getResourceMap().getString("btwLabel.text"));
		add(taxLabel, new AbsoluteConstraints(275, 50, -1, 20));

		JLabel emailLabel = new JLabel();
		emailLabel.setFont(AppSettings.MAIN_FONT);
		emailLabel.setText(getResourceMap().getString("emailLabel.text")); // NOI18N
		add(emailLabel, new AbsoluteConstraints(20, 80, -1, 20));

		JLabel addressLabel = new JLabel();
		addressLabel.setFont(AppSettings.MAIN_FONT);
		addressLabel.setText(getResourceMap().getString("addressLabel.text")); // NOI18N
		add(addressLabel, new AbsoluteConstraints(20, 110, -1, 20));

		JLabel telephoneLabel = new JLabel();
		telephoneLabel.setFont(AppSettings.MAIN_FONT);
		telephoneLabel.setText(getResourceMap().getString("telephoneLabel.text")); // NOI18N
		add(telephoneLabel, new AbsoluteConstraints(20, 140, -1, 20));

		JLabel locationLabel = new JLabel();
		locationLabel.setFont(AppSettings.MAIN_FONT);
		locationLabel.setText(getResourceMap().getString("locationLabel.text")); // NOI18N
		add(locationLabel, new  AbsoluteConstraints(20, 170, -1, 20));		

		//Create some undo and redo actions
		UndoableEditListener undoListener = RunwalkVideoApp.getApplication().getApplicationActions().getUndoableEditListener();

//		JTable clientTable = RunwalkVideoApp.getApplication().getClientTable();
		ClientTablePanel clientTable = RunwalkVideoApp.getApplication().getClientTablePanel();
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
		
		firstnameField = new JTextField();
		firstnameField.setFont(AppSettings.MAIN_FONT);
		firstnameField.getDocument().addUndoableEditListener(undoListener);
		BeanProperty<ClientTablePanel, String> firstname = BeanProperty.create("selectedItem.firstname");
		valueBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, clientTable, firstname, firstnameField, textFieldValue);
		valueBinding.setConverter(new FirstCharacterToUpperCaseConverter());
		bindingGroup.addBinding(valueBinding);
		enabledBinding = Bindings.createAutoBinding(UpdateStrategy.READ, clientTable, isSelected, firstnameField, enabled);
		bindingGroup.addBinding(enabledBinding);
		add(firstnameField, new  AbsoluteConstraints(120, 20, 110, 20));
		
		nameField = new JTextField();
		nameField.getDocument().addUndoableEditListener(undoListener);
		nameField.setFont(AppSettings.MAIN_FONT);
		BeanProperty<ClientTablePanel, String> name = BeanProperty.create("selectedItem.name");
		valueBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, clientTable, name, nameField, textFieldValue);
		valueBinding.setConverter(new FirstCharacterToUpperCaseConverter());

		bindingGroup.addBinding(valueBinding);
		enabledBinding = Bindings.createAutoBinding(UpdateStrategy.READ, clientTable, isSelected, nameField, enabled);
		bindingGroup.addBinding(enabledBinding);
		add(nameField, new  AbsoluteConstraints(235, 20, 225, 20));

		JTextField organisationField = new JTextField();
		organisationField.getDocument().addUndoableEditListener(undoListener);
		BeanProperty<ClientTablePanel, String> organization = BeanProperty.create("selectedItem.organization");
		valueBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, clientTable, organization, organisationField, textFieldValue);
		valueBinding.setConverter(new FirstCharacterToUpperCaseConverter());
		bindingGroup.addBinding(valueBinding);
		enabledBinding = Bindings.createAutoBinding(UpdateStrategy.READ, clientTable, isSelected, organisationField, enabled);
		bindingGroup.addBinding(enabledBinding);
		organisationField.setFont(AppSettings.MAIN_FONT);
		add(organisationField, new  AbsoluteConstraints(120, 50, 150, 20));

		JTextField taxField = new JTextField();
		taxField.getDocument().addUndoableEditListener(undoListener);
		taxField.setFont(AppSettings.MAIN_FONT);
		BeanProperty<ClientTablePanel, String> taxNo = BeanProperty.create("selectedItem.btwnr");
		valueBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, clientTable, taxNo, taxField, textFieldValue);
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
		enabledBinding = Bindings.createAutoBinding(UpdateStrategy.READ, clientTable, isSelected, taxField, enabled);
		bindingGroup.addBinding(enabledBinding);
		add(taxField, new  AbsoluteConstraints(320, 50, 140, 20));

		JTextField emailField = new JTextField();
		emailField.getDocument().addUndoableEditListener(undoListener);
		emailField.setFont(AppSettings.MAIN_FONT);
		BeanProperty<ClientTablePanel, String> email = BeanProperty.create("selectedItem.emailAddress");
		valueBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, clientTable, email, emailField, textFieldValue);
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
		enabledBinding = Bindings.createAutoBinding(UpdateStrategy.READ, clientTable, isSelected, emailField, enabled);
		bindingGroup.addBinding(enabledBinding);
		add(emailField, new  AbsoluteConstraints(120, 80, 340, 20));

		JTextField addressField = new JTextField();
		addressField.getDocument().addUndoableEditListener(undoListener);
		addressField.setFont(AppSettings.MAIN_FONT);
		BeanProperty<ClientTablePanel, String> address = BeanProperty.create("selectedItem.address.address");
		valueBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, clientTable, address, addressField, textFieldValue);
		bindingGroup.addBinding(valueBinding);
		enabledBinding = Bindings.createAutoBinding(UpdateStrategy.READ, clientTable, isSelected, addressField, enabled);
		bindingGroup.addBinding(enabledBinding);
		add(addressField, new AbsoluteConstraints(120, 110, 340, 20));

		JTextField phoneField = new JTextField();
		phoneField.getDocument().addUndoableEditListener(undoListener);
		phoneField.setFont(AppSettings.MAIN_FONT);
		BeanProperty<ClientTablePanel, String> phone = BeanProperty.create("selectedItem.phone");
		valueBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, clientTable, phone, phoneField, textFieldValue);
		bindingGroup.addBinding(valueBinding);
		enabledBinding = Bindings.createAutoBinding(UpdateStrategy.READ, clientTable, isSelected, phoneField, enabled);
		bindingGroup.addBinding(enabledBinding);
		add(phoneField, new AbsoluteConstraints(120, 140, 120, 20));

		Query cityQuery = Beans.isDesignTime() ? null : RunwalkVideoApp.getApplication().getEntityManagerFactory().createEntityManager().createNamedQuery("findAllCities");
		List<City> cityList = ObservableCollections.observableList(cityQuery.getResultList());
		
		BeanProperty<ClientTablePanel, City> city = BeanProperty.create("selectedItem.address.city");
		BeanProperty<JComboBox, City> selectedItem = BeanProperty.create("selectedItem");

		final JComboBox locationField = new JComboBox();
		JComboBoxBinding<City, List<City>, JComboBox> cityBinding = SwingBindings.createJComboBoxBinding(UpdateStrategy.READ_WRITE, cityList, locationField);
		bindingGroup.addBinding(cityBinding);
		Binding<ClientTablePanel, City, JComboBox, City> comboBoxBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, clientTable, city, locationField, selectedItem);
		comboBoxBinding.setSourceNullValue(null);
		comboBoxBinding.setSourceUnreadableValue(null);
		bindingGroup.addBinding(comboBoxBinding);
		enabledBinding = Bindings.createAutoBinding(UpdateStrategy.READ, clientTable, isSelected, locationField, enabled);
		bindingGroup.addBinding(enabledBinding);

		final JComboBox zipcodeField = new JComboBox();
		cityBinding = SwingBindings.createJComboBoxBinding(UpdateStrategy.READ_WRITE, cityList, zipcodeField);
		bindingGroup.addBinding(cityBinding);
		comboBoxBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, clientTable, city, zipcodeField, selectedItem);
		comboBoxBinding.setSourceNullValue(null);
		comboBoxBinding.setSourceUnreadableValue(null);
		bindingGroup.addBinding(comboBoxBinding);
		enabledBinding = Bindings.createAutoBinding(UpdateStrategy.READ, clientTable, isSelected, zipcodeField, enabled);
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

		class CityInfoEditor extends BasicComboBoxEditor {

			private String methodName;
			private Class<?> methodClass;

			public CityInfoEditor(ComboBoxEditor origEditor, Class<?> methodClass, String methodName) {
				super();
				this.methodName = methodName;
				this.methodClass = methodClass;
				editor.setBorder(((JComponent) origEditor.getEditorComponent()).getBorder());
			}

			@Override
			public void setItem(Object anObject) {
				if (anObject != null && methodClass.isAssignableFrom(anObject.getClass())) {
					Method method = ReflectionUtil.getMethod(anObject, methodName, new Class<?>[] {});
					try {
						super.setItem(method.invoke(anObject, new Object[] {}));
					} catch (IllegalAccessException e) {
						getLogger().error(e);
					} catch (InvocationTargetException e) {
						getLogger().error(e);
					}
				} else {
					super.setItem(anObject);
				}
			}

			@Override
			public Object getItem() {
				Object value = super.getItem();
				City selectedCity = null;
				List<?> resultList = null;
				if (value instanceof String) {
					selectedCity = (City) locationField.getSelectedItem();
					Query query = RunwalkVideoApp.getApplication().getEntityManagerFactory().createEntityManager().createNamedQuery("findByName");
					query.setParameter("name", value);
					resultList = query.getResultList();
				} else if (int.class.isAssignableFrom(value.getClass()) || value instanceof Integer) {
					selectedCity = (City) zipcodeField.getSelectedItem();
					if (!value.equals(selectedCity.getCode())) {
						Query query = RunwalkVideoApp.getApplication().getEntityManagerFactory().createEntityManager().createNamedQuery("findByZipCode");
						query.setParameter("zipCode", value);
						resultList = query.getResultList();
					}
				}
				return resultList != null && resultList.size() > 0 ? resultList.get(0) : selectedCity;
			}
		};
		
		locationField.setEditor(new CityInfoEditor(locationField.getEditor(), City.class, "getName"));
		locationField.setRenderer(new CityInfoRenderer());
		locationField.setEditable(true);
		locationField.setFont(AppSettings.MAIN_FONT);
		add(locationField, new AbsoluteConstraints(250, 170, 170, 20));

		new AutoCompletion(locationField, new ComboBoxSearchable(locationField) {
			@Override
			protected String convertElementToString(Object object) {
				return ((City) object).getName();
			}
		});

		zipcodeField.setEditor(new CityInfoEditor(zipcodeField.getEditor(), City.class, "getCode"));
		zipcodeField.setRenderer(new CityInfoRenderer());
		zipcodeField.setEditable(true);
		zipcodeField.setFont(AppSettings.MAIN_FONT);
		add(zipcodeField, new  AbsoluteConstraints(120, 170, 120, 20));

		new AutoCompletion(zipcodeField, new ComboBoxSearchable(zipcodeField) {
			@Override
			protected String convertElementToString(Object object) {
				return Integer.toString(((City) object).getCode());
			}
		});
		bindingGroup.bind();
		
	}

	public void requestFocus() {
		firstnameField.requestFocus();
	}

}