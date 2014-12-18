jsPlumb
        .ready(function() {

            createMenu();

            var color = "lightgreen";

            var instance = jsPlumb.getInstance({
                // notice the 'curviness' argument to this Bezier curve. the
                // curves on this page are far smoother
                // than the curves on the first canvas, which use the default
                // curviness value.
                Connector : [ "Bezier", {
                    curviness : 50
                } ],
                DragOptions : {
                    cursor : "pointer",
                    zIndex : 2000
                },
                PaintStyle : {
                    strokeStyle : color,
                    lineWidth : 2
                },
                EndpointStyle : {
                    radius : 9,
                    fillStyle : color
                },
                HoverPaintStyle : {
                    strokeStyle : "#ec9f2e"
                },
                EndpointHoverStyle : {
                    fillStyle : "#ec9f2e"
                },
                Container : "container",
            });

            // suspend drawing and initialise.
            instance
                    .doWhileSuspended(function() {

                        $.post('', {
                            requestType : "nodes"
                        }, function(data) {
                                            
                                        var jobject = data;

                                            // var jobject = data;

                                            // creating the node-divs
                                            createNodes(jobject.levels);

                                            // declare some common values:
                                            var arrowCommon = {
                                                foldback : 0.7,
                                                fillStyle : color,
                                                width : 14
                                            },
                                            // use three-arg spec to create two
                                            // different arrows with the
                                            // common values:
                                            overlays = [ [ "Arrow", {
                                                location : 0.7
                                            }, arrowCommon ], [ "Arrow", {
                                                location : 0.3,
                                                direction : -1
                                            }, arrowCommon ] ];

                                            // TODO only endpoints where needed
                                            var windows = jsPlumb.getSelector(".window");

                                            $.each(windows, function() {
                                                instance.addEndpoint(this, {
                                                    uuid : this.getAttribute("id") + "-bottom",
                                                    anchor : "Bottom",
                                                    maxConnections : -1
                                                });

                                                instance.addEndpoint(this, {
                                                    uuid : this.getAttribute("id") + "-top",
                                                    anchor : "Top",
                                                    maxConnections : -1
                                                });
                                            })

                                            /*
                                             * Connect all nodes. This has to
                                             * happen after all the nodes have
                                             * been drawn
                                             */
                                            $.each(jobject.levels, function(key, obj) {

                                                $.each(obj.nodes, function(key, node) {

                                                    instance.connect({
                                                        uuids : [ node.desiredParentId + "-bottom",
                                                                node.agentId + "-top" ],
                                                        overlays : overlays
                                                    });
                                                });
                                            });

                                            instance.draggable(windows);
                                        });
                    });
            jsPlumb.fire("dataLoaded", instance);
});

function createMenu() {

    $.post('', {
        requestType : "menu"
    }, function(data) {

        var json = data;

        var li, ul, subLi;

        $.each(json.menu, function(key, obj) {

            li = createLi(obj.title);

            if (obj.items.length > 0) {
                ul = $("<ul>");

                $.each(obj.items, function(key, obj) {

                    subLi = createLi(obj.title);
                    subLi.addClass("subLi");
                    subLi.click(function() {
                        createNode(obj.fpid)
                    })

                    subLi.appendTo(ul);
                });

                ul.appendTo(li);
            }

            li.appendTo("#new-menu");
        });

        // turning the delete-button spans into buttons
        $(".subLi").button().on("click", function() {
            $("#dialog-form").dialog("open");
        });

    });
}

function createLi(text, sub) {
    return $("<li>", {}).append("<b>" + text + "</b>");
}

var nodeHeight = 66;
var nodewidth = 100;

// creating the node-divs
function createNodes(levels) {
    // creating the tree

    var top = 32;
    var vertDistance = nodeHeight * 2;
    var horiDistance = Math.round(nodewidth / 2);

    var centre = Math.round($("#container").width() / 2);

    var temp = top;

    $.each(levels.reverse(), function(key, level) {

        var temp = top + level.level * vertDistance;

        var nodesOnLevel = level.nodes.length;

        if (nodesOnLevel == 1) {
            createDiv(level.nodes[0], centre, temp);
        } else {

            var levelWidth = nodesOnLevel * nodewidth + (nodesOnLevel - 1) * horiDistance;
            
            var left = Math.round((centre + horiDistance) - (levelWidth / 2));

            $.each(level.nodes, function(key, node) {
                createDiv(node, left, temp);

                left += nodewidth + horiDistance;
            });
        }
    });

    initializeForm();
}

var count = 0;

function createDiv(node, left, top) {
    var nodeDiv = // creating the div
    $("<div/>", {
        id : node.agentId,
        class : "window"
    });

    var fpidString = node.fpid;
    // creating the div
    nodeDiv.append("<br /><b>" + fpidString.substring(fpidString.lastIndexOf(".") + 1) + "</b><br />" + node.agentId
            + "<br />");
    // inserting a span in the div
    nodeDiv.append($("<span/>", {
        class : "ui-icon ui-icon-pencil edit-button",
        click : function() {
            editNode(node.pid, node.fpid);
        },
        hover : "hover",
    }));
    nodeDiv.append($("<span/>", {
        id : node.pid,
        class : "ui-icon ui-icon-trash delete-button" + count,
        hover : "hover",
    }));

    nodeDiv.css("height", nodeHeight).css("width", nodewidth).css("left", left).css("top", top);

    // putting the div in the container
    nodeDiv.appendTo("#container");

    // turning the delete-button spans into buttons
    $(".delete-button" + count).button().on("click", function() {
        deleteNode(this.id);
    });
    
    count++;
}