package com.emeraldhieu.integrationtests;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import org.junit.Test;

public class MultiDmaServiceIT {

    @Test
    public void validRequestReturns200() {
        // TODO Find a better way to assert unorder list of dmas. JsonPath could be used.
        given().when()
                .get("/multi?tickers=FB,MSFT,TWTR&startDate=2000-01-01")
                .then()
                .statusCode(200)
                .body("", hasKey("200dma"))
                .root("200dma")
                .body(
                        "dmas[0].ticker", either(equalTo("FB")).or(equalTo("MSFT")).or(equalTo("TWTR")),
                        "dmas[0]", either(hasKey("avg")).or(hasEntry("errorMessage", "Too many requests at the same time")),
                        "dmas[1].ticker", either(equalTo("FB")).or(equalTo("MSFT")).or(equalTo("TWTR")),
                        "dmas[1]", either(hasKey("avg")).or(hasEntry("errorMessage", "Too many requests at the same time")),
                        "dmas[2].ticker", either(equalTo("FB")).or(equalTo("MSFT")).or(equalTo("TWTR")),
                        "dmas[2]", either(hasKey("avg")).or(hasEntry("errorMessage", "Too many requests at the same time"))
                );
    }

    @Test
    public void invalidTickerReturnsDataWithErrorMessage() {
        // TODO Find a better way to assert unorder list of dmas. JsonPath could be used.
        given().when()
                .get("/multi?tickers=FB,TIKI&startDate=2000-01-01")
                .then()
                .statusCode(200)
                .body("", hasKey("200dma"))
                .root("200dma")
                .body(
                        "dmas[0].ticker", either(equalTo("FB")).or(equalTo("TIKI")),
                        "dmas[0]", either(hasKey("avg")).or(hasKey("errorMessage")),
                        "dmas[1].ticker", either(equalTo("FB")).or(equalTo("TIKI")),
                        "dmas[1]", either(hasKey("avg")).or(hasKey("errorMessage"))
                );
    }

    @Test
    public void omittingStartDateReturnsDataOfPossibleStartDate() {
        given().when()
                .get("/multi?tickers=FB,MSFT,TWTR")
                .then()
                .statusCode(404)
                .body(
                        "", hasKey("error"),
                        "error.code", equalTo("notFound"),
                        "error.message", containsString("Invalid date")
                );
    }

    @Test
    public void noDataReturnsDataOfPossibleStartDate() {
        // TODO Find a better way to assert unorder list of dmas. JsonPath could be used.
        given().when()
                .get("/multi?tickers=FB,MSFT,TWTR&startDate=3000-01-01")
                .then()
                .statusCode(200)
                .body("", hasKey("200dma"))
                .root("200dma")
                .body(
                        "dmas[0].ticker", either(equalTo("FB")).or(equalTo("MSFT")).or(equalTo("TWTR")),
                        "dmas[0].errorMessage", either(containsString("There is no data for this 'startDate'. Try again with")).or(equalTo("Too many requests at the same time")),
                        "dmas[1].ticker", either(equalTo("FB")).or(equalTo("MSFT")).or(equalTo("TWTR")),
                        "dmas[1].errorMessage", either(containsString("There is no data for this 'startDate'. Try again with")).or(equalTo("Too many requests at the same time")),
                        "dmas[2].ticker", either(equalTo("FB")).or(equalTo("MSFT")).or(equalTo("TWTR")),
                        "dmas[2].errorMessage", either(containsString("There is no data for this 'startDate'. Try again with")).or(equalTo("Too many requests at the same time"))
                );
    }
}
