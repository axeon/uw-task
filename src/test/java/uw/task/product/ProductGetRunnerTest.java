package uw.task.product;

import com.google.common.collect.Lists;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import uw.task.TaskData;
import uw.task.util.TaskMessageConverter;

import java.util.List;

/**
 * @author liliang
 * @since 2018-05-30
 */
public class ProductGetRunnerTest {
    public static void main(String[] args) {
        TaskMessageConverter converter = new TaskMessageConverter();

        ProductGetRunner runner = new ProductGetRunner();
        TaskData<ProductGetParam, ResponseData<List<HotelProductAndRoom>>> taskData =
                new TaskData<ProductGetParam, ResponseData<List<HotelProductAndRoom>>>();
        ProductGetParam getParam = new ProductGetParam();
        getParam.setHotelId(1231231L);
        taskData.setTaskParam(getParam);
        taskData.setTaskClass("zwy.saas.hotel.task.supplier.meituan.runner.ProductGetRunner");
        ResponseData<List<HotelProductAndRoom>> responseData = new ResponseData<List<HotelProductAndRoom>>();
        List<HotelProductAndRoom> hotelProductAndRoomList = Lists.newArrayList();
        HotelProductAndRoom productAndRoom = new HotelProductAndRoom();

        HotelRoomInfo roomInfo = new HotelRoomInfo();
        roomInfo.setId(1231231);
        productAndRoom.setHotelRoomInfo(roomInfo);
        hotelProductAndRoomList.add(productAndRoom);
        responseData.setData(hotelProductAndRoomList);
        taskData.setResultData(responseData);

        TaskMessageConverter.constructTaskDataType("zwy.saas.hotel.task.supplier.meituan.runner.ProductGetRunner",runner);

        MessageProperties properties = new MessageProperties();
        Message message = converter.createMessage(taskData,properties);

        TaskData<ProductGetParam, ResponseData<List<HotelProductAndRoom>>> fromData =
                (TaskData<ProductGetParam, ResponseData<List<HotelProductAndRoom>>> )converter.fromMessage(message);
        ResponseData<List<HotelProductAndRoom>> data = fromData.getResultData();
    }
}
