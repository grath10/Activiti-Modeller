$(function () {
    debugger;
    $('.form-datetime').datetimepicker({
        format: 'yyyy-mm-dd',
        language: 'zh-CN',
        weekStart: 1,
        startView: 2,
        minView: 2,
        autoclose: true,
        todayBtn: true,
        pickerPosition: "bottom-left"
    });
    $(".date-time-picker").val(new Date().Format('yyyy-mm-dd'));
    getUserApp();
});

function getUserApp() {
    $.ajax({
        url: '/workflow/queryApp',
        data: null,
        type: 'get',
        async: false,
        success: function (data) {
            getProcessDetails(data);
        },
        error: function () {
            console.log('查询失败');
        }
    });
}

function getProcessDetails(data) {
    var $table = $("#info-table");
    var table = $table.DataTable({
        dom: 't<"row"<"col-sm-5"i><"col-sm-7"p>>',
        lengthChange: false,
        destroy: true,
        iDisplayLength: 10,  //每页显示10条数据
        autoWidth: true,   //禁用自动调整列宽
        searching: false,
        ordering: false,
        data: data,
        columns: [{
            "title": "编号",
            className: "text-center",
            data: null,
            defaultContent: ''
        }, {
            "title": "流程实例ID",
            data: 'processInstanceId',
            className: "text-center"
        }, {
            "title": "流程定义ID",
            data: 'processDefinitionId',
            className: "text-center"
        }, {
            "title": "流程名称",
            data: 'name',
            className: "text-center"
        }, {
            "title": "流程状态",
            data: 'state',
            className: "text-center"
        }, {
            "title": "操作",
            data: null,
            defaultContent: '',
            className: "text-center"
        }],
        columnDefs: [{
            "targets": 4,
            'render': function (data, type, full, meta) {
                var str = '<span class="label label-success">已完成</span>';
                if(data == 'running'){
                    str = '<span class="label label-primary">运行中</span>';
                }else if(data == 'suspended'){
                    str = '<span class="label label-warning">暂停</span>';
                }
                return str;
            }
        }, {
            "targets": 5,
            'render': function (data, type, full, meta) {
                var processDefinitionId = full['processDefinitionId'];
                var processInstanceId = full['processInstanceId'];
                var state = full['state'];
                var str = '<a href="javascript:void(0)" onclick=getNodeInfo("' + processInstanceId + '","' + processDefinitionId + '")>查看详情</a>';
                if(state == 'running'){
                    str += '&nbsp;&nbsp;&nbsp;<a href="javascript:void(0)" onclick="suspendProcess(\'' + processInstanceId + '\')">挂起</a>';
                }else if(state == 'suspended'){
                    str += '&nbsp;&nbsp;&nbsp;<a href="javascript:void(0)" onclick="activateProcess(\'' + processInstanceId + '\')">激活</a>';
                }
                return str;
            }
        }],
        language: {
            url: '/i18n/Chinese.lang'
        },
        drawCallback: function(){
            var api = this.api();
            api.column(0).nodes().each(function(cell, i) {
                cell.innerHTML =  i + 1;
            });
        }
    });
}

function suspendProcess(processInstanceId) {
    $.ajax({
        url: '/workflow/suspend',
        data: {
            processInstanceId: processInstanceId
        },
        type: 'POST',
        async: false,
        success: function () {
            getUserApp();
        },
        error: function () {
            console.log('查询失败');
        }
    });
}

function activateProcess(processInstanceId) {
    $.ajax({
        url: '/workflow/activate',
        data: {
            processInstanceId: processInstanceId
        },
        type: 'POST',
        async: false,
        success: function () {
            getUserApp();
        },
        error: function () {
            console.log('查询失败');
        }
    });
}

function getNodeInfo(processId, processDefinitionId) {
    $.ajax({
        url: '/workflow/queryRunningNodes',
        data: {
            id: processDefinitionId,
            processInstanceId: processId
        },
        type: 'get',
        async: false,
        success: function (data) {
            $("#processDetailPanel").modal('show');
            getDetails(data);
        },
        error: function () {
            console.log('查询失败');
        }
    });
}

function getDetails(data) {
    var $table = $("#process-detail-table");
    var table = $table.DataTable({
        dom: 't<"row"<"col-sm-5"i><"col-sm-7"p>>',
        lengthChange: false,
        destroy: true,
        iDisplayLength: 10,  //每页显示10条数据
        autoWidth: false,   //禁用自动调整列宽
        searching: false,
        ordering: false,
        data: data,
        columns: [{
            "title": "编号",
            className: "text-center",
            data: null,
            defaultContent: ''
        }, {
            "title": "任务ID",
            data: 'taskId',
            className: "text-center"
        }, {
            "title": "任务名称",
            data: 'name',
            className: "text-center"
        }, {
            "title": "任务状态",
            data: 'status',
            className: "text-center"
        }, {
            "title": "处理人",
            className: "text-center",
            data: 'userId'
        }, {
            "title": "处理时间",
            className: "text-center",
            data: 'processTime'
        }, {
            "title": "处理结果",
            className: "text-center",
            data: 'result'
        }],
        columnDefs: [{
            "targets": 3,
            'render': function (data, type, full, meta) {
                if(data == 'FINISHED'){
                    return '<span class="label label-success">已执行</span>';
                }else if(data == 'PENDING'){
                    return '<span class="label label-danger">待执行</span>';
                }else if(data == 'PROCESSING'){
                    return '<span class="label label-primary">执行中</span>';
                }else if(data == 'EXPIRED'){
                    return '<span class="label label-warning">已超期</span>';
                }else{
                    return '<span class="label label-warning">未执行</span>';
                }
            }
        }, {
            "targets": 6,
            'render': function (data, type, full, meta) {
                if(data == 'true'){
                    return '<span class="label label-success">同意</span>';
                }else if(data == 'false'){
                    return '<span class="label label-danger">拒绝</span>';
                }else if(data == null){
                    return '<span class="label label-primary">提交</span>';
                }else{
                    return '';
                }
            }
        }],
        language: {
            url: '/i18n/Chinese.lang'
        },
        drawCallback: function(){
            var api = this.api();
            api.column(0).nodes().each(function(cell, i) {
                cell.innerHTML =  i + 1;
            });
        }
    });
}