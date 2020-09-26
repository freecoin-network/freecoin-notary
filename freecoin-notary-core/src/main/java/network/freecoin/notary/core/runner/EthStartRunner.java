package network.freecoin.notary.core.runner;

import lombok.extern.slf4j.Slf4j;
import network.freecoin.notary.core.handler.EthWithdrawConfirmHandler;
import network.freecoin.notary.core.handler.EthWithdrawVerifyHandler;
import network.freecoin.notary.core.listener.EthMintListener;
import network.freecoin.notary.core.listener.EthWithdrawListener;
import network.freecoin.notary.core.service.EthNotaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class EthStartRunner implements ApplicationRunner {

    @Autowired
    private EthMintListener ethMintListener;
    @Autowired
    private EthWithdrawListener ethWithdrawListener;
    @Autowired
    private EthWithdrawConfirmHandler ethWithdrawConfirmHandler;
    @Autowired
    private EthWithdrawVerifyHandler ethWithdrawVerifyHandler;
    @Autowired
    private EthNotaryService ethNotaryService;

    @Override
    public void run(ApplicationArguments applicationArguments) {
        ethNotaryService.refresh();
        new Thread(() -> ethMintListener.run()).start();
        new Thread(() -> ethWithdrawListener.run()).start();
        new Thread(() -> ethWithdrawConfirmHandler.run()).start();
        new Thread(() -> ethWithdrawVerifyHandler.run()).start();
        logger.info("end ethStartRunner");
    }
}