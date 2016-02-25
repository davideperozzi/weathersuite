package weathersuite.models;

import java.io.Serializable;

public class StatisticModel implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	public int clientCount = 0;
	public int stationCount = 0;
	
	public StatisticModel(int clientCount, int stationCount) {
		this.clientCount = clientCount;
		this.stationCount = stationCount;
	}
}
