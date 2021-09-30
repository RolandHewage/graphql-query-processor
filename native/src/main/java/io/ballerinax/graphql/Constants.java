package io.ballerinax.graphql;

import io.ballerina.runtime.api.Module;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BString;

public class Constants {
    public static final String ORG_NAME = "ballerinax";
    public static final String GRAPHQL = "grapql";
    public static final String GRAPHQL_VERSION = "0.1.0";
    public static final Module PACKAGE_ID_GRAPHQL = new Module(ORG_NAME, GRAPHQL, GRAPHQL_VERSION);

    public static final String GRAPHQL_QUERY_PREFIX = "query Query { ";
    public static final String GRAPHQL_QUERY_SUFFIX = "}";

    public static final String GRAPHQL_HTTP_CLIENT = "Httpclient";
    public static final String HTTP_REQUEST = "httprequest";
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String APPLICATION_JSON = "application/json";
    public static final BString PATH = StringUtils.fromString("");
    public static final BString MEDIA_TYPE = StringUtils.fromString("application/json");
    public static final String SERVICE_URL = "https://countries.trevorblades.com";
    public static final String HTTP_POST = "post";

    public static final String QUERY = "query";
}
