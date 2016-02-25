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
	private String type;
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
		System.out.println("Client connected");
		
		/*DataModel data = new DataModel("74889", "T", "1");
		ArrayList<DataModel> models = new ArrayList<DataModel>();
		models.add(data);
		
		this.writeObject(new WrapperModel(models));*/
		
		// Thread for input 
		(new Thread(){
			public void run(){
				try {
					ObjectInputStream input = new ObjectInputStream(ClientSession.this.socket.getInputStream());
					
					while (true) {
						WrapperModel wrapper = (WrapperModel)input.readObject();
						
						switch (wrapper.type) {
							case WrapperModel.TYPE_DATA_MODELS:
								ClientSession.this.processInput(wrapper.dataModels);
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
			StatisticModel statistic = new StatisticModel(stationCount, clientCount);
			this.writeObject(new WrapperModel(statistic));
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
	
	private void processInput(ArrayList<DataModel> model) {
		System.out.println("Inpit ");
	}
}
