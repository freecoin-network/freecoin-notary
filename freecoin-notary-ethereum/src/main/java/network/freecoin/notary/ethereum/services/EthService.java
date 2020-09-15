package network.freecoin.notary.ethereum.services;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import network.freecoin.notary.ethereum.configuration.ConstSetting;
import network.freecoin.notary.ethereum.configuration.EthConfig;
import network.freecoin.notary.ethereum.configuration.EthContractConfig;
import network.freecoin.notary.ethereum.configuration.EthWallteConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.*;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.utils.Numeric;

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
@Slf4j
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
     * @return
     */
    public Credentials getWallet() {
        List<Credentials> wallets = ethWallteConfig.wallets().getWalletList();
        if (wallets.size() > 0) return wallets.get(0);
        return null;
    }

    @SneakyThrows
    public long getProposalId(Credentials credentials){
        String from = credentials.getAddress();
        String to = ethContractConfig.getAddress();
        List<Type> input = new ArrayList<>();
        List<TypeReference<?>> output = new ArrayList<>();
        output.add(new TypeReference<Uint256>() {});
        Function f = new Function(ConstSetting.GET_PROPOSALID, input, output);

        EthCall response = web3j.ethCall(Transaction.createEthCallTransaction(credentials.getAddress(), to, FunctionEncoder.encode(f)), DefaultBlockParameterName.LATEST).send();
        String result = response.getResult();
        List<Type> types = FunctionReturnDecoder.decode(result, f.getOutputParameters());
        Uint256 p = (Uint256) types.get(0).getValue();
        return p.getValue().longValue();
    }

    @SneakyThrows
    public List<String> getDepositAddr(Credentials credentials){
        String from = credentials.getAddress();
        String to = ethContractConfig.getAddress();
        List<Type> input = new ArrayList<>();
        List<TypeReference<?>> output = new ArrayList<>();
        //todo
        output.add(new TypeReference<Address>() {});
        Function f = new Function(ConstSetting.GET_DEPOSITOR_ADDR, input, output);
        EthCall response = web3j.ethCall(Transaction.createEthCallTransaction(credentials.getAddress(), to, FunctionEncoder.encode(f)), DefaultBlockParameterName.LATEST).send();
        String result = response.getResult();
        List<Type> types = FunctionReturnDecoder.decode(result, f.getOutputParameters());

        List<String> address = new ArrayList<>();
        for(Type t : types) {
            String s = t.getValue().toString();
            address.add(s);
        }
        return address;
    }

    /**
     * 选择验证者
     */
    @SneakyThrows
    public List<Credentials> genSinger() {
        return ethWallteConfig.wallets().getWalletList();
    }

    /**
     * @param credentials
     * @return
     */

    public void sendTransaction(Credentials credentials, String txSender, long amout, String txOnSideChain) {
        logger.info("CreAddr {},  txSender {}, ammout {}, txOnSideChain {} ",
                credentials.getAddress(), txSender, amout, txOnSideChain);
        String from = credentials.getAddress();
        BigInteger nonce = getNonce(from);
        BigInteger gasPrice = null;
        try {
            gasPrice = web3j.ethGasPrice().send().getGasPrice();
        } catch (IOException e) {
            logger.error("get gas Price Error: {}", e.getMessage());
        }
        String to = ethContractConfig.getAddress();
        BigInteger gasLimit = ethConfig.getGaslimit();
        String data = buildDepositRequest(txSender, amout, txOnSideChain);
        RawTransaction t = RawTransaction.createTransaction(
                nonce, gasPrice, gasLimit, to, data
        );
        String hex = SignMessage(credentials, t);
        EthSendTransaction e = null;
        try {
            e = web3j.ethSendRawTransaction(hex).send();
        } catch (IOException e1) {
            logger.error("SendRawException {}", e1.getMessage());
        }

        logger.info("SendRawTransaction Result : {}", e.getResult());
    }

    @SneakyThrows
    public boolean verifyMintTransaction(Credentials credentials, long proposalId) {
        String to = ethContractConfig.getAddress();
        Function f = buildVerifyRequest(proposalId);
        EthCall response = web3j.ethCall(Transaction.createEthCallTransaction(credentials.getAddress(), to, FunctionEncoder.encode(f)), DefaultBlockParameterName.LATEST).send();
        String result = response.getResult();
        if(result.equals("0x")) return false;
        List<Type> out = FunctionReturnDecoder.decode(result, f.getOutputParameters());
        Boolean bool = (Boolean) out.get(0).getValue();
        return bool;
    }


    public String buildDepositRequest(String txSender, long amout, String txOnSideChain) {
        List<Type> input = new ArrayList<>();
        List<TypeReference<?>> output = new ArrayList<>();
        input.add(new Utf8String(txSender));
        input.add(new Uint256(amout));
        input.add(new Uint256(amout));
        input.add(new Utf8String(txOnSideChain));
        output.add(new TypeReference<Bool>() {});
        Function function = new Function(ConstSetting.DEPOSIT_CONFIRM, input, output);
        String data = FunctionEncoder.encode(function);
        return data;
    }

    public Function buildVerifyRequest(long proposalId) {
        List<Type> input = new ArrayList<>();
        List<TypeReference<?>> output = new ArrayList<>();
        input.add(new Uint256(proposalId));
        output.add(new TypeReference<Bool>() {
        });
        Function function = new Function(ConstSetting.VERIFY_MINT, input, output);
        return function;
    }


    /**
     * 获取nonce
     *
     * @param address
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    private BigInteger getNonce(String address) {
        EthGetTransactionCount ethGetTransactionCount = null;
        try {
            ethGetTransactionCount = web3j.ethGetTransactionCount(
                    address, DefaultBlockParameterName.LATEST).send();
        } catch (IOException e) {
            logger.error("Address : {} Get Nonce Error{}", address);
        }
        return ethGetTransactionCount.getTransactionCount();
    }

    private String SignMessage(Credentials credentials, RawTransaction t) {
        byte[] b = TransactionEncoder.signMessage(t, credentials);
        String hex = Numeric.toHexString(b);
        return hex;
    }

    @SneakyThrows
    public long getOffsetData() {
        Credentials credentials = ethWallteConfig.wallets().getCredentials();
        String from = credentials.getAddress();
        List<Type> input = new ArrayList<>();
        List<TypeReference<?>> output = new ArrayList<>();
        output.add(new TypeReference<Uint256>() {});

        Function f = new Function(ConstSetting.GET_BURN_OFFSET, input, output);
        String data = FunctionEncoder.encode(f);
        EthCall ethCall = web3j.ethCall(Transaction.createEthCallTransaction(from, ethContractConfig.getAddress(), data),DefaultBlockParameterName.LATEST).send();

        String result = ethCall.getResult();
        List<Type> types = FunctionReturnDecoder.decode(result, f.getOutputParameters());
        Long offset = Long.valueOf(types.get(0).getValue().toString());
        return offset;
    }

    public void burn(long burnId) {

    }
}
