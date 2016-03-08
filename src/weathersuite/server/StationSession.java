package weathersuite.server;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;

public class StationSession extends AbstractSession
{	
	public StationSession(Socket socket) {			
		super(socket);
	}
	
	public void run() {		
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
			String line;
			
			while ((line = in.readLine()) != null && line.length() != 0) {
				this.processInput(line);
			}
		} 
		catch (IOException e) {
			// Ignore the error while reading
			// and just dispose this session
		}
		finally {
			this.disposeInternal();
		}
	}
	
	private void processInput(String line) {
		String commandType = line.substring(0, 3);
			
		if (commandType.equals("<u>")) {
			this.interaction.onDataSend(line.substring(3));
		}
	}
}
