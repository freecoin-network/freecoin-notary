package network.freecoin.notary.core.runner;

import lombok.extern.slf4j.Slf4j;
import network.freecoin.notary.core.dao.entity.TronDepositMeta;
import network.freecoin.notary.core.dao.mapper.TronDepositMetaMapper;
import network.freecoin.notary.core.listener.TronDepositListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class StartRunner implements ApplicationRunner {

  @Autowired
  private TronDepositListener tronDepositListener;
  @Autowired
  private TronDepositMetaMapper tronDepositMetaMapper;

  @Override
  public void run(ApplicationArguments applicationArguments) {
    // todo:
    //  1. handle tx in db
    //  2. start listener
    TronDepositMeta tronDepositMeta = tronDepositMetaMapper.selectById(1);
    tronDepositListener.run(tronDepositMeta.getBlockNum(),
        tronDepositMeta.getMintProposalId());
  }
}