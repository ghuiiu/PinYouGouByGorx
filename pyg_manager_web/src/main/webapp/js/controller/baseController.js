 //品牌控制层 
app.controller('baseController' ,function($scope){	
	
    //重新加载列表 数据
    $scope.reloadList=function(){
    	//切换页码  
    	$scope.search( $scope.paginationConf.currentPage, $scope.paginationConf.itemsPerPage);	   	
    }
    
	//分页控件配置 
	$scope.paginationConf = {
         currentPage: 1,
         totalItems: 10,
         itemsPerPage: 10,
         perPageOptions: [10, 20, 30, 40, 50],
         onChange: function(){
        	 $scope.reloadList();//重新加载
     	 }
	}; 
	
	$scope.selectIds=[];//选中的ID集合 

	//更新复选
	$scope.updateSelection = function($event, id) {		
		if($event.target.checked){//如果是被选中,则增加到数组
			$scope.selectIds.push( id);			
		}else{
			var idx = $scope.selectIds.indexOf(id);
            $scope.selectIds.splice(idx, 1);//删除 
		}
	}

    //组装json文本数据
	$scope.jsonToStr = function (jsonStr, key) {
        //把json字符串转换json对象
		var typeJson = JSON.parse(jsonStr);
        //定义字符串对象，拼接json数组数据
		var value = "";
		//循环json数组对象
		for (var i = 0;i<typeJson.length;i++){
			if (i > 0) {
				value+=","
			}
			value += typeJson[i][key]
		}
		return value;
    }
});














