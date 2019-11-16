# Fake Server

This project provides a local HTTP server for testing the Duel itself.

## Server Endpoints

The following endpoints are available while running the server.

| Path               | Description |
|--------------------|-------------|
|/echo               | Returns the JSON in the response body that is the same as the one sent by the request.|
|/report             | Returns the diagnostic report about the received request.|
|/status?code={value}| Returns the specified status code.|
|/json/*             | Returns a JSON file with "application/json" content-type.|
