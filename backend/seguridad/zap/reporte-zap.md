# OWASP ZAP · API de Familias que Suman

ZAP by [Checkmarx](https://checkmarx.com/).


## Summary of Alerts

| Risk Level | Number of Alerts |
| --- | --- |
| High | 0 |
| Medium | 0 |
| Low | 0 |
| Informational | 1 |




## Insights

| Level | Reason | Site | Description | Statistic |
| --- | --- | --- | --- | --- |
| Low | Warning |  | ZAP errors logged - see the zap.log file for details | 1    |
| Low | Warning |  | ZAP warnings logged - see the zap.log file for details | 10    |
| Low | Exceeded High | http://127.0.0.1:8000 | Percentage of responses with status code 4xx | 91 % |
| Info | Informational | http://127.0.0.1:8000 | Percentage of responses with status code 2xx | 7 % |
| Info | Informational | http://127.0.0.1:8000 | Percentage of responses with status code 3xx | 1 % |
| Info | Informational | http://127.0.0.1:8000 | Percentage of endpoints with content type application/json | 75 % |
| Info | Informational | http://127.0.0.1:8000 | Percentage of endpoints with method DELETE | 20 % |
| Info | Informational | http://127.0.0.1:8000 | Percentage of endpoints with method GET | 36 % |
| Info | Informational | http://127.0.0.1:8000 | Percentage of endpoints with method PATCH | 3 % |
| Info | Informational | http://127.0.0.1:8000 | Percentage of endpoints with method POST | 18 % |
| Info | Informational | http://127.0.0.1:8000 | Percentage of endpoints with method PUT | 20 % |
| Info | Informational | http://127.0.0.1:8000 | Count of total endpoints | 91    |




## Alerts

| Name | Risk Level | Number of Instances |
| --- | --- | --- |
| User Agent Fuzzer | Informational | Systemic |




## Alert Detail



### [ User Agent Fuzzer ](https://www.zaproxy.org/docs/alerts/10104/)



##### Informational (Medium)

### Description

Check for differences in response based on fuzzed User Agent (eg. mobile sites, access as a Search Engine Crawler). Compares the response statuscode and the hashcode of the response body with the original response.

* URL: http://127.0.0.1:8000/v1/centros/
  * Node Name: `http://127.0.0.1:8000/v1/centros/`
  * Method: `GET`
  * Parameter: `Header User-Agent`
  * Attack: `Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1)`
  * Evidence: ``
  * Other Info: ``
* URL: http://127.0.0.1:8000/v1/fotos/testimonios/
  * Node Name: `http://127.0.0.1:8000/v1/fotos/testimonios/`
  * Method: `GET`
  * Parameter: `Header User-Agent`
  * Attack: `Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.0)`
  * Evidence: ``
  * Other Info: ``
* URL: http://127.0.0.1:8000/v1/fotos/testimonios/
  * Node Name: `http://127.0.0.1:8000/v1/fotos/testimonios/`
  * Method: `GET`
  * Parameter: `Header User-Agent`
  * Attack: `Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1)`
  * Evidence: ``
  * Other Info: ``

Instances: Systemic


### Solution



### Reference


* [ https://owasp.org/wstg ](https://owasp.org/wstg)



#### Source ID: 1


