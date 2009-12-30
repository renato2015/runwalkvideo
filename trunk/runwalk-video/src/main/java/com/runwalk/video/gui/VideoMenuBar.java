package com.runwalk.video.gui;

import java.awt.Component;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.text.DefaultEditorKit;

import org.jdesktop.application.Application;
import org.jdesktop.application.ResourceMap;

import com.runwalk.video.RunwalkVideoApp;
import com.runwalk.video.gui.actions.ResourceInjector;

public class VideoMenuBar extends JMenuBar implements PropertyChangeListener {

	private static final long serialVersionUID = 1L;

	private HashMap<JCheckBoxMenuItem, Component> boxWindowMap = new HashMap<JCheckBoxMenuItem, Component>();
	private HashMap<Component, JCheckBoxMenuItem> windowBoxMap = new HashMap<Component, JCheckBoxMenuItem>();
	private JMenu windowMenu;

	public VideoMenuBar() {
		ResourceMap resourceMap = Application.getInstance().getContext().getResourceMap(VideoMenuBar.class);

		JSeparator separator = new JSeparator();
		JMenu fileMenu = new  JMenu(resourceMap.getString("fileMenu.text"));
		JMenuItem newClientMenuItem = new JMenuItem(RunwalkVideoApp.getApplication().getTableActionMap().get("addClient"));
		fileMenu.add(newClientMenuItem);
		JMenuItem deleteClientMenuItem = new JMenuItem(RunwalkVideoApp.getApplication().getTableActionMap().get("deleteClient"));
		fileMenu.add(deleteClientMenuItem);
		fileMenu.add(separator);

		JMenuItem createAnalysisItem = new  JMenuItem(RunwalkVideoApp.getApplication().getTableActionMap().get("addAnalysis"));
		fileMenu.add(createAnalysisItem);
		JMenuItem deleteAnalysisItem = new  JMenuItem(RunwalkVideoApp.getApplication().getTableActionMap().get("deleteAnalysis"));
		fileMenu.add(deleteAnalysisItem);
		fileMenu.add(new JSeparator());

		JMenuItem refreshMenuItem = new  JMenuItem(RunwalkVideoApp.getApplication().getApplicationActionMap().get("refresh"));
		fileMenu.add(refreshMenuItem);
		JMenuItem saveMenuItem = new  JMenuItem(RunwalkVideoApp.getApplication().getApplicationActionMap().get("save"));
		fileMenu.add(saveMenuItem);

		//		JMenuItem perparesyncMenuItem = new JMenuItem(RunwalkVideoApp.getApplication().getTableActionMap().get("preparesync"));
		//		fileMenu.add(perparesyncMenuItem);
		//		JMenuItem syncMenuItem = new  JMenuItem(RunwalkVideoApp.getApplication().getTableActionMap().get("synchronize"));
		//		fileMenu.add(syncMenuItem);

		fileMenu.add(new JSeparator());
		JMenuItem exitMenuItem = new  JMenuItem(RunwalkVideoApp.getApplication().getApplicationActionMap().get("exit"));
		fileMenu.add(exitMenuItem);
		add(fileMenu);

		//the edit menu?
		JMenu editMenu = new JMenu(resourceMap.getString("editMenu.text"));
		JMenuItem undo = new JMenuItem(RunwalkVideoApp.getApplication().getApplicationActionMap().get("undo"));
		editMenu.add(undo);
		JMenuItem redo = new JMenuItem(RunwalkVideoApp.getApplication().getApplicationActionMap().get("redo"));
		editMenu.add(redo);
		editMenu.add(new JSeparator());
		JMenuItem cut = new JMenuItem(ResourceInjector.injectResources(new DefaultEditorKit.CutAction(), "cut"));
		editMenu.add(cut);
		JMenuItem copy = new JMenuItem(ResourceInjector.injectResources(new DefaultEditorKit.CopyAction(), "copy"));
		editMenu.add(copy);
		JMenuItem paste = new JMenuItem(ResourceInjector.injectResources(new DefaultEditorKit.PasteAction(), "paste"));
		editMenu.add(paste);
		add(editMenu);
		
		//TODO refactor this into an Action object..
		JMenu videoMenu = new JMenu(resourceMap.getString("videoMenu.text"));
		JMenuItem cameraItem = new JMenuItem("startCapturer");
		JMenuItem initCameraItem = new JMenuItem("initCaptureGraph");
		
		JCheckBoxMenuItem rejectFilterItem = new JCheckBoxMenuItem("Verwijder Pause Filter");
		rejectFilterItem.setSelected(RunwalkVideoApp.getApplication().getPlayer().rejectPauseFilter());
		rejectFilterItem.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				boolean rejectPauseFilter = (e.getStateChange() == ItemEvent.SELECTED) ? true : false;
				RunwalkVideoApp.getApplication().getPlayer().setRejectPauseFilter(rejectPauseFilter);
			}
		});
		videoMenu.add(rejectFilterItem);
		videoMenu.add(new JSeparator());
		videoMenu.add(new JMenuItem(RunwalkVideoApp.getApplication().getPlayerActionMap().get("setCaptureEncoder")));
		videoMenu.add(new JMenuItem(RunwalkVideoApp.getApplication().getPlayerActionMap().get("setFrameRate")));
		videoMenu.add(new JMenuItem(RunwalkVideoApp.getApplication().getPlayerActionMap().get("viewFilterProperties")));
		videoMenu.add(new JSeparator());
		videoMenu.add(cameraItem);
		videoMenu.add(initCameraItem);
		videoMenu.add(RunwalkVideoApp.getApplication().getPlayerActionMap().get("toggleCamera"));
		videoMenu.add(new JSeparator());
		videoMenu.add(RunwalkVideoApp.getApplication().getApplicationActionMap().get("selectVideoDir"));
		
		add(videoMenu);

		windowMenu = new JMenu(resourceMap.getString("windowMenu.text"));
		add(windowMenu);

		JMenu helpMenu = new  JMenu(resourceMap.getString("helpMenu.text"));
		JMenuItem aboutMenuItem = new JMenuItem(RunwalkVideoApp.getApplication().getApplicationActionMap().get("about"));
		JMenuItem uploadLogFiles = new JMenuItem(RunwalkVideoApp.getApplication().getApplicationActionMap().get("uploadLogFiles"));
		helpMenu.add(uploadLogFiles);
		helpMenu.add(new JSeparator());
		helpMenu.add(aboutMenuItem);
		add(helpMenu);
	}


	public void addWindow(Component component) {
		if (component instanceof JInternalFrame) {
			final JInternalFrame frame = (JInternalFrame) component;
			frame.addInternalFrameListener(new InternalFrameAdapter() {

				@Override
				public void internalFrameClosed(InternalFrameEvent e) {
					setCheckboxSelection(frame);
				}

				@Override
				public void internalFrameOpened(InternalFrameEvent e) {
					setCheckboxSelection(frame);
				}
			});
		}
		else if (component instanceof Window) {
			final Window window = (Window) component;
			window.addWindowListener(new WindowAdapter() {

				@Override
				public void windowClosed(WindowEvent e) {
					setCheckboxSelection(window);
				}

				@Override
				public void windowOpened(WindowEvent e) {
					setCheckboxSelection(window);
				}
			});
			window.addPropertyChangeListener(this);	
		}
		JCheckBoxMenuItem checkedItem = new JCheckBoxMenuItem(component.getName());
		checkedItem.setSelected(component.isVisible());
		boxWindowMap.put(checkedItem, component);
		windowBoxMap.put(component, checkedItem);
		checkedItem.setAccelerator(KeyStroke.getKeyStroke(Character.forDigit(windowBoxMap.size(), 9), ActionEvent.CTRL_MASK));
		checkedItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JCheckBoxMenuItem selectedItem = (JCheckBoxMenuItem) e.getSource();
				boxWindowMap.get(selectedItem).setVisible(selectedItem.isSelected());
			}
		});
		windowMenu.add(checkedItem);
	}

	public void removeWindow(Component component) {
		JCheckBoxMenuItem boxMenuItem = windowBoxMap.get(component);
		windowMenu.remove(boxMenuItem);
		boxWindowMap.remove(boxMenuItem);
		windowBoxMap.remove(component);
	}

	public void setCheckboxSelection(Component component) {
		JCheckBoxMenuItem chckBox = windowBoxMap.get(component);
		if (chckBox != null) {
			chckBox.setSelected(component.isVisible());
		}
	}


	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getSource() instanceof Component) {
			Component source = (Component) evt.getSource();
			if (windowBoxMap.containsKey(source)) {
				JCheckBoxMenuItem item = windowBoxMap.get(source);
				item.setText(source.getName());
				item.validate();
			}
		}


	}

}
