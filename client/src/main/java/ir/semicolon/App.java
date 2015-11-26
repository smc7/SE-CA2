package ir.semicolon;

public class App 
{
	
	public static void main(String[] args) {
		
		Terminal T = new Terminal();
		
		T.buildObjectsFromXml();
		
		T.getConnected();
		
		T.sendTransactionsAndGetResponse();
		
		T.buildXmlFromResponses();
		
	}

}