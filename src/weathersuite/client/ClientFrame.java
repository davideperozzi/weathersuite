package weathersuite.client;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class ClientFrame extends Frame
{
	private static final long serialVersionUID = 1L;
	
	private FormListener formListener;
	private JLabel clientCounter;
	private JLabel stationCounter;
	private JTextField locationField;
	private JTextField outputField;
	private Checkbox weathermapCheckbox;
	private JComboBox<String> typeSelection;

	public ClientFrame(String title) {
		super(title);
		
		JPanel panel = new JPanel(new GridLayout(7, 1));
		
		// Station counter label
		this.stationCounter = new JLabel("0");
		
		// Client counter label
		this.clientCounter = new JLabel("0");
		
		// Type selection
		String[] types = {"Temperatur", "Status", "Wind"};
		this.typeSelection = new JComboBox<String>(types);
	
		// Locatioan text field
		this.locationField = new JTextField(1);
		
		// Weathermap checkbox
		this.weathermapCheckbox = new Checkbox("Wetterkarte");
		this.weathermapCheckbox.addItemListener(new ItemListener() {
			
			@Override
			public void itemStateChanged(ItemEvent e) {
				ClientFrame.this.locationField.setEnabled(e.getStateChange() != 1);
			}
		});
		
		// Output text field
		this.outputField = new JTextField(100);
		this.outputField.setEnabled(false);
		
		// Submit button
		JButton submitBtn = new JButton("Update");
		submitBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ClientFrame.this.updatePerformed();
			}
		});
		
		// Add components to panel
		panel.add(this.stationCounter);
		panel.add(this.clientCounter);
		panel.add(this.typeSelection);
		panel.add(this.locationField);
		panel.add(this.weathermapCheckbox);
		panel.add(this.outputField);
		panel.add(submitBtn);
		
		this.add(panel);
	}
	
	public void addFormListener(FormListener listener) {
		this.formListener = listener;
	}
	
	private void updatePerformed() {
		if (this.locationField.getText().length() != 2 && !this.weathermapCheckbox.getState()) {
			this.showError("Bitte gültige Region eingeben");
		}
		else {
			if (this.formListener != null) {
				this.formListener.onUpdate(
					this.typeSelection.getSelectedItem().toString(), 
					this.locationField.getText(),
					this.weathermapCheckbox.getState() ? 1 : 0
				);
			}
		}
	}
	
	private void showError(String message) {
		JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
	}
	
	public void setClientCounter(String count) {
		this.clientCounter.setText(count);
	}
	
	public void setStationCounter(String count) {
		this.stationCounter.setText(count);
	}
}
