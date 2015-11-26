package ir.semicolon;

import java.math.BigDecimal;

public class Deposit {
	
	String CustomerName;
	long id;
	BigDecimal initialBalance;
	BigDecimal upperBound;
	
	Deposit(String cuName, String idi, String initBalance, String upBound){
		
		initBalance = initBalance.replaceAll(",", "");
		upBound = upBound.replaceAll(",", "");
		
		CustomerName = cuName;
		id = Integer.parseInt(idi);
		initialBalance = new BigDecimal(initBalance);
		upperBound = new BigDecimal(upBound);
		
	}
	
	public String toString(){
		
		String stringed = "Customer: " + CustomerName + ", id: " + Long.toString(id) + ", initBalance: " + initialBalance.toString() + ", upBound: " + upperBound.toString();
		return stringed;
		
	}
	
}
