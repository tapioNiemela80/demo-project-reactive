# Portfolio: Kevyt reaktiivinen projektinhallintamalli

Tämä on esimerkki kevyestä projektinhallintamallista, jossa tiimit ja projektit toimivat domain-aggregaatteina. Esimerkin tavoitteena on havainnollistaa domain-keskeistä arkkitehtuuria, jossa liiketoimintasäännöt asuvat aggregaateissa, ei serviceissä. Käytössä on lisäksi reaktiivinen malli, jossa puhdas domain-malli on eriytetty tietokantakerroksesta ns. mappereilla

Arkkitehtuuri on rakennettu siten, että domain-malli on teknisesti riippumaton.
Sovellus käyttää erillisiä mappauskerroksia (domain <-> DTO <-> persistence entity),
mikä mahdollistaa domainin uudelleenkäytön myös toisenlaisen persistenssi- tai
integraatiotekniikan kanssa.

## Tavoite

- Rakentaa testattava ja ymmärrettävä DDD-pohjainen malli.
- Korostaa domainin eheyttä (esim. TimeEstimation ei ole vain `int`, vaan ValueObject).
- Näyttää miten write- ja read-mallit voidaan erottaa kevyesti.
- Näyttää miten reaktiivisen arkkitehtuurin päälle voidaan rakentaa myös DDD-pohjainen malli, jossa puhdas malli on eriytetty tietokantakerroksesta
- Käyttää tapahtumia (esim. `TeamTaskCompletedEvent`) tilan päivittämiseen aggregaattien välillä.


## Teknologiat

- Java 17
- Maven
- PostgreSQL
- Docker
- Spring Boot
- Spring Webflux
- Reaktiivinen malli
- JUnit 5 + Mockito

## Peruskäyttö

Sovellus vaatii PostgreSQL:n, joka ajetaan Dockerin kautta. docker-compose.yml on konfiguroitu seuraavasti:

### .env-tiedosto (luo juureen kopioimalla .env-example ja muokkaamalla)
POSTGRES_PASSWORD=salasana123

PGDATA_VOLUME=/c/Users/demo/postgres-data

#### PostgreSql-kontin käynnistys
```docker compose up -d```

### Spring-boot 
Sovellus olettaa ympäristömuuttujista löytyvän tietokannan salasanan. Sen voi Windowsin cmd-promptissa asettaa esimerkiksi näin ```set POSTGRES_PASSWORD=salasana123```
Itse sovellus käynnistetään ajamalla komento ```mvn spring-boot:run```

## Domainin rakenne

- **Project**: Omistaa alkuarvion (InitialEstimation), projektille lisätään tehtäviä. Projekti itse huolehtii tehtäviä lisättäessä, että arvioitu aika-arvio ei ylity
- **Team**: Omistaa tiimin jäsenet ja vastaa tehtävien hallinnasta.
- **ProjectTask**: On osa projektisuunnittelua. Se määrittää mitä pitää tehdä ja kuinka paljon työtä siihen on alun perin arvioitu.
- **TeamTask**: Edustaa sitä, miten tiimi toteuttaa projektitehtävän: kuka tekee sen, missä vaiheessa se on, ja paljonko todellista aikaa kului.

## Value Objectit

- **TimeEstimation**: Abstraktoi ajan arvion. Estää virheelliset arvot (esim. negatiiviset tunnit).
- **ActualSpentTime**: Kuvaa oikeasti kulunutta aikaa. Voi päivittyä vasta kun task on valmis.
- **ProjectId, ProjectTaskId, TaskId, TeamId, TeamTaskId, TeamMemberId**: Varmistavat oikeat ID-käytännöt ilman paljaita merkkijonoja tai UUID:itä.
- **Email**: Arvotyyppi sähköpostiosoitteelle. Syöte validoidaan aina, ja osoitteen lähetyskelpoisuus arvioidaan erikseen käytön yhteydessä.

## Eventit

Tietyt aggregaattitapahtumat laukaisevat muita päivityksiä järjestelmässä:

- `TaskAddedToProjectEvent`: syntyy, kun uusi taski lisätään projektille, käsittelijä lähettää tästä sähköpostia projektin yhteyshenkilölle. Tämä demonstroi "side-effect":in käsittelyä
- `TeamTaskCompletedEvent`: kun tiimi merkitsee tehtävän valmiiksi, tämän eventin käsittelijä päivittää projektin vastaavan taskin valmiiksi toteutuneen työmäärän kanssa. Projekti itse huolehtii itse siitä, että projekti merkitään valmiiksi jos kaikki sen tehtävät ovat valmiita. Tämän eventin käsittely demonstroi DDD:n perusperiaatetta, että kahta aggregate roottia ei saa tallentaa yhdessä transaktiossa. Eventin käsittely on myös idempotentti. Jos sen käsittelyn aikana tapahtuu optimistisen lukituksen virhe, yritetään uudestaan. Jos puolestaan toinen osapuoli on yrittänyt lisätä tehtävää, tarkistetaan onko projekti jo valmis ja hylätään sen aiheuttama päivitys (jos projekti on jo valmis)

## Sähköposti-ilmoitukset ja suostumusmalli (supportive co-domain)

Kun projektille lisätään uusi taski, järjestelmä voi lähettää tästä
sähköposti-ilmoituksen projektin yhteyshenkilölle. Ilmoituksen lähetys
ei kuitenkaan ole automaattista, vaan se perustuu kahteen ehtoon:

1. Projektin yhteyshenkilön sähköpostiosoitteen tulee olla teknisesti validi
2. Vastaanottaja ei saa olla kieltänyt ilmoituksia (opt-out / consent), esim. GDPR-syistä

Opt-out -logiikka elää omassa **tukidomainissa (supportive co-domain)**, eikä siihen tässä portfoliossa ole tehty REST-kerrosta

Projektin domain ei tunne opt-out -toteutuksen yksityiskohtia, vaan käyttää siihen
ainoastaan `EmailNotificationPolicy`-rajapintaa.

### Tekniset huomiot
Eventit välitetään reaktiivisesti komponentin ReactiveDomainEventPublisher kautta. Kyseessä on ns. hot source, mikä tarkoittaa:

- Eventit alkavat virrata heti kun niitä julkaistaan, eikä niitä säilötä uusille tilaajille
- Vain ne käsittelijät, jotka ovat jo rekisteröityneet ennen eventin julkaisua, saavat eventin
- Kaikki kuuntelijat rekisteröidään sovelluksen käynnistyksessä (ReactiveEventListenerRegistrar-komponentti), jolloin voidaan varmistaa, että ne ehtivät kuulla kaikki eventit
- Käsittelijämetodit määritellään annotaatiolla @ReactiveEventListener, ja niiden tulee palauttaa Mono<Void> tai Mono<T>. Tämä mahdollistaa ei-blokkaavan, ketjutettavan käsittelyn

#### Löyhä kytkentä
Tapahtumien julkaisu ja niiden käsittely on erotettu toisistaan. Julkaisija ei tunne käsittelijöitä eikä ole riippuvainen siitä, onko yksittäinen eventille kuuntelijaa lainkaan. Tämä parantaa modulaarisuutta, testattavuutta ja ylläpidettävyyttä — komponentit voivat kehittyä itsenäisesti.

## REST-endpointit (esimerkit)

### Luo projekti
```curl --location 'http://localhost:8081/projects' --header 'Content-Type: application/json' --data-raw '{"name":"coding project", "description":"portfolio demonstration", "estimatedEndDate": "2026-01-01", "estimation":{"hours":10,"minutes":55}, "contactPersonInput":{"name":"tapio niemelä","email":"tapio.niemela_1@yahoo.com"}}'```

### Lisää taski projektille
```curl --location 'http://localhost:8081/projects/cd8a4243-717b-4181-bb5a-83381f511920/tasks' --header 'Content-Type: application/json' --data '{"name":"java code", "description":"make java code demonstrating ddd and spring data jdbc", "estimation":{"hours":8, "minutes":0}}'```

### Lisää tiimi
```curl --location 'http://localhost:8081/teams' --header 'Content-Type: application/json' --data '{"name":"ddd and spring data jdbc demonstration team"}'```

### Lisää tiimille jäsen
```curl --location 'http://localhost:8081/teams/791031a6-922b-4ea0-93da-ae7b21a7a09b/members' --header 'Content-Type: application/json' --data '{"name":"tapio niemelä", "profession":"ddd enthuistic"}'```

### Poista jäsen tiimistä
```curl --location --request DELETE 'http://localhost:8081/teams/791031a6-922b-4ea0-93da-ae7b21a7a09b/members/c41c9a87-688f-428d-a1d5-4134f1faeeaf'```

### Lisää (projektin) taski tiimille
```curl --location --request POST 'http://localhost:8081/teams/791031a6-922b-4ea0-93da-ae7b21a7a09b/tasks/by-project-id/6e46e573-1bf4-46e9-a633-fb7447e42c16' --data ''```

### Assignoi taski tiimin jäsenelle
```curl --location --request PATCH 'http://localhost:8081/teams/791031a6-922b-4ea0-93da-ae7b21a7a09b/tasks/5ad12dec-34be-40a9-ab9a-0c619b6ae6ab/assignee' --header 'Content-Type: application/json' --data '{"assigneeId":"c41c9a87-688f-428d-a1d5-4134f1faeeaf"}'```

### Ota taski käsittelyyn
```curl --location --request POST 'http://localhost:8081/teams/791031a6-922b-4ea0-93da-ae7b21a7a09b/tasks/5ad12dec-34be-40a9-ab9a-0c619b6ae6ab/mark-in-progress' --data ''```

### Merkitse taski valmiiksi
```curl --location 'http://localhost:8081/teams/791031a6-922b-4ea0-93da-ae7b21a7a09b/tasks/f078044f-72dc-4f70-9230-affece4758db/complete' --header 'Content-Type: application/json' --data '{"hours":2, "minutes":0}'```

### Unassignoi task
```curl --location 'http://localhost:8081/teams/791031a6-922b-4ea0-93da-ae7b21a7a09b/tasks/a41eb504-ae94-40d7-a38f-e0cd2217e5f9/unassign' --header 'Content-Type: application/json' --data ''```

### Poista annettu task tiimiltä
```curl --location --request DELETE 'http://localhost:8081/teams/791031a6-922b-4ea0-93da-ae7b21a7a09b/tasks/a41eb504-ae94-40d7-a38f-e0cd2217e5f9'```

### Hae annettu projekti
```curl --location 'http://localhost:8081/projects/cd8a4243-717b-4181-bb5a-83381f511920' --data ''```

### Hae annettu tiimi
```curl --location 'http://localhost:8081/teams/791031a6-922b-4ea0-93da-ae7b21a7a09b' --data ''```

## Rajoitteet ja huomiot

- Tämä projekti demonstroi lähinnä DDD ja reaktiivisen ohjelmointimallin osaamista. Siinä ei ole toteutettu mm. oikeaa autentikoitumista tai minkäänlaista käyttöliittymää
- Toteutuksessa on tarkoituksella valittu hieman keinotekoinen liiketoimintamalli, jonka tehtävä on antaa sopivan rikas konteksti DDD-rakenteiden esittelemiseen
- Tavoitteena on ollut pitää aggregate-malli keskittyneenä toimintoihin (write). Tietojen hakeminen(read) on toteutettu erikseen suorilla SQL-kyselyillä. Read-malli on tehty kevyesti, koska se ei ole oleellinen osa demoa
- Yksittäisen projektin hakeminen palauttaa näkymän jossa sen sisältämien taskien aikamääreitä on laskettu yhteen. Toinen tapa toteuttaa vastaava olisi ollut kirjoittaa ne tietokantaan päivitysten yhteydessä; tässä valittiin kuitenkin yksinkertaisempi tapa
- Yksikkötestit on tehty vain kriittisille toiminnallisuuksille
- ns. puhdas domain-malli olisi helposti käyttöönotettavissa myös toisessa portfolio-projektissa [https://github.com/tapioNiemela80/demo-project](https://github.com/tapioNiemela80/demo-project-spring-data-jdbc)

### DTO-muunnokset ja mappauslogiikka
- Puhdas domain-malli muunnetaan DTO-muotoon ja takaisin kahden erillisen mappaustason kautta:

    - DomainMapper (esim. ProjectDomainMapper) vastaa domain-olioiden (Project) ja DTO:n (ProjectDTO) välisestä mappauksesta.

    - PersistenceMapper (esim. ProjectPersistenceMapper) huolehtii entiteettien (ProjectEntity) ja DTO:n (ProjectDTO) välisestä mappauksesta.

- Molemmat mappaukset käyttävät DTO:ta (ProjectDTO) yhteisenä muotona domainin ja persistenssin välillä.

- Tämä arkkitehtuuri:

    - Eristää domainin kokonaan persistenssikerroksesta

    - Mahdollistaa sen, ettei domain-olioiden tarvitse paljastaa get-metodeja paketin ulkopuolelle

    - Säilyttää domain-olioiden kapseloinnin: niillä voi olla toDto()-metodi, mutta sitä käytetään ainoastaan DomainMapper-luokan kautta

## Kehittäjä

- Toteuttanut Tapio Niemelä. Portfolio toimii todisteena osaamisesta:
- Java + Spring Boot + Spring reaktiivinen kehys
- Domain Driven Design (aggregaatit, säännöt, eventit)
- Selkeä kerrosarkkitehtuuri, jossa domain on riippumaton persistenssistä ja HTTP-rajapinnasta
- Käytännöllinen REST-rajapinta
