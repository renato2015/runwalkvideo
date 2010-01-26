package com.runwalk.video.gui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import javax.swing.JSeparator;
import javax.swing.KeyStroke;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.text.DefaultEditorKit;
import javax.xml.ws.Action;

import com.runwalk.video.util.ResourceInjector;

public class VideoMenuBar extends ComponentDecorator<JMenuBar> implements PropertyChangeListener {

	private HashMap<JCheckBoxMenuItem, ComponentDecorator<? extends Container>> boxWindowMap = new HashMap<JCheckBoxMenuItem, ComponentDecorator<? extends Container>>();
	private HashMap<ComponentDecorator<? extends Container>, JCheckBoxMenuItem> windowBoxMap = new HashMap<ComponentDecorator<? extends Container>, JCheckBoxMenuItem>();
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

		//TODO refactor this into an Action object..
		JMenu videoMenu = new JMenu(getResourceMap().getString("videoMenu.text"));
		JMenuItem cameraItem = new JMenuItem("startCapturer");
		JMenuItem initCameraItem = new JMenuItem("initCaptureGraph");

/*		JCheckBoxMenuItem rejectFilterItem = new JCheckBoxMenuItem("Verwijder Pause Filter");
		rejectFilterItem.setSelected( getApplication().getPlayerEngine().rejectPauseFilter());
		rejectFilterItem.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				boolean rejectPauseFilter = (e.getStateChange() == ItemEvent.SELECTED) ? true : false;
				getApplication().getPlayerEngine().setRejectPauseFilter(rejectPauseFilter);
			}
		});
		videoMenu.add(rejectFilterItem);
		videoMenu.add(new JSeparator());
		videoMenu.add(new JMenuItem( getApplication().getPlayerActionMap().get("setCaptureEncoder")));
		videoMenu.add(new JMenuItem( getApplication().getPlayerActionMap().get("setFrameRate")));
		videoMenu.add(new JMenuItem( getApplication().getPlayerActionMap().get("viewFilterProperties")));
		videoMenu.add(new JSeparator());
		videoMenu.add(cameraItem);
		videoMenu.add(initCameraItem);
		videoMenu.add( getApplication().getPlayerActionMap().get("toggleCamera"));*/
		videoMenu.add(new JSeparator());
		videoMenu.add( getApplication().getApplicationActionMap().get("selectVideoDir"));

		getComponent().add(videoMenu);

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

	public void addWindow(ComponentDecorator<? extends Container> decorator) {
		Component component = decorator.getComponent();
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
			window.addPropertyChangeListener(this);	
		}
		
		JMenu menu = new JMenu(decorator.getName());
		
		JCheckBoxMenuItem checkedItem = new JCheckBoxMenuItem(component.getName());
		checkedItem.setSelected(component.isVisible());
		boxWindowMap.put(checkedItem, decorator);
		windowBoxMap.put(decorator, checkedItem);
		checkedItem.setAccelerator(KeyStroke.getKeyStroke(Character.forDigit(windowBoxMap.size(), 9), ActionEvent.CTRL_MASK));
		checkedItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JCheckBoxMenuItem selectedItem = (JCheckBoxMenuItem) e.getSource();
				boxWindowMap.get(selectedItem).setVisible(selectedItem.isSelected());
			}
		});
		menu.add(checkedItem);
		
		ActionMap actionMap = decorator.getActionMap();
		if (actionMap != null && actionMap.keys() != null && actionMap.keys().length > 0) {
			menu.add(new JSeparator());
			for (Object key : actionMap.keys()) {
				javax.swing.Action action = actionMap.get(key);
				JMenuItem item = new JMenuItem(action);
				if (action.getValue(javax.swing.Action.NAME) == null) {
					item.setText(action.getValue(javax.swing.Action.SHORT_DESCRIPTION).toString());
				}
//				item.setIcon(null);
				menu.add(item);
			}
		}
		boolean isInternalFrame = component instanceof JInternalFrame;
		//TODO add internal frame instance at the end of the menu and after a separator..
		windowMenu.add(menu);
	}

	public void removeWindow(Component component) {
		JCheckBoxMenuItem boxMenuItem = windowBoxMap.get(component);
		if (boxMenuItem != null) {
			windowMenu.remove(boxMenuItem);
			boxWindowMap.remove(boxMenuItem);
			windowBoxMap.remove(component);
		}
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
