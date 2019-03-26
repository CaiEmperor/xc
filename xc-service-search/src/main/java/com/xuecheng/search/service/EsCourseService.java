package com.xuecheng.search.service;

import com.xuecheng.framework.domain.course.CoursePub;
import com.xuecheng.framework.domain.course.TeachplanMediaPub;
import com.xuecheng.framework.domain.search.CourseSearchParam;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * 课程搜索
 */
@Service
public class EsCourseService {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Value("${xuecheng.course.index}")
    private String es_index;
    @Value("${xuecheng.media.index}")
    private String media_index;
    @Value("${xuecheng.course.type}")
    private String es_type;
    @Value("${xuecheng.media.type}")
    private String media_type;
    @Value("${xuecheng.course.source_field}")
    private String source_field;
    @Value("${xuecheng.media.source_field}")
    private String media_source_field;

    /**
     * 课程搜索
     * 1.分页查询()
     *  2.关键字,多个字段查询(multiMatchQuery)
     *  3.根据类别,难度等级查询(filter)
     * @param page
     * @param size
     * @param courseSearchParam 分类条件
     * @return
     */
    public QueryResponseResult<CoursePub> list(int page, int size, CourseSearchParam courseSearchParam) {
        if (courseSearchParam == null){
            courseSearchParam = new CourseSearchParam();
        }
        //1.创建搜索请求对象
        SearchRequest searchRequest = new SearchRequest(es_index);
        //2.设置类型
        searchRequest.types(es_type);
        //3.创建构建搜索源对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //4.过滤字段数组
        String[] source_fields = source_field.split(",");
        //5.设置过滤字段
        searchSourceBuilder.fetchSource(source_fields, new String[]{});
        //6.关键字全文检索多字段查询
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        if (StringUtils.isNotEmpty(courseSearchParam.getKeyword())){
            //全文检索,多字段搜索
            MultiMatchQueryBuilder multiMatchQueryBuilder = QueryBuilders.multiMatchQuery(courseSearchParam.getKeyword(), "name", "description", "teachplan").minimumShouldMatch("70%").field("name", 10);
            //创建布尔查询对象,将multiMatchQuery加入boolQuery中
           boolQueryBuilder.must(multiMatchQueryBuilder);
        }
        //7.根据分类和难度查询,过滤器精确查询
        if (StringUtils.isNotEmpty(courseSearchParam.getMt())){
            //一级分类
            boolQueryBuilder.filter(QueryBuilders.termQuery("mt", courseSearchParam.getMt()));
        }
        if (StringUtils.isNotEmpty(courseSearchParam.getSt())){
            //二级分类
            boolQueryBuilder.filter(QueryBuilders.termQuery("st", courseSearchParam.getSt()));
        }
        if (StringUtils.isNotEmpty(courseSearchParam.getGrade())){
            //难度级别
            boolQueryBuilder.filter(QueryBuilders.termQuery("grade", courseSearchParam.getGrade()));
        }
        //将查询条件添加到构建搜索源对象中
        searchSourceBuilder.query(boolQueryBuilder);
        //8.分页查询
        if (page < 0){
            page = 1;
        }
        int from = (page - 1) * size;//开始的索引下标
        if (size < 0){
            size = 1;
        }
        //将分页参数设置到构建搜索源对象中
        searchSourceBuilder.from(from);
        searchSourceBuilder.size(size);
        //9.创建高亮显示对象
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        //设置高亮样式,开始和结束标记
        highlightBuilder.preTags("<font class='eslight'>");
        highlightBuilder.postTags("</font>");
        //设置高亮字段
        highlightBuilder.fields().add(new HighlightBuilder.Field("name"));
        //将高亮显示添加到构建搜索源对象中
        searchSourceBuilder.highlighter(highlightBuilder);
        //将搜索源设置到搜索对象中
        searchRequest.source(searchSourceBuilder);
        //执行搜索
        //QueryResult对象
        QueryResult<CoursePub> queryResult = new QueryResult<>();
        //数据集合
        ArrayList<CoursePub> list = new ArrayList<>();
        try {
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest);
            //获取结果结果集对象
            SearchHits hits = searchResponse.getHits();
            //获取总记录条数
            long totalHits = hits.totalHits;
            //给queryResult设置Total
            queryResult.setTotal(totalHits);
            //获取数据列表
            SearchHit[] hitsHits = hits.getHits();
            for (SearchHit hitsHit : hitsHits) {
                CoursePub coursePub = new CoursePub();
                //获取数据源文档
                Map<String, Object> sourceAsMap = hitsHit.getSourceAsMap();
                //获取id
                String id = (String) sourceAsMap.get("id");
                coursePub.setId(id);
                //获取名字
                String name = (String) sourceAsMap.get("name");
                //获取高亮字段内容
                Map<String, HighlightField> highlightFields = hitsHit.getHighlightFields();
                //获取高亮字段的名字
                if (highlightFields != null){
                    HighlightField nameField = highlightFields.get("name");
                    if (nameField != null){
                        //name的段信息
                        Text[] fragments = nameField.getFragments();
                        //拼接对象
                        StringBuilder stringBuilder = new StringBuilder();
                        for (Text fragment : fragments) {
                            stringBuilder.append(fragment.toString());
                        }
                        //赋值给name
                        name = stringBuilder.toString();
                    }
                }
                coursePub.setName(name);
                //获取图片
                String pic = (String) sourceAsMap.get("pic");
                coursePub.setPic(pic);
                //获取原价
                Double price = (Double) sourceAsMap.get("price");
                coursePub.setPrice(price);
                //获取新价
                Double price_old = (Double) sourceAsMap.get("price_old");
                coursePub.setPrice_old(price_old);
                //将coursePub添加到list集合中
                list.add(coursePub);
                //给queryResult设置List
                queryResult.setList(list);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        //设置返回结果
        QueryResponseResult<CoursePub> queryResponseResult = new QueryResponseResult<>(CommonCode.SUCCESS, queryResult);
        return queryResponseResult;
    }

    /**
     * 根据课程id查询课程信息
     *      1.创建搜索请求对象
     *      2.设置类型
     *      3.创建构建搜索源对象
     *      4.根据课程id精确查询
     *      5.将构建搜索源对象加到搜索请求对象中
     *      6.执行搜索
     *      7.获取搜索结果对象
     * @param courseId
     * @return 返回的课程信息为json结构：key为课程id，value为课程内容。
     */
    public Map<String, CoursePub> getall(String courseId) {
        //1.创建搜索请求对象
        SearchRequest searchRequest = new SearchRequest(es_index);
        //2.设置类型
        searchRequest.types(es_type);
        //3.创建构建搜索源对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //4.根据课程id精确查询
        searchSourceBuilder.query(QueryBuilders.termQuery("id", courseId));
        //5.将构建搜索源对象加到搜索请求对象中
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = null;
        try {
            //6.执行搜索
            searchResponse = restHighLevelClient.search(searchRequest);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //7.获取搜索结果对象
        SearchHits responseHits = searchResponse.getHits();
        //获取结果列表
        SearchHit[] searchHits = responseHits.getHits();
        //创建map集合
        HashMap<String, CoursePub> map = new HashMap<>();
        for (SearchHit searchHit : searchHits) {
            CoursePub coursePub = new CoursePub();
            //获取搜索源文档
            Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();
            //获取文档id
            String id = (String) sourceAsMap.get("id");
            String name = (String) sourceAsMap.get("name");
            String grade = (String) sourceAsMap.get("grade");
            String charge = (String) sourceAsMap.get("charge");
            String pic = (String) sourceAsMap.get("pic");
            String description = (String) sourceAsMap.get("description");
            String teachplan = (String) sourceAsMap.get("teachplan");
            coursePub.setId(id);
            coursePub.setName(name);
            coursePub.setPic(pic);
            coursePub.setGrade(grade);
            coursePub.setCharge(charge);
            coursePub.setTeachplan(teachplan);
            coursePub.setDescription(description);
            map.put(courseId, coursePub);
        }
        return map;
    }

    /**
     * 根据课程计划id查询媒资信息
     *      1.获取搜索请求对象
     *      2.设置类型
     *      3.创建构建搜索源对象
     *      4.根据课程计划id精确查询
     *      5.获取过滤字段的数组
     *      6.将构建搜索源对象设置到搜索请求对象中
     *      7.使用es进行搜索
     *      8.获取搜索结果对象
     * @param teachplanId
     * @return
     */
    public TeachplanMediaPub getmedia(String teachplanId) {
        //1.获取搜索请求对象
        SearchRequest searchRequest = new SearchRequest(media_index);
        //2.设置类型
        searchRequest.types(media_type);
        //3.创建构建搜索源对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //4.根据课程计划id精确查询
        searchSourceBuilder.query(QueryBuilders.termQuery("teachplan_id", teachplanId));
        //5.获取过滤字段的数组
        String[] split = media_source_field.split(",");
        //设置过滤字段
        searchSourceBuilder.fetchSource(split, new String[]{});
        //6.将构建搜索源对象设置到搜索请求对象中
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = null;
        try {
            //7.使用es进行搜索
            searchResponse = restHighLevelClient.search(searchRequest);

        } catch (IOException e) {
            e.printStackTrace();
        }
        //8.获取搜索结果对象
        SearchHits responseHits = searchResponse.getHits();
        //获取数据列表
        SearchHit[] searchHits = responseHits.getHits();
        TeachplanMediaPub teachplanMediaPub = null;
        for (SearchHit searchHit : searchHits) {
            teachplanMediaPub  =new TeachplanMediaPub();
            //获取数据源文档
            Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();
            //取出课程计划媒资信息
            String courseid = (String) sourceAsMap.get("courseid");
            String media_id = (String) sourceAsMap.get("media_id");
            String media_url = (String) sourceAsMap.get("media_url");
            String teachplan_id = (String) sourceAsMap.get("teachplan_id");
            String media_fileoriginalname = (String) sourceAsMap.get("media_fileoriginalname");
            //将信息设置到teachplanMediaPub中
            teachplanMediaPub.setCourseId(courseid);
            teachplanMediaPub.setMediaUrl(media_url);
            teachplanMediaPub.setMediaFileOriginalName(media_fileoriginalname);
            teachplanMediaPub.setMediaId(media_id);
            teachplanMediaPub.setTeachplanId(teachplan_id);
        }
        return teachplanMediaPub;
    }
}
