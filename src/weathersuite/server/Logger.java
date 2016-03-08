package weathersuite.server;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Logger {
	private static String LOG_FILE = "server.log";
	
	public static ArrayList<String> read() {
		BufferedReader in;
		ArrayList<String> lines = new ArrayList<String>();
		try {
			in = new BufferedReader(new FileReader(LOG_FILE));
			String line;
			
			while ((line = in.readLine()) != null && line.length() > 0) {
				lines.add(line);
			}
		}
		catch(FileNotFoundException e) {
			log("Created logfile");
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		
		return lines;
	}
	
	public static void log(String line) {
		File file = new File(LOG_FILE);
		ArrayList<String> lines = null;
		
		if (file.exists() && !file.isDirectory()) {
			lines = read();
		}
		else {
			lines = new ArrayList<String>();
		}
		
		try {
			SimpleDateFormat date = new SimpleDateFormat("[YYY-MM-DD HH:mm:ss] ");
			PrintWriter pw = new PrintWriter(LOG_FILE);
			
			for (String lineX : lines) {
				pw.println(lineX);
			}
			
			pw.println(date.format(new Date()) + line);
			pw.flush();
			pw.close();
		} 
		catch (IOException e) {
			System.err.println("Error while writing log: \n");
			e.printStackTrace();
		}
	}
}
