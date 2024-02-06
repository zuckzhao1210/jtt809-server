package org.wanji.protocol.commons;

import org.junit.Test;
import java.util.HashMap;

/**
 * @author zhaozhe
 * @date 2023/10/11 16:39
 */
public class DataTypeIndex {
    /**
     * key 为 业务类型编码例如 0x9801
     * value 为 dataType字段在数据头和数据体中的位置
     * 如果没有dataType字段,则设置为 -1
      */

    private static HashMap<Integer, Integer> dataTypeIndexMap = new HashMap<>();
    private static int defaultValue = -1;
    static {
        dataTypeIndexMap.put(0x9800, 44);
    }

    public static int get(int msgId) {
        if (dataTypeIndexMap.containsKey(msgId)) {
            return dataTypeIndexMap.get(msgId);
        } else {
            return defaultValue;
        }
    }

    @Test
    public void test(){
        System.out.println(dataTypeIndexMap.get(0x9802));
    }
}
