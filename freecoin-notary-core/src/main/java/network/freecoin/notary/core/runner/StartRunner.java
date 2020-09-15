package network.freecoin.notary.core.runner;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import network.freecoin.notary.core.common.TronNotaryAddressPool;
import network.freecoin.notary.core.common.config.ConstSetting;
import network.freecoin.notary.core.dao.entity.TronDeposit;
import network.freecoin.notary.core.dao.entity.TronDepositAddress;
import network.freecoin.notary.core.dao.entity.TronDepositMeta;
import network.freecoin.notary.core.dao.mapper.TronDepositAddressMapper;
import network.freecoin.notary.core.dao.mapper.TronDepositMapper;
import network.freecoin.notary.core.dao.mapper.TronDepositMetaMapper;
import network.freecoin.notary.core.listener.EthMinter;
import network.freecoin.notary.core.listener.TronDepositHandler;
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
  private TronDepositMapper tronDepositMapper;
  @Autowired
  private TronDepositAddressMapper tronDepositAddressMapper;
  @Autowired
  private EthMinter ethMinter;
  @Autowired
  private TronNotaryAddressPool tronNotaryAddressPool;
  @Autowired
  private TronDepositHandler tronDepositHandler;

  @Override
  public void run(ApplicationArguments applicationArguments) {
    // todo: add from eth contract
    List<TronDepositAddress> depositAddressList = tronDepositAddressMapper
        .selectList(null);
    depositAddressList.forEach(
        tronDepositAddress -> tronNotaryAddressPool.addOne(tronDepositAddress.getAddress()));
    TronDepositMeta tronDepositMeta = tronDepositMetaMapper
        .selectById(ConstSetting.TRON_DEPOSIT_META_ID);

    List<TronDeposit> tronDepositList = tronDepositMapper.selectToHandleList();
    for (TronDeposit data : tronDepositList) {
      tronDepositHandler.handleTx(data.getSenderOnSideChain(), data.getAmount(),
          data.getTxOnSideChain(), data.getBlockNum());
    }

    long fromBlockNum;
    int fromTxIndex;
    TronDeposit lastTronDeposit = tronDepositMapper.selectLastDeposit();
    if (lastTronDeposit == null || lastTronDeposit.getBlockNum() < tronDepositMeta.getBlockNum()) {
      fromBlockNum = tronDepositMeta.getBlockNum();
      fromTxIndex = tronDepositMeta.getTxIndexOnSideChain();
    } else {
      fromBlockNum = lastTronDeposit.getBlockNum();
      fromTxIndex = lastTronDeposit.getTxIndexOnSideChain() + 1;
    }

    tronDepositListener.run(fromBlockNum, fromTxIndex);
  }
}