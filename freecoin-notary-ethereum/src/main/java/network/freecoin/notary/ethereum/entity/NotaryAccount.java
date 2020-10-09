package network.freecoin.notary.ethereum.entity;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import network.freecoin.notary.ethereum.configuration.ConstSetting;
import network.freecoin.notary.ethereum.configuration.InputBuilder;
import network.freecoin.notary.ethereum.configuration.OutputBuilder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Type;
import org.web3j.crypto.Credentials;

import java.util.List;


@Slf4j
@Data
public class NotaryAccount extends EthAccount{

    private String to;

    public NotaryAccount(Credentials c) {
        super(c);
    }
    public NotaryAccount(Credentials c,String to) {
        super(c);
        this.to = to;
    }

    /**
     * mint - depositeConfirm
     * @param txOnSideChain
     */
    public void depositConfirm(String txSender, long amount, String txOnSideChain) {
        logger.info("[DepositConfirm] CreAddr {},  txSender {}, amount {}, txOnSideChain {} ",
                c.getAddress(), txSender, amount, txOnSideChain);
        List<Type> input = InputBuilder.build()
                .addUtf8String(txOnSideChain).addUtf8String(txSender)
                .addUInt256(amount).addUInt256(amount).get();
        List<TypeReference<?>> output = OutputBuilder.build().addBool().get();
        String hash = this.send(to, ConstSetting.DEPOSIT_CONFIRM, input, output);
        logger.info("[DepositConfirm] tx-hash : {}", hash);
    }

    /**
     * mint - verify mint
     * @param txOnSideChain
     * @return
     */
    public boolean verifyMintTransaction(String txOnSideChain) {
        List<Type> input = InputBuilder.build().addUtf8String(txOnSideChain).get();
        List<TypeReference<?>> output = OutputBuilder.build().addBool().get();
        List<Object> ret = call(to, ConstSetting.VERIFY_MINT, input, output);
        if(ret == null || ret.size() == 0) return false;
        return (boolean) ret.get(0);
    }

    /**
     * withdraw - getOffsetData
     * @return
     */
    public long getOffsetData() {
        List<Type> input = InputBuilder.build().get();
        List<TypeReference<?>> output = OutputBuilder.build().addUint256().get();
        List ret = call(to, ConstSetting.GET_BURN_OFFSET, input, output);
        return (long) ret.get(0);
    }

    /**
     * withdraw confirm transaction
     * @param burnProposalId
     * @param amountOnSideChain
     * @param txOnSideChain
     */
    public String withdrawConfirmTransaction(long burnProposalId, long amountOnSideChain, String txOnSideChain) {
        logger.info("[WithdrawConfirm]({}) transaction burnPId({}), amoutOnSideChain({}), txOnSideChain({})",
                c.getAddress(), burnProposalId, amountOnSideChain, txOnSideChain);
        List<Type> input = InputBuilder.build()
                .addUInt256(burnProposalId)
                .addUInt256(amountOnSideChain)
                .addUtf8String(txOnSideChain)
                .get();
        List<TypeReference<?>> output = OutputBuilder.build().addBool().get();
        String txId = send(to, ConstSetting.WITHDRAW_CONFIRM, input, output);
        logger.info("[WithdrawConfirm] SendRawTransaction Result : {}", txId);
        return txId;
    }

    /**
     * withdraw getBurnStatus
     * @param burnProposalId
     * @return
     */
    public boolean getBurnStatus(long burnProposalId){
        logger.info("[getBurnStatus] burnProposalId {}", burnProposalId);
        List<Type> input = InputBuilder.build().addUInt256(burnProposalId).get();
        List<TypeReference<?>> output = OutputBuilder.build().addBool().get();
        List<Object> ret = call(to, ConstSetting.GET_BURN_STATUS, input, output);
        return (boolean) ret.get(0);
    }
}
