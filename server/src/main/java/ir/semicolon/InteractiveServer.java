package ir.semicolon;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.math.BigDecimal;
import java.net.Socket;

public class InteractiveServer extends Server implements Runnable{
	
	Socket clientSocket;
	static String theTerminalId;
	static String theTerminalType;
	
	static DataOutputStream informationSender;
	static DataInputStream informationReciever;
	
	InteractiveServer(Socket cliSocket) {
		
		clientSocket = cliSocket;
		
		try {
			informationReciever = new DataInputStream(clientSocket.getInputStream());
		} catch (IOException e2) {
			logger.error("failed to build information sender");
		}
		
		try {
			informationSender = new DataOutputStream(clientSocket.getOutputStream());
		} catch (IOException e2) {
			logger.error("failed to build information sender");
		}
	}

	public void run(){
		
		System.out.println("New terminal got Connected..");
		logger.info("New Terminal got connected..");
		
		int transactionsCount = 0;
		try {
			theTerminalId = informationReciever.readUTF();
			theTerminalType = informationReciever.readUTF();
			transactionsCount = informationReciever.readInt();
			
			logger.info("The Terminal ID: " + theTerminalId + ", and it's Type: " + theTerminalType);
		} catch (IOException e1) {
			logger.error("unable to read Transaction list length");
		}
		
		ObjectInputStream  objectReader = null;
		try {
			objectReader = new ObjectInputStream(clientSocket.getInputStream());
		} catch (IOException e) {
			logger.error("failed to build object reader");
		}
		
		for (int i = 0; i < transactionsCount; i++) {
			
			Transaction curTransaction = null;
			try {
				
				curTransaction = (Transaction) objectReader.readObject();
				
			} catch (ClassNotFoundException e) {
				logger.error("Object not of Transaction Type");
			} catch (IOException e) {
				logger.error("couldn't read object");
			}
			
			synchronized (allDeposits.get(curTransaction.depositNumber).initialBalance) {
				
				String transactionResultMessage = doTransaction(curTransaction);
				sendResultToClient(transactionResultMessage);
			}
		}
		
		logger.info("Done! The Terminal ID: " + theTerminalId + "just left.");
	}
	
	static String doTransaction(Transaction T){
		
		String depositID = T.depositNumber;
		Deposit dep = allDeposits.get(depositID);
		String transactionResult = null;
		
		if(dep == null){
			System.out.println("No such DepositID in Transaction Number: " + T.id);
			logger.error("Terminal" + theTerminalId + ": Transaction:" + T.id + "failed!" + " : No such DepositID");
			
			transactionResult = "No such Deposit ID";
			return transactionResult;
		}
		
		logger.info("Terminal" + theTerminalId + ": Transaction:" + T.id + " initBalance: " + allDeposits.get(depositID).initialBalance);
		logger.info("Terminal" + theTerminalId + ": " + T);
		
		if("deposit".equals(T.type)){
			
			if(dep.initialBalance.add(T.amount).compareTo(dep.upperBound) != 1){
				allDeposits.get(depositID).initialBalance = dep.initialBalance.add(T.amount);
				
				logger.info("Terminal" + theTerminalId + ": Transaction:" + T.id + " Succeed!");
				transactionResult = "Succeeded";
			}
			else{
				System.out.println("this Amount is more than Upper bound for transaction Number: " + T.id + " Terminal: " + theTerminalId);
				logger.error("Terminal" + theTerminalId + ": Transaction:" + T.id + " failed!" + " : Upper Bound Exeeded");
				transactionResult = "Upper Bound Exeeded";
			}
			
		}else if("withdraw".equals(T.type)){
			
			if(dep.initialBalance.subtract(T.amount).compareTo(new BigDecimal(0)) != -1){
				allDeposits.get(depositID).initialBalance = dep.initialBalance.subtract(T.amount);
				
				logger.info("Terminal" + theTerminalId + ": Transaction:" + T.id + " Succeed!");
				transactionResult = "Succeeded";
			}
			else{
				System.out.println("the balance will be less than 0 for transaction Number: " + T.id + " Terminal: " + theTerminalId);
				logger.error("Terminal" + theTerminalId + ": Transaction:" + T.id + " failed!" + " : Balance Not Enough");
				transactionResult = "Balance Not Enough";
			}
		}
		
		logger.info("Terminal" + theTerminalId + ": Transaction:" + T.id + " initBalance: " + allDeposits.get(depositID).initialBalance);
		return transactionResult;
		
	}
	
	static void sendResultToClient(String transactionResultMessage){
		
		if("Succeeded".equals(transactionResultMessage)){
			
			try {
				informationSender.writeUTF(transactionResultMessage);
			} catch (IOException e) {
				logger.error("couldn't send transaction result to client");
			}
			
		}else{
			
			try {
				informationSender.writeUTF("Failed : " + transactionResultMessage);
			} catch (IOException e) {
				logger.error("couldn't send transaction result to client");
			}
			
		}
		
	}

}
