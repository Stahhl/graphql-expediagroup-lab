Graphql-kotlin app byggd med ramverk https://opensource.expediagroup.com/graphql-kotlin/docs

Dokumentationen är väldigt basic med det finns utförligt exempel repo:
https://github.com/ExpediaGroup/graphql-kotlin/tree/master/examples/server/spring-server/src/main/kotlin/com/expediagroup/graphql/examples/server/spring

# Bakgrund

Jag kommer från dotnet / nodejs världen och har en del vana att köra graphql där med ramverk som hotchocolate och apollo.
Nu försöker jag hitta någon ramverk jag gillar inom spring boot / kotlin världen. 
Detta från expediagroup verkar trevligt.
Målet är code-first, kotlin och typsäkerhet överallt. Med minimal boilerplate. 

# App
Jag har satt upp denna lilla demo app med DDD design. Med funktionella mönster via Arrow.

### app/domain
Domän-lager, domän-typer skall inte exponeras direkt i api-schemat.
### app/app
Applikations-lager, använder UseCase-abstraktion för användningsfall / affärslogik.
### app/infra
Infrastruktur-lager, mockade tjänster med hårdkodad data. Repository kan ses som en databas medans Service ett externt api.
### app/api
Api-lager, så som jag tänker det.
* Api-modeller tar en privat domän-typ via konstruktor och exponerar valda fält. 
* Relationer laddas via funktioner där vi inject:ar tjänster.
* Vi använder egna scalar-typer så mycket som möjligt.
* Vi använder union-typer så mycket som möjligt.
* Mutationer inject:ar våra UseCase och mappar output till api-typer.

# Frågetecken
* Använder vi ramverket så som de gör internt på exediagroup? Något vi kan göra bättre?
* Finns det några prestanda problem med den nuvarande lösningen? När man vandrar grafen tex fram och tillbaka mellan book och author?
* Att inject:a tjänster på funktionsnivå funkar bra men nu måste vi sätta två annoterignar @GraphQLIgnore @Autowired på varje. Kan det göras snygagre?
* Skulle vilja flytta så att EmailScalar lever i domän-lagret med ett minimalt lager i api-lagret för felmeddelande osv.
* Något annat som kan förbättras?
