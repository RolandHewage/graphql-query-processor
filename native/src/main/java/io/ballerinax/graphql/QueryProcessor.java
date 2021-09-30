package io.ballerinax.graphql;

import io.ballerina.runtime.api.Environment;
import io.ballerina.runtime.api.Future;
import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.async.Callback;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.types.AnnotatableType;
import io.ballerina.runtime.api.types.ArrayType;
import io.ballerina.runtime.api.types.RecordType;
import io.ballerina.runtime.api.types.StringType;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.utils.TypeUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BTypedesc;
import org.ballerinalang.net.http.HttpErrorType;
import org.ballerinalang.net.http.HttpUtil;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static io.ballerina.runtime.api.constants.RuntimeConstants.CURRENT_TRANSACTION_CONTEXT_PROPERTY;
import static io.ballerina.runtime.observability.ObservabilityConstants.KEY_OBSERVER_CONTEXT;
import static io.ballerinax.graphql.Constants.APPLICATION_JSON;
import static io.ballerinax.graphql.Constants.CONTENT_TYPE;
import static io.ballerinax.graphql.Constants.GRAPHQL_HTTP_CLIENT;
import static io.ballerinax.graphql.Constants.GRAPHQL_QUERY_PREFIX;
import static io.ballerinax.graphql.Constants.GRAPHQL_QUERY_SUFFIX;
import static io.ballerinax.graphql.Constants.HTTP_POST;
import static io.ballerinax.graphql.Constants.HTTP_REQUEST;
import static io.ballerinax.graphql.Constants.MEDIA_TYPE;
import static io.ballerinax.graphql.Constants.PACKAGE_ID_GRAPHQL;
import static io.ballerinax.graphql.Constants.PATH;
import static io.ballerinax.graphql.Constants.QUERY;
import static io.ballerinax.graphql.Constants.SERVICE_URL;
import static org.ballerinalang.net.http.HttpConstants.ORIGIN_HOST;
import static org.ballerinalang.net.http.HttpConstants.POOLED_BYTE_BUFFER_FACTORY;
import static org.ballerinalang.net.http.HttpConstants.REMOTE_ADDRESS;
import static org.ballerinalang.net.http.HttpConstants.SRC_HANDLER;

public class QueryProcessor {
    public static void externalInit(Environment env, BObject client, BObject httpClient) {
//        client.addNativeData("HttpClient", httpClient);

        // Create an HTTP client
        HttpClient httpclient = HttpClient.newHttpClient();
        client.addNativeData(GRAPHQL_HTTP_CLIENT, httpclient);
    }

    /**
     * Executes the native query when the corresponding Ballerina remote operation is invoked
     */
    public static Object executeQuery(Environment env, BObject client, BTypedesc ballerinaType,
                                      BString queryType, BMap<BString, Object> arguments) {
        // TODO: Generate GraphQL payload using the user defined record
        String requestPayload = buildRequestPayload(ballerinaType, arguments, queryType);

        // TODO: Create HTTP request object with the payload attached to it
        createHttpRequest(requestPayload, client);

        // TODO: Make an HTTP POST request
        makeHttpPostRequest(env, client, ballerinaType, requestPayload);

        // TODO: Process the HTTP response and map to Ballerina record
        return processHttpResponse(ballerinaType);
    }

    /**
     * Build the GraphQL query using the user defined record
     */
    private static String buildRequestPayload(BTypedesc ballerinaType, BMap<BString, Object> arguments,
                                              BString queryType) {

        Node<GraphqlField> queryTreeRoot = getQueryTreeRoot(queryType, arguments);
        RecordType userDefinedRecord = getUserDefinedRecord(ballerinaType);
        populateTree(userDefinedRecord, queryTreeRoot);
        String graphQLQuery = getGraphQLQuery(queryTreeRoot);
        return getRequestPayload(graphQLQuery);
    }

    /**
     * Get the initialized root node of the GraphQL query tree
     */
    private static Node<GraphqlField> getQueryTreeRoot(BString queryType, BMap<BString, Object> arguments) {
        GraphqlField rootQueryField = new GraphqlField(queryType.toString(), arguments);
        Node<GraphqlField> queryTreeRoot = new Node<>(rootQueryField);
        return queryTreeRoot;
    }

    /**
     * Get the user defined record
     */
    private static RecordType getUserDefinedRecord(BTypedesc ballerinaType) {
        RecordType userDefinedRecord;
        if (ballerinaType.getDescribingType() instanceof ArrayType) {
            ArrayType inputArray = (ArrayType) ballerinaType.getDescribingType();
            userDefinedRecord = (RecordType) inputArray.getElementType();
        } else {
            userDefinedRecord = (RecordType) ballerinaType.getDescribingType();
        }
        return userDefinedRecord;
    }

    /**
     * Populate the tree representation of the query using the user defined record
     */
    private static void populateTree(RecordType userDefinedRecord, Node<GraphqlField> parentNode) {
        for (String fieldName : userDefinedRecord.getFields().keySet()) {
            // TODO: Populate arguments of each GraphQL field
            System.out.println("Field Name : " + fieldName +
                    " | Field Type : " + userDefinedRecord.getFields().get(fieldName).getFieldType());
//            getFieldArgumentsFromAnnotation(userDefinedRecord, fieldName);
            Node<GraphqlField> newNode = new Node<>(new GraphqlField(userDefinedRecord.getFields().get(fieldName)));
            parentNode.addChild(newNode);

            if (userDefinedRecord.getFields().get(fieldName).getFieldType() instanceof RecordType) {
                RecordType nestedRecord = (RecordType) userDefinedRecord.getFields().get(fieldName).getFieldType();
                populateTree(nestedRecord, newNode);
            } else if (userDefinedRecord.getFields().get(fieldName).getFieldType() instanceof ArrayType) {
                ArrayType inputArray = (ArrayType) userDefinedRecord.getFields().get(fieldName).getFieldType();
                RecordType nestedRecord = (RecordType) inputArray.getElementType();
                populateTree(nestedRecord, newNode);
            }
        }
    }

    /**
     * Generate the GraphQL query by serializing the tree representation of the query
     */
    private static String getGraphQLQuery(Node<GraphqlField> queryTreeRootNode) {
        // This is where our serialized string will be stored.
        StringBuilder graphqlQuery = new StringBuilder();
        graphqlQuery.append(GRAPHQL_QUERY_PREFIX);
        serialize(queryTreeRootNode, new HashSet<>(), graphqlQuery);
        graphqlQuery.append(GRAPHQL_QUERY_SUFFIX);
        return graphqlQuery.toString();
    }

    /**
     * Serialize the tree representation of the GraphQL query
     */
    private static void serialize(Node<GraphqlField> node, HashSet<GraphqlField> visited,
                                  StringBuilder graphqlQuery) {
        // Keeping the track of already visited node,
        // so that we only proceed with new nodes only
        visited.add(node.getData());

        // Adding the node name to the serialized string
        if (node.getData().isRecordType() || node.getData().isArrayType()) {
            graphqlQuery.append(node.getData().getName()).append(" { ");
        } else if (node.isRoot()) {
//            str.append(node.getData().getName() + "(code: \"LK\")" + " { ");
            graphqlQuery.append(node.getData().getName());
            if (node.getData().getArguments().size() != 0) {
                graphqlQuery.append("(").append(buildGraphqlQueryArguments(node.getData().getArguments()))
                        .append(")");
            }
            graphqlQuery.append(" { ");
        } else {
            graphqlQuery.append(node.getData().getName()).append(" ");
        }

        // Recurring for the children of the current node
        for (Node<GraphqlField> child : node.getChildren()) {
            serialize(child, visited, graphqlQuery);
        }

        // While returning from recursion i.e. in post order
        // we will add delimiters to represent 'end of the subtree'
        if (visited.contains(node.getData())) {
            if (node.getData().isRecordType() || node.getData().isArrayType() || node.isRoot()) {
                graphqlQuery.append("} ");
            }
        }
    }

    /**
     * Build GraphQL query arguments
     */
    private static String buildGraphqlQueryArguments(BMap<BString, Object> variables) {
        StringBuilder arguments = new StringBuilder();
        int length = 0;
        for (BString key : variables.getKeys()) {
            if (TypeUtils.getType(variables.get(key)) instanceof StringType) {
                arguments.append(key).append(":\"").append(variables.get(key)).append("\"");
            } else {
                arguments.append(key).append(":").append(variables.get(key));
            }
            if (length++ != variables.size()-1) {
                arguments.append(", ");
            }
        }
        return arguments.toString().replaceAll("\"([^\"]+)\":","$1:");
    }

    /**
     * Get the GraphQL Request Payload
     */
    private static String getRequestPayload(String graphQLQuery) {
        JSONObject graphqlJsonPayload = new JSONObject();
        graphqlJsonPayload.put(QUERY, graphQLQuery);
        return graphqlJsonPayload.toString();
    }

    /**
     * Create HTTP request object with the GraphQL payload attached to it
     */
    private static void createHttpRequest(String graphqlPayload, BObject client) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(SERVICE_URL))
                .headers(CONTENT_TYPE, APPLICATION_JSON)
                .POST(HttpRequest.BodyPublishers.ofString(graphqlPayload, StandardCharsets.UTF_8))
                .build();
        client.addNativeData(HTTP_REQUEST, request);
    }

    /**
     * Make an HTTP POST request
     */
    private static void makeHttpPostRequest(Environment env, BObject client, BTypedesc ballerinaType, String requestPayload) {
        synchronousRequestUsingJavaHttp(client);
//        synchronousRequestUsingBallerinaHttp(env, client, ballerinaType, requestPayload);
    }

    private static void synchronousRequestUsingJavaHttp(BObject client) {
        HttpClient httpClient = (HttpClient) client.getNativeData(GRAPHQL_HTTP_CLIENT);
        HttpRequest httpRequest = (HttpRequest) client.getNativeData(HTTP_REQUEST);
        try {
            // Use the client to send the request
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode()==200) {
                // The response:
                System.out.println(response.body());
                JSONObject jo = new JSONObject(response.body());
            }
        } catch (IOException | InterruptedException e) {
//            log.error("Exception Occurred: ", e);
        }
    }

    private static void synchronousRequestUsingBallerinaHttp(Environment env, BObject client, BTypedesc ballerinaType,
                                                             String requestPayload) {
        BObject httpClient = (BObject) client.getNativeData("HttpClient");
        BString mediaType = StringUtils.fromString("application/json");

        Object[] paramFeed = new Object[10];
        paramFeed[0] = PATH;
        paramFeed[1] = true;
        paramFeed[2] = StringUtils.fromString(requestPayload);
        paramFeed[3] = true;
        paramFeed[4] = null;
        paramFeed[5] = true;
        paramFeed[6] = MEDIA_TYPE;
        paramFeed[7] = true;
        paramFeed[8] = ballerinaType;
        paramFeed[9] = true;
        Object response = invokeClientMethod(env, httpClient, HTTP_POST, paramFeed);
        // ValueCreator.createTypedescValue(TypeUtils.getType(returnMap))
    }

    // TODO: Process the HTTP response and map to Ballerina record
    private static BMap<BString, Object> processHttpResponse(BTypedesc ballerinaType) {
        RecordType inputRecord = (RecordType) ballerinaType.getDescribingType();
        BMap<BString, Object> objectTypeFieldRecord =
                ValueCreator.createRecordValue(PACKAGE_ID_GRAPHQL, inputRecord.getName());
        // TODO: Implement dynamic logic
        Object[] objectTypeFieldRecordValues = new Object[11];
//        Object[] objectTypeFieldRecordValues = new Object[inputRecord.getFields().values().size()];
        objectTypeFieldRecordValues[0] = "LK";
        objectTypeFieldRecordValues[1] = "Sri Lanka";
//        objectTypeFieldRecordValues[2] = "śrī laṃkāva";
        objectTypeFieldRecordValues[3] = "94";
//        objectTypeFieldRecordValues[4] = ValueCreator.createRecordValue(); // TODO: Implement record dynamically
        objectTypeFieldRecordValues[5] = "Colombo";
        objectTypeFieldRecordValues[6] = "LKR";
//        objectTypeFieldRecordValues[7] = ValueCreator.createArrayValue(); // TODO: Implement array dynamically
        objectTypeFieldRecordValues[8] = "LK";
        objectTypeFieldRecordValues[9] = "U+1F1F1 U+1F1F0";
//        objectTypeFieldRecordValues[10] = ValueCreator.createArrayValue(); // TODO: Implement array dynamically

        return ValueCreator.createRecordValue(objectTypeFieldRecord, objectTypeFieldRecordValues);
    }

    public static Object executeListingQuery(Environment env, BObject client, BTypedesc ballerinaType,
                                             BString functionName, BMap<BString, Object> variables) {
        // TODO: Generate GraphQL query using the user defined record
        String graphqlQuery = buildRequestPayload(ballerinaType, variables, functionName);

        // TODO: Create HTTP request object with the payload attached to it
        createHttpRequest(graphqlQuery, client);

        // TODO: Make a HTTP POST request
        makeHttpPostRequest(env, client, ballerinaType, graphqlQuery);

        // TODO: Process the HTTP response and map to Ballerina record
        return processHttpArrayResponse(ballerinaType);
    }

    // TODO: Process the HTTP response and map to Ballerina record
    private static BArray processHttpArrayResponse(BTypedesc ballerinaType) {
        Object[] recordArray = new Object[11]; // TODO: Take the array count from the HTTP response

        // TODO: Implement dynamic logic
        ArrayType inputArray = (ArrayType) ballerinaType.getDescribingType();
        RecordType inputRecord = (RecordType) inputArray.getElementType();
        BMap<BString, Object> objectTypeFieldRecord =
                ValueCreator.createRecordValue(PACKAGE_ID_GRAPHQL, inputRecord.getName());
        Object[] objectTypeFieldRecordValues = new Object[11];
//        Object[] objectTypeFieldRecordValues = new Object[inputRecord.getFields().values().size()];
        objectTypeFieldRecordValues[0] = "LK";
        objectTypeFieldRecordValues[1] = "Sri Lanka";
//        objectTypeFieldRecordValues[2] = "śrī laṃkāva";
        objectTypeFieldRecordValues[3] = "94";
//        objectTypeFieldRecordValues[4] = ValueCreator.createRecordValue(); // TODO: Implement record dynamically
        objectTypeFieldRecordValues[5] = "Colombo";
        objectTypeFieldRecordValues[6] = "LKR";
//        objectTypeFieldRecordValues[7] = ValueCreator.createArrayValue(); // TODO: Implement array dynamically
        objectTypeFieldRecordValues[8] = "LK";
        objectTypeFieldRecordValues[9] = "U+1F1F1 U+1F1F0";
//        objectTypeFieldRecordValues[10] = ValueCreator.createArrayValue(); // TODO: Implement array dynamically

        recordArray[0] = ValueCreator.createRecordValue(objectTypeFieldRecord, objectTypeFieldRecordValues);

        return ValueCreator.createArrayValue(recordArray, (ArrayType) ballerinaType.getDescribingType());
    }

    private static Object invokeClientMethod(Environment env, BObject client, String methodName, Object[] paramFeed) {
        Future balFuture = env.markAsync();
        Map<String, Object> propertyMap = getPropertiesToPropagate(env);
        env.getRuntime().invokeMethodAsync(client, methodName, null, null, new Callback() {
            @Override
            public void notifySuccess(Object result) {
                System.out.println(result.toString());
                balFuture.complete(result);
            }

            @Override
            public void notifyFailure(BError bError) {
                BError invocationError =
                        HttpUtil.createHttpError("client method invocation failed: " + bError.getErrorMessage(),
                                HttpErrorType.CLIENT_ERROR, bError);
                balFuture.complete(invocationError);
            }
        }, propertyMap, PredefinedTypes.TYPE_NULL, paramFeed);
        return null;
    }

    private static Map<String, Object> getPropertiesToPropagate(Environment env) {
        String[] keys = {CURRENT_TRANSACTION_CONTEXT_PROPERTY, KEY_OBSERVER_CONTEXT, SRC_HANDLER,
                POOLED_BYTE_BUFFER_FACTORY, REMOTE_ADDRESS, ORIGIN_HOST};
        Map<String, Object> subMap = new HashMap<>();
        for (String key : keys) {
            Object value = env.getStrandLocal(key);
            if (value != null) {
                subMap.put(key, value);
            }
        }
        return subMap;
    }



















    private static void getFieldArgumentsFromAnnotation(RecordType ballerinaType, String fieldName) {
        String typeName = TypeUtils.getType(((AnnotatableType) ballerinaType)
                .getAnnotation(StringUtils.fromString("$field$." + fieldName))).getName();
        BMap fieldAnnotation = (BMap) ((AnnotatableType) ballerinaType)
                .getAnnotation(StringUtils.fromString("$field$." + fieldName));
        System.out.println("Name : " + typeName);
    }

    private static void getFieldArgumentsFromUserDefinedRecord(BTypedesc ballerinaType, String fieldName) {
        String typeName = TypeUtils.getType(((AnnotatableType) ballerinaType.getDescribingType())
                .getAnnotation(StringUtils.fromString("$field$." + fieldName))).getName();
        BMap fieldAnnotation = (BMap) ((AnnotatableType) ballerinaType.getDescribingType())
                .getAnnotation(StringUtils.fromString("$field$." + fieldName));
        System.out.println("Name : " + typeName);
    }

    public static void getNameFromAnnotation(BTypedesc ballerinaType, String fieldName) {
        BMap fieldAnnotation = ((BMap) ((AnnotatableType) ballerinaType.getDescribingType())
                .getAnnotation(StringUtils.fromString("$field$.continent")));
        for (Object name : fieldAnnotation.getKeys()) {
            BMap argumentConfigRecord;
            if (TypeUtils.getType(fieldAnnotation.get(name)) instanceof RecordType) {
                argumentConfigRecord = (BMap) fieldAnnotation.get(name);
                for (Object key : argumentConfigRecord.getKeys()) {
//                    log.info("key : " + key + "| value : " + argumentConfigRecord.get(key));
                }
            }
        }
    }













//    public static void getNameFromAnnotation(BTypedesc ballerinaType) {
//        AnnotatableType argumentConfig = ((AnnotatableType) userDefinedRecord.getFields().get("continent").getFieldType());
//        BObject mn = (BObject) argumentConfig.getAnnotation(StringUtils.fromString(ORG_NAME + ORG_NAME_SEPARATOR
//                + GRAPHQL + VERSION_SEPARATOR + GRAPHQL_VERSION + ":" + "Arguments"));

//        BMap argumentConfig = (BMap) ((AnnotatableType) userDefinedRecord.getFields().get("continent").getFieldType())
//                .getAnnotation(StringUtils.fromString(ORG_NAME + ORG_NAME_SEPARATOR
//                        + GRAPHQL + VERSION_SEPARATOR + GRAPHQL_VERSION + ":" + "Arguments"));
//        System.out.println(TypeUtils.getType(argumentConfig).getName());


//        BMap serviceConfig = (BMap) ((AnnotatableType) service.getType())
//                .getAnnotation(StringUtils.fromString(ModuleUtils.getModule().getOrg() + ORG_NAME_SEPARATOR
//                        + ModuleUtils.getModule().getName() + VERSION_SEPARATOR + ModuleUtils.getModule().getMajorVersion()
//                        + ":" + "Arguments"));
//        @SuppressWarnings(ASBConstants.UNCHECKED)
//        BMap<BString, Object> queueConfig =
//                (BMap) serviceConfig.getMapValue(ASBConstants.ALIAS_QUEUE_CONFIG);
//        return queueConfig.getStringValue(ASBConstants.QUEUE_NAME).getValue();
//    }
//
//    public static void getQueueNameFromConfig(BTypedesc ballerinaType) {
//        RecordType userDefinedRecord = null;
//        if (ballerinaType.getDescribingType() instanceof ArrayType) {
//            ArrayType inputArray = (ArrayType) ballerinaType.getDescribingType();
//            userDefinedRecord = (RecordType) inputArray.getElementType();
//        } else {
//            userDefinedRecord = (RecordType) ballerinaType.getDescribingType();
//        }
//
//        BMap argumentConfig = (BMap) ((AnnotatableType) userDefinedRecord.getFields().get("continent").getFieldType())
//                .getAnnotation(StringUtils.fromString(ORG_NAME + ORG_NAME_SEPARATOR
//                        + GRAPHQL + VERSION_SEPARATOR + GRAPHQL_VERSION + ":" + "ServiceConfi"));
//
//
////        for (Object n : argumentConfig.getKeys()) {
////
////        }
////        System.out.println(argumentConfig.size());
////        BMap<BString, Object> queueConfig =
////                (BMap) argumentConfig.getMapValue(StringUtils.fromString("entityConfig"));
////        return queueConfig.getStringValue(StringUtils.fromString("entityPath")).getValue();
//    }

//    // Tree populate logic
//    private static String generateGraphqlQuery(BTypedesc ballerinaType, BMap<BString, Object> variables) {
//        String graphqlQuery = "";
//        RecordType inputRecord = (RecordType) ballerinaType.getDescribingType();
//        for (String fieldName : inputRecord.getFields().keySet()) {
//            System.out.println("Field Name : " + fieldName + " | Field Type : " + inputRecord.getFields().get(fieldName).getFieldType());
//            Node<Field> newNode = new Node<Field>(inputRecord.getFields().get(fieldName));
//            if (inputRecord.getFields().get(fieldName).getFieldType() instanceof RecordType) {
//                RecordType inputRecord1 = (RecordType) inputRecord.getFields().get(fieldName).getFieldType();
//                populateTree(inputRecord1, newNode);
////                for (String fieldName1 : inputRecord1.getFields().keySet()) {
////                    System.out.println("Field Name : " + fieldName1 + " | Field Type : " + inputRecord1.getFields().get(fieldName1).getFieldType());
////                }
//            }
////            if (inputRecord.getFields().get(fieldName).getFieldType() instanceof StringType) {
////                System.out.println("My StringType" + fieldName);
////            }
//        }
//        return graphqlQuery;
//    }
//    private static void populateTree(RecordType inputRecord, Node<Field> parentNode) {
//        for (String fieldName : inputRecord.getFields().keySet()) {
//            System.out.println("Field Name : " + fieldName + " | Field Type : " + inputRecord.getFields().get(fieldName).getFieldType());
//            Node<Field> newNode = new Node<Field>(inputRecord.getFields().get(fieldName), parentNode);
//            if (inputRecord.getFields().get(fieldName).getFieldType() instanceof RecordType) {
//                RecordType outputRecord = (RecordType) inputRecord.getFields().get(fieldName).getFieldType();
//                populateTree(outputRecord, newNode);
//            } else if (inputRecord.getFields().get(fieldName).getFieldType() instanceof ArrayType) {
//                ArrayType inputArray = (ArrayType) inputRecord.getFields().get(fieldName).getFieldType();
//                RecordType outputRecord = (RecordType) inputArray.getElementType();
//                populateTree(outputRecord, newNode);
//            }
//        }
//    }

    // TODO: executeQuery - DONE
    // TODO: BString queryType - DONE
    // TODO: Change as buildRequestPayload - DONE
    // TODO: Rename queryTree - DONE

}