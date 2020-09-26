package network.freecoin.notary.core.dao.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@TableName("t_eth_notary")
@NoArgsConstructor
@AllArgsConstructor
public class EthNotary {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private String credentials;
    private int type;
}
