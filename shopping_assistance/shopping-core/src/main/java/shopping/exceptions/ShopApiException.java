package shopping.exceptions;

public class ShopApiException extends RuntimeException{

    public ShopApiException(String message){
        super(message);
    }

    public ShopApiException(String message, Throwable cause){
        super(message, cause);
    }
}
