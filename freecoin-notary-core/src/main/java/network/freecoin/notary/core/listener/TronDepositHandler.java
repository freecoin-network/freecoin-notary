package network.freecoin.notary.core.listener;

import lombok.extern.slf4j.Slf4j;
import network.freecoin.notary.core.common.TronDepositPool;
import network.freecoin.notary.core.dao.entity.TronDeposit;
import network.freecoin.notary.core.dto.DepositData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TronDepositHandler {

  @Autowired
  private TronDepositPool tronDepositPool;
  @Autowired
  private EthMintListener ethMintListener;

  public void handleTx(String sender, long amount, String txId, long blockNum) {

    TronDeposit tronDeposit = TronDeposit.builder()
        .senderOnSideChain(sender)
        .amount(amount)
        .txOnSideChain(txId)
        .build();
    ethMintListener.mint(tronDeposit);

    DepositData depositData = DepositData.builder()
        .blockNum(blockNum)
        .senderOnSideChain(sender)
        .amount(amount)
        .txOnSideChain(txId)
        .build();
    tronDepositPool.produce(depositData);
  }
}