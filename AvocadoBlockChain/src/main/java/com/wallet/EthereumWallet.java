package com.wallet;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;

import org.json.JSONObject;
import org.slf4j.LoggerFactory;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthGasPrice;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.protocol.ipc.UnixIpcService;
import org.web3j.tx.Transfer;
import org.web3j.utils.Convert;

import ch.qos.logback.classic.LoggerContext;
import rx.Subscription;

public class EthereumWallet {

    private final static String IPC_SOCKET_PATH = "/opt/apps/geth.ipc";
    private final static String URL_JSON_RPC = "http://127.0.0.1:8545";
    private final static String WALLET_PATH = "C:/Users/Kbubs DELL/Desktop/Avocado/Demo";
    private final static boolean USING_IPC = false;
    private final static boolean DEBUG = false;
    private static final String WALLET_PASSWORD = "123456";
    static HashMap<String, String> addressMap = new HashMap<>();

    public static Web3j web3j;

    public static Subscription subscription;

    private static EthereumWallet ethereumWallet;

    private EthereumWallet() {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        loggerContext.stop();
        init();
    }

    public static EthereumWallet getInstance() {
        if (ethereumWallet == null) {
            ethereumWallet = new EthereumWallet();
        }
        return ethereumWallet;
    }

    // Metodo inicial donde creamos el entorno de ejecuacion de nuestro monedero
    private void init() {
        if (USING_IPC) {
            if (DEBUG) { // IPC Logging
                System.setProperty("org.apache.commons.logging.simplelog.log.org.web3j.protocol.ipc", "DEBUG");
            }
            web3j = Web3j.build(new UnixIpcService(IPC_SOCKET_PATH));
        } else {
            if (DEBUG) { // HTTP Logging
                System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");
                System.setProperty("org.apache.commons.logging.simplelog.showdatetime", "true");
                System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http.wire", "DEBUG");
                System.setProperty("org.apache.commons.logging.simplelog.log.org.web3j.protocol.ipc", "DEBUG");
            }
            web3j = Web3j.build(new HttpService("https://mainnet.infura.io/v3/a06d5575499f48a5976182b4c6285ab5"));
            //web3j = Web3j.build(new HttpService());
        }
        
        
        //Commentado por que infura no da soporte a metodos de filtro
        /*subscription = web3j.blockObservable(false).subscribe(block -> {
            blockObservable(block);
        }, Throwable::printStackTrace, () -> System.out.println("block done"));
        
        
        web3j.transactionObservable().subscribe(tx -> {
            transactionObservable(tx);
        }, Throwable::printStackTrace, () -> System.out.println("tx done"));

      
        
        web3j.pendingTransactionObservable().subscribe(tx -> {
            pendingTransactionObservable(tx);
        }, Throwable::printStackTrace, () -> System.out.println("ptx done"));*/


    }

    // Cerrar la conexion de los eventos
    public void close() {
        try {
            if (subscription != null) {
                subscription.unsubscribe();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    //--------------- Eventos del Monedero
    private void blockObservable(EthBlock block) {
        // Este es un ejemplo del envento blockObservable, se comenta por que no lo vamos a usar en la billetera
        System.out.println("blockObservable: " + block.getBlock().getNumber());
        EthBlock.Block ethBlock = block.getBlock();
        LocalDateTime timestamp = Instant.ofEpochSecond(
                ethBlock.getTimestamp().longValueExact()).atZone(ZoneId.of("UTC")).toLocalDateTime();
        int transactionCount = ethBlock.getTransactions().size();
        String hash = ethBlock.getHash();
        String parentHash = ethBlock.getParentHash();
        System.out.println(
                timestamp + " "
                + "Number: " + ethBlock.getNumber() + ", "
                + "Tx count: " + transactionCount + ", "
                + "Hash: " + hash + ", "
                + "Parent hash: " + parentHash
        );
    }

    private void transactionObservable(Transaction tx) {
        // Si es de mi monedero capturo el evento
        if (addressMap.containsKey(tx.getTo()) || addressMap.containsKey(tx.getFrom())) {
            onTransactionReceived(tx);
        }
    }

    private void pendingTransactionObservable(Transaction tx) {
        // Si es de mi monedero capturo el evento
        if (addressMap.containsKey(tx.getTo()) || addressMap.containsKey(tx.getFrom())) {
            onTransactionReceived(tx);
        }
    }

    public void onTransactionReceived(Transaction tx) {
        BigInteger wei = tx.getValue();
        BigDecimal ether = Convert.fromWei(new BigDecimal(tx.getValue()), Convert.Unit.ETHER);
        String to = tx.getTo();
        String hash = tx.getHash();
        String from = tx.getFrom();

        if (addressMap.containsKey(tx.getTo())) {
            // credito
            System.out.println("Se ha recibido su depósito de " + ether + " ether enviado a su cuenta " + to + " enviado por " + from + " tx " + hash);
        } else if (addressMap.containsKey(tx.getFrom())) {
            //debito
            System.out.println("Se ha hecho un retiro de " + ether + " ether desde su cuenta " + to + " hacia la cuenta destino " + from + " tx " + hash);
        }
    }

    /**
     * Este metodo crea una nueva cuenta de ethereum
     *
     * @param password
     * @return String[] Para este ejemplo retornamos el nombre del fichero
     * generado y el JSON del contenido
     * @throws Exception
     */
    public String[] createNewWallet(String password) throws Exception {
        try {
        	
            File file = new File(WALLET_PATH);

            String name = null;
            String json = null;
            if (file.exists()) {
                // al crear el monedero nos retorna el nombre del archivo generado dentro de la carpeta indicada
                name = WalletUtils.generateFullNewWalletFile(password, file);
                // vamos a abrir el monedero y retornar el json generado
                
                Path path = FileSystems.getDefault().getPath(WALLET_PATH, name);
                byte[] b = java.nio.file.Files.readAllBytes(path);
                json = new String(b);
                return new String[]{name, json};
            } else {
                throw new Exception("Invalid WALLET_PATH " + WALLET_PATH);
            }
            
            
        } catch (Exception ex) {
            throw ex;
        }
    }

    /**
     * Abrir una cuenta ethereum
     *
     * @param password
     * @param walletName
     * @return
     * @throws Exception
     */
    public Credentials openWallet(String password, String walletName) throws Exception {
        Credentials credentials = WalletUtils.loadCredentials(password, WALLET_PATH + '/' + walletName);
        return credentials;
    }

    /**
     * Ver el saldo de una cuenta ethereum
     *
     * @param address
     * @return
     * @throws Exception
     */
    public BigInteger getAddressBalance(String address) throws Exception {
        try {
            // Vamos a enviar una solicitud asíncrona usando el objecto web3j 
        	System.out.println(address);
            EthGetBalance ethGetBalance = web3j
                    .ethGetBalance(address, DefaultBlockParameterName.LATEST)
                    .sendAsync()
                    .get();

            // saldo en wei
            BigInteger wei = ethGetBalance.getBalance();
            return wei;
        } catch (Exception ex) {
            throw ex;
        }
    }

    public RemoteCall<TransactionReceipt> sendCoins(Credentials credentials, String toAddress, BigDecimal value) throws Exception {
        try {
            RemoteCall<TransactionReceipt> transactionReceipt = Transfer.sendFunds(web3j, credentials, toAddress, value, Convert.Unit.ETHER);
            //BigInteger gas = transactionReceipt.getGasUsed();
            //String transactionHash1 = transactionReceipt.getTransactionHash();
            return transactionReceipt;
        } catch (Exception ex) {
            throw ex;
        }
    }
    
    public BigInteger getGasPrice() throws IOException {
        EthGasPrice ethGasPrice = web3j.ethGasPrice().send();
        return ethGasPrice.getGasPrice();
    }



    
}
