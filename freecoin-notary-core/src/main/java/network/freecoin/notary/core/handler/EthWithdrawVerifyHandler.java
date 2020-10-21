package network.freecoin.notary.core.handler;


import lombok.extern.slf4j.Slf4j;
import network.freecoin.notary.core.common.EthVerifyTrxTransPool;
import network.freecoin.notary.core.common.EthWithdrawConfirmPool;
import network.freecoin.notary.core.common.config.ConstSetting;
import network.freecoin.notary.core.dao.entity.EthBurnInfo;
import network.freecoin.notary.core.dao.mapper.EthBurnInfoMapper;
import network.freecoin.notary.core.dto.EthVerifyData;
import network.freecoin.notary.core.service.EthNotaryService;
import network.freecoin.notary.ethereum.entity.NotaryAccount;
import network.freecoin.notary.tron.protos.Protocol;
import network.freecoin.notary.tron.protos.Protocol.TransactionInfo;
import network.freecoin.notary.tron.service.BlockInfoService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.List;
import static network.freecoin.notary.core.common.config.ConstSetting.TRX_CONFIRM_SECOND;

@Component
@Slf4j
public class EthWithdrawVerifyHandler {

  @Autowired
  private EthVerifyTrxTransPool ethVerifyTrxTransPool;
  @Autowired
  private EthWithdrawConfirmPool ethWithdrawConfirmPool;
  @Autowired
  private EthBurnInfoMapper ethBurnInfoMapper;
  @Autowired
  private EthNotaryService ethNotaryService;
  @Autowired
  private AlertHandler alertHandler;
  @Autowired
  private BlockInfoService blockInfoService;

  private boolean isRunning;

  public EthWithdrawVerifyHandler() {
    this.isRunning = true;
  }

  private boolean verifyTrxTrans(String txId) {
    Protocol.TransactionInfo transactionInfo = blockInfoService.getTransactionInfoById(txId);
    if (transactionInfo.equals(TransactionInfo.getDefaultInstance())) {
      return false;
    }
    return transactionInfo.getResult() == Protocol.TransactionInfo.code.SUCESS;
  }

  public void withdrawConfirm(EthVerifyData ethVerifyData) {
    long burnPorposalId = ethVerifyData.getBurnProposalId();
    long amountOnSideChain = ethVerifyData.getAmountOnSideChain();
    String txOnSideChain = ethVerifyData.getTxOnSideChain();
    List<NotaryAccount> notaryies = ethNotaryService.getNotaries();
    notaryies.forEach(
            n -> n.withdrawConfirmTransaction(burnPorposalId, amountOnSideChain, txOnSideChain));
  }

  public void run() {
    logger.info("Start EthWithdrawVerifyHandler");
    try {
      while (isRunning) {
        EthVerifyData ethVerifyData = ethVerifyTrxTransPool.consume();
        long timestamp = ethVerifyData.getTimestamp();
        long now = System.currentTimeMillis() / 1_000;
        long needSleepSecond = timestamp + TRX_CONFIRM_SECOND - now;
        if (needSleepSecond > 0) {
          Thread.sleep(needSleepSecond * 1_000);
        }

        EthBurnInfo ethBurnInfo = EthBurnInfo.builder().build();
        BeanUtils.copyProperties(ethVerifyData, ethBurnInfo);
        String txOnSideChain = ethVerifyData.getTxOnSideChain();
        if (verifyTrxTrans(txOnSideChain)) {
          ethBurnInfo.setStatus(ConstSetting.WITHDRAW_NO_ETH_CONFIRM);
          ethBurnInfoMapper.updateById(ethBurnInfo);
          this.withdrawConfirm(ethVerifyData);
          ethWithdrawConfirmPool.produce(ethVerifyData);
        } else {
          //TRX交易发送失败，直接进入失败状态
          ethBurnInfo.setStatus(ConstSetting.WITHDRAW_FAILED);
          ethBurnInfoMapper.updateById(ethBurnInfo);
          logger.error("Verify burnProposalId({}) failed", ethVerifyData.getBurnProposalId());
          alertHandler.sendAlert("send trx fail, handle next", null);
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
