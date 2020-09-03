package network.freecoin.notary.core.controller;

import org.springframework.stereotype.Component;

/**
 * @author pengyuxiang
 * @date 2020/9/3
 */

@Component
public class CoreController {


    /**
     * 用户触发铸币请求
     * @return
     */
    public String deposit(){


        //TODO 验证交易正确性 验证TRX已转入质押者钱包
        //TODO 铸币 向对应ETH账号铸造合成资产
        //TODO
        return "";
    }


    /**
     * 用户触发提现请求
     * @return
     */
    public String withdraw(){
        //TODO 提交取现账号
        return "";
    }
}
