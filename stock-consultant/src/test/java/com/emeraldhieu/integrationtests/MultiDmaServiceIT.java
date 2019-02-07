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
                        "dmas[0]", hasKey("avg"),
                        "dmas[1].ticker", either(equalTo("FB")).or(equalTo("MSFT")).or(equalTo("TWTR")),
                        "dmas[1]", hasKey("avg"),
                        "dmas[2].ticker", either(equalTo("FB")).or(equalTo("MSFT")).or(equalTo("TWTR")),
                        "dmas[2]", hasKey("avg")
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
        // TODO Find a better way to assert unorder list of dmas. JsonPath could be used.
        given().when()
                .get("/multi?tickers=FB,MSFT,TWTR")
                .then()
                .statusCode(200)
                .body("", hasKey("200dma"))
                .root("200dma")
                .body(
                        "dmas[0].ticker", either(equalTo("FB")).or(equalTo("MSFT")).or(equalTo("TWTR")),
                        "dmas[0]", hasKey("avg"),
                        "dmas[1].ticker", either(equalTo("FB")).or(equalTo("MSFT")).or(equalTo("TWTR")),
                        "dmas[1]", hasKey("avg"),
                        "dmas[2].ticker", either(equalTo("FB")).or(equalTo("MSFT")).or(equalTo("TWTR")),
                        "dmas[2]", hasKey("avg")
                );
    }
}
