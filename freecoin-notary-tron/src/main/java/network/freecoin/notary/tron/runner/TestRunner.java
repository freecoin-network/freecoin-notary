package network.freecoin.notary.tron.runner;

import lombok.extern.slf4j.Slf4j;
import network.freecoin.notary.tron.service.NormalTxService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TestRunner implements ApplicationRunner {
  @Autowired
  private NormalTxService normalTxService;

  @Override
  public void run(ApplicationArguments applicationArguments) {
    String txId = normalTxService.sendTrx("TPynyGFnMP64p4vbjFbDrzqNBUEJvaErDT", 1000);
    logger.info("txId: {}", txId);
  }
}
