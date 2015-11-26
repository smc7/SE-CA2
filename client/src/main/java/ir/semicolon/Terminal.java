package ir.semicolon;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Appender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class Terminal {
	
	static String terminalId;
	static String terminalType;
	static InetAddress serverIP;
	static int serverPort;
	
	String inputFileName = "termainl.xml";
	static File inputFile;
	
	static ArrayList<Transaction> allTransactions;
	static Socket terminalSocket;
	
	static String logFileName;
	static final Logger logger = Logger.getLogger(Terminal.class);
	static String responsesXmlName = "response.xml";
	
	static DataOutputStream informationSender;
	static DataInputStream informationReciever;
	static ObjectOutputStream transactionSender;
	
	static HashMap<String, Boolean> transactionResults;
	
	Terminal(){
		allTransactions = new ArrayList<Transaction>();
    	inputFile = new File("terminal.xml");
    	
    	transactionResults = new HashMap<String, Boolean>();
	}
	
	static void setLoggingDetails(){
		
		File logFile = new File(logFileName);
		if(!(logFile.isFile()))
			try {
				logFile.createNewFile();
			} catch (IOException e1) {
				System.out.println("couldn't build logFile");
			}
		
		Layout l1 = new PatternLayout("%d %-5p [%M] %m%n");
		
		Appender logAppender = null;
		try {
			logAppender = new FileAppender(l1, logFileName, true);
		} catch (IOException e) {
			logger.error("couldn't create Log Appender, So NO Beautiful Log!!");
		}
		logger.addAppender(logAppender);
		
		logger.info("Terminal ID: " + terminalId + ",  Terminal Type: " + terminalType);
		logger.info("Log File created, Logging Started");
		
	}
	
	
	public static void buildObjectsFromXml(){
        
    	DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
    	DocumentBuilder docBuilder = null;
		try {
			docBuilder = builderFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			System.out.println("Couldn't get the DocBuilder");
		}
    	
    	Document doc = null;
		try {
			doc = docBuilder.parse(inputFile);
		} catch (SAXException e) {
			System.out.println("File not Parsable, formating problem");
		} catch (IOException e) {
			System.out.println("couldn't read file to parse");
		}
    	doc.getDocumentElement().normalize();
    	
    	Node root = doc.getDocumentElement();
    	extractTerminalFields(root.getAttributes());
    	    	
    	NodeList xmlNodeList = root.getChildNodes();
    	
    	for (int i = 0; i < xmlNodeList.getLength(); i++) {
    		
    		if(xmlNodeList.item(i).getNodeType() == Node.ELEMENT_NODE){
    				
	    		if(xmlNodeList.item(i).getNodeName() == "server"){
	    			extractServerFields(xmlNodeList.item(i).getAttributes());
	    		}
	    		
	    		if(xmlNodeList.item(i).getNodeName() == "outLog"){
	    			logFileName = xmlNodeList.item(i).getAttributes().getNamedItem("path").getNodeValue();
	    			setLoggingDetails();
	    		}
	    		
				if(xmlNodeList.item(i).getNodeName() == "transactions"){
					
					NodeList nl2 = xmlNodeList.item(i).getChildNodes();
					for (int j = 0; j < nl2.getLength(); j++) {
						if(nl2.item(j).getNodeType() == Node.ELEMENT_NODE){
							
							NamedNodeMap nodeAttributes = nl2.item(j).getAttributes();
							
							Transaction current = buildTransaction(nodeAttributes);
							if(current != null){
								allTransactions.add(current);
								logger.info("Transaction: " + current.toString() );
							}else{
								logger.error("Problematic Transaction Found");
							}
								
						}
					}
				}
    		}
    	}
    	
    	for (int i = 0; i < allTransactions.size(); i++) {
			System.out.println(allTransactions.get(i).toString());
		}
    	
    }
	
	public static Transaction buildTransaction(NamedNodeMap xmlNode){
		
		ArrayList<String> nodeFields = extractTransactionFields(xmlNode);
		
		boolean correctTypes = Transaction.checkTypes(nodeFields.get(0), nodeFields.get(1), nodeFields.get(2), nodeFields.get(3));
		if(correctTypes){
			Transaction currentTransaction = new Transaction(nodeFields.get(0), nodeFields.get(1), nodeFields.get(2), nodeFields.get(3));
			return currentTransaction;
		}
		else
			return null;
	}
	
	
	public static ArrayList<String> extractTransactionFields(NamedNodeMap xmlNode){
		
		ArrayList<String> nodeFields = new ArrayList<String>();
		
		nodeFields.add(xmlNode.getNamedItem("id").getNodeValue());
		nodeFields.add(xmlNode.getNamedItem("type").getNodeValue());
		nodeFields.add(xmlNode.getNamedItem("amount").getNodeValue());
		nodeFields.add(xmlNode.getNamedItem("deposit").getNodeValue());
		
		return nodeFields;
	}
	
	
	public static void extractServerFields(NamedNodeMap xmlNode){
		
		String sIp = xmlNode.getNamedItem("ip").getNodeValue();
		try {
			serverIP = InetAddress.getByName(sIp);
		} catch (UnknownHostException e) {
			logger.error("Invalid IP is given in File");
		}
		String sPort = xmlNode.getNamedItem("port").getNodeValue();
		serverPort = Integer.parseInt(sPort);

	}
	
	
	public static void extractTerminalFields(NamedNodeMap xmlNode){
		
    	terminalId = xmlNode.getNamedItem("id").getNodeValue();
    	terminalType = xmlNode.getNamedItem("type").getNodeValue();
    	System.out.println(terminalId);
    	System.out.println(terminalType);
    	
	}

	
	public static void getConnected(){
		
		try{
		terminalSocket = new Socket(serverIP,serverPort);
		}catch(Exception e){
			logger.error("couldn't get connected to server");
		}
		
		try {
			informationSender = new DataOutputStream(terminalSocket.getOutputStream());
		} catch (IOException e2) {
			logger.error("couldn't build information sender stream");
		}
		
		int transactionsCount = allTransactions.size();
		try {
			informationSender.writeUTF(terminalId);
			informationSender.writeUTF(terminalType);
			informationSender.writeInt(transactionsCount);
		} catch (IOException e1) {
			logger.error("unable to send transaction list length");
		}
		
		try {
			informationReciever = new DataInputStream(terminalSocket.getInputStream());
		} catch (IOException e2) {
			logger.error("couldn't build information sender stream");
		}
		
	}

	
	public static void sendTransactionsAndGetResponse(){
		
		try {
			transactionSender = new ObjectOutputStream(terminalSocket.getOutputStream());
		} catch (IOException e) {
			logger.error("couldn't write transaction object to socket");
		}
		
		for (int i = 0; i < allTransactions.size(); i++) {
			
			try {
				transactionSender.writeObject(allTransactions.get(i));
			} catch (IOException e) {
				logger.error("failed to write Transaction");
			}
			
			String response = null;
			try {
				response = informationReciever.readUTF();
				
				logger.info("Transaction:" + allTransactions.get(i).id + " " + response);
			} catch (IOException e) {
				logger.error("couldn't recieve response");
			}
			addResultToList(allTransactions.get(i), response);
			
		}
	}
	
	private static void addResultToList(Transaction transaction, String response) {
		
		if(response.contains("Succeeded")){
			transactionResults.put(transaction.id, true);
		}else{
			transactionResults.put(transaction.id, false);
		}
		
	}

	
	static void buildXmlFromResponses(){
		
		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
    	DocumentBuilder docBuilder = null;
		try {
			docBuilder = builderFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			System.out.println("Couldn't get the DocBuilder");
		}
		
		Document logDoc = docBuilder.newDocument();
		
		Element xmlRoot = logDoc.createElement("TransactionsReults");
		logDoc.appendChild(xmlRoot);
		
		for (int i = 0; i < transactionResults.size(); i++) {
			
			Element requestResponse = logDoc.createElement("requsetResponse");
			
			requestResponse.setAttribute("TransactionId", allTransactions.get(i).id);
			requestResponse.setAttribute("Success", transactionResults.get(allTransactions.get(i).id).toString());
			
			xmlRoot.appendChild(requestResponse);
			
		}
		
		Transformer transformer = null;
		try {
			transformer = TransformerFactory.newInstance().newTransformer();
		} catch (TransformerConfigurationException e) {
			logger.error("couldn't build DOM to File transformer");
		}
		DOMSource source = new DOMSource(logDoc);
		File outXmlFile = new File(responsesXmlName);
		if(!(outXmlFile.isFile())){
			try {
				outXmlFile.createNewFile();
			} catch (IOException e) {
				logger.error("XML log not built!");
			}
		}
		StreamResult result = new StreamResult(outXmlFile);
		
		try {
			transformer.setOutputProperty(OutputKeys.INDENT,"yes");
			transformer.transform(source, result);
		} catch (TransformerException e) {
			logger.error("problem in writing to XML");
		}
		
	}
	
}
