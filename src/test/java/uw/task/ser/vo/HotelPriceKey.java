package uw.task.ser.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDate;

/**
 * 酒店报价KEY。
 *
 * @author axeon
 */
public class HotelPriceKey {

    /**
     * 序列化ID
     */
    private static final long serialVersionUID = 1L;

    /**
     * HASH_TAG前缀
     */
    private static final byte[] HASH_TAG_PREFIX = "{".getBytes();

    /**
     * HASH_TAG后缀
     */
    private static final byte[] HASH_TAG_SUFFIX = "}".getBytes();

    /**
     * 基础计算日。
     */
    private static LocalDate BASE_DATE = LocalDate.of(2019, 1, 1);

    /**
     * 产品ID
     */
    private long productId;

    /**
     * 分销商id，此值为可选，如果>0，则说明指定分销商查询。
     */
    private long distributorMchId;

    /**
     * 日期天数误差。
     */
    @JsonIgnore
    private int dateDiff;

    /**
     * 价格日期。
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private volatile LocalDate priceDate;

    /**
     * 成人数
     */
    private int adultNum;

    /**
     * 儿童数
     */
    private int childNum;

    /**
     * 儿童年龄,逗号隔开
     */
    private String childAges;

    /**
     * 是否国际酒店单产品的价格
     */
    @JsonIgnore
    private boolean isGlobalOrderCheckPrice;


    public HotelPriceKey() {
    }

    public long getProductId() {
        return productId;
    }

    public void setProductId(long productId) {
        this.productId = productId;
    }

    public long getDistributorMchId() {
        return distributorMchId;
    }

    public void setDistributorMchId(long distributorMchId) {
        this.distributorMchId = distributorMchId;
    }

    public int getDateDiff() {
        return dateDiff;
    }

    public void setDateDiff(int dateDiff) {
        this.dateDiff = dateDiff;
    }

    public LocalDate getPriceDate() {
        return priceDate;
    }

    public void setPriceDate(LocalDate priceDate) {
        this.priceDate = priceDate;
    }

    public int getAdultNum() {
        return adultNum;
    }

    public void setAdultNum(int adultNum) {
        this.adultNum = adultNum;
    }

    public int getChildNum() {
        return childNum;
    }

    public void setChildNum(int childNum) {
        this.childNum = childNum;
    }

    public String getChildAges() {
        return childAges;
    }

    public void setChildAges(String childAges) {
        this.childAges = childAges;
    }

    public boolean isGlobalOrderCheckPrice() {
        return isGlobalOrderCheckPrice;
    }

    public void setGlobalOrderCheckPrice(boolean globalOrderCheckPrice) {
        isGlobalOrderCheckPrice = globalOrderCheckPrice;
    }
}
