package uk.tw.energy.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AccountServiceTest {

  private static final String PRICE_PLAN_ID = "price-plan-id";
  private static final String SMART_METER_ID = "smart-meter-id";

  private AccountService accountService;

  @BeforeEach
  public void setUp() {
    Map<String, String> smartMeterToPricePlanAccounts = new HashMap<>();
    smartMeterToPricePlanAccounts.put(SMART_METER_ID, PRICE_PLAN_ID);

    accountService = new AccountService(smartMeterToPricePlanAccounts);
  }

  @Test
  void givenTheSmartMeterIdReturnsThePricePlanId() {
    assertThat(accountService.getPricePlanIdForSmartMeterId(SMART_METER_ID))
        .isEqualTo(PRICE_PLAN_ID);
  }
}
