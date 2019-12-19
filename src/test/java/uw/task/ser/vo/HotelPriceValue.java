package uw.task.ser.vo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalTime;
import java.util.Map;
import java.util.Objects;

/**
 * 酒店价格信息。
 * 酒店价格信息中，包含了产品信息字段，但是产品信息不入缓存。
 *
 * @author axeon
 */
public class HotelPriceValue {

    private final static Logger log = LoggerFactory.getLogger(HotelPriceValue.class);

    /**
     * 蟾皮n
     */
    private final static int DEFAULT_RULE_NUM = -5;

    public enum ChangeType {
        STATE(0),
        NUM_STOCK(1),
        NUM_ALL(2),
        PRICE_BASE(3),
        PRICE_DIST(4),
        PRICE_SALE(5),
        CONFIRM_TYPE(6),
        ALLOW_OVER_SOLD(7),
        MIN_ADV_MINUTES(8),
        MAX_ADV_MINUTES(9),
        DAY_START_TIME(10),
        DAY_END_TIME(11),
        ARRIVAL_START_TIME(12),
        ARRIVAL_END_TIME(13),
        BREAKFAST_NUM(14),
        MIN_DAYS(15),
        MAX_DAYS(16),
        MIN_ROOM_NUM(17),
        MAX_ROOM_NUM(18),
        CANCEL_TYPE(19),
        CANCEL_POLICY_MAP(20);

        public int value;

        ChangeType(int value) {
            this.value = value;
        }


    }

    /**
     * 别问了，这个时间就是我写这个代码时的时间戳！
     * 这么干，就是想节省4个字节存储。
     */
    private static final long TIMESTAMP_DIFF = 1531235689000L;

    /**
     * 上次更新时间。
     */
    private long lastUpdate;
    /**
     * 状态。 1正常  0关房
     */
    private int state;
    /**
     * 剩余库存数
     */
    private int numStock;
    /**
     * 总库存数
     */
    private int numAll;
    /**
     * 协议价(供应商获取到的价格)--采购价
     */
    private long priceBase;
    /**
     * 分销价
     */
    private long priceDist;
    /**
     * 零售价
     */
    private long priceSale;

    /**
     * 报价附带的产品信息。
     */
    private RuleInfo ruleInfo;
    /**
     * 分销商日历价格对象。当指定distributorMchid的时候，此信息才可能出现。
     * 此信息不参与序列化。
     */
    private DistributorPrice distributorPrice;

    /**
     * 判断规则是否改变,进行或运算
     */
    private int changeType;

    public HotelPriceValue() {
        this.lastUpdate = System.currentTimeMillis() - TIMESTAMP_DIFF;
    }


    public int getChangeType() {
        return changeType;
    }

    public void setChangeType(int changeType) {
        this.changeType = changeType;
    }

    public DistributorPrice getDistributorPrice() {
        return distributorPrice;
    }

    public void setDistributorPrice(DistributorPrice distributorPrice) {
        this.distributorPrice = distributorPrice;
    }

    public long getPriceBase() {
        return priceBase;
    }

    public void setPriceBase(long priceBase) {
        this.priceBase = priceBase;
    }

    public long getPriceDist() {
        return priceDist;
    }

    public void setPriceDist(long priceDist) {
        this.priceDist = priceDist;
    }

    public long getPriceSale() {
        return priceSale;
    }

    public void setPriceSale(long priceSale) {
        this.priceSale = priceSale;
    }

    public int getNumStock() {
        return numStock;
    }

    public void setNumStock(int numStock) {
        this.numStock = numStock;
    }

    public int getNumAll() {
        return numAll;
    }

    public void setNumAll(int numAll) {
        this.numAll = numAll;
    }

    public RuleInfo getRuleInfo() {
        return ruleInfo;
    }

    public void setRuleInfo(RuleInfo ruleInfo) {
        this.ruleInfo = ruleInfo;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public long getLastUpdate() {
        return lastUpdate + TIMESTAMP_DIFF;
    }

    public void setLastUpdate(long lastUpdate) {
        this.lastUpdate = lastUpdate - TIMESTAMP_DIFF;
    }

    /**
     * 产品信息。
     */
    public static class RuleInfo {

        /**
         * 确认类型 0不确定 1即时确认 2延迟确认
         */
        private int confirmType;
        /**
         * 是否允许超售 0:不允许 1:允许
         */
        private int allowOversold;
        /**
         * 最小提前预定分钟数，当为负数说明可以凌晨下定
         */
        private int minAdvMinutes;
        /**
         * 最大提前预定分钟数
         */
        private int maxAdvMinutes;
        /**
         * 每日起售时间
         */
        private LocalTime dayStartTime;
        /**
         * 每日结束时间
         */
        private LocalTime dayEndTime;
        /**
         * 客人到店最早时间
         */
        private LocalTime arrivalStartTime;
        /**
         * 客人到店最晚时间
         */
        private LocalTime arrivalEndTime;

        /**
         * 早餐数
         */
        private int breakfastNum;

        /**
         * 最小入住天数
         */
        private int minDays;
        /**
         * 最大入住天数
         */
        private int maxDays;
        /**
         * 最小预订间数
         */
        private int minRoomNum;
        /**
         * 最大预订间数
         */
        private int maxRoomNum;

        /**
         * 取消政策 -1:不允许 1:允许,3:条件允许
         */
        private int cancelType;

        /**
         * 取消规则.K:提前取消分钟数,V:取消需要扣的费用
         */
        private Map<Integer, Long> cancelPolicyMap;

        public RuleInfo() {
        }


        public static RuleInfo init() {
            RuleInfo ruleInfo = new RuleInfo();
            ruleInfo.setConfirmType(DEFAULT_RULE_NUM);
            ruleInfo.setAllowOversold(DEFAULT_RULE_NUM);
            ruleInfo.setMinAdvMinutes(DEFAULT_RULE_NUM);
            ruleInfo.setMaxAdvMinutes(DEFAULT_RULE_NUM);
            ruleInfo.setDayStartTime(null);
            ruleInfo.setDayEndTime(null);
            ruleInfo.setArrivalStartTime(null);
            ruleInfo.setArrivalEndTime(null);
            ruleInfo.setBreakfastNum(DEFAULT_RULE_NUM);
            ruleInfo.setMinDays(DEFAULT_RULE_NUM);
            ruleInfo.setMaxDays(DEFAULT_RULE_NUM);
            ruleInfo.setMinRoomNum(DEFAULT_RULE_NUM);
            ruleInfo.setMaxRoomNum(DEFAULT_RULE_NUM);
            ruleInfo.setCancelType(DEFAULT_RULE_NUM);
            ruleInfo.setCancelPolicyMap(null);
            return ruleInfo;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof RuleInfo)) {
                return false;
            }
            RuleInfo that = (RuleInfo) o;
            boolean result = false;
            if (this.confirmType != DEFAULT_RULE_NUM) {
                result = this.confirmType == that.getConfirmType();
            }
            if (this.allowOversold != DEFAULT_RULE_NUM) {
                result = this.allowOversold == that.getAllowOversold();
            }
            if (this.minAdvMinutes != DEFAULT_RULE_NUM) {
                result = this.minAdvMinutes == that.getMinAdvMinutes();
            }
            if (this.maxAdvMinutes != DEFAULT_RULE_NUM) {
                result = this.maxAdvMinutes == that.getMaxAdvMinutes();
            }
            if (this.breakfastNum != DEFAULT_RULE_NUM) {
                result = this.breakfastNum == that.getBreakfastNum();
            }
            if (this.minDays != DEFAULT_RULE_NUM) {
                result = this.minDays == that.getMinDays();
            }
            if (this.maxDays != DEFAULT_RULE_NUM) {
                result = this.maxDays == that.getMaxDays();
            }
            if (this.minRoomNum != DEFAULT_RULE_NUM) {
                result = this.minRoomNum == that.getMinRoomNum();
            }
            if (this.maxRoomNum != DEFAULT_RULE_NUM) {
                result = this.maxRoomNum == that.getMaxRoomNum();
            }
            if (this.cancelType != DEFAULT_RULE_NUM) {
                result = this.cancelType == that.getCancelType();
            }
            if (this.dayStartTime != null) {
                result = Objects.equals(this.dayStartTime, that.getDayStartTime());
            }
            if (this.dayEndTime != null) {
                result = Objects.equals(this.dayEndTime, that.getDayEndTime());
            }
            if (this.cancelPolicyMap != null) {
                result = Objects.equals(this.cancelPolicyMap, that.getCancelPolicyMap());
            }
            return result;
        }

        /**
         * 不需要添加DEFAULT_RULE_NUM判断
         *
         * @return
         */
        @Override
        public int hashCode() {
            return Objects.hash(getConfirmType(), getAllowOversold(), getMinAdvMinutes(), getMaxAdvMinutes(), getDayStartTime(), getDayEndTime(),
                    getArrivalStartTime(), getArrivalEndTime(), getBreakfastNum(), getMinDays(), getMaxDays(), getMinRoomNum(), getMaxRoomNum(),
                    getCancelType(), getCancelPolicyMap());
        }

        public int getConfirmType() {
            return confirmType;
        }

        public void setConfirmType(int confirmType) {
            this.confirmType = confirmType;
        }

        public int getAllowOversold() {
            return allowOversold;
        }

        public void setAllowOversold(int allowOversold) {
            this.allowOversold = allowOversold;
        }

        public int getMinAdvMinutes() {
            return minAdvMinutes;
        }

        public void setMinAdvMinutes(int minAdvMinutes) {
            this.minAdvMinutes = minAdvMinutes;
        }

        public int getMaxAdvMinutes() {
            return maxAdvMinutes;
        }

        public void setMaxAdvMinutes(int maxAdvMinutes) {
            this.maxAdvMinutes = maxAdvMinutes;
        }

        public LocalTime getDayStartTime() {
            return dayStartTime;
        }

        public void setDayStartTime(LocalTime dayStartTime) {
            this.dayStartTime = dayStartTime;
        }

        public LocalTime getDayEndTime() {
            return dayEndTime;
        }

        public void setDayEndTime(LocalTime dayEndTime) {
            this.dayEndTime = dayEndTime;
        }

        public LocalTime getArrivalStartTime() {
            return arrivalStartTime;
        }

        public void setArrivalStartTime(LocalTime arrivalStartTime) {
            this.arrivalStartTime = arrivalStartTime;
        }

        public LocalTime getArrivalEndTime() {
            return arrivalEndTime;
        }

        public void setArrivalEndTime(LocalTime arrivalEndTime) {
            this.arrivalEndTime = arrivalEndTime;
        }

        public int getBreakfastNum() {
            return breakfastNum;
        }

        public void setBreakfastNum(int breakfastNum) {
            this.breakfastNum = breakfastNum;
        }

        public int getMinDays() {
            return minDays;
        }

        public void setMinDays(int minDays) {
            this.minDays = minDays;
        }

        public int getMaxDays() {
            return maxDays;
        }

        public void setMaxDays(int maxDays) {
            this.maxDays = maxDays;
        }

        public int getMinRoomNum() {
            return minRoomNum;
        }

        public void setMinRoomNum(int minRoomNum) {
            this.minRoomNum = minRoomNum;
        }

        public int getMaxRoomNum() {
            return maxRoomNum;
        }

        public void setMaxRoomNum(int maxRoomNum) {
            this.maxRoomNum = maxRoomNum;
        }

        public int getCancelType() {
            return cancelType;
        }

        public void setCancelType(int cancelType) {
            this.cancelType = cancelType;
        }

        public Map<Integer, Long> getCancelPolicyMap() {
            return cancelPolicyMap;
        }

        public void setCancelPolicyMap(Map<Integer, Long> cancelPolicyMap) {
            this.cancelPolicyMap = cancelPolicyMap;
        }

    }

    /**
     * 分销商日历定价。
     *
     * @author axeon
     */
    public static class DistributorPrice {

        public enum OperationType {

            /**
             * 不计算
             */
            NONE(0, "不计算"),

            /**
             * 采购价加
             */
            PRICE_BASE_ADD(1, "采购价加"),

            /**
             * 采购价减
             */
            PRICE_BASE_SUB(2, "采购价减"),

            /**
             * 采购价乘
             */
            PRICE_BASE_MUL(3, "采购价乘"),

            /**
             * 分销价加
             */
            PRICE_DIST_ADD(5, "分销价加"),

            /**
             * 分销价减
             */
            PRICE_DIST_SUB(6, "分销价减"),

            /**
             * 分销价乘
             */
            PRICE_DIST_MUL(7, "分销价乘");

            public int value;

            public String label;

            OperationType(int value, String label) {
                this.value = value;
                this.label = label;
            }


        }

        /**
         * 上次更新时间。
         * 如果上次更新时间为0，那么是动态生成的。
         */
        private long lastUpdate;

        /**
         * 分销价计算类型
         */
        private int distOpType;

        /**
         * 零售价计算类型
         */
        private int saleOpType;

        /**
         * 分销成本价格
         */
        private long priceCost;

        /**
         * 分销价格
         */
        private long priceDist;

        /**
         * 终端价格
         */
        private long priceSale;

        /**
         * 取消规则.K:提前取消小时数,V:取消需要扣的费用(分销)
         */
        private Map<Integer, Long> cancelPriceDist;

        /**
         * 取消规则.K:提前取消小时数,V:取消需要扣的费用(终端)
         */
        private Map<Integer, Long> cancelPriceSale;

        public DistributorPrice() {
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof DistributorPrice)) {
                return false;
            }
            DistributorPrice that = (DistributorPrice) o;
            return this.distOpType == that.distOpType &&
                    this.saleOpType == that.saleOpType &&
                    this.priceDist == that.priceDist &&
                    this.priceSale == that.priceSale;
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.distOpType, this.saleOpType, this.priceDist, this.priceSale);
        }


        public long getLastUpdate() {
            if (lastUpdate > 0) {
                return lastUpdate + TIMESTAMP_DIFF;
            } else {
                return 0;
            }
        }

        public void setLastUpdate(long lastUpdate) {
            this.lastUpdate = lastUpdate - TIMESTAMP_DIFF;
        }

        public int getDistOpType() {
            return distOpType;
        }

        public void setDistOpType(int distOperaType) {
            this.distOpType = distOperaType;
        }

        public int getSaleOpType() {
            return saleOpType;
        }

        public void setSaleOpType(int saleOperaType) {
            this.saleOpType = saleOperaType;
        }

        public long getPriceCost() {
            return priceCost;
        }

        public void setPriceCost(long priceCost) {
            this.priceCost = priceCost;
        }

        public long getPriceDist() {
            return priceDist;
        }

        public void setPriceDist(long priceDist) {
            this.priceDist = priceDist;
        }

        public long getPriceSale() {
            return priceSale;
        }

        public void setPriceSale(long priceSale) {
            this.priceSale = priceSale;
        }

        public Map<Integer, Long> getCancelPriceDist() {
            return cancelPriceDist;
        }

        public void setCancelPriceDist(Map<Integer, Long> cancelPriceDist) {
            this.cancelPriceDist = cancelPriceDist;
        }

        public Map<Integer, Long> getCancelPriceSale() {
            return cancelPriceSale;
        }

        public void setCancelPriceSale(Map<Integer, Long> cancelPriceSale) {
            this.cancelPriceSale = cancelPriceSale;
        }
    }

}
