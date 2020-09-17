package network.freecoin.notary.core.listener;


import lombok.SneakyThrows;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.*;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.abi.datatypes.generated.Uint8;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.protocol.http.HttpService;
import org.web3j.tuples.EmptyTuple;
import org.web3j.tuples.Tuple;

import java.lang.ref.Reference;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * @author pengyuxiang
 * @date 2020/9/10
 */
public class Test {

    @SneakyThrows
    public static void main(String[] args) {
//        Web3j web3j = Web3j.build(new HttpService("https://rinkeby.infura.io/v3/072097a24b7c4994912ccd66991d5300"));
        Web3j web3j = Web3j.build(new HttpService("https://rinkeby.infura.io/v3/072097a24b7c4994912ccd66991d5300"));

        Credentials c = Credentials.create("7a9f84511df4138739766e4dafe66869cd3b8fbd7f424b6e241427c0011bfa5f");
//        String to = "0xD313C042d996f29db672B49BC3b1e018E065dbe7";
//        String to = "0xB4c62c686C0445a62012081Fa9B7832A71aB4A38";
        String to = "0x90C83828f75Db44b0B643f45A834D31733112A3d";

//        List<EthBlock.TransactionResult> block = web3j.ethGetBlockByNumber(DefaultBlockParameter.valueOf(new BigInteger("7195304")), true)
//                .send().getBlock().getTransactions();


//        for(EthBlock.TransactionResult tx : block) {
//            EthBlock.TransactionObject transaction = (EthBlock.TransactionObject) tx.get();
//            String from = transaction.getFrom();
//            String to = transaction.getTo();
//            transaction.getBlockNumber();
//
//        }
        List<Type> input = new ArrayList<>();
        List<TypeReference<?>> output = new ArrayList<>();
        input.add(new Uint256(0));
//        input.add(new Uint256(0));
        output.add(new TypeReference<Address>() {});
//        output.add(new TypeReference<Uint8>() {});
//        output.add(new TypeReference<Bool>() {});
        output.add(new TypeReference<Uint256>() {});
        output.add(new TypeReference<Utf8String>() {});
        output.add(new TypeReference<Uint256>() {});
        output.add(new TypeReference<Utf8String>() {});
        output.add(new TypeReference<Uint8>() {});
        output.add(new TypeReference<Bool>() {});
//        TypeReference t = new TypeReference<Array>() {};
//        TypeReference<DynamicArray> d1 = new TypeReference<DynamicArray>();
//        output.add(new TypeReference<Bool>() {});
//        EmptyTuple e = new
//        output.add(new TypeReference<Uint256>() {});
//        output.add(new TypeReference<Utf8String>() {});
        Function function = new Function("getBurnInfo", input, output);
        String data = FunctionEncoder.encode(function);
//
        EthCall response = web3j.ethCall(Transaction.createEthCallTransaction(c.getAddress(), to, data), DefaultBlockParameterName.LATEST).sendAsync().get();
        List<Type> result = FunctionReturnDecoder.decode(response.getValue(), function.getOutputParameters());
        Address addr = new Address(result.get(0).getValue().toString());
        long amout = ((Uint256)result.get(1)).getValue().longValue();
        String recipient = result.get(2).toString();
        long amoutOnSideChain = ((Uint256)result.get(3)).getValue().longValue();
        String txOnSideChain = result.get(4).toString();
        int approve = ((Uint8)result.get(5)).getValue().intValue();
        boolean success = ((Bool) result.get(6)).getValue();

        System.out.println();

    }
}
