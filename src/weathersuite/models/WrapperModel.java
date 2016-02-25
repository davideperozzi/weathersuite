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
	public static final int TYPE_STATISTIC_MODEL = 2;
	
	public ArrayList<DataModel> dataModels;
	public StatisticModel statisticModel;
	public int type = 0;
	
	public WrapperModel(ArrayList<DataModel> models) {
		this.dataModels = models;
		this.type = TYPE_DATA_MODELS;
	}
	
	public WrapperModel(StatisticModel model) {
		this.statisticModel = model;
		this.type = TYPE_STATISTIC_MODEL;
	}
}
