import weathersuite.*;

public class WeatherSuite 
{
	public static void showHelp() {
		System.out.println(
				"Please use the following arguments to run a program: \n"
				+ "weathersuite mode arg1 arg2 ... \n"
				+ "\t client host port \n"
				+ "\t server station-port client-port [update-interval]\n\n"
				+ "\t E.g. running a server: \n"
				+ "\t weathersuite server 5555 5556 \n\n"
				+ "\t E.g. starting a client: \n"
				+ "\t weathersuite client localhost 5556"
		);
	}

	public static void main(String[] args) {
		if (args.length >= 1) {
			// Detect mode (server or client)
			String mode = args[0];
			
			if (!mode.equals("client") && !mode.equals("server")) {
				System.err.println("Please decide which program you want to run.");
			}
			else {
				// Start a program if the mode was found
				if (mode.equals("server")) {
					int stationPort = 0;
					int clientPort = 0;
					int updateInterval = 0;
					
					// Get the station and client port
					if (args.length >= 2) {
						if (args.length >= 2 && args[1] != null) {
							stationPort = Integer.parseInt(args[1]); 
						}
						
						if (args.length >= 3 && args[2] != null) {
							clientPort = Integer.parseInt(args[2]);
						}
						
						if (args.length >= 4 && args[3] != null) {
							updateInterval = Integer.parseInt(args[3]);
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
						Server server;
						
						if (updateInterval > 0) {
							server = new Server(stationPort, clientPort, updateInterval);
						}
						else {
							server = new Server(stationPort, clientPort);
						}
						
						server.run();
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
						Client client = new Client(host, serverPort);
						client.connect();
					}
				}
			}
		}
		else {
			showHelp();
		}
	}
}
