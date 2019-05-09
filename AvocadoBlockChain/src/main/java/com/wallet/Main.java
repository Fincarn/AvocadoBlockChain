package com.wallet;

import java.math.BigDecimal;

import org.json.JSONObject;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.core.RemoteCall;

import com.wallet.*;


public class Main {
    public static void main(String[] args) {
        try {
        	EthereumWallet e = EthereumWallet.getInstance();
        	EthereumWallet e2 = EthereumWallet.getInstance();
        	

        	
        	String[] wallet = e.createNewWallet("123456");
        	String[] wallet2 = e2.createNewWallet("7890123");

        	
        	for (String string : wallet) {
        		System.out.println(string);
        		System.out.println(wallet[1]);
        		
        		Credentials credentials= e.openWallet("7890123",wallet2[0]);
        		System.out.println("Credenciales "+ credentials.getAddress());
        		RemoteCall<TransactionReceipt> transfer;
        		BigDecimal value=BigDecimal.valueOf(100).movePointLeft(2);
        		transfer=e.sendCoins(credentials, credentials.getAddress(), value);
        		
            	String jsonString = wallet[1];
            	JSONObject jsonResult = new JSONObject(jsonString);
            	System.out.println(jsonResult.get("address"));
            	System.out.println("Balance: "+e.getAddressBalance("0x"+jsonResult.get("address").toString()));

            }
        	
        	for (String string : wallet2) {
        		System.out.println(string);
        		System.out.println(wallet2[1]);
        		
        		Credentials credentials= e.openWallet("123456",wallet[0]);
        		System.out.println("Credenciales "+ credentials.getAddress());
        		RemoteCall<TransactionReceipt> transfer;
        		BigDecimal value=BigDecimal.valueOf(100).movePointLeft(2);
        		transfer=e.sendCoins(credentials, credentials.getAddress(), value);
        		
            	String jsonString = wallet2[1];
            	JSONObject jsonResult = new JSONObject(jsonString);
            	System.out.println(jsonResult.get("address"));
            	System.out.println("Balance: "+e.getAddressBalance("0x"+jsonResult.get("address").toString()));

            }
        } catch (Exception ex) {
            System.err.println(ex);
        }

    }
}
