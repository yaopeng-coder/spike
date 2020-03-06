package cn.hust.spike.common;

import lombok.Data;

/**
 * @program: spike
 * @author: yaopeng
 * @create: 2020-02-21 15:08
 **/
@Data
public class Const {

    public static final String CONTENT_TYPE_FORMED = "application/x-www-form-urlencoded";

    public static final String CURRENT_USER = "CURRENT_USER";


    public enum Stock_Status{

        INIT_STOCK_STATUS(1,"初始化状态"),
        STOCK_DECREASE_SUCCESS(2,"库存扣减成功"),
        STOCK_ROLLBACK(3,"下单失败回滚");


        private  int code;
        private  String desc;

        Stock_Status(Integer code, String desc){
            this.code =code;
            this.desc = desc;
        }

        public int getCode(){
            return code;
        }
        public String getDesc(){
            return desc;
        }
    }
}
