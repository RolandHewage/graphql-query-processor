public type Arguments record {|
    CountryArguments? countryArgs?;
    CountriesArguments? countriesArgs?;
    ContinentArguments? continentArgs?;
    ContinentsArguments? continentsArgs?;
    LanguageArguments? languageArgs?;
    LanguagesArguments? languagesArgs?;
|};

public type CountryArguments record {|
    string code;
|};

public type CountriesArguments record {|
    CountryFilterInput? filter?;
|};

public type Country record {|
    string code?;
    string name?;
    string native?;
    string phone?;
    Continent continent?;
    string? capital?;
    string? currency?;
    Language[] languages?;
    string emoji?;
    string emojiU?;
    State[] states?;
|};

public type ContinentArguments record {|
    string code;
|};

public type ContinentsArguments record {|
    ContinentFilterInput? filter?;
|};

public type Continent record {|
    string code?;
    string name?;
    Country[] countries?;
|};

public type LanguageArguments record {|
    string code;
|};

public type LanguagesArguments record {|
    LanguageFilterInput? filter?;
|};

public type Language record {|
    string code?;
    string? name?;
    string? native?;
    boolean rtl?;
|};

public type State record {|
    string? code?;
    string name?;
    Country country?;
|};

// Input Types

public type StringQueryOperatorInput record {
    string? eq?;
    string? ne?;
    string?[]? 'in?;
    string?[]? nin?;
    string? regex?;
    string? glob?;
};

public type CountryFilterInput record {
    StringQueryOperatorInput? code?;
    StringQueryOperatorInput? currency?;
    StringQueryOperatorInput? continent?;
};

public type LanguageFilterInput record {
    StringQueryOperatorInput? code?;
};

public type ContinentFilterInput record {
    StringQueryOperatorInput? code?;
};

public const QueryCountry = "country";
public const QueryCountries = "countries";
public const QueryLanguage = "language";
public const QueryLanguages = "languages";
public const QueryContinent = "continent";
public const QueryContinents = "continents";

public type ArgsAttributes record {
    string? code?;
    CountryFilterInput? filter?;
    // ContinentFilterInput? filter?;
    // LanguageFilterInput? filter?;
};

public annotation ArgsAttributes Arguments on record field, type;




















// public type CountryResponse record {
//     CountryData? data?;
//     Error[]? errors?;
//     record {}? extensions?;
// };

// public type CountryData record {|
//     Country? country?;
// |};

// public type Error record {
//     string? message?;
//     Location[]? locations?;
//     record {}? extensions?;
// };

// public type Location record {
//     int line?;
//     int column?;
// };

// Query

// query Query {
//     country(code: "LK") {
//         code
//         phone
//         continent {
//             code
//             countries {
//                 name
//                 continent {
//                     code
//                 }
//             }
//         }
//     }
// }

// query Query {
//   countries(filter: {code: {eq: "LK"}}) {
//     code
//     phone
//     continent {
//       code
//       countries {
//         name
//         continent {
//           code
//         }
//       }
//     }
//   }
// }

// query  {
//   film(filmID: 1) {
//     title
//     speciesConnection(first: 5) {
//       totalCount
//       species {
//         name
//         personConnection(last: 3) {
//           totalCount
//           people {
//             name
//             birthYear
//           }
//         }
//       }
//     }
//   }
// }

// query {
//   film(filmID: 1) {
//     title
//     speciesConnection(first: 5) {
//       totalCount
//       species {
//         name
//         personConnection(last: 3) {
//           totalCount
//           people {
//             name
//             birthYear
//             filmConnection(first: 2) {
//               totalCount
//               films {
//                 title
//                 speciesConnection(first: 4) {
//                   totalCount
//                   species {
//                     name
//                   }
//                 }
//               }
//             }
//           }
//         }
//       }
//     }
//   }
// }

// query Query { countries(filter:{code:{eq:"LK"}}) { code phone continent { code countries { name continent { code } } } name } }

// query Query { country(code:"LK") { code phone continent { code countries { name continent { code } } } name } }


// Field Name : code | Field Type : string
// Me : code | Parent : countries
// Field Name : phone | Field Type : string
// Me : phone | Parent : countries
// Field Name : continent | Field Type : grapql:record {| string code?; record {| string name?; record {| string code?; |} continent?; |}[] countries?; |}
// Me : continent | Parent : countries
// Field Name : code | Field Type : string
// Me : code | Parent : continent
// Field Name : countries | Field Type : grapql:record {| string name?; record {| string code?; |} continent?; |}[]
// Me : countries | Parent : continent
// Field Name : name | Field Type : string
// Me : name | Parent : countries
// Field Name : continent | Field Type : grapql:record {| string code?; |}
// Me : continent | Parent : countries
// Field Name : code | Field Type : string
// Me : code | Parent : continent
// Field Name : name | Field Type : string
// Me : name | Parent : countries


// Field Name : code | Field Type : string
// Me : code | Parent : country
// Field Name : phone | Field Type : string
// Me : phone | Parent : country
// Field Name : continent | Field Type : grapql:record {| string code?; record {| string name?; record {| string code?; |} continent?; |}[] countries?; |}
// Me : continent | Parent : country
// Field Name : code | Field Type : string
// Me : code | Parent : continent
// Field Name : countries | Field Type : grapql:record {| string name?; record {| string code?; |} continent?; |}[]
// Me : countries | Parent : continent
// Field Name : name | Field Type : string
// Me : name | Parent : countries
// Field Name : continent | Field Type : grapql:record {| string code?; |}
// Me : continent | Parent : countries
// Field Name : code | Field Type : string
// Me : code | Parent : continent
// Field Name : name | Field Type : string
// Me : name | Parent : country


//        HttpRequest request = HttpRequest.newBuilder()
//                .uri(URI.create("https://countries.trevorblades.com"))
//                .headers("Content-Type", "application/json")
//                .POST(HttpRequest.BodyPublishers.ofString("{\"query\":\"query Query { country(code: \\\"LK\\\") " +
//                        "{ code phone continent { code countries { name continent { code } } } } }\"," +
//                        "\"variables\":\"{\\r\\n}\"}", StandardCharsets.UTF_8))
//                .build();

// key : ballerinax/grapql:0.1.0:Arguments value : argumentConfig & readonly
// key : name| value : A

// public type Foo record {|
//    int i?;
//    string s?;        
// |};

// public type Bar record {|
//    int i?;
// |};

















// type CountryArguments record {|
//     readonly string argumentName;
//     string code;
// |};

// type CodeArguments record {|
//     readonly string argumentName;
//     string eq;
// |};

// type CountriesArguments record {|
//     readonly string argumentName;
//     CountryFilterInput filter;
// |};

// table<CountryArguments|CodeArguments|CountriesArguments> key(argumentName) arguments = table [
//     {argumentName: "country", code: "LK"},
//     {argumentName: "country.code", eq: "LK"},
//     {argumentName: "country.continent", filter : {code: {eq: "LK"}}},
//     {argumentName: "country.continent.code", eq: "LK"},
//     {argumentName: "country.continent.countries.continent", filter : {code: {eq: "LK"}}}
// ];
