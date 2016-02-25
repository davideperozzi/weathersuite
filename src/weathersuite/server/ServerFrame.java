package weathersuite.server;
import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;

public class ServerFrame extends JFrame
{
	private static final long serialVersionUID = 1L;
	
	private JLabel stationLabel;
	private JTable stationTable;
	private DefaultTableModel stationModel;
	private JLabel clientLabel;
	private JTable clientTable;
	private DefaultTableModel clientModel;
	private int stationCounter = 0;
	private int clientCounter = 0;
	
	public ServerFrame(String title) {
		super(title);
		
		// Create Grid-Layout panel
		JPanel panel = new JPanel(new GridLayout(2, 2));
		
		// Create client label
		panel.add(this.clientLabel = new JLabel());
		this.updateClientLabel();
		
		// Create client table
		this.clientModel = new DefaultTableModel();
		this.clientTable = new JTable(this.clientModel);
		
		panel.add(new JScrollPane(this.clientTable));
		
		this.clientModel.addColumn("Session ID");
		this.clientModel.addColumn("IP-Address");
		
		// Create station table label
		panel.add(this.stationLabel = new JLabel());
		this.updateStationLabel();
		
		// Create station table
		this.stationModel = new DefaultTableModel();
		this.stationTable = new JTable(this.stationModel);
		
		panel.add(new JScrollPane(this.stationTable));
		
		this.stationModel.addColumn("Session ID");
		this.stationModel.addColumn("IP-Address");
		
		// Add panel
		this.add(panel);
	}
	
	private void updateStationLabel() {
		this.stationLabel.setText("Connected stations (" + this.stationCounter + "): ");
	}
	
	private void updateClientLabel() {
		this.clientLabel.setText("Connected clients (" + this.clientCounter + "): ");
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