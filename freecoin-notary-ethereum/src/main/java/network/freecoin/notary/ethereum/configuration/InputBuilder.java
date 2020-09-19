package network.freecoin.notary.ethereum.configuration;


import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.abi.datatypes.generated.Uint32;

import java.util.ArrayList;
import java.util.List;

public class InputBuilder {
    private List<Type> input;
    public InputBuilder() {

    }

    public static InputBuilder build(){
        InputBuilder i = new InputBuilder();
        i.input = new ArrayList<>();
        return i;
    }

    public InputBuilder addUtf8String(String s) {
        input.add(new Utf8String(s));
        return this;
    }

    public InputBuilder addUInt32(long l) {
        input.add(new Uint32(l));
        return this;
    }
    public InputBuilder addUInt256(long l) {
        input.add(new Uint256(l));
        return this;
    }

    public List<Type> get() {
        return this.input;
    }

}
