package me.Cutiemango.LogUploader;

import me.Cutiemango.LogUploader.app.Application;
import me.Cutiemango.LogUploader.uploader.UploadHelper;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.net.URI;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class AbstractLogEntry
{
	public AbstractLogEntry(Encounter en, int offset)
	{
		encounter = en;
		panelOffset = offset;
	}

	private Encounter encounter;
	private List<File> files;
	private JComboBox comboBox;
	private int panelOffset;
	private String link;

	public void createTitle(JPanel panel)
	{
		JLabel lab = Application.createLabel(encounter.toString() + ": ");
		lab.setBounds(180, 25*panelOffset, 100, 20);
		lab.setHorizontalAlignment(SwingConstants.RIGHT);
		panel.add(lab);
	}

	public void createComboBox(JPanel panel)
	{
		files = getAllFiles(PreferenceManager.getLogDirectory() + File.separator + encounter.getFileName());
		Collections.sort(files, (f1, f2) ->
		{
			try
			{
				return parseDate(f2.getName()).compareTo(parseDate(f1.getName()));
			}
			catch (ParseException e)
			{
				e.printStackTrace();
			}
			return 1;
		});

		List<String> list = files.stream().map(file -> file.getName()).collect(Collectors.toList());
		comboBox = Application.createComboBox(list.toArray(new String[list.size()]));
		comboBox.setFont(new Font("微軟正黑體", Font.PLAIN, 14));
		comboBox.setBounds(300, 25*panelOffset, 440, 20);
		panel.add(comboBox);
	}

	public void uploadSelectedFile()
	{
		System.out.println("File Selected: " + comboBox.getSelectedItem());
		File f = files.get(comboBox.getSelectedIndex());

		link = UploadHelper.upload(f);
		System.out.println("Link Generated for \'" + f.getName() + "\' : " + link);
	}

	public void createLogLink(JPanel panel)
	{
		JLabel linkLabel = Application.createLabel(link);
		linkLabel.setBounds(300, 25*panelOffset, 440, 20);
		linkLabel.setFont(new Font("微軟正黑體", Font.PLAIN, 16));
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

		panel.remove(comboBox);
		panel.add(linkLabel);
	}

	public void createCopyButton(JPanel panel)
	{
		JButton button = Application.createButton("Copy");
		button.setFont(new Font("微軟正黑體", Font.BOLD, 16));
		button.setBounds(760,25*panelOffset,100,20);
		button.addActionListener(evt ->
		{
			StringSelection stringSelection = new StringSelection(link);
			Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			clipboard.setContents(stringSelection, null);
			button.setText("Copied");
		});
		button.setHorizontalAlignment(SwingConstants.CENTER);
		panel.add(button, BorderLayout.CENTER);
	}

	private Date parseDate(String fileName) throws ParseException
	{
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd-HHmmss");
		return df.parse(fileName.replace(".evtc", ""));
	}

	public String getLink()
	{
		return link;
	}

	private List<File> getAllFiles(String directoryName)
	{
		List<File> resultList = new ArrayList<>();

		for (File file : new File(directoryName).listFiles())
		{
			if (file.isDirectory())
				resultList.addAll(getAllFiles(file.getAbsolutePath()));
			else
				resultList.add(file);
		}
		return resultList;
	}
}
