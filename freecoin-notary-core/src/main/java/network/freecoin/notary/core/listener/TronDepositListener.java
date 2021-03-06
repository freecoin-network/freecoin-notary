package network.freecoin.notary.core.listener;

import static network.freecoin.notary.core.common.config.ConstSetting.TRX_CONFIRM_SECOND;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import network.freecoin.notary.core.common.TronNotaryAddressPool;
import network.freecoin.notary.core.common.config.ConstSetting;
import network.freecoin.notary.core.common.utils.TronTxUtil;
import network.freecoin.notary.core.dao.entity.TronDeposit;
import network.freecoin.notary.core.dao.entity.TronDepositMeta;
import network.freecoin.notary.core.dao.mapper.TronDepositMapper;
import network.freecoin.notary.core.dao.mapper.TronDepositMetaMapper;
import network.freecoin.notary.core.handler.AlertHandler;
import network.freecoin.notary.core.handler.TronDepositHandler;
import network.freecoin.notary.tron.protos.Protocol.Block;
import network.freecoin.notary.tron.protos.Protocol.Transaction;
import network.freecoin.notary.tron.service.BlockInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TronDepositListener {

  @Autowired
  private BlockInfoService blockInfoService;
  @Autowired
  private TronNotaryAddressPool tronNotaryAddressPool;
  @Autowired
  private TronDepositMapper tronDepositMapper;
  @Autowired
  private TronDepositMetaMapper tronDepositMetaMapper;
  @Autowired
  private TronDepositHandler tronDepositHandler;
  @Autowired
  private AlertHandler alertHandler;

  private volatile boolean isRunning;

  public TronDepositListener() {
    isRunning = true;
  }

  public void run(long fromBlockNum, int fromTxIndex) {
    logger.info("fromBlockNum: {}, fromTxIndex: {}", fromBlockNum, fromTxIndex);
    long blockNum = fromBlockNum;
    try {
      while (isRunning) {
        Block block = blockInfoService.getBlockByNum(blockNum);
        if (waitingForNewOrConfirmBlock(block)) {
          continue;
        }

        TronDepositMeta tronDepositMeta = TronDepositMeta.builder()
            .id(ConstSetting.TRON_DEPOSIT_META_ID)
            .blockNum(blockNum)
            .txIndexOnSideChain(0)
            .build();
        tronDepositMetaMapper.updateById(tronDepositMeta);

        logger.debug("block: {}", block);

        List<Transaction> transactionList = block.getTransactionsList();
        // maintain the order
        for (int txIndex = blockNum == fromBlockNum ? fromTxIndex : 0;
            txIndex < transactionList.size(); txIndex++) {
          Transaction transaction = transactionList.get(txIndex);
          preHandleTx(transaction, blockNum, txIndex);
        }

        blockNum++;
      }
    } catch (Exception e) {
      alertHandler.sendAlert("listener loop break", e);
    }
  }

  private boolean waitingForNewOrConfirmBlock(Block block) {
    boolean needWait = false;
    if (block.equals(Block.getDefaultInstance())) {
      logger.info("waiting for generating new block: {}",
          block.getBlockHeader().getRawData().getNumber());
      needWait = true;
    } else {
      long now = System.currentTimeMillis() / 1_000;
      long blockTimestamp = block.getBlockHeader().getRawData().getTimestamp() / 1_000;
      if (blockTimestamp + TRX_CONFIRM_SECOND > now) {
        needWait = true;
      }
    }
    if (needWait) {
      try {
        Thread.sleep(ConstSetting.WAITING_FOR_NEW_BLOCK_MS);
      } catch (InterruptedException e) {
        logger.warn("interrupted exception", e);
        Thread.currentThread().interrupt();
      }
    }
    return needWait;
  }

  private void preHandleTx(Transaction transaction, long blockNum, int txIndex) {
    TronTxUtil tronTxUtil = new TronTxUtil(transaction);
    if (filter(tronTxUtil)) {
      return;
    }
    String sender = tronTxUtil.getOwner();
    long amount = tronTxUtil.getSunValue();
    String txId = tronTxUtil.getTxId();
    // todo: use solidity node
    TronDeposit tronDeposit = TronDeposit.builder()
        .blockNum(blockNum)
        .senderOnSideChain(sender)
        .amount(amount)
        .txOnSideChain(txId)
        .txIndexOnSideChain(txIndex)
        .status(0)
        .build();
    tronDepositMapper.insert(tronDeposit);
    tronDepositHandler.handleTx(sender, amount, txId, blockNum);
  }

  private boolean filter(TronTxUtil tronTxUtil) {
    // the transaction must be success from solidity node,
    //  especially when smart contract
    String toAddress = tronTxUtil.getToAddress();
    return !tronNotaryAddressPool.contain(toAddress);
  }

  public void stop() {
    isRunning = false;
  }

}