package com.jc.integralgradle.client;

import com.jc.integralgradle.contract.Asset;
import org.fisco.bcos.sdk.BcosSDK;
import org.fisco.bcos.sdk.abi.datatypes.generated.tuples.generated.Tuple2;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.model.TransactionReceipt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.Properties;

@Component
public class AssetClient implements ApplicationContextAware {

    static Logger logger = LoggerFactory.getLogger(AssetClient.class);
    //本地合约地址
    private static final String CONTRACT_ADDRESS = "0x1d666e17905703c9fada0d4bedcd81f180f9784e";

    private ApplicationContext context;

    private BcosSDK bcosSDK;
    private Client client;
    private CryptoKeyPair cryptoKeyPair;

    public void initialize() {
        ApplicationContext context =
                new ClassPathXmlApplicationContext("classpath:applicationContext.xml");
        bcosSDK = context.getBean(BcosSDK.class);
        client = bcosSDK.getClient(1);
        cryptoKeyPair = client.getCryptoSuite().createKeyPair();
        client.getCryptoSuite().setCryptoKeyPair(cryptoKeyPair);
        logger.debug("create client for group1, account address is " + cryptoKeyPair.getAddress());
    }

    //部署合约
    public void deployAssetAndRecordAddr() {
        try {
            Asset asset = Asset.deploy(client, cryptoKeyPair);
            System.out.println(
                    " deploy Asset success, contract address is " + asset.getContractAddress());

            recordAssetAddr(asset.getContractAddress());
        } catch (Exception e) {
            // TODO Auto-generated catch block
            // e.printStackTrace();
            System.out.println(" deploy Asset contract failed, error message is  " + e.getMessage());
        }
    }

    public void recordAssetAddr(String address) throws FileNotFoundException, IOException {
        Properties prop = new Properties();
        prop.setProperty("address", address);
        final Resource contractResource = new ClassPathResource("contract.properties");
        FileOutputStream fileOutputStream = new FileOutputStream(contractResource.getFile());
        prop.store(fileOutputStream, "contract address");
    }

    public String loadAssetAddr() throws Exception {
        // load Asset contact address from contract.properties
        Properties prop = new Properties();
        final Resource contractResource = new ClassPathResource("contract.properties");
        prop.load(contractResource.getInputStream());

        String contractAddress = prop.getProperty("address");
        if (contractAddress == null || contractAddress.trim().equals("")) {
            throw new Exception(" load Asset contract address failed, please deploy it first. ");
        }
        logger.info(" load Asset address from contract.properties, address is {}", contractAddress);
        return contractAddress;
    }

    public Tuple2<BigInteger, BigInteger> queryAssetAmount(String assetAccount) {
        try {
            Asset asset = Asset.load(CONTRACT_ADDRESS, client, cryptoKeyPair);
            Tuple2<BigInteger, BigInteger> result = asset.select(assetAccount);
            if (result.getValue1().compareTo(new BigInteger("0")) == 0) {
                System.out.printf(" asset account %s, value %s \n", assetAccount, result.getValue2());
                return result;
            } else {
                System.out.printf(" %s asset account is not exist \n", assetAccount);
                return null;
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            logger.error(" queryAssetAmount exception, error message is {}", e.getMessage());
            System.out.printf(" query asset account failed, error message is %s\n", e.getMessage());
        }
        return null;
    }

    public int registerAssetAccount(String assetAccount, BigInteger amount) {
        int i = 0;
        try {
            Asset asset = Asset.load(CONTRACT_ADDRESS, client, cryptoKeyPair);
            TransactionReceipt receipt = asset.register(assetAccount, amount);
            List<Asset.RegisterEventEventResponse> response = asset.getRegisterEventEvents(receipt);
            if (!response.isEmpty()) {
                if (response.get(0).ret.compareTo(new BigInteger("0")) == 0) {
                    System.out.printf(
                            " register asset account success => asset: %s, value: %s \n", assetAccount, amount);
                    i = 1;
                } else {
                    System.out.printf(
                            " register asset account failed, ret code is %s \n", response.get(0).ret.toString());
                }
            } else {
                System.out.println(" event log not found, maybe transaction not exec. ");
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            logger.error(" registerAssetAccount exception, error message is {}", e.getMessage());
            System.out.printf(" register asset account failed, error message is %s\n", e.getMessage());
        }
        return i;
    }

    public int updateAssetAccount(String assetAccount, BigInteger amount, BigInteger isReduce) {
        int i = 0;
        try {
            Asset asset = Asset.load(CONTRACT_ADDRESS, client, cryptoKeyPair);
            TransactionReceipt receipt = asset.update(assetAccount, amount, isReduce);
            List<Asset.UpdateEventEventResponse> response = asset.getUpdateEventEvents(receipt);
            if (!response.isEmpty()) {
                if (response.get(0).ret.compareTo(new BigInteger("0")) == 0) {
                    System.out.printf(
                            " update asset account success => asset: %s, value: %s \n", assetAccount, amount);
                    i = 1;
                } else {
                    System.out.printf(
                            " update asset account failed, ret code is %s \n", response.get(0).ret.toString());
                }
            } else {
                System.out.println(" event log not found, maybe transaction not exec. ");
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            logger.error(" updateAssetAccount exception, error message is {}", e.getMessage());
            System.out.printf(" update asset account failed, error message is %s\n", e.getMessage());
        }
        return i;
    }

    public int deleteAssetAccount(String assetAccount) {
        int i = 0;
        try {
            Asset asset = Asset.load(CONTRACT_ADDRESS, client, cryptoKeyPair);
            TransactionReceipt receipt = asset.deleteAccount(assetAccount);
            List<Asset.DeleteEventEventResponse> response = asset.getDeleteEventEvents(receipt);
            if (!response.isEmpty()) {
                if (response.get(0).ret.compareTo(new BigInteger("0")) == 0) {
                    System.out.printf(
                            " delete asset account success => asset: %s \n", assetAccount);
                    i = 1;
                } else {
                    System.out.printf(
                            " delete asset account failed, ret code is %s \n", response.get(0).ret.toString());
                }
            } else {
                System.out.println(" event log not found, maybe transaction not exec. ");
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            logger.error(" deleteAssetAccount exception, error message is {}", e.getMessage());
            System.out.printf(" delete asset account failed, error message is %s\n", e.getMessage());
        }
        return i;
    }

    public void transferAsset(String fromAssetAccount, String toAssetAccount, BigInteger amount) {
        try {
            Asset asset = Asset.load(CONTRACT_ADDRESS, client, cryptoKeyPair);
            TransactionReceipt receipt = asset.transfer(fromAssetAccount, toAssetAccount, amount);
            List<Asset.TransferEventEventResponse> response = asset.getTransferEventEvents(receipt);
            if (!response.isEmpty()) {
                if (response.get(0).ret.compareTo(new BigInteger("0")) == 0) {
                    System.out.printf(
                            " transfer success => from_asset: %s, to_asset: %s, amount: %s \n",
                            fromAssetAccount, toAssetAccount, amount);
                } else {
                    System.out.printf(
                            " transfer asset account failed, ret code is %s \n", response.get(0).ret.toString());
                }
            } else {
                System.out.println(" event log not found, maybe transaction not exec. ");
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            // e.printStackTrace();

            logger.error(" registerAssetAccount exception, error message is {}", e.getMessage());
            System.out.printf(" register asset account failed, error message is %s\n", e.getMessage());
        }
    }

    public static void Usage() {
        System.out.println(" Usage:");
        System.out.println(
                "\t java -cp conf/:lib/*:apps/* org.fisco.bcos.asset.client.AssetClient deploy");
        System.out.println(
                "\t java -cp conf/:lib/*:apps/* org.fisco.bcos.asset.client.AssetClient query account");
        System.out.println(
                "\t java -cp conf/:lib/*:apps/* org.fisco.bcos.asset.client.AssetClient register account value");
        System.out.println(
                "\t java -cp conf/:lib/*:apps/* org.fisco.bcos.asset.client.AssetClient transfer from_account to_account amount");
        System.exit(0);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
        initialize();
    }
}
