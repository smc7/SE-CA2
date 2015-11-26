package ir.semicolon;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONObject;

public class BatchServer extends Server{
	
	static String jsonFileName = "core.json";
	static FileReader jsonCore;
	static JSONArray jsonsArray;

	BatchServer() {
		
		allDeposits =  new HashMap<String, Deposit>();
		
		try {
			jsonCore = new FileReader(jsonFileName);
		} catch (FileNotFoundException e) {
			System.out.println("no file found with this name");
		}
		
	}
	
	public static String convertFileToString(FileReader fileReader){
		
		BufferedReader fileBuffer = new BufferedReader(fileReader);
		StringBuilder stringedJsonFile = new StringBuilder(); 
		
		String line = null;
		try {
			line = fileBuffer.readLine();
		} catch (IOException e) {
			System.out.println("Error in reading first json line");
		}
		
		while(line != null){
			stringedJsonFile.append(line);
			try {
				line = fileBuffer.readLine();
			} catch (IOException e) {
				System.out.println("failed to read file lines");
			}
		}
		
		try {
			fileBuffer.close();
		} catch (IOException e) {
			System.out.println("file buffer couldn't be closed");
		}
		
		return stringedJsonFile.toString();
	}
	
	public static void buildJsonObjects(){
		
		String coreJsonString = convertFileToString(jsonCore);
		JSONObject jsonObj = new JSONObject(coreJsonString);
		
		port = jsonObj.getInt("port");
		logName = jsonObj.getString("outLog");
		
		jsonsArray = jsonObj.getJSONArray("deposits");

		for (int i = 0; i < jsonsArray.length(); i++) {
			
			String mapId = jsonsArray.getJSONObject(i).getString("id");
			Deposit deposit = buildDeposit(jsonsArray.getJSONObject(i));
			System.out.println(deposit.toString());
			allDeposits.put(mapId, deposit);
			
		}
	}
	
	public static Deposit buildDeposit(JSONObject jobj){
		
		String cuName = jobj.getString("customer");
		String id = jobj.getString("id");
		String initBalance = jobj.getString("initialBalance");
		String upBound = jobj.getString("upperBound");
		
		return (new Deposit(cuName, id, initBalance, upBound));
	}
	
	public static void writeUpdatedJsons(){
		
		FileWriter jsonFileWr = null;
		try {
			jsonFileWr = new FileWriter(jsonFileName);
		} catch (IOException e) {
			logger.error("no file found to update");
		}
		
		try {
			jsonFileWr.write("");
		} catch (IOException e) {
			logger.error("can't write to file");
		}
		
		updateJsonArray();

		try {
			jsonFileWr.append("{\n\t\"port\": " + Integer.toString(port) + ",\n\t\"deposits\":");
			jsonFileWr.append(jsonsArray.toString(4));
			jsonFileWr.append(",\n \"outLog\": \"" + logName + "\" \n}");
		} catch (IOException e) {
			logger.error("couldn't write back JSONs to file");
		}
		
		try {
			jsonFileWr.flush();
			jsonFileWr.close();
		} catch (IOException e) {
			logger.error("unable to close file!");
		}
		
		logger.info("json.core Updated Successfully");
		
	}
	
	public static void updateJsonArray(){
		
		DecimalFormat df = new DecimalFormat("#,###");
		
		for (int i = 0; i < jsonsArray.length(); i++) {
			
			String mapId = jsonsArray.getJSONObject(i).getString("id");
			BigDecimal amount = allDeposits.get(mapId).initialBalance;
			
			String newBalance = df.format(amount);

			jsonsArray.getJSONObject(i).remove("initialBalance");
			jsonsArray.getJSONObject(i).put("initialBalance", newBalance);
			
		}
		
	}
	
}
