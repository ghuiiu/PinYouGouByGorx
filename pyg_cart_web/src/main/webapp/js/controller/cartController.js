app.controller('cartController',function ($scope, cartService) {

    /**
     * 获取购物车列表
     */
    $scope.findCartListFromRedis = function () {
        cartService.findCartListFromRedis().success(
            function (response) {
                $scope.cartList = response;
                //每次查购物车的时候，求和
                sum();
            }
        )
    }

    /**
     * 商品添加购物车列表
     * @param itemId
     * @param num
     */

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


    /**
     * 查找所有地址列表
     */
    $scope.findListByUserId = function () {
         cartService.findListByUserId().success(
             function (response) {
                 $scope.addressList = response;
                 //在findAddressList方法中，回调方法增加，设置默认地址
                 for (var i = 0; i < $scope.addressList.length; i++) {
                     if ($scope.addressList[i].isDefault=='1'){
                         $scope.address = $scope.addressList[i];
                         break;
                     }
                 }
             }
         )
    }

    //页面选择人名后
    $scope.selectAddress=function (address) {
        $scope.address = address;
    }

    //判断是否是当前选中的地址
    $scope.isSelectedAddress=function(address){
        if(address==$scope.address){
            return true;
        }else{
            return false;
        }
    }


    //订单对象默认值
    $scope.order = {paymentType:'1'};

    //页面选择支付方式
    $scope.selectPaymentType = function (type) {
        $scope.order.paymentType = type;
    }

    //保存订单
    $scope.createOrder = function () {
        //收货人电话
        $scope.order.receiverMobile = $scope.address.mobile;
        //收货地址
        $scope.order.receiverAreaName = $scope.address.address;
        //收货人
        $scope.order.receiver = $scope.address.contact;

        cartService.createOrder($scope.order).success(
            function (response) {
                if (response.success){
                    location.href = "pay.html";
                } else {
                    alert(response.message);
                }
            }
        )
    }

    //求合计数和总金额
    sum = function () {
        //总金额
        $scope.totalMoney = 0;
        //总数量
        $scope.totalNum = 0;

        //循环购物车
        for (var i = 0; i < $scope.cartList.language; i++) {
            //从购物车列表获取购物车
            var cart = $scope.cartList[i];
            //循环获取购物车中的商品对象
            for (var j = 0; j < cart.orderItemList.length; j++) {
                //将每个商品的总金额累加
                $scope.totalMoney += cart.orderItemList[j].totalFee;
                //累加总数量
                $scope.totalNum += cart.orderItemList[j].num;
            }
        }
    }
});


















