// 全局公用Jquery plugins
(function ($) {
    // 行政区划联动下拉列表， geoselects
    $.fn.geoselects = function (options) {
        // build main options before element iteration
        var opts = $.extend({}, $.fn.geoselects.defaults, options);
        var $this = $(this);

        $("select", $this).select2({
            ajax: {
                url: opts.url,
                dataType: 'json',
                delay: opts.delay,
                data: function (params) {
                    var type = $(this).attr('geo_type');
                    //get parentID
                    var parentId = -1;
                    if (type == 'CT') {
                        if (opts.provId) {
                            parentId = opts.provId;
                        }
                        else {
                            var pVal = $("select[geo_type='PR']", $this).val();
                            if (pVal)
                                parentId = pVal;
                        }
                    } else if (type == 'DI') {
                        if (opts.cityId) {
                            parentId = opts.cityId;
                        }
                        else {
                            var pVal = $("select[geo_type='CT']", $this).val();
                            if (pVal)
                                parentId = pVal;
                        }
                    } else if (type == 'ST') {
                        if (opts.distId) {
                            parentId = opts.distId;
                        }
                        else {
                            var pVal = $("select[geo_type='DI']", $this).val();
                            if (pVal)
                                parentId = pVal;
                        }
                    }
                    return {
                        term: params.term,
                        type: type,
                        parentId: parentId,
                        page: params.page
                    };
                },
                processResults: function (data, page) {
                    return {results: data};
                }
            }
        }).on("select2:select", function (e) {
            var selected = e.params.data;
            if (typeof selected !== "undefined") {
                //save to localstorage
                saveSelectedOption(e.target, selected);

                var type = $(this).attr('geo_type');
                if (type == 'PR') {
                    var subSelects = "select[geo_type != 'PR']";
                    if ($(subSelects, $this).length > 0) {
                        $(subSelects, $this).reset();
                    }
                } else if (type == 'CT') {
                    var subSelects = "select[geo_type != 'PR'][geo_type != 'CT']";
                    if ($(subSelects, $this).length > 0) {
                        $(subSelects, $this).reset();
                    }
                } else if (type == 'DI') {
                    var subSelects = "select[geo_type = 'ST']";
                    if ($(subSelects, $this).length > 0) {
                        $(subSelects, $this).reset();
                    }
                }

                if (opts.refresh) {
                    refreshMainTable(opts.table);
                }

                if (opts.selected) {
                    opts.selected();
                }
            }
        }).on("select2:unselect", function (e) {
            //remove from localstorage
            removeSaved(e.target);

            $(this).val(null);
            if (opts.refresh) {
                refreshMainTable(opts.table);
            }
        });

        //clean before load from localStorage
        $("select", $this).val(null).trigger('change');
        //load data from localstorage
        $("select", $this).each(function (idx, element) {
            loadSelectedOption(element);
        });
    };

    function refreshMainTable(tableid) {
        var tgt = $(tableid);
        if (tgt.length > 0) {
            $(tgt).DataTable().draw();
        } else {
            var $tables = $(".dataTable");
            if ($tables.length > 0) {
                $tables.each(function (idx, table) {
                    $(table).DataTable().draw();
                });
            }
        }
    }

    //刷新主表
    function refreshDatatables() {
        var $tables = $(".dataTable");
        if ($tables.length > 0) {
            $tables.each(function (idx, table) {
                $(table).DataTable().draw();
            });
        }
    }

    $.submitSuccess = function (modal, form, url) {
        $(modal).modal("hide");
        // $(modal).remove();
        // $('.modal-backdrop').remove();
        $(form + " input[type='text']").val('');
        if (!url) {
            refreshDatatables();
        }

        if (url) {
            // $(document).unbind('_child_page_ready');
            // $('#main-content').load(url, function () {
            //     $(document).trigger('_child_page_ready');
            // });
        }
    }

    /**
     * load selected option val from local storage
     */
    function loadSelectedOption(selectElement) {
        //if 'id' is not there, use 'name'
        var key = $(selectElement).attr('id');
        key = key ? key : $(selectElement).attr('name');
        var selected = window.localStorage.getItem(key);
        if (selected) {
            var obj = JSON.parse(selected);
            $(selectElement).append("<option value='" + obj.id + "'>" + obj.text + "</option>");
            $(selectElement).val(obj.id).trigger("change");
        }
    }

    /**
     * save selected option val into local storage
     */
    function saveSelectedOption(selectElement, selectedOption) {
        //if 'id' is not there, use 'name'
        var key = $(selectElement).attr('id');
        key = key ? key : $(selectElement).attr('name');
        var selected = {
            id: $(selectElement).val(),
            text: $(selectElement).find("option:selected").text()
        };
        //save data to localStorage
        window.localStorage.setItem(key, JSON.stringify(selected));
    }

    /**
     * remove selected option val from local storage
     */
    function removeSaved(selectElement) {
        //if 'id' is not there, use 'name'
        var key = $(selectElement).attr('id');
        key = key ? key : $(selectElement).attr('name');
        window.localStorage.removeItem(key);
    }

    // plugin defaults
    $.fn.geoselects.defaults = {
        url: '/system/geography/optionsData',
        delay: 500,
        table: '#main_datatable',
        refresh: true,
        selected: null
    };

    /**
     * 单选下拉列表
     */
    $.fn.singleSelect = function (options) {
        // build main options before element iteration
        var opts = $.extend({}, $.fn.singleSelect.defaults, options);
        var selObj;
        if (opts.url == null || opts.url == '') {
            selObj = $(this).select2();
        } else {
            selObj = $(this).select2({
                ajax: {
                    url: opts.url,
                    delay: opts.delay,
                    data: opts.data,
                    processResults: function (data, page) {
                        return {results: data};
                    }
                }
            });
        }
        selObj.on('select2:select', function (e) {
            var selected = e.params.data;
            if (typeof selected !== "undefined") {
                //save data to localStorage
                saveSelectedOption(this, selected);
            }
            if (opts.refresh) {
                refreshMainTable(opts.table);
            }

            if (opts.selected) {
                opts.selected();
            }
        }).on("select2:unselect", function (e) {
            $(this).val(null);
            //remove from localStorage
            removeSaved(this);
            if (opts.refresh) {
                refreshMainTable(opts.table);
            }
        });
        //load from localstorage
        loadSelectedOption(this);
        return selObj;
    };

    //reset selected value for dropdown list
    $.fn.reset = function () {
        if (this.length > 1) {
            $(this).each(function (idx, e) {
                if ($(this).is('select')) {
                    $(e).val(null).trigger('change');
                    removeSaved(e);
                }
            });
        } else if (this.length == 1) {
            //reset the value
            if ($(this).is('select')) {
                $(this).val(null).trigger('change');
                //clean local storage
                removeSaved(this);
            }
        }
    };

    $.fn.resetRangeSelect = function () {
        if (this.length > 1) {
            $(this).each(function (idx, e) {
                $('span', this).html('<i class="fa fa-calendar"></i> 请选择查询时段');
            });
        } else if (this.length == 1) {
            //reset the value
            $('span', this).html('<i class="fa fa-calendar"></i> 请选择查询时段');
        }
    };

    // plugin defaults
    $.fn.singleSelect.defaults = {
        url: '',
        delay: 500,
        table: '#main_datatable',
        refresh: true
    };

    $.fn.deleSelected = function (options) {
        var opts = $.extend({}, $.fn.deleSelected.defaults, options);
        $(this).click(function () {
            var message = '确定操作?';
            message = opts.msg ? opts.msg : message;
            var vids = [];
            $("input[name='checkList']:checked").each(function () {
                vids.push($(this).attr(opts.rowid));
            });
            if (vids.length > 0) {
                if (confirm(message)) {
                    $.ajax({
                        url: opts.url,
                        type: 'POST',
                        dataType: "json",
                        contentType: "application/json;charset=utf-8",
                        data: JSON.stringify(vids),
                        success: function (data, textStatus, jqXHR) {
                            // ComponentInitializer.alertInfoMessage("操作成功！");
                            // 刷新表格
                            refreshMainTable(opts.table);
                        }
                    });
                }
            } else {
                alert("请先选择需要操作的数据行！");
            }
        });
    };

    // plugin defaults
    $.fn.deleSelected.defaults = {
        url: '',
        rowid: 'rowid',
        table: '#main_datatable'
    };

    //checkbox的全选
    $.fn.checkAll = function (options) {
        var opts = $.extend({}, $.fn.checkAll.defaults, options);
        $(this).click(function () {
            //checkbox的全选
            $(opts.selects).each(function () {
                $(this).prop("checked", $(opts.top).prop("checked"));
            });
        });
    };

    // plugin defaults
    $.fn.checkAll.defaults = {
        top: "#checkAll",
        selects: "input[name='checkList']"
    };

    $.fn.resetForm = function (form, success) {
        $(this).click(function () {
            form = form ? form : "#queryForm";
            $(form + " select").reset();
            if (success) {
                success();
            }
            refreshDatatables();
        });
    };

    //history based on <div>
    var history_opts;
    $.fn.history = function (options) {
        //if still not init
        if (!history_opts) {
            history_opts = $.extend({}, $.fn.history.defaults, options);
        }
    };

    /**
     * return back to previous page
     * @param url
     */
    $.fn.return = function () {
        $(this).click(function () {
            var url = historyObj.pop();
            if (!url) {
                return;
            }
            $(document).unbind("_child_page_ready");
            $("#main-content").load(url, function () {
                $("#main-content a[target]").convertlink();
                $(document).trigger('_child_page_ready');
            });
        });
    }

    // plugin defaults
    $.fn.history.defaults = {
        maxsteps: 5,
        maincontent: "#main-content"
    };

    String.prototype.endWith = function (s) {
        if (s == null || s == "" || this.length == 0 || s.length > this.length)
            return false;

        if (this.substring(this.length - s.length) == s)
            return true;
        else
            return false;
    }

    //serialize form to json string
    $.fn.serializeJson = function (success) {
        var serializeObj = {};
        var array = this.serializeArray();
        $(array).each(function () {
            var eleName = this.name;

            if (this.name.endWith("[]")) {
                eleName = this.name.replace("[]", "");
            }

            if (serializeObj[eleName]) {
                if ($.isArray(serializeObj[eleName])) {
                    serializeObj[eleName].push(this.value);
                } else {
                    serializeObj[eleName] = [serializeObj[eleName], this.value];
                }
            } else {
                if (this.name.endWith("[]")) {
                    serializeObj[eleName] = [this.value];
                } else {
                    serializeObj[eleName] = this.value;
                }
            }
        });
        if (success) {
            success(serializeObj);
        }
        return JSON.stringify(serializeObj);
    };

    /**
     * DataRangePicker
     */
    $.fn.emsdaterangepicker = function (options, callback) {
        var defaultOptions = {
            autoApply: true,
            locale: {
                applyLabel: '确认',
                cancelLabel: '取消',
                fromLabel: '从',
                toLabel: '到',
                weekLabel: '周',
                customRangeLabel: '自定义',
                daysOfWeek: ["日", "一", "二", "三", "四", "五", "六"],
                monthNames: ["一月", "二月", "三月", "四月", "五月", "六月", "七月", "八月", "九月", "十月", "十一月", "十二月"]
            },
            startDate: moment().startOf('week'),
            endDate: moment().endOf('week')
        }
        var opts = $.extend({}, defaultOptions, options);
        var $this = $(this);

        //Date range as a button
        $(this).daterangepicker(
            opts,
            function (start, end, label) {
                $("span", $this).html(start.format('YYYY-MM-DD') + ' 到 ' + end.format('YYYY-MM-DD'));
                callback(start, end);
                //刷新表格
                refreshMainTable(opts.table);
            }
        );
    }

    /**
     * 把所有a连接元素转换成ajax加载，前提是target的值是以'#'开头
     */
    $.fn.convertlink = function () {
        //为所有的左边菜单连接加载ajax页面加载，前提是连接的target是#号开头
        $(this).unbind("click").click(function () {
            var tgt = $(this).attr("target");
            if (tgt && tgt.startsWith("#")) {
                $(document).unbind("_child_page_ready");
                var href = $(this).attr("href");
                //push to history step
                historyObj.push(href);

                $(tgt).load(href, function (response, status, xhr) {
                    if (status == "success") {
                        $("a[target]", tgt).convertlink();
                        $(document).trigger('_child_page_ready');
                    }
                    else {
                        $("#li-busy").css('display', 'none');
                        historyObj.pop();
                    }
                });
                return false;
            }
        })
    }

    $.fn.report = function (url, form) {
        $(this).unbind("click").click(function () {
            var params = '';
            if (form) {
                params = $(form).serialize();
            } else {
                params = $('#queryForm').serialize();
            }

            //push to history step
            historyObj.push("/reports");

            $("#main-content").load("/reports", function () {
                var rpturl = '';
                if (url.indexOf("?") > 0) {
                    rpturl = url + params;
                } else {
                    rpturl = url + '?' + params;
                }
                rpturl = rpturl + "";
                $("#reportDisplayArea").attr("src", rpturl);
            });
        });
    }

    var Fb = /^(?:input|select|textarea|keygen)/i,
        Eb = /^(?:submit|button|image|reset|file)$/i,
        Db = /\r?\n/g,
        X = /^(?:input|select|textarea|button)$/i;

    $.fn.serializeArr = function() {
        return this.map(function() {
            var a = $.prop(this, "elements");
            return a ? $.makeArray(a) : this
        }).filter(function() {
            var a = this.type;
            return this.name && Fb.test(this.nodeName) && !Eb.test(a) && (this.checked || !X.test(a))
        }).map(function(a, b) {
            var c = $(this).val();
            return null == c ? null : $.isArray(c) ? $.map(c, function(a) {
                return {
                    name: b.name,
                    value: a.replace(Db, "\r\n")
                }
            }) : {
                name: b.name,
                value: c.replace(Db, "\r\n")
            }
        }).get();
    }
})(jQuery);