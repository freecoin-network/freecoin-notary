package network.freecoin.notary.core.listener;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import network.freecoin.notary.core.common.TronDepositPool;
import network.freecoin.notary.core.dao.entity.TronDeposit;
import network.freecoin.notary.core.dao.mapper.TronDepositMapper;
import network.freecoin.notary.core.dto.DepositData;
import network.freecoin.notary.core.service.EthNotaryService;
import network.freecoin.notary.ethereum.entity.NotaryAccount;
import network.freecoin.notary.tron.service.BlockInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author pengyuxiang
 * @date 2020/9/6
 */

@Component
@Slf4j
public class EthMintListener {

    @Autowired
    private EthNotaryService ethNotaryService;
    @Autowired
    private TronDepositPool tronDepositPool;
    @Autowired
    private TronDepositMapper tronDepositMapper;
    @Autowired
    private BlockInfoService blockInfoService;
    private volatile boolean isRunning;

    public EthMintListener() {
        this.isRunning = true;
    }

    public void mint(TronDeposit tronDeposit) {
        String txSender = tronDeposit.getSenderOnSideChain();
        long amount = tronDeposit.getAmount();
        String txOnSideChain = tronDeposit.getTxOnSideChain();
        List<NotaryAccount> notaryAccounts = ethNotaryService.getNotaries();
        notaryAccounts.forEach(n -> n.depositConfirm(txSender, amount, txOnSideChain));
    }

    @SneakyThrows
    public void run() {
        logger.info("start run ethListener");

        while (isRunning) {
            DepositData d = tronDepositPool.consume();
            String trxSender = d.getSenderOnSideChain();
            long amount = d.getAmount();
            long blockNum = d.getBlockNum();
            String txOnSideChain = d.getTxOnSideChain();

            // todo add timestamp
            //六个区块认证
            //todo 没有确认块高

//            long latest = ethNotaryService.getEthSender().getLatestBlockNum().longValue();
//            while (latest - blockNum < 6) {
//                latest = ethNotaryService.getEthSender().getLatestBlockNum().longValue();
//                Thread.sleep(15000);
//            }

            Thread.sleep(60_000);

            // todo: end

            UpdateWrapper<TronDeposit> uw = new UpdateWrapper<>();
            uw.eq("tx_on_side_chain", txOnSideChain);
            if (ethNotaryService.verifyMint(txOnSideChain)) {
                TronDeposit newDeposit = TronDeposit.builder()
                        .blockNum(blockNum)
                        .amount(amount)
                        .txOnSideChain(txOnSideChain)
                        .senderOnSideChain(trxSender)
                        .status(1)
                        .build();
                tronDepositMapper.update(newDeposit, uw);
                logger.info("Mint Success txOnSideChain {}, amount {}", txOnSideChain, amount);
            } else {
                TronDeposit newDeposit = TronDeposit.builder()
                        .blockNum(blockNum)
                        .amount(amount)
                        .txOnSideChain(txOnSideChain)
                        .senderOnSideChain(trxSender)
                        .status(2)
                        .build();
                tronDepositMapper.update(newDeposit, uw);
                logger.error("Mint Failed txOnSideChain {}, amount {}", txOnSideChain, amount);
            }
        }
    }

    public void stop() {
        this.isRunning = false;
    }
}
