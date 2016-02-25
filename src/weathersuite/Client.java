package weathersuite;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.*;
import java.net.*;
import java.util.ArrayList;

import weathersuite.client.*;
import weathersuite.models.DataModel;

public class Client 
{
	private String host;
	private int port;
	private Socket socket;
	private ClientFrame frame;
	
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
				try {
					PrintWriter out = new PrintWriter(Client.this.socket.getOutputStream(), true);
					out.println(type + ":" + location + ":" + map);
				} 
				catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				System.out.println("Update: " + type + " - " + location + " - " + map);
			}
		});
		
		this.frame.setSize(600, 600);
		this.frame.setVisible(true);
	}
	
	public void connect() {
		try {
			this.socket = new Socket(this.host, this.port);
			
			(new Thread(){
				public void run() {
					try {
						BufferedReader input = new BufferedReader(
							new InputStreamReader(Client.this.socket.getInputStream())
						);
						
						String line;
						while ((line = input.readLine()) != null && line.length() != 0) {
							Client.this.processInput(line);
						}
					} 
					catch (IOException e) {
						// Ignore the error while reading
						// and just dispose this session
					}
					finally {
						this.interrupt();
					}
				}
			}).start();
			
			(new Thread(){
				@SuppressWarnings("unchecked")
				public void run() {
					ObjectInputStream input;
					try {
						input = new ObjectInputStream(Client.this.socket.getInputStream());
						
						ArrayList<DataModel> models;
						while ((models = (ArrayList<DataModel>)input.readObject()) != null) {
							for (DataModel model : models) {
								System.out.println("Model: " + model.getZipCode());
							}
						}
					} 
					catch (IOException e) {
						e.printStackTrace();
					} 
					catch (ClassNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
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
	
	synchronized private void processInput(String line) {
		String commandType = line.substring(0, 3);
		
		if (commandType.equals("<s>")) {
			String[] values = line.substring(3).split(":");
			
			this.frame.setStationCounter(values[0]);
			this.frame.setClientCounter(values[1]);
		}
		else {
			System.out.println("Line: " + line);
		}
	}
}
