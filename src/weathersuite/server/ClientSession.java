package weathersuite.server;

import java.io.*;
import java.net.*;
import java.util.ArrayList;

import weathersuite.Client;
import weathersuite.models.DataModel;
import weathersuite.models.StatisticModel;
import weathersuite.models.WrapperModel;

public class ClientSession extends AbstractSession
{
	private String zipCode;
	private int type;
	private boolean map;
	ObjectOutputStream out;
	
	public ClientSession(Socket socket) {	
		super(socket);
		
		try {
			this.out = new ObjectOutputStream(this.socket.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void run() {		
		// Thread for input
		(new Thread(){
			public void run(){
				try {
					ObjectInputStream input = new ObjectInputStream(ClientSession.this.socket.getInputStream());
					
					while (true) {
						WrapperModel wrapper = (WrapperModel)input.readObject();
						
						switch (wrapper.type) {
							case WrapperModel.TYPE_DATA_MODEL:
								ClientSession.this.processInput(wrapper.dataModel);
								break;
						}
					}
				} 
				catch (IOException e) {
					// Ignore the error while reading
					// and just dispose this session
				} 
				catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
				finally {
					ClientSession.this.disposeInternal();
				}
			}
		}).start();
	}
	
	public void updateStatistics(int stationCount, int clientCount) {
		if (this.socket.isClosed()) {
			this.disposeInternal();
		}
		else {		
			this.writeObject(
				new WrapperModel(new StatisticModel(stationCount, clientCount))
			);
		}
	}
	
	public void updateData(ArrayList<DataModel> models) {
		
	}
	
	synchronized private void writeObject(WrapperModel model) {
		try {
			this.out.writeObject(model);
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void processInput(DataModel model) {
		this.type = model.getType();
		this.zipCode = model.getZipCode();
		this.map = model.isMap();
	}
	
	public String getZipCode() {
		return this.zipCode;
	}
	
	public int getType() {
		return this.type;
	}
	
	public boolean isMap() {
		return this.map;
	}
}
