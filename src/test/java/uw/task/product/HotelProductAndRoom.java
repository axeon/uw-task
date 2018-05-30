package uw.task.product;

/**
 * 产品与房型
 *
 * @author Terry
 * @date 2018-04-26 11:07
 */
public class HotelProductAndRoom extends HotelProduct {

    private HotelRoomInfo hotelRoomInfo;

    public HotelRoomInfo getHotelRoomInfo() {
        return hotelRoomInfo;
    }

    public void setHotelRoomInfo(HotelRoomInfo hotelRoomInfo) {
        this.hotelRoomInfo = hotelRoomInfo;
    }
}
