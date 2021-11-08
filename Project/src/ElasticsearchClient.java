import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpHost;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;

/**
 * An Elasticsearch client that uses Java High Level REST Client and its Search API to accept
 * request objects and return response objects.
 *
 * For examples and more step-by-step guide @see <a href="https://www.elastic.co/guide/en/elasticsearch/client/java-rest/master/java-rest-high-search.html">elasticsearch.co</a>
 */
public class ElasticsearchClient {
    // Specify search fields
    final private static String[] FETCH_FIELDS = { "fileName", "report", "year", "authority", "goals", "tasks" };
    final private static String INDEX = "test";
    final private String LOCALHOST = "ec2-34-254-66-248.eu-west-1.compute.amazonaws.com";
    final private Integer PORT = 9200;
    final private String SCHEMA = "http";

    private RestHighLevelClient client;
    private SearchRequest searchRequest;
    private BoolQueryBuilder queryBuilder;
    private SearchSourceBuilder searchSourceBuilder;

    ElasticsearchClient () {
        // Do nothing
    }

    private void makeConnection () {
        HttpHost httpHost = new HttpHost(LOCALHOST, PORT, SCHEMA);
        client = new RestHighLevelClient(RestClient.builder(httpHost));
        searchRequest = new SearchRequest(INDEX);
        searchSourceBuilder = new SearchSourceBuilder();
        queryBuilder = QueryBuilders.boolQuery();
    }

    private void closeConnection () throws IOException {
        client.close();
    }

    long searchCount (String searchField, String mustMatch) throws IOException {
        makeConnection();

        // Assign search requirements
        String processedSearchField = searchField;
        if (searchField.equals(EntryField.GOALS) || searchField.equals(EntryField.TASKS))
            processedSearchField += ".content";

        queryBuilder.must(QueryBuilders.matchQuery(processedSearchField, mustMatch));
        searchSourceBuilder.query(queryBuilder).fetchSource(FETCH_FIELDS, null);
        searchSourceBuilder.size(10000);
        searchRequest.source(searchSourceBuilder);

        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        long count = searchResponse.getHits().getTotalHits().value;

        closeConnection();
        return count;
    }

    /**
     * Look for the documents that match the search requirements and parse result into {@link PostingEntry}
     * @param searchField the field to compare
     * @param mustMatch the value to match
     * @return a list of matching documents
     * @throws IOException if error occurs while connecting to elasticsearch server
     */
    List<PostingEntry> search (String searchField, String mustMatch) throws IOException {
        makeConnection();

        // Assign search requirements
        String processedSearchField = searchField;
        if (searchField.equals(EntryField.GOALS) || searchField.equals(EntryField.TASKS))
            processedSearchField += ".content";

        queryBuilder.must(QueryBuilders.matchQuery(processedSearchField, mustMatch));
        searchSourceBuilder.query(queryBuilder).fetchSource(FETCH_FIELDS, null);
        searchSourceBuilder.size(20);
        searchRequest.source(searchSourceBuilder);

        // Execute search
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

        // Create results
        List<PostingEntry> matchingEntries = new ArrayList<>();
        if (searchResponse.getHits().getTotalHits().value > 0) {

            for (SearchHit hit : searchResponse.getHits()) {
                PostingEntry matchingEntry = new PostingEntry();

                for (String fetchField : FETCH_FIELDS) {
                    switch (fetchField) {
                        case EntryField.FILENAME:
                            matchingEntry.setFilename(hit.getSourceAsMap().get(fetchField).toString());
                            break;
                        case EntryField.REPORT:
                            matchingEntry.setReport(hit.getSourceAsMap().get(fetchField).toString());
                            break;
                        case EntryField.YEAR:
                            matchingEntry.setYear(hit.getSourceAsMap().get(fetchField).toString());
                            break;
                        case EntryField.AUTHORITY:
                            matchingEntry.setAuthority(hit.getSourceAsMap().get(fetchField).toString());
                            break;
                        case EntryField.GOALS:
                            ArrayList<HashMap<String, String>> goals = (ArrayList) hit.getSourceAsMap().get(fetchField);
                            if (goals != null) {
                                List<Goal> goalsToAdd = new ArrayList<>();
                                for (HashMap<String, String> goal : goals) {
                                    String content = goal.get("content");
                                    if (searchField.equals(EntryField.GOALS) && matched(mustMatch, content)) {
                                        String title = goal.get("header");
                                        goalsToAdd.add(new Goal(title, content));
                                    }
                                }
                                matchingEntry.goals = goalsToAdd;
                            } else {
                                matchingEntry.goals = null;
                            }
                            break;
                        case EntryField.TASKS:
                            ArrayList<HashMap<String, String>> tasks = (ArrayList) hit.getSourceAsMap().get(fetchField);
                            if (tasks != null) {
                                List<Task> tasksToAdd = new ArrayList<>();
                                for (HashMap<String, String> task : tasks) {
                                    String content = task.get("content");
                                    if (searchField.equals(EntryField.TASKS) && matched(mustMatch, content)) {
                                        String title = task.get("header");
                                        tasksToAdd.add(new Task(title, content));
                                    }
                                }
                                matchingEntry.tasks = tasksToAdd;
                            } else {
                                matchingEntry.tasks = null;
                            }
                            break;
                        default:
                            System.out.println("Invalid field name: " + fetchField);
                            System.exit(-1);
                    }
                }
                matchingEntries.add(matchingEntry);
            }
        } else {
            System.out.println("No results matching the criteria.");
        }

        closeConnection();

        return matchingEntries;
    }

    private boolean matched (String mustMatch, String content) {
        String[] searchTerms = mustMatch.split(" ");
        for (String term: searchTerms) {
            if (content.contains(term.trim()))
                return true;
        }
        return false;
    }

    public static void main(String[] args) throws IOException {
        // Test a toy problem
        ElasticsearchClient client = new ElasticsearchClient();

        String mustMatch = "är";
        List<PostingEntry> results = client.search(EntryField.GOALS, mustMatch);
        System.out.println("Found : " + results.size() + " entries.");
//
//        for (PostingEntry entry : results) {
//            System.out.println("File name: " + entry.getFilename());
//            for (Goal g : entry.goals) {
//                System.out.println(g.getTitle() + ": " + g.getContent());
//            }
//            for (Task t : entry.tasks) {
//                System.out.println(t.getTitle() + ": " + t.getContent());
//            }
//            System.out.println("--------------------------");
//        }

        long count = client.searchCount(EntryField.TASKS, mustMatch);
        System.out.println("Count : " + count + " entries.");

    }
}

