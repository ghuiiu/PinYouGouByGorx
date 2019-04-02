app.service('cartService',function ($http) {

    this.findCartListFromRedis = function () {
        return $http.get('cart/findCartListFromRedis.do')
    }

    this.addGoodsToCartList = function (itemId, num) {
        return $http.get('cart/addGoodsToCartList.do?itemId='+itemId+'&num='+num)
    }
    
    this.findListByUserId = function () {
        return $http.get('address/findListByUserId.do')
    }

    //保存订单
    this.createOrder = function (order) {
        return $http.post('order/add.do',order);
    }
});