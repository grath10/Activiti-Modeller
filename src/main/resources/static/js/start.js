$(function () {
    $("#process-start-btn").click(function () {
            var name = $("#processName").val();
            if(name == 'leave'){
                $("#leave-panel").modal('show');
            }else if(name == 'emergency'){
                startProcess(name);
            }
        }
    );

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
    $("#leave-submit").on('click', function(){
        return startProcess('leave');
    });
});

function startProcess(key) {
    if(key == 'leave') {
        var startDate = $("#leave-startDate").val();
        var endDate = $("#leave-endDate").val();
        var reason = $("#leave-reason").val();
        $.ajax({
            url: '/workflow/initiate',
            data: {
                key: key,
                startDate: startDate,
                endDate: endDate,
                reason: reason
            },
            async: false,
            success: function () {
                alert('操作成功');
            }
        });
    }else{
        $.ajax({
            url: '/workflow/initiate',
            data: {
                key: key
            },
            async: false,
            success: function () {
                alert('操作成功');
            }
        });
    }
}