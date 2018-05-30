package uw.task.product;

import com.fasterxml.jackson.core.type.TypeReference;
import uw.task.TaskData;
import uw.task.TaskRunner;

import java.util.List;

/**
 * @author liliang
 * @since 2018-05-30
 */
public abstract class BaseProductGetRunner extends TaskRunner<ProductGetParam, ResponseData<List<HotelProductAndRoom>>> {

    @Override
    public ResponseData<List<HotelProductAndRoom>> runTask(TaskData<ProductGetParam, ResponseData<List<HotelProductAndRoom>>> taskdata) throws Exception {
        ProductGetParam productGetParam = taskdata.getTaskParam();
        ResponseData<List<HotelProductAndRoom>> prodSyncReturnData = ResponseData.error("");
        return prodSyncReturnData;
    }
}
