app.controller('cartController',function ($scope, cartService) {

    /**
     * 获取购物车列表
     */
    $scope.findCartListFromRedis = function () {
        cartService.findCartListFromRedis().success(
            function (response) {
                $scope.cartList = response;
            }
        )
    }

    $scope.addGoodsToCartList = function (itemId, num) {
        cartService.addGoodsToCartList(itemId, num).success(
            function (response) {
                if (response.success){
                    //刷新购物车页面
                    $scope.findCartListFromRedis();
                }else {
                    alert(response.message);
                }
            }
        )
    }
});