package weathersuite.server;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Logger 
{
	private static String LOG_FILE = "server.log";
	private static ArrayList<LogListener> listeners = new ArrayList<LogListener>();
	
	synchronized public static ArrayList<String> read() {
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
	
	synchronized public static void log(String line) {
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
			
			line = date.format(new Date()) + line;
			
			pw.println(line);
			pw.flush();
			pw.close();
			
			lines.add(line);
			
			// Call listeners
			for (LogListener listener : listeners) {
				listener.onLog(lines);
			}
		} 
		catch (IOException e) {
			System.err.println("Error while writing log: \n");
			e.printStackTrace();
		}
	}
	
	public static void onLog(LogListener listener) {
		listeners.add(listener);
	}
	
	public static ArrayList<String> getLines() {
		return read();
	}
}
