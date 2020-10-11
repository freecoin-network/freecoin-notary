package network.freecoin.notary.core.common.config;

public class ConstSetting {

  private ConstSetting() {
    throw new IllegalStateException("can't constructor");
  }

  public static final long TRON_DEPOSIT_META_ID = 1;
  public static final long WAITING_FOR_NEW_BLOCK_MS = 3_000;

  public static final String WITHDRAW_SUCCESS = "1";
  public static final String WITHDRAW_FAILED = "2";
  public static final String WITHDRAW_NO_TRX_VERIFY = "3";
  public static final String WITHDRAW_NO_ETH_CONFIRM = "4";
  public static final String WITHDRAW_ETH_CONFIRM_FAILED = "5";
  public static final String WITHDRAW_INIT = "0";
  public static final long ETH_CONFIRM_SECOND = 90;
  public static final long TRX_CONFIRM_SECOND = 60;

}
