package com.runwalk.video.ui;

import java.awt.Component;
import java.awt.Window;
import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import org.jdesktop.application.ApplicationAction;

import com.runwalk.video.core.AppComponent;
import com.runwalk.video.core.OnEdt;
import com.runwalk.video.core.WindowConstants;
import com.runwalk.video.ui.actions.ApplicationActionConstants;
import com.runwalk.video.util.ResourceInjector;

@SuppressWarnings("serial")
@AppComponent
public class VideoMenuBar extends JMenuBar implements ApplicationActionConstants, WindowConstants {

	private static final String MENU_BAR_TITLE = "Menu Bar";
	private JMenu windowMenu;
	private JDialog aboutBox;

	public VideoMenuBar() {
		// TODO generate file menu using the tablePanels list here!
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
		JMenuItem refreshMenuItem = new JMenuItem( getAction(REFRESH_ACTION));
		fileMenu.add(refreshMenuItem);
		JMenuItem saveMenuItem = new JMenuItem( getApplication().getClientTablePanel().getAction("save"));
		fileMenu.add(saveMenuItem);
		JMenuItem saveSettingsMenuItem = new JMenuItem( getAction(SAVE_SETTINGS_ACTION));
		fileMenu.add(saveSettingsMenuItem);

		fileMenu.add(new JSeparator());
		JMenuItem selectVideoDir = new JMenuItem( getApplication().getRecordingTablePanel().getAction("selectVideoDir"));
		fileMenu.add(selectVideoDir);
		JMenuItem selectUncompressedVideoDir = new JMenuItem( getApplication().getRecordingTablePanel().getAction("selectUncompressedVideoDir"));
		fileMenu.add(selectUncompressedVideoDir);

		//fileMenu.add(new JSeparator());
		JMenuItem organiseVideoFiles = new JMenuItem( getApplication().getRecordingTablePanel().getAction("organiseVideoFiles"));
		fileMenu.add(organiseVideoFiles);

		fileMenu.add(new JSeparator());
		JMenuItem exitMenuItem = new JMenuItem( getAction(QUIT_ACTION) );
		fileMenu.add(exitMenuItem);
		add(fileMenu);

		//the edit menu?
		JMenu editMenu = new JMenu(getResourceMap().getString("editMenu.text"));
		JMenuItem undo = new JMenuItem( getApplication().getApplicationActionMap().get("undo"));
		editMenu.add(undo);
		JMenuItem redo = new JMenuItem( getApplication().getApplicationActionMap().get("redo"));
		editMenu.add(redo);
		editMenu.add(new JSeparator());
		ResourceInjector resourceInjector = ResourceInjector.getInstance();

		JMenuItem cut = new JMenuItem(resourceInjector.injectResources(getAction("cut")));
		editMenu.add(cut);
		JMenuItem copy = new JMenuItem(resourceInjector.injectResources(getAction("copy")));
		editMenu.add(copy);
		JMenuItem paste = new JMenuItem(resourceInjector.injectResources(getAction("paste")));
		editMenu.add(paste);
		add(editMenu);

		//		JMenu videoMenu = new JMenu(getResourceMap().getString("videoMenu.text"));
		//		getComponent().add(videoMenu);

		windowMenu = new JMenu(getResourceMap().getString("windowMenu.text"));
		add(windowMenu);

		JMenu helpMenu = new  JMenu(getResourceMap().getString("helpMenu.text"));
		JMenuItem aboutMenuItem = new JMenuItem( getAction(ABOUT_ACTION));
		JMenuItem uploadLogFiles = new JMenuItem( getAction(UPLOAD_LOG_FILES_ACTION));
		helpMenu.add(uploadLogFiles);
		helpMenu.add(new JSeparator());
		helpMenu.add(aboutMenuItem);
		add(helpMenu);
	}

	@org.jdesktop.application.Action
	public void about() {
		Window parent = SwingUtilities.windowForComponent(this);
		if (aboutBox == null) {
			aboutBox = new AboutDialog(parent);
		}
		aboutBox.setLocationRelativeTo(parent);
		// FIXME should use the windowmanager to show the dialog box here!!
		getApplication().show(aboutBox);
	}

	@OnEdt
	public void addMenu(String title, ActionMap actionMap) {
		JMenu menu = new JMenu(title);
		if (actionMap != null && actionMap.allKeys() != null && actionMap.allKeys().length > 0) {
			Action toggleVisibilityAction = actionMap.get(TOGGLE_VISIBILITY_ACTION);
			char shortcut = Character.forDigit(windowMenu.getMenuComponentCount(), 9);
			KeyStroke keyStroke = KeyStroke.getKeyStroke(shortcut, ActionEvent.CTRL_MASK);
			toggleVisibilityAction.putValue(Action.ACCELERATOR_KEY, keyStroke);
			menu.add(createMenuItem(toggleVisibilityAction));
			Action toggleFullScreenAction = actionMap.get(TOGGLE_FULL_SCREEN_ACTION);
			if (toggleFullScreenAction != null) {
				menu.add(createMenuItem(toggleFullScreenAction));
			}
			// add all actions from the appcomponent's actionmap to the menu
			menu.add(new JSeparator());
			for (Object key : actionMap.allKeys()) {
				Action action = actionMap.get(key);
				// see whether the action has already been added to the menu
				JMenuItem menuItem = getMenuItem(menu, action);
				if (getContext().getActionMap().get(key) == null && menuItem == null) {
					menu.add(createMenuItem(action));
				}
			}
		}
		windowMenu.add(menu);
	}
	
	private JMenuItem createMenuItem(Action action) {
		JMenuItem result = null;
		Object selectedKey = action.getValue(Action.SELECTED_KEY);
		if (selectedKey != null) {
			result = new JCheckBoxMenuItem(action);
			if (action instanceof ApplicationAction) {
				ApplicationAction applicationAction = (ApplicationAction) action;
				if (applicationAction.getProxy() != null) {
					applicationAction.setProxySource(result);
				}
			}
		} else {
			result = new JMenuItem(action);
		}
		result.setText(getActionDescription(action));
		return result;
	}
	
	private String getActionDescription(Action action) {
		Object actionDescription = action.getValue(javax.swing.Action.NAME);
		if (actionDescription == null) {
			actionDescription = action.getValue(javax.swing.Action.SHORT_DESCRIPTION);
		}
		return actionDescription.toString();
	}
	
	private JMenuItem getMenuItem(JMenu menu, String actionDescription) {
		for (int i = 0; i < menu.getMenuComponentCount(); i++) {
			Component menuComponent = menu.getMenuComponent(i);
			if (menuComponent instanceof JMenuItem ) {
				JMenuItem menuItem = (JMenuItem) menuComponent;
				if (menuItem.getText().equals(actionDescription)) {
					return menuItem;
				}
			}
		}
		return null;
	}
	
	private JMenuItem getMenuItem(JMenu menu, Action action) {
		String actionDescription = getActionDescription(action);
		return getMenuItem(menu, actionDescription);
	}

	@OnEdt
	public void removeMenu(String title) {
		JMenuItem menuItem = getMenuItem(windowMenu, title);
		if (menuItem != null) {
			windowMenu.remove(menuItem);
		}
		windowMenu.updateUI();
		windowMenu.revalidate();
	}

	/*private boolean checkWindowPresence(AppWindowWrapper appWindowWrapper) {
		for (int i = 0; i < windowMenu.getMenuComponentCount(); i++) {
			Component menuComponent = windowMenu.getMenuComponent(i);
			if (menuComponent instanceof JMenuItem ) {
				JMenuItem menuItem = (JMenuItem) menuComponent;
				if (menuItem.getText().equals(appWindowWrapper.getTitle())) {
					return true;
				}
			}
		}
		return false;
	}*/

	public String getTitle() {
		return MENU_BAR_TITLE;
	}

}
