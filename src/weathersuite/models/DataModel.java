package weathersuite.models;

import java.io.*;

public class DataModel implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	private String zipCode;
	private String type;
	private String value;
	
	public DataModel(String zipCode, String type, String value) {
		this.zipCode = zipCode;
		this.type = type;
		this.value = value;
	}

	public void setZipCode(String zipCode) {
		this.zipCode = zipCode;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
	public String getZipCode() {
		return zipCode;
	}

	public String getType() {
		return type;
	}

	public String getValue() {
		return value;
	}
}
