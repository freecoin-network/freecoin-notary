package network.freecoin.notary.ethereum.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import network.freecoin.notary.ethereum.configuration.ConstSetting;
import network.freecoin.notary.ethereum.services.CommonUtils;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.Response;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;


@Slf4j
@Data
@NoArgsConstructor
public class EthAccount {

    protected Credentials c;
    protected String from;
    protected BigInteger gasLimit;
    protected Web3j web3j;

    public EthAccount(Credentials c) {
        this(c, ConstSetting.RINKEBY);
    }

    public EthAccount(Credentials c, String service) {
        this.c = c;
        this.from = c.getAddress();
        web3j = Web3j.build(new HttpService(service));
    }

    public List<Object> call(String to, String fn, List<Type> input, List<TypeReference<?>> output) {
        Function f = new Function(fn, input, output);
        String data = FunctionEncoder.encode(f);
        EthCall e = exec(web3j.ethCall(Transaction.createEthCallTransaction(from, to, data),
                DefaultBlockParameterName.LATEST));
        List<Type> types = FunctionReturnDecoder.decode(e.getResult(), f.getOutputParameters());
        return CommonUtils.resolve(types);
    }

    protected String send(String to, String fn, List<Type> input, List<TypeReference<?>> output) {
        Function f = new Function(fn, input, output);
        String data = FunctionEncoder.encode(f);
        BigInteger nonce = getNonce();
        BigInteger gasPrice = getGasPrice();
        RawTransaction t = RawTransaction.createTransaction(
                nonce, gasPrice, gasLimit, to, data
        );
        String hex = SignMessage(t);
        EthSendTransaction e = exec(web3j.ethSendRawTransaction(hex));
        if (e.hasError()) {
            logger.error("SendError({}) to({}), fn({}), nonce({}), gasPrice({}), gasLimit({})"
                    , e.getError().getMessage(), to, fn, nonce, gasPrice, gasLimit);
        }
        return e.getTransactionHash();
    }

    public <T extends Response> T exec(Request<?, T> r) {
        int retry = 3;
        T t = null;

        while (t == null && retry-- > 0) {
            try {
                t = r.send();
                logger.debug("t({}) retry({})", t.getClass().getSimpleName(), 3 - retry);
            } catch (IOException e) {
                logger.error("t({}) e({})", t.getClass().getSimpleName(), e.getMessage());
            }
        }
        return t;
    }


    public BigInteger getNonce() {
        return this.exec(web3j.ethGetTransactionCount(from, DefaultBlockParameterName.LATEST)).getTransactionCount();
    }

    public BigInteger getGasPrice() {
        return this.exec(web3j.ethGasPrice()).getGasPrice();
    }

    public BigInteger getBalance() {
        return this.exec(web3j.ethGetBalance(c.getAddress(), DefaultBlockParameterName.LATEST)).getBalance();
    }

    public BigInteger getLatestBlockNum() {
        return this.exec(web3j.ethBlockNumber()).getBlockNumber();
    }

    public String SignMessage(RawTransaction t) {
        byte[] b = TransactionEncoder.signMessage(t, c);
        return Numeric.toHexString(b);
    }
}
