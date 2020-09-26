package network.freecoin.notary.core.service;


import lombok.Data;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import network.freecoin.notary.core.dao.entity.EthBurnInfo;
import network.freecoin.notary.core.dao.mapper.EthNotaryMapper;
import network.freecoin.notary.ethereum.configuration.ConstSetting;
import network.freecoin.notary.ethereum.configuration.EthContractConfig;
import network.freecoin.notary.ethereum.configuration.InputBuilder;
import network.freecoin.notary.ethereum.configuration.OutputBuilder;
import network.freecoin.notary.ethereum.entity.NotaryAccount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Type;
import org.web3j.crypto.Credentials;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
@Data
public class EthNotaryService {

    @Autowired
    private EthContractConfig ethContractConfig;

    @Autowired
    private EthNotaryMapper ethNotaryMapper;

    @Getter
    private List<NotaryAccount> notaries;

    @Getter
    private NotaryAccount ethSender;

    public void refresh() {
        this.notaries = refreshNotaries();
        this.ethSender = refreshSender();
    }

    private List<NotaryAccount> refreshNotaries() {
        List<String> crs = ethNotaryMapper.getNotary();
        List<NotaryAccount> notaries = new ArrayList<>();
        crs.forEach(c -> {
            NotaryAccount account = new NotaryAccount(Credentials.create(c));
            account.setGasLimit(BigInteger.valueOf(210000));
            account.setTo(ethContractConfig.getAddress());
            notaries.add(account);
        });
        return notaries;
    }

    private NotaryAccount refreshSender() {
        String s = ethNotaryMapper.getSender();
        this.ethSender = new NotaryAccount(Credentials.create(s), ethContractConfig.getAddress());
        this.ethSender.setGasLimit(BigInteger.valueOf(210000));
        return this.ethSender;
    }

    @SneakyThrows
    public EthBurnInfo getBurnInfo(long burnProposalId) {
        String to = ethContractConfig.getAddress();
        List<Type> input = InputBuilder.build().addUInt256(burnProposalId).get();
        List<TypeReference<?>> output = OutputBuilder.build()
                .addAddress().addUint256().addUtf8String()
                .addUint256().addUtf8String().addUint8().addBool().get();
        List resolved = ethSender.call(to, ConstSetting.GET_BURN_INFO, input, output);
        long amount = (long) resolved.get(1);
        String recipient = (String) resolved.get(2);
        long amoutOnSideChain = (long) resolved.get(3);
        String txOnSideChain = (String) resolved.get(4);
        long approve = (long) resolved.get(5);
        boolean success = (boolean) resolved.get(6);
        String status = success ? "1" : "0";
        return EthBurnInfo.builder()
                .amount(amount)
                .recipient(recipient)
                .amountOnSideChain(amoutOnSideChain)
                .txOnSideChain(txOnSideChain)
                .approve(approve)
                .status(status)
                .build();
    }

    public boolean verifyMint(String mintProposalId){
        return ethSender.verifyMintTransaction(mintProposalId);
    }

    public String withdrawConfirmTransaction(long burnProposalId, long amountOnSideChain, String txOnSideChain) {
        return ethSender.withdrawConfirmTransaction(burnProposalId, amountOnSideChain, txOnSideChain);
    }

    public boolean getBurnStatus(long burnProposalId){
        return ethSender.getBurnStatus(burnProposalId);
    }


}
