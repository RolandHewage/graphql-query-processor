import ballerina/jballerina.java;
import ballerina/http;

public isolated client class Client {
    # Gets invoked to initialize the `connector`.
    #
    # + clientConfig - The configurations to be used when initializing the `connector`
    # + serviceUrl - URL of the target service
    # + return - An error at the failure of client initialization
    public isolated function init(string serviceUrl, http:ClientConfiguration clientConfig = {}) returns error? {
        http:Client httpEp = check new (serviceUrl, clientConfig);
        externalInit(self, httpEp);
    }

    remote isolated function country(typedesc<Country> returnType = <>, QueryCountry queryCountry = "country", 
    *CountryArguments args) returns returnType|error = @java:Method {
        'class: "io.ballerinax.graphql.QueryProcessor",
        name: "executeQuery"
    } external;

    remote isolated function countries(typedesc<Country[]> returnType = <>, QueryCountries queryCountries = "countries", 
    *CountriesArguments args) returns returnType|error = @java:Method {
        'class: "io.ballerinax.graphql.QueryProcessor",
        name: "executeListingQuery"
    } external;

    remote isolated function language(typedesc<Language> returnType = <>, QueryLanguage queryLanguage = "language", 
    *LanguageArguments args) returns returnType|error = @java:Method {
        'class: "io.ballerinax.graphql.QueryProcessor",
        name: "executeQuery"
    } external;

    remote isolated function languages(typedesc<Language[]> returnType = <>, QueryLanguages queryLanguages = "languages", 
    *LanguagesArguments args) returns returnType|error = @java:Method {
        'class: "io.ballerinax.graphql.QueryProcessor",
        name: "executeListingQuery"
    } external;

    remote isolated function continent(typedesc<Continent> returnType = <>, QueryContinent queryContinent = "continent", 
    *ContinentArguments args) returns returnType|error = @java:Method {
        'class: "io.ballerinax.graphql.QueryProcessor",
        name: "executeQuery"
    } external;

    remote isolated function continents(typedesc<Continent[]> returnType = <>, QueryContinents queryContinents = "continents", 
    *ContinentsArguments args) returns returnType|error = @java:Method {
        'class: "io.ballerinax.graphql.QueryProcessor",
        name: "executeListingQuery"
    } external;
}

isolated function externalInit(Client caller, http:Client httpCaller) = @java:Method {
    'class: "io.ballerinax.graphql.QueryProcessor",
    name: "externalInit"
} external;



















// isolated function countryProcessor(typedesc<Country> returnType, *CountryVariables variables)
// returns Country {
//     http:Request request = new;
//     map<anydata> variables = { "code": code };
//     json graphqlPayload = check getGraphqlPayload(query, variables);
//     request.setPayload(graphqlPayload);
//     Country response = check self.clientEp-> post("", request, targetType = returnType);
//     return response;
// }
