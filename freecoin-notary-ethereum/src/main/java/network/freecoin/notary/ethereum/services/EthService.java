package network.freecoin.notary.ethereum.services;

import lombok.SneakyThrows;
import network.freecoin.notary.ethereum.configuration.ConstSetting;
import network.freecoin.notary.ethereum.configuration.EthConfig;
import network.freecoin.notary.ethereum.configuration.EthContractConfig;
import network.freecoin.notary.ethereum.configuration.EthWallteConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.*;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.*;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * @author pengyuxiang
 * @date 2020/9/3
 */

@Component
public class EthService {

    @Autowired
    private EthConfig ethConfig;

    @Autowired
    private EthWallteConfig ethWallteConfig;

    @Autowired
    private EthContractConfig ethContractConfig;

    @Autowired
    private Web3j web3j;


    /**
     * 选择验证者
     */
    @SneakyThrows
    public List<Credentials> genSinger(){
        int count = ethConfig.getSinger();
        List<Credentials> cs = ethWallteConfig.wallets().getWalletList();
        if (count > cs.size()) throw new Exception();
        if (count == cs.size()) return cs;
        List<Credentials> signers = new ArrayList<>();
        Collections.copy(cs, signers);
        while (signers.size() > count) {
            int idx = (int) (System.currentTimeMillis() % signers.size());
            signers.remove(idx);
        }
        return signers;
    }

    @SneakyThrows
    public String getEthAddrByTrx(Credentials credentials, String trxAddr){
        BigInteger nonce = getNonce(credentials.getAddress());
        BigInteger gasPrice = web3j.ethGasPrice().send().getGasPrice();
        String to = ethContractConfig.getAddress();
        BigInteger gasLimit = ethConfig.getGaslimit();

        List<Type> input = Collections.emptyList();
        input.add(new Utf8String(trxAddr));
        List<TypeReference<?>> output = new ArrayList<>();
        TypeReference<Utf8String> t2 = new TypeReference<Utf8String>() {};
        output.add(t2);
        Function function = new Function(ConstSetting.GET_ETH_ADDR, input, output);
        String data = FunctionEncoder.encode(function);
        EthCall response = web3j.ethCall(Transaction.createEthCallTransaction(credentials.getAddress(), to, data), DefaultBlockParameterName.LATEST).sendAsync().get();
        String result = response.getResult();
        return result;
    }

    /**
     * @param credentials
     * @return
     */
    @SneakyThrows
    public boolean sendTransaction(Credentials credentials, long proposalId, String txSender, long amout, String txOnSideChain) {
        String from = credentials.getAddress();
        BigInteger nonce = getNonce(from);
        BigInteger gasPrice = web3j.ethGasPrice().send().getGasPrice();
        String to = ethContractConfig.getAddress();
        BigInteger gasLimit = ethConfig.getGaslimit();
        String data = buildDepositRequest(proposalId, txSender, amout, txOnSideChain);
        RawTransaction t = RawTransaction.createTransaction(
                nonce, gasPrice, gasLimit, to,  data
        );
        String hex = SignMessage(credentials, t);
        EthSendTransaction e = web3j.ethSendRawTransaction(hex).sendAsync().get();
        return true;
    }


    public String buildDepositRequest(long proposalId, String txSender, long amout, String txOnSideChain) {
        List<Type> input = new ArrayList<>();
        List<TypeReference<?>> output = new ArrayList<>();
        input.add(new Uint256(proposalId));
        input.add(new Utf8String(txSender));
        input.add(new Uint256(amout));
        input.add(new Utf8String(txOnSideChain));
        output.add(new TypeReference<Bool>() {});
        Function function = new Function(ConstSetting.DEPOSIT_CONFIRM, input, output);
        String data = FunctionEncoder.encode(function);
        return data;
    }


    /**
     * 获取nonce
     *
     * @param address
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    private BigInteger getNonce(String address) throws ExecutionException, InterruptedException {
        EthGetTransactionCount ethGetTransactionCount = web3j.ethGetTransactionCount(
                ethConfig.getService(), DefaultBlockParameterName.LATEST).sendAsync().get();
        return ethGetTransactionCount.getTransactionCount();
    }

    private String SignMessage(Credentials credentials, RawTransaction t) {
        byte[] b = TransactionEncoder.signMessage(t, credentials);
        String hex = Numeric.toHexString(b);
        return hex;
    }

}
