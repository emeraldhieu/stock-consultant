package com.emeraldhieu;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import org.springframework.stereotype.Component;

@Component
@ApplicationPath(".rest")
public class JaxRsApplication extends Application {

}