package ir.semicolon;

import java.io.Serializable;
import java.math.BigDecimal;

public class Transaction implements Serializable {
	
    private static final long serialVersionUID = 3737L;
    
	String id;
	String type;
	BigDecimal amount;
	String depositNumber;
	
	public String toString(){
		
		String stringed = "DepositNumber: " + depositNumber + ", id: " + id + ", type: " + type + ", amount: " + amount.toString();  
		return stringed;
		
	}
	
}