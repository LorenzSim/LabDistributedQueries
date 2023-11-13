package be.ucll.da.apigateway.domain;

public class HospitalServiceException extends RuntimeException{
    public HospitalServiceException() {
    }

    public HospitalServiceException(String message) {
        super(message);
    }

    public HospitalServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public HospitalServiceException(Throwable cause) {
        super(cause);
    }

    public HospitalServiceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
