package weathersuite.client;

import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

import javax.swing.*;

import net.miginfocom.swing.MigLayout;

public class ClientFrame extends JFrame
{
	private static final long serialVersionUID = 1L;
	public static String REGION_FILE = "regions.csv";
	public static String locationPlaceholder = "Region";
	
	private FormListener formListener;
	private JLabel clientCounterLabel;
	private JLabel stationCounterLabel;
	private JTextField locationField;
	private JTextArea outputField;
	private Checkbox weathermapCheckbox;
	private JComboBox<String> typeSelection;
	private int clientCounter = 0;
	private int stationCounter = 0;
	private HashMap<String, String> types = new HashMap<String, String>();
	private HashMap<String, String> units = new HashMap<String, String>();
	private HashMap<String, Integer> regions = new HashMap<String, Integer>();
	private boolean regionsFeatureEnabled = false;

	public ClientFrame(String title) {
		super(title);
		
		// Add border layout
		this.getContentPane().setLayout(new BorderLayout(0, 0));
		
		// Create panel
		JPanel panel = new JPanel();
		this.getContentPane().add(panel, BorderLayout.NORTH);
		
		// Set types
		this.types.put("t", "Temperatur");
		this.types.put("s", "Status");
		this.types.put("w", "Wind");
		
		// Set units
		this.units.put("t", "°C");
		this.units.put("s", "");
		this.units.put("w", "Bft");
		
		// Station counter label
		this.stationCounterLabel = new JLabel();
		this.stationCounterLabel.setFont(new Font("Arial", Font.PLAIN, 18));
		this.setStationCounter(this.stationCounter);
		panel.add(this.stationCounterLabel);
		
		// Seperator label
		JLabel sepLabel = new JLabel("|");
		sepLabel.setFont(new Font("Arial", Font.PLAIN, 18));
		panel.add(sepLabel);
		
		// Client counter label
		this.clientCounterLabel = new JLabel("Clients: 0");
		this.clientCounterLabel.setFont(new Font("Arial", Font.PLAIN, 18));
		this.setClientCounter(this.clientCounter);
		panel.add(this.clientCounterLabel);
		
		// Create panel 2
		JPanel panel2 = new JPanel();
		this.getContentPane().add(panel2, BorderLayout.CENTER);
		panel2.setLayout(new MigLayout("", "[grow]", "[][grow][]"));

		// Locatioan text field		
		this.locationField = new JTextField();
		this.locationField.setFont(new Font("Arial", Font.PLAIN, 18));
		this.locationField.setForeground(new Color(122, 122, 122));
		this.locationField.setColumns(10);
		this.locationField.setText(locationPlaceholder);
		this.locationField.addFocusListener(new FocusListener() {
			@Override
			public void focusLost(FocusEvent e) {
				if (ClientFrame.this.locationField.getText().isEmpty()) {
					ClientFrame.this.locationField.setText(ClientFrame.locationPlaceholder);
					ClientFrame.this.locationField.setForeground(new Color(122, 122, 122));
				}
			}
			
			@Override
			public void focusGained(FocusEvent e) {
				if (ClientFrame.this.locationField.getText().equals(ClientFrame.locationPlaceholder)) {
					ClientFrame.this.locationField.setText("");
					ClientFrame.this.locationField.setForeground(new Color(0, 0, 0));
				}
			}
		});
		panel2.add(this.locationField, "flowx,cell 0 0,alignx leading,growy");
		
		// Weathermap checkbox
		this.weathermapCheckbox = new Checkbox("Wetterkarte");
		this.weathermapCheckbox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				ClientFrame.this.locationField.setEnabled(e.getStateChange() != 1);
			}
		});
		panel2.add(this.weathermapCheckbox, "cell 0 0,growy");
		
		// Type selection
		String[] types = {"Temperatur", "Status", "Wind"};
		this.typeSelection = new JComboBox<String>();
		this.typeSelection.setFont(new Font("Arial", Font.PLAIN, 18));
		this.typeSelection.setModel(new DefaultComboBoxModel<String>(types));
		panel2.add(this.typeSelection, "cell 0 0,grow");
		
		// Create scroll pane
		JScrollPane scrollPane = new JScrollPane();
		panel2.add(scrollPane, "cell 0 1,grow");
		
		// Output text field
		this.outputField = new JTextArea();
		this.outputField.setLineWrap(true);
		this.outputField.setWrapStyleWord(true);
		this.outputField.setEditable(false);
		this.outputField.setFont(new Font("Arial", Font.BOLD, 16));
		scrollPane.setViewportView(this.outputField);
		
		// Submit button		
		JButton btnUpdate = new JButton("Update anfordern");
		btnUpdate.setBackground(SystemColor.menu);
		btnUpdate.setFont(new Font("Arial", Font.BOLD, 20));
		btnUpdate.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ClientFrame.this.updatePerformed();
			}
		});
		panel2.add(btnUpdate, "cell 0 2,growx");
	}
	
	public void addFormListener(FormListener listener) {
		this.formListener = listener;
	}
	
	private void updatePerformed() {
		String text = this.locationField.getText();
		
		if (this.isMap()) {
			this.updateCredentials();
		}
		else {
			if (text.isEmpty() || text.equals(locationPlaceholder)) {
				this.showError("Bitte Region eingeben");
			}
			else {
				try {
					int plz = -1;
					
					if (this.regionsFeatureEnabled && this.regions.get(text) != null) {
						plz = this.regions.get(text);
					}
					else if (text.endsWith("*") && text.length() == 2) {
						plz = Integer.parseInt(text.substring(0, 1));
					}
					else {
						plz = Integer.parseInt(text);
					}
					
					if (plz >= 0 && plz <= 99) {
						this.updateCredentials();
					}
					else {
						this.showError("Bitte gültige Region eingeben");
					}
				}
				catch (NumberFormatException e) {
					this.showError("Bitte gültige Region eingeben");
				}
			}
		}
	}
	
	private void updateCredentials() {
		if (this.formListener != null) {
			String location = this.locationField.getText();
			
			this.formListener.onUpdate(
				this.typeSelection.getSelectedItem().toString(), 
				this.regionsFeatureEnabled && this.regions.containsKey(location)
					? this.regions.get(location).toString() 
					: this.locationField.getText(),
				this.weathermapCheckbox.getState() ? 1 : 0
			);
		}
	}
	
	private void showError(String message) {
		JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
	}
	
	public void setClientCounter(int count) {
		this.clientCounter = count;
		this.clientCounterLabel.setText("Clients: " + count);
	}
	
	public void setStationCounter(int count) {
		this.stationCounter = count;
		this.stationCounterLabel.setText("Stationen: " + count);
	}
	
	public String getTypeKey() {
		String selected = this.typeSelection.getSelectedItem().toString();
		String key = "";
		
		for (String k : this.types.keySet()) {
			if (this.types.get(k).equals(selected)) {
				key = k;
				break;
			}
		}
		
		return key;
	}
	
	public String getLocationFieldText() {	
		return this.locationField.getText();
	}
	
	public void setOutputText(String text) {
		this.outputField.setText(text);
	}
	
	public String getUnit() {
		String key = this.getTypeKey();
		
		if (key.isEmpty()) {
			return key;
		}
		
		return this.getUnit(key);
	}
	
	public String getUnit(String key) {
		return this.units.get(key);
	}
	
	public boolean isMap() {
		return this.weathermapCheckbox.getState();
	}
	
	public void readRegions() {
		this.regionsFeatureEnabled = true;
		
		try {
			@SuppressWarnings("resource")
			BufferedReader in = new BufferedReader(new FileReader(REGION_FILE));
			String line;
			
			while ((line = in.readLine()) != null && line.length() > 0) {
				String[] parts = line.split("\\t");
				
				if (parts.length == 2) {
					int id = -1;
					
					parts[0] = parts[0].trim();
					parts[1] = parts[1].trim();
					
					try {
						id = Integer.parseInt(parts[0]);
					}
					catch (NumberFormatException e) {
						continue;
					}
					
					if (parts[1].startsWith("– ") || parts[1].startsWith("-")) {
						continue;
					}
					
					String[] regions = parts[1].split(","); 
					
					for (String region : regions) {
						this.regions.put(region.trim(), id);
					}
				}
			}
		} 
		catch (IOException e) {	
			this.regionsFeatureEnabled = false;
		}
		
		if ( ! this.regionsFeatureEnabled) {
			System.out.println("Warnung: Textuelle Ortseingaben wurden deaktiviert!");
		}
	}
}
