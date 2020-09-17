package network.freecoin.notary.core.listener;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import network.freecoin.notary.core.dao.entity.EthBurnInfo;
import network.freecoin.notary.core.dao.mapper.EthBurnInfoMapper;
import network.freecoin.notary.ethereum.services.EthService;
import network.freecoin.notary.tron.protos.Protocol;
import network.freecoin.notary.tron.service.BlockInfoService;
import network.freecoin.notary.tron.service.NormalTxService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;

import java.util.List;
import java.util.Map;

/**
 * @author pengyuxiang
 * @date 2020/9/13
 */


@Component
@Slf4j
public class EthWithdrawListener {


    @Autowired
    private Web3j web3j;
    @Autowired
    private EthBurnInfoMapper ethBurnInfoMapper;
    @Autowired
    private EthService ethService;
    @Autowired
    private NormalTxService normalTxService;
    @Autowired
    private BlockInfoService blockInfoService;

    private volatile boolean isRunning;

    public EthWithdrawListener() {
        this.isRunning = true;
    }


    public void withdrawConfirm(EthBurnInfo ethBurnInfo) {
        long burnPorposalId = ethBurnInfo.getBurnProposalId();
        long amountOnSideChain = ethBurnInfo.getAmountOnSideChain();
        String txOnSideChain = ethBurnInfo.getTxSidechain();
        List<Credentials> crs = ethService.genSinger();
        crs.stream().forEach(cr -> {
            ethService.withdrawConfirmTransaction(cr, burnPorposalId, amountOnSideChain, txOnSideChain);
        });
    }

    public boolean verifyTrxTrans(String txId){
        Protocol.TransactionInfo txinfo = blockInfoService.getTransactionInfoById(txId);
        boolean result = (txinfo.getResult() == Protocol.TransactionInfo.code.SUCESS);
        //todo 未确认
        return true;
    }

    @SneakyThrows
    public void run() {
        logger.info("start run EthWithdrawListener {}");
        while (isRunning) {
            Thread.sleep(3000);
            long recordOffset = ethBurnInfoMapper.getCurRecord() + 1;
            long nowOffset = ethService.getOffsetData();
            while (recordOffset < nowOffset) {
                logger.info("Start to Consume : recordOffset {} : nowOffset {}",
                        recordOffset, nowOffset);
                long curOffset = recordOffset;
                Map<String, Object> m = ethService.getBurnInfo(curOffset);
                String to = (String) m.get("recipient");
                long amount = (long) m.get("amount");
                String txOnSideChain = (String) m.get("txOnSideChain");

                logger.info("Send {} TRX to {}", amount, to);
                EthBurnInfo ethBurnInfo = EthBurnInfo.builder().
                        recipient(to).amount(amount).burnProposalId(curOffset).success("0").build();
                ethBurnInfoMapper.insert(ethBurnInfo);
                //todo trx sendTransaction
                //String txId = normalTxService.sendTrx(to, amooutOnSideChain);
                ethBurnInfo.setSuccess("1");
                ethBurnInfo.setId(0);
                ethBurnInfoMapper.updateById(ethBurnInfo);
                recordOffset++;
                String txId = "txid";

                ethBurnInfo.setAmountOnSideChain(amount);
                ethBurnInfo.setTxSidechain(txId);
                //todo getBurnStatus
                Thread.sleep(60000);
                if(verifyTrxTrans(txId)) {
                    //todo success
                    withdrawConfirm(ethBurnInfo);
                }
                else {
                    //todo failed
                    logger.error("Send Trx Transaction failed txId {}, burnProposalId {}",
                            txId, ethBurnInfo.getBurnProposalId());
                }

            }
        }
    }
}

