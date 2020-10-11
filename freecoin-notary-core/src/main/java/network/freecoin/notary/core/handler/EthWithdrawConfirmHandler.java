package network.freecoin.notary.core.handler;


import lombok.extern.slf4j.Slf4j;
import network.freecoin.notary.core.common.EthWithdrawConfirmPool;
import network.freecoin.notary.core.common.config.ConstSetting;
import network.freecoin.notary.core.dao.entity.EthBurnInfo;
import network.freecoin.notary.core.dao.mapper.EthBurnInfoMapper;
import network.freecoin.notary.core.dto.EthVerifyData;
import network.freecoin.notary.core.service.EthNotaryService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class EthWithdrawConfirmHandler {

  @Autowired
  private EthWithdrawConfirmPool ethWithdrawConfirmPool;
  @Autowired
  private EthBurnInfoMapper ethBurnInfoMapper;
  @Autowired
  private EthNotaryService ethNotaryService;
  @Autowired
  private AlertHandler alertHandler;

  private boolean isRunning;

  public EthWithdrawConfirmHandler() {
    this.isRunning = true;
  }

  public void run() {
    logger.info("Start EthWithdrawConfirmHandler");
    try {
      while (isRunning) {
        EthVerifyData ethVerifyData = ethWithdrawConfirmPool.consume();
        EthBurnInfo ethBurnInfo = EthBurnInfo.builder().build();
        BeanUtils.copyProperties(ethVerifyData, ethBurnInfo);
        boolean ret = ethNotaryService.getBurnStatus(ethVerifyData.getBurnProposalId());
        if (ret) {
          //提现成功
          ethBurnInfo.setStatus(ConstSetting.WITHDRAW_SUCCESS);
        } else {
          //提现失败，不进行重试
          ethBurnInfo.setStatus(ConstSetting.WITHDRAW_ETH_CONFIRM_FAILED);
          alertHandler.sendAlert("withdraw eth confirm fail, handle next", null);
        }
        // fixme: no "id"
        ethBurnInfoMapper.updateById(ethBurnInfo);
      }
    } catch (Exception e) {
      alertHandler.sendAlert("handler loop break", e);
    }
  }

  public void stop() {
    this.isRunning = false;
  }
}
