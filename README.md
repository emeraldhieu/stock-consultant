# Stock Consultant

Stock Consultant serves stock data of all companies around the world based on professional Quandl stock database.

## Getting started

(To be updated)

## Querying close price

```
GET /api/v2/<ticker>/closePrice?startDate=<startDate>&endDate=<endDate>
```

| Parameters  | Description | Value format | Default |
| -------------------| ----------- | ----- | ------- |
| `ticker `          | Ticker symbol of requested data | Alphabet   | |
| `startDate `       | Start date of requested data.   | yyyy-mm-dd | |
| `endDate`          | End date of requested data      | yyyy-mm-dd | |

##### Example

```sh
curl -X GET \
"http://<host>/api/v2/FB/closePrice?startDate=2014-01-01&endDate=2014-12-03"
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
GET /api/v2/<ticker>/200dma?startDate=<startDate>
```

| Parameters  | Description | Value format | Default |
| -------------------| ----------- | ----- | ------- |
| `ticker `          | Ticker symbol of requested data | Alphabet   | |
| `startDate `       | Start date of requested data.   | yyyy-mm-dd | |

##### Example

```sh
curl -X GET \
"http://<host>/api/v2/FB/200dma?startDate=2000-01-01"
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
GET /api/v2/multi?tickers=<tickers>&startDate=<startDate>
```

| Parameters  | Description | Value format | Default |
| -------------------| ----------- | ----- | ------- |
| `tickers`          | Ticker symbols of requested data; separated by comma | tickerA,tickerB,tickerC   | |
| `startDate `       | Start date of requested data.   | yyyy-mm-dd | |

##### Example

```sh
curl -X GET \
"http://<host>/api/v2/multi?tickers=FB,MSFT,TWTR,GOOGL,AMZN&startDate=2000-01-01"
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

(To be updated)

##### Response

(To be updated)

##### Unknown exceptions

As for the exceptions that are not mentioned above, the status code is 500 and the error code is "unknown".


