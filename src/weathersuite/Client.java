package weathersuite;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Collections;

import weathersuite.client.*;
import weathersuite.models.DataModel;
import weathersuite.models.StatisticModel;
import weathersuite.models.WrapperModel;

public class Client 
{	
	private String host;
	private int port;
	private Socket socket;
	private ClientFrame frame;
	private ObjectOutputStream out;
	private boolean connectionResetActive;
	final static int TIMEOUT_RESET = 10;
	
	public Client(String host, int port) {
		this.host = host;
		this.port = port;
		
		this.frame = new ClientFrame("Weathersuite: Client");
		this.frame.setBounds(100, 100, 800, 680);
		this.frame.setVisible(true);
		this.frame.readRegions();
		this.frame.addWindowListener(new WindowListener(){
			@Override
			public void windowClosing(WindowEvent e) {
				if (Client.this.socket != null) {
					Client.this.disconnect();
				}
				System.exit(1);
			}
			@Override
			public void windowActivated(WindowEvent e) {}
			@Override
			public void windowClosed(WindowEvent e) {}
			@Override
			public void windowDeactivated(WindowEvent e) {}
			@Override
			public void windowDeiconified(WindowEvent e) {}
			@Override
			public void windowIconified(WindowEvent e) {}
			@Override
			public void windowOpened(WindowEvent e) {}
		});
		
		this.frame.addFormListener(new FormListener() {
			@Override
			public void onUpdate(String type, String location, int map) {
				if (Client.this.socket != null && !Client.this.socket.isClosed()) {
					Client.this.writeObject(
						new WrapperModel(new DataModel(location, type, map >= 1 ? true : false))
					);
				}
				else {
					Client.this.connectionReset();
				}
			}
		});
	}
	
	public void connect() {
		try {
			this.connectionResetActive = false;
			this.frame.setOutputText("Verbinde...");
			this.socket = new Socket(this.host, this.port);
			
			try {
				this.out = new ObjectOutputStream(this.socket.getOutputStream());
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			(new Thread(){
				public void run() {
					ObjectInputStream input = null;
					
					try {
						input = new ObjectInputStream(Client.this.socket.getInputStream());
						
						Client.this.frame.setOutputText("");
						
						while (true) {
							WrapperModel wrapper = (WrapperModel)input.readObject();
							
							switch (wrapper.type) {
								case WrapperModel.TYPE_DATA_MODELS:
									Client.this.processInput(wrapper.dataModels);
									break;
									
								case WrapperModel.TYPE_STATISTIC_MODEL:
									Client.this.processInput(wrapper.statisticModel);
									break;
							}
						}
					}
					catch (SocketException e) {
						// e.printStackTrace();
					}
					catch (IOException e) {
						e.printStackTrace();
					} 
					catch (ClassNotFoundException e) {
						e.printStackTrace();
					}
					finally {
						this.interrupt();
						
						if ( ! Client.this.socket.isClosed()) {
							Client.this.disconnect();
						}
					}
				}
			}).start();
		} 
		catch (IOException e) {
			this.connectionReset();
		}
	}
	
	private void connectionReset() {
		if (this.connectionResetActive) {
			return;
		}
		
		this.connectionResetActive = true;
		final String connectMsg = "Verbindung zum Server fehlgeschlagen!\n"
				+ "Erneuter Verbindungsversuch in %s Sekunde(n).";
		
		this.frame.setOutputText(String.format(connectMsg, TIMEOUT_RESET));
		
		(new Thread(){
			private int secondsLast = TIMEOUT_RESET;
			
			public void run() {
				while (true) {
					try {
						sleep(1000);
					} 
					catch (InterruptedException e) {
						e.printStackTrace();
					}
					
					Client.this.frame.setOutputText(String.format(connectMsg, --secondsLast));
					
					if (secondsLast <= 0) {
						Client.this.connect();
						this.interrupt();
						break;
					}
				}
			}
		}).start();
	}
	
	public void disconnect() {
		try {
			this.socket.close();
		} 
		catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	synchronized private void writeObject(WrapperModel model) {
		try {
			this.out.writeObject(model);
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	synchronized private void processInput(StatisticModel model) {
		this.frame.setClientCounter(model.clientCount);
		this.frame.setStationCounter(model.stationCount);
	}
	
	synchronized static String parseStatus(String status) {
		try {
			return parseStatus(Math.round(Float.parseFloat(status)));
		} 
		catch (NumberFormatException e) {
			e.printStackTrace();
		}
		
		return "";
	}
	
	synchronized static String parseStatus(int status) {
		switch (status) {
			case 9: return "sonnig"; 
			case 8: return "meist Sonnig";
			case 7: return "teils wolkig";
			case 6: return "bedeckt";
			case 5: return "Leichter Regen";
			case 4: return "Regen";
			case 3: return "Nebel";
			case 2: return "Leichter Schneefall";
			case 1: return "Schneefall";
		}
		
		return "Unbekannt";
	}

	synchronized private void processInput(ArrayList<DataModel> models) {
		if (models.size() > 0) {
			String value = "";
			
			if (this.frame.isMap()) {
				// Sort models
				Collections.sort(models);
				
				// Columns
				int columns = 5;
				
				for (int i = 0, len = models.size(); i < len; i++) {
					DataModel model = models.get(i);
					String modelValue = this.frame.getTypeKey().equals("s") 
							? parseStatus(model.getValue()) 
							: model.getValue();
					
					if (i > 0) {
						if (i % columns == 0) {
							value += "\n\n";
						}
						else {
							value += "  |  ";
						}
					}
		
					value += model.getZipCode();
					value +=  ": " + modelValue + " " + this.frame.getUnit();
				}
			}
			else {
				if (this.frame.getLocationFieldText().endsWith("*")) {
					double sum = 0;
					
					for (DataModel model : models) {
						sum += Double.parseDouble(model.getValue());
					}
					
					sum /= models.size();
					sum = Math.round(sum);
					
					value = Double.toString(sum);
				}
				else {
					value = models.get(0).getValue();
				}
				
				if (this.frame.getTypeKey().equals("s")) {
					value = parseStatus(value);
				}
				else {
					value += " " + this.frame.getUnit();
				}
			}
			
			this.frame.setOutputText(value);
		}
		else {
			this.frame.setOutputText("Keine Wetterdaten vorhanden");
		}
	}
}
