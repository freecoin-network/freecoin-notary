package network.freecoin.notary.ethereum.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.math.BigInteger;


@Data
@Configuration
@ConfigurationProperties(prefix = "eth", ignoreInvalidFields = true)
public class EthConfig {
    private String service;
    private BigInteger gaslimit;
    private String pub;
    private String pri;

}