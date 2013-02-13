<%@page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <title>Review</title>
    <style>
        .active {
            background-color: yellow;
        }
    </style>
    <script type="text/javascript" src="js/jquery-1.9.js"></script>
    <script type="text/javascript">
        var revMap = new Object();
        var ctrPressed = false;

        function loadLog() {
            $('#log').html('Loading...');
            $.get('review/log', function (data) {
                var table = '<table border="1"><tr><th>Revision</th><th>Author</th><th>Date</th><th>Message</th></tr>';
                for (var i = 0; i < data.logentry.length; i++) {
                    var entry = data.logentry[i];
                    table += '<tr><td>' + entry.revision + '</td><td>' + entry.author + '</td><td>' + entry.date + '</td>' +
                            '<td>' + entry.msg + '</td><td><input type="button" value="->" onclick="showRevision(' + entry.revision + ');"/></td></tr>';
                    revMap[entry.revision] = entry;
                }
                $('#log').html(table + '</table>');
            });
        }

        function showRevision(revision) {
            $('#revision').html('<div class="row" action="' + revMap[revision].path[0].action + '" rev="' + revision + '" file="' + revMap[revision].path[0].value + '"  style="padding: 5px 0;">' + revMap[revision].path[0].value + ':' + revMap[revision].path[0].action + '</div>');
            for (var j = 1; j < revMap[revision].path.length; j++) {
                var path = revMap[revision].path[j];
                $('#revision').append('<div class="row" action="' + path.action + '" rev="' + revision + '" file="' + path.value + '" style="padding: 5px 0; border-top: dotted 1px;">' + path.value + ':' + path.action + '</div>');
            }
            $('.row').hover(function () {
                $(this).toggleClass('active');
            });
        }

        function showDiff(action, file, rev) {
            if ($('.active').length > 0) {
                if (action == 'A') {
                    $.get('review/cat?file=' + encodeURIComponent(file) + '&rev=' + rev, function (data) {
                        $('#left').html(data);
                    });
                } else if (action == 'D') {
                    $.get('review/cat?file=' + encodeURIComponent(file) + '&rev=' + rev, function (data) {
                        $('#right').html(data);
                    });
                } else {
                    var baseText;
                    var newText;
                    $.get('review/cat?file=' + encodeURIComponent(file) + '&rev=' + rev, function (data) {
                        newText = data;
                        $.get('review/cat?file=' + encodeURIComponent(file) + '&rev=' + (rev - 1), function (data) {
                            baseText = data;
                            diff(baseText, newText);
                        });
                    });
                }
            }
        }

        function diff(baseText, newText) {
            $('#left').html(baseText);
            $('#right').html(newText);
        }

        $(document).keydown(function (event) {
            if (event.keyCode == 17) ctrPressed = true;
            if (event.keyCode == 68 && ctrPressed) {
                showDiff($('.active').attr('action'), $('.active').attr('file'), $('.active').attr('rev'));
            }
        });

        $(document).keyup(function (event) {
            if (event.keyCode == 17) ctrPressed = false;
        });
    </script>
</head>
<body>

<div id="log" style="float: left;"></div>

<div id="revision" style="float: left; border: solid 1px; margin-left: 50px;"></div>

<div style="clear: both;"></div>

<textarea id="left" style="width: 600px; height: 600px;"></textarea>

<textarea id="right" style="width: 600px; height: 600px;"></textarea>

<input type="button" onclick="loadLog();"/>

</body>
</html>