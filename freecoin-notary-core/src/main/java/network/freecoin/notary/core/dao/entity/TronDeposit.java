package network.freecoin.notary.core.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@TableName("tron_deposit")
public class TronDeposit {

  @TableId(value = "id", type = IdType.AUTO)
  private long id;
  private long blockNum;
  private long mintProposalId;
  private String senderOnSideChain;
  private long amount;
  private String txOnSideChain;
  // 0: toDeposit; 1: done
  private int status;
}
