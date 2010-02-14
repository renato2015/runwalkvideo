package com.runwalk.video.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;

import javax.swing.ActionMap;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.text.DefaultEditorKit;

import org.jdesktop.application.Action;

import com.runwalk.video.util.ResourceInjector;

public class VideoMenuBar extends ComponentDecorator<JMenuBar> {

	private HashMap<JCheckBoxMenuItem, ComponentDecorator<? extends Container>> boxWindowMap = new HashMap<JCheckBoxMenuItem, ComponentDecorator<? extends Container>>();
	private HashMap<String, JCheckBoxMenuItem> windowBoxMap = new HashMap<String, JCheckBoxMenuItem>();
	private JMenu windowMenu;
	private JDialog aboutBox;

	public VideoMenuBar() {
		super(new JMenuBar());
		JSeparator separator = new JSeparator();
		JMenu fileMenu = new  JMenu(getResourceMap().getString("fileMenu.text"));
		JMenuItem newClientMenuItem = new JMenuItem(getApplication().getClientTablePanel().getAction("addClient"));
		fileMenu.add(newClientMenuItem);
		JMenuItem deleteClientMenuItem = new JMenuItem(getApplication().getClientTablePanel().getAction("deleteClient"));
		fileMenu.add(deleteClientMenuItem);
		fileMenu.add(separator);

		JMenuItem createAnalysisItem = new  JMenuItem( getApplication().getAnalysisTablePanel().getAction("addAnalysis"));
		fileMenu.add(createAnalysisItem);
		JMenuItem deleteAnalysisItem = new  JMenuItem( getApplication().getAnalysisTablePanel().getAction("deleteAnalysis"));
		fileMenu.add(deleteAnalysisItem);
		fileMenu.add(new JSeparator());

		JMenuItem refreshMenuItem = new  JMenuItem( getApplication().getClientTablePanel().getAction("refresh"));
		fileMenu.add(refreshMenuItem);
		JMenuItem saveMenuItem = new  JMenuItem( getApplication().getClientTablePanel().getAction("save"));
		fileMenu.add(saveMenuItem);

		//		JMenuItem perparesyncMenuItem = new JMenuItem( getApplication().getTableActionMap().get("preparesync"));
		//		fileMenu.add(perparesyncMenuItem);
		//		JMenuItem syncMenuItem = new  JMenuItem( getApplication().getTableActionMap().get("synchronize"));
		//		fileMenu.add(syncMenuItem);

		fileMenu.add(new JSeparator());
		JMenuItem exitMenuItem = new  JMenuItem( getApplication().getApplicationActionMap().get("exit"));
		fileMenu.add(exitMenuItem);
		getComponent().add(fileMenu);

		//the edit menu?
		JMenu editMenu = new JMenu(getResourceMap().getString("editMenu.text"));
		JMenuItem undo = new JMenuItem( getApplication().getApplicationActionMap().get("undo"));
		editMenu.add(undo);
		JMenuItem redo = new JMenuItem( getApplication().getApplicationActionMap().get("redo"));
		editMenu.add(redo);
		editMenu.add(new JSeparator());
		JMenuItem cut = new JMenuItem(ResourceInjector.injectResources(new DefaultEditorKit.CutAction(), "cut"));
		editMenu.add(cut);
		JMenuItem copy = new JMenuItem(ResourceInjector.injectResources(new DefaultEditorKit.CopyAction(), "copy"));
		editMenu.add(copy);
		JMenuItem paste = new JMenuItem(ResourceInjector.injectResources(new DefaultEditorKit.PasteAction(), "paste"));
		editMenu.add(paste);
		getComponent().add(editMenu);

		JMenu videoMenu = new JMenu(getResourceMap().getString("videoMenu.text"));
//		getComponent().add(videoMenu);

		windowMenu = new JMenu(getResourceMap().getString("windowMenu.text"));
		getComponent().add(windowMenu);

		JMenu helpMenu = new  JMenu(getResourceMap().getString("helpMenu.text"));
		JMenuItem aboutMenuItem = new JMenuItem( getAction("about"));
		JMenuItem uploadLogFiles = new JMenuItem( getApplication().getApplicationActionMap().get("uploadLogFiles"));
		helpMenu.add(uploadLogFiles);
		helpMenu.add(new JSeparator());
		helpMenu.add(aboutMenuItem);
		getComponent().add(helpMenu);
	}

	@Action
	public void about() {
		if (aboutBox == null) {
			aboutBox = new RunwalkVideoAboutBox(getApplication().getMainFrame()).getComponent();
		}
		aboutBox.setLocationRelativeTo(getApplication().getMainFrame());
		getApplication().show(aboutBox);
	}

	public void addWindow(ComponentDecorator<? extends Container> componentDecorator, boolean addToMenu) {
		Component component = componentDecorator.getComponent();
		if (component instanceof JInternalFrame) {
			final JInternalFrame frame = (JInternalFrame) component;
//			frame.addPropertyChangeListener(new ComponentPropertyChangeListener());
			frame.addInternalFrameListener(new InternalFrameAdapter() {

				@Override
				public void internalFrameDeactivated(InternalFrameEvent e) {
					setCheckboxSelection(frame);
				}


				@Override
				public void internalFrameActivated(InternalFrameEvent e) {
					setCheckboxSelection(frame);
				}

			});
		} else if (component instanceof Window) {
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
//			window.addPropertyChangeListener(new ComponentPropertyChangeListener());	
		}
		JCheckBoxMenuItem checkedItem = null;
		if (!boxWindowMap.containsValue(componentDecorator)) {
			JMenu menu = new JMenu(componentDecorator.getName());

			checkedItem = new JCheckBoxMenuItem(getAction("showWindow"));
			checkedItem.setSelected(component.isVisible());
			boxWindowMap.put(checkedItem, componentDecorator);
			windowBoxMap.put(componentDecorator.getName(), checkedItem);
			KeyStroke keyStroke = KeyStroke.getKeyStroke(Character.forDigit(windowBoxMap.size(), 9), ActionEvent.CTRL_MASK);
			checkedItem.setAccelerator(keyStroke);
			menu.add(checkedItem);

			ActionMap actionMap = componentDecorator.getActionMap();
			if (actionMap != null && actionMap.keys() != null && actionMap.keys().length > 0) {
				menu.add(new JSeparator());
				for (Object key : actionMap.allKeys()) {
					javax.swing.Action action = actionMap.get(key);
					if (getContext().getActionMap().get(key) == null) {
						JMenuItem item = new JMenuItem(action);
						if (action.getValue(javax.swing.Action.NAME) == null) {
							item.setText(action.getValue(javax.swing.Action.SHORT_DESCRIPTION).toString());
						}
						menu.add(item);
					}
				}
			}
			boolean isInternalFrame = component instanceof JInternalFrame;
			//TODO add internal frame instance at the end of the menu and after a separator..
			windowMenu.add(menu);
		} 
	}

	@Action
	public void showWindow(ActionEvent e) {
		JCheckBoxMenuItem selectedItem = (JCheckBoxMenuItem) e.getSource();
		boxWindowMap.get(selectedItem).setVisible(selectedItem.isSelected());
	}

	public void removeWindow(ComponentDecorator<? extends Container> componentDecorator) {
		JCheckBoxMenuItem boxItem = windowBoxMap.get(componentDecorator.getName());
		if (boxItem != null) {
			windowMenu.remove(boxItem);
			boxWindowMap.remove(boxItem);
			windowBoxMap.remove(componentDecorator.getName());
		}
	}

	public void setCheckboxSelection(Component component) {
		JCheckBoxMenuItem chckBox = windowBoxMap.get(component.getName());
		if (chckBox != null) {
			chckBox.setSelected(component.isVisible());
		}
	}

}
