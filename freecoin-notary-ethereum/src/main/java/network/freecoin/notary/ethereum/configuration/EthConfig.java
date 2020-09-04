package network.freecoin.notary.ethereum.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

import java.math.BigInteger;


@Data
@Configuration
@ConfigurationProperties(prefix = "eth", ignoreInvalidFields = true)
public class EthConfig {
    private String service;
    private BigInteger gaslimit;
    private int singer;


    @Bean
    public Web3j web3j() {
        return Web3j.build(new HttpService(service));
    }
}