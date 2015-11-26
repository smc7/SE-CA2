package ir.semicolon;

import java.io.Serializable;
import java.math.BigDecimal;

public class Transaction implements Serializable{
	
	private static final long serialVersionUID = 3737L;
	
	String id;
	String type;
	BigDecimal amount;
	String depositNumber;
	
	public Transaction(String tid, String ttype, String tamount, String tdepNum){
		
		tamount = tamount.replaceAll(",", "");
		
		id = tid;
		type = ttype;
		amount = new BigDecimal(tamount);
		depositNumber = tdepNum;
	
	}
	
	public String toString(){

		String stringed = "DepositNumber: " + depositNumber + ", id: " + id + ", type: " + type + ", amount: " + amount.toString();  
		return stringed;
		
	}
	
	static boolean checkTypes(String tid, String ttype, String tamount, String tdepNum){
		
		tamount = tamount.replaceAll(",", "");
		try{
			Long.parseLong(tdepNum);
			Long.parseLong(tid);
		}catch(NumberFormatException nfe){
			return false;
		}
		
		try{
			new BigDecimal(tamount);
		}catch(NumberFormatException nfe){
			return false;
		}
			
		return true;
	}
	
}
