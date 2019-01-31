package com.emeraldhieu.errorhandler;

import javax.ws.rs.NotFoundException;

import org.json.JSONObject;
import org.springframework.stereotype.Component;

@Component
public class ErrorHandlingService {

    private static final String INVALID_TICKER_SYMBOL_ERROR_MESSAGE = "Invalid ticker symbol";

    private static final String TOO_FAST_REQUEST_ERROR_MESSAGE = "Too many requests at the same time";

    public boolean isTickerValid(JSONObject responseObj) {
        return isResponseValid(responseObj, "QECx02");
    }

    public boolean isRequestSlowEnough(JSONObject responseObj) {
        return isResponseValid(responseObj, "QELx04");
    }

    public void validateTicker(JSONObject responseObj) {
        if (!isResponseValid(responseObj, "QECx02")) {
            throw new NotFoundException(INVALID_TICKER_SYMBOL_ERROR_MESSAGE);
        }
    }

    private boolean isResponseValid(JSONObject responseObj, String quandlErrorCode) {
        if (responseObj.has("quandl_error")) {
            JSONObject errorObject = (JSONObject) responseObj.get("quandl_error");
            String errorCode = errorObject.getString("code");
            if (errorCode.equals(quandlErrorCode)) {
                return false;
            }
        }
        return true;
    }

    public static String getInvalidTickerSymbolErrorMessage() {
        return INVALID_TICKER_SYMBOL_ERROR_MESSAGE;
    }

    public static String getTooFastRequestErrorMessage() {
        return TOO_FAST_REQUEST_ERROR_MESSAGE;
    }
}
