package network.freecoin.notary.ethereum.configuration;

import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.abi.datatypes.generated.Uint8;

import java.util.ArrayList;
import java.util.List;

public class OutputBuilder {

    private List<TypeReference<?>> output;

    public static OutputBuilder build(){
        OutputBuilder o = new OutputBuilder();
        o.output = new ArrayList<>();
        return o;
    }

    public OutputBuilder addBool() {
        output.add(new TypeReference<Bool>() {});
        return this;
    }

    public OutputBuilder addAddress(){
        output.add(new TypeReference<Address>() {});
        return this;
    }
    public OutputBuilder addUtf8String(){
        output.add(new TypeReference<Utf8String>() {});
        return this;
    }
    public OutputBuilder addUint256(){
        output.add(new TypeReference<Uint256>() {});
        return this;
    }
    public OutputBuilder addUint8(){
        output.add(new TypeReference<Uint8>() {});
        return this;
    }

    public List<TypeReference<?>> get(){
        return this.output;
    }
}
