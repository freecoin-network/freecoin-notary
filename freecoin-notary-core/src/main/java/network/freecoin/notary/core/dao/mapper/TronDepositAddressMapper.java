package network.freecoin.notary.core.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import network.freecoin.notary.core.dao.entity.TronDepositAddress;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface TronDepositAddressMapper extends BaseMapper<TronDepositAddress> {

}