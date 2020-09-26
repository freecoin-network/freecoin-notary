package network.freecoin.notary.core.listener;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import network.freecoin.notary.core.common.EthVerifyTrxTransPool;
import network.freecoin.notary.core.common.config.ConstSetting;
import network.freecoin.notary.core.dao.entity.EthBurnInfo;
import network.freecoin.notary.core.dao.mapper.EthBurnInfoMapper;
import network.freecoin.notary.core.service.EthNotaryService;
import network.freecoin.notary.ethereum.entity.NotaryAccount;
import network.freecoin.notary.tron.protos.Protocol;
import network.freecoin.notary.tron.service.BlockInfoService;
import network.freecoin.notary.tron.service.NormalTxService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

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

    private volatile boolean isRunning;

    public EthWithdrawListener() {
        this.isRunning = true;
    }

    public void withdrawConfirm(EthBurnInfo ethBurnInfo) {
        long burnPorposalId = ethBurnInfo.getBurnProposalId();
        long amountOnSideChain = ethBurnInfo.getAmountOnSideChain();
        String txOnSideChain = ethBurnInfo.getTxOnSideChain();
        List<NotaryAccount> notaryies = ethNotaryService.getNotaries();
        notaryies.forEach(n -> n.withdrawConfirmTransaction(burnPorposalId, amountOnSideChain, txOnSideChain));
    }

    public boolean verifyTrxTrans(String txId){
        Protocol.TransactionInfo txinfo = blockInfoService.getTransactionInfoById(txId);
        boolean result = (txinfo.getResult() == Protocol.TransactionInfo.code.SUCESS);
        //todo 未确认 看下要不要挪到 trx的service里面
        return true;
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
                EthBurnInfo e = ethNotaryService.getBurnInfo(curOffset);
                String recipient = e.getRecipient();
                long amount = e.getAmount();
                long amountOnSideChain = e.getAmountOnSideChain();

                logger.info("Send {} TRX to {}", amount, recipient);
                EthBurnInfo ethBurnInfo = ethBurnInfoMapper.selectById(curOffset);
                if(ethBurnInfo == null) {
                    ethBurnInfo = EthBurnInfo.builder()
                            .recipient(recipient)
                            .approve(0L)
                            .amountOnSideChain(amountOnSideChain)
                            .amount(amount)
                            .burnProposalId(curOffset)
                            .status(ConstSetting.WITHDRAW_INIT)
                            .build();
                    logger.info("Insert new BurnInfo curOffset({}) recipient({}), amount({})",
                            curOffset, recipient, amount);
                    ethBurnInfoMapper.insert(ethBurnInfo);
                }
                //send trx when status is init or failed (no_verify not send)
                if(ethBurnInfo.getStatus().equals(ConstSetting.WITHDRAW_INIT)){
                    //todo trx sendTransaction
                    //String txId = normalTxServic e.sendTrx(to, amooutOnSideChain);
                    String txId = "270e76453bccfa1ad0c9a1f84cc5b110c74f3692d48bc23c66a96d39b89";
                    ethBurnInfo.setTxOnSideChain(txId);
                    ethBurnInfo.setStatus(ConstSetting.WITHDRAW_NO_TRX_VERIFY);
                    ethBurnInfoMapper.updateById(ethBurnInfo);
                }
                //todo 发到
                ethVerifyTrxTransPool.produce(ethBurnInfo);
                recordOffset++;
            }
        }
    }
}

