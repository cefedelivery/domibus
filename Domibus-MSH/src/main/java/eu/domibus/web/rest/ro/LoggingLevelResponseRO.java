package eu.domibus.web.rest.ro;

public class LoggingLevelResponseRO extends LoggingLevelRO {

    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    private Result result;

    public Result getResult() {
        return result;
    }

    public void setResult(Result result) {
        this.result = result;
    }

    public enum Result {
        SUCCESS, ERROR
    }
}
