package net.bubbaland.gui;

import java.awt.Rectangle;
import java.awt.Window;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;
import net.bubbaland.megaciv.client.GameClient;

public abstract class BubbaGuiController {

	final private Properties			properties;

	// List of active windows
	final private ArrayList<BubbaFrame>	windowList;

	// Format for log timestamps
	private final SimpleDateFormat		timestampFormat;

	// File name to store window positions
	private final String				defaultsFilename;
	// File name to store window positions
	protected final String				settingsFilename;

	protected BubbaGuiController(String defaultsFilename, String settingsFilename, String settingsVersion) {
		// this.tabInformationHash = tabInformationHash;
		this.defaultsFilename = defaultsFilename;
		this.settingsFilename = settingsFilename;

		this.properties = new Properties();
		this.windowList = new ArrayList<BubbaFrame>();

		/**
		 * Default properties
		 */
		this.loadDefaults();

		/**
		 * Load saved properties from file
		 */
		final File file = new File(System.getProperty("user.home") + "/" + settingsFilename);
		try {
			final BufferedReader fileBuffer = new BufferedReader(new FileReader(file));
			properties.load(fileBuffer);
		} catch (final IOException e) {
			System.out.println("Couldn't load properties file, may not exist yet.");
		}

		/**
		 * If the version doesn't match, reload defaults
		 */
		final String version = properties.getProperty("SettingsVersion");
		if (version == null || !version.equals(settingsVersion)) {
			System.out.println("Using defaults");
			loadDefaults();
			properties.setProperty("SettingsVersion", settingsVersion);
		}

		// Set timestamp format
		timestampFormat = new SimpleDateFormat(properties.getProperty("TimestampFormat"));
	}

	/**
	 * Clear all saved data from file.
	 *
	 */
	public void loadDefaults() {
		properties.clear();
		final InputStream defaults = GameClient.class.getResourceAsStream(defaultsFilename);
		try {
			properties.load(defaults);
		} catch (final IOException e) {
			System.out.println("Couldn't load default properties file, aborting!");
			System.exit(-1);
		}
		BubbaDialogPanel.loadProperties(properties);
	}

	/**
	 * Save the current properties to the settings file.
	 */
	protected void savePropertyFile() {
		final File file = new File(System.getProperty("user.home") + "/" + settingsFilename);
		try {
			final BufferedWriter outfileBuffer = new BufferedWriter(new FileWriter(file));
			properties.store(outfileBuffer, "MegaCiv");
			outfileBuffer.close();
		} catch (final IOException e) {
			System.out.println("Error saving properties.");
		}
	}


	/**
	 * Save the position and size of the window to file.
	 *
	 * @param window
	 *            The window whose size and position is to be saved
	 *
	 */
	public void savePosition(Window window) {
		final Rectangle r = window.getBounds();
		final int x = (int) r.getX();
		final int y = (int) r.getY();
		final int width = (int) r.getWidth();
		final int height = (int) r.getHeight();

		final String frameID = window.getName();

		properties.setProperty(frameID + ".X", x + "");
		properties.setProperty(frameID + ".Y", y + "");
		properties.setProperty(frameID + ".Width", width + "");
		properties.setProperty(frameID + ".Height", height + "");
	}

	/**
	 * Get the name for the next top-level frame.
	 *
	 * @return The frame name
	 */
	public String nextWindowName() {
		final ArrayList<String> windowNames = new ArrayList<String>(0);
		for (final BubbaFrame frame : this.windowList) {
			windowNames.add(frame.getTitle());
		}
		String name = "MegaCiv";
		for (int i = 1; windowNames.contains(name); i++) {
			name = "MegaCiv (" + i + ")";
		}
		return name;
	}

	/**
	 * Register a window as a child of the client. New Trivia Frames do this so the client can track events from them.
	 *
	 * @param frame
	 *            The window to track
	 */
	public void registerWindow(BubbaFrame frame) {
		this.windowList.add(frame);
	}

	/**
	 * Unregister a window as a child of the client. This is done when a window closes.
	 *
	 * @param frame
	 *            The window to stop tracking
	 */
	public void unregisterWindow(BubbaFrame frame) {
		this.windowList.remove(frame);
	}

	/**
	 * Display message in the status bar and in console
	 *
	 * @param message
	 *            Message to log
	 */
	public void log(String message) {
		final String timestamp = timestampFormat.format(new Date());
		for (final BubbaFrame panel : this.windowList) {
			// Display message in status bar
			panel.log(timestamp + " " + message);
		}
		// Print message to console
		System.out.println(timestamp + " " + message);
	}

	public int getNWindows() {
		return this.windowList.size();
	}

	public Properties getProperties() {
		return this.properties;
	}

	/**
	 * Add the current window contents to properties, then save the properties to the settings file and exit.
	 */
	public void endProgram() {
		// Remove previously saved windows
		for (int f = 0; this.properties.getProperty("Window" + f) != null; f++) {
			properties.remove("Window" + f);
		}
		for (BubbaFrame window : this.windowList) {
			window.saveProperties();
			this.savePosition(window);
		}
		this.savePropertyFile();
		System.exit(0);
	}

	public void updateGui(boolean forceUpdate) {
		for (BubbaFrame frame : this.windowList) {
			frame.updateGui(forceUpdate);
		}
	}


}
