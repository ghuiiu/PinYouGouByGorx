package com.pinyougou.page.service;

/**
 * @Auther: Gorx
 * @Date: 2019/3/15 23:38
 * @Description:
 */
public interface ItemPageService {
    /**
     * 生成商品详细页
     * @param goodsId
     */
    public void createHtml(Long goodsId);


    /**
     *  删除商品详细页
     * @param ids
     */
    void removeHtml(Long[] ids);
}
