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
	
	public ArrayList<DataModel> getData(String zipCode, String type) {
		return this.getData(zipCode, DataModel.parseType(type));
	}
	
	public ArrayList<DataModel> getData(String zipCode, int type) {
		ArrayList<DataModel> models = new ArrayList<DataModel>();
		
		for (DataModel model : this.data) {
			if ((model.getZipCode().matches(zipCode) || model.getZipCode().startsWith(zipCode)) && model.getType() == type) {
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
		boolean found = false;
		DataModel retModel = null;
		boolean wildcard = zipCode.endsWith("*");
		
		System.out.println("Set " + zipCode);
		
		// Update data with same zipcode and type
		for (DataModel model : this.data) {
			boolean typeMatch = model.getType() == DataModel.parseType(type);
			
			if (wildcard) {
				if (model.getZipCode().startsWith(zipCode.substring(0, 1)) && !model.getZipCode().endsWith("*") && typeMatch) {
					model.setValue(value);
				}
			}
			
			if (model.getZipCode().equals(zipCode) && typeMatch) {
				model.setValue(value);
				retModel = model;
				found = true;
				
				// Exit loop if no wildcard is active
				// to ensure that all data related to 
				// the wildcard will be updated
				if (!wildcard) {
					break;
				}
			}
		}
		
		// Add new data model
		if ( ! found) {
			retModel = new DataModel(zipCode, type, value);
			this.data.add(retModel);
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
		
		if ( ! zipCode.isEmpty() && ! type.isEmpty() && ! value.isEmpty()) {
			model = this.setDataByZipCode(zipCode, type, value);
		}
		else {
			System.err.println("Data not complete!");
		}
		
		return model;
	}
}
