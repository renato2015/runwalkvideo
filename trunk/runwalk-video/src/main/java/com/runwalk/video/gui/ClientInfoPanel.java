package com.runwalk.video.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.persistence.Query;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.UndoableEditListener;

import org.jdesktop.application.Application;
import org.jdesktop.application.ResourceMap;
import org.netbeans.lib.awtextra.AbsoluteConstraints;

import com.runwalk.video.RunwalkVideoApp;
import com.runwalk.video.entities.City;
import com.runwalk.video.entities.Client;
import com.runwalk.video.util.ApplicationSettings;
import com.runwalk.video.util.ApplicationUtil;


public class ClientInfoPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private JTextField addressField, emailField, nameField, firstnameField, organizationField, taxField, phoneField;
	private JComboBox postcodeField, locationField;
	private List<City> cityList;

	private HashMap<String, City> locationMap, postalMap;

	public ClientInfoPanel() {
		setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
		ResourceMap resourceMap = Application.getInstance().getContext().getResourceMap(ClientInfoPanel.class);

		JLabel nameLabel = new JLabel();
		nameLabel.setFont(ApplicationSettings.MAIN_FONT);
		nameLabel.setText(resourceMap.getString("nameLabel.text")); // NOI18N
		add(nameLabel, new  AbsoluteConstraints(20, 20, -1, 20));

		JLabel organizationLabel = new JLabel();
		organizationLabel.setFont(ApplicationSettings.MAIN_FONT);
		organizationLabel.setText(resourceMap.getString("organisationLabel.text")); // NOI18N
		add(organizationLabel, new AbsoluteConstraints(20, 50, -1, 20));

		JLabel taxLabel = new JLabel();
		taxLabel.setFont(ApplicationSettings.MAIN_FONT);
		taxLabel.setText(resourceMap.getString("btwLabel.text"));
		add(taxLabel, new AbsoluteConstraints(275, 50, -1, 20));

		JLabel emailLabel = new JLabel();
		emailLabel.setFont(ApplicationSettings.MAIN_FONT);
		emailLabel.setText(resourceMap.getString("emailLabel.text")); // NOI18N
		add(emailLabel, new AbsoluteConstraints(20, 80, -1, 20));

		JLabel addressLabel = new JLabel();
		addressLabel.setFont(ApplicationSettings.MAIN_FONT);
		addressLabel.setText(resourceMap.getString("addressLabel.text")); // NOI18N
		add(addressLabel, new AbsoluteConstraints(20, 110, -1, 20));

		JLabel telephoneLabel = new JLabel();
		telephoneLabel.setFont(ApplicationSettings.MAIN_FONT);
		telephoneLabel.setText(resourceMap.getString("telephoneLabel.text")); // NOI18N
		add(telephoneLabel, new AbsoluteConstraints(20, 140, -1, 20));

		JLabel locationLabel = new JLabel();
		locationLabel.setFont(ApplicationSettings.MAIN_FONT);
		locationLabel.setText(resourceMap.getString("locationLabel.text")); // NOI18N
		add(locationLabel, new  AbsoluteConstraints(20, 170, -1, 20));		

		//Create some undo and redo actions
		UndoableEditListener undoListener = RunwalkVideoApp.getApplication().getApplicationActions().getUndoableEditListener();
		
		class CitySelectionListener implements ActionListener {
			private HashMap<String, City> map;

			public CitySelectionListener(HashMap<String, City> map) {
				this.map = map;
			}

			public void actionPerformed(ActionEvent e) {
				//only fire when selected
				String obj = ((JComboBox) e.getSource()).getSelectedItem().toString();
				if (obj.equals("")) {
					emptyLocationBoxes();
					return;
				}
				City selected = map.get(obj);
				if (selected == null) {
					//TODO Add postal code to database (later)
					return;
				}
				if (!selected.equals(RunwalkVideoApp.getApplication().getSelectedClient().getCity())) {
					RunwalkVideoApp.getApplication().getSelectedClient().setCity(selected);
					RunwalkVideoApp.getApplication().setSaveNeeded(true);
				}
				setCity(selected);			
			}
		}
		
		//Register all changes made in the text fields and enable saving accordingly
		KeyListener changeListener = new KeyListener() {
			public void keyPressed(KeyEvent e) { }
			public void keyReleased(KeyEvent e) { }
			public void keyTyped(KeyEvent e) {
				RunwalkVideoApp.getApplication().setSaveNeeded(true);
			}
		};

		firstnameField = new JTextField();
		firstnameField.setFont(ApplicationSettings.MAIN_FONT);
		firstnameField.getDocument().addUndoableEditListener(undoListener);
		firstnameField.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e) { }
			public void keyReleased(KeyEvent e) { 
				String firstname = ApplicationUtil.firstLettersToUpperCase(firstnameField.getText());
				setFirstname(firstname);
				RunwalkVideoApp.getApplication().getSelectedClient().setFirstname(firstname);
				RunwalkVideoApp.getApplication().getClientTableModel().updateSelectedRow();
			}
			public void keyTyped(KeyEvent e) {	}
		});
		firstnameField.addKeyListener(changeListener);
		add(firstnameField, new  AbsoluteConstraints(120, 20, 110, 20));

		nameField = new JTextField();
		nameField.setFont(ApplicationSettings.MAIN_FONT);
		nameField.getDocument().addUndoableEditListener(undoListener);
		nameField.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e) { }
			public void keyReleased(KeyEvent e) { 
				String name = ApplicationUtil.firstLettersToUpperCase(nameField.getText());
				setClientName(name);
				RunwalkVideoApp.getApplication().getSelectedClient().setName(name);
				RunwalkVideoApp.getApplication().getClientTableModel().updateSelectedRow();
			}
			public void keyTyped(KeyEvent e) {	}
		});
		nameField.addKeyListener(changeListener);
		add(nameField, new  AbsoluteConstraints(235, 20, 225, 20));

		organizationField = new JTextField();
		organizationField.getDocument().addUndoableEditListener(undoListener);
		organizationField.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e) { }
			public void keyReleased(KeyEvent e) {
				if (nameField.getText().equals("")) {
					String organization = ApplicationUtil.firstLettersToUpperCase(organizationField.getText());
					RunwalkVideoApp.getApplication().getSelectedClient().setOrganization(organization);
					RunwalkVideoApp.getApplication().getClientTableModel().updateSelectedRow();
				}
			}
			public void keyTyped(KeyEvent e) {	}
		});
		organizationField.addKeyListener(changeListener);
		organizationField.setFont(ApplicationSettings.MAIN_FONT);
		add(organizationField, new  AbsoluteConstraints(120, 50, 150, 20));

		taxField = new JTextField();
		taxField.getDocument().addUndoableEditListener(undoListener);
		taxField.addKeyListener(changeListener);
		taxField.setFont(ApplicationSettings.MAIN_FONT);
		add(taxField, new  AbsoluteConstraints(320, 50, 140, 20));

		emailField = new JTextField();
		emailField.getDocument().addUndoableEditListener(undoListener);
		emailField.addKeyListener(changeListener);
		emailField.setFont(ApplicationSettings.MAIN_FONT);
		add(emailField, new  AbsoluteConstraints(120, 80, 340, 20));

		addressField = new JTextField();
		addressField.getDocument().addUndoableEditListener(undoListener);
		addressField.addKeyListener(changeListener);
		addressField.setFont(ApplicationSettings.MAIN_FONT);
		add(addressField, new AbsoluteConstraints(120, 110, 340, 20));

		phoneField = new JTextField();
		phoneField.getDocument().addUndoableEditListener(undoListener);
		phoneField.addKeyListener(changeListener);
		phoneField.setFont(ApplicationSettings.MAIN_FONT);
		add(phoneField, new AbsoluteConstraints(120, 140, 120, 20));

		Query query = RunwalkVideoApp.getApplication().createQuery("select DISTINCT OBJECT(city) from City city"); // NOI18N
		cityList = query.getResultList();

		postalMap = new HashMap<String, City>();
		locationMap = new HashMap<String, City>();
		postalMap.put("", null);
		locationMap.put("", null);
		for(int i = 0; i < cityList.size();i++) {
			locationMap.put(cityList.get(i).getName(), cityList.get(i));
			postalMap.put("" + cityList.get(i).getCode(), cityList.get(i));
		}

		List<String> postcodes = new ArrayList<String>(postalMap.keySet());
		Collections.sort(postcodes);
		
		postcodeField = new Java2sAutoComboBox(postcodes);
		postcodeField.addKeyListener(changeListener);
		postcodeField.setFont(ApplicationSettings.MAIN_FONT);
		postcodeField.addActionListener(new CitySelectionListener(postalMap));
		add(postcodeField, new  AbsoluteConstraints(120, 170, 70, 20));

		List<String> locations = new ArrayList<String>(locationMap.keySet());
		Collections.sort(locations);
		locationField = new Java2sAutoComboBox(locations);
		locationField.setFont(ApplicationSettings.MAIN_FONT);
		locationField.addKeyListener(changeListener);
		locationField.addActionListener(new CitySelectionListener(locationMap));
		add(locationField, new AbsoluteConstraints(200, 170, 170, 20));
		
		setEnabled(false);
	}

	/**
	 * Save changes to the selected client record.
	 * @param theClient The client to be updated.
	 */
	public void showDetails(Client theClient) {
		phoneField.setText(theClient.getPhoneNumber());
		organizationField.setText(theClient.getOrganization());
		addressField.setText(theClient.getAddress());
		emailField.setText(theClient.getEmailAddress());
		setClientName(theClient.getName());
		setFirstname(theClient.getFirstname());
		setTaxField(theClient.getTaxNumber());
		setCity(theClient.getCity());
		RunwalkVideoApp.getApplication().getApplicationActions().discardAllEdits();
	}


	private void emptyLocationBoxes() {
		if (!postcodeField.getSelectedItem().toString().equals("")) {
			postcodeField.setSelectedItem("");
		}
		if (!locationField.getSelectedItem().toString().equals("")) {
			locationField.setSelectedItem("");
		}
		RunwalkVideoApp.getApplication().getSelectedClient().setCity(null);
	}

	private void setCity(City city) {
		if (city == null) {
			emptyLocationBoxes();
		} else {
			String code =  "" + city.getCode();
			if (!code.equals(postcodeField.getSelectedItem())) {
				postcodeField.setSelectedItem(code);
			}
			if (!city.getName().equals(locationField.getSelectedItem())) 
				locationField.setSelectedItem(city.getName());
		}
	}

	/**
	 * Save all changes made.
	 * @return a boolean that indicates whether all changed values were validated and changed.
	 */
	public boolean saveData() {
		if (!taxField.getText().equals("")) {
			if (!FieldValidation.isNumeric(taxField.getText()) || (taxField.getText().length() != 9)) {
				RunwalkVideoApp.getApplication().showError("Voer een geldige BTW nummer in!");
				return false;
			}
			RunwalkVideoApp.getApplication().getSelectedClient().setTaxNumber(Integer.parseInt(taxField.getText()));
		}
		if (!FieldValidation.isEmailValid(emailField.getText()) && !emailField.getText().equals("")) {
			RunwalkVideoApp.getApplication().showError("Voer een geldig e-mail adres in!");
			return false;
		}
		if (!FieldValidation.isNumeric(phoneField.getText()) && phoneField.getText().length() != 0 ) {
			RunwalkVideoApp.getApplication().showError("Voer een geldige telefoonnummer in: gebruik enkel cijfers.");
			return false;
		}
		RunwalkVideoApp.getApplication().getSelectedClient().setPhoneNumber(phoneField.getText());
		RunwalkVideoApp.getApplication().getSelectedClient().setOrganization(organizationField.getText());
		RunwalkVideoApp.getApplication().getSelectedClient().setEmailAddress(emailField.getText().toLowerCase());
		RunwalkVideoApp.getApplication().getSelectedClient().setAddress(addressField.getText());
		return true;
	}

	public void setFirstname(String firstname) {
		firstnameField.setText(firstname);
	}

	public void setClientName(String name) {
		nameField.setText(name);
	}

	private void setTaxField(Integer taxNumber) {
		String taxString = ""+taxNumber;
		if (taxString.equals("null") || taxString.equals("0")) {
			taxField.setText("");
		} else {
			taxField.setText(taxString);
		}
	}
	
	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		firstnameField.setEnabled(enabled);
		nameField.setEnabled(enabled);
		organizationField.setEnabled(enabled);
		taxField.setEnabled(enabled);
		emailField.setEnabled(enabled);
		addressField.setEnabled(enabled);
		phoneField.setEnabled(enabled);
		locationField.setEnabled(enabled);
		postcodeField.setEnabled(enabled);
	}


	@Override
	public void requestFocus() {
		super.requestFocus();
		firstnameField.requestFocus();
	}

}
