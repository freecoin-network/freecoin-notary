package network.freecoin.notary.core.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author pengyuxiang
 * @date 2020/9/3
 */

@Data
@Builder
@TableName("t_eth_withdraw")
@NoArgsConstructor
@AllArgsConstructor
public class EthBurnInfo {

  @TableId(value = "id", type = IdType.AUTO)
  private long id;
  private String recipient;
  private String txOnSideChain;
  private long burnProposalId;
  private long amountOnSideChain;
  private long amount;
  private long approve;
  private String status;
}
