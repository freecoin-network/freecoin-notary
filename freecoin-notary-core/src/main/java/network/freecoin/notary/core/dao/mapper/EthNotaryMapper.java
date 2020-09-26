package network.freecoin.notary.core.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import network.freecoin.notary.core.dao.entity.EthNotary;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;


@Mapper
@Repository
public interface EthNotaryMapper extends BaseMapper<EthNotary> {

    @Select("Select credentials from t_eth_notary where type = 0")
    List<String> getNotary();

    @Select("Select credentials from t_eth_notary where type = 1")
    String getSender();
}
