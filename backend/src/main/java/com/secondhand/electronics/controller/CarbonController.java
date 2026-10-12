package com.secondhand.electronics.controller;

import com.secondhand.electronics.entity.CarbonAccount;
import com.secondhand.electronics.entity.CarbonPointRecord;
import com.secondhand.electronics.entity.User;
import com.secondhand.electronics.exception.BusinessException;
import com.secondhand.electronics.service.CarbonService;
import com.secondhand.electronics.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/carbon")
@RequiredArgsConstructor
public class CarbonController {

    private final CarbonService carbonService;
    private final UserService userService;

    /** 获取当前用户碳账户 */
    @GetMapping("/account")
    public ResponseEntity<Map<String, Object>> getAccount(Authentication auth) {
        User user = userService.getUserByUsername(auth.getName());
        CarbonAccount account = carbonService.getOrCreate(user);
        return ResponseEntity.ok(toMap(account));
    }

    /** 每日签到 */
    @PostMapping("/sign")
    public ResponseEntity<?> dailySign(Authentication auth) {
        User user = userService.getUserByUsername(auth.getName());
        try {
            CarbonAccount account = carbonService.award(user, "DAILY_SIGN", "每日签到", null);
            return ResponseEntity.ok(toMap(account));
        } catch (BusinessException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    /** 积分明细 */
    @GetMapping("/records")
    public ResponseEntity<Page<CarbonPointRecord>> getRecords(
            Authentication auth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        User user = userService.getUserByUsername(auth.getName());
        Page<CarbonPointRecord> records = carbonService.getRecords(user, page, size);
        records.forEach(r -> {
            if (r.getUser() != null) r.getUser().getUsername();
        });
        return ResponseEntity.ok(records);
    }

    private Map<String, Object> toMap(CarbonAccount a) {
        Map<String, Object> m = new HashMap<>();
        m.put("totalPoints", a.getTotalPoints());
        m.put("availablePoints", a.getAvailablePoints());
        m.put("level", a.getLevel());
        m.put("carbonSavedGrams", a.getCarbonSavedGrams());
        Map<String, Integer> thresholds = Map.of("SPROUT", 50, "LEAF", 200, "TREE", 500, "GUARDIAN", 999999);
        m.put("nextLevelPoints", thresholds.getOrDefault(a.getLevel(), 999999));
        m.put("signedToday", carbonService.hasSignedToday(a.getUser()));
        return m;
    }
}
