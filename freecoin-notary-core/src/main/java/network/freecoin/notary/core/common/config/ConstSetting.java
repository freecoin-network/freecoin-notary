package network.freecoin.notary.core.common.config;

public class ConstSetting {

  private ConstSetting() {
    throw new IllegalStateException("can't constructor");
  }

  public static final long RELAY_MS = 60_000;
  public static final long TRON_DEPOSIT_META_ID = 1;
  public static final long WAITING_FOR_NEW_BLOCK__MS = 3_000;
}
