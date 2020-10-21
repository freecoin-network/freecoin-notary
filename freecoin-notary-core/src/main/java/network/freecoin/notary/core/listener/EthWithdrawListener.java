package network.freecoin.notary.core.listener;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import network.freecoin.notary.core.common.EthVerifyTrxTransPool;
import network.freecoin.notary.core.common.config.ConstSetting;
import network.freecoin.notary.core.dao.entity.EthBurnInfo;
import network.freecoin.notary.core.dao.mapper.EthBurnInfoMapper;
import network.freecoin.notary.core.dto.EthVerifyData;
import network.freecoin.notary.core.handler.AlertHandler;
import network.freecoin.notary.core.service.EthNotaryService;
import network.freecoin.notary.tron.service.NormalTxService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author pengyuxiang
 * @date 2020/9/13
 */


@Component
@Slf4j
public class EthWithdrawListener {

  @Autowired
  private EthNotaryService ethNotaryService;
  @Autowired
  private EthBurnInfoMapper ethBurnInfoMapper;
  @Autowired
  private NormalTxService normalTxService;
  @Autowired
  private EthVerifyTrxTransPool ethVerifyTrxTransPool;
  @Autowired
  private AlertHandler alertHandler;

  private volatile boolean isRunning;

  public EthWithdrawListener() {
    this.isRunning = true;
  }

  @SneakyThrows
  public void run() {
    logger.info("start run EthWithdrawListener");
    while (isRunning) {
      Thread.sleep(3000);
      long curOffset = ethBurnInfoMapper.getCurRecord() + 1;
      long nowOffset = ethNotaryService.getEthSender().getOffsetData();
      while (curOffset < nowOffset) {
        logger.info("Start to Consume : curOffset {} : nowOffset {}",
                curOffset, nowOffset);
        EthBurnInfo ethBurnInfo = ethNotaryService.getBurnInfo(curOffset);
        String recipient = ethBurnInfo.getRecipient();
        long amount = ethBurnInfo.getAmount();
        long amountOnSideChain = amount;
        long approve = ethBurnInfo.getApprove();

        ethBurnInfo.setAmountOnSideChain(amountOnSideChain);
        logger.info("Insert new BurnInfo curOffset({}) recipient({}), amount({})",
            curOffset, recipient, amount);
        ethBurnInfoMapper.insert(ethBurnInfo);
        //send trx when status is init or failed (no_verify not send)
        // todo: init, fail will not retry when restart

        // todo: check "recipient" address format is right ?
        logger.info("send {} trx to {}", amountOnSideChain, recipient);
        String txId = "";
        String status = ConstSetting.WITHDRAW_NO_TRX_VERIFY;
        try {
          txId = normalTxService.sendTrx(recipient, amountOnSideChain);
        } catch (Exception e) {
          status = ConstSetting.WITHDRAW_FAILED;
          alertHandler.sendAlert("send trx fail, handle next", e);
        }
        ethBurnInfo.setTxOnSideChain(txId);
        ethBurnInfo.setStatus(status);

        ethBurnInfoMapper.updateById(ethBurnInfo);

        EthVerifyData ethVerifyData = EthVerifyData.builder()
            .id(ethBurnInfo.getId())
            .recipient(recipient)
            .txOnSideChain(txId)
            .burnProposalId(curOffset)
            .amountOnSideChain(amountOnSideChain)
            .amount(amount)
            .approve(approve)
            .timestamp(System.currentTimeMillis() / 1_000)
            .build();
        ethVerifyTrxTransPool.produce(ethVerifyData);
        curOffset++;
      }
    }
  }
}

