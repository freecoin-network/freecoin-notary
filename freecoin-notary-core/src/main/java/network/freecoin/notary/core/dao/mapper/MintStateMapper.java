package network.freecoin.notary.core.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import network.freecoin.notary.core.dao.entity.MintState;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * @author pengyuxiang
 * @date 2020/9/4
 */

@Mapper
@Repository
public interface MintStateMapper extends BaseMapper<MintState> {
}
