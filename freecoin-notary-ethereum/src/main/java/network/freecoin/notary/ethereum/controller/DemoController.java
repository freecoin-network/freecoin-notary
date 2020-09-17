package network.freecoin.notary.ethereum.controller;

import network.freecoin.notary.ethereum.configuration.EthConfig;
import network.freecoin.notary.ethereum.configuration.EthContractConfig;
import network.freecoin.notary.ethereum.configuration.EthWallteConfig;
import network.freecoin.notary.ethereum.services.EthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.web3j.protocol.Web3j;
import org.web3j.crypto.*;

import java.util.List;


/**
 * @author pengyuxiang
 * @date 2020/9/4
 */

@RestController
@RequestMapping("/")
public class DemoController {

    @Autowired
    private EthConfig ethConfig;
    @Autowired
    private EthContractConfig ethContractConfig;
    @Autowired
    private EthWallteConfig ethWallteConfig;
    @Autowired
    private Web3j web3j;

    @Autowired
    private EthService ethService;

    @GetMapping("hello")
    public String demo() throws Exception {
        Credentials c = ethService.genSinger().get(0);
//        ethService.sendTransaction(c, "");
        return "hello";
    }
}
