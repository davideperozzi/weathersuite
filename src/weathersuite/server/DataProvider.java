package weathersuite.server;

import java.io.*;
import java.util.*;

import weathersuite.models.DataModel;

public class DataProvider 
{
	private String file;
	private ArrayList<DataModel> data = new ArrayList<DataModel>();
	
	public DataProvider(String file) {
		this.file = file;
		
		try {
			this.load();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}	
	}
	
	@SuppressWarnings("unchecked")
	private void load() throws IOException {
		try {
			ObjectInputStream input = new ObjectInputStream(new FileInputStream(this.file));
			
			if (input != null) {
				try {
					this.data = (ArrayList<DataModel>)input.readObject();
				}
				catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			}
			
			input.close();
		}
		catch(FileNotFoundException e) {
			
		}
	}
	
	public ArrayList<DataModel> getData() {
		return this.data;
	}
	
	synchronized public void save() throws IOException {
		try {
			ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(this.file));
			output.writeObject(this.data);
			output.close();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	synchronized public void setDataByZipCode(String zipCode, String type, String value) {
		boolean found = false;
		
		// Update data with same zipcode and type
		for (DataModel model : this.data) {
			if (model.getZipCode().equals(zipCode) && model.getType().equals(type)) {
				model.setValue(value);
				found = true;
				break;
			}
		}
		
		// Add new data model
		if ( ! found) {
			this.data.add(new DataModel(zipCode, type, value));
		}
	}
	
	synchronized public void parseData(String data) {
		String[] parts = data.split(":");
		
		String zipCode = ""; 
		String type = "";
		String value = "";
		
		for (int i = 0; i < parts.length; i++) {
			switch (i) {
				case 0:
					zipCode = parts[i].trim();
				break;
					
				case 1: 
					type = parts[i].trim();
				break;
					
				case 2:
					value = parts[i].trim();
				break;
			}
		}
		
		if ( ! zipCode.isEmpty() && ! type.isEmpty() && ! value.isEmpty()) {
			this.setDataByZipCode(zipCode, type, value);
		}
		else {
			System.err.println("Data not complete!");
		}
	}
}
