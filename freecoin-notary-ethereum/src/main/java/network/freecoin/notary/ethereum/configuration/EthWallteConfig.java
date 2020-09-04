package network.freecoin.notary.ethereum.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author pengyuxiang
 * @date 2020/9/4
 */

@Data
@Configuration
@ConfigurationProperties(prefix = "eth.wallet", ignoreInvalidFields = true)
public class EthWallteConfig {
    public String address;

}
