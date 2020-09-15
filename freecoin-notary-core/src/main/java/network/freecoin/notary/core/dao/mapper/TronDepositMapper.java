package network.freecoin.notary.core.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import java.util.List;
import network.freecoin.notary.core.dao.entity.TronDeposit;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface TronDepositMapper extends BaseMapper<TronDeposit> {

  @Select("select * from tron_deposit order by block_num, tx_index_on_side_chain desc limit 1")
  TronDeposit selectLastDeposit();

  @Select("select * from tron_deposit where status = 0")
  List<TronDeposit> selectToHandleList();
}