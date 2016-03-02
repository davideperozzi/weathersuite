package weathersuite;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.*;
import java.net.*;
import java.util.ArrayList;

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
	
	public Client(String host, int port) {
		this.host = host;
		this.port = port;
		
		this.frame = new ClientFrame("Weather Client");
		this.frame.addWindowListener(new WindowListener(){
			@Override
			public void windowClosing(WindowEvent e) {		
				Client.this.disconnect();
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
				Client.this.writeObject(
					new WrapperModel(new DataModel(location, type, map >= 1 ? true : false))
				);
			}
		});
		
		this.frame.setSize(600, 600);
		this.frame.setVisible(true);
	}
	
	public void connect() {
		try {
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
			e.printStackTrace();
		}
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
		this.frame.setClientCounter(Integer.toString(model.clientCount));
		this.frame.setStationCounter(Integer.toString(model.stationCount));
	}

	synchronized private void processInput(ArrayList<DataModel> models) {
		
	}
}
