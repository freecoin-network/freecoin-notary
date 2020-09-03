package network.freecoin.notary.ethereum.configuration;

import lombok.Data;
import network.freecoin.notary.tron.common.utils.WalletUtil;
import org.spongycastle.util.encoders.Hex;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "eth")
public class EthConfig {

  private String service;

}