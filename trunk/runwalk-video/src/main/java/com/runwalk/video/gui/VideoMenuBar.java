package com.runwalk.video.gui;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.ActionMap;
import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;
import javax.swing.text.DefaultEditorKit;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.runwalk.video.RunwalkVideoApp;
import com.runwalk.video.gui.media.VideoComponent;
import com.runwalk.video.gui.media.VideoComponent.State;
import com.runwalk.video.util.AppUtil;
import com.runwalk.video.util.ResourceInjector;
import com.tomtessier.scrollabledesktop.BaseInternalFrame;

@SuppressWarnings("serial")
public class VideoMenuBar extends JMenuBar implements AppComponent, PropertyChangeListener, ComponentListener {

	private BiMap<AppWindowWrapper, JCheckBoxMenuItem> windowBoxMap = HashBiMap.create();
	private JMenu windowMenu;
	private JDialog aboutBox;

	public VideoMenuBar() {

		JSeparator separator = new JSeparator();
		JMenu fileMenu = new  JMenu(getResourceMap().getString("fileMenu.text"));
		JMenuItem newClientMenuItem = new JMenuItem(getApplication().getClientTablePanel().getAction("addClient"));
		fileMenu.add(newClientMenuItem);
		JMenuItem deleteClientMenuItem = new JMenuItem(getApplication().getClientTablePanel().getAction("deleteClient"));
		fileMenu.add(deleteClientMenuItem);
		fileMenu.add(separator);

		JMenuItem createAnalysisItem = new JMenuItem( getApplication().getAnalysisTablePanel().getAction("addAnalysis"));
		fileMenu.add(createAnalysisItem);
		JMenuItem deleteAnalysisItem = new JMenuItem( getApplication().getAnalysisTablePanel().getAction("deleteAnalysis"));
		fileMenu.add(deleteAnalysisItem);

		fileMenu.add(new JSeparator());
		JMenuItem showVideoFileItem = new JMenuItem( getApplication().getAnalysisTablePanel().getAction("showVideoFile"));
		fileMenu.add(showVideoFileItem);
		//		JMenuItem openVideoFileItem = new JMenuItem( getApplication().getAnalysisTablePanel().getAction("openVideoFile"));
		//		fileMenu.add(openVideoFileItem);

		fileMenu.add(new JSeparator());
		JMenuItem refreshMenuItem = new JMenuItem( getAction(RunwalkVideoApp.REFRESH_ACTION));
		fileMenu.add(refreshMenuItem);
		JMenuItem saveMenuItem = new JMenuItem( getApplication().getClientTablePanel().getAction("save"));
		fileMenu.add(saveMenuItem);
		JMenuItem saveSettingsMenuItem = new JMenuItem( getAction(RunwalkVideoApp.SAVE_SETTINGS_ACTION));
		fileMenu.add(saveSettingsMenuItem);

		fileMenu.add(new JSeparator());
		JMenuItem selectVideoDir = new JMenuItem( getApplication().getAnalysisOverviewTablePanel().getAction("selectVideoDir"));
		fileMenu.add(selectVideoDir);
		JMenuItem selectUncompressedVideoDir = new JMenuItem( getApplication().getAnalysisOverviewTablePanel().getAction("selectUncompressedVideoDir"));
		fileMenu.add(selectUncompressedVideoDir);

		fileMenu.add(new JSeparator());
		JMenuItem organiseVideoFiles = new JMenuItem( getApplication().getAnalysisOverviewTablePanel().getAction("organiseVideoFiles"));
		fileMenu.add(organiseVideoFiles);

		fileMenu.add(new JSeparator());
		JMenuItem exitMenuItem = new JMenuItem( getAction(RunwalkVideoApp.EXIT_ACTION) );
		fileMenu.add(exitMenuItem);
		add(fileMenu);

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
		add(editMenu);

		//		JMenu videoMenu = new JMenu(getResourceMap().getString("videoMenu.text"));
		//		getComponent().add(videoMenu);

		windowMenu = new JMenu(getResourceMap().getString("windowMenu.text"));
		add(windowMenu);

		JMenu helpMenu = new  JMenu(getResourceMap().getString("helpMenu.text"));
		JMenuItem aboutMenuItem = new JMenuItem( getAction("about"));
		JMenuItem uploadLogFiles = new JMenuItem( getAction(RunwalkVideoApp.UPLOAD_LOG_FILES_ACTION));
		helpMenu.add(uploadLogFiles);
		helpMenu.add(new JSeparator());
		helpMenu.add(aboutMenuItem);
		add(helpMenu);
	}

	@org.jdesktop.application.Action
	public void about() {
		if (aboutBox == null) {
			aboutBox = new RunwalkVideoAboutDialog(getApplication().getMainFrame());
		}
		aboutBox.setLocationRelativeTo(getApplication().getMainFrame());
		getApplication().show(aboutBox);
	}

	public void addWindow(final AppWindowWrapper appComponent) {
 		if (!windowBoxMap.containsKey(appComponent)) {
			appComponent.addComponentListener(this);
			appComponent.addPropertyChangeListener(this);
			JMenu menu = createMenu(appComponent);
			//TODO add internal frame instance at the end of the menu and after a separator..
			windowMenu.add(menu);
		} 
	}

	private JMenu createMenu(AppWindowWrapper appComponent) {
		JMenu result = new JMenu(appComponent.getTitle());
		JCheckBoxMenuItem checkedItem = new JCheckBoxMenuItem(getAction("showWindow"));
		Component component = appComponent.getHolder();
		checkedItem.setSelected(component.isVisible());
		windowBoxMap.put(appComponent, checkedItem);
		windowBoxMap.inverse().put(checkedItem, appComponent);
		char shortcut = Character.forDigit(windowBoxMap.size(), 9);
		KeyStroke keyStroke = KeyStroke.getKeyStroke(shortcut, ActionEvent.CTRL_MASK);
		checkedItem.setAccelerator(keyStroke);
		result.add(checkedItem);
		// add all actions from the appcomponent's actionmap to the menu
		ActionMap actionMap = appComponent.getApplicationActionMap();
		if (actionMap != null && actionMap.allKeys() != null && actionMap.allKeys().length > 0) {
			result.add(new JSeparator());
			for (Object key : actionMap.allKeys()) {
				Action action = actionMap.get(key);
				if (getContext().getActionMap().get(key) == null) {
					result.add(createMenuItem(action));
				}
			}
		}
		return result;
	}
	
	private JMenuItem createMenuItem(Action action) {
		JMenuItem result = null;
		Object commandKey = action.getValue(Action.ACTION_COMMAND_KEY);
		if (commandKey != null && commandKey.toString().startsWith("toggle")) {
			result = new JCheckBoxMenuItem(action);
		} else {
			result = new JMenuItem(action);
		}
		Object actionDescription = action.getValue(javax.swing.Action.NAME);
		if (actionDescription == null) {
			actionDescription = action.getValue(javax.swing.Action.SHORT_DESCRIPTION);
		}
		result.setText(actionDescription.toString());
		return result;
	}

	@org.jdesktop.application.Action
	public void showWindow(ActionEvent e) {
		JCheckBoxMenuItem selectedItem = (JCheckBoxMenuItem) e.getSource();
		AppWindowWrapper component  = windowBoxMap.inverse().get(selectedItem);
		component.getHolder().setVisible(selectedItem.isSelected());
	}

	private void removeWindow(AppWindowWrapper appComponent) {
		JCheckBoxMenuItem boxItem = windowBoxMap.get(appComponent);
		if (boxItem != null) {
			for (int i = 0; i < windowMenu.getMenuComponentCount(); i++) {
				Component menuComponent = windowMenu.getMenuComponent(i);
				if (menuComponent instanceof JMenuItem ) {
					JMenuItem menuItem = (JMenuItem) menuComponent;
					if (menuItem.getText().equals(appComponent.getTitle())) {
						windowMenu.remove(menuItem);
					}
				}
			}
			windowBoxMap.remove(appComponent);
			windowMenu.updateUI();
			windowMenu.revalidate();
		}
	}

	private void hideWindow(AppWindowWrapper appComponent) {
		Container container = appComponent == null ? null : appComponent.getHolder();
		if (container != null) {
			if (container instanceof BaseInternalFrame) {
				BaseInternalFrame baseInternalFrame = (BaseInternalFrame) container;
				baseInternalFrame.getAssociatedButton().setEnabled(false);
//				baseInternalFrame.getAssociatedMenuButton().setEnabled(false);
			}
			container.setVisible(false);
		}
	}

	private void setCheckboxSelection(Component component) {
		AppWindowWrapper appComponent = AppUtil.getWindowWrapper(windowBoxMap.keySet(), component);
		JCheckBoxMenuItem checkBox = windowBoxMap.get(appComponent);
		if (checkBox != null) {
			checkBox.setSelected(component.isVisible());
		}
	}

	public String getTitle() {
		return "Menu Bar";
	}

	public void propertyChange(PropertyChangeEvent evt) {
		// hiding and closing windows is handled by listening to the components' events
		if (VideoComponent.STATE.equals(evt.getPropertyName())) {
			VideoComponent.State newState = (State) evt.getNewValue();
			if (VideoComponent.State.DISPOSED.equals(newState)) {
				AppWindowWrapper appComponent = (AppWindowWrapper) evt.getSource();
				removeWindow(appComponent);
				appComponent.removeComponentListener(this);
				appComponent.removePropertyChangeListener(this);
			}
		} else if (VideoComponent.FULL_SCREEN.equals(evt.getPropertyName())) {
			Boolean fullScreen = (Boolean) evt.getNewValue();
			if (fullScreen) {
				AppWindowWrapper appComponent = (AppWindowWrapper) evt.getSource();
				hideWindow(appComponent);
			}
		}
	}


	public void componentShown(ComponentEvent e) {
		setCheckboxSelection(e.getComponent());
	}

	public void componentHidden(ComponentEvent e) {
		setCheckboxSelection(e.getComponent());
	}
	
	public void componentResized(ComponentEvent e) { }
	
	public void componentMoved(ComponentEvent e) { }
	
}
