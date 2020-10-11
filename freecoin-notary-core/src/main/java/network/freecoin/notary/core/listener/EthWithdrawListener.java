package network.freecoin.notary.core.listener;

import java.util.List;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import network.freecoin.notary.core.common.EthVerifyTrxTransPool;
import network.freecoin.notary.core.common.config.ConstSetting;
import network.freecoin.notary.core.dao.entity.EthBurnInfo;
import network.freecoin.notary.core.dao.mapper.EthBurnInfoMapper;
import network.freecoin.notary.core.dto.EthVerifyData;
import network.freecoin.notary.core.handler.AlertHandler;
import network.freecoin.notary.core.service.EthNotaryService;
import network.freecoin.notary.ethereum.entity.NotaryAccount;
import network.freecoin.notary.tron.service.BlockInfoService;
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
  private BlockInfoService blockInfoService;
  @Autowired
  private EthVerifyTrxTransPool ethVerifyTrxTransPool;
  @Autowired
  private AlertHandler alertHandler;

  private volatile boolean isRunning;

  public EthWithdrawListener() {
    this.isRunning = true;
  }

  public void withdrawConfirm(EthBurnInfo ethBurnInfo) {
    long burnPorposalId = ethBurnInfo.getBurnProposalId();
    long amountOnSideChain = ethBurnInfo.getAmountOnSideChain();
    String txOnSideChain = ethBurnInfo.getTxOnSideChain();
    List<NotaryAccount> notaryies = ethNotaryService.getNotaries();
    notaryies.forEach(
        n -> n.withdrawConfirmTransaction(burnPorposalId, amountOnSideChain, txOnSideChain));
  }

  @SneakyThrows
  public void run() {
    logger.info("start run EthWithdrawListener");
    while (isRunning) {
      Thread.sleep(3000);
      long recordOffset = ethBurnInfoMapper.getCurRecord() + 1;
      long nowOffset = ethNotaryService.getEthSender().getOffsetData();
      while (recordOffset < nowOffset) {
        logger.info("Start to Consume : recordOffset {} : nowOffset {}",
            recordOffset, nowOffset);
        long curOffset = recordOffset;
        EthBurnInfo ethBurnInfo = ethNotaryService.getBurnInfo(curOffset);
        String recipient = ethBurnInfo.getRecipient();
        long amount = ethBurnInfo.getAmount();
        long amountOnSideChain = amount;
        long approve = ethBurnInfo.getApprove();

        // fixme: 之前失败的，应该在进函数之前处理好了
        // if (ethBurnInfo == null) {}
        // EthBurnInfo ethBurnInfo = ethBurnInfoMapper.selectById(curOffset);

        ethBurnInfo.setAmountOnSideChain(amountOnSideChain);
        logger.info("Insert new BurnInfo curOffset({}) recipient({}), amount({})",
            curOffset, recipient, amount);
        ethBurnInfoMapper.insert(ethBurnInfo);
        //send trx when status is init or failed (no_verify not send)
        // todo: init, fail will not retry when restart
        // fixme: always true
        // if (ethBurnInfo.getStatus().equals(ConstSetting.WITHDRAW_INIT)) {}

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

        // fixme: there is not "id" in dto
        ethBurnInfoMapper.updateById(ethBurnInfo);
        
        EthVerifyData ethVerifyData = EthVerifyData.builder()
            .recipient(recipient)
            .txOnSideChain(txId)
            .burnProposalId(curOffset)
            .amountOnSideChain(amountOnSideChain)
            .amount(amount)
            .approve(approve)
            .timestamp(System.currentTimeMillis() / 1_000)
            .build();
        ethVerifyTrxTransPool.produce(ethVerifyData);

        recordOffset++;
      }
    }
  }
}

