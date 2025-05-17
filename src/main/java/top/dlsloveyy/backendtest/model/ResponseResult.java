package top.dlsloveyy.backendtest.model;

public class ResponseResult {
    private int code;
    private String message;
    private Object data;

    public static ResponseResult success(String message) {
        ResponseResult result = new ResponseResult();
        result.setCode(200);
        result.setMessage(message);
        return result;
    }

    public static ResponseResult error(String message) {
        ResponseResult result = new ResponseResult();
        result.setCode(400);
        result.setMessage(message);
        return result;
    }

    // Getter & Setter
    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
