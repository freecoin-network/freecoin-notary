package network.freecoin.notary.core.common;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import lombok.extern.slf4j.Slf4j;
import network.freecoin.notary.core.dto.DepositData;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TronDepositPool {

  private BlockingQueue<DepositData> queue;

  public TronDepositPool() {
    this.queue = new LinkedBlockingQueue<>();
  }

  public void produce(DepositData depositData) {
    try {
      queue.put(depositData);
    } catch (InterruptedException e) {
      logger.warn("interrupted exception", e);
      Thread.currentThread().interrupt();
    }
  }

  public DepositData consume() {
    try {
      return queue.take();
    } catch (InterruptedException e) {
      logger.warn("interrupted exception", e);
      Thread.currentThread().interrupt();
    }
    return null;
  }
}
