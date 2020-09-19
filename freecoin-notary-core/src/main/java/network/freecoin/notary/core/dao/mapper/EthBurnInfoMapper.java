package network.freecoin.notary.core.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import network.freecoin.notary.core.dao.entity.EthBurnInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

/**
 * @author pengyuxiang
 * @date 2020/9/3
 */

@Mapper
@Repository
public interface EthBurnInfoMapper extends BaseMapper<EthBurnInfo> {


    @Select("select ifnull(max(burn_proposal_id), -1) from T_ETH_WITHDRAW where status = '1'")
    long getCurRecord();
}
