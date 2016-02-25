package weathersuite.server;

import java.io.*;
import java.net.*;
import java.util.ArrayList;

import weathersuite.models.DataModel;

public class ClientSession extends AbstractSession
{
	private String zipCode;
	private String type;
	
	public ClientSession(Socket socket) {	
		super(socket);
	}
	
	public void run() {
		System.out.println("Client connected");
		
		DataModel data = new DataModel("74889", "T", "1");
		ArrayList<DataModel> models = new ArrayList<DataModel>();
		models.add(data);
		
		try {
			ObjectOutputStream out = new ObjectOutputStream(this.socket.getOutputStream());
			out.writeObject(models);
			out.flush();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		// Thread for input 
		(new Thread(){
			public void run(){
				try {
					BufferedReader in = new BufferedReader(
						new InputStreamReader(ClientSession.this.socket.getInputStream())
					);
					
					String line;
					while ((line = in.readLine()) != null && line.length() != 0) {
						ClientSession.this.processInput(line);
					}
				} 
				catch (IOException e) {
					// Ignore the error while reading
					// and just dispose this session
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
			try {
				PrintWriter out = new PrintWriter(this.socket.getOutputStream());
				out.println("<s>" + stationCount + ":" + clientCount);
				out.flush();
			}
			catch (IOException e) {
				System.err.println("Error while updating statistics:");
				e.printStackTrace();
			}
		}
	}
	
	private void processInput(String line) {
		//String commandType = line.substring(0, 3);
			
		/*if (commandType.equals("<u>")) {
			this.interaction.onDataSend(line.substring(3));
		}*/
		
		this.interaction.onDataSend(line);
	}
}
