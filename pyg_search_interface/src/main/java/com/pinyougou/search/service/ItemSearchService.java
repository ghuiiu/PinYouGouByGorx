package com.pinyougou.search.service;

import java.util.Map;

/**
 * @Auther: Gorx
 * @Date: 2019/3/13 22:56
 * @Description:
 */
public interface ItemSearchService {
    //solr搜索方法，参数是map类型，返回值是map
    public Map search(Map searchMap);

    public void importItemToSolr(Long[] ids);

    public void removeItemFromSolr(Long[] ids);
}
