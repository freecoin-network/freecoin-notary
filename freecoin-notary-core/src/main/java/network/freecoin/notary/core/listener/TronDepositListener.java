package network.freecoin.notary.core.listener;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import network.freecoin.notary.core.common.TronNotaryAddressPool;
import network.freecoin.notary.core.common.config.ConstSetting;
import network.freecoin.notary.core.common.utils.TronTxUtil;
import network.freecoin.notary.core.dao.entity.TronDeposit;
import network.freecoin.notary.core.dao.entity.TronDepositMeta;
import network.freecoin.notary.core.dao.mapper.TronDepositMapper;
import network.freecoin.notary.core.dao.mapper.TronDepositMetaMapper;
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

  private volatile boolean isRunning;

  public TronDepositListener() {
    isRunning = true;
  }

  public void run(long fromBlockNum, long fromMintProposalId, int fromTxIndex) {
    logger.info("fromBlockNum: {}, fromMintProposalId: {}, fromTxIndex: {}", fromBlockNum,
        fromMintProposalId, fromTxIndex);
    long blockNum = fromBlockNum;
    long mintProposalId = fromMintProposalId;
    while (isRunning) {
      Block block = blockInfoService.getBlockByNum(blockNum);
      if (waitingForNewBlock(block)) {
        continue;
      }

      TronDepositMeta tronDepositMeta = TronDepositMeta.builder()
          .blockNum(blockNum)
          .txIndexOnSideChain(0)
          .id(ConstSetting.TRON_DEPOSIT_META_ID)
          .build();
      tronDepositMetaMapper.updateById(tronDepositMeta);

      logger.debug("block: {}", block);

      List<Transaction> transactionList = block.getTransactionsList();
      // todo test order
      for (int txIndex = blockNum == fromBlockNum ? fromTxIndex : 0;
          txIndex < transactionList.size(); txIndex++) {
        Transaction transaction = transactionList.get(txIndex);
        mintProposalId = preHandleTx(transaction, blockNum, mintProposalId, txIndex);
      }

      blockNum++;
    }
  }

  private boolean waitingForNewBlock(Block block) {
    if (block.equals(Block.getDefaultInstance())) {
      logger.info("waiting for generating new block: {}",
          block.getBlockHeader().getRawData().getNumber());
      try {
        Thread.sleep(ConstSetting.WAITING_FOR_NEW_BLOCK__MS);
      } catch (InterruptedException e) {
        logger.warn("interrupted exception", e);
        Thread.currentThread().interrupt();
      }
      return true;
    }
    return false;
  }

  private long preHandleTx(Transaction transaction, long blockNum, long mintProposalId,
      int txIndex) {
    TronTxUtil tronTxUtil = new TronTxUtil(transaction);
    if (filter(tronTxUtil)) {
      return mintProposalId;
    }
    String sender = tronTxUtil.getOwner();
    long amount = tronTxUtil.getSunValue();
    String txId = tronTxUtil.getTxId();
    long txTime = tronTxUtil.getTimestamp();
    long now = System.currentTimeMillis();
    long sleepTime = txTime + ConstSetting.RELAY_MS - now;
    if (sleepTime > 0) {
      try {
        Thread.sleep(sleepTime);
      } catch (InterruptedException e) {
        logger.warn("interrupted exception", e);
        Thread.currentThread().interrupt();
      }
    }
    TronDeposit tronDeposit = TronDeposit.builder()
        .blockNum(blockNum)
        .mintProposalId(mintProposalId)
        .senderOnSideChain(sender)
        .amount(amount)
        .txOnSideChain(txId)
        .txIndexOnSideChain(txIndex)
        .status(0)
        .build();
    // fixme: insert ignore
    tronDepositMapper.insert(tronDeposit);
    tronDepositHandler.handleTx(sender, amount, txId, blockNum, mintProposalId);
    return mintProposalId + 1;
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