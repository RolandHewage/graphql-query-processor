import ballerina/log;
import ballerina/test;

Client baseClient = check new Client(serviceUrl = "https://countries.trevorblades.com/");

type MyCountry record {|
    string code?;
    string phone?;
    record {|
        string code?;
        record {|
            string name?;
            record {|
                string code?;
            |} continent?;
        |}[] countries?;
    |} continent?;
    string name?;
|};

@test:Config {}
function testCountry() returns error? {
    MyCountry response = check baseClient->country(code = "LK");
    log:printInfo(response.toString());
}

@test:Config {}
function testCountries() returns error? {
    MyCountry[] response = check baseClient->countries(filter = {code: {eq: "LK"}});
    log:printInfo(response.toString());
}










// type MyCountryResponse record {|
//     MyCountryData? data?;
// |};

// type MyCountryData record {|
//     MyCountry country?;
//     // Error[]? errors?;
//     // record {}? extensions?;
// |};



















// @Arguments { code: "LK" }
// type MyCountry record {|
//     string code?;
//     string phone?;
//     @Arguments { code: "LK" }
//     record {|
//         @Arguments { code: "LK" }
//         string code?;
//         @Arguments { filter: {code: {eq: "LK"}}}
//         record {|
//             string name?;
//             @Arguments { code: "LK" }
//             record {|
//                 string code?;
//             |} continent?;
//         |}[] countries?;
//     |} continent?;
//     @Arguments { code: "LK" }
//     string name?;
// |};








// query Query {
//     country(code: "LK") {
//         code(nk: "US", mk: "LS")
//         phone
//         continent(code: "LK") {
//             code(nk: "US")
//             countries {
//                 name(nk: "US")
//                 continent(filter: {code: {eq: "LK"}}) {
//                     code(nk: "US")
//                 }
//             }
//         }
//     }
// }

// {
//     code: {
//         vnK: "US",
//         vmk: "LS"
//     },
//     continent: {
//         vcode: {
//             nk: "US"
//         },
//         code: {
//             vnK: "US"
//         },
//         countries: {
//             name: {
//                 vnK: "US"
//             },
//             continent: {
//                 vfilter: {code: {eq: "LK"}},
//                 code: {
//                     vnk: "US"
//                 }
//             }
//         }
//     }
// }





// query Query {
//     country(code: "LK") {
//         code(eq: "LK")
//         phone
//         continent(filter : {code: {eq: "LK"}}) {
//             code(eq: "LK")
//             countries {
//                 name
//                 continent(filter : {code: {eq: "LK"}}) {
//                     code
//                 }
//             }
//         }
//     }
// }

// @Arguments { code: "LK" }
// type MyCountry1 record {|
//     @Arguments { eq: "LK" }
//     string code?;
//     string phone?;
//     @Arguments { filter : {code: {eq: "LK"}}}
//     record {|
//         @Arguments { eq: "LK" }
//         string code?;
//         record {|
//             string name?;
//             @Arguments { filter : {code: {eq: "LK"}}}
//             record {|
//                 string code?;
//             |} continent?;
//         |}[] countries?;
//     |} continent?;
//     string name?;
// |};

// map<any> argumentsMap1 = {
//     code: {
//         veq: "LK"
//     },
//     continent: {
//         vfilter: {code: {eq: "LK"}},
//         code: {
//             veq: "LK"
//         },
//         countries: {
//             continent: {
//                 vfilter: {code: {eq: "LK"}}
//             }
//         }
//     }
// };

// map<any> argumentsMap2 = {
//     code : { eq: "LK" },
//     continent : { filter : {code: {eq: "LK"}}},
//     continent_code : { eq: "LK" },
//     continent_countries_continent : { filter : {code: {eq: "LK"}}}
// };


