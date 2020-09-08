package network.freecoin.notary.core.listener;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import network.freecoin.notary.core.common.TronDepositPool;
import network.freecoin.notary.core.dao.entity.TronDeposit;
import network.freecoin.notary.core.dao.mapper.TronDepositMapper;
import network.freecoin.notary.core.dto.DepositData;
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
    @Autowired
    private TronDepositPool tronDepositPool;
    @Autowired
    private TronDepositMapper tronDepositMapper;
    private volatile boolean isRunning;

    public EthMinter() {
        this.isRunning = true;
    }

    public void mint(long proposalId, String txSender, long amout, String txOnSideChain) {
        List<Credentials> crs = ethService.genSinger();
        crs.stream().forEach(cr -> {
            ethService.sendTransaction(cr, proposalId, txSender, amout, txOnSideChain);
        });
    }

    public void run() {
        while (isRunning) {
            DepositData d = tronDepositPool.consume();
            Credentials c = ethService.getWallet();
            long proposalId = d.getMintProposalId();
            String trxSender = d.getSenderOnSideChain();
            long amout = d.getAmount();
            String txOnSideChain = d.getTxOnSideChain();
            int retryCount = 5;
            while(false == ethService.verifyMintTransaction(c, proposalId, trxSender, amout, txOnSideChain)) {
                try {
                    Thread.sleep(30000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if(--retryCount > 0) break;
            }
            UpdateWrapper<TronDeposit> uw = new UpdateWrapper();
            uw.eq("proposalId", proposalId);
            TronDeposit newDeposit = TronDeposit.builder().status(1).build();
            if(retryCount == 0) ; //todo failed
            if(retryCount > 0) {
                tronDepositMapper.update(newDeposit, uw);
            }; //success
        }
    }

    public void stop() {
        this.isRunning = false;
    }
}
