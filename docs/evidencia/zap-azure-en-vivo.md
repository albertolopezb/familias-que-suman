# OWASP ZAP contra Azure (20 de octubre de 2026)

Escaneo activo corto (fuerza baja, 3 minutos) solo de rutas de lectura y del inicio de sesión, para no crear datos en producción. Sin subir el límite: casi todo recibió 429. El escaneo completo de la entrega está en backend/seguridad/zap/reporte-zap.html.

# ZAP by Checkmarx Scanning Report

ZAP by [Checkmarx](https://checkmarx.com/).


## Summary of Alerts

| Risk Level | Number of Alerts |
| --- | --- |
| High | 0 |
| Medium | 0 |
| Low | 1 |
| Informational | 2 |




## Insights

| Level | Reason | Site | Description | Statistic |
| --- | --- | --- | --- | --- |
| Low | Warning |  | ZAP errors logged - see the zap.log file for details | 1    |
| Low | Warning |  | ZAP warnings logged - see the zap.log file for details | 5    |
| Info | Informational | http://hunter-marilyn-larger-frequent.trycloudflare.com | Percentage of responses with status code 2xx | 100 % |
| Info | Informational | http://hunter-marilyn-larger-frequent.trycloudflare.com | Percentage of endpoints with content type application/json | 100 % |
| Info | Informational | http://hunter-marilyn-larger-frequent.trycloudflare.com | Percentage of endpoints with method GET | 100 % |
| Info | Informational | http://hunter-marilyn-larger-frequent.trycloudflare.com | Count of total endpoints | 6    |
| Info | Informational | http://hunter-marilyn-larger-frequent.trycloudflare.com | Percentage of slow responses | 100 % |
| Info | Informational | https://hunter-marilyn-larger-frequent.trycloudflare.com | Percentage of responses with status code 2xx | 1 % |
| Info | Informational | https://hunter-marilyn-larger-frequent.trycloudflare.com | Percentage of responses with status code 3xx | 2 % |
| Info | Informational | https://hunter-marilyn-larger-frequent.trycloudflare.com | Percentage of responses with status code 4xx | 96 % |
| Info | Informational | https://hunter-marilyn-larger-frequent.trycloudflare.com | Percentage of endpoints with content type application/json | 70 % |
| Info | Informational | https://hunter-marilyn-larger-frequent.trycloudflare.com | Percentage of endpoints with method GET | 94 % |
| Info | Informational | https://hunter-marilyn-larger-frequent.trycloudflare.com | Percentage of endpoints with method POST | 5 % |
| Info | Informational | https://hunter-marilyn-larger-frequent.trycloudflare.com | Count of total endpoints | 17    |
| Info | Informational | https://hunter-marilyn-larger-frequent.trycloudflare.com | Percentage of slow responses | 15 % |




## Alerts

| Name | Risk Level | Number of Instances |
| --- | --- | --- |
| HTTPS Content Available via HTTP | Low | 1 |
| Re-examine Cache-control Directives | Informational | 2 |
| User Agent Fuzzer | Informational | Systemic |




## Alert Detail



### [ HTTPS Content Available via HTTP ](https://www.zaproxy.org/docs/alerts/10047/)



##### Low (Medium)

### Description

Content which was initially accessed via HTTPS (i.e.: using SSL/TLS encryption) is also accessible via HTTP (without encryption).

* URL: https://hunter-marilyn-larger-frequent.trycloudflare.com/v1/salud
  * Node Name: `http://hunter-marilyn-larger-frequent.trycloudflare.com/v1/salud`
  * Method: `GET`
  * Parameter: ``
  * Attack: ``
  * Evidence: `http://hunter-marilyn-larger-frequent.trycloudflare.com/v1/salud`
  * Other Info: `ZAP attempted to connect via: http://hunter-marilyn-larger-frequent.trycloudflare.com/v1/salud`


Instances: 1

### Solution

Ensure that your web server, application server, load balancer, etc. is configured to only serve such content via HTTPS. Consider implementing HTTP Strict Transport Security.

### Reference


* [ https://cheatsheetseries.owasp.org/cheatsheets/HTTP_Strict_Transport_Security_Cheat_Sheet.html ](https://cheatsheetseries.owasp.org/cheatsheets/HTTP_Strict_Transport_Security_Cheat_Sheet.html)
* [ https://owasp.org/www-community/Security_Headers ](https://owasp.org/www-community/Security_Headers)
* [ https://en.wikipedia.org/wiki/HTTP_Strict_Transport_Security ](https://en.wikipedia.org/wiki/HTTP_Strict_Transport_Security)
* [ https://caniuse.com/stricttransportsecurity ](https://caniuse.com/stricttransportsecurity)
* [ https://datatracker.ietf.org/doc/html/rfc6797 ](https://datatracker.ietf.org/doc/html/rfc6797)


#### CWE Id: [ 311 ](https://cwe.mitre.org/data/definitions/311.html)


#### WASC Id: 4

#### Source ID: 1

### [ Re-examine Cache-control Directives ](https://www.zaproxy.org/docs/alerts/10015/)



##### Informational (Low)

### Description

The cache-control header has not been set properly or is missing, allowing the browser and proxies to cache content. For static assets like css, js, or image files this might be intended, however, the resources should be reviewed to ensure that no sensitive content will be cached.

* URL: https://hunter-marilyn-larger-frequent.trycloudflare.com/v1/proyectos
  * Node Name: `https://hunter-marilyn-larger-frequent.trycloudflare.com/v1/proyectos`
  * Method: `GET`
  * Parameter: `cache-control`
  * Attack: ``
  * Evidence: `no-store`
  * Other Info: ``
* URL: https://hunter-marilyn-larger-frequent.trycloudflare.com/v1/salud
  * Node Name: `https://hunter-marilyn-larger-frequent.trycloudflare.com/v1/salud`
  * Method: `GET`
  * Parameter: `cache-control`
  * Attack: ``
  * Evidence: `no-store`
  * Other Info: ``


Instances: 2

### Solution

For secure content, ensure the cache-control HTTP header is set with "no-cache, no-store, must-revalidate". If an asset should be cached consider setting the directives "public, max-age, immutable".

### Reference


* [ https://cheatsheetseries.owasp.org/cheatsheets/Session_Management_Cheat_Sheet.html#web-content-caching ](https://cheatsheetseries.owasp.org/cheatsheets/Session_Management_Cheat_Sheet.html#web-content-caching)
* [ https://developer.mozilla.org/en-US/docs/Web/HTTP/Reference/Headers/Cache-Control ](https://developer.mozilla.org/en-US/docs/Web/HTTP/Reference/Headers/Cache-Control)
* [ https://grayduck.mn/2021/09/13/cache-control-recommendations/ ](https://grayduck.mn/2021/09/13/cache-control-recommendations/)


#### CWE Id: [ 525 ](https://cwe.mitre.org/data/definitions/525.html)


#### WASC Id: 13

#### Source ID: 3

### [ User Agent Fuzzer ](https://www.zaproxy.org/docs/alerts/10104/)



##### Informational (Medium)

### Description

Check for differences in response based on fuzzed User Agent (eg. mobile sites, access as a Search Engine Crawler). Compares the response statuscode and the hashcode of the response body with the original response.

* URL: https://hunter-marilyn-larger-frequent.trycloudflare.com/v1/centros/
  * Node Name: `https://hunter-marilyn-larger-frequent.trycloudflare.com/v1/centros/`
  * Method: `GET`
  * Parameter: `Header User-Agent`
  * Attack: `Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1)`
  * Evidence: ``
  * Other Info: ``
* URL: https://hunter-marilyn-larger-frequent.trycloudflare.com/v1/fotos/testimonios/
  * Node Name: `https://hunter-marilyn-larger-frequent.trycloudflare.com/v1/fotos/testimonios/`
  * Method: `GET`
  * Parameter: `Header User-Agent`
  * Attack: `Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1)`
  * Evidence: ``
  * Other Info: ``
* URL: https://hunter-marilyn-larger-frequent.trycloudflare.com/v1/proyectos/
  * Node Name: `https://hunter-marilyn-larger-frequent.trycloudflare.com/v1/proyectos/`
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


