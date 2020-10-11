package network.freecoin.notary.core.listener;

import static network.freecoin.notary.core.common.config.ConstSetting.ETH_CONFIRM_SECOND;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import network.freecoin.notary.core.common.TronDepositPool;
import network.freecoin.notary.core.dao.entity.TronDeposit;
import network.freecoin.notary.core.dao.mapper.TronDepositMapper;
import network.freecoin.notary.core.dto.DepositData;
import network.freecoin.notary.core.handler.AlertHandler;
import network.freecoin.notary.core.service.EthNotaryService;
import network.freecoin.notary.ethereum.entity.NotaryAccount;
import network.freecoin.notary.tron.service.BlockInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
  @Autowired
  private AlertHandler alertHandler;

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

  public void run() {
    logger.info("start run ethListener");

    try {
      while (isRunning) {
        DepositData d = tronDepositPool.consume();
        long blockNum = d.getBlockNum();
        String trxSender = d.getSenderOnSideChain();
        long amount = d.getAmount();
        String txOnSideChain = d.getTxOnSideChain();
        long timestamp = d.getTimestamp();

        long now = System.currentTimeMillis() / 1_000;
        long needSleepSecond = timestamp + ETH_CONFIRM_SECOND - now;
        if (needSleepSecond > 0) {
          Thread.sleep(needSleepSecond * 1_000);
        }

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
          alertHandler.sendAlert("mint fail, handle next", null);
        }
      }
    } catch (Exception e) {
      alertHandler.sendAlert("listener loop break", e);
    }
  }

  public void stop() {
    this.isRunning = false;
  }
}
