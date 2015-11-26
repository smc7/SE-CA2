package ir.semicolon;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;

import org.apache.log4j.Appender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

public class Server {

	static BatchServer batchServer;
	static ConsoleServer consoleServer;
	static HashMap<String, Deposit> allDeposits;
	
	static InetAddress serverIp;
	static int port;
	static String logName;
	
	static final Logger logger = Logger.getLogger(Server.class);
	
	static void initBatchServer(){
		batchServer = new BatchServer();
	}
	
	static void startConsole(){
		consoleServer = new ConsoleServer();
		Thread T = new Thread(consoleServer);
		T.start();
	}
	
	static void setServerIp(){
		
		try {
			serverIp = InetAddress.getByName("127.0.0.1");
		} catch (UnknownHostException e1) {
			logger.error("Problem with assigning server IP");
		}

	}
	
	static void setLoggingDetails(){
		
		File logFile = new File(logName);
		if(!(logFile.isFile()))
			try {
				logFile.createNewFile();
			} catch (IOException e1) {
				logger.error("couldn't build logFile");
			}
		
		Layout l1 = new PatternLayout("%d %-5p [%c{1}:%M] %m%n");
		
		Appender logAppender = null;
		try {
			logAppender = new FileAppender(l1, logName, true);
		} catch (IOException e) {
			logger.error("couldn't create Log Appender, So NO Beautiful Log!!");
		}
		logger.addAppender(logAppender);
		
	}
	
	static void startInteractionWithClients(){
		ServerSocket masterSocket = null;
		try {
			masterSocket = new ServerSocket(port, 50, serverIp);
			System.out.println("Master Socket is up!");
			logger.info("Now Master Socket is up!");
		} catch (IOException e) {
			logger.error("Couldn't build Server Socket!");
		}
		
		while(true){
			
			Socket clientSocket = null;
			try {
				clientSocket = masterSocket.accept();
			} catch (IOException e) {
				logger.error("Couldn't accept new client!");
			}
			
			Thread serverClientInteractor = new Thread(new InteractiveServer(clientSocket));
			serverClientInteractor.start();
		}
	}
}
