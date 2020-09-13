package network.freecoin.notary.core.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@TableName("tron_deposit_address")
public class TronDepositAddress {

  @TableId(value = "id", type = IdType.AUTO)
  private long id;
  private String address;
}
