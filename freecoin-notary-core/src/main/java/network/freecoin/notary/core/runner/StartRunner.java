package network.freecoin.notary.core.runner;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class StartRunner implements ApplicationRunner {

  @Override
  public void run(ApplicationArguments applicationArguments) {
    // todo:
    //  1. handle tx in db
    //  2. start listener
  }
}