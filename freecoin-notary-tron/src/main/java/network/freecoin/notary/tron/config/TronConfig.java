package network.freecoin.notary.tron.config;

import java.util.List;
import javax.annotation.PostConstruct;
import lombok.Data;
import network.freecoin.notary.tron.common.utils.WalletUtil;
import org.spongycastle.util.encoders.Hex;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "tron")
public class TronConfig {

  private List<String> fullnode;
  private String privateKeyHex;
  private String contractBase58Check;
  private byte[] privateKey;
  private byte[] contract;

  @PostConstruct
  private void init() {
    privateKey = Hex.decode(privateKeyHex);
    contract = WalletUtil.decodeFromBase58Check(contractBase58Check);
  }

}