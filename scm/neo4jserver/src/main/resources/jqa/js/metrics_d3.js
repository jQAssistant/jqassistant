var jqad3 = {

    /* list with header names*/
    headerList: null,

    currentResult: null,

    /**
     * Callback function. renders the chart.
     * @param result json object with response
     */
    renderChart: function (result) {

        console.log("renderChart");

        this.currentResult = result;

        this.drawChart(result);
    },

    /**
     * Render the chart again with the last result
     */
    reRenderChart: function () {

        console.log("reRenderChart()")

        if (!this.currentResult) {
            // there is actually nothing to draw
            console.log("reRenderChart() there is nothing to draw. Abort.")
            return;
        }

        this.drawChart(this.currentResult);
    },

    /**
     * Draw the chart.
     *
     * @param result the JSON used to draw the chart
     */
    drawChart: function (result) {

        console.log("drawChart()")

        var containerDiv = d3.select("#treemapContainer");

        var width = parseInt(containerDiv.style('width'));
        var height = parseInt(containerDiv.style('height'));

        var layout = d3.layout.treemap().sticky(false).round(false).sort(function (a, b) {
            return a.size < b.size ? -1 : a.size > b.size ? 1 : 0;
        });
        layout.size([width, height]).value(function (d) {
            return d.size;
        });

        containerDiv.selectAll(".node").remove();
        jqad3.headerList = result.columns;

        var d3hierarchy = jqad3.transformJson(result.data);
        var colors = jqad3.createColorMap(result.data);

        // node is an array of nodes (divs)
        var node = containerDiv
            .datum(d3hierarchy)
            .selectAll(".node")
            .data(layout.nodes)
            .enter()
            .append("div")
            .attr("class", "node node-d3")
            .style("background", function (d) {
                return colors(d.size);
            })
            .style("position", "absolute")
            .style("left", function (d) {
                return d.x + "px";
            })
            .style("top", function (d) {
                return d.y + "px";
            })
            .style("width", function (d) {
                return Math.max(0, d.dx - 1) + "px";
            })
            .style("height", function (d) {
                return Math.max(0, d.dy - 1) + "px";
            })
            .html(function (d) {
                var backgroundColor = colors(d.size);
                var textColor = getContrastYIQ(backgroundColor);
                return "<div style='color:" + textColor + ";'>" + d.name + "</div>";
            });

        // set event handler to all nodes
        node.attr("onmousemove", "jqad3.showTooltip(event, this);");
        node.attr("onmouseout", "jqad3.hideTooltip(event, this);");
        node.attr("onclick", "jqad3.clickNode(this);");
    },

    /* click on a square */
    clickNode: function (element) {

        console.log("clickNode " + element.__data__.name);

        var metric = MetricGroups.activeMetric;
        var next = metric["next"];
        if (next == null) {
            // nothing to drill down
            return;
        }

        var parameterMap = {};
        parameterMap[JqaConstants.GROUP_ID] = metric["group"]["id"];
        parameterMap[JqaConstants.METRICS_ID] = metric["next"]["id"];

        // now get the current drill down parameters from the URL, we use the "parameters" attribute of the next metric
        // for this
        var parameters = next["parameters"];
        if (parameters && parameters.length > 0) {
            $.each(parameters, function (index, value) {
                var urlHashValue = getUrlHashParameterValue(value);
                if (urlHashValue) {
                    parameterMap[value] = urlHashValue;
                }
            });
        }

        // the first parameter in the header list is the "drill down" parameter
        var drillDownParameterName = this.headerList[0];
        if ($.inArray(drillDownParameterName, parameters) != -1) {
            parameterMap[drillDownParameterName] = element.__data__.name;
        } else {
            console.log("Required drill down parameter '" + drillDownParameterName + "' not found in data of the node. Drill down aborted.");
            return;
        }

        runMetric(parameterMap);
    },

    /* transforms response json to D3-Treemap-JSON. */
    transformJson: function (data) {
        var result = {
            "name": "artifact",
            //if hide=true the node has a dimension of height=0 and width=0
            "hide": true,
            "children": null
        };

        var children = [];
        $.each(data, function (index, value) {
            children[index] = {
                "name": value.row[0],
                "size": value.row[1],
                // if there are only 2 return values then
                // color & size are equals
                "color": value.row[(value.row.length > 2 ? 2 : 1)],
                "hide": false
            };
        });
        result.children = children;
        return result;
    },
    /* creates a map with colors for a range of numbers. */
    createColorMap: function (data) {
        var min = 0;
        var max = 0;
        if (data.length > 0) {
            var useValue = (data[0].row.length > 2 ? 2 : 1);
            if (data.length > 0) {
                min = data[0].row[useValue];
                max = data[0].row[useValue];
            }

            for (var i = 0; i < data.length; i++) {
                var value = data[i].row[useValue];
                if (value > max) {
                    max = value;
                }
                if (value < min) {
                    min = value;
                }
            }
        }
        return d3.scale.linear().domain([min, (max + min) / 2, max])
            .range(["green", "yellow", "red"]);

    },
    showTooltip: function (event) {
        var element = event.currentTarget;
        if (element.__data__) {
            var tooltip = $("#tooltip");
            tooltip.find("#header0").text(this.headerList[0]);
            tooltip.find("#value0").text(element.__data__.name);
            tooltip.find("#header1").text(this.headerList[1]);
            tooltip.find("#value1").text(element.__data__.size);
            if (this.headerList.length > 2) {
                tooltip.find("#header2").text(this.headerList[2]);
                tooltip.find("#value2").text(element.__data__.color);
                tooltip.find("#row2").show();
            } else {
                tooltip.find("#row2").hide();
            }

            var bodyWidth = $(document).width();
            var tooltipWidth = tooltip.width();
            var left;
            if ((event.pageX + tooltipWidth) > bodyWidth) {
                left = event.pageX - (tooltipWidth + 20);
            } else {
                left = event.pageX + 10;
            }
            tooltip.css("left", left);

            var bodyHeight = $(document).height();
            var tooltipHeight = tooltip.height();
            var top;
            if ((event.pageY + tooltipHeight) > bodyHeight) {
                top = event.pageY - tooltipHeight - 10;
            } else {
                top = event.pageY + 10;

            }
            tooltip.css("top", top);

            tooltip.show();
        }
    },

    hideTooltip: function (event) {
        $("#tooltip").hide();
    }
};
