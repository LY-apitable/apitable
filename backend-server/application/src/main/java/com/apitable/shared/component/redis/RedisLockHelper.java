/*
 * APITable <https://github.com/apitable/apitable>
 * Copyright (C) 2022 APITable Ltd. <https://apitable.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.apitable.shared.component.redis;

import cn.hutool.core.util.ObjectUtil;
import com.apitable.core.exception.BusinessException;
import com.apitable.core.util.SpringContextHolder;
import jakarta.annotation.Resource;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * <p>
 * RedisLockHelper.
 * </p>
 *
 * @author Chambers
 */
@Component
@Slf4j
public class RedisLockHelper {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    public static RedisLockHelper me() {
        return SpringContextHolder.getBean(RedisLockHelper.class);
    }

    /**
     * <p>
     * preventDuplicateRequests.
     * </p>
     *
     * @param key a {@link java.lang.String} object.
     */
    public void preventDuplicateRequests(String key) {
        BoundValueOperations<String, Object> ops = redisTemplate.boundValueOps(key);
        if (ObjectUtil.isNotNull(ops.get())) {
            throw new BusinessException("repeat request");
        }
        ops.set("", 1, TimeUnit.HOURS);
    }

    public boolean tryLock(String key) {
        return tryLock(key, 30000);
    }

    /**
     * try lock.
     *
     * @param key key
     * @param lockExpireMils lockExpireMils
     * @return boolean
     */
    public boolean tryLock(String key, long lockExpireMils) {
        long intervalTimeMils = 100L;
        while (true) {
            if (redisTemplate.opsForValue().setIfAbsent(key, "1", lockExpireMils, TimeUnit.MILLISECONDS)) {
                return true;
            }
            sleep(intervalTimeMils);
        }
    }

    public void releaseLock(String key) {
        redisTemplate.delete(key);
    }
    
    private void sleep(long intervalTimeMils) {
        try {
            TimeUnit.MILLISECONDS.sleep(intervalTimeMils);
        } catch (InterruptedException e) {
            log.error("RedisLock interval sleep error", e);
        }
    }
}
