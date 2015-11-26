package ir.semicolon;

public class App 
{
    public static void main( String[] args )
    {
        
		Server.initBatchServer();
		
		Server.batchServer.buildJsonObjects();
		
		Server.setServerIp();
		
		Server.setLoggingDetails();

		Server.startConsole();
		
		Server.startInteractionWithClients();
    }
}