package network.freecoin.notary.core.runner;

import lombok.extern.slf4j.Slf4j;
import network.freecoin.notary.core.listener.EthMinter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class EthStartRunner implements ApplicationRunner {

    @Autowired
    private EthMinter ethMinter;

    @Override
    public void run(ApplicationArguments applicationArguments) {
        new Thread(() -> ethMinter.run()).start();
        logger.info("end ethStartRunner");
    }
}