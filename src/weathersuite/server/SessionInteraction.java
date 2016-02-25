package weathersuite.server;

public interface SessionInteraction 
{
	public void onDisconnect();
	public void onDataSend(String data);
}
