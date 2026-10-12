package com.secondhand.electronics.service;

import com.secondhand.electronics.entity.CarbonAccount;
import com.secondhand.electronics.entity.CarbonPointRecord;
import com.secondhand.electronics.entity.User;
import com.secondhand.electronics.exception.BusinessException;
import com.secondhand.electronics.repository.CarbonAccountRepository;
import com.secondhand.electronics.repository.CarbonPointRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CarbonService {

    // 积分规则：每个行为对应的积分和碳减排量(克)
    private static final Map<String, int[]> RULES = Map.of(
        "PUBLISH",        new int[]{10, 500},   // 发布闲置 +10分，减碳500g
        "COMPLETE_ORDER", new int[]{20, 2000},  // 交易完成 +20分，减碳2000g
        "REVIEW",         new int[]{5,  100},   // 写评价   +5分，减碳100g
        "DAILY_SIGN",     new int[]{2,  50}     // 每日签到 +2分，减碳50g
    );

    // 等级阈值（累计积分）
    private static final int[] LEVEL_THRESHOLDS = {0, 50, 200, 500};
    private static final String[] LEVEL_NAMES = {"SPROUT", "LEAF", "TREE", "GUARDIAN"};

    private final CarbonAccountRepository accountRepo;
    private final CarbonPointRecordRepository recordRepo;

    /** 获取或初始化用户碳账户 */
    @Transactional
    public CarbonAccount getOrCreate(User user) {
        return accountRepo.findByUserId(user.getId())
                .orElseGet(() -> accountRepo.save(new CarbonAccount(user)));
    }

    /** 发放积分 */
    @Transactional
    public CarbonAccount award(User user, String actionType, String description, Long refId) {
        int[] rule = RULES.get(actionType);
        if (rule == null) return getOrCreate(user);

        // 每日签到防重复
        if ("DAILY_SIGN".equals(actionType)) {
            LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
            if (recordRepo.existsTodaySign(user.getId(), startOfDay)) {
                throw new BusinessException("今天已经签到过了，明天再来吧");
            }
        }

        CarbonAccount account = getOrCreate(user);
        account.setTotalPoints(account.getTotalPoints() + rule[0]);
        account.setAvailablePoints(account.getAvailablePoints() + rule[0]);
        account.setCarbonSavedGrams(account.getCarbonSavedGrams() + rule[1]);
        account.setLevel(calcLevel(account.getTotalPoints()));
        accountRepo.save(account);

        recordRepo.save(new CarbonPointRecord(user, rule[0], actionType, description, refId));
        return account;
    }

    /** 消费积分（下单抵扣） */
    @Transactional
    public void redeemPoints(User user, int points, String description, Long refId) {
        CarbonAccount account = getOrCreate(user);
        if (account.getAvailablePoints() < points) {
            throw new IllegalStateException("积分不足");
        }
        account.setAvailablePoints(account.getAvailablePoints() - points);
        accountRepo.save(account);
        recordRepo.save(new CarbonPointRecord(user, -points, "REDEEM", description, refId));
    }

    /** 今日是否已签到 */
    @Transactional(readOnly = true)
    public boolean hasSignedToday(User user) {
        return recordRepo.existsTodaySign(user.getId(), LocalDate.now().atStartOfDay());
    }

    /** 查询积分明细（分页） */
    @Transactional(readOnly = true)
    public Page<CarbonPointRecord> getRecords(User user, int page, int size) {
        return recordRepo.findByUserIdOrderByCreatedAtDesc(user.getId(), PageRequest.of(page, size));
    }

    private String calcLevel(int totalPoints) {
        String level = LEVEL_NAMES[0];
        for (int i = LEVEL_THRESHOLDS.length - 1; i >= 0; i--) {
            if (totalPoints >= LEVEL_THRESHOLDS[i]) {
                level = LEVEL_NAMES[i];
                break;
            }
        }
        return level;
    }
}
