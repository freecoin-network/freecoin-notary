package network.freecoin.notary.tron.service;

import java.util.Optional;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import network.freecoin.notary.tron.api.GrpcAPI.BlockExtention;
import network.freecoin.notary.tron.client.WalletClient;
import network.freecoin.notary.tron.config.TronConfig;
import network.freecoin.notary.tron.protos.Protocol.Block;
import network.freecoin.notary.tron.protos.Protocol.TransactionInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class BlockInfoService {

  private WalletClient normalWallet;
  @Autowired
  private TronConfig tronConfig;
  private int fullNodeIndex = 0;

  @PostConstruct
  private void initWallet() {
    String fullNode = tronConfig.getFullnode().get(fullNodeIndex);
    fullNodeIndex = (fullNodeIndex + 1) % tronConfig.getFullnode().size();
    if (normalWallet == null) {
      logger.info("connect to fullNode[" + fullNode + "]");
    } else {
      logger.warn("switch fullNode to [" + fullNode + "]");
    }
    normalWallet = new WalletClient(fullNode, tronConfig.getPrivateKey());
  }

  public TransactionInfo getTransactionInfoById(String txID) {
    Optional<TransactionInfo> transactionInfoOptional = normalWallet.getTransactionInfoById(txID);
    return transactionInfoOptional.orElse(null);
  }

  public Block getBlockByNum(long blockNum) {
    Optional<Block> blockOptional = normalWallet.getBlockByNum(blockNum);
    return blockOptional.orElse(null);
  }
}