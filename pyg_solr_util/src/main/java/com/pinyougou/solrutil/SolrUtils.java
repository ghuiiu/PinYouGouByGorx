package com.pinyougou.solrutil;

import com.alibaba.fastjson.JSON;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * @Auther: Gorx
 * @Date: 2019/3/13 22:19
 * @Description:
 */
@Component
public class SolrUtils {
    @Autowired
    private TbItemMapper itemMapper;

    @Autowired
    private SolrTemplate solrTemplate;

    //一次性将tbitem导入solr库
    public void importItemToSolr(){
        List<TbItem> items = itemMapper.selectByExample(null);
        for (TbItem item : items) {
            System.out.println(item.getId()+" "+ item.getTitle()+ " "+item.getPrice());
            //item.getSpec()是个json的字符串但是可以转成map{"网络":"移动4G","机身内存":"16G"}
            //从数据库中提取规格json字符串转换为map
            Map<String,String> specMap = JSON.parseObject(item.getSpec(), Map.class);
            item.setSpecMap(specMap);
        }
        //保存array到solr库
        solrTemplate.saveBeans(items);
        solrTemplate.commit();//提交
    }

    public static void main(String[] args) {
        //classpath* 代表访问jar包中的配置文件
        ApplicationContext ac = new ClassPathXmlApplicationContext("classpath*:spring/applicationContext*.xml");
        SolrUtils solrUtils = (SolrUtils) ac.getBean("solrUtils");
        solrUtils.importItemToSolr();
    }
}

















