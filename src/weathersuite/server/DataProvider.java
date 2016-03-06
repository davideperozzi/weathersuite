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
	
	public ArrayList<DataModel> getData(String type) {
		return this.getData(DataModel.parseType(type));
	}
	
	public ArrayList<DataModel> getData(int type) {
		ArrayList<DataModel> models = new ArrayList<DataModel>();
		
		for (DataModel model : this.data) {
			if (model.getType() == type) {
				models.add(model);
			}
		}
		
		return models;
	}
	
	public ArrayList<DataModel> getData(String zipCode, String type) {
		return this.getData(zipCode, DataModel.parseType(type));
	}
	
	public ArrayList<DataModel> getData(String zipCode, int type) {
		ArrayList<DataModel> models = new ArrayList<DataModel>();
		boolean wildcard = zipCode.endsWith("*");
		
		for (DataModel model : this.data) {
			if (model.getType() != type) {
				continue;
			}
			
			if (wildcard) {
				if (model.getZipCode().substring(0, 1).equals(zipCode.substring(0, 1))) {
					models.add(model);
				}
			}
			else if (model.getZipCode().equals(zipCode)) {
				models.add(model);
			}
		}
		
		return models;
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
	
	synchronized public DataModel setDataByZipCode(String zipCode, String type, String value) {
		DataModel retModel = null;
		boolean wildcard = zipCode.endsWith("*");
		boolean found = false;
		
		// Update data with same zipcode and type
		for (DataModel model : this.data) {
			boolean typeMatch = model.getType() == DataModel.parseType(type);
			
			if (wildcard) {
				if (model.getZipCode().startsWith(zipCode.substring(0, 1)) && !model.getZipCode().endsWith("*") && typeMatch) {					
					model.setValue(value);
				}
			}
			else if (model.getZipCode().equals(zipCode) && typeMatch) {
				model.setValue(value);
				retModel = model;
				found = true;
				
				break;
			}
		}
		
		// Add new data model
		if ( ! found && ! wildcard) {
			retModel = new DataModel(zipCode, type, value);
			this.data.add(retModel);
		}
		else if (wildcard) {
			// Return sample model to work with
			return new DataModel(zipCode, type, value);
		}
		
		return retModel;
	}
	
	synchronized public DataModel parseData(String data) {
		String[] parts = data.split(":");
		DataModel model = null;
		
		String zipCode = ""; 
		String value = "";
		String type = "";
		
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
		
		boolean error = false;
		
		try {
			Integer.parseInt(zipCode);
		}
		catch(NumberFormatException e) {
			error = true;
		}
		
		if ( ! zipCode.isEmpty() && ! type.isEmpty() && ! value.isEmpty() && ! error) {
			model = this.setDataByZipCode(zipCode, type, value);
		}
		else {
			System.err.println("Data not complete or invalid!");
		}
		
		return model;
	}
}
