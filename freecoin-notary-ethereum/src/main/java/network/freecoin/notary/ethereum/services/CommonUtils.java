package network.freecoin.notary.ethereum.services;


import org.web3j.abi.datatypes.*;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class CommonUtils {

    public static List<Object> resolve(List<Type> result) {
        List<Object> ret = new ArrayList<>();
        result.forEach(type -> {
            if(type instanceof Address) ret.add(type.getValue().toString());
            if(type instanceof IntType) ret.add(((BigInteger)type.getValue()).longValue());
            if(type instanceof Utf8String) ret.add(type.getValue().toString());
            if(type instanceof Bool) ret.add(((Bool)type).getValue());
        });
        return ret;
    }

    private CommonUtils(){};
}
