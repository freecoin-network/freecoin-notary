package network.freecoin.notary.core.common.utils;

import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import lombok.extern.slf4j.Slf4j;
import network.freecoin.notary.tron.common.utils.Sha256Hash;
import network.freecoin.notary.tron.common.utils.WalletUtil;
import network.freecoin.notary.tron.protos.Contract;
import network.freecoin.notary.tron.protos.Contract.AccountCreateContract;
import network.freecoin.notary.tron.protos.Contract.AccountUpdateContract;
import network.freecoin.notary.tron.protos.Contract.AssetIssueContract;
import network.freecoin.notary.tron.protos.Contract.CreateSmartContract;
import network.freecoin.notary.tron.protos.Contract.ExchangeCreateContract;
import network.freecoin.notary.tron.protos.Contract.ExchangeInjectContract;
import network.freecoin.notary.tron.protos.Contract.ExchangeTransactionContract;
import network.freecoin.notary.tron.protos.Contract.ExchangeWithdrawContract;
import network.freecoin.notary.tron.protos.Contract.FreezeBalanceContract;
import network.freecoin.notary.tron.protos.Contract.ParticipateAssetIssueContract;
import network.freecoin.notary.tron.protos.Contract.ProposalApproveContract;
import network.freecoin.notary.tron.protos.Contract.ProposalCreateContract;
import network.freecoin.notary.tron.protos.Contract.ProposalDeleteContract;
import network.freecoin.notary.tron.protos.Contract.SetAccountIdContract;
import network.freecoin.notary.tron.protos.Contract.TransferAssetContract;
import network.freecoin.notary.tron.protos.Contract.TransferContract;
import network.freecoin.notary.tron.protos.Contract.TriggerSmartContract;
import network.freecoin.notary.tron.protos.Contract.UnfreezeAssetContract;
import network.freecoin.notary.tron.protos.Contract.UnfreezeBalanceContract;
import network.freecoin.notary.tron.protos.Contract.UpdateAssetContract;
import network.freecoin.notary.tron.protos.Contract.UpdateSettingContract;
import network.freecoin.notary.tron.protos.Contract.VoteAssetContract;
import network.freecoin.notary.tron.protos.Contract.VoteWitnessContract;
import network.freecoin.notary.tron.protos.Contract.WithdrawBalanceContract;
import network.freecoin.notary.tron.protos.Contract.WitnessCreateContract;
import network.freecoin.notary.tron.protos.Contract.WitnessUpdateContract;
import network.freecoin.notary.tron.protos.Protocol.Transaction;
import org.spongycastle.util.encoders.Hex;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TronTxUtil {

  private Transaction transaction;

  public TronTxUtil(Transaction trx) {
    this.transaction = trx;
  }

  public TronTxUtil(byte[] data) throws InvalidProtocolBufferException {
    this.transaction = Transaction.parseFrom(data);
  }

  public long getTimestamp() {
    return transaction.getRawData().getTimestamp();
  }

  public String getTxId() {
    return Hex.toHexString(Sha256Hash.of(transaction.getRawData().toByteArray()).getBytes());
  }

  public String getOwner() {
    ByteString owner;
    Transaction.Contract contract = this.transaction.getRawData().getContract(0);
    try {
      Any contractParameter = contract.getParameter();
      switch (contract.getType()) {
        case AccountCreateContract:
          owner = contractParameter.unpack(AccountCreateContract.class).getOwnerAddress();
          break;
        case TransferContract:
          owner = contractParameter.unpack(TransferContract.class).getOwnerAddress();
          break;
        case TransferAssetContract:
          owner = contractParameter.unpack(TransferAssetContract.class).getOwnerAddress();
          break;
        case VoteAssetContract:
          owner = contractParameter.unpack(VoteAssetContract.class).getOwnerAddress();
          break;
        case VoteWitnessContract:
          owner = contractParameter.unpack(VoteWitnessContract.class).getOwnerAddress();
          break;
        case WitnessCreateContract:
          owner = contractParameter.unpack(WitnessCreateContract.class).getOwnerAddress();
          break;
        case AssetIssueContract:
          owner = contractParameter.unpack(AssetIssueContract.class).getOwnerAddress();
          break;
        case WitnessUpdateContract:
          owner = contractParameter.unpack(WitnessUpdateContract.class).getOwnerAddress();
          break;
        case ParticipateAssetIssueContract:
          owner = contractParameter.unpack(ParticipateAssetIssueContract.class).getOwnerAddress();
          break;
        case AccountUpdateContract:
          owner = contractParameter.unpack(AccountUpdateContract.class).getOwnerAddress();
          break;
        case FreezeBalanceContract:
          owner = contractParameter.unpack(FreezeBalanceContract.class).getOwnerAddress();
          break;
        case UnfreezeBalanceContract:
          owner = contractParameter.unpack(UnfreezeBalanceContract.class).getOwnerAddress();
          break;
        case UnfreezeAssetContract:
          owner = contractParameter.unpack(UnfreezeAssetContract.class).getOwnerAddress();
          break;
        case WithdrawBalanceContract:
          owner = contractParameter.unpack(WithdrawBalanceContract.class).getOwnerAddress();
          break;
        case CreateSmartContract:
          owner = contractParameter.unpack(Contract.CreateSmartContract.class).getOwnerAddress();
          break;
        case TriggerSmartContract:
          owner = contractParameter.unpack(Contract.TriggerSmartContract.class).getOwnerAddress();
          break;
        case UpdateAssetContract:
          owner = contractParameter.unpack(UpdateAssetContract.class).getOwnerAddress();
          break;
        case ProposalCreateContract:
          owner = contractParameter.unpack(ProposalCreateContract.class).getOwnerAddress();
          break;
        case ProposalApproveContract:
          owner = contractParameter.unpack(ProposalApproveContract.class).getOwnerAddress();
          break;
        case ProposalDeleteContract:
          owner = contractParameter.unpack(ProposalDeleteContract.class).getOwnerAddress();
          break;
        case SetAccountIdContract:
          owner = contractParameter.unpack(SetAccountIdContract.class).getOwnerAddress();
          break;
        case UpdateSettingContract:
          owner = contractParameter.unpack(UpdateSettingContract.class)
              .getOwnerAddress();
          break;
        case ExchangeCreateContract:
          owner = contractParameter.unpack(ExchangeCreateContract.class).getOwnerAddress();
          break;
        case ExchangeInjectContract:
          owner = contractParameter.unpack(ExchangeInjectContract.class).getOwnerAddress();
          break;
        case ExchangeWithdrawContract:
          owner = contractParameter.unpack(ExchangeWithdrawContract.class).getOwnerAddress();
          break;
        case ExchangeTransactionContract:
          owner = contractParameter.unpack(ExchangeTransactionContract.class).getOwnerAddress();
          break;
        default:
          owner = ByteString.EMPTY;
      }
      return WalletUtil.encode58Check(owner.toByteArray());
    } catch (Exception ex) {
      logger.error(ex.getMessage());
      return null;
    }
  }

  public String getToAddress() {
    Transaction.Contract contract = this.transaction.getRawData().getContract(0);
    ByteString to;
    try {
      Any contractParameter = contract.getParameter();
      switch (contract.getType()) {
        case TransferContract:
          to = contractParameter.unpack(TransferContract.class).getToAddress();
          break;
        case TransferAssetContract:
          to = contractParameter.unpack(TransferAssetContract.class).getToAddress();
          break;
        case ParticipateAssetIssueContract:
          to = contractParameter.unpack(ParticipateAssetIssueContract.class).getToAddress();
          break;
        default:
          return null;
      }
      return WalletUtil.encode58Check(to.toByteArray());
    } catch (Exception ex) {
      logger.error(ex.getMessage());
      return null;
    }
  }

  public long getSunValue() {
    Transaction.Contract contract = this.transaction.getRawData().getContract(0);
    try {
      Any contractParameter = contract.getParameter();
      switch (contract.getType()) {
        case TransferContract:
          return contractParameter.unpack(TransferContract.class).getAmount();
        // case TriggerSmartContract:
        //   return contractParameter.unpack(TriggerSmartContract.class).getCallValue();
        // case CreateSmartContract:
        //   return contractParameter.unpack(CreateSmartContract.class).getNewContract()
        //       .getCallValue();
        default:
          return 0L;
      }
    } catch (Exception ex) {
      logger.error(ex.getMessage());
      return 0L;
    }
  }
}
