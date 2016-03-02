package weathersuite.models;

import java.io.*;

public class DataModel implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	public static final int TYPE_STATUS = 1;
	public static final int TYPE_TEMPERATURE = 2;
	public static final int TYPE_WIND= 3;
	
	private String zipCode;
	private int type;
	private String value;
	private boolean map;
	
	public DataModel(String zipCode, int type, String value) {
		this.type = type;
		this.zipCode = zipCode;
		this.value = value;
	}
	
	public DataModel(String zipCode, String type, String value) {
		this.setType(type);
		this.zipCode = zipCode;
		this.value = value;
	}
	
	public DataModel(String zipCode, String type, boolean map) {
		this.setType(type);
		this.zipCode = zipCode;
		this.map = map;
	}
	
	protected void setType(String type) {
		this.type = DataModel.parseType(type);
	}
	
	public void setValue(String value) {
		this.value = value;
	}
	
	public String getZipCode() {
		return zipCode;
	}

	public int getType() {
		return type;
	}

	public String getValue() {
		return value;
	}
	
	public boolean isMap() {
		return this.map;
	}
	
	public static int parseType(String type) {
		int retType = 0;
		
		switch (type.toLowerCase()) {
			case "t":
			case "temperatur":
			case "temperature:":
				retType = TYPE_TEMPERATURE;
				break;
			
			case "w":
			case "wind":
				retType = TYPE_WIND;
				break;
				
			case "s":
			case "status":
				retType = TYPE_STATUS;
				break;
		}
		
		return retType;
	}
}
