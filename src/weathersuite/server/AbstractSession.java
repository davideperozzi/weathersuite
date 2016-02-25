package weathersuite.server;

import java.io.IOException;
import java.net.Socket;
import java.util.UUID;

abstract public class AbstractSession extends Thread
{
	protected Socket socket;
	protected String uid;
	protected SessionInteraction interaction;
		
	public AbstractSession(Socket socket) {
		this.socket = socket;
		this.uid = UUID.randomUUID().toString();
	}
	
	public String getUid() {
		return this.uid;
	}
	
	protected void disposeInternal() {		
		if ( ! this.socket.isClosed()) {
			try {
				this.socket.close();
			} catch (IOException e) {
				System.out.println("Socket could not be closed");
			}
		}
		
		if (this.interaction != null) {
			this.interaction.onDisconnect();
		}
		
		this.interrupt();
	}
	
	public void setInteraction(SessionInteraction interaction) {
		this.interaction = interaction;
	}
	
	public Socket getSocket() {
		return this.socket;
	}
}
