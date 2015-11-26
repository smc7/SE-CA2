package ir.semicolon;

import java.util.Scanner;

public class ConsoleServer extends Server implements Runnable{

	public void run() {
		Scanner conReader = new Scanner(System.in);
		
		String theWord;
		while(conReader.hasNext()){
			
			System.out.println("Waiting for input:");
			theWord = conReader.next();
			System.out.println(theWord);
			
			if("sync".equals(theWord)){
				System.out.println("Got a sync command!");
				batchServer.writeUpdatedJsons();
			}
		}
	}

}
