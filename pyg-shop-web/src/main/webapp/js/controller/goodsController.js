 //控制层 
app.controller('goodsController' ,function($scope,$controller   ,goodsService,itemCatService,typeTemplateService,uploadService){
	
	$controller('baseController',{$scope:$scope});//继承
	
    //读取列表数据绑定到表单中  
	$scope.findAll=function(){
		goodsService.findAll().success(
			function(response){
				$scope.list=response;
			}			
		);
	}    
	
	//分页
	$scope.findPage=function(page,rows){			
		goodsService.findPage(page,rows).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	
	//查询实体 
	$scope.findOne=function(id){				
		goodsService.findOne(id).success(
			function(response){
				$scope.entity= response;					
			}
		);				
	}
	
	//保存 
	$scope.save=function(){
		$scope.entity.goodsDesc.introduction = editor.html();
		goodsService.add($scope.entity).success(
            function(response){
                if(response.success){
                    alert('保存成功');
                    $scope.entity={};
                    //清空富文本编辑器
					editor.html('');
                }else{
                    alert(response.message);
                }
            }
        );
	}
	
	 
	//批量删除 
	$scope.dele=function(){			
		//获取选中的复选框			
		goodsService.dele( $scope.selectIds ).success(
			function(response){
				if(response.success){
					$scope.reloadList();//刷新列表
                    $scope.selectIds=[];
				}						
			}		
		);				
	}
	
	$scope.searchEntity={};//定义搜索对象 
	
	//搜索
	$scope.search=function(page,rows){			
		goodsService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}


    //读取一级分类
	$scope.selectItemCat1List=function () {
		itemCatService.findByParentId(0).success(function (response) {
			$scope.itemCat1List = response;
        })
    }

    //读取二级分类
	$scope.$watch('entity.goods.category1Id',function (newValue, oldValue) {
        //根据选择的值，查询二级分类
		itemCatService.findByParentId(newValue).success(function (response) {
			$scope.itemCat2List = response;
        })
    });

    //观察二级分类如果id有值，进行查询三级分类列表
    $scope.$watch('entity.goods.category2Id',function (newValue, oldValue) {
        itemCatService.findByParentId(newValue).success(
            function (response) {
                $scope.itemCat3List = response;
            }
        )
    });


    //观察三级分类id是否有新值
    $scope.$watch('entity.goods.category3Id', function(newValue, oldValue) {
        //根据选择的值，查询二级分类
        itemCatService.findOne(newValue).success(
            function(response){
                $scope.entity.goods.typeTemplateId = response.typeId;//更新模板ID
            }
        );
    });

    $scope.entity = {goodsDesc:{customAttributeItems:[]}}; //一定要初始化封装对象，否则无法直接赋值
    //模板ID选择后  更新品牌列表
    $scope.$watch('entity.goods.typeTemplateId',function (newValue, oldValue) {
        typeTemplateService.findOne(newValue).success(
            function (response) {
                $scope.typeTemplate = response;//获取类型模板
                $scope.typeTemplate.brandIds = JSON.parse($scope.typeTemplate.brandIds);//品牌列表
                //将模版中的自定义属性赋值给封装对象的自定义属性
				$scope.entity.goodsDesc.customAttributeItems = JSON.parse($scope.typeTemplate.customAttributeItems);
            }
        )

        //查询规格列表
		typeTemplateService.findSpecList(newValue).success(
			function (response) {
				$scope.specList = response;
            }
		)
    })


    //图片上传
    $scope.uploadFile = function () {
        uploadService.uploadFile().success(
            function (response) {
                if(response.success){
                    $scope.image_entity.url = response.message; //将图片地址封装到image_entity
                }else{
                    alert(response.message);
                }
            }
        )
    }

    //初始化图片列表结构
    $scope.entity = {goodsDesc:{customAttributeItems:[],itemImages:[],specificationItems:[]}};

    //添加图片列表
	$scope.addImage = function () {
        $scope.entity.goodsDesc.itemImages.push($scope.image_entity);
    }

    //移除图片列表中的image_entity对象
	$scope.deleImage=function ($index) {
        $scope.entity.goodsDesc.itemImages.splice($index,1)
    }

    //name 规格名称  value 规格选项名称   $event:checkbox, push的时候是否已经初始化specificationItems
    $scope.updateSpecAttribute = function ($event,name,value) {
        //查询传入的name是否已经存在$scope.entity.goodsDesc.specificationItems
		var object = searchObjectByKey($scope.entity.goodsDesc.specificationItems,'attributeName',name);
		
		if (object != null){//根据勾选的规格名称从$scope.entity.goodsDesc.specificationItems中找到
			if ($event.target.checked){//勾选
				object.attributeValue.push(value);
			}else {//取消勾选
				object.attributeValue.splice(object.attributeValue.indexOf(value),1);//移除掉规格选项
                //判断规格选项的集合是否还有内容，如果没有将整个规格从$scope.entity.goodsDesc.specificationItems移除掉
				if (object.attributeValue.length < 1) {
                    $scope.entity.goodsDesc.specificationItems.splice($scope.entity.goodsDesc.specificationItems.indexOf(object),1);
				}
			}
		}else {
            //直接push一个对象到封装对象中
            $scope.entity.goodsDesc.specificationItems.push({"attributeName":name,"attributeValue":[value]});
		}
    }

    //[{"attributeName":"网络制式","attributeValue":["移动3G","移动4G"]},{"attributeName":"屏幕尺寸","attributeValue":["6寸","5.5寸"]}]
    searchObjectByKey = function (list,key,value) {
        for (var i = 0; i < list.length; i++) {
			if (list[i][key]==value){
				return list[i];
			}
        }
        return null;
    }

    //创建出封装对象的itemList
    $scope.createItemList = function() {
        $scope.entity.itemList = [ {spec : {},price : 0,num : 99999,status : '0',isDefault : '0'} ];// 初始
        var items = $scope.entity.goodsDesc.specificationItems; //用户勾选的规格&规格选项
        for (var i = 0; i < items.length; i++) {   //循环用户所有的规格
            $scope.entity.itemList = addColumn($scope.entity.itemList, items[i].attributeName, items[i].attributeValue);
        }
    }


    //list = [ {spec : {},price : 0,num : 99999,status : '0',isDefault : '0'} ];  columnName=规格名称  ;conlumnValues = 规格选项集合
    addColumn = function(list, columnName, conlumnValues) {
        var newList = [];// 新的集合
        for (var i = 0; i < list.length; i++) {
            var oldRow = list[i];  //获取出当前行的内容 {spec:{},price:'0.01',num:'99999',status:'0',isDefault:'0'}
            for (var j = 0; j < conlumnValues.length; j++) {//循环attributeValue数组的内容
                var newRow = JSON.parse(JSON.stringify(oldRow));// 深克隆,根据attributeValue的数量
                newRow.spec[columnName] = conlumnValues[j];//{spec:{"网络制式":"移动4G"},price:'0.01',num:'99999',status:'0',isDefault:'0'}
                newList.push(newRow);
            }
        }
        return newList;
    }
});	


















