package com.emeraldhieu.integrationtests;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import org.junit.Test;

public class ClosePriceServiceIT {

    @Test
    public void validRequestReturns200() {
        given().when()
                .get("FB/closePrice?startDate=2014-01-01&endDate=2014-12-03")
                .then()
                .statusCode(200)
                .body(
                        "", hasKey("prices"),
                        "prices[0].ticker", equalTo("FB"),
                        "prices[0]", hasKey("dateClose")
                );
    }

    @Test
    public void invalidTickerReturns404() {
        given().when()
                .get("AnInvalidTicker/closePrice?startDate=2014-01-01&endDate=2014-12-03")
                .then()
                .statusCode(404)
                .body(
                        "", hasKey("error"),
                        "error.code", equalTo("notFound"),
                        "error.message", equalTo("Invalid ticker symbol")
                );
    }

    @Test
    public void invalidDateRangeReturns404() {
        given().when()
                .get("FB/closePrice?startDate=2014-12-03&endDate=2014-01-01")
                .then()
                .statusCode(404)
                .body(
                        "", hasKey("error"),
                        "error.code", equalTo("notFound"),
                        "error.message", equalTo("Invalid date range")
                );
    }

    @Test
    public void invalidStartDateReturns404() {
        given().when()
                .get("FB/closePrice?startDate=somethingInvalid&endDate=2014-12-03")
                .then()
                .statusCode(404)
                .body(
                        "", hasKey("error"),
                        "error.code", equalTo("notFound"),
                        "error.message", equalTo("Invalid date")
                );
    }

    @Test
    public void invalidEndDateReturns404() {
        given().when()
                .get("FB/closePrice?startDate=2014-01-01&endDate=somethingInvalid")
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
                .get("FB/closePrice?endDate=2014-12-03")
                .then()
                .statusCode(404)
                .body(
                        "", hasKey("error"),
                        "error.code", equalTo("notFound"),
                        "error.message", containsString("'startDate' is missing. Try again with")
                );
    }

    @Test
    public void omittingEndDateReturnsDataWithLatestEndDate() {
        given().when()
                .get("FB/closePrice?startDate=2014-01-01")
                .then()
                .statusCode(200)
                .body(
                        "", hasKey("prices"),
                        "prices[0].ticker", equalTo("FB"),
                        "prices[0]", hasKey("dateClose")
                );
    }
}
