package com.etherscan;

import java.io.IOException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.codehaus.jackson.map.ObjectMapper;


/**
 *
 * @author Jose Luis Estevez Prieto
 * jose.estevez.prieto@gmail.com
 * http://www.joseluisestevez.com.ve/
 * 
 */
public class EtherscanService {

    private final static String ETHERSCAN_APIKEY = "CE2C6JM9F2RYHZMXEQNI8E5KN4TASPHG2HY";
    private final static String ENDPOINT_TRANSACTIONS_LIST = "/api?module=account&action=txlist";
    private final static String API_RESTFUL_URI = "http://rinkeby.etherscan.io";

    public static Transactions getTransactionsByAddress(String address) {
        String endpoint = API_RESTFUL_URI + ENDPOINT_TRANSACTIONS_LIST;
        
        Client client = ClientBuilder.newClient();
        Response response = client.target(endpoint)
                .queryParam("address", address)
                .queryParam("startblock", "0")
                .queryParam("endblock", "99999999")
                .queryParam("sort", "asc")
                .queryParam("apikey", ETHERSCAN_APIKEY)
                .request(MediaType.APPLICATION_JSON)
                .get();
        
        String json = response.readEntity(String.class);
        
        client.close();
        ObjectMapper mapper = new ObjectMapper();
        Transactions transactions = null;
        if (response.getStatus() == 200) {
            try {
                transactions = mapper.readValue(json, Transactions.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return transactions;
    }

}
