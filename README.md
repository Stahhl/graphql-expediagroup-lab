Graphql-kotlin app byggd med ramverk https://opensource.expediagroup.com/graphql-kotlin/docs

Dokumentationen är väldigt basic men det finns utförligt exempel repo:
https://github.com/ExpediaGroup/graphql-kotlin/tree/master/examples/server/spring-server/src/main/kotlin/com/expediagroup/graphql/examples/server/spring

# Bakgrund

Jag kommer från dotnet / nodejs världen och har en del vana att köra graphql där med ramverk som hotchocolate och apollo.
Nu försöker jag hitta någon ramverk jag gillar inom spring boot / kotlin världen.
Detta från expediagroup verkar trevligt.
Målet är code-first, kotlin och typsäkerhet överallt. Med minimal boilerplate.

# App

Liten demo-app med DDD-design och funktionella mönster via Arrow.

### app/domain
Domän-lager. Domän-typer exponeras inte direkt i api-schemat — med ett undantag:
`Email` är en domän-typ som api-lagret mappar till en egen scalar (se nedan).

### app/app
Applikations-lager. `UseCase`-abstraktion för användningsfall / affärslogik, med
`Either<Error, Output>` som resultat.

### app/infra
Infrastruktur-lager, mockade tjänster med hårdkodad data. `Database` motsvarar en databas,
`ReviewApi` ett externt api. Båda exponerar **batchade** `find*`-metoder som tar en mängd
nycklar — det är signaturen en DataLoader behöver.

### app/api
* Api-modeller tar en privat domän-typ via konstruktor och exponerar valda fält.
* Relationer laddas via **DataLoader**, inte via direkta uppslag. Se `DataLoaders.kt`.
* Egna scalar-typer: domäntypen bor i `app/domain`, GraphQL-glue i `app/api/scalars`.
  Registrering sker automatiskt — se nedan.
* Union-typer används för mutationsresultat (`CreateBookOk | CreateBookError`).
* Mutationer inject:ar våra UseCase och mappar output till api-typer.
* Schemanamn styrs med `@GraphQLName` så Kotlin-namn inte läcker ut.

Kör: `./gradlew bootRun` → http://localhost:8081/graphiql, schema på `/sdl`.

# Anteckningar från genomgången

### Använder vi ramverket som de gör på expediagroup?
I stort ja. Det som skilde var att relationer laddades med direkta uppslag i stället för
DataLoaders, och att Kotlin-namn läckte ut i schemat (`getBook`, `BookType`, `bookResponse`).
Båda är åtgärdade.

### Prestanda
Två problem, båda fixade:

* **N+1.** `{ books { author { books { author } } } }` gjorde 8 separata författaruppslag
  för 2 unika författare. Med DataLoaders blir det 1 batch (`keys=[author1, author2]`).
* **Cyklisk graf.** `Book <-> Author <-> Book` kan nästlas obegränsat; en liten query gav
  147 resolver-anrop. `MaxQueryDepthInstrumentation` + `MaxQueryComplexityInstrumentation`
  i `GraphQLConfig.kt` sätter tak. Instrumentation-beans plockas upp automatiskt.

Notera: `graphql.batching.enabled=true` (SYNC_EXHAUSTION) är medvetet **av**. Den låser
sig när en DataLoader används inuti en mutation — se kommentaren i `application.properties`.

### @GraphQLIgnore + @Autowired på varje parameter
Går att få ner till **en** annotering. Schemageneratorn tittar bara efter `@GraphQLIgnore`
och `SpringDataFetcher` bara efter `@Autowired`; ingen av dem löser meta-annoteringar, så
en egen sammansatt annotering fungerar inte. Men `dataFetcherFactoryProvider` är
`@ConditionalOnMissingBean` och därmed utbytbar — se `BeanInjectingDataFetcher` i
`GraphQLConfig.kt`. Priset är att `@GraphQLIgnore` på en parameter nu betyder "det här är
en bean". `@Autowired` fungerar fortfarande via `super`, inklusive `@Qualifier`.

Relationer använder ändå `DataFetchingEnvironment` + DataLoader och behöver ingen
annotering alls, så mekanismen finns kvar för tjänster som inte är nyckelbaserade uppslag.

### Email i domän-lagret
`Email` är en value class med privat konstruktor och `Email.of(): Either<Invalid, Email>` —
en instans är per definition validerad. Domänen känner inte till graphql-java.
`decode` returnerar domäntypen direkt, vilket gör att ramverket aldrig anropar den privata
konstruktorn reflektivt — valideringen går inte att kringgå.

### Egna scalars
En scalar definieras på ett enda ställe, som en klass i `app/api/scalars`:

```kotlin
@Component
class EmailScalar : StringApiScalar<Email>(
    name = "Email",
    description = "A valid email address",
) {
    override fun encode(value: Email): String = value.value

    override fun decode(raw: String): Email =
        Email.of(raw).getOrNull() ?: invalid("Invalid email format: $raw")
}
```

`@Component` räcker — `Hooks` injicerar alla `ApiScalar`-beans och bygger registret. Ingen
manuell mappning att komma ihåg. Domäntypen läses ur typparametern via Spring
`ResolvableType`, samma mekanism Spring använder för `Converter<S, T>`.

`StringApiScalar` implementerar alla tre `Coercing`-metoderna, packar upp `StringValue` ur
AST:n och översätter `invalid(...)` till rätt exception beroende på om värdet kom som literal
eller variabel. Behöver du en annan serialiserad representation ärver du `ApiScalar<D, S>`
direkt. Metoderna heter `encode`/`decode` eftersom `Coercing` redan deklarerar
`serialize(Object)` — `serialize(Domain)` hade krockat på JVM-signatur efter erasure.

**Glömmer du en scalar?** En ny domäntyp kräver ingenting i api-lagret så länge den aldrig
exponeras i schemat. Exponeras den utan `ApiScalar` startar appen inte:

```
com.example.graphqldemo.app.domain.Isbn is exposed in the GraphQL schema but has no
ApiScalar. Without one it silently becomes an object type. Add an ApiScalar in
app/api/scalars, or list the class in Hooks.ALLOW_OBJECT_TYPE if the object type is intended.
```

Utan den kontrollen hade ramverket tyst genererat `type Isbn { value: String! }` —
`willGenerateGraphQLType` körs först av allt, och returnerar den null faller typen vidare
till `objectFromReflection`. Kontrollen ligger i samma hook, så den ser bara typer som
faktiskt når schemat.

`Hooks` avvisar också två `ApiScalar` med samma schemanamn eller samma domäntyp. Det första
fallet skulle annars ge graphql-javas intetsägande *"redefined the type 'Email' from being a
'GraphQLScalarType' to a 'GraphQLScalarType'"*; det andra upptäcks inte av någon alls — sista
registreringen hade vunnit tyst. Namnet är ett runtime-värde, så kompilatorn kan inte fånga
det, men `SchemaSnapshotTest` bygger schemat under `./gradlew build` och fäller bygget.

### Buggar som fixades på vägen
* `Book.publisher` använde `.single()` på ett non-null-fält. En bok utan förlag kastade
  `NoSuchElementException` och sänkte hela svaret. Nu nullable.
* `createBook` uppdaterade aldrig författarens boklista. Relationen härleds nu från
  `BookEntity.authorId` i stället för att dupliceras i en `MutableList` på författaren.
* `List<BookType?>` med `it!!` — NPE vid dinglande id. Borta.
* `UseCase.kt` hade en extension där `Error` band till `kotlin.Error`, inte till en
  typparameter. Borttagen.
* `mutableMapOf` muterades från resolvers under WebFlux. Nu `ConcurrentHashMap`.
* `petId = ""` i stället för `null`.
* Standardhanteraren för exceptions läckte interna meddelanden till klienten.
  `SafeDataFetcherExceptionHandler` loggar internt och svarar `INTERNAL_ERROR`.

### Tester
`SchemaSnapshotTest` jämför genererat schema mot `src/test/resources/schema.graphql`.
Code-first betyder att schemat är en bieffekt av koden — snapshoten gör oavsiktliga
API-ändringar synliga i CI. Är ändringen avsiktlig: kopiera in det nya schemat och granska
diffen. `GraphQLApiTest` täcker buggarna ovan.

# Kvar att titta på
* Resolvers är blockerande. Så fort `Database` blir en riktig databas bör de bli `suspend`
  (stöds nativt) eller köras på egen scheduler — annars blockeras Nettys event loop.
* `graphql.introspection.enabled=false` gäller bara profilen `prod`
  (`application-prod.properties`).
* `SpringSingletonKotlinDataFetcherFactoryProvider` delar en property-data-fetcher i
  stället för en per fält. Marginellt nu, relevant när schemat växer.
