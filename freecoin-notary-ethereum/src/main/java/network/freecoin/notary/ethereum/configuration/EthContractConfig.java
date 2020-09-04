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
@ConfigurationProperties(prefix = "eth.contract" , ignoreInvalidFields = true)
public class EthContractConfig {

    private String address;
}
