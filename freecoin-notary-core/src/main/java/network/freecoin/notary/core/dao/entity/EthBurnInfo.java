package network.freecoin.notary.core.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * @author pengyuxiang
 * @date 2020/9/3
 */

@Data
@AllArgsConstructor
@Builder
@TableName("T_ETH_WITHDRAW")
public class EthBurnInfo {

    @TableId(value = "id", type = IdType.AUTO)
    private long id;
    private long burnProposalId;
    private String recipient;
    private long amount;
    private long amountOnSideChain;
    private String txSidechain;
    private int approve;
    private String success;
}
