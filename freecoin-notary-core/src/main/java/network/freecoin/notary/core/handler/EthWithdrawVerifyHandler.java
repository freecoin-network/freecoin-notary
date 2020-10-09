package network.freecoin.notary.core.handler;


import lombok.extern.slf4j.Slf4j;
import network.freecoin.notary.core.common.EthVerifyTrxTransPool;
import network.freecoin.notary.core.common.EthWithdrawConfirmPool;
import network.freecoin.notary.core.common.config.ConstSetting;
import network.freecoin.notary.core.dao.entity.EthBurnInfo;
import network.freecoin.notary.core.dao.mapper.EthBurnInfoMapper;
import network.freecoin.notary.core.service.EthNotaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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

  private boolean isRunning;

  public EthWithdrawVerifyHandler() {
    this.isRunning = true;
  }

  private boolean verifyTrx() {
    return true;
  }

  public void run() {
    logger.info("Start EthWithdrawVerifyHandler");
    while (isRunning) {
      EthBurnInfo e = ethVerifyTrxTransPool.consume();
      // todo add timestamp
      if (verifyTrx()) {
        //验证TRX交易成功，继续执行
        e.setStatus(ConstSetting.WITHDRAW_NO_ETH_CONFIRM);
        ethBurnInfoMapper.updateById(e);

        String txId = ethNotaryService.withdrawConfirmTransaction(
            e.getBurnProposalId(),
            e.getAmountOnSideChain(),
            e.getTxOnSideChain()
        );
        logger.info("Send WithdrawConfirm ({})", txId);

        ethWithdrawConfirmPool.produce(e);
      } else {
        //TRX交易发送失败，直接进入失败状态
        e.setStatus(ConstSetting.WITHDRAW_FAILED);
        ethBurnInfoMapper.updateById(e);
        logger.error("Verify burnProposalId{} failed", e.getBurnProposalId());
      }
    }
  }

  public void stop() {
    this.isRunning = false;
  }
}
