package network.freecoin.notary.tron.runner;

import lombok.extern.slf4j.Slf4j;
import network.freecoin.notary.tron.common.utils.Sha256Hash;
import network.freecoin.notary.tron.protos.Protocol.Block;
import network.freecoin.notary.tron.protos.Protocol.Transaction;
import network.freecoin.notary.tron.protos.Protocol.TransactionInfo;
import network.freecoin.notary.tron.service.BlockInfoService;
import network.freecoin.notary.tron.service.NormalTxService;
import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TestRunner implements ApplicationRunner {

  @Autowired
  private NormalTxService normalTxService;
  @Autowired
  private BlockInfoService blockInfoService;

  @Override
  public void run(ApplicationArguments applicationArguments) {
    // String txId = normalTxService.sendTrx("TPynyGFnMP64p4vbjFbDrzqNBUEJvaErDT", 1000);
    // logger.info("txId: {}", txId);

    Block block = blockInfoService.getBlockByNum(22953366);
    logger.info("block: {}", block);
    Transaction transaction = block.getTransactions(0);
    String txId = Hex.toHexString(Sha256Hash.of(transaction.getRawData().toByteArray()).getBytes());
    TransactionInfo transactionInfo = blockInfoService.getTransactionInfoById(txId);
    logger.info("transactionInfo: {}", transactionInfo);
  }
}
