package network.freecoin.notary.tron.service;

import io.grpc.NameResolver.Args;
import java.util.List;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import network.freecoin.notary.tron.client.WalletClient;
import network.freecoin.notary.tron.config.TronConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class NormalTxService {

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

  public String sendTrx(String to, long amount) {
    return normalWallet.sendTrx(to, amount);
  }
}