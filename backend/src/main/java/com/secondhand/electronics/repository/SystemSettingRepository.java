package com.secondhand.electronics.repository;

import com.secondhand.electronics.entity.SystemSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SystemSettingRepository extends JpaRepository<SystemSetting, Long> {
    
    /**
     * 根据设置键查询设置
     */
    Optional<SystemSetting> findBySettingKey(String settingKey);
    
    /**
     * 根据设置类型查询设置列表
     */
    List<SystemSetting> findBySettingType(String settingType);
    
    /**
     * 查询设置键以指定前缀开头的设置
     */
    @Query("SELECT s FROM SystemSetting s WHERE s.settingKey LIKE :prefix% ORDER BY s.settingKey")
    List<SystemSetting> findBySettingKeyStartingWith(@Param("prefix") String prefix);
    
    /**
     * 检查设置键是否存在
     */
    boolean existsBySettingKey(String settingKey);
    
    /**
     * 根据设置键删除设置
     */
    void deleteBySettingKey(String settingKey);
}