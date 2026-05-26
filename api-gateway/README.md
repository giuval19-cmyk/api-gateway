# 🌐 API Gateway Service

Il servizio **API Gateway** funge da punto di ingresso unico (*Reverse Proxy*) per tutto il traffico client verso l'ecosistema dei microservizi. È sviluppato utilizzando **Spring Cloud Gateway** e agisce come scudo di sicurezza e vigile urbano dell'intera architettura.

---

## 🚀 Funzionalità Principali & Competenze Dimostrate

* **Routing Dinamico e Intelligente:** Il Gateway non ha indirizzi IP dei microservizi scritti nel codice (*hardcoded*). Dialoga direttamente con il **Service Registry (Eureka)** per scoprire istantaneamente dove si trovano i servizi (`iam-service`, `support-service`, ecc.) e instradare il traffico di conseguenza.
* **Sicurezza Perimetrale (Validazione JWT stateless):** Intercetta ogni singola richiesta in entrata. Prima di inoltrare la chiamata ai microservizi interni, estrae il token JWT dall'header `Authorization`, ne verifica la validità e la firma, bloccando sul nascere i tentativi di accesso non autorizzati (Risposta `401 Unauthorized`).
* **Trasparenza dell'Infrastruttura:** Nasconde la complessità della rete interna. Il client (es. un'applicazione frontend) parla solo con la porta del Gateway (`8080`), senza sapere quanti microservizi ci siano dietro o su quali porte stiano girando.
* **Iniezione di Header (Forward Strategy):** È configurato con `server.forward-headers-strategy=framework` per preservare gli header originari della richiesta (come l'host o il protocollo), garantendo che i microservizi interni ricevano correttamente i metadati del client.