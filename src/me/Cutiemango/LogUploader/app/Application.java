package me.Cutiemango.LogUploader.app;

import me.Cutiemango.LogUploader.AbstractLogEntry;
import me.Cutiemango.LogUploader.Encounter;
import me.Cutiemango.LogUploader.PreferenceManager;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Application
{
	private JFrame frame;
	// 0, 1, 2, 3, 4, 5(Button)
	private JPanel mainPanel, directoryPanel, typePanel, wingPanel, encounterPanel, filePanel;
	// The button at the lowest right
	private JButton globalButton;

	private static final Font GLOBAL_FONT = new Font("微軟正黑體", Font.PLAIN, 18);

	// Selected Items
	private int SELECTED_ENCOUNTER_TYPE = -1;
	private int SELECTED_RAID_WING = 0;
	private List<Encounter> SELECTED_ENCOUNTERS = new ArrayList<>();

	public Application(String title, int width, int height)
	{
		frame = new JFrame(title);
		mainPanel = new JPanel();

		frame.setSize(width, height);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(false);
		frame.add(mainPanel);

		mainPanel.setLayout(null);

		directoryPanel = new JPanel();
		directoryPanel.setBounds(0, 0, 960, 60);

		typePanel = new JPanel();
		typePanel.setBounds(0, 60, 960, 60);

		wingPanel = new JPanel();
		wingPanel.setBounds(0, 120, 960, 60);

		encounterPanel = new JPanel();
		encounterPanel.setBounds(0, 180, 960, 60);

		filePanel = new JPanel();
		filePanel.setBounds(0, 240, 960, 240);

		globalButton = createButton("Next");
		globalButton.setBounds(10, 500, 930, 60);
		globalButton.setVisible(false);

		mainPanel.add(directoryPanel);
		mainPanel.add(typePanel);
		mainPanel.add(wingPanel);
		mainPanel.add(encounterPanel);
		mainPanel.add(filePanel);
		mainPanel.add(globalButton);

		setUpDirectory();

		frame.setVisible(true);
	}

	public void setUpDirectory()
	{
		PreferenceManager.loadPreference();
		// Set up directory panel
		directoryPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 0));

		JLabel directory = createLabel("ArcDPS Log Directory: ");
		directoryPanel.add(directory);

		JLabel msg = createLabel(PreferenceManager.hasDirectorySet() ? PreferenceManager.getLogDirectory() : "<Please select the directory>");
		directoryPanel.add(msg);

		JButton browse = createButton("Browse");
		browse.addActionListener(e ->
		{
			JFileChooser fc = new JFileChooser();
			fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			fc.setDialogTitle("Select ArcDPS Directory");
			int returnVal = fc.showOpenDialog(null);
			if (returnVal == JFileChooser.APPROVE_OPTION)
			{
				PreferenceManager.setLogDirectory(fc.getSelectedFile().getAbsolutePath());
				selectType();
			}
			msg.setText(PreferenceManager.hasDirectorySet() ? PreferenceManager.getLogDirectory() : "<Not a valid directory>");
		});

		directoryPanel.add(browse);


		if (PreferenceManager.hasDirectorySet())
			selectType();
	}

	// Step: 2
	public void selectType()
	{
		typePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 0));

		JLabel label = createLabel("Select Type: ");
		typePanel.add(label);

		JToggleButton fotm = createToggleButton("Fractals");
		JToggleButton raid = createToggleButton("Raids");

		fotm.addActionListener(e ->
		{
			// Chooses fotm
			if (SELECTED_ENCOUNTER_TYPE == 1)
				return;
			SELECTED_ENCOUNTER_TYPE = 1;
			resetType();
			selectEncounters();
		});

		raid.addActionListener(e ->
		{
			// Chooses raid
			if (SELECTED_ENCOUNTER_TYPE == 2)
				return;
			SELECTED_ENCOUNTER_TYPE = 2;
			resetType();
			selectWing();
		});

		ButtonGroup group = new ButtonGroup();
		group.add(fotm);
		group.add(raid);

		typePanel.add(fotm);
		typePanel.add(raid);
	}

	private void checkRemovePanels(int step)
	{
		// Remove all previous panels
		for (int i = step; i < mainPanel.getComponentCount()-1; i++)
		{
			JPanel panel = ((JPanel) mainPanel.getComponent(i));
			panel.removeAll();
			panel.revalidate();
			panel.repaint();
		}

		// Reset button as well
		globalButton.setText("Next");
	}

	private void resetType()
	{
		SELECTED_RAID_WING = 0;
		SELECTED_ENCOUNTERS.clear();

		checkRemovePanels(2);

		globalButton.setVisible(false);
	}

	// Step: 3
	public void selectWing()
	{
		wingPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 0));

		JLabel label = createLabel("Select Wing: ");
		wingPanel.add(label);

		ButtonGroup buttonGroup = new ButtonGroup();
		for (int i = 1; i <= 7; i++)
		{
			JToggleButton wingButton = createToggleButton(Integer.toString(i));
			int wing = i;
			wingButton.addActionListener(e ->
			{
				checkRemovePanels(3);
				SELECTED_RAID_WING = wing;
				selectEncounters();
			});
			wingPanel.add(wingButton);
			buttonGroup.add(wingButton);
		}
	}

	// Step: 4
	public void selectEncounters()
	{
		encounterPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 0));

		JLabel label = createLabel("Select Encounter(s): ");
		encounterPanel.add(label);

		Arrays.stream(Encounter.values())
				.filter(encounter -> isRaidWingSet() ? encounter.getWing() == SELECTED_RAID_WING : encounter.isFractal())
				.forEach(encounter ->
				{
					JToggleButton button = createToggleButton(encounter.toString());

					if (SELECTED_ENCOUNTERS.contains(encounter))
						button.setSelected(true);

					button.addActionListener(e ->
					{
						checkRemovePanels(4);

						if (button.isSelected())
							SELECTED_ENCOUNTERS.add(encounter);
						else
							SELECTED_ENCOUNTERS.remove(encounter);

						globalButton.setVisible(SELECTED_ENCOUNTERS.size() != 0);
						globalButton.addActionListener(e2 ->
						{
							selectFile();
						});
						resetActionListener();
					});

					encounterPanel.add(button);
				});

		globalButton.addActionListener(e ->
		{
			selectFile();
		});
		resetActionListener();
	}

	public void selectFile()
	{
		filePanel.setLayout(null);
		JLabel title = createLabel("Select File(s): ");
		title.setBounds(10, 0, 120, 50);
		title.setVerticalAlignment(SwingConstants.TOP);
		filePanel.add(title);

		HashMap<Encounter, AbstractLogEntry> map = new HashMap<>();
		int offset = 0;
		// Initialize File Panel
		for (Encounter encounter : SELECTED_ENCOUNTERS)
		{
			AbstractLogEntry entry = new AbstractLogEntry(encounter, offset++);
			entry.createTitle(filePanel);
			entry.createComboBox(filePanel);

			map.put(encounter, entry);
		}

		globalButton.setText("Upload All");
		globalButton.addActionListener(e ->
		{
			globalButton.setText("Uploading...");
			System.out.println("Started upload task.");

			List<String> links = new ArrayList<>();
			for (Encounter encounter : SELECTED_ENCOUNTERS)
			{
				AbstractLogEntry entry = map.get(encounter);
				entry.uploadSelectedFile();
				links.add(entry.getLink());

				entry.createLogLink(filePanel);
				entry.createCopyButton(filePanel);
			}

			System.out.println("Upload task finished.");

			createCopyAll(links);

			filePanel.revalidate();
			filePanel.repaint();
		});
		resetActionListener();

		filePanel.revalidate();
		filePanel.repaint();
	}

	private void createCopyAll(List<String> links)
	{
		globalButton.setText("Copy All");
		globalButton.addActionListener(e ->
		{
			StringSelection stringSelection = new StringSelection(String.join("\n", links));
			Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			clipboard.setContents(stringSelection, null);
		});
		resetActionListener();
	}

	public static JComboBox createComboBox(String[] items)
	{
		JComboBox comboBox = new JComboBox(items);
		comboBox.setFont(GLOBAL_FONT);
		return comboBox;
	}

	public static JButton createButton(String title)
	{
		JButton button = new JButton(title);
		button.setFont(GLOBAL_FONT);
		return button;
	}

	public static JLabel createLabel(String s)
	{
		JLabel label = new JLabel(s);
		label.setFont(GLOBAL_FONT);
		return label;
	}

	public static JToggleButton createToggleButton(String title)
	{
		JToggleButton button = new JToggleButton(title);
		button.setFont(GLOBAL_FONT);
		return button;
	}

	private void resetActionListener()
	{
		while (globalButton.getActionListeners().length > 1)
			globalButton.removeActionListener(globalButton.getActionListeners()[1]);
	}

	public boolean hasRaidSelected()
	{
		return SELECTED_ENCOUNTER_TYPE == 2;
	}

	public boolean isRaidWingSet()
	{
		return hasRaidSelected() && SELECTED_RAID_WING != 0;
	}
}
