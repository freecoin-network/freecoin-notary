package network.freecoin.notary.core.listener;

import lombok.extern.slf4j.Slf4j;
import network.freecoin.notary.core.common.TronDepositPool;
import network.freecoin.notary.core.dto.DepositData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TronDepositHandler {

  @Autowired
  private TronDepositPool tronDepositPool;
  @Autowired
  private EthMinter ethMinter;

  public void handleTx(String sender, long amount, String txId, long blockNum,
      long mintProposalId) {

    ethMinter.mint(mintProposalId, sender, amount, txId);

    DepositData depositData = DepositData.builder()
        .blockNum(blockNum)
        .mintProposalId(mintProposalId)
        .senderOnSideChain(sender)
        .amount(amount)
        .txOnSideChain(txId)
        .build();
    tronDepositPool.produce(depositData);
  }
}