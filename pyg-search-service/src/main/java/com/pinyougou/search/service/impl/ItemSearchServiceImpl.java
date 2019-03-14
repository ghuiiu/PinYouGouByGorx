package com.pinyougou.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbItemExample;
import com.pinyougou.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Criteria;
import org.springframework.data.solr.core.query.HighlightOptions;
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
        System.out.println(keywords);

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



























