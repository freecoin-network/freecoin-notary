package network.freecoin.notary.core.service;


import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import network.freecoin.notary.core.dao.entity.EthBurnInfo;
import network.freecoin.notary.core.dao.entity.TronDeposit;
import network.freecoin.notary.ethereum.configuration.*;
import network.freecoin.notary.ethereum.services.EthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Type;
import org.web3j.crypto.Credentials;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@Slf4j
public class EthNotaryService {

    @Autowired
    private EthService ethService;

    @Autowired
    private EthWallteConfig ethWallteConfig;

    @Autowired
    private EthContractConfig ethContractConfig;

    @SneakyThrows
    public EthBurnInfo getBurnInfo(long burnProposalId) {
        Credentials c = ethWallteConfig.wallets().getCredentials();
        String to = ethContractConfig.getAddress();
        List<Type> input = InputBuilder.build().addUInt256(burnProposalId).get();
        List<TypeReference<?>> output = OutputBuilder.build()
                .addAddress().addUint256().addUtf8String()
                .addUint256().addUtf8String().addUint8().addBool().get();
        List<Type> ret = ethService.callTransaction(c, to, ConstSetting.GET_BURN_INFO, input, output);
        List resolved = ethService.resolve(ret);
        String addr = resolved.get(0).toString();
        long amount = (long) resolved.get(1);
        String recipient = (String) resolved.get(2);
        long amoutOnSideChain = (long) resolved.get(3);
        String txOnSideChain = (String) resolved.get(4);
        long approve = (long) resolved.get(5);
        boolean success = (boolean) resolved.get(6);
        String status = success ? "1" : "2";
        Map<String, Object> m = new HashMap<>();
        EthBurnInfo ethBurnInfo = EthBurnInfo.builder()
                .amount(amount)
                .recipient(recipient)
                .amountOnSideChain(amoutOnSideChain)
                .txOnSideChain(txOnSideChain)
                .approve(approve)
                .status(status)
                .build();

        return ethBurnInfo;
    }

}
