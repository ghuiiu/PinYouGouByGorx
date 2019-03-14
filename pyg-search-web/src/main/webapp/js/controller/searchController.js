app.controller('searchController',function ($scope,searchService) {

    //搜索条件
    //前台传递搜索参数条件很多，因此使用对象对参数做一个封装
    //分类
    //品牌
    //内存，尺寸，价格，排序，分页.....
    $scope.searchMap = {keywords:'三星',spec:{},sort:'ASC'};//注意一定要对spec规格进行初始化 ,sort是升降序

    //关键字搜索
    $scope.search = function () {
        searchService.search($scope.searchMap).success(
            function (response) {
                $scope.list = response.content;
                /*alert(JSON.stringify($scope.list));*/
            }
        )
    }
});

//自定义过滤器 第一个参数是trustHtml名字$sce是内部指令
app.filter('trustHtml',['$sce',function ($sce) {
    return function (data) {
        return $sce.trustAsHtml(data);
    }
}]);