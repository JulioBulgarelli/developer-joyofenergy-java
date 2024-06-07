package uk.tw.energy.service;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AccountService {

    private final Logger log = LoggerFactory.getLogger(AccountService.class);

    private final Map<String, String> smartMeterToPricePlanAccounts;

    public AccountService(Map<String, String> smartMeterToPricePlanAccounts) {
        log.info("Instantiating Bean || smartMeterToPricePlanAccounts = {}", smartMeterToPricePlanAccounts);
        this.smartMeterToPricePlanAccounts = smartMeterToPricePlanAccounts;
        log.info("Instantiated Bean successfully");
    }

    public String getPricePlanIdForSmartMeterId(String smartMeterId) {
        log.info("Started ::getPricePlanIdForSmartMeterId || smartMeterId = {}", smartMeterId);

        String pricePlanId = smartMeterToPricePlanAccounts.get(smartMeterId);

        log.info("Finished ::getPricePlanIdForSmartMeterId || pricePlanId = {}", pricePlanId);
        return pricePlanId;
    }
}
