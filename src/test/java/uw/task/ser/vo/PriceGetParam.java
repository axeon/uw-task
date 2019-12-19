package uw.task.ser.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import uw.task.product.HotelProduct;

import java.time.LocalDate;
import java.util.List;

/**
 * 获取价格参数
 **/
public class PriceGetParam {

    /**
     * 分销商ID
     * 同时也是渠道接口配置ID
     */
    protected long distributorMchId;


    /**
     * 按酒店查询时候缓存的产品列表，用于提高性能。
     * 分发时，绑定产品信息，可以减少数据库操作。
     * 对于价格处理,hotelProduct只有这些信息
     * id, saas_id, supplier_mch_id, hotel_id,
     * product_price, supplier_id, supplier_hotel_id,
     * supplier_room_id, supplier_product_id,
     * channel_config_ids, area_code, currency
     */
    protected List<HotelProduct> hotelProductList;

    /**
     * 开始查询日期 格式yyyy-MM-dd
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    protected LocalDate startDate;

    /**
     * 结束查询日期 格式yyyy-MM-dd
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    protected LocalDate endDate;

    /**
     * 是否从缓存获取
     */
    protected boolean fromCache = false;

    /**
     * 是变价请求类型，需要清除队列信息。
     */
    protected int priceChangeRequestType;

    public long getDistributorMchId() {
        return distributorMchId;
    }

    public void setDistributorMchId(long distributorMchId) {
        this.distributorMchId = distributorMchId;
    }

    public List<HotelProduct> getHotelProductList() {
        return hotelProductList;
    }

    public void setHotelProductList(List<HotelProduct> hotelProductList) {
        this.hotelProductList = hotelProductList;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public boolean isFromCache() {
        return fromCache;
    }

    public void setFromCache(boolean fromCache) {
        this.fromCache = fromCache;
    }

    public int getPriceChangeRequestType() {
        return priceChangeRequestType;
    }

    public void setPriceChangeRequestType(int priceChangeRequestType) {
        this.priceChangeRequestType = priceChangeRequestType;
    }
}
