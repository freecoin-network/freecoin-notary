package network.freecoin.notary.ethereum.services;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import network.freecoin.notary.ethereum.configuration.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.*;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.abi.datatypes.generated.Uint8;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.*;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.math.BigInteger;
import java.util.*;
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
     * @param c
     * @return
     */

    public void sendTransaction(Credentials c, String txSender, long amount, String txOnSideChain) {
        logger.info("[DepositConfirm] CreAddr {},  txSender {}, ammout {}, txOnSideChain {} ",
                c.getAddress(), txSender, amount, txOnSideChain);
        String from = c.getAddress();
        BigInteger nonce = getNonce(from);
        BigInteger gasPrice = null;
        try {
            gasPrice = web3j.ethGasPrice().send().getGasPrice();
        } catch (IOException e) {
            logger.error("get gas Price Error: {}", e.getMessage());
        }
        String to = ethContractConfig.getAddress();
        BigInteger gasLimit = ethConfig.getGaslimit();
        List<Type> input = InputBuilder.build()
                .addUtf8String(txOnSideChain).addUtf8String(txSender)
                .addUInt256(amount).addUInt256(amount).get();
        List<TypeReference<?>> output = OutputBuilder.build().addBool().get();

        this.sendTransaction(c, to,gasLimit, ConstSetting.DEPOSIT_CONFIRM, input, output);
    }

    @SneakyThrows
    public boolean verifyMintTransaction(String txOnSideChain) {
        Credentials credentials = ethWallteConfig.wallets().getCredentials();
        String to = ethContractConfig.getAddress();
        List<Type> input = InputBuilder.build().addUtf8String(txOnSideChain).get();
        List<TypeReference<?>> output = OutputBuilder.build().addBool().get();
        List<Type> t = this.callTransaction(credentials, to, ConstSetting.VERIFY_MINT, input, output);
        if(t == null || t.size() == 0) return false;
        List ret = this.resolve(t);
        return (boolean) ret.get(0);
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

    public void withdrawConfirmTransaction(Credentials credentials, long burnProposalId, long amountOnSideChain, String txOnSideChain) {
        logger.info("[WithdrawConfirm]({}) transaction burnPId({}), amoutOnSideChain({}), txOnSideChain({})",
                credentials.getAddress(), burnProposalId, amountOnSideChain, txOnSideChain);
        String from = credentials.getAddress();
        String to = ethContractConfig.getAddress();
        BigInteger nonce = getNonce(from);
        BigInteger gasPrice = null;
        try {
            gasPrice = web3j.ethGasPrice().send().getGasPrice();
        } catch (IOException e) {
            logger.error("get gas Price Error: {}", e.getMessage());
        }
        BigInteger gasLimit = ethConfig.getGaslimit();

        List<Type> input = new ArrayList<>();
        List<TypeReference<?>> output = new ArrayList<>();
        input.add(new Uint256(burnProposalId));
        input.add(new Uint256(amountOnSideChain));
        input.add(new Utf8String(txOnSideChain));
        output.add(new TypeReference<Bool>() {});
        Function f = new Function(ConstSetting.WITHDRAW_CONFIRM, input, output);
        String data = FunctionEncoder.encode(f);

        RawTransaction t = RawTransaction.createTransaction(
                nonce, gasPrice, gasLimit, to, data
        );

        String hex = SignMessage(credentials, t);
        EthSendTransaction e = null;
        try {
            e = web3j.ethSendRawTransaction(hex).send();
        } catch (IOException e1) {
            logger.error("[WithdrawConfirm] SendRawException {}", e1.getMessage());
        }

        logger.info("[WithdrawConfirm] SendRawTransaction Result : {}", e.getResult());
    }

    public List<?>  resolve(List<Type> result) {
        List ret = new ArrayList<>();
        result.stream().forEach(type -> {
            if(type instanceof Address) ret.add(type.getValue().toString());
            if(type instanceof IntType) ret.add(((BigInteger)type.getValue()).longValue());
            if(type instanceof Utf8String) ret.add(type.getValue().toString());
            if(type instanceof Bool) ret.add(((Bool)type).getValue());
        });
        return ret;
    }

    public List<Type> callTransaction(Credentials c, String to, String fname, List<Type> input, List<TypeReference<?>> output) {
        String from = c.getAddress();
        Function f = new Function(fname, input, output);
        String data = FunctionEncoder.encode(f);
        EthCall ethCall = execCall(Transaction.createEthCallTransaction(from, to, data));
        if(ethCall == null) return null;
        List<Type> types = FunctionReturnDecoder.decode(ethCall.getResult(), f.getOutputParameters());
        return types;
    }

    public String sendTransaction(Credentials c, String to, BigInteger gasLimit, String fname, List<Type> input, List<TypeReference<?>> output) {
        String from = c.getAddress();
        Function f = new Function(fname, input, output);
        String data = FunctionEncoder.encode(f);
        BigInteger nonce = getNonce(from);

        BigInteger gasPrice = null;
        try {
            gasPrice = web3j.ethGasPrice().send().getGasPrice();
        } catch (IOException e) {
            logger.error("get gas Price Error: {}", e.getMessage());
        }

        RawTransaction t = RawTransaction.createTransaction(
                nonce, gasPrice, gasLimit, to, data
        );

        String hex = SignMessage(c, t);
        EthSendTransaction e = null;
        try {
            e = web3j.ethSendRawTransaction(hex).send();
        } catch (IOException e1) {
            logger.error("SendRawException {}", e1.getMessage());
        }

        logger.info("SendRawTransaction Result : {}", e.getResult());
        return e.getTransactionHash();
    }

    public EthCall execCall(Transaction t){
        int retry = 3;
        EthCall ethcall = null;
        while(ethcall == null && retry-- > 0) {
            try {
                ethcall = web3j.ethCall(t, DefaultBlockParameterName.LATEST).send();
            } catch (IOException e) {
                logger.error("execCall Error from({}), to({}), nonce({})", t.getFrom(), t.getTo(), t.getNonce());
            }
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                logger.error("execCall Thread Sleep Error {}", e.getMessage());
            }
        }
        return ethcall;
    }

}
