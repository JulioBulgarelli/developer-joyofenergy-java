package uk.tw.energy.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.tw.energy.service.AccountService;
import uk.tw.energy.service.PricePlanService;

@RestController
@RequestMapping("/price-plans")
public class PricePlanComparatorController {

    public static final String PRICE_PLAN_ID_KEY = "pricePlanId";
    public static final String PRICE_PLAN_COMPARISONS_KEY = "pricePlanComparisons";
    private final PricePlanService pricePlanService;
    private final AccountService accountService;

    public PricePlanComparatorController(PricePlanService pricePlanService, AccountService accountService) {

        this.pricePlanService = pricePlanService;
        this.accountService = accountService;
    }

    @GetMapping("/comparisons")
    public ResponseEntity<Map<String, Object>> calculatedCostForEachPricePlan(
            @RequestParam(value = "smart-meter-id") String smartMeterId,
            @RequestParam(value = "offset", required = false, defaultValue = "0") Integer offset,
            @RequestParam(value = "limit", required = false, defaultValue = "10") Integer limit) {

        String pricePlanId = accountService.getPricePlanIdForSmartMeterId(smartMeterId);
        Optional<Map<String, BigDecimal>> consumptionsForPricePlans =
                pricePlanService.getConsumptionCostOfElectricityReadingsForEachPricePlan(smartMeterId, offset, limit);

        if (consumptionsForPricePlans.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Map<String, Object> pricePlanComparisons = new HashMap<>();
        pricePlanComparisons.put(PRICE_PLAN_ID_KEY, pricePlanId);
        pricePlanComparisons.put(PRICE_PLAN_COMPARISONS_KEY, consumptionsForPricePlans.get());

        return ResponseEntity.ok(pricePlanComparisons);
    }

    @GetMapping("/recommendations")
    public ResponseEntity<List<Map.Entry<String, BigDecimal>>> recommendCheapestPricePlans(
            @RequestParam(value = "smart-meter-id") String smartMeterId,
            @RequestParam(value = "offset", required = false, defaultValue = "0") Integer offset,
            @RequestParam(value = "limit", required = false, defaultValue = "10") Integer limit) {

        Optional<Map<String, BigDecimal>> consumptionsForPricePlans =
                pricePlanService.getConsumptionCostOfElectricityReadingsForEachPricePlan(smartMeterId, offset, limit);

        if (consumptionsForPricePlans.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<Map.Entry<String, BigDecimal>> recommendations =
                new ArrayList<>(consumptionsForPricePlans.get().entrySet());
        recommendations.sort(Map.Entry.comparingByValue());

        if (limit != null && limit <= recommendations.size()) {
            return ResponseEntity.ok(recommendations.subList(0, limit));
        }

        return ResponseEntity.ok(recommendations);
    }
}
