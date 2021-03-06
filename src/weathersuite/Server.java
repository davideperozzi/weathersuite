package weathersuite;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.*;
import java.net.*;
import java.util.*;

import weathersuite.models.DataModel;
import weathersuite.server.ClientSession;
import weathersuite.server.DataProvider;
import weathersuite.server.Logger;
import weathersuite.server.ServerFrame;
import weathersuite.server.SessionInteraction;
import weathersuite.server.StationSession;

public class Server
{		
	private int stationPort;
	private int clientPort;
	private ServerSocket clientServerSocket;
	private ServerSocket stationServerSocket;
	private ArrayList<ClientSession> clients = new ArrayList<ClientSession>();
	private ArrayList<StationSession> stations = new ArrayList<StationSession>();
	private DataProvider dataProvider;
	private ServerFrame frame;
	
	public Server(int stationPort, int clientPort) {		
		// Set configs
		this.stationPort = stationPort;
		this.clientPort = clientPort;
		
		// Setup Frame
		this.frame = new ServerFrame("Weathersuite: Server");		
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
				
				Logger.log("Server shutted down");
				
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
		
		this.frame.setSize(860, 600);
		this.frame.setVisible(true);
		
		// Create data provider to save the data in
		this.dataProvider = new DataProvider("weather-data");
		
		// Create server sockets
		try {
			this.clientServerSocket = new ServerSocket(this.clientPort);
			this.stationServerSocket = new ServerSocket(this.stationPort);
		}
		catch (BindException e) {
			System.err.println("Something went wrong while creating the Server sockets. \n"
					+ "Maybe some ports are blocked?: ");
			e.printStackTrace();
			
			Logger.log("Server shutted down: " + e.getClass());
			
			// Exit with error code
			System.exit(1);
		}
		catch (IOException e) {
			e.printStackTrace();
			
			Logger.log("Server shutted down: " + e.getClass());
			
			// Stop the application. 
			// Without sockets -> no interaction
			System.exit(1);
		}
		
		Logger.log("Server started");
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
				DataModel model = Server.this.dataProvider.parseData(data);
				
				try {
					Server.this.dataProvider.save();
				} 
				catch (IOException e) {
					Logger.log("Error while saving station data");
					System.err.println("Error while saving data: ");
					e.printStackTrace();
				}
				
				// Update client sessions data
				if (model != null) {
					for (ClientSession session : Server.this.clients) {
						if (session.matchModel(model) || session.isMap()) {
							Server.this.updateClientData(session);
						}
					}
				}
			}

			@Override
			public void onUpdateClientSession() {}
		};
		
		session.setInteraction(interaction);
		session.start();
		
		this.frame.addStation(
			session.getUid(), 
			session.getSocket().getInetAddress().toString()
		);
			
		this.stations.add(session);
		this.updateClientStatistics();
		
		Logger.log("Station " + session.getUid() + " with " + session.getSocket().getInetAddress() + " connected");
	}
	
	synchronized private void disconnect(StationSession session) {
		ArrayList<Integer> removeIndicies = new ArrayList<Integer>();
		
		// Collect indices to remove the
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
		
		Logger.log("Station " + session.getUid() + " with " + session.getSocket().getInetAddress() + " disconnected");
	}
	
	synchronized private void connect(final ClientSession session) {
		SessionInteraction interaction = new SessionInteraction(){
			@Override
		    public void onDisconnect() {
				Server.this.disconnect(session);
			}
			
			@Override
			public void onDataSend(String data) {}

			@Override
			public void onUpdateClientSession() {
				Server.this.updateClientData(session);
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
		
		Logger.log("Client " + session.getUid() + " with " + session.getSocket().getInetAddress() + " connected");
	}
	
	synchronized private void disconnect(ClientSession session) {
		ArrayList<Integer> removeIndicies = new ArrayList<Integer>();
		
		// Collect indices to remove the
		// client from the frame table
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
		
		Logger.log("Client " + session.getUid() + " with " + session.getSocket().getInetAddress() + " disconnected");
	}
	
	synchronized private void updateClientStatistics() {
		for (ClientSession session : this.clients) {
			session.updateStatistics(this.clients.size(), this.stations.size());
		}
	}
	
	synchronized private void updateClientData(ClientSession session) {
		ArrayList<DataModel> models = new ArrayList<DataModel>();
	
		if (session.isMap()) {
			models = this.dataProvider.getData(session.getType());
		}
		else {
			models = this.dataProvider.getData(
				session.getZipCode(),
				session.getType()
			);
		}
		
		session.updateData(models);
	}
}
