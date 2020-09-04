package network.freecoin.notary.core.listener;

import lombok.extern.slf4j.Slf4j;
import network.freecoin.notary.core.common.DataPool;
import network.freecoin.notary.core.dto.DepositData;
import network.freecoin.notary.tron.common.utils.Sha256Hash;
import network.freecoin.notary.tron.protos.Protocol.Block;
import network.freecoin.notary.tron.protos.Protocol.Transaction;
import network.freecoin.notary.tron.protos.Protocol.TransactionInfo;
import network.freecoin.notary.tron.service.BlockInfoService;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TronDepositListener {

  @Autowired
  private BlockInfoService blockInfoService;
  @Autowired
  private DataPool dataPool;
  private volatile boolean isRunning;

  public TronDepositListener() {
    isRunning = true;
  }

  public void run(long fromBlockNum) {
    logger.info("fromBlockNum: {}", fromBlockNum);
    while (isRunning) {
      Block block = blockInfoService.getBlockByNum(fromBlockNum);
      logger.debug("block: {}", block);
      for (Transaction transaction : block.getTransactionsList()) {
        String txId = Hex.toHexString(Sha256Hash
            .of(transaction.getRawData().toByteArray()).getBytes());
        if (filter(transaction)) {
          continue;
        }
        long sleepTime = 0;
        if (sleepTime > 0) {

          try {
            Thread.sleep(sleepTime);
          } catch (InterruptedException e) {
            logger.warn("interrupted exception", e);
            Thread.currentThread().interrupt();
          }
        }
        // todo: record in db
        TransactionInfo transactionInfo = blockInfoService.getTransactionInfoById(txId);
        logger.debug("transactionInfo: {}", transactionInfo);
        // todo: handle this
        DepositData depositData = DepositData.builder()
            .mintProposalId(1)
            .senderOnSideChain("")
            .amount(0)
            .txOnSideChain("")
            .build();
        dataPool.produce(depositData);
      }
    }
  }

  public boolean filter(Transaction transaction) {
    return false;
  }

  public void stop() {
    isRunning = false;
  }

}