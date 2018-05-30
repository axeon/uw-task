package uw.task.product;

/**
 * @Description: 同步房型需要用到的类, 用于传参
 * @author: Terry
 * @create 2018-04-17 14:11
 */
public class ProductGetParam {

    /**
     * 供应商配置ID（供应商商户id）
     */
    private long supplierConfigId;

    /**
     * 供应商酒店ID
     */
    private String supplierHotelId;

    /**
     * 自我游酒店ID
     */
    private long hotelId;

    public long getSupplierConfigId() {
        return supplierConfigId;
    }

    public void setSupplierConfigId(long supplierConfigId) {
        this.supplierConfigId = supplierConfigId;
    }

    public String getSupplierHotelId() {
        return supplierHotelId;
    }

    public void setSupplierHotelId(String supplierHotelId) {
        this.supplierHotelId = supplierHotelId;
    }

    public long getHotelId() {
        return hotelId;
    }

    public void setHotelId(long hotelId) {
        this.hotelId = hotelId;
    }

}
