app.service('cartService',function ($http) {

    this.findCartListFromRedis = function () {
        return $http.get('cart/findCartListFromRedis.do')
    }

    this.addGoodsToCartList = function (itemId, num) {
        return $http.get('cart/addGoodsToCartList.do?itemId='+itemId+'&num='+num)
    }
});