package network.freecoin.notary.core.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EthVerifyData {

  private long id;
  private String recipient;
  private String txOnSideChain;
  private long burnProposalId;
  private long amountOnSideChain;
  private long amount;
  private long approve;
  private String status;
  private long timestamp;
}
