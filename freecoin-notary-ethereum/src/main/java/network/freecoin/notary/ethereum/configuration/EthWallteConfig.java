package network.freecoin.notary.ethereum.configuration;

import lombok.Data;
import lombok.SneakyThrows;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.web3j.crypto.Credentials;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author pengyuxiang
 * @date 2020/9/4
 */

@Data
@Configuration
@ConfigurationProperties(prefix = "eth.wallet", ignoreInvalidFields = true)
public class EthWallteConfig {
    public String path;

    @SneakyThrows
    @Bean
    public Wallets wallets() {
        File dir = new File(path);
        List<Credentials> wallets = new ArrayList<>();
        wallets.add(Credentials.create("c8b3a74931b12bbabb1c67e7116d8adeda02f575c5d3b8571328b00ff29d61ab"));
        wallets.add(Credentials.create("7a9f84511df4138739766e4dafe66869cd3b8fbd7f424b6e241427c0011bfa5f"));
        wallets.add(Credentials.create("51869ca806bc0e6d7415c79a3193a82898975aff063fb5b946e4e72f5d03078d"));
        return Wallets.builder().walletList(wallets).build();
    }
}
