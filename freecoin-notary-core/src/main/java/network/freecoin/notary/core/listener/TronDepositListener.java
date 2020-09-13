package network.freecoin.notary.core.listener;

import lombok.extern.slf4j.Slf4j;
import network.freecoin.notary.core.common.TronDepositPool;
import network.freecoin.notary.core.common.TronNotaryAddressPool;
import network.freecoin.notary.core.common.config.ConstSetting;
import network.freecoin.notary.core.common.utils.TronTxUtil;
import network.freecoin.notary.core.dao.entity.TronDeposit;
import network.freecoin.notary.core.dao.entity.TronDepositMeta;
import network.freecoin.notary.core.dao.mapper.TronDepositMapper;
import network.freecoin.notary.core.dao.mapper.TronDepositMetaMapper;
import network.freecoin.notary.core.dto.DepositData;
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
  private TronDepositPool tronDepositPool;
  @Autowired
  private TronNotaryAddressPool tronNotaryAddressPool;
  @Autowired
  private TronDepositMapper tronDepositMapper;
  @Autowired
  private TronDepositMetaMapper tronDepositMetaMapper;
  @Autowired
  private EthMinter ethMinter;

  private volatile boolean isRunning;

  public TronDepositListener() {
    isRunning = true;
  }

  public void run(long fromBlockNum, long fromMintProposalId) {
    logger.info("fromBlockNum: {}", fromBlockNum);
    long blockNum = fromBlockNum;
    long mintProposalId = fromMintProposalId;
    while (isRunning) {
      TronDepositMeta tronDepositMeta = TronDepositMeta.builder()
          .blockNum(blockNum)
          .mintProposalId(mintProposalId)
          .id(ConstSetting.TRON_DEPOSIT_META_ID)
          .build();
      tronDepositMetaMapper.updateById(tronDepositMeta);
      Block block = blockInfoService.getBlockByNum(blockNum);
      if (block.equals(Block.getDefaultInstance())) {
        logger.info("waiting for generating new block: {}", blockNum);
        try {
          Thread.sleep(ConstSetting.WAITING_FOR_NEW_BLOCK__MS);
        } catch (InterruptedException e) {
          logger.warn("interrupted exception", e);
          Thread.currentThread().interrupt();
        }
        continue;
      }
      logger.debug("block: {}", block);
      for (Transaction transaction : block.getTransactionsList()) {
        TronTxUtil tronTxUtil = new TronTxUtil(transaction);
        if (filter(tronTxUtil)) {
          continue;
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
            .status(0)
            .build();
        // fixme: insert ignore
        tronDepositMapper.insert(tronDeposit);
        // TransactionInfo transactionInfo = blockInfoService.getTransactionInfoById(txId);
        // logger.debug("transactionInfo: {}", transactionInfo);
        ethMinter.mint(mintProposalId, sender, amount, txId);
        DepositData depositData = DepositData.builder()
            .blockNum(blockNum)
            .mintProposalId(mintProposalId)
            .senderOnSideChain(sender)
            .amount(amount)
            .txOnSideChain(txId)
            .build();
        tronDepositPool.produce(depositData);
        mintProposalId++;
      }
      blockNum++;
    }
  }

  public boolean filter(TronTxUtil tronTxUtil) {
    // the transaction must be success from solidity node,
    //  especially when smart contract
    String toAddress = tronTxUtil.getToAddress();
    if (!tronNotaryAddressPool.contain(toAddress)) {
      return true;
    }
    return false;
  }

  public void stop() {
    isRunning = false;
  }

}