package network.freecoin.notary.core.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AlertHandler {

  public void sendAlert(String msg, Throwable t) {
    logger.error("FAIL ALERT: [{}]", msg, t);
  }
}