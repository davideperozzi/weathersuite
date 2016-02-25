package weathersuite;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.*;
import java.net.*;
import java.util.*;

import weathersuite.server.ClientSession;
import weathersuite.server.DataProvider;
import weathersuite.server.ServerFrame;
import weathersuite.server.SessionInteraction;
import weathersuite.server.StationSession;

public class Server
{	
	public static int STATION_PORT;
	public static int CLIENT_PORT; 
	public static int UPDATE_INTERVAL;
	
	private ServerSocket clientServerSocket;
	private ServerSocket stationServerSocket;
	private ArrayList<ClientSession> clients = new ArrayList<ClientSession>();
	private ArrayList<StationSession> stations = new ArrayList<StationSession>();
	private DataProvider dataProvider;
	private ServerFrame frame;
	
	public Server(int stationPort, int clientPort) {
		this(stationPort, clientPort, 1000);
	}
	
	public Server(int stationPort, int clientPort, int updateInterval) {
		// Set static configs
		STATION_PORT = stationPort;
		CLIENT_PORT = clientPort;
		UPDATE_INTERVAL = updateInterval;
		
		// Setup Frame
		this.frame = new ServerFrame("Weather Server");		
		this.frame.addWindowListener(new WindowListener(){
			@Override
			public void windowClosing(WindowEvent e) {		
				try {
					Server.this.stationServerSocket.close();
					Server.this.clientServerSocket.close();
				} 
				catch (IOException e1) {
					e1.printStackTrace();
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
		
		this.frame.setSize(600, 600);
		this.frame.setVisible(true);
		
		// Create data provider to save the data in
		this.dataProvider = new DataProvider("weather-data");
		
		// Create server sockets
		try {
			this.clientServerSocket = new ServerSocket(clientPort);
			this.stationServerSocket = new ServerSocket(stationPort);
		}
		catch (BindException e) {
			System.err.println("Something went wrong while creating the Server sockets. \n"
					+ "Maybe some ports are blocked?: ");
			e.printStackTrace();
			
			// Exit with error code
			System.exit(1);
		}
		catch (IOException e) {
			e.printStackTrace();
			
			// Stop the application. 
			// Without sockets -> no interaction
			System.exit(1);
		}
	}
	
	public void run() {		
		// Wait for stations
		(new Thread(){
			public void run() {
				try {
					while(true) {						
						Socket socket = Server.this.stationServerSocket.accept();
						final StationSession session = new StationSession(socket);
						
						Server.this.connect(session);
					}
				} 
				catch (IOException e) {
					//e.printStackTrace();
				}
				finally {
					if (Server.this.stationServerSocket != null) {
						try {
							Server.this.stationServerSocket.close();
							this.interrupt();
						} 
						catch (IOException e) {
							// Error while closing server socket
						}
					}
				}
			}
		}).start();
		
		// Wait for clients 
		(new Thread(){
			public void run() {
				try {
					while (true) {						
						Socket socket = Server.this.clientServerSocket.accept();						
						final ClientSession session = new ClientSession(socket);
						
						Server.this.connect(session);
					}
				}
				catch (IOException e) {
					//e.printStackTrace();
				}
				finally {
					if (Server.this.clientServerSocket != null) {
						try {
							Server.this.clientServerSocket.close();
						} 
						catch (IOException e) {
							// Error while closing server socket
						}
					}
				}
			}
		}).start();
	}
	
	synchronized private void connect(final StationSession session) {
		SessionInteraction interaction = new SessionInteraction(){
			@Override
		    public void onDisconnect() {
				Server.this.disconnect(session);
			}
			
			@Override
			public void onDataSend(String data) {
				Server.this.dataProvider.parseData(data);
				
				try {
					Server.this.dataProvider.save();
				} 
				catch (IOException e) {
					System.err.println("Error while saving data: ");
					e.printStackTrace();
				}
			}
		};
		
		session.setInteraction(interaction);
		session.start();
		
		this.frame.addStation(
			session.getUid(), 
			session.getSocket().getInetAddress().toString()
		);
			
		this.stations.add(session);
		this.updateClientStatistics();
	}
	
	synchronized private void disconnect(StationSession session) {
		ArrayList<Integer> removeIndicies = new ArrayList<Integer>();
		
		// Collect indices to remove and remove active 
		// station from the frame table
		for (StationSession station : this.stations) {
			if (station.getUid().equals(session.getUid())) {
				int index = this.stations.indexOf(station);
			 	
				if (index >= 0) {
					this.frame.removeStation(station.getUid());
					removeIndicies.add(index);
				}
			}
		}
		
		// Remove objects by collected indices
		for (Integer index : removeIndicies) {
			this.stations.remove(this.stations.get(index));
		}
		
		this.updateClientStatistics();
	}
	
	synchronized private void connect(final ClientSession session) {
		SessionInteraction interaction = new SessionInteraction(){
			@Override
		    public void onDisconnect() {
				Server.this.disconnect(session);
			}
			
			@Override
			public void onDataSend(String data) {
				System.out.println(data);
			}
		};
		
		session.setInteraction(interaction);
		session.start();
		
		this.frame.addClient(
			session.getUid(), 
			session.getSocket().getInetAddress().toString()
		);
				
		this.clients.add(session);
		this.updateClientStatistics();
	}
	
	synchronized private void disconnect(ClientSession session) {
		ArrayList<Integer> removeIndicies = new ArrayList<Integer>();
		
		// Collect indices to remove and remove active 
		// station from the frame table
		for (ClientSession client : this.clients) {
			if (client.getUid().equals(session.getUid())) {
				int index = this.clients.indexOf(client);
			 	
				if (index >= 0) {
					this.frame.removeClient(client.getUid());
					removeIndicies.add(index);
				}
			}
		}
		
		// Remove objects by collected indices
		for (Integer index : removeIndicies) {
			this.clients.remove(this.clients.get(index));
		}
		
		this.updateClientStatistics();
	}
	
	synchronized private void updateClientStatistics() {
		for (ClientSession session : this.clients) {
			session.updateStatistics(this.stations.size(), this.clients.size());
		}
	}
}
