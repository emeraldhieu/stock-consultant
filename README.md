# Stock Consultant

Stock Consultant serves stock data of all companies around the world based on professional Quandl stock database.

## Getting started

Check out the project. In project's root directory, run the following commands.

`cd stock-consultant` # Change directory to "stock-consultant"

You can do either one of these

##### Run the project using Maven plugin

`mvn spring-boot:run`

##### Build and execute a JAR file

`mvn clean install` # Build a jar file into "target" folder

`cd target` # Change directory to "target" folder

`java -jar stock-consultant-1.0-SNAPSHOT-exec.jar` # Run the executable

#### Check it out!

Open `http://localhost:8080/.rest/api/v2/hello` on your web browser

`Hello world` displayed means it's working!!

⚠️ Make sure port 8080 is available before executing above commands.

## Querying close price

```
GET /.rest/api/v2/<ticker>/closePrice?startDate=<startDate>&endDate=<endDate>
```

| Parameters  | Description | Value format | Default |
| -------------------| ----------- | ----- | ------- |
| `ticker `          | Ticker symbol of requested data | Alphabet   | |
| `startDate `       | Start date of requested data.   | yyyy-mm-dd | |
| `endDate`          | End date of requested data      | yyyy-mm-dd | |

##### Example

```sh
curl -X GET \
"http://<host>/.rest/api/v2/FB/closePrice?startDate=2014-01-01&endDate=2014-12-03"
```

##### Response

```json
{
  "prices": [
    {
      "ticker": "FB",
      "dateClose": [
        [
          "2014-12-03",
          "74.88"
        ],
        [
          "2014-12-02",
          "75.46"
        ],
        [
          "2014-12-01",
          "75.1"
        ]
      ]
    }
  ]
}
```

For conciseness, only three data rows appear in the example response.

## Querying 200-day-moving average

```
GET /.rest/api/v2/<ticker>/200dma?startDate=<startDate>
```

| Parameters  | Description | Value format | Default |
| -------------------| ----------- | ----- | ------- |
| `ticker `          | Ticker symbol of requested data | Alphabet   | |
| `startDate `       | Start date of requested data.   | yyyy-mm-dd | |

##### Example

```sh
curl -X GET \
"http://<host>/.rest/api/v2/FB/200dma?startDate=2000-01-01"
```

##### Response

```json
{
  "200dma": {
    "ticker": "FB",
    "avg": "25.6699035"
  }
}
```

## Querying a list of 200-day-moving averages

```
GET /.rest/api/v2/multi?tickers=<tickers>&startDate=<startDate>
```

| Parameters  | Description | Value format | Default |
| -------------------| ----------- | ----- | ------- |
| `tickers`          | Ticker symbols of requested data; separated by comma | tickerA,tickerB,tickerC   | |
| `startDate `       | Start date of requested data.   | yyyy-mm-dd | |

##### Example

```sh
curl -X GET \
"http://<host>/.rest/api/v2/multi?tickers=FB,MSFT,TWTR,GOOGL,AMZN&startDate=2000-01-01"
```

##### Response

```json
{
  "200dma": {
    "dmas": [
      {
        "avg": "25.6699035",
        "ticker": "FB"
      },
      {
        "avg": "80.3493",
        "ticker": "MSFT"
      },
      {
        "avg": "46.45835",
        "ticker": "TWTR"
      },
      {
        "avg": "179.4554",
        "ticker": "GOOGL"
      },
      {
        "avg": "51.69955",
        "ticker": "AMZN"
      }
    ]
  }
}
```

## Exception handling

Exceptions are caught in exception mappers and responses are structurally displayed in a requested media type

* If the requested media type is not supported, JSON is returned as fallback
* If no resource method is matched, JSON is returned as fallback

The tables below show several common exceptions, their status code and error code respectively.

### JAX-RS Exceptions

| Exception              | HTTP status code | Error code       | Description                           | 
|------------------------|------------------|------------------|---------------------------------------| 
| NotAuthorizedException | 401              | notAuthorized    | Unauthorized                          | 
| BadRequestException    | 400              | badRequest       | Bad request                           | 
| NotAllowedException    | 405              | methodNotAllowed | Request method is not supported       | 
| NotAcceptableException | 406              | notAcceptable    | Requested media type is not supported | 
| NotFoundException      | 404              | notFound         | Resource not found                    | 
| ReaderException        | 400              | readerError      | JAX-RS exception                      | 
| WriterException        | 500              | writerError      | JAX-RS exception                      | 

##### Example

```sh
curl -X GET \
"http://<host>/.rest/api/v2/WATERSTONES/closePrice?startDate=2014-01-01&endDate=2014-07-20"
```

##### Response

```json
{
  "error": {
    "code": "notFound",
    "message": "Invalid ticker symbol"
  }
}
```

##### Unknown exceptions

As for the exceptions that are not mentioned above, the status code is 500 and the error code is "unknown".

## Caching

(To be updated)

## TODO

The project still needs more time for improvements.

These are the TODOs that I am going to do in the near future.

* Add unit tets and integration tests for caching
* Provide coverage and more unit tests
* Implement caching for `/multi` endpoint
* Improve performance of `/multi` endpoint
* Extract caching processing into ContainerRequestFilter and ResponseRequestFilter

