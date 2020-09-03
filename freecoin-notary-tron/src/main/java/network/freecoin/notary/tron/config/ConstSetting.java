package network.freecoin.notary.tron.config;

import org.springframework.stereotype.Component;

@Component
public class ConstSetting {

  public static final long TRX = 1_000_000;
  public static final long FEE_LIMIT = 100 * TRX;
  public static final int TOKEN_CALL_VALUE = 0;
  public static final long TOKEN_ID = 0;
}
