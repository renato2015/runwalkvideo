package com.runwalk.video.gui.mediaplayer;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JToggleButton;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;

import org.apache.log4j.Logger;
import org.jdesktop.application.Action;

import com.runwalk.video.RunwalkVideoApp;
import com.runwalk.video.entities.Analysis;
import com.runwalk.video.entities.Keyframe;
import com.runwalk.video.entities.Recording;
import com.runwalk.video.gui.AbstractTableModel;
import com.runwalk.video.gui.actions.RecordingStatus;
import com.runwalk.video.util.ApplicationUtil;

import de.humatic.dsj.DSCapture;
import de.humatic.dsj.DSFilterInfo;

public class PlayerFrame extends PlayerGUI {

	private JPanel southPanel;
	private JLabel time;

	private long timeRecording;

	private Timer recordTimer;
	public Timer playTimer;
	private int currentTime;
	private Frame captureFrame;
	private Frame movieFrame;

	private final static Logger LOGGER = Logger.getLogger(PlayerFrame.class);

	public PlayerFrame(PlayerEngine engine) {
		super("Camera", engine);
		//		setMinimumSize(new Dimension(800, 615));
		//		setResizable(true);
		//		activeDragAndDrop();
	}

	private final void browseFiles() {
		JFileChooser chooser = new JFileChooser();
		int returnVal = chooser.showOpenDialog(RunwalkVideoApp.getApplication().getMainFrame());
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			try {
				//				engine.addItem(chooser.getSelectedFile().getAbsolutePath());
			} catch (Exception ex) {
				ex.printStackTrace();
				JOptionPane.showMessageDialog(null, ex.getMessage(), "Error",
						JOptionPane.OK_OPTION);
			}
		}
	}

	protected void initInterface() {

		menu = new JPanel();
		ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
		open = new JButton(new ImageIcon(contextClassLoader.getResource("com/runwalk/video/gui/mediaplayer/resources/icons/player_eject.png")));
		open.setMargin(new Insets(0, 0, 0, 0));
		record = new JButton(new ImageIcon(contextClassLoader.getResource("com/runwalk/video/gui/mediaplayer/resources/icons/record.png")));
		record.setMargin(new Insets(0, 0, 0, 0));
		play_pause = new JToggleButton(new ImageIcon(contextClassLoader.getResource("com/runwalk/video/gui/mediaplayer/resources/icons/player_play.png")));
		play_pause.setMargin(new Insets(0, 0, 0, 0));
		stop = new JButton(new ImageIcon(contextClassLoader.getResource("com/runwalk/video/gui/mediaplayer/resources/icons/player_stop.png")));
		stop.setMargin(new Insets(0, 0, 0, 0));
		fwd = new JButton(new ImageIcon(contextClassLoader.getResource("com/runwalk/video/gui/mediaplayer/resources/icons/player_fwd.png")));
		fwd.setMargin(new Insets(0, 0, 0, 0));
		bwd = new JButton(new ImageIcon(contextClassLoader.getResource("com/runwalk/video/gui/mediaplayer/resources/icons/player_bwd.png")));
		bwd.setMargin(new Insets(0, 0, 0, 0));
		next = new JButton(new ImageIcon(contextClassLoader.getResource("com/runwalk/video/gui/mediaplayer/resources/icons/player_next.png")));
		next.setMargin(new Insets(0, 0, 0, 0));
		snapshot = new JButton(new ImageIcon(contextClassLoader.getResource("com/runwalk/video/gui/mediaplayer/resources/icons/snapshot.png")));
		snapshot.setMargin(new Insets(0, 0, 0, 0));
		prev = new JButton(new ImageIcon(contextClassLoader.getResource("com/runwalk/video/gui/mediaplayer/resources/icons/player_previous.png")));
		prev.setMargin(new Insets(0, 0, 0, 0));
		captureSettingsBtn = new JButton(new ImageIcon(contextClassLoader.getResource("com/runwalk/video/gui/mediaplayer/resources/icons/capture_settings.png")));
		captureSettingsBtn.setMargin(new Insets(0, 0, 0, 0));
		cameraSettingsBtn = new JButton(new ImageIcon(contextClassLoader.getResource("com/runwalk/video/gui/mediaplayer/resources/icons/camera.png")));
		cameraSettingsBtn.setMargin(new Insets(0, 0, 0, 0));
		vol_down = new JButton(new ImageIcon(contextClassLoader.getResource("com/runwalk/video/gui/mediaplayer/resources/icons/volume_down.png")));
		vol_down.setMargin(new Insets(0, 0, 0, 0));
		vol_up = new JButton(new ImageIcon(contextClassLoader.getResource("com/runwalk/video/gui/mediaplayer/resources/icons/volume_up.png")));
		vol_up.setMargin(new Insets(0, 0, 0, 0));
		mute = new JButton(new ImageIcon(contextClassLoader.getResource("com/runwalk/video/gui/mediaplayer/resources/icons/volume_on.png")));
		mute.setMargin(new Insets(0, 0, 0, 0));
		fullscreen = new JButton(new ImageIcon(contextClassLoader.getResource("com/runwalk/video/gui/mediaplayer/resources/icons/window_fullscreen.png")));
		fullscreen.setMargin(new Insets(0, 0, 0, 0));

		setSlider(new JSlider(JSlider.HORIZONTAL, 0, 1000, 0));
		//		getSlider().setMajorTickSpacing(1000);
		//		getSlider().setMinorTickSpacing(100);
		getSlider().setPaintTicks(false);
		getSlider().setPaintLabels(true);
		getSlider().setSnapToTicks(false);
		getSlider().setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
		clearLabels();

		// Listener for the scroll
		getSlider().addMouseListener(new MouseAdapter() {
			public void mouseReleased(MouseEvent e) {
				if (engine.getDSMovieGraph() != null && (engine.getDSMovieGraph().getActive())) {
					double pos = getSlider().getValue() * engine.getDuration() / 1000;
					System.out.println("Slide stateChanged : " + (int) pos);
					engine.setPosition((int) pos);
				}
			}
		});

		time = new JLabel("00:00.000 / 00:00.000");

		playTimer = new Timer(50, null);
		recordTimer = new Timer(1000, null);

		recordTimer.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Analysis analysis = RunwalkVideoApp.getApplication().getSelectedAnalysis();
				//TODO fire updates naar de modellen om de timing te veranderen, haal de waarde van deze timer daar dan op..
//				analysis.getRecording().setDuration((int) (System.currentTimeMillis() - timeRecording));
				String elapsedTime = analysis.getRecording().formatDuration(ApplicationUtil.EXTENDED_DURATION_FORMATTER);

				int index = RunwalkVideoApp.getApplication().getAnalysisOverviewTableModel().getItemIndex(analysis);
				RunwalkVideoApp.getApplication().getAnalysisOverviewTableModel().fireTableRowsUpdated(index, index);
				index = RunwalkVideoApp.getApplication().getAnalysisTableModel().getItemIndex(analysis);
				RunwalkVideoApp.getApplication().getAnalysisTableModel().fireTableRowsUpdated(index, index);

				time.setText("00:00.000 / " + elapsedTime);
			}
		});

		playTimer.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				if (engine.isPlaying()) {
					if (engine.getPosition() == 0) {
						engine.stop();
					}
					//					updateTimeStamps(engine.getPosition());
					updateTimeStamps();
				}
			}
		});

		menu.setPreferredSize(new Dimension(600,40));
		menu.setLayout(new java.awt.FlowLayout());

		//		menu.add(open);
		menu.add(prev);
		menu.add(bwd);
		menu.add(record);
		menu.add(stop);
		menu.add(play_pause);
		menu.add(fwd);
		menu.add(next);
		menu.add(snapshot);
		menu.add(vol_down);
		menu.add(vol_up);
		menu.add(mute);
		menu.add(captureSettingsBtn);
		menu.add(cameraSettingsBtn);
		//		menu.add(fullscreen);

		southPanel = new JPanel();
		southPanel.setBorder(new EmptyBorder(new Insets(10,10,0,10)));
		southPanel.setLayout(new BorderLayout());
		southPanel.add(getSlider(), BorderLayout.NORTH);
		southPanel.add(time, BorderLayout.EAST);
		southPanel.add(menu, BorderLayout.CENTER);
		//		getContentPane().add(southPanel, BorderLayout.SOUTH);

		// resize the this
		//		this.pack();

		play_pause.setEnabled(false);

		// Add actionListener on each button
		//		open.addActionListener(new ActionListener() {
		//			public void actionPerformed(ActionEvent e) {
		//				playFile();
		//			}
		//		});

		// Play Pause listener
		play_pause.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				engine.switchPlay();
			}
		});
		play_pause.setToolTipText("Speel/Pauzeer");
		play_pause.setMnemonic(KeyEvent.VK_SPACE);

		// Stop Listener
		stop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (engine.getCaptureGraph().getState() == DSCapture.RECORDING) {
					stopRecording();
				}
				else if (engine.getDSMovieGraph().getActive()) {
					engine.stop();
					getSlider().setValue(0);
				}
			}
		});
		stop.setToolTipText("Stop");

		record.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				startRecording();
			}
		});


		snapshot.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				makeSnapshot();
			}
		});

		// Forward listener : modify the speed through the setRate function
		fwd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				engine.forward();
			}
		});
		fwd.setToolTipText("Versnel");
		fwd.setMnemonic(KeyEvent.VK_RIGHT);

		// //Backward listener : Modify the speed through the setRate function
		bwd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {                            
				engine.backward();
			}
		});
		bwd.setToolTipText("Vertraag");
		bwd.setMnemonic(KeyEvent.VK_LEFT);

		// Next listener : to go to the next song in the playlist
		next.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				engine.nextItem();
			}
		});
		next.setToolTipText("Volgend beeld");

		// Previous listener : to go to the previous song in the playlist
		prev.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				engine.previousItem();
			}
		});
		prev.setToolTipText("Vorig beeld");


		// Loop listener : mode loop for the current song.
		captureSettingsBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				engine.showCaptureSettings();
			}
		});
		captureSettingsBtn.setToolTipText("Opname instellingen");

		// Random listener : mode random or not
		cameraSettingsBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				engine.showCameraSettings();
			}
		});
		cameraSettingsBtn.setToolTipText("Camera instellingen");

		// Volume up
		vol_up.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				engine.increaseVolume();
			}
		});
		vol_up.setToolTipText("Volume omhoog");

		// Volume down
		vol_down.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				engine.decreaseVolume();
			}
		});
		vol_down.setToolTipText("Volume omlaag");

		// Mute listener : mute or not
		mute.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
				if (engine.switchVolume()) {
					mute.setIcon(new ImageIcon(contextClassLoader.getResource("com/runwalk/video/gui/mediaplayer/resources/icons/volume_mute.png")));
				}
				else {
					mute.setIcon(new ImageIcon(contextClassLoader.getResource("com/runwalk/video/gui/mediaplayer/resources/icons/volume_on.png")));
				}
			}
		});
		mute.setToolTipText("Geluid uit");

		// Full screen listener : mode full screen
		fullscreen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				engine.toggleFullScreen();
			}
		});

		fullscreen.setToolTipText("Fullscreen");
		//		fullscreen.setEnabled(false);
	}

	public JPanel getSouthPanel() {
		return southPanel;
	}

	public void enableRecording(boolean recordSelected) {
		record.setEnabled(recordSelected);
		//TODO record button enabled + 
//		if (recordSelected) {
//			setCaptureControls();
//		}
	}

	public boolean isRecordingEnabled() {
		return record.isEnabled();
	}

	private void setCaptureControls() {
		getSlider().setValue(0);
		time.setText("00:00.000 / 00:00.000");
		getSlider().setEnabled(false);
		play_pause.setEnabled(false);
		next.setEnabled(false);
		prev.setEnabled(false);
		fwd.setEnabled(false);
		bwd.setEnabled(false);
		mute.setEnabled(false);
		vol_up.setEnabled(false);
		vol_down.setEnabled(false);
		stop.setEnabled(false);
		captureSettingsBtn.setEnabled(true);
		cameraSettingsBtn.setEnabled(true);
		open.setEnabled(true);
		snapshot.setEnabled(false);
		enableRecording(RunwalkVideoApp.getApplication().getAnalysisTablePanel().isSelectedItemRecorded());
		clearLabels();
	}

	private void setPlayerControls() {
		if (engine.getDSMovieGraph() != null) {
			//		getSlider().setValue(0);
			addLabels(engine.getMovie());
			snapshot.setEnabled(true);
			//		time.setText("00:00.000 /  " + RunwalkVideoApp.formatDate(new Date(engine.getDuration()), new SimpleDateFormat("mm:ss.SSS")));
			updateTimeStamps();
			//		setRecordingEnabled(false);
			getSlider().setEnabled(true);
			play_pause.setEnabled(true);
			next.setEnabled(true);
			prev.setEnabled(true);
			fwd.setEnabled(true);
			bwd.setEnabled(true);
			mute.setEnabled(true);
			vol_up.setEnabled(true);
			vol_down.setEnabled(true);
			stop.setEnabled(true);
			cameraSettingsBtn.setEnabled(false);
			captureSettingsBtn.setEnabled(false);
			open.setEnabled(false);
			snapshot.setEnabled(true);
			enableRecording(false);
		} else {
			setCaptureControls();
		}
	}

	@Action
	public void startCapturer() {
		//		if (engine.isPlaying()) {
		//			engine.stop();
		//		}
		RunwalkVideoApp.getApplication().getPlayer().initCaptureGraph();
		captureFrame = engine.getCaptureGraph().getFullScreenWindow() ;
		if (captureFrame == null) {
			GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			GraphicsDevice[] gs = ge.getScreenDevices();
			engine.getCaptureGraph().goFullScreen(gs[1], 1);
			captureFrame = engine.getCaptureGraph().getFullScreenWindow();
			captureFrame.setTitle(engine.getSelectedDevice().getName());
			captureFrame.setName("Camera: " + engine.getSelectedDevice().getName());
			RunwalkVideoApp.getApplication().getMenuBar().addWindow(captureFrame);
			captureFrame.addWindowListener(new WindowAdapter() {

				@Override
				public void windowGainedFocus(WindowEvent e) {
					if (engine.getDSMovieGraph() != null) {
						engine.stop();
					}
					if (engine.getCaptureGraph().getState() != DSCapture.RECORDING) {
						setCaptureControls();
					}
				}

				@Override
				public void windowActivated(WindowEvent e) {
					if (engine.getDSMovieGraph() != null) {
						engine.stop();
					}
					if (engine.getCaptureGraph().getState() != DSCapture.RECORDING) {
						setCaptureControls();
					}
				}

				/*				@Override
				public void windowClosed(WindowEvent e) {
					if (engine.getDSCapture().getState() == DSCapture.RECORDING) {
						Toolkit.getDefaultToolkit().beep();
						stopRecording();
						JOptionPane.showConfirmDialog(RunwalkVideoApp.getApplication().getMainFrame(),
								"Opnemen kan enkel met videoscherm open!\nDe opname werd stopgezet.", 
								"Opname beëindigd", JOptionPane.OK_OPTION, JOptionPane.WARNING_MESSAGE);
					}

				}*/
			});
		}
		captureFrameToFront();
	}

	/**
	 * Bring the capture frame to front
	 */
	public void captureFrameToFront() {
		if (!captureFrame.isActive()) {
			captureFrame.toFront();
		}	
	}
	
	/**
	 * Bring the movie frame to front
	 */
	public void movieFrameToFront() {
		if (!movieFrame.isActive()) {
			movieFrame.toFront();
		}	
	}

	public void startRecording() {
		captureFrameToFront();
		//setCaptureControls();
		//TODO check if controls are in the right enabled state
		
		Analysis analysis = RunwalkVideoApp.getApplication().getSelectedAnalysis();
		if (analysis != null) {
			Recording recording = analysis.getRecording();
			if (recording == null) {
				recording = analysis.createRecording();
			}
			recording.setRecordingStatus(RecordingStatus.RECORDING);
			
			//TODO Block GUI???

			int itemIndex = RunwalkVideoApp.getApplication().getAnalysisTableModel().getItemIndex(analysis);
			RunwalkVideoApp.getApplication().getAnalysisTableModel().fireTableRowsUpdated(itemIndex, itemIndex);

			itemIndex = RunwalkVideoApp.getApplication().getAnalysisOverviewTableModel().getItemIndex(analysis);
			RunwalkVideoApp.getApplication().getAnalysisOverviewTableModel().fireTableRowsUpdated(itemIndex, itemIndex);

			RunwalkVideoApp.getApplication().getStatusPanel().setIndeterminate(true);
			timeRecording = System.currentTimeMillis();

			File destFile = recording.getUncompressedVideoFile();

			engine.getCaptureGraph().setAviExportOptions(-1, -1, -1, engine.rejectPauseFilter(), -1);
			engine.getCaptureGraph().setCaptureFile(destFile.getAbsolutePath(), 
					engine.getCaptureEncoder(),
					DSFilterInfo.doNotRender(),
					true);
			LOGGER.debug("Movie recording to file " + destFile.getAbsolutePath() + "");
			LOGGER.debug("Video encoder = " + engine.getCaptureEncoder().getName() + ".");
			LOGGER.debug("Pause filter rejection set to " + engine.rejectPauseFilter() + ".");

			engine.record();
			recordTimer.restart();
			Thread thread = new Thread(new Runnable() {
				public void run() {
					while(engine.getCaptureGraph().getState() == DSCapture.RECORDING) {
						LOGGER.debug("captured: " + engine.getCaptureGraph().getFrameDropInfo()[0] + " dropped: "+ engine.getCaptureGraph().getFrameDropInfo()[1]);
						try {
							Thread.sleep(400);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			});
			thread.start();

			enableRecording(false);
			stop.setEnabled(true);
			RunwalkVideoApp.getApplication().showMessage("Opname voor " + 
					analysis.getClient().getName() + " " + analysis.getClient().getFirstname() + " gestart..");
			RunwalkVideoApp.getApplication().setSaveNeeded(true);
		}

	}

	public void stopRecording() {
		stop.setEnabled(false);
		enableRecording(false);
		recordTimer.stop();
		engine.record();
		engine.getCaptureGraph().setPreview();

		Analysis analysis = RunwalkVideoApp.getApplication().getSelectedAnalysis();
		analysis.getRecording().setRecordingStatus(RecordingStatus.UNCOMPRESSED);

//		RunwalkVideoApp.getApplication().getAnalysisTablePanel().getTable().setEnabled(true);
		//Don't update... yet
		RunwalkVideoApp.getApplication().getStatusPanel().setIndeterminate(false);

		int itemIndex = RunwalkVideoApp.getApplication().getAnalysisTableModel().getItemIndex(analysis);
		RunwalkVideoApp.getApplication().getAnalysisTableModel().fireTableRowsUpdated(itemIndex, itemIndex);

		itemIndex = RunwalkVideoApp.getApplication().getAnalysisOverviewTableModel().getItemIndex(analysis);
		RunwalkVideoApp.getApplication().getAnalysisOverviewTableModel().fireTableRowsUpdated(itemIndex, itemIndex);

		RunwalkVideoApp.getApplication().showMessage("Opname voor " + analysis.getClient().getName() + 
				" " + analysis.getClient().getFirstname() + " voltooid.");
		RunwalkVideoApp.getApplication().setSaveNeeded(true);
		RunwalkVideoApp.getApplication().getTableActions().setCompressionEnabled(true);
	}

	@Action
	private void makeSnapshot() {
		if (engine.isPlaying()) {
			engine.switchPlay();
		}
		//				updateTimeStamps(currentTime);
		int stamp = engine.setPosition(currentTime);
		Keyframe snapshot = new Keyframe(engine.getMovie(), stamp);
		AbstractTableModel.persistEntity(snapshot);
		engine.getMovie().addKeyframe(snapshot);
		RunwalkVideoApp.getApplication().getAnalysisTableModel().fireTableCellUpdated(RunwalkVideoApp.getApplication().getAnalysisTableModel().getSelectedIndex(), 2);
		getSlider().getLabelTable().put(getSliderPosition(currentTime), new JLabel("*"));
		getSlider().updateUI();
		getSlider().revalidate();
		RunwalkVideoApp.getApplication().showMessage("Snapshot genomen op " + 
				ApplicationUtil.formatDate(new Date(currentTime), new SimpleDateFormat("mm:ss.SSS"))); 
		RunwalkVideoApp.getApplication().setSaveNeeded(true);
	}

	public void playFile(final Recording recording) {
		engine.initMovieComponent(recording);
		movieFrame = engine.getDSMovieGraph().getFullScreenWindow();
		if (movieFrame == null) {
			GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			GraphicsDevice[] gs = ge.getScreenDevices();
			engine.getDSMovieGraph().goFullScreen(gs[1], 1);
			movieFrame = engine.getDSMovieGraph().getFullScreenWindow();
			RunwalkVideoApp.getApplication().getMenuBar().addWindow(movieFrame);
			movieFrame.addWindowListener(new WindowAdapter() {

				@Override
				public void windowGainedFocus(WindowEvent e) {
					if (engine.getCaptureGraph().getState() == DSCapture.RECORDING) {
						Toolkit.getDefaultToolkit().beep();
						stopRecording();
						JOptionPane.showMessageDialog(RunwalkVideoApp.getApplication().getMainFrame(),
								"Opnemen kan enkel met videoscherm open!", "Opname beëindigd", JOptionPane.WARNING_MESSAGE);
					}
					if (!engine.isPlaying()) {
						setPlayerControls();
					}
				}

				@Override
				public void windowActivated(WindowEvent e) {
					/*if (engine.getDSCapture().getState() == DSCapture.RECORDING) {
						Toolkit.getDefaultToolkit().beep();
						stopRecording();
						JOptionPane.showMessageDialog(RunwalkVideoApp.getApplication().getMainFrame(),
								"Opnemen kan enkel met videoscherm open!", "Opname beëindigd", JOptionPane.WARNING_MESSAGE);
					}*/
					if (!engine.isPlaying()) {
						setPlayerControls();
					}
				}

				@Override
				public void windowClosed(WindowEvent e) {
					//					engine.stop();
					engine.disposeDSMovieGraph();
					setPlayerControls();
				}

			});
		}

		movieFrame.setTitle(recording.getVideoFileName() + " afspelen.. ");
		movieFrame.setName(recording.getVideoFileName() + " afspelen.. ");
		movieFrameToFront();

		getSlider().setValue(0);
		addLabels(recording);
	}


	private Hashtable<Integer, JLabel> createLabelTable() {
		Hashtable<Integer, JLabel> table = new Hashtable<Integer, JLabel>();
		table.put(getSlider().getMinimum(), new JLabel("|"));
		table.put(getSlider().getMaximum(), new JLabel("|"));
		return table;
	}
	
	public void clearLabels() {
		getSlider().setLabelTable(createLabelTable());
	}

	public void addLabels(Recording movie) {
		Hashtable<Integer, JLabel> table = createLabelTable();
		JLabel asterisk = new JLabel("*");
		for (Keyframe e:movie.getKeyframes()) {
			table.put(getSliderPosition(e.getPosition()), asterisk);
		}
		getSlider().setLabelTable(table);
		//		getSlider().setPaintLabels(true);
		getSlider().updateUI();
		getSlider().revalidate();
	}

	private void updateTimeStamps() {
		currentTime = engine.getPosition();
		getSlider().setValue(getSliderPosition(currentTime));
		String elapsedTime = ApplicationUtil.formatDate(new Date(currentTime), new SimpleDateFormat("mm:ss.SSS"));
		String totalTime = ApplicationUtil.formatDate(new Date(engine.getDuration()), new SimpleDateFormat("mm:ss.SSS"));
		time.setText(elapsedTime + " / " + totalTime);
	}

	private int getSliderPosition(int position) {
		return (int) ((double) 1000 *  position / engine.getDuration());
	}


	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals("position")) {
			//			updateTimeStamps((Integer) evt.getNewValue());
			updateTimeStamps();
			//			getSlider().setValue(getSliderPosition((Integer) evt.getNewValue()));
			LOGGER.debug("Position changed :" + evt.getNewValue());
		}
		//		if (engine.isPlaying()) {
		//		 if (DSJUtils.getEventType(evt) == DSMovie.FRAME_NOTIFY) {
		//				if (engine.getPosition() == 0) {
		//					engine.stop();
		//				}
		//				updateTimeStamps(DSJUtils.getEventValue_int(evt));
		////				updateTimeStamps((Integer) evt.getOldValue());
		//			}
		//		}

		//		System.out.println("bron: " + evt.getSource() + " eigenschap: " + evt.getPropertyName() + " oud: " + evt.getOldValue() + " new:  " + evt.getNewValue() + " frame calback: " + DSFiltergraph.FRAME_CALLBACK);
		//		System.out.println("bron: "  );
	}
	
}

