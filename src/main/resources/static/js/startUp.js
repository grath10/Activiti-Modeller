$(document).bind('_page_ready', function () {
    ComponentInitializer.initPageComponents();
    // 转换超链接为ajax加载，引用tools.js
    $("a[hf]").each(function (e) {
        $(this).attr("href", $(this).attr("hf"));
    })
    $("a[target]").convertlink();
});

/*<![CDATA[*/
$(document).bind('_child_page_ready', function () {

});
/*]]>*/

$(function () {
    $(document).trigger('_page_ready');
    window.localStorage.clear();
});