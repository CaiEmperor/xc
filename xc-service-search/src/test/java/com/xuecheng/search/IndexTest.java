package com.xuecheng.search;

import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.IndicesClient;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.*;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * ElasticaSearch测试
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class IndexTest {

    @Autowired
    private RestHighLevelClient restHighLevelClient;
    @Autowired
    private RestClient restClient;

    /**
     * 创建索引库
     * @throws IOException
     */
    @Test
    public void createIndex() throws IOException {
        //创建索引请求对象，并设置索引名称
        CreateIndexRequest createIndexRequest = new CreateIndexRequest("xc_course");
        //设置索引参数
        createIndexRequest.settings(Settings.builder().put("number_of_shards", 1).put("number_of_replicas", 0));
        //设置映射
        createIndexRequest.mapping("doc","  {\n" +
                "    \"properties\": {\n" +
                "           \"name\": {\n" +
                "              \"type\": \"text\",\n" +
                "              \"analyzer\":\"ik_max_word\",\n" +
                "              \"search_analyzer\":\"ik_smart\"\n" +
                "           },\n" +
                "           \"description\": {\n" +
                "              \"type\": \"text\",\n" +
                "              \"analyzer\":\"ik_max_word\",\n" +
                "              \"search_analyzer\":\"ik_smart\"\n" +
                "           },\n" +
                "           \"studymodel\": {\n" +
                "              \"type\": \"keyword\"\n" +
                "           },\n" +
                "           \"price\": {\n" +
                "              \"type\": \"float\"\n" +
                "           },\n" +
                "           \"timestamp\": {\n" +
                "                \"type\":   \"date\",\n" +
                "                \"format\": \"yyyy-MM-dd HH:mm:ss||yyyy-MM-dd||epoch_millis\"\n" +
                "            }\n" +
                "        }\n" +
                "}", XContentType.JSON);
        //创建索引操作客户端
        IndicesClient indices = restHighLevelClient.indices();
        //创建响应对象
        CreateIndexResponse createIndexResponse = indices.create(createIndexRequest);
        //得到响应结果
        boolean acknowledged = createIndexResponse.isAcknowledged();
        System.out.println(acknowledged);
    }

    /**
     * 删除索引库
     * @throws IOException
     */
    @Test
    public void deleteIndex() throws IOException {
        //删除索引请求对象
        DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest("xc_course");
        //删除索引
        DeleteIndexResponse deleteIndexResponse = restHighLevelClient.indices().delete(deleteIndexRequest);
        //删除索引响应结果
        boolean acknowledged = deleteIndexResponse.isAcknowledged();
        System.out.println(acknowledged);
    }

    /**
     * 向索引库中添加文档
     * @throws IOException
     */
    @Test
    public void addDocument() throws IOException {
        //准备json数据
        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("name", "spring cloud实战");
        jsonMap.put("description", "本课程主要从四个章节进行讲解： 1.微服务架构入门 2.spring cloud 基础入门 3.实战Spring Boot 4.注册中心eureka。");
        jsonMap.put("studymodel", "201001");
        SimpleDateFormat dateFormat =new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        jsonMap.put("timestamp", dateFormat.format(new Date()));
        jsonMap.put("price", 5.6f);
        //索引请求对象
        IndexRequest indexRequest = new IndexRequest("xc_course", "doc");
        //指定索引文档内容
        indexRequest.source(jsonMap);
        //索引响应对象
        IndexResponse indexResponse = restHighLevelClient.index(indexRequest);
        //获取响应结果
        DocWriteResponse.Result result = indexResponse.getResult();
        System.out.println(result);
    }

    /**
     * 查询文档(根据id)
     * @throws IOException
     */
    @Test
    public void queryDocument() throws IOException {
        //查询索引请求对象
        GetRequest getRequest = new GetRequest("xc_course", "doc", "m3sGCWgBYol_u5v8Cic9");
        //索引响应对象
        GetResponse getResponse = restHighLevelClient.get(getRequest);
        //查询索引响应结果
        boolean exists = getResponse.isExists();
        //转换为map格式
        Map<String, Object> sourceAsMap = getResponse.getSourceAsMap();
        System.out.println(sourceAsMap);
    }

    /**
     * 根据id更新文档(全部更新,部分更新:先删除,再添加)
     * @throws IOException
     */
    @Test
    public void updateDocument() throws IOException {
        //更新索引请求对象
        UpdateRequest updateRequest = new UpdateRequest("xc_course", "doc", "m3sGCWgBYol_u5v8Cic9");
        //准备更新的数据,进行更新
        Map<String, String> map = new HashMap<>();
        map.put("name", "spring cloud实战");
        updateRequest.doc(map);
        //索引响应对象
        UpdateResponse updateResponse = restHighLevelClient.update(updateRequest);
        //更新的转状态
        RestStatus status = updateResponse.status();
        System.out.println(status);
    }

    /**
     * 根据id删除文档
     * @throws IOException
     */
    @Test
    public void deleteDocument() throws IOException {
        //文档id
        String id = "m3sGCWgBYol_u5v8Cic9";
        //删除索引请求对象
        DeleteRequest deleteRequest = new DeleteRequest("xc_course", "doc", id);
        //索引响应对象
        DeleteResponse deleteResponse = restHighLevelClient.delete(deleteRequest);
        DocWriteResponse.Result result = deleteResponse.getResult();
        //获取响应结果
        System.out.println(result);
    }

    //DSL(Domain Specific Language)是ES提出的基于json的搜索方式，在搜索时传入特定的json格式的数据来完成不同的搜索需求。
   //DSL比URI搜索方式功能强大，在项目中建议使用DSL方式来完成搜索。

    /**
     * matchAllQuery:查询所有和分页查询,
     * @throws IOException
     * @throws ParseException
     */
    @Test
    public void searchAll() throws IOException, ParseException {
        //搜索请求对象
        SearchRequest searchRequest = new SearchRequest("xc_course");
        //设置类型
        searchRequest.types("doc");
        //搜索源构建对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();


        //设置分页参数
        int page = 1;//页码
        int size = 1;//显示条数
        int from = (page - 1) * size;//表示起始文档的下标，从0开始
        searchSourceBuilder.from(from);
        searchSourceBuilder.size(size);
        //搜索全部
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());


        //搜索源字段过滤
        searchSourceBuilder.fetchSource(new String[]{"name","studymodel","price","timestamp"}, new String[]{});
        //设置搜索源
        searchRequest.source(searchSourceBuilder);
        //执行搜索
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest);
        //搜索匹配结果
        SearchHits searchHits = searchResponse.getHits();
        //搜索总记录数
        long totalHits = searchHits.totalHits;
        //获取匹配度较高的前N个文档
        SearchHit[] hitsHits = searchHits.getHits();
        //日期格式化对象
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (SearchHit hitsHit : hitsHits) {
            //文档id
            String id = hitsHit.getId();
            //源文档内容
            Map<String, Object> sourceAsMap = hitsHit.getSourceAsMap();
            //获取文档的具体信息
            String name = (String) sourceAsMap.get("name");
            String description = (String) sourceAsMap.get("description");
            String studymodel = (String) sourceAsMap.get("studymodel");
            Double price = (Double) sourceAsMap.get("price");
            Date timestamp = dateFormat.parse((String) sourceAsMap.get("timestamp"));
            System.out.println(name);
            System.out.println(description);
            System.out.println(studymodel);
            System.out.println(price);
            System.out.println(timestamp);
        }
    }

    /**
     *termQuery:精准查询(搜索时不分词(关键字),索引分词)
     * @throws IOException
     * @throws ParseException
     */
    @Test
    public void termQuery() throws IOException, ParseException {
        //搜索请求对象
        SearchRequest searchRequest = new SearchRequest("xc_course");
        //设置类型
        searchRequest.types("doc");
        //搜索源构建对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();


        //名字,(可以多个关键字查询,id也可以)
        String[] names = new String[]{"spring","基础"};
        //TermQuery,精确查询,关键字
        searchSourceBuilder.query(QueryBuilders.termsQuery("name", names));


        //搜索源字段过滤
        searchSourceBuilder.fetchSource(new String[]{"name","studymodel","price","timestamp"}, new String[]{});
        //设置搜索源
        searchRequest.source(searchSourceBuilder);
        //执行搜索
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest);
        //搜索匹配结果
        SearchHits searchHits = searchResponse.getHits();
        //搜索总记录数
        long totalHits = searchHits.totalHits;
        //获取匹配度较高的前N个文档
        SearchHit[] hitsHits = searchHits.getHits();
        //日期格式化对象
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (SearchHit hitsHit : hitsHits) {
            //文档id
            String id = hitsHit.getId();
            //源文档内容
            Map<String, Object> sourceAsMap = hitsHit.getSourceAsMap();
            //获取文档的具体信息
            String name = (String) sourceAsMap.get("name");
            String description = (String) sourceAsMap.get("description");
            String studymodel = (String) sourceAsMap.get("studymodel");
            Double price = (Double) sourceAsMap.get("price");
            Date timestamp = dateFormat.parse((String) sourceAsMap.get("timestamp"));
            System.out.println(name);
            System.out.println(description);
            System.out.println(studymodel);
            System.out.println(price);
            System.out.println(timestamp);
        }
    }

    /**
     *matchQuery:全文检索(搜索和索引都分词)
     * operator：or 表示 只要有一个词在文档中出现则就符合条件，and表示每个词都在文档中出现则才符合条件。
     * minimum_should_match指定文档匹配词的占比:“spring开发框架”会被分为三个词：spring、开发、框架
     *  设置"minimum_should_match": "80%"表示，三个词在文档的匹配占比为80%，即3*0.8=2.4，向上取整得2，表示至少有两个词在文档中要匹配成功。
     * @throws IOException
     * @throws ParseException
     */
    @Test
    public void matchQuery() throws IOException, ParseException {
        //搜索请求对象
        SearchRequest searchRequest = new SearchRequest("xc_course");
        //设置类型
        searchRequest.types("doc");
        //搜索源构建对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();


        //全文检索
        searchSourceBuilder.query(QueryBuilders.matchQuery("description", "spring开发框架").operator(Operator.OR).minimumShouldMatch("80%"));


        //搜索源字段过滤
        searchSourceBuilder.fetchSource(new String[]{"name","studymodel","price","timestamp"}, new String[]{});
        //设置搜索源
        searchRequest.source(searchSourceBuilder);
        //执行搜索
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest);
        //搜索匹配结果
        SearchHits searchHits = searchResponse.getHits();
        //搜索总记录数
        long totalHits = searchHits.totalHits;
        //获取匹配度较高的前N个文档
        SearchHit[] hitsHits = searchHits.getHits();
        //日期格式化对象
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (SearchHit hitsHit : hitsHits) {
            //文档id
            String id = hitsHit.getId();
            //源文档内容
            Map<String, Object> sourceAsMap = hitsHit.getSourceAsMap();
            //获取文档的具体信息
            String name = (String) sourceAsMap.get("name");
            String description = (String) sourceAsMap.get("description");
            String studymodel = (String) sourceAsMap.get("studymodel");
            Double price = (Double) sourceAsMap.get("price");
            Date timestamp = dateFormat.parse((String) sourceAsMap.get("timestamp"));
            System.out.println(name);
            System.out.println(description);
            System.out.println(studymodel);
            System.out.println(price);
            System.out.println(timestamp);
        }
    }

    /**
     *multiQuery:一次可以匹配多个字段
     * boost:匹配多个字段时可以提升字段的boost（权重）来提高得分
     * @throws IOException
     * @throws ParseException
     */
    @Test
    public void multiQuery() throws IOException, ParseException {
        //搜索请求对象
        SearchRequest searchRequest = new SearchRequest("xc_course");
        //设置类型
        searchRequest.types("doc");
        //搜索源构建对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();


        //multiQuery
        searchSourceBuilder.query(QueryBuilders.multiMatchQuery("spring css", "name", "description").minimumShouldMatch("50%").field("name", 10));


        //搜索源字段过滤
        searchSourceBuilder.fetchSource(new String[]{"name","studymodel","price","timestamp"}, new String[]{});
        //设置搜索源
        searchRequest.source(searchSourceBuilder);
        //执行搜索
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest);
        //搜索匹配结果
        SearchHits searchHits = searchResponse.getHits();
        //搜索总记录数
        long totalHits = searchHits.totalHits;
        //获取匹配度较高的前N个文档
        SearchHit[] hitsHits = searchHits.getHits();
        //日期格式化对象
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (SearchHit hitsHit : hitsHits) {
            //文档id
            String id = hitsHit.getId();
            //源文档内容
            Map<String, Object> sourceAsMap = hitsHit.getSourceAsMap();
            //获取文档的具体信息
            String name = (String) sourceAsMap.get("name");
            String description = (String) sourceAsMap.get("description");
            String studymodel = (String) sourceAsMap.get("studymodel");
            Double price = (Double) sourceAsMap.get("price");
            Date timestamp = dateFormat.parse((String) sourceAsMap.get("timestamp"));
            System.out.println(name);
            System.out.println(description);
            System.out.println(studymodel);
            System.out.println(price);
            System.out.println(timestamp);
        }
    }

    /**
     *boolQuery:一次可以匹配多个字段
     *布尔查询对应于Lucene的BooleanQuery查询，实现将多个查询组合起来。
     *三个参数：
     *must：文档必须匹配must所包括的查询条件，相当于 “AND”
     *should：文档应该匹配should所包括的查询条件其中的一个或多个，相当于 "OR"
     *must_not：文档不能匹配must_not所包括的该查询条件，相当于“NOT”
     * @throws IOException
     * @throws ParseException
     */
    @Test
    public void boolQuery() throws IOException, ParseException {
        //搜索请求对象
        SearchRequest searchRequest = new SearchRequest("xc_course");
        //设置类型
        searchRequest.types("doc");
        //搜索源构建对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();


        //全文检索multiQuery
        MultiMatchQueryBuilder multiMatchQueryBuilder = QueryBuilders.multiMatchQuery("spring css", "name", "description").minimumShouldMatch("50%").field("name", 10);
        //TermQuery
        TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("studymodel", "201001");
        //boolQuery
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        //将MultiMatcherQuery和TermQuery组织在一起
        boolQueryBuilder.must(multiMatchQueryBuilder);
        boolQueryBuilder.must(termQueryBuilder);
        //添加过虑器
        //过虑是针对搜索的结果进行过虑，过虑器主要判断的是文档是否匹配，不去计算和判断文档的匹配度得分，
        // 所以过虑器性能比查询要高，且方便缓存，推荐尽量使用过虑器去实现查询或者过虑器和查询共同使用。
        //项过虑
        boolQueryBuilder.filter(QueryBuilders.termQuery("studymodel","201001"));
        //范围过虑
        boolQueryBuilder.filter(QueryBuilders.rangeQuery("price").gte(60).lte(100));
        searchSourceBuilder.query(boolQueryBuilder);


        //搜索源字段过滤
        searchSourceBuilder.fetchSource(new String[]{"name","studymodel","price","timestamp"}, new String[]{});
        //设置搜索源
        searchRequest.source(searchSourceBuilder);
        //执行搜索
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest);
        //搜索匹配结果
        SearchHits searchHits = searchResponse.getHits();
        //搜索总记录数
        long totalHits = searchHits.totalHits;
        //获取匹配度较高的前N个文档
        SearchHit[] hitsHits = searchHits.getHits();
        //日期格式化对象
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (SearchHit hitsHit : hitsHits) {
            //文档id
            String id = hitsHit.getId();
            //源文档内容
            Map<String, Object> sourceAsMap = hitsHit.getSourceAsMap();
            //获取文档的具体信息
            String name = (String) sourceAsMap.get("name");
            String description = (String) sourceAsMap.get("description");
            String studymodel = (String) sourceAsMap.get("studymodel");
            Double price = (Double) sourceAsMap.get("price");
            Date timestamp = dateFormat.parse((String) sourceAsMap.get("timestamp"));
            System.out.println(name);
            System.out.println(description);
            System.out.println(studymodel);
            System.out.println(price);
            System.out.println(timestamp);
        }
    }

    /**
     *排序:可以在字段上添加一个或多个排序，支持在keyword、date、float等类型上添加，text类型的字段上不允许添加排序。
     *高亮显示:高亮显示可以将搜索结果一个或多个字突出显示，以便向用户展示匹配关键字的位置。
     * @throws IOException
     * @throws ParseException
     */
    @Test
    public void filter() throws IOException, ParseException {
        //搜索请求对象
        SearchRequest searchRequest = new SearchRequest("xc_course");
        //指定类型
        searchRequest.types("doc");
        //搜索源构建对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        //boolQuery搜索方式
        //先定义一个MultiMatchQuery
        MultiMatchQueryBuilder multiMatchQueryBuilder = QueryBuilders.multiMatchQuery("开发框架", "name", "description")
                .minimumShouldMatch("50%")
                .field("name", 10);

        //定义一个boolQuery
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.must(multiMatchQueryBuilder);
        //定义过虑器
        boolQueryBuilder.filter(QueryBuilders.rangeQuery("price").gte(0).lte(100));

        searchSourceBuilder.query(boolQueryBuilder);
        //设置源字段过虑,第一个参数结果集包括哪些字段，第二个参数表示结果集不包括哪些字段
        searchSourceBuilder.fetchSource(new String[]{"name","studymodel","price","timestamp", "description"},new String[]{});

        //设置高亮
        //高亮显示可以将搜索结果一个或多个字突出显示，以便向用户展示匹配关键字的位置。
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.preTags("<tag>");
        highlightBuilder.postTags("</tag>");
        highlightBuilder.fields().add(new HighlightBuilder.Field("name"));
        highlightBuilder.fields().add(new HighlightBuilder.Field("description"));
        searchSourceBuilder.highlighter(highlightBuilder);

        //向搜索请求对象中设置搜索源
        searchRequest.source(searchSourceBuilder);
        //设置排序
        //可以在字段上添加一个或多个排序，支持在keyword、date、float等类型上添加，text类型的字段上不允许添加排序。
        searchSourceBuilder.sort("studymodel", SortOrder.DESC);
        searchSourceBuilder.sort("price", SortOrder.ASC);
        //执行搜索,向ES发起http请求
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest);
        //搜索结果
        SearchHits hits = searchResponse.getHits();
        //匹配到的总记录数
        long totalHits = hits.getTotalHits();
        //得到匹配度高的文档
        SearchHit[] searchHits = hits.getHits();
        //日期格式化对象
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for(SearchHit hit:searchHits){
            //文档的主键
            String id = hit.getId();
            //源文档内容
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            //源文档的name字段内容
            String name = (String) sourceAsMap.get("name");
            //取出高亮字段
            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            if(highlightFields!=null){
                //取出name高亮字段
                HighlightField nameHighlightField = highlightFields.get("name");
                if(nameHighlightField!=null){
                    Text[] fragments = nameHighlightField.getFragments();
                    StringBuffer stringBuffer = new StringBuffer();
                    for(Text text:fragments){
                        stringBuffer.append(text);
                    }
                    name = stringBuffer.toString();
                }
            }

            String description = (String) sourceAsMap.get("description");
            //学习模式
            String studymodel = (String) sourceAsMap.get("studymodel");
            //价格
            Double price = (Double) sourceAsMap.get("price");
            //日期
            Date timestamp = dateFormat.parse((String) sourceAsMap.get("timestamp"));
            System.out.println(name);
            System.out.println(price);
            System.out.println(studymodel);
            System.out.println(description);
            System.out.println(timestamp);
        }
    }
}
