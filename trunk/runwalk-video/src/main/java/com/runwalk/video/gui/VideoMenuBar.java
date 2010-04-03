package com.runwalk.video.gui;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;

import javax.swing.ActionMap;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.text.DefaultEditorKit;

import org.apache.log4j.Logger;
import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.ResourceMap;

import com.runwalk.video.RunwalkVideoApp;
import com.runwalk.video.gui.AppWindowWrapper.AppWindowWrapperListener;
import com.runwalk.video.util.ResourceInjector;

@SuppressWarnings("serial")
public class VideoMenuBar extends JMenuBar implements AppComponent {

	private HashMap<JCheckBoxMenuItem, AppWindowWrapper> boxWindowMap = new HashMap<JCheckBoxMenuItem, AppWindowWrapper>();
	private HashMap<AppWindowWrapper, JCheckBoxMenuItem> windowBoxMap = new HashMap<AppWindowWrapper, JCheckBoxMenuItem>();
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
		JMenuItem uploadLogFiles = new JMenuItem( getApplication().getApplicationActionMap().get("uploadLogFiles"));
		helpMenu.add(uploadLogFiles);
		helpMenu.add(new JSeparator());
		helpMenu.add(aboutMenuItem);
		add(helpMenu);
	}

	@Action
	public void about() {
		if (aboutBox == null) {
			aboutBox = new RunwalkVideoAboutBox(getApplication().getMainFrame());
		}
		aboutBox.setLocationRelativeTo(getApplication().getMainFrame());
		getApplication().show(aboutBox);
	}

	//TODO need some reattach method here, too...
	public void addWindow(final AppWindowWrapper appComponent) {
		Component component = appComponent.getHolder();
		JCheckBoxMenuItem checkedItem = null;
		if (!boxWindowMap.containsValue(appComponent)) {
			appComponent.addAppWindowWrapperListener(new AppWindowWrapperListener() {

				@Override
				public void windowClosed(WindowEvent e) {
					setCheckboxSelection(appComponent);
				}

				@Override
				public void windowOpened(WindowEvent e) {
					setCheckboxSelection(appComponent);
				}

				@Override
				public void internalFrameDeactivated(InternalFrameEvent e) {
					setCheckboxSelection(appComponent);
				}

				@Override
				public void internalFrameActivated(InternalFrameEvent e) {
					setCheckboxSelection(appComponent);
				}

			});
			JMenu menu = new JMenu(appComponent.getTitle());

			checkedItem = new JCheckBoxMenuItem(getAction("showWindow"));
			checkedItem.setSelected(component.isVisible());
			boxWindowMap.put(checkedItem, appComponent);
			windowBoxMap.put(appComponent, checkedItem);
			KeyStroke keyStroke = KeyStroke.getKeyStroke(Character.forDigit(windowBoxMap.size(), 9), ActionEvent.CTRL_MASK);
			checkedItem.setAccelerator(keyStroke);
			menu.add(checkedItem);

			ActionMap actionMap = appComponent.getApplicationActionMap();
			if (actionMap != null && actionMap.allKeys() != null && actionMap.allKeys().length > 0) {
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
			//TODO add internal frame instance at the end of the menu and after a separator..
			windowMenu.add(menu);
		} 
	}

	@Action
	public void showWindow(ActionEvent e) {
		JCheckBoxMenuItem selectedItem = (JCheckBoxMenuItem) e.getSource();
		AppWindowWrapper component = boxWindowMap.get(selectedItem);
		//FIXME this should not be null!!
		if (component != null) {
			component.getHolder().setVisible(selectedItem.isSelected());
		}
	}

	public void removeWindow(AppComponent appComponent) {
		JCheckBoxMenuItem boxItem = windowBoxMap.get(appComponent);
		if (boxItem != null) {
			windowMenu.remove(boxItem);
			boxWindowMap.remove(boxItem);
			windowBoxMap.remove(appComponent);
		}
	}

	public void setCheckboxSelection(AppWindowWrapper appComponent) {
		JCheckBoxMenuItem chckBox = windowBoxMap.get(appComponent);
		if (chckBox != null) {
			chckBox.setSelected(appComponent.getHolder().isVisible());
		}
	}

	public javax.swing.Action getAction(String name) {
		return getApplicationActionMap().get(name);
	}

	public RunwalkVideoApp getApplication() {
		return RunwalkVideoApp.getApplication();
	}

	public JMenuBar getComponent() {
		return this;
	}

	public ApplicationContext getContext() {
		return getApplication().getContext();
	}

	public Logger getLogger() {
		return Logger.getLogger(getClass());
	}

	public ResourceMap getResourceMap() {
		return getContext().getResourceMap(getClass(), VideoMenuBar.class);
	}

	public ActionMap getApplicationActionMap() {
		return getContext().getActionMap(VideoMenuBar.class, this);
	}

	public String getTitle() {
		return "Menu Bar";
	}

}
