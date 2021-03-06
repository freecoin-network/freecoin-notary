package network.freecoin.notary.core.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DepositData {

  private long blockNum;
  private String senderOnSideChain;
  private long amount;
  private String txOnSideChain;
  private long timestamp;
}
