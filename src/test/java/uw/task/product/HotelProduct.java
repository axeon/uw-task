package uw.task.product;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * @author liliang
 * @since 2018-05-30
 */
public class HotelProduct {

    private long id;

    private long saasId;

    private long supplierMchId;

    private long areaCode;


    private String currency;

    private int hotelStar;

    private String hotelName;


    private String hotelNameEn;


    private String roomName;

    private String roomNameEn;


    private String productTag;

    private String productTagEn;


    private long productPrice;

    private String productInfo;

    private String bookingInfo;

    private long hotelId;

    private long roomId;


    private int confirmType;

    private int allowOversold;

    private int minAdvMinutes;

    private int maxAdvMinutes;
    @JsonFormat(pattern = "HH:mm:ss")
    private java.util.Date dayStartTime;
    @JsonFormat(pattern = "HH:mm:ss")
    private java.util.Date dayEndTime;
    @JsonFormat(pattern = "HH:mm:ss")
    private java.util.Date arrivalStartTime;
    @JsonFormat(pattern = "HH:mm:ss")
    private java.util.Date arrivalEndTime;

    private String weekLimit;

    private int breakfastNum;

    private int lunchNum;
    private int dinnerNum;
    private int minDays;

    private int maxDays;

    private int minRoomNum;

    private int maxRoomNum;

    private int cancelType;

    private String cancelPolicy;

    private int marketRule;
    private String marketCodes;

    private String bedType;
    private int guestType;

    private long supplierId;

    private String supplierHotelId;

    private String supplierRoomId;
    private String supplierProductId;

    private String supplierSourceId;

    private int supplierState;

    private String channelConfigIds;

    private int sendNotifyType;
    private int paymentMinutes;
    private int paymentType;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private java.util.Date saleStartDate;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private java.util.Date saleStopDate;

    private int isSendSms;

    private int hasPlan;

    private String sendSmsContent;
    private int saleRange;

    private int permitBundle;

    private String signImg;

    private java.util.Date createDate;

    private java.util.Date modifyDate;

    private int invoiceType;

    private int invoiceProvider;

    private int invoiceFee;
    private String invoiceDesc;

    private long orderCheckNum;

    private long orderCheckFail;

    private long orderSaleNum;

    private long orderSaleCancel;

    private int shareType;

    private int state;

    private String signature;

    private java.util.Date lastUpdate;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getSaasId() {
        return saasId;
    }

    public void setSaasId(long saasId) {
        this.saasId = saasId;
    }

    public long getSupplierMchId() {
        return supplierMchId;
    }

    public void setSupplierMchId(long supplierMchId) {
        this.supplierMchId = supplierMchId;
    }

    public long getAreaCode() {
        return areaCode;
    }

    public void setAreaCode(long areaCode) {
        this.areaCode = areaCode;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public int getHotelStar() {
        return hotelStar;
    }

    public void setHotelStar(int hotelStar) {
        this.hotelStar = hotelStar;
    }

    public String getHotelName() {
        return hotelName;
    }

    public void setHotelName(String hotelName) {
        this.hotelName = hotelName;
    }

    public String getHotelNameEn() {
        return hotelNameEn;
    }

    public void setHotelNameEn(String hotelNameEn) {
        this.hotelNameEn = hotelNameEn;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public String getRoomNameEn() {
        return roomNameEn;
    }

    public void setRoomNameEn(String roomNameEn) {
        this.roomNameEn = roomNameEn;
    }

    public String getProductTag() {
        return productTag;
    }

    public void setProductTag(String productTag) {
        this.productTag = productTag;
    }

    public String getProductTagEn() {
        return productTagEn;
    }

    public void setProductTagEn(String productTagEn) {
        this.productTagEn = productTagEn;
    }

    public long getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(long productPrice) {
        this.productPrice = productPrice;
    }

    public String getProductInfo() {
        return productInfo;
    }

    public void setProductInfo(String productInfo) {
        this.productInfo = productInfo;
    }

    public String getBookingInfo() {
        return bookingInfo;
    }

    public void setBookingInfo(String bookingInfo) {
        this.bookingInfo = bookingInfo;
    }

    public long getHotelId() {
        return hotelId;
    }

    public void setHotelId(long hotelId) {
        this.hotelId = hotelId;
    }

    public long getRoomId() {
        return roomId;
    }

    public void setRoomId(long roomId) {
        this.roomId = roomId;
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

    public Date getDayStartTime() {
        return dayStartTime;
    }

    public void setDayStartTime(Date dayStartTime) {
        this.dayStartTime = dayStartTime;
    }

    public Date getDayEndTime() {
        return dayEndTime;
    }

    public void setDayEndTime(Date dayEndTime) {
        this.dayEndTime = dayEndTime;
    }

    public Date getArrivalStartTime() {
        return arrivalStartTime;
    }

    public void setArrivalStartTime(Date arrivalStartTime) {
        this.arrivalStartTime = arrivalStartTime;
    }

    public Date getArrivalEndTime() {
        return arrivalEndTime;
    }

    public void setArrivalEndTime(Date arrivalEndTime) {
        this.arrivalEndTime = arrivalEndTime;
    }

    public String getWeekLimit() {
        return weekLimit;
    }

    public void setWeekLimit(String weekLimit) {
        this.weekLimit = weekLimit;
    }

    public int getBreakfastNum() {
        return breakfastNum;
    }

    public void setBreakfastNum(int breakfastNum) {
        this.breakfastNum = breakfastNum;
    }

    public int getLunchNum() {
        return lunchNum;
    }

    public void setLunchNum(int lunchNum) {
        this.lunchNum = lunchNum;
    }

    public int getDinnerNum() {
        return dinnerNum;
    }

    public void setDinnerNum(int dinnerNum) {
        this.dinnerNum = dinnerNum;
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

    public String getCancelPolicy() {
        return cancelPolicy;
    }

    public void setCancelPolicy(String cancelPolicy) {
        this.cancelPolicy = cancelPolicy;
    }

    public int getMarketRule() {
        return marketRule;
    }

    public void setMarketRule(int marketRule) {
        this.marketRule = marketRule;
    }

    public String getMarketCodes() {
        return marketCodes;
    }

    public void setMarketCodes(String marketCodes) {
        this.marketCodes = marketCodes;
    }

    public String getBedType() {
        return bedType;
    }

    public void setBedType(String bedType) {
        this.bedType = bedType;
    }

    public int getGuestType() {
        return guestType;
    }

    public void setGuestType(int guestType) {
        this.guestType = guestType;
    }

    public long getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(long supplierId) {
        this.supplierId = supplierId;
    }

    public String getSupplierHotelId() {
        return supplierHotelId;
    }

    public void setSupplierHotelId(String supplierHotelId) {
        this.supplierHotelId = supplierHotelId;
    }

    public String getSupplierRoomId() {
        return supplierRoomId;
    }

    public void setSupplierRoomId(String supplierRoomId) {
        this.supplierRoomId = supplierRoomId;
    }

    public String getSupplierProductId() {
        return supplierProductId;
    }

    public void setSupplierProductId(String supplierProductId) {
        this.supplierProductId = supplierProductId;
    }

    public String getSupplierSourceId() {
        return supplierSourceId;
    }

    public void setSupplierSourceId(String supplierSourceId) {
        this.supplierSourceId = supplierSourceId;
    }

    public int getSupplierState() {
        return supplierState;
    }

    public void setSupplierState(int supplierState) {
        this.supplierState = supplierState;
    }

    public String getChannelConfigIds() {
        return channelConfigIds;
    }

    public void setChannelConfigIds(String channelConfigIds) {
        this.channelConfigIds = channelConfigIds;
    }

    public int getSendNotifyType() {
        return sendNotifyType;
    }

    public void setSendNotifyType(int sendNotifyType) {
        this.sendNotifyType = sendNotifyType;
    }

    public int getPaymentMinutes() {
        return paymentMinutes;
    }

    public void setPaymentMinutes(int paymentMinutes) {
        this.paymentMinutes = paymentMinutes;
    }

    public int getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(int paymentType) {
        this.paymentType = paymentType;
    }

    public Date getSaleStartDate() {
        return saleStartDate;
    }

    public void setSaleStartDate(Date saleStartDate) {
        this.saleStartDate = saleStartDate;
    }

    public Date getSaleStopDate() {
        return saleStopDate;
    }

    public void setSaleStopDate(Date saleStopDate) {
        this.saleStopDate = saleStopDate;
    }

    public int getIsSendSms() {
        return isSendSms;
    }

    public void setIsSendSms(int isSendSms) {
        this.isSendSms = isSendSms;
    }

    public int getHasPlan() {
        return hasPlan;
    }

    public void setHasPlan(int hasPlan) {
        this.hasPlan = hasPlan;
    }

    public String getSendSmsContent() {
        return sendSmsContent;
    }

    public void setSendSmsContent(String sendSmsContent) {
        this.sendSmsContent = sendSmsContent;
    }

    public int getSaleRange() {
        return saleRange;
    }

    public void setSaleRange(int saleRange) {
        this.saleRange = saleRange;
    }

    public int getPermitBundle() {
        return permitBundle;
    }

    public void setPermitBundle(int permitBundle) {
        this.permitBundle = permitBundle;
    }

    public String getSignImg() {
        return signImg;
    }

    public void setSignImg(String signImg) {
        this.signImg = signImg;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Date getModifyDate() {
        return modifyDate;
    }

    public void setModifyDate(Date modifyDate) {
        this.modifyDate = modifyDate;
    }

    public int getInvoiceType() {
        return invoiceType;
    }

    public void setInvoiceType(int invoiceType) {
        this.invoiceType = invoiceType;
    }

    public int getInvoiceProvider() {
        return invoiceProvider;
    }

    public void setInvoiceProvider(int invoiceProvider) {
        this.invoiceProvider = invoiceProvider;
    }

    public int getInvoiceFee() {
        return invoiceFee;
    }

    public void setInvoiceFee(int invoiceFee) {
        this.invoiceFee = invoiceFee;
    }

    public String getInvoiceDesc() {
        return invoiceDesc;
    }

    public void setInvoiceDesc(String invoiceDesc) {
        this.invoiceDesc = invoiceDesc;
    }

    public long getOrderCheckNum() {
        return orderCheckNum;
    }

    public void setOrderCheckNum(long orderCheckNum) {
        this.orderCheckNum = orderCheckNum;
    }

    public long getOrderCheckFail() {
        return orderCheckFail;
    }

    public void setOrderCheckFail(long orderCheckFail) {
        this.orderCheckFail = orderCheckFail;
    }

    public long getOrderSaleNum() {
        return orderSaleNum;
    }

    public void setOrderSaleNum(long orderSaleNum) {
        this.orderSaleNum = orderSaleNum;
    }

    public long getOrderSaleCancel() {
        return orderSaleCancel;
    }

    public void setOrderSaleCancel(long orderSaleCancel) {
        this.orderSaleCancel = orderSaleCancel;
    }

    public int getShareType() {
        return shareType;
    }

    public void setShareType(int shareType) {
        this.shareType = shareType;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }
}
