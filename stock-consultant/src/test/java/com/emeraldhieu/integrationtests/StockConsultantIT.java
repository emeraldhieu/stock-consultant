package com.emeraldhieu.integrationtests;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.springframework.boot.SpringApplication;
import org.springframework.util.SocketUtils;

import com.emeraldhieu.Application;

import io.restassured.RestAssured;

@RunWith(Suite.class)
@SuiteClasses({ClosePriceServiceIT.class, DmaServiceIT.class, MultiDmaServiceIT.class})
public class StockConsultantIT {

    @BeforeClass
    public static void startingApplicationUp() {
        RestAssured.basePath = "/.rest/api/v2";
        int port = SocketUtils.findAvailableTcpPort();
        RestAssured.port = port;
        SpringApplication springApplication = new SpringApplication(Application.class);
        springApplication.run("--server.port=" + port).registerShutdownHook();
    }

    @AfterClass
    public static void shuttingDownApplication() {
        given().basePath("/").when()
                .contentType("application/json")
                .post("/actuator/shutdown")
                .then()
                .statusCode(200)
                .body("message", equalTo("Shutting down, bye..."));
    }
}