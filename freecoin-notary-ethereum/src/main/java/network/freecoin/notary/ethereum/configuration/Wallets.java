package network.freecoin.notary.ethereum.configuration;

import lombok.Builder;
import lombok.Data;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.Wallet;

import java.util.List;

/**
 * @author pengyuxiang
 * @date 2020/9/4
 */

@Builder
@Data
public class Wallets {

    List<Credentials> walletList;

    Credentials credentials;
}
