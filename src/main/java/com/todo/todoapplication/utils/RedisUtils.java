package com.todo.todoapplication.utils;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis 工具类，封装常用的 String / Hash / List / Set 操作。
 *
 * <p>直接注入使用：</p>
 * <pre>
 *   {@code @Autowired}
 *   private RedisUtils redisUtils;
 * </pre>
 *
 * ===================== 使用示例 =====================
 *
 * 【String】
 * <pre>
 *   redisUtils.set("user:1", userObj, 30, TimeUnit.MINUTES); // 缓存对象30分钟
 *   User user = (User) redisUtils.get("user:1");             // 取出
 *   redisUtils.del("user:1");                                // 删除
 * </pre>
 *
 * 【黑名单 Token（退出登录场景）】
 * <pre>
 *   // 用户退出时，将 Token 加入黑名单，过期时间设为 Token 剩余有效期
 *   redisUtils.set("blacklist:" + token, "1", 7, TimeUnit.DAYS);
 *
 *   // 过滤器中判断 Token 是否在黑名单
 *   boolean banned = redisUtils.hasKey("blacklist:" + token);
 * </pre>
 *
 * 【Hash（存用户信息）】
 * <pre>
 *   redisUtils.hSet("user:info:1", "username", "admin");
 *   String name = (String) redisUtils.hGet("user:info:1", "username");
 * </pre>
 *
 * 【List（消息队列/日志）】
 * <pre>
 *   redisUtils.lPush("msg:queue", "消息内容");
 *   String msg = (String) redisUtils.rPop("msg:queue");
 * </pre>
 *
 * 【Set（收藏/去重）】
 * <pre>
 *   redisUtils.sAdd("todo:tags", "工作", "学习");
 *   Set<Object> tags = redisUtils.sMembers("todo:tags");
 * </pre>
 *
 * ===================== 注意事项 =====================
 * 1. key 建议使用冒号分隔的命名规范，如 "user:1"、"todo:list:userId"。
 * 2. 存储的对象必须可被 Jackson 序列化（推荐加 @Data + 无参构造）。
 * 3. get() 返回 Object，需自行强转，或用泛型封装。
 */
@Component
public class RedisUtils {

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisUtils(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // ======================== 通用操作 ========================

    /**
     * 判断 key 是否存在
     */
    public boolean hasKey(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    /**
     * 删除单个 key
     */
    public void del(String key) {
        redisTemplate.delete(key);
    }

    /**
     * 批量删除 key
     */
    public void del(Collection<String> keys) {
        redisTemplate.delete(keys);
    }

    /**
     * 设置 key 的过期时间
     *
     * @param key      键
     * @param timeout  时长
     * @param timeUnit 时间单位
     */
    public boolean expire(String key, long timeout, TimeUnit timeUnit) {
        return Boolean.TRUE.equals(redisTemplate.expire(key, timeout, timeUnit));
    }

    /**
     * 获取 key 的剩余过期时间（秒），-1 表示永不过期，-2 表示不存在
     */
    public long ttl(String key) {
        Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
        return ttl != null ? ttl : -2;
    }

    // ======================== String 操作 ========================

    /**
     * 存入值（永不过期）
     */
    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * 存入值并设置过期时间
     *
     * @param key      键
     * @param value    值（支持任意对象）
     * @param timeout  过期时长
     * @param timeUnit 时间单位（TimeUnit.MINUTES / TimeUnit.HOURS / TimeUnit.DAYS ...）
     */
    public void set(String key, Object value, long timeout, TimeUnit timeUnit) {
        redisTemplate.opsForValue().set(key, value, timeout, timeUnit);
    }

    /**
     * 取出值
     *
     * @return 值，不存在返回 null
     */
    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * 自增（适用于计数器）
     *
     * @param delta 步长
     */
    public long increment(String key, long delta) {
        Long result = redisTemplate.opsForValue().increment(key, delta);
        return result != null ? result : 0;
    }

    // ======================== Hash 操作 ========================

    /**
     * 向 Hash 中存入单个字段
     */
    public void hSet(String key, String field, Object value) {
        redisTemplate.opsForHash().put(key, field, value);
    }

    /**
     * 向 Hash 中批量存入字段
     */
    public void hSetAll(String key, Map<String, Object> map) {
        redisTemplate.opsForHash().putAll(key, map);
    }

    /**
     * 获取 Hash 中单个字段的值
     */
    public Object hGet(String key, String field) {
        return redisTemplate.opsForHash().get(key, field);
    }

    /**
     * 获取 Hash 中所有字段和值
     */
    public Map<Object, Object> hGetAll(String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    /**
     * 删除 Hash 中的字段
     */
    public void hDel(String key, Object... fields) {
        redisTemplate.opsForHash().delete(key, fields);
    }

    /**
     * 判断 Hash 中字段是否存在
     */
    public boolean hHasKey(String key, String field) {
        return redisTemplate.opsForHash().hasKey(key, field);
    }

    // ======================== List 操作 ========================

    /**
     * 从左端插入（相当于入队头）
     */
    public void lPush(String key, Object value) {
        redisTemplate.opsForList().leftPush(key, value);
    }

    /**
     * 从右端插入（相当于入队尾）
     */
    public void rPush(String key, Object value) {
        redisTemplate.opsForList().rightPush(key, value);
    }

    /**
     * 从右端弹出（相当于出队，先进先出）
     */
    public Object rPop(String key) {
        return redisTemplate.opsForList().rightPop(key);
    }

    /**
     * 获取 List 指定范围内的元素（0 到 -1 表示全部）
     */
    public List<Object> lRange(String key, long start, long end) {
        return redisTemplate.opsForList().range(key, start, end);
    }

    /**
     * 获取 List 长度
     */
    public long lSize(String key) {
        Long size = redisTemplate.opsForList().size(key);
        return size != null ? size : 0;
    }

    // ======================== Set 操作 ========================

    /**
     * 向 Set 中添加元素
     */
    public void sAdd(String key, Object... values) {
        redisTemplate.opsForSet().add(key, values);
    }

    /**
     * 获取 Set 中所有元素
     */
    public Set<Object> sMembers(String key) {
        return redisTemplate.opsForSet().members(key);
    }

    /**
     * 判断元素是否在 Set 中
     */
    public boolean sIsMember(String key, Object value) {
        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(key, value));
    }

    /**
     * 从 Set 中删除元素
     */
    public void sRemove(String key, Object... values) {
        redisTemplate.opsForSet().remove(key, values);
    }
}

