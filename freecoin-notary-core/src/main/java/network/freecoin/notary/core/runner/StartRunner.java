package network.freecoin.notary.core.runner;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import network.freecoin.notary.core.common.TronNotaryAddressPool;
import network.freecoin.notary.core.common.config.ConstSetting;
import network.freecoin.notary.core.dao.entity.TronDepositAddress;
import network.freecoin.notary.core.dao.entity.TronDepositMeta;
import network.freecoin.notary.core.dao.mapper.TronDepositAddressMapper;
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
  private TronDepositAddressMapper tronDepositAddressMapper;
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
    List<TronDepositAddress> depositAddressList = tronDepositAddressMapper
        .selectList(null);
    depositAddressList.forEach(
        tronDepositAddress -> tronNotaryAddressPool.addOne(tronDepositAddress.getAddress()));
    TronDepositMeta tronDepositMeta = tronDepositMetaMapper
        .selectById(ConstSetting.TRON_DEPOSIT_META_ID);
    tronDepositListener.run(tronDepositMeta.getBlockNum(),
        tronDepositMeta.getMintProposalId());
  }
}