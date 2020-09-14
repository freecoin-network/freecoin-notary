package network.freecoin.notary.core.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import java.util.List;
import network.freecoin.notary.core.dao.entity.TronDeposit;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface TronDepositMapper extends BaseMapper<TronDeposit> {

  @Select("select ifnull(max(mint_proposal_id), -1) from tron_deposit")
  long selectMaxMintProposalId();

  @Select("select * from tron_deposit where mint_proposal_id = #{mintProposalId} limit 1")
  TronDeposit selectMaxDeposit(@Param("mintProposalId") long mintProposalId);

  @Select("select * from tron_deposit where status = 0")
  List<TronDeposit> selectToHandleList();
}