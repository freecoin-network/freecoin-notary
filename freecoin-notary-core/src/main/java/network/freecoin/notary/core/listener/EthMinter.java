package network.freecoin.notary.core.listener;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import lombok.SneakyThrows;
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
        logger.info("start run ethListener");
        while (isRunning) {
            DepositData d = tronDepositPool.consume();
            long proposalId = d.getMintProposalId();
            Credentials c = ethService.getWallet();
            String trxSender = d.getSenderOnSideChain();
            long amout = d.getAmount();
            String txOnSideChain = d.getTxOnSideChain();
            int retryCount = 6;
            while(false == ethService.verifyMintTransaction(c, proposalId)) {
                try {
                    Thread.sleep(30000);
                } catch (InterruptedException e) {
                    logger.error("Thread Sleep {}", e.getMessage());
                }
                if(--retryCount > 0) break;
            }
            UpdateWrapper<TronDeposit> uw = new UpdateWrapper();
            uw.eq("mint_proposal_id", proposalId);

            if(retryCount == 0) {
                TronDeposit newDeposit = TronDeposit.builder().
                        mintProposalId(proposalId).amount(amout).txOnSideChain(txOnSideChain).senderOnSideChain(trxSender).status(2).build();
                tronDepositMapper.update(newDeposit, uw);
                logger.error("Mint Failed prososalId {}, amout {}", proposalId, amout);
            }; //todo failed
            if(retryCount > 0) {
                TronDeposit newDeposit = TronDeposit.builder().
                        mintProposalId(proposalId).amount(amout).txOnSideChain(txOnSideChain).senderOnSideChain(trxSender).status(1).build();
                tronDepositMapper.update(newDeposit, uw);
                logger.info("Mint Success prososalId {}, amout {}", proposalId, amout);
            }; //success
        }
    }

    public void stop() {
        this.isRunning = false;
    }
}
