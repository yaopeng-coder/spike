package cn.hust.spike.Common;

/**
 * @program: spike
 * @author: yaopeng
 * @create: 2020-02-21 14:22
 **/
public enum ResponseCode {

    SUCCESS(0,"SUCCESS"),
    ERROR(1,"ERROR"),
    NEED_LOGIN(10,"NEED_LOGIN"),
    NO_STOCK(11,"NO_STOCK"),
    ILLEGAL_ARGUMENT(2,"ILLEGAL_ARGUMENT");


    private final int code;
    private final String desc;


    ResponseCode(int code,String desc){
        this.code = code;
        this.desc = desc;
    }

    public int getCode(){
        return code;
    }
    public String getDesc(){
        return desc;
    }

}
