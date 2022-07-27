package top.yifan;

import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.xcontent.XContentHelper;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

/**
 * 提供Elasticsearch相关功能的类
 * 
 * @author Star Zheng
 */
public class ElasticsearchTemplate {
    
    private static final Logger log = LoggerFactory.getLogger(ElasticsearchTemplate.class);
    
    private ElasticsearchClient elasticsearchClient;
    
    public ElasticsearchTemplate(ElasticsearchProperties esProperties) {
        this.elasticsearchClient = new ElasticsearchClient(esProperties);
    }
    
    public ElasticsearchTemplate(ElasticsearchClient elasticsearchClient) {
        this.elasticsearchClient = elasticsearchClient;
    }
    
    /**
     * 获取指定索引下指定主键的数据，并转换成指定class
     * 
     * @param index - 索引
     * @param id - 主键值
     * @param resultClazz - 结果类型
     * @return 返回指定类型的查询结果
     */
    public <T> T get(String index, String id, Class<T> resultClazz) {
        GetResponse getResponse = getResponse(index, id);
        BytesReference bytes = getResponse.getSourceAsBytesRef();
        if (bytes == null) {
            return null;
        }
        try {
            String sourceAsJson = XContentHelper.convertToJson(bytes, false, XContentType.JSON);
            if (StringUtils.isBlank(sourceAsJson)) {
                return null;
            }
            return JSON.parseObject(sourceAsJson, resultClazz);
        } catch (IOException e) {
            throw new ServiceException("Get elasticsearch data[index=" + index + ", "
                    + "id=" + id + "] and converting data type[JSON] error", e);
        }
    }
    
    /**
     * 获取指定索引下指定主键的数据
     * 
     * @param index - 索引
     * @param id - 主键值
     * @return 返回查询到的结果
     */
    public Map<String, Object> get(String index, String id) {
        GetResponse getResponse = getResponse(index, id);
        return getResponse.getSourceAsMap();
    }
    
    private GetResponse getResponse(String index, String id) {
        GetRequest getRequest = new GetRequest(index, id);
        try {
            return getClient().get(getRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new ServiceException("Get elasticsearch data[index=" + index + ", "
                    + "id=" + id + "] exception", e);
        }
    }
    
    /**
     * 根据指定的搜索条件，在指定的一个或多个索引下搜索数据
     * 
     * @param sourceBuilder - 搜索条件的建造器
     * @param indices - 索引列表
     * @return 返回搜索到的聚合结果
     */
    public Aggregations search(SearchSourceBuilder sourceBuilder, String... indices) {
        SearchRequest searchRequest = new SearchRequest(indices);
        searchRequest.source(sourceBuilder);
        try {
            SearchResponse searchResponse = getClient().search(searchRequest, RequestOptions.DEFAULT);
            return searchResponse.getAggregations();
        } catch (IOException e) {
            throw new ServiceException("Search elasticsearch data[index=" + Arrays.toString(indices)
                    + ", params: " + sourceBuilder + "] exception", e);
        }
    }
    
    /**
     * 保存指定数据到指定索引下，并为其指定主键值
     * 
     * @param index - 索引
     * @param id - 主键值
     * @param sourceData - 源数据
     */
    public void save(String index, String id, Object sourceData) {
        String source = toSource(sourceData);
        IndexRequest indexRequest = new IndexRequest(index);
        indexRequest.id(id);
        indexRequest.source(source, XContentType.JSON);
        try {
            getClient().index(indexRequest, RequestOptions.DEFAULT);
            log.debug("Saved data[id={}] to {}", id, index);
        } catch (IOException e) {
            throw new ServiceException("Save elasticsearch data[index=" + index + ", "
                    + "id=" + id + "] exception, and data: " + source, e);
        }
    }
    
    /**
     * 从指定索引下移除指定主键的数据
     * 
     * @param index - 索引
     * @param id - 主键值
     */
    public void remove(String index, String id) {
        DeleteRequest deleteRequest = new DeleteRequest(index, id);
        try {
            getClient().delete(deleteRequest, RequestOptions.DEFAULT);
            log.debug("Removed data[id={}] from {}", id, index);
        } catch (IOException e) {
            throw new ServiceException("Delete elasticsearch data[index=" + index + ", "
                    + "id=" + id + "] exception", e);
        }
    }
    
    private String toSource(Object sourceData) {
        return JSON.toJSONString(sourceData);
    }
    
    private RestHighLevelClient getClient() {
        return elasticsearchClient.getClient();
    }
    
}
