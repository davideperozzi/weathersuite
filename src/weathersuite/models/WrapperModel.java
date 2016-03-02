package weathersuite.models;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Wrapper class for sending objects through 
 * the sockets without invalid headers
 */
public class WrapperModel implements Serializable
{
	private static final long serialVersionUID = 1L;
	public static final int TYPE_DATA_MODELS = 1;
	public static final int TYPE_DATA_MODEL = 2;
	public static final int TYPE_STATISTIC_MODEL = 3;
	
	public ArrayList<DataModel> dataModels;
	public StatisticModel statisticModel;
	public DataModel dataModel;
	public int type = 0;
	
	public WrapperModel(ArrayList<DataModel> models) {
		this.dataModels = models;
		this.type = TYPE_DATA_MODELS;
	}
	
	public WrapperModel(StatisticModel model) {
		this.statisticModel = model;
		this.type = TYPE_STATISTIC_MODEL;
	}
	
	public WrapperModel(DataModel model) {
		this.dataModel = model;
		this.type = TYPE_DATA_MODEL;
	}
}
