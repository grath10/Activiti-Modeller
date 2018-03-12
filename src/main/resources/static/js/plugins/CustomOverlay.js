// 定义构造函数并继承Overlay
function CustomOverlay(center, img, name, text){
    this._center = center;
    this._img = img;
    this._name = name;
    this._text = text;
}

// 继承API BMap.Overlay
CustomOverlay.prototype = new BMap.Overlay();

// 初始化自定义覆盖物
CustomOverlay.prototype.initialize = function (map) {
    // 保存map对象实例
    this._map = map;
    // 创建div元素，作为自定义覆盖物的容器
    var div = document.createElement("div");
    div.style.position = 'absolute';
    // 根据参数设置元素外观
    div.style.background = "url(" + this._img + ") no-repeat 0 -18px";
    div.style.MozUserSelect = 'none';

    var oneDiv = document.createElement("div");
    $(oneDiv).css({
        "height":"30",
        "width":"18",
        "white-space": "nowrap",
        "color":"#fff",
        "text-align":"center",
        "line-height":"30px"
    });
    $(div).append('<div class="infoDetail" style="display:none; position:absolute;padding:10px; left:25px; background:#fff;">' +
        '<div style="width:258px;height: 20px;"><span style="float: left;">名称：</span><span style="float: left;">' + this._name +
        '</span></div><div style="width: 258px;height: 20px;"><span style="float:left;">备注：</span><span style="float:left;">'+ this._text +'</span></div></div>');
    div.appendChild(oneDiv);
    $(div).mousemove(function(){
        $(this).css("z-index","999999");
        $(this).children(".infoDetail").css("display","block");
    });
    $(div).mouseout(function(){
        $(this).children(".infoDetail").css("display","none");
    });

    // 将div添加到覆盖物容器中
    map.getPanes().markerPane.appendChild(div);
    // 保存div实例
    this._div = div;
    // 需要将div元素作为方法的返回值，当调用该覆盖物的show、
    // hide方法，或者对覆盖物进行移除时，API都将操作此元素
    return div;
}

// 绘制覆盖物
CustomOverlay.prototype.draw = function () {
    // 根据地理坐标转换为像素坐标，并设置给容器
    var position = this._map.pointToOverlayPixel(this._center);
    this._div.style.left = position.x - 5 + 'px';
    this._div.style.top = position.y - 5 + 'px';
}

// 实现显示方法
CustomOverlay.prototype.show = function () {
  if(this._div){
      this._div.style.display = '';
  }
}

// 实现隐藏方法
CustomOverlay.prototype.hide = function () {
  if(this._div){
      this._div.style.display = 'none';
  }
}

