package network.freecoin.notary.core.listener;

import lombok.extern.slf4j.Slf4j;
import network.freecoin.notary.ethereum.services.EthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.web3j.crypto.Credentials;

import java.util.List;

/**
 * @author pengyuxiang
 * @date 2020/9/6
 */

@Component
@Slf4j
public class EthMinter {
    @Autowired
    private EthService ethService;


    public void mint(long proposalId, String txSender, long amout, String txOnSideChain){
        List<Credentials> crs = ethService.genSinger();
        crs.stream().forEach(cr -> {
            ethService.sendTransaction(cr, proposalId, txSender, amout, txOnSideChain);
        });
    }
}
