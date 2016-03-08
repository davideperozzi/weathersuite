import weathersuite.*;

public class WeatherSuite 
{
	public static void showHelp() {
		System.out.println("Usage: java -jar weathersuite.jar [MODE] [ARG1] [ARG2] [...] \n\n"
				+ "client: [ARG1:host] [ARG2:port] \n"
				+ "server: [ARG1:station-port] [ARG2:client-port] \n\n"
				+ "Example running a server: \n"
				+ "weathersuite server 5555 5556 \n\n"
				+ "Example starting a client: \n"
				+ "weathersuite client localhost 5556"
		);
	}

	public static void main(String[] args) {
		if (args.length >= 1) {
			// Detect mode (server or client)
			String mode = args[0].trim();
			
			if (!mode.equals("client") && !mode.equals("server")) {
				System.err.println("Please decide which program you want to run.");
			}
			else {
				// Start a program if the mode was found
				if (mode.equals("server")) {
					int stationPort = 0;
					int clientPort = 0;
					
					// Get the station and client port
					if (args.length >= 2) {
						if (args.length >= 2 && args[1] != null) {
							stationPort = Integer.parseInt(args[1]); 
						}
						
						if (args.length >= 3 && args[2] != null) {
							clientPort = Integer.parseInt(args[2]);
						}
					}
					else if (args.length < 1) {
						System.err.println("Server needs at least 2 arguments");
					}
					
					// Check for valid ports
					if (stationPort <= 0 || clientPort <= 0) {
						System.err.println("The given ports are invalid");
					}
					else {
						(new Server(stationPort, clientPort)).run();
					}
				}
				else if (mode.equals("client")) {
					String host = "";
					int serverPort = 0; 
					
					if (args.length >= 2) {
						if (args.length >= 2 && args[1] != null) {
							host = args[1]; 
						}
						
						if (args.length >= 3 && args[2] != null) {
							serverPort = Integer.parseInt(args[2]);
						}
					}
					else if (args.length < 1) {
						System.err.println("Client needs 2 arguments");
					}
					
					if (host.isEmpty()) {
						System.err.println("A host needs to be provided");
					}
					else if (serverPort <= 0) {
						System.err.println("The given server port is invalid");
					}
					else {
						(new Client(host, serverPort)).connect();
					}
				}
			}
		}
		else {
			showHelp();
		}
	}
}
