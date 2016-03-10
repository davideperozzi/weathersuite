package weathersuite.server;
import java.awt.Dimension;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.table.*;

import net.miginfocom.swing.MigLayout;

public class ServerFrame extends JFrame
{
	private static final long serialVersionUID = 1L;
	
	private JLabel stationLabel;
	private JTable stationTable;
	private DefaultTableModel stationModel;
	private JLabel clientLabel;
	private JTable clientTable;
	private JTextArea logArea;
	private DefaultTableModel clientModel;
	private int stationCounter = 0;
	private int clientCounter = 0;
	
	public ServerFrame(String title) {
		super(title);
		
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} 
		catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e1) {
			e1.printStackTrace();
		}
		
		this.getContentPane().setLayout(
			new MigLayout("", "[][grow][grow]", "[][][][grow][grow]")
		);
		
		// Client label
		this.getContentPane().add(this.clientLabel = new JLabel(), "cell 1 1");
		this.updateClientLabel();
		
		// Station label
		this.getContentPane().add(this.stationLabel = new JLabel(), "cell 2 1");
		this.updateStationLabel();
		
		JScrollPane scrollPane = new JScrollPane();
		this.getContentPane().add(scrollPane, "cell 1 3,grow");
		
		// Create client table
		this.clientModel = new DefaultTableModel();
		this.clientTable = new JTable(this.clientModel);
		this.clientTable.setEnabled(false);
		
		scrollPane.setViewportView(this.clientTable);
		
		this.clientModel.addColumn("Session ID");
		this.clientModel.addColumn("IP-Address");
		
		// Create station table
		JScrollPane scrollPane2 = new JScrollPane();
		this.getContentPane().add(scrollPane2, "cell 2 3,grow");
		
		this.stationModel = new DefaultTableModel();
		this.stationTable = new JTable(this.stationModel);
		this.stationTable.setEnabled(false);
		
		scrollPane2.setViewportView(this.stationTable);
		
		this.stationModel.addColumn("Session ID");
		this.stationModel.addColumn("IP-Address");
		
		final JScrollPane scrollPane3 = new JScrollPane();
		this.getContentPane().add(scrollPane3, "cell 1 4 2 1,grow");
		
		scrollPane3.setMaximumSize(new Dimension(Integer.MAX_VALUE, 300));
		
		// Create log text area
		this.logArea = new JTextArea();
		this.logArea.setEditable(false);
		scrollPane3.setViewportView(this.logArea);
		this.setLogAreaText(Logger.getLines());
		
		JScrollBar vertical = scrollPane3.getVerticalScrollBar();
		vertical.setValue(vertical.getMaximum());
		
		Logger.onLog(new LogListener() {
			@Override
			public void onLog(ArrayList<String> lines) {
				ServerFrame.this.setLogAreaText(lines);
				
				JScrollBar vertical = scrollPane3.getVerticalScrollBar();
				vertical.setValue(vertical.getMaximum());
			}
		});
	}
	
	private void setLogAreaText(ArrayList<String> lines) {
		String output = "";
		
		for (String line : lines) {
			output += line + "\n";
		}
		
		this.logArea.setText(output);
	}
	
	private void updateStationLabel() {
		this.stationLabel.setText("Stationen (" + this.stationCounter + ")");
	}
	
	private void updateClientLabel() {
		this.clientLabel.setText("Clients (" + this.clientCounter + ")");
	}
	
	public void addStation(String id, String ipAddress) {
		this.stationModel.addRow(new Object[]{id, ipAddress});
		
		this.stationCounter++;
		this.updateStationLabel();
	}
	
	public void removeStation(String id) {
		int rowCount = this.stationModel.getRowCount();
		
		for (int i = 0; i < rowCount; i++) {
			if (this.stationModel.getValueAt(i, 0).equals(id)) {
				this.stationModel.removeRow(i);
				
				this.stationCounter--;
				this.updateStationLabel();
				break;
			}
		}
	}
	
	public void addClient(String id, String ipAddress) {
		this.clientModel.addRow(new Object[]{id, ipAddress});
		
		this.clientCounter++;
		this.updateClientLabel();
	}
	
	public void removeClient(String id) {
		int rowCount = this.clientModel.getRowCount();
		
		for (int i = 0; i < rowCount; i++) {
			if (this.clientModel.getValueAt(i, 0).equals(id)) {
				this.clientModel.removeRow(i);
				
				this.clientCounter--;
				this.updateClientLabel();
				break;
			}
		}
	}
}