package network.freecoin.notary.core.handler;


import lombok.extern.slf4j.Slf4j;
import network.freecoin.notary.core.common.EthWithdrawConfirmPool;
import network.freecoin.notary.core.common.config.ConstSetting;
import network.freecoin.notary.core.dao.entity.EthBurnInfo;
import network.freecoin.notary.core.dao.mapper.EthBurnInfoMapper;
import network.freecoin.notary.core.service.EthNotaryService;
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

    private boolean isRunning;

    public EthWithdrawConfirmHandler() {
        this.isRunning = true;
    }

    public void run(){
        logger.info("Start EthWithdrawConfirmHandler");
        while(isRunning) {
            EthBurnInfo e = ethWithdrawConfirmPool.consume();
            boolean ret = ethNotaryService.getBurnStatus(e.getBurnProposalId());
            if(ret) {
                //提现成功
                e.setStatus(ConstSetting.WITHDRAW_SUCCESS);
                ethBurnInfoMapper.updateById(e);
            }
            else {
                //提现失败，进行重试
                e.setStatus(ConstSetting.WITHDRAW_ETH_CONFIRM_FAILED);
                ethBurnInfoMapper.updateById(e);
                ethWithdrawConfirmPool.produce(e);
            }

        }
    }

    public void stop(){
        this.isRunning = false;
    }
}
