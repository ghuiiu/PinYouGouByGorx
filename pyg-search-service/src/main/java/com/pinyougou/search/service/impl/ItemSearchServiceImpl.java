package com.pinyougou.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbItemExample;
import com.pinyougou.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Criteria;
import org.springframework.data.solr.core.query.HighlightOptions;
import org.springframework.data.solr.core.query.SimpleFilterQuery;
import org.springframework.data.solr.core.query.SimpleHighlightQuery;
import org.springframework.data.solr.core.query.result.HighlightEntry;
import org.springframework.data.solr.core.query.result.HighlightPage;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Auther: Gorx
 * @Date: 2019/3/13 23:01
 * @Description:
 */
@Service
public class ItemSearchServiceImpl implements ItemSearchService {

    @Autowired
    private SolrTemplate solrTemplate;

    @Autowired
    private TbItemMapper itemMapper;

    @Override
    public Map search(Map searchMap) {
        //传入的关键字
        String keywords = (String) searchMap.get("keywords");
        // 创建高亮查询对象
        SimpleHighlightQuery query = new SimpleHighlightQuery();

        //设置高亮的域
        HighlightOptions highlightOptions = new HighlightOptions().addField("item_title");
        //设置高亮前缀
        highlightOptions.setSimplePrefix("<em style='color:red'>");
        //高亮后缀
        highlightOptions.setSimplePostfix("</em>");
        //设置高亮选项
        query.setHighlightOptions(highlightOptions);

        if (keywords != null && keywords.length()>0){
            //拼接关键字查询
            Criteria criteria = new Criteria("item_keywords");//设置查询域的名字，查询的是复制域
            criteria = criteria.contains(keywords);//查询关键字条件
            query.addCriteria(criteria);//查询添加条件
        }

        //=========根据分类进行过滤条件查询========
        String category = (String) searchMap.get("category");
        if (category  != null && category .length()>0) {
            //在查询中增加过滤条件
            SimpleFilterQuery filterQuery = new SimpleFilterQuery();
            Criteria criteria = new Criteria("item_category");//需要设置域的名字
            criteria = criteria.is(category);//封装分类的查询条件
            filterQuery.addCriteria(criteria);
            query.addFilterQuery(filterQuery);//过滤查询
        }


        //============根据品牌进行过滤查询==========条件必须在查询之前完成
        String brandStr = (String) searchMap.get("brand");
        if (brandStr!= null && brandStr .length()>0) {
            //在查询中增加过滤条件
            SimpleFilterQuery filterQuery = new SimpleFilterQuery();
            Criteria criteria = new Criteria("item_brand");//需要设置域的名字
            criteria = criteria.is(brandStr);//封装品牌的查询条件
            filterQuery.addCriteria(criteria);
            query.addFilterQuery(filterQuery);//过滤查询
        }

        //============根据规格进行过滤查询==========
        Map<String, String> specMap = (Map<String, String>) searchMap.get("spec");
        if (specMap!=null) {
            for (String key : specMap.keySet()) {
                //循环所有规格的名称并添加入搜索条件
                SimpleFilterQuery filterQuery = new SimpleFilterQuery();
                Criteria criteria = new Criteria("item_spec_"+key);//需要设置域的名字
                criteria = criteria.is(specMap.get(key));
                filterQuery.addCriteria(criteria);
                query.addFilterQuery(filterQuery);//过滤查询
            }
        }

        //============根据价格进行过滤查询==========
        String priceStr = (String) searchMap.get("price");
        if (priceStr!= null && priceStr.length()>0) {
            //按照-进行切割
            String[] prices = priceStr.split("-");

            //价格的起始条件封装
            SimpleFilterQuery filterQuery = new SimpleFilterQuery();
            Criteria criteria = new Criteria("item_price");
            //大于等于prices[0]
            criteria = criteria.greaterThanEqual(prices[0]);
            filterQuery.addCriteria(criteria);
            query.addFilterQuery(filterQuery);//过滤查询
            //小于的等于的条件不是*
            if (!"*".equals(prices[1])){
                SimpleFilterQuery filterQuery2 = new SimpleFilterQuery();
                Criteria criteria2 = new Criteria("item_price");
                criteria2 = criteria2.lessThan(prices[1]);
                filterQuery2.addCriteria(criteria2);
                query.addFilterQuery(filterQuery2);
            }
        }


        //增加排序条件
        String sortStr = (String) searchMap.get("sort");

        if ("ASC".equals(sortStr)){
            Sort sort = new Sort(Sort.Direction.ASC, "item_price");
            query.addSort(sort);
        }else {
            Sort sort = new Sort(Sort.Direction.DESC, "item_price");
            query.addSort(sort);
        }

        //设置分页条件，需要
        Integer pageSize = (Integer) searchMap.get("pageSize");
        Integer pageNo = (Integer) searchMap.get("pageNo");

        //设置页面尺寸
        query.setRows(pageSize);
        //设置起始页数
        query.setOffset((pageNo-1)*pageSize);


        //进行高亮查询
        HighlightPage<TbItem> page = solrTemplate.queryForHighlightPage(query, TbItem.class);

        List<TbItem> content = page.getContent();

        //循环所有的结果,将高亮效果重新设置到title中
        for (TbItem item : content) {
            List<HighlightEntry.Highlight> highlights = page.getHighlights(item); //拿到highlights集合

            if (highlights.size()>0) {//防止get0崩
                HighlightEntry.Highlight highlight = highlights.get(0);
                List<String> snipplets = highlight.getSnipplets();//snipplets也是集合

                if (snipplets.size()>0) {
                    String str = snipplets.get(0);//高亮效果
                    item.setTitle(str);
                }
            }
        }



        //封装返回值
        Map returnMap = new HashMap();
        returnMap.put("content",content);

        //封装总记录数
        returnMap.put("total",page.getTotalElements());
        return returnMap;//替换掉没有高亮效果的title
    }

    @Override
    public void importItemToSolr(Long[] ids) {

        TbItemExample example = new TbItemExample();
        //通过工具类将Long[]转成array
        example.createCriteria().andGoodsIdIn(Arrays.asList(ids));
        List<TbItem> items = itemMapper.selectByExample(example);
        for (TbItem item : items) {
            System.out.println("==向solr库添加=="+item.getTitle());
        }

        solrTemplate.saveBeans(items);//将查询到的所有符合条件的item对象存入solr
        solrTemplate.commit();//提交一下
    }

    @Override
    public void removeItemFromSolr(Long[] ids) {
        TbItemExample example = new TbItemExample();
        //通过工具类将Long[]转成array
        example.createCriteria().andGoodsIdIn(Arrays.asList(ids));
        List<TbItem> items = itemMapper.selectByExample(example);

        //循环查到的所有符合条件的tbItem
        for (TbItem item : items) {
            solrTemplate.deleteById(item.getId().toString());//需要tbItem的id
            System.out.println("==从solr库移除=="+item.getTitle());
        }

        solrTemplate.commit();//提交
    }


}



























