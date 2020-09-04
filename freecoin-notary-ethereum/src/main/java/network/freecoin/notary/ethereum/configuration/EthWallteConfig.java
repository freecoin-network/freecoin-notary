package network.freecoin.notary.ethereum.configuration;

import lombok.Data;
import lombok.SneakyThrows;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;

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
        if (dir.exists() && !dir.isDirectory()) return null;
        File[] files = dir.listFiles();
        for (File f : files) {
            Credentials credentials = WalletUtils.loadCredentials("fcn12#$", f);
            wallets.add(credentials);
        }

        return Wallets.builder().walletList(wallets).build();
    }
}
