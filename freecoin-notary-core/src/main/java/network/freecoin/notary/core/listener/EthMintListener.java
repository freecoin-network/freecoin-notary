package network.freecoin.notary.core.listener;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import network.freecoin.notary.core.common.TronDepositPool;
import network.freecoin.notary.core.dao.entity.TronDeposit;
import network.freecoin.notary.core.dao.mapper.TronDepositMapper;
import network.freecoin.notary.core.dto.DepositData;
import network.freecoin.notary.ethereum.services.EthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.web3j.crypto.Credentials;

/**
 * @author pengyuxiang
 * @date 2020/9/6
 */

@Component
@Slf4j
public class EthMintListener {

  @Autowired
  private EthService ethService;
  @Autowired
  private TronDepositPool tronDepositPool;
  @Autowired
  private TronDepositMapper tronDepositMapper;
  private volatile boolean isRunning;

  public EthMintListener() {
    this.isRunning = true;
  }

  public void mint(TronDeposit tronDeposit) {
    String txSender = tronDeposit.getSenderOnSideChain();
    long amout = tronDeposit.getAmount();
    String txOnSideChain = tronDeposit.getTxOnSideChain();
    List<Credentials> crs = ethService.genSinger();
    crs.stream().forEach(cr -> {
      ethService.sendTransaction(cr, txSender, amout, txOnSideChain);
    });
  }

  public void run() {
    logger.info("start run ethListener");
    while (isRunning) {
      DepositData d = tronDepositPool.consume();
      String trxSender = d.getSenderOnSideChain();
      long amount = d.getAmount();
      long blockNum = d.getBlockNum();
      String txOnSideChain = d.getTxOnSideChain();

      try {
        Thread.sleep(90000);
      } catch (InterruptedException e) {
        logger.error("Thread Sleep Error {}", e.getMessage());
      }

      UpdateWrapper<TronDeposit> uw = new UpdateWrapper();
      uw.eq("tx_on_side_chain", txOnSideChain);
      if(ethService.verifyMintTransaction(txOnSideChain)) {
        TronDeposit newDeposit = TronDeposit.builder()
                .blockNum(blockNum)
                .amount(amount)
                .txOnSideChain(txOnSideChain)
                .senderOnSideChain(trxSender)
                .status(1)
                .build();
        tronDepositMapper.update(newDeposit, uw);
        logger.info("Mint Success txOnSideChain {}, amount {}", txOnSideChain, amount);
      }
      else{
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
