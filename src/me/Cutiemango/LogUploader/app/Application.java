package me.Cutiemango.LogUploader.app;

import me.Cutiemango.LogUploader.Encounter;
import me.Cutiemango.LogUploader.uploader.UploadHelper;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Application
{
	private JFrame frame;
	// 0, 1, 2, 3, 4, 5, 6(Button)
	private JPanel mainPanel, directoryPanel, typePanel, wingPanel, encounterPanel, characterPanel, filePanel;
	// The button at the lowest right
	private JButton button;
	private Font GLOBAL_FONT = new Font("微軟正黑體", Font.PLAIN, 18);

	// Selected Items
	private int SELECTED_ENCOUNTER_TYPE = -1;
	private int SELECTED_RAID_WING = 0;
	private List<Encounter> SELECTED_ENCOUNTERS = new ArrayList<>();
	private String CHARACTER = "";

	private String LOG_DIRECTORY = null;

	public Application(String title, int width, int height)
	{
		frame = new JFrame(title);
		mainPanel = new JPanel();

		frame.setSize(width, height);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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

		characterPanel = new JPanel();
		characterPanel.setBounds(0, 240, 960, 60);

		filePanel = new JPanel();
		filePanel.setBounds(0, 300, 960, 180);

		button = createButton("Next");
		button.setBounds(0, 500, 960, 60);
		button.setVisible(false);

		mainPanel.add(directoryPanel);
		mainPanel.add(typePanel);
		mainPanel.add(wingPanel);
		mainPanel.add(encounterPanel);
		mainPanel.add(characterPanel);
		mainPanel.add(filePanel);
		mainPanel.add(button);

		setUpDirectory();

		frame.setVisible(true);
	}

	public void setUpDirectory()
	{
		// Set up directory panel
		directoryPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

		JLabel directory = createLabel("ArcDPS Log Directory: ");
		directoryPanel.add(directory);

		JLabel msg = createLabel("<Please select the directory>");
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
				if (LOG_DIRECTORY == null)
					selectType();
				LOG_DIRECTORY = fc.getSelectedFile().getAbsolutePath();
			}
			msg.setText(hasDirectorySelected() ? LOG_DIRECTORY : "<Not a valid directory>");
		});

		directoryPanel.add(browse);
	}

	// Step: 2
	public void selectType()
	{
		typePanel.setLayout(new FlowLayout(FlowLayout.LEFT));

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
	}

	private void resetType()
	{
		SELECTED_RAID_WING = 0;
		SELECTED_ENCOUNTERS.clear();

		checkRemovePanels(2);
	}

	// Step: 3
	public void selectWing()
	{
		wingPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

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
		encounterPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

		JLabel label = createLabel("Select Encounter(s): ");
		encounterPanel.add(label);

		Arrays.stream(Encounter.values())
				.filter(encounter -> isRaidWingSet() ? encounter.getWing() == SELECTED_RAID_WING : encounter.isFractal())
				.forEach(encounter ->
				{
					JToggleButton button = createToggleButton(encounter.toString());
					button.addActionListener(e ->
					{
						checkRemovePanels(4);

						if (button.isSelected())
							SELECTED_ENCOUNTERS.add(encounter);
						else
							SELECTED_ENCOUNTERS.remove(encounter);
					});

					encounterPanel.add(button);
				});

		button.addActionListener(e ->
		{
			resetActionListener();
			selectCharacter();

			characterPanel.revalidate();
			characterPanel.repaint();
		});
		button.setVisible(true);
	}

	// Step: 5
	public void selectCharacter()
	{
		characterPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

		JLabel label = createLabel("Select Character: ");
		characterPanel.add(label);

		Set<String> characters = getCharacters(SELECTED_ENCOUNTERS);
		JComboBox<String> comboBox = createComboBox(characters.toArray(new String[characters.size()]));

		button.addActionListener(e ->
		{
			checkRemovePanels(5);

			CHARACTER = (String)comboBox.getSelectedItem();
			resetActionListener();
			selectFile();

			filePanel.revalidate();
			filePanel.repaint();
		});

		characterPanel.add(comboBox);
		button.setVisible(true);
	}

	public void selectFile()
	{
		filePanel.setLayout(new GridLayout(1, 3));

		int count = SELECTED_ENCOUNTERS.size();

		JPanel encounters = new JPanel();
		encounters.setLayout(new GridLayout(count, 1));
		filePanel.add(encounters);

		JPanel boxPanel = new JPanel();
		boxPanel.setLayout(new GridLayout(count, 1));
		filePanel.add(boxPanel);

		JPanel copyButtons = new JPanel();
		copyButtons.setLayout(new GridLayout(count, 1));
		filePanel.add(copyButtons);

		HashMap<Encounter, List<File>> map = new HashMap<>();
		HashMap<Encounter, JComboBox> boxes = new HashMap<>();
		for (Encounter encounter : SELECTED_ENCOUNTERS)
		{
			JLabel lab = createLabel(encounter.toString() + ": ");
			lab.setHorizontalAlignment(SwingConstants.RIGHT);
			encounters.add(lab);

			List<File> fileList = getLogFiles(encounter);
			Collections.reverse(fileList);
			List<String> list = fileList.stream().map(file -> file.getName()).collect(Collectors.toList());

			JComboBox comboBox = createComboBox(list.toArray(new String[list.size()]));
			boxPanel.add(comboBox);

			boxes.put(encounter, comboBox);
			map.put(encounter, fileList);
		}

		button.setText("Upload All");
		button.addActionListener(e ->
		{
			List<String> links = new ArrayList<>();
			for (Encounter encounter : SELECTED_ENCOUNTERS)
			{
				JComboBox box = boxes.get(encounter);
				File f = map.get(encounter).get(box.getSelectedIndex());
				System.out.println("File Selected: " + box.getSelectedItem());

				String link = UploadHelper.upload(f);
				System.out.println("Link Generated for \'" + f.getName() + "\' : " + link);
				links.add(encounter.toString() + ": " + link);

				JLabel linkLabel = createLabel(link);
				linkLabel.setForeground(Color.BLUE.darker());
				linkLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				linkLabel.addMouseListener(new MouseAdapter()
				{
					@Override
					public void mouseClicked(MouseEvent e)
					{
						try
						{
							Desktop.getDesktop().browse(new URI(link));
						}
						catch (Exception ex)
						{
							ex.printStackTrace();
						}
					}

					@Override
					public void mouseExited(MouseEvent e)
					{
						linkLabel.setText(link);
					}

					@Override
					public void mouseEntered(MouseEvent e)
					{
						linkLabel.setText("<html><a href=''>" + link + "</a></html>");
					}
				});

				boxPanel.add(linkLabel, SELECTED_ENCOUNTERS.indexOf(encounter));

				JButton button = createButton("Copy");
				button.addActionListener(evt ->
				{
					StringSelection stringSelection = new StringSelection(link);
					Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
					clipboard.setContents(stringSelection, null);
					button.setText("Copied");
				});
				button.setHorizontalAlignment(SwingConstants.LEFT);
				copyButtons.add(button);
			}

			resetActionListener();

			button.setText("Copy All");
			button.addActionListener(e1 ->
			{
				resetActionListener();

				StringSelection stringSelection = new StringSelection(String.join("\n", links));
				Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				clipboard.setContents(stringSelection, null);
			});
			button.setVisible(true);
		});
		button.revalidate();
		button.repaint();
	}

	private Set<String> getCharacters(Encounter encounter)
	{
		Set<String> result = new HashSet<>();
		for (File f : new File(LOG_DIRECTORY + File.separator + encounter.getFileName()).listFiles())
			result.add(f.getName());

		return result;
	}

	private Set<String> getCharacters(List<Encounter> encounters)
	{
		Set<String> result = new HashSet<>();
		for (Encounter encounter : encounters)
			result.addAll(getCharacters(encounter));
		return result;
	}

	private List<File> getLogFiles(Encounter encounter)
	{
		return Arrays.asList(new File(LOG_DIRECTORY + File.separator + encounter.getFileName() + File.separator + CHARACTER).listFiles());
	}

	private JComboBox createComboBox(String[] items)
	{
		JComboBox comboBox = new JComboBox(items);
		comboBox.setFont(GLOBAL_FONT);
		return comboBox;
	}

	private JButton createButton(String title)
	{
		JButton button = new JButton(title);
		button.setFont(GLOBAL_FONT);
		return button;
	}

	private JLabel createLabel(String s)
	{
		JLabel label = new JLabel(s);
		label.setFont(GLOBAL_FONT);
		return label;
	}

	private JToggleButton createToggleButton(String title)
	{
		JToggleButton button = new JToggleButton(title);
		button.setFont(GLOBAL_FONT);
		return button;
	}

	private void resetActionListener()
	{
		button.removeActionListener(button.getActionListeners()[0]);
	}
	public boolean hasDirectorySelected()
	{
		return LOG_DIRECTORY != null;
	}

	public boolean hasRaidSelected()
	{
		return hasDirectorySelected() && SELECTED_ENCOUNTER_TYPE == 2;
	}

	public boolean hasFotmSelected()
	{
		return hasDirectorySelected() && SELECTED_ENCOUNTER_TYPE == 1;
	}

	public boolean isRaidWingSet()
	{
		return hasRaidSelected() && SELECTED_RAID_WING != 0;
	}

	public boolean hasEncounterSelected()
	{
		return (isRaidWingSet() || hasFotmSelected()) && !SELECTED_ENCOUNTERS.isEmpty();
	}

	public boolean hasCharacterSelected()
	{
		return hasEncounterSelected() && CHARACTER != null;
	}
}
