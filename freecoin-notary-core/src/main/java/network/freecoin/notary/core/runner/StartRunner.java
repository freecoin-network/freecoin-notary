package network.freecoin.notary.core.runner;

import java.util.Collections;
import lombok.extern.slf4j.Slf4j;
import network.freecoin.notary.core.common.TronNotaryAddressPool;
import network.freecoin.notary.core.common.config.ConstSetting;
import network.freecoin.notary.core.dao.entity.TronDepositMeta;
import network.freecoin.notary.core.dao.mapper.TronDepositMetaMapper;
import network.freecoin.notary.core.listener.EthMinter;
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
  @Autowired
  private EthMinter ethMinter;
  @Autowired
  private TronNotaryAddressPool tronNotaryAddressPool;

  @Override
  public void run(ApplicationArguments applicationArguments) {
    // todo:
    //  1. handle tx in db
    //  2. start listener
    // todo: add from eth contract
    tronNotaryAddressPool.add(Collections.singletonList("TPynyGFnMP64p4vbjFbDrzqNBUEJvaErDT"));
    ethMinter.run();
    TronDepositMeta tronDepositMeta = tronDepositMetaMapper.selectById(ConstSetting.TRON_DEPOSIT_META_ID);
    tronDepositListener.run(tronDepositMeta.getBlockNum(),
        tronDepositMeta.getMintProposalId());
  }
}