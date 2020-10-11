package network.freecoin.notary.core.common;


import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import lombok.extern.slf4j.Slf4j;
import network.freecoin.notary.core.dto.EthVerifyData;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class EthWithdrawConfirmPool {

  private BlockingQueue<EthVerifyData> queue;

  public EthWithdrawConfirmPool() {
    this.queue = new LinkedBlockingQueue<>();
  }

  public void produce(EthVerifyData ethVerifyData) {
    try {
      queue.put(ethVerifyData);
    } catch (InterruptedException e) {
      logger.warn("interrupted exception", e);
      Thread.currentThread().interrupt();
    }
  }

  public EthVerifyData consume() {
    try {
      return queue.take();
    } catch (InterruptedException e) {
      logger.warn("interrupted exception", e);
      Thread.currentThread().interrupt();
    }
    return null;
  }
}
