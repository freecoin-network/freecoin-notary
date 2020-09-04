package network.freecoin.notary.ethereum.services;

import network.freecoin.notary.ethereum.configuration.EthConfig;
import network.freecoin.notary.ethereum.configuration.EthContractConfig;
import network.freecoin.notary.ethereum.configuration.EthWallteConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;

import java.io.IOException;
import java.math.BigInteger;
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
    public List<Credentials> genSinger() throws Exception {
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


    /**
     * @param credentials
     * @return
     */
    public boolean sendTransaction(Credentials credentials) throws ExecutionException, InterruptedException, IOException {
        String from = credentials.getAddress();
        BigInteger nonce = getNonce(from);
        BigInteger gasPrice = web3j.ethGasPrice().send().getGasPrice();
        String to = ethContractConfig.getAddress();
        BigInteger gasLimit = ethConfig.getGaslimit();

        //todo send trans
        return true;
    }


    /**
     * 获取nonce
     *
     * @param address
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public BigInteger getNonce(String address) throws ExecutionException, InterruptedException {
        EthGetTransactionCount ethGetTransactionCount = web3j.ethGetTransactionCount(
                ethConfig.getService(), DefaultBlockParameterName.LATEST).sendAsync().get();
        return ethGetTransactionCount.getTransactionCount();
    }

}
