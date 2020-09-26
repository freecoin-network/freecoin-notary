package network.freecoin.notary.core.common;


import lombok.extern.slf4j.Slf4j;
import network.freecoin.notary.core.dao.entity.EthBurnInfo;
import org.springframework.stereotype.Component;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Component
@Slf4j
public class EthVerifyTrxTransPool {
    private BlockingQueue<EthBurnInfo> queue;

    public EthVerifyTrxTransPool(){
        this.queue = new LinkedBlockingQueue<>();
    }

    public void produce(EthBurnInfo ethBurnInfo){
        try {
            queue.put(ethBurnInfo);
        } catch (InterruptedException e) {
            logger.warn("interrupted exception", e);
            Thread.currentThread().interrupt();
        }
    }

    public EthBurnInfo consume(){
        try {
            return queue.take();
        } catch (InterruptedException e) {
            logger.warn("interrupted exception", e);
            Thread.currentThread().interrupt();
        }
        return null;
    }
}
