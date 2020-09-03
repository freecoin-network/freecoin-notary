package network.freecoin.notary.ethereum.services;

import java.io.IOException;

import network.freecoin.notary.ethereum.configuration.EthConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthBlockNumber;
import org.web3j.protocol.core.methods.response.EthGasPrice;
import org.web3j.protocol.core.methods.response.Web3ClientVersion;
import org.web3j.protocol.http.HttpService;

/**
 * @author pengyuxiang
 * @date 2020/9/3
 */

@Component
public class EthService {

    @Autowired
    private EthConfig ethConfig;
    private Web3j web3j;


    /**
     * 选择验证者
     */
    public String genSinger(){

        return "";
    }



}
