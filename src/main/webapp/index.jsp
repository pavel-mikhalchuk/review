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

    .row {
        height: 14px;
        border-top: solid 1px white;
        border-bottom: solid 1px white;
    }

    .number {
        background-color: #f3f3f3;
    }

    .modified {
        background-color: aqua;
    }

    .added {
        background-color: #7cfc00;
    }

    .deleted {
        background-color: #a9a9a9;
    }

    .first {
        border-top: solid 1px brown;
    }

    .last {
        border-bottom: solid 1px brown;
    }
</style>
<script type="text/javascript" src="js/jquery-1.9.js"></script>
<script type="text/javascript" src="js/jquery.mousewheel.js"></script>
<script type="text/javascript">
var revMap = {};
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
            $.get('review/diff?file=' + encodeURIComponent(file) + '&rev=' + rev, function (data) {
                diff(data);
            })
        }
    }
}

var _diff;

var leftByLine = {}; //
var leftByRealLine = {}; //
var leftLineCount;
var leftFirst = 1;
var leftLast = 40;
var leftLineByRealLine = {};
var leftRealLineByLine = {};
var leftOver = 0;

var rightByLine = {}; //
var rightByRealLine = {}; //
var rightLineCount;
var rightFirst = 1;
var rightLast = 40;
var rightLineByRealLine = {};
var rightRealLineByLine = {};
var rightOver = 0;

var maxRow = 20;
var downOver = 6;

function diff(diff) {
    _diff = diff;

    var left = writeSide(diff.left);
    $('#left-content').html('<pre style="font-family: inherit; display: inline-block; margin: 0;">' + left.lines + '</pre>');
    $('#left-line').html('<pre style="margin: 0; color: #a52a2a;">' + left.numbers + '</pre>');
    leftLineCount = left.lineCount;
    leftLineByRealLine = left.lineByRealLine;
    leftRealLineByLine = left.realLineByLine;
    setLeftRow(1 - leftFirst);
    leftOver = 0;
    $('#left').unmousewheel(scrollLeft);
    $('#left').mousewheel(scrollLeft);

    var right = writeSide(diff.right);
    $('#right-content').html('<pre style="font-family: inherit; display: inline-block; margin: 0;">' + right.lines + '</pre>');
    $('#right-line').html('<pre style="margin: 0; color: #a52a2a;">' + right.numbers + '</pre>');
    rightLineCount = right.lineCount;
    rightLineByRealLine = right.lineByRealLine;
    rightRealLineByLine = right.realLineByLine;
    setRightRow(1 - rightFirst);
    rightOver = 0;
    $('#right').unmousewheel(scrollRight);
    $('#right').mousewheel(scrollRight);
}

function writeSide(lines) {
    var border = false;

    var lineByRealLine = {};
    var realLineByLine = {};
    var lineCount = 1;

    var line = lines[0];
    lineByRealLine['1'] = {'number': 1, 'scrolled': false};
    realLineByLine['1'] = {'number': 1, 'scrolled': false};

    var wl = writeLine(line, lineCount, false, !emptyLine(line) && (line.action == '+' || line.action == '-') ? 'first' : '');
    var linesHtml = wl.html;
    var numbersHtml = wl.number;
    lineCount++;

    for (var i = 1; i < lines.length; i++) {
        line = lines[i];
        lineByRealLine[lineCount] = {'number': i + 1, 'scrolled': false};
        realLineByLine[i + 1] = {'number': lineCount, 'scrolled': false};

        if (emptyLine(line)) border = true;
        else {
            var side = '';
            if (!emptyLine(line) && (line.action == '+' || line.action == '-')) {
                if (lines[i - 1].action != line.action) side = 'first';
                if (i == lines.length - 1 || lines[i + 1].action != line.action) side += ' last';
            }

            wl = writeLine(line, lineCount, border, side);
            linesHtml += wl.html;
            numbersHtml += wl.number;
            lineCount++;
            border = false;
        }
    }
    return { lines: linesHtml, numbers: numbersHtml, lineCount: lineCount - 1, lineByRealLine: lineByRealLine, realLineByLine: realLineByLine };
}

function emptyLine(line) {
    return (line.action == '+' && line.line == '') || (line.action == '-' && line.line == '');
}

function writeLine(line, number, border, firstOrLast) {
    if (line.action == '+') {
        return { html: '<div class="row added ' + firstOrLast + '">' + safeTags(line.line) + '</div>', number: '<div class="row number ' + firstOrLast + '">' + number + '</div>' };
    } else if (line.action == '-') {
        return { html: '<div class="row deleted ' + firstOrLast + '">' + safeTags(line.line) + '</div>', number: '<div class="row number ' + firstOrLast + '">' + number + '</div>' };
    } else if (line.action == '!') {
        return { html: '<div class="row modified first-last">' + safeTags(line.line) + '</div>', number: '<div class="row number first-last">' + number + '</div>' };
    }
    return { html: '<div class="row ' + (border == true ? ' first">' : ';">') + safeTags(line.line) + '</div>', number: '<div class="row number ' + (border == true ? ' first;">' : ' ;">') + number + '</div>' };
}

function safeTags(str) {
    return str.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
}

function scrollLeft(event, delta, deltaX, deltaY) {
    var rowsDelta = 0;

    if (deltaY < 0) {
        rowsDelta += 1;
    } else if (deltaY > 0) {
        rowsDelta += -1;
    }

    if (rowsDelta == 0 || leftFirst + rowsDelta <= 0 || leftLast + rowsDelta > leftLineCount + downOver) rowsDelta = 0;

    if (rowsDelta != 0) {
        if (leftOver != 0) {
            leftOver += rowsDelta;
        } else {
            if (rightLast + rowsDelta <= rightLineCount + downOver && rightFirst + rowsDelta > 0) {
                if (rowsDelta > 0) {
                    for (var row = leftFirst; row <= leftFirst + maxRow; row++) {
                        if (rightRealLineByLine[leftLineByRealLine[row].number].number == rightRealLineByLine[leftLineByRealLine[row].number + 1].number) {
                            if (leftLineByRealLine[row].scrolled == false) {
                                leftLineByRealLine[row].scrolled = true;
                                row = -1;
                                break;
                            }
                        }
                    }
                    if (row != -1) setRightRow(rowsDelta);
                } else {
                    for (var row = leftLast; row >= leftFirst + maxRow - 1; row--) {
                        if (row <= leftLineCount - downOver) {
                            if (rightRealLineByLine[leftLineByRealLine[row]] == rightRealLineByLine[leftLineByRealLine[row] - 1]) {
                                if (leftLineByRealLine[row].scrolled == true) {
                                    leftLineByRealLine[row].scrolled = false;
                                    row = -1;
                                    break;
                                }
                            }
                        }
                    }
                    if (row != -1) setRightRow(rowsDelta);
                }
            } else {
                leftOver += rowsDelta;
            }
        }
        setLeftRow(rowsDelta);
    }

    $('#left-out').html('~~~~~' + ':' + leftFirst + ':' + leftLast + ':' + leftLineCount + ";  " + leftOver);

    event.preventDefault();
}

function setLeftRow(rowsDelta) {
    if (rowsDelta == 0) return;

    var c = $('#left-content').offset();
    var l = $('#left-line').offset();

    $('#left-content').offset({top: c.top - 16 * rowsDelta, left: c.left});
    $('#left-line').offset({top: l.top - 16 * rowsDelta, left: l.left});

    leftFirst += rowsDelta;
    leftLast += rowsDelta;
}

function scrollRight(event, delta, deltaX, deltaY) {
    var rowsDelta = 0;

    if (deltaY < 0) {
        rowsDelta += 1;
    } else if (deltaY > 0) {
        rowsDelta += -1;
    }

    if (rowsDelta == 0 || rightFirst + rowsDelta <= 0 || rightLast + rowsDelta > rightLineCount + downOver) rowsDelta = 0;

    if (rowsDelta != 0) {
        if (rightOver != 0) {
            rightOver += rowsDelta;
        } else {
            if (leftLast + rowsDelta <= leftLineCount + downOver && leftFirst + rowsDelta > 0) {
                if (rowsDelta > 0) {
                    for (var row = rightFirst; row <= rightFirst + maxRow; row++) {
                        if (leftRealLineByLine[rightLineByRealLine[row].number].number == leftRealLineByLine[rightLineByRealLine[row].number + 1].number) {
                            if (rightLineByRealLine[row].scrolled == false) {
                                rightLineByRealLine[row].scrolled = true;
                                row = -1;
                                break;
                            }
                        }
                    }
                    if (row != -1) setLeftRow(rowsDelta);
                } else {
                    for (var row = rightLast; row >= rightFirst + maxRow - 1; row--) {
                        if (row <= rightLineCount - downOver) {
                            if (leftRealLineByLine[rightLineByRealLine[row]] == leftRealLineByLine[rightLineByRealLine[row] - 1]) {
                                if (rightLineByRealLine[row].scrolled == true) {
                                    rightLineByRealLine[row].scrolled = false;
                                    row = -1;
                                    break;
                                }
                            }
                        }
                    }
                    if (row != -1) setLeftRow(rowsDelta);
                }
            } else {
                rightOver += rowsDelta;
            }
        }
        setRightRow(rowsDelta);
    }

    $('#right-out').html('~~~~~' + ':' + rightFirst + ':' + rightLast + ':' + rightLineCount + ';   ' + rightOver);

    event.preventDefault();
}

function setRightRow(rowsDelta) {
    if (rowsDelta == 0) return;

    var c = $('#right-content').offset();
    var r = $('#right-line').offset();

    $('#right-content').offset({top: c.top - 16 * rowsDelta, left: c.left});
    $('#right-line').offset({top: r.top - 16 * rowsDelta, left: r.left});

    rightFirst += rowsDelta;
    rightLast += rowsDelta;
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

function loadTestDiff() {
    $.get('review/diff?file=' + encodeURIComponent('/app-framework/trunk/app-core/src/main/java/com/shc/obu/app/framework/flow/ItemSearchIncrementalIndexFlow.java') + '&rev=' + '124714', function (data) {
        diff(data);
    })
}
</script>
</head>
<body style="margin: 4px;">

<div style="font-size: 9px;">

    <div id="log" style="float: left;"></div>

    <div id="revision" style="float: left; border: solid 1px; margin-left: 50px;"></div>

</div>

<div style="clear: both; margin-bottom: 4px;"></div>

<div id="diff" style="width: 1342px; height: 600px;">

    <div id="left" style="width: 654px; height: 640px; float: left; border: solid 1px #acacac; overflow: hidden;">
        <div id="left-scroll"
             style="width: 20px; height: 640px; background-color: #f3f3f3; float: left; border-right: solid 1px #acacac;"></div>
        <div id="left-line"
             style="float: right; background-color: #f3f3f3; border-left: dotted 1px #acacac; direction: rtl;"></div>
        <div style="width: 30px; height: 640px; background-color: #f3f3f3; float: right; border-left: dotted 1px #acacac;"></div>
        <div id="left-content" style="overflow: hidden;"></div>
    </div>

    <div id="right"
         style="width: 654px; height: 640px; float: left; border: solid 1px #acacac; overflow: hidden; margin-left: 30px;">
        <div id="right-line"
             style="background-color: #f3f3f3; float: left; border-right: dotted 1px #acacac; direction: rtl;"></div>
        <div style="width: 30px; height: 640px; background-color: #f3f3f3; float: left; border-right: dotted 1px #acacac;"></div>
        <div id="right-scroll"
             style="width: 20px; height: 640px; background-color: #f3f3f3; float: right; border-left: solid 1px #acacac;"></div>
        <div id="right-content" style="overflow: hidden;"></div>
    </div>

</div>

<div>
    <div id="left-out" style="width: 656px; float: left;">left-out</div>
    <div id="right-out" style="width: 656px; float: left; margin-left: 30px;">right-out</div>
</div>

<input type="button" onclick="loadLog();"/>
<input type="button" onclick="loadTestDiff();"/>

</body>
</html>