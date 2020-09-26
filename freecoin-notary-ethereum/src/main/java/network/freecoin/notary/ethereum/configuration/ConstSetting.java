package network.freecoin.notary.ethereum.configuration;


public class ConstSetting {

  public static final String ROPSTEN = "https://ropsten.infura.io/v3/072097a24b7c4994912ccd66991d5300";
  public static final String RINKEBY = "https://rinkeby.infura.io/v3/072097a24b7c4994912ccd66991d5300";
  public static final String MAINNET = "https://mainnet.infura.io/v3/072097a24b7c4994912ccd66991d5300";

  public static final String GET_ETH_ADDR = "";
  public static final String DEPOSIT_CONFIRM = "depositConfirm";
  public static final String VERIFY_MINT = "getMintStatus";
  public static final String GET_PROPOSALID = "getMintStatus";
  public static final String GET_DEPOSITOR_ADDR = "getMintStatus";
  public static final String GET_BURN_OFFSET = "burnCount";
  public static final String GET_BURN_INFO = "getBurnInfo";
  public static final String GET_BURN_STATUS = "getBurnStatus";
  public static final String WITHDRAW_CONFIRM = "withdrawConfirm";

  private ConstSetting(){}
}
