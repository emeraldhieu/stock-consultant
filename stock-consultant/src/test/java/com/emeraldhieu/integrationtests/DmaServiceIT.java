package com.emeraldhieu.integrationtests;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import org.junit.Test;

public class DmaServiceIT extends AbstractIT {

    @Test
    public void validRequestReturns200() {
        given().when()
                .get("FB/200dma?startDate=2014-01-01&endDate=2014-12-03")
                .then()
                .statusCode(200)
                .body(
                        "", hasKey("200dma"),
                        "200dma.ticker", equalTo("FB"),
                        "200dma", hasKey("avg")
                );
    }

    @Test
    public void invalidTickerReturns404() {
        given().when()
                .get("AnInvalidTicker/200dma?startDate=2000-01-01")
                .then()
                .statusCode(404)
                .body(
                        "", hasKey("error"),
                        "error.code", equalTo("notFound"),
                        "error.message", equalTo("Invalid ticker symbol")
                );
    }

    @Test
    public void invalidStartDateReturns404() {
        given().when()
                .get("FB/200dma?startDate=somethingInvalid")
                .then()
                .statusCode(404)
                .body(
                        "", hasKey("error"),
                        "error.code", equalTo("notFound"),
                        "error.message", equalTo("Invalid date")
                );
    }

    @Test
    public void omittingStartDateReturns404AndSuggestsAPossibleStartDate() {
        given().when()
                .get("FB/200dma?endDate=2014-12-03")
                .then()
                .statusCode(404)
                .body(
                        "", hasKey("error"),
                        "error.code", equalTo("notFound"),
                        "error.message", containsString("'startDate' is missing. Try again with")
                );
    }
}
